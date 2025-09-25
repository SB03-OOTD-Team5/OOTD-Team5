package com.sprint.ootd5team.base.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageCreateRequest;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private static final String SUB_PREFIX = "/sub/direct-messages_";
	private static final String PUB_SEND   = "/pub/direct-messages_send";

	private final DirectMessageRoomRepository roomRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (acc == null) return message;

		// 반복되는 로그 줄이기
		if (acc.getMessageType() != SimpMessageType.HEARTBEAT) {
			log.debug("[STOMP {}] type={}, dest={}, user={}",
				acc.getCommand(), acc.getDestination(), (acc.getUser()!=null?acc.getUser().getName():"-"),
				acc.getUser());
		}

		StompCommand cmd = acc.getCommand();
		if (cmd == null) return message;

		switch (cmd) {
			case CONNECT -> handleConnect(acc);
			case SUBSCRIBE -> handleSubscribe(acc);
			case SEND      -> handleSend(acc, message);
			default -> { /* NOP */ }
		}
		return message;
	}

	// DM 채팅방 참여자격 인증
	private void handleSubscribe(StompHeaderAccessor acc) {
		String dest = acc.getDestination();
		if (dest == null || !dest.startsWith(SUB_PREFIX)) return; // 잘못된 접근

		String dmKey = dest.substring(SUB_PREFIX.length());
		UUID me = extractUserId(requireAuth(acc));
		DirectMessageRoom room = roomRepository.findByDmKey(dmKey)
			.orElseThrow(() -> new AccessDeniedException("채팅방을 찾을 수 없습니다."));

		boolean member = Objects.equals(me, room.getUser1Id()) || Objects.equals(me, room.getUser2Id());
		if (!member) {
			throw new AccessDeniedException("해당 채팅방의 구독 권한이 없습니다.");
		}
	}

	private void handleSend(StompHeaderAccessor acc, Message<?> message) {
		String dest = acc.getDestination();
		if (dest == null || !dest.equals(PUB_SEND)) return; // 다른 발행은 패스(필요시 화이트리스트 확장)

		UUID me = extractUserId(requireAuth(acc));

		// payload(JSON) 파싱
		String json = payloadAsString(message.getPayload());
		DirectMessageCreateRequest req;
		try {
			req = objectMapper.readValue(json, DirectMessageCreateRequest.class);
		} catch (Exception e) {
			throw new AccessDeniedException("잘못된 메시지 포맷입니다.");
		}

		// senderId 위조 방지: payload의 senderId는 반드시 현재 사용자와 같아야 함
		if (req.senderId() == null || !req.senderId().equals(me)) {
			throw new AccessDeniedException("senderId가 로그인 사용자와 일치하지 않습니다.");
		}
		if (req.receiverId() == null) throw new AccessDeniedException("receiverId는 null일 수 없습니다.");
	}
	/**
	 * CONNECT 단계에서 Authorization을 읽어 access 토큰을 검증하고,
	 * 검증되면 UsernamePasswordAuthenticationToken을 acc.setUser(...)로 설정한다.
	 */
	private void handleConnect(StompHeaderAccessor acc) {
		String token = extractAccessToken(acc);
		if (token == null) {
			throw new AccessDeniedException("Authorization token is missing.");
		}
		if (!jwtTokenProvider.validateAccessToken(token)) {
			throw new AccessDeniedException("Invalid or expired access token.");
		}

		// 토큰에서 UserId 추출
		UUID userId = jwtTokenProvider.getUserId(token);

		// (선택) 존재하는 사용자만 허용하고 싶다면 미리 확인
		userRepository.findById(userId)
			.orElseThrow(() -> new AccessDeniedException("User not found"));

		// 권한은 굳이 필요 없으면 빈 리스트로.
		Authentication auth =
			new UsernamePasswordAuthenticationToken(userId.toString(), "N/A", List.of());
		acc.setUser(auth);

		log.debug("[STOMP CONNECT] authenticated as {}", userId);
	}

	// ==== 공용 헬퍼 ====

	private Authentication requireAuth(StompHeaderAccessor acc) {
		if (acc.getUser() instanceof Authentication auth && auth.isAuthenticated()) {
			return auth;
		}
		throw new AccessDeniedException("인증되지 않은 연결입니다.");
	}

	private UUID extractUserId(Authentication auth) {
		// principal 이 UUID 문자열로 세팅된 형태를 우선 지원
		Object principal = auth.getPrincipal();
		if (principal instanceof String s) {
			try { return UUID.fromString(s); }
			catch (Exception ignored) {}
		}
		// 혹시 모를 커스텀 UserDetails 지원 (테스트/이관 대비)
		if (principal instanceof OotdUserDetails ud) return ud.getUserId();

		// 마지막 폴백: auth.getName()이 UUID 문자열이어야만 허용
		try { return UUID.fromString(auth.getName()); }
		catch (Exception e) {
			throw new AccessDeniedException("인증 주체가 UUID 형식이 아닙니다.");
		}
	}
	private String payloadAsString(Object payload) {
		if (payload instanceof byte[] bytes) {
			return new String(bytes, StandardCharsets.UTF_8);
		}
		return String.valueOf(payload);
	}
	/**
	 * CONNECT에서 토큰을 읽는다.
	 * 우선순위: nativeHeader.Authorization → 핸드셰이크 세션(HEADER) → 핸드셰이크 세션(QUERY)
	 * 값이 'Bearer xxx'면 'xxx'만 반환.
	 */
	private String extractAccessToken(StompHeaderAccessor acc) {
		// 1) STOMP native header
		String h = firstNativeHeader(acc, "Authorization");
		if (hasText(h)) return stripBearer(h);

		// 2) Handshake attributes (HttpHandshakeAuthInterceptor가 저장)
		Map<String, Object> attrs = acc.getSessionAttributes();
		if (attrs != null) {
			Object authHeader = attrs.get(StompConfig.ATTR_HTTP_AUTHORIZATION);
			if (authHeader instanceof String s && hasText(s)) return stripBearer(s);

			Object queryToken = attrs.get(StompConfig.ATTR_QUERY_ACCESS_TOKEN);
			if (queryToken instanceof String s && hasText(s)) return s;
		}
		return null;
	}

	private static String firstNativeHeader(StompHeaderAccessor acc, String name) {
		List<String> values = acc.getNativeHeader(name);
		return (values != null && !values.isEmpty()) ? values.get(0) : null;
	}

	private static boolean hasText(String s) {
		return s != null && !s.trim().isEmpty();
	}

	private static String stripBearer(String v) {
		String s = v.trim();
		if (s.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return s.substring(7).trim();
		}
		return s;
	}
}
