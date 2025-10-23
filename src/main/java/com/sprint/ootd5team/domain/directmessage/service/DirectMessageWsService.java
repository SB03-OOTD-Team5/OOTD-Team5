package com.sprint.ootd5team.domain.directmessage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageCreateRequest;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.dto.ParticipantDto;
import com.sprint.ootd5team.domain.directmessage.dto.event.DirectMessageCommittedEvent;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRepository;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import com.sprint.ootd5team.domain.notification.event.type.single.DmCreatedEvent;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.base.exception.directmessage.DirectMessageAccessDeniedException;
import com.sprint.ootd5team.base.exception.directmessage.DirectMessageRoomCreationFailedException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageWsService {

	private final ObjectMapper objectMapper;                         // 문자열을 JSON으로 파싱
	private final DirectMessageRepository messageRepository;         // 영속화
	private final DirectMessageRoomRepository roomRepository;        // 방 조회/생성
	private final UserRepository userRepository;                     // 이름 맵핑 보강
	private final ApplicationEventPublisher eventPublisher;           // 구독 브로드캐스트 이벤트 발행
	private final ProfileRepository profileRepository;

	//방 조회시 Username,profileUrl 캐시
	private final Cache<UUID, String> userNameCache;
	private final Cache<UUID, Optional<String>> profileUrlCache;
	private final Cache<String, DirectMessageRoom> roomCache;

	@Transactional
	public void handleSend(String payload) throws Exception {
		// 1) JSON 파싱 (DirectMessageCreateRequest: receiverId, senderId, content)
		DirectMessageCreateRequest req = objectMapper.readValue(payload, DirectMessageCreateRequest.class);
		log.debug("[WebSocket DM Service] payload JSON으로 파싱됨: receiverId={}, contentLen={}",
			req.receiverId(), (req.content() == null ? 0 : req.content().length()));

		// 2) dmKey계산, 방 조회 또는 생성
		DirectMessageRoom room = getOrCreateRoomCached(req.senderId(), req.receiverId());
		log.debug("[WebSocket DM Service] 채팅방 연결됨 : roomId={}", room.getId());

		// 3) 올바른 이용자 검증
		boolean member = Objects.equals(req.senderId(), room.getUser1Id()) || Objects.equals(req.senderId(), room.getUser2Id());
		if (!member) throw DirectMessageAccessDeniedException.notParticipant(room.getId(), req.senderId());


		// 4) 메시지 저장
		DirectMessage saved = messageRepository.save(DirectMessage.builder()
			.directMessageRoom(room)
			.senderId(req.senderId())
			.content(req.content())
			.build());
		log.debug("[WebSocket DM Service] 서버에 메세지 저장 : id={}, len={}",
			saved.getId(), (saved.getContent() == null ? 0 : saved.getContent().length()));

		// 5) 송/수신자 ParticipantDto 구성
		ParticipantDto sender = toParticipant(req.senderId());
		ParticipantDto receiver = toParticipant(resolveReceiverId(room, req.senderId()));

		// 6) 발행 Dto 구성: 기본 DTO(id, createdAt, content) 매핑 후 sender/receiver 보강
		DirectMessageDto dto = DirectMessageDto.builder()
			.id(saved.getId())
			.createdAt(saved.getCreatedAt())
			.sender(sender)
			.receiver(receiver)
			.content(saved.getContent())
			.build();

		// 7) 구독 채널로 브로드캐스트
		String destination = "/topic/direct-messages_" + room.getDmKey();
		eventPublisher.publishEvent(new DirectMessageCommittedEvent(destination, dto));

		// 8) 알림 이벤트 발행
		eventPublisher.publishEvent(new DmCreatedEvent(dto));
	}

	// ===== 내부 헬퍼 =====

	private ParticipantDto toParticipant(UUID userId) {
		if (userId == null) {
			return ParticipantDto.builder().userId(null).name("탈퇴한 사용자").profileImageUrl(null).build();
		}
		String name = userNameCache.get(userId, id ->
			userRepository.findById(id).map(User::getName).orElse("탈퇴한 사용자")
		);
		Optional<String> profileUrl = profileUrlCache.get(userId, id ->
			profileRepository.findByUserId(id)
				.map(Profile::getProfileImageUrl)
		);
		return ParticipantDto.builder().userId(userId).name(name).profileImageUrl(profileUrl.orElse(null)).build();
	}


	private UUID resolveReceiverId(DirectMessageRoom room, UUID senderId) {
		UUID u1 = room.getUser1Id(), u2 = room.getUser2Id();
		if (u1 == null && u2 == null) return null;       // 방에 유저 없음(이례적)
		if (senderId == null) return (u1 != null ? u1 : u2);
		if (Objects.equals(senderId, u1)) return u2;
		if (Objects.equals(senderId, u2)) return u1;
		return (u1 != null ? u1 : u2);                   // senderId가 둘 중 하나가 아니면 fallback
	}

	private DirectMessageRoom getOrCreateRoomCached(UUID senderId, UUID receiverId) {
		String dmKey = dmKeyOf(senderId, receiverId);
		log.debug("[WebSocket DM Service] DM 키 조합성공: dmKey={}",dmKey);
		return roomCache.get(dmKey, key -> loadOrCreateRoom(key, senderId, receiverId));
	}

	private DirectMessageRoom loadOrCreateRoom(String dmKey, UUID senderId, UUID receiverId) {
		return roomRepository.findByDmKey(dmKey)
			.orElseGet(() -> createRoomSafely(dmKey, senderId, receiverId));
	}
	private String dmKeyOf(UUID a, UUID b) {
		String s1 = a.toString(), s2 = b.toString();
		return (s1.compareTo(s2) <= 0) ? (s1 + "_" + s2) : (s2 + "_" + s1);
	}

	// UNIQUE(dm_key) 가정: 경합 시 재조회
	private DirectMessageRoom createRoomSafely(String dmKey, UUID a, UUID b) {
		try {
			return roomRepository.save(DirectMessageRoom.builder()
				.dmKey(dmKey)
				.user1Id(min(a, b))
				.user2Id(max(a, b))
				.build());
		} catch (DataIntegrityViolationException e) {
			// 동시 생성 충돌 → 기존 것을 재조회
			return roomRepository.findByDmKey(dmKey)
				.orElseThrow(() -> DirectMessageRoomCreationFailedException.withDmKey(dmKey, a, b, e));
		}
	}
	private UUID min(UUID a, UUID b) { return (a.toString().compareTo(b.toString()) <= 0) ? a : b; }
	private UUID max(UUID a, UUID b) { return (a.toString().compareTo(b.toString()) <= 0) ? b : a; }
}
