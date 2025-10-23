package com.sprint.ootd5team.base.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.directmessage.DirectMessageRoomCreationFailedException;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdSecurityUserDetails;
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
import org.springframework.dao.DataIntegrityViolationException;
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

	private static final String CLIENT_SUB_PREFIX = "/sub/direct-messages_";
	private static final String BROKER_TOPIC_PREFIX = "/topic/direct-messages_";
	private static final String PUB_SEND   = "/pub/direct-messages_send";

	private final DirectMessageRoomRepository roomRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null) {
			return message;
		}

		if (accessor.getMessageType() != SimpMessageType.HEARTBEAT) {
			log.debug("[STOMP {}] dest={}, user={}",
				accessor.getCommand(), accessor.getDestination(),
				accessor.getUser() != null ? accessor.getUser().getName() : "-");
		}

		StompCommand command = accessor.getCommand();
		if (command == null) {
			return message;
		}

		switch (command) {
			case CONNECT -> handleConnect(accessor);
			case SUBSCRIBE -> handleSubscribe(accessor);
			case SEND -> handleSend(accessor, message);
			default -> { }
		}
		return message;
	}

	private void handleSubscribe(StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();
		if (destination == null || !destination.startsWith(CLIENT_SUB_PREFIX)) {
			return;
		}

		String dmKey = destination.substring(CLIENT_SUB_PREFIX.length());
		String brokerDestination = BROKER_TOPIC_PREFIX + dmKey;
		accessor.setDestination(brokerDestination);

		UUID currentUser = extractUserId(requireAuth(accessor));
		UUID[] members = parseDmKey(dmKey);

		if (!Objects.equals(currentUser, members[0]) && !Objects.equals(currentUser, members[1])) {
			throw new AccessDeniedException("구독 권한이 없는 채팅방입니다.");
		}

		ensureRoomExists(dmKey, members[0], members[1]);
	}

	private void handleSend(StompHeaderAccessor accessor, Message<?> message) {
		String destination = accessor.getDestination();
		if (destination == null) {
			return;
		}
		if (destination.startsWith(CLIENT_SUB_PREFIX)) {
			String dmKey = destination.substring(CLIENT_SUB_PREFIX.length());
			accessor.setDestination(BROKER_TOPIC_PREFIX + dmKey);
		}
		if (!PUB_SEND.equals(accessor.getDestination())) {
			return;
		}

		UUID currentUser = extractUserId(requireAuth(accessor));
		String json = payloadAsString(message.getPayload());
		DirectMessageCreateRequest request;
		try {
			request = objectMapper.readValue(json, DirectMessageCreateRequest.class);
		} catch (Exception e) {
			throw new AccessDeniedException("메시지 형식을 파싱할 수 없습니다.");
		}

		if (request.senderId() == null || !request.senderId().equals(currentUser)) {
			throw new AccessDeniedException("현재 사용자와 다른 senderId가 전달되었습니다.");
		}
		if (request.receiverId() == null) {
			throw new AccessDeniedException("receiverId는 필수입니다.");
		}
	}

	private void handleConnect(StompHeaderAccessor accessor) {
		String token = extractAccessToken(accessor);
		if (token == null) {
			throw new AccessDeniedException("Authorization token is missing.");
		}
		if (!jwtTokenProvider.validateAccessToken(token)) {
			throw new AccessDeniedException("Invalid or expired access token.");
		}

		UUID userId = jwtTokenProvider.getUserId(token);
		userRepository.findById(userId)
			.orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다."));

		Authentication authentication =
			new UsernamePasswordAuthenticationToken(userId.toString(), "N/A", List.of());
		accessor.setUser(authentication);

		log.debug("[STOMP CONNECT] authenticated as {}", userId);
	}

	private Authentication requireAuth(StompHeaderAccessor accessor) {
		if (accessor.getUser() instanceof Authentication authentication && authentication.isAuthenticated()) {
			return authentication;
		}
		throw new AccessDeniedException("인증되지 않은 연결입니다.");
	}

	private UUID extractUserId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof String asString) {
			try {
				return UUID.fromString(asString);
			} catch (Exception ignored) { }
		}
		if (principal instanceof OotdSecurityUserDetails details) {
			return details.getUserId();
		}
		try {
			return UUID.fromString(authentication.getName());
		} catch (Exception e) {
			throw new AccessDeniedException("인증 주체가 UUID 형식이 아닙니다.");
		}
	}

	private String payloadAsString(Object payload) {
		if (payload instanceof byte[] bytes) {
			return new String(bytes, StandardCharsets.UTF_8);
		}
		return String.valueOf(payload);
	}

	private String extractAccessToken(StompHeaderAccessor accessor) {
		String header = firstNativeHeader(accessor, "Authorization");
		if (hasText(header)) {
			return stripBearer(header);
		}

		Map<String, Object> attributes = accessor.getSessionAttributes();
		if (attributes != null) {
			Object httpHeader = attributes.get(StompConfig.ATTR_HTTP_AUTHORIZATION);
			if (httpHeader instanceof String s && hasText(s)) {
				return stripBearer(s);
			}
			Object queryToken = attributes.get(StompConfig.ATTR_QUERY_ACCESS_TOKEN);
			if (queryToken instanceof String s && hasText(s)) {
				return s;
			}
		}
		return null;
	}

	private static String firstNativeHeader(StompHeaderAccessor accessor, String name) {
		List<String> values = accessor.getNativeHeader(name);
		return (values != null && !values.isEmpty()) ? values.get(0) : null;
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static String stripBearer(String value) {
		String trimmed = value.trim();
		if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return trimmed.substring(7).trim();
		}
		return trimmed;
	}

	private UUID[] parseDmKey(String dmKey) {
		String[] tokens = dmKey.split("_");
		if (tokens.length != 2) {
			throw new AccessDeniedException("잘못된 채팅방 식별자입니다.");
		}
		try {
			return new UUID[]{UUID.fromString(tokens[0]), UUID.fromString(tokens[1])};
		} catch (IllegalArgumentException e) {
			throw new AccessDeniedException("잘못된 채팅방 식별자입니다.");
		}
	}

	private void ensureRoomExists(String dmKey, UUID a, UUID b) {
		UUID user1 = min(a, b);
		UUID user2 = max(a, b);

		DirectMessageRoom existing = roomRepository.findByDmKey(dmKey).orElse(null);
		if (existing != null) {
			boolean matches = Objects.equals(existing.getUser1Id(), user1)
				&& Objects.equals(existing.getUser2Id(), user2);
			if (!matches) {
				throw new AccessDeniedException("채팅방 정보가 일치하지 않습니다.");
			}
			return;
		}

		userRepository.findById(user1)
			.orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다."));
		if (!Objects.equals(user1, user2)) {
			userRepository.findById(user2)
				.orElseThrow(() -> new AccessDeniedException("사용자 정보를 찾을 수 없습니다."));
		}

		try {
			roomRepository.save(DirectMessageRoom.builder()
				.dmKey(dmKey)
				.user1Id(user1)
				.user2Id(user2)
				.build());
		} catch (DataIntegrityViolationException e) {
			roomRepository.findByDmKey(dmKey)
				.orElseThrow(() -> DirectMessageRoomCreationFailedException.withDmKey(dmKey, a, b, e));
		}
	}

	private UUID min(UUID a, UUID b) {
		return a.toString().compareTo(b.toString()) <= 0 ? a : b;
	}

	private UUID max(UUID a, UUID b) {
		return a.toString().compareTo(b.toString()) <= 0 ? b : a;
	}
}
