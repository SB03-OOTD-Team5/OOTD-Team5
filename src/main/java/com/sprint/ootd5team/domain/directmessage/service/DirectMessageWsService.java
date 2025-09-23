package com.sprint.ootd5team.domain.directmessage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageCreateRequest;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.dto.ParticipantDto;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.mapper.DirectMessageMapper;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRepository;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectMessageWsService {

	private final ObjectMapper objectMapper;                         // 문자열을 JSON으로 파싱
	private final SimpMessagingTemplate messagingTemplate;           // 구독 브로드캐스트
	private final DirectMessageRepository messageRepository;         // 영속화
	private final DirectMessageRoomRepository roomRepository;        // 방 조회/생성
	private final UserRepository userRepository;                     // 이름 맵핑 보강
	private final DirectMessageMapper mapper;
	private final ProfileRepository profileRepository;

	@Transactional
	public void handleSend(String payload) throws Exception {
		// 1) JSON 파싱 (DirectMessageCreateRequest: receiverId, senderId, content)
		DirectMessageCreateRequest req = objectMapper.readValue(payload, DirectMessageCreateRequest.class);
		log.debug("[WebSocket DM Service] payload JSON으로 파싱됨: {}", req);

		// 2) dmKey 계산 → 방 조회 또는 생성
		String dmKey = dmKeyOf(req.senderId(), req.receiverId());
		DirectMessageRoom room = roomRepository.findByDmKey(dmKey)
			.orElseGet(() -> roomRepository.save(DirectMessageRoom.builder()
				.dmKey(dmKey)
				.user1Id(min(req.senderId(), req.receiverId()))
				.user2Id(max(req.senderId(), req.receiverId()))
				.build()));
		log.debug("[WebSocket DM Service] DM 키 생성, 채팅방 개설됨 : dmKey={}", dmKey);

		// 3) 메시지 저장
		DirectMessage saved = messageRepository.save(DirectMessage.builder()
			.directMessageRoom(room)
			.senderId(req.senderId())
			.content(req.content())
			.build());
		log.debug("[WebSocket DM Service] 서버에 메세지 저장됨 : content={}", saved.getContent());

		// 4) 참여자 정보/프로필 한 번에 준비(방의 두 명만)
		Map<UUID, User> users = loadUsers(room);
		Map<UUID, String> profileUrlByUserId = loadProfileUrls(room);

		// 5) 송/수신자 ParticipantDto 구성
		UUID receiverId = resolveReceiverId(room, saved.getSenderId());
		ParticipantDto sender   = toParticipant(saved.getSenderId(), users, profileUrlByUserId);
		ParticipantDto receiver = toParticipant(receiverId,         users, profileUrlByUserId);

		// 6) 엔티티 -> 기본 DTO(id, createdAt, content) 매핑 후 sender/receiver 보강
		DirectMessageDto base = mapper.toDto(saved);
		DirectMessageDto dto = DirectMessageDto.builder()
			.id(base.id())
			.createdAt(base.createdAt())
			.content(base.content())
			.sender(sender)
			.receiver(receiver)
			.build();

		// 5) 구독 채널로 브로드캐스트
		String destination = "/sub/direct-messages_" + room.getDmKey();
		messagingTemplate.convertAndSend(destination, dto);
		log.debug("[WebSocket DM Service] 구독자에게 브로드캐스트: destination={}", destination);
	}

	// ===== 내부 헬퍼 =====

	private Map<UUID, User> loadUsers(DirectMessageRoom room) {
		Set<UUID> ids = new HashSet<>();
		if (room.getUser1Id() != null) ids.add(room.getUser1Id());
		if (room.getUser2Id() != null) ids.add(room.getUser2Id());
		return userRepository.findAllById(ids).stream()
			.collect(Collectors.toMap(User::getId, u -> u));
	}

	private Map<UUID, String> loadProfileUrls(DirectMessageRoom room) {
		Map<UUID, String> map = new HashMap<>();
		UUID u1 = room.getUser1Id();
		UUID u2 = room.getUser2Id();
		if (u1 != null) profileRepository.findByUserId(u1).ifPresent(p -> map.put(u1, p.getProfileImageUrl()));
		if (u2 != null) profileRepository.findByUserId(u2).ifPresent(p -> map.put(u2, p.getProfileImageUrl()));
		return map;
	}

	private ParticipantDto toParticipant(UUID userId, Map<UUID, User> users, Map<UUID, String> profileUrlByUserId) {
		// 탈퇴/NULL 사용자는 공통 포맷으로 반환
		if (userId == null) {
			return ParticipantDto.builder()
				.userId(null)
				.name("탈퇴한 사용자")
				.profileImageUrl(null)
				.build();
		}
		User u = users.get(userId);                 // 이름
		String url = profileUrlByUserId.get(userId); // 프로필 URL
		return ParticipantDto.builder()
			.userId(userId)
			.name(u != null ? u.getName() : "탈퇴한 사용자")
			.profileImageUrl(u != null ? url : null)
			.build();
	}

	private String dmKeyOf(UUID a, UUID b) {
		String s1 = a.toString(), s2 = b.toString();
		return (s1.compareTo(s2) <= 0) ? (s1 + "_" + s2) : (s2 + "_" + s1);
	}
	private UUID min(UUID a, UUID b) { return (a.toString().compareTo(b.toString()) <= 0) ? a : b; }
	private UUID max(UUID a, UUID b) { return (a.toString().compareTo(b.toString()) <= 0) ? b : a; }

	private UUID resolveReceiverId(DirectMessageRoom room, UUID senderId) {
		UUID u1 = room.getUser1Id(), u2 = room.getUser2Id();
		if (u1 == null && u2 == null) return null;       // 방에 유저 없음(이례적)
		if (senderId == null) return (u1 != null ? u1 : u2);
		if (Objects.equals(senderId, u1)) return u2;
		if (Objects.equals(senderId, u2)) return u1;
		return (u1 != null ? u1 : u2);                   // senderId가 둘 중 하나가 아니면 fallback
	}
}
