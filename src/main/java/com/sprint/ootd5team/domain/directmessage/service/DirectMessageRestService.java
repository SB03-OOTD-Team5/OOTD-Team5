package com.sprint.ootd5team.domain.directmessage.service;

import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.sprint.ootd5team.domain.directmessage.dto.ParticipantDto;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRepository;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DirectMessageRestService {

	private static final int MAX_LIMIT = 100;

	private final DirectMessageRepository messageRepository;
	private final DirectMessageRoomRepository roomRepository;
	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;

	/**
	 * 대화상대와의 DM 히스토리 페이지네이션(최신→과거 DESC).
	 * 프론트 파라미터: userId(partner), cursor(optional, Instant), idAfter(optional), limit
	 */
	public DirectMessageDtoCursorResponse listByPartner(UUID partnerUserId,
		String cursor, UUID idAfter, int limit) {
		log.debug("[REST DM Service] ReceiverId로 대화내역 조회 시작: Id={}", partnerUserId);

		// 로그인 사용자로부터 SenderId추출
		UUID senderId = currentUserId();

		// DmKey 계산
		String dmKey = dmKeyOf(senderId, partnerUserId);
		// Dm키로 대화방 조회
		Optional<DirectMessageRoom> maybeRoom = roomRepository.findByDmKey(dmKey);
		if (maybeRoom.isEmpty()) {
			return DirectMessageDtoCursorResponse.builder()
				.data(List.of())
				.nextCursor(null)
				.nextIdAfter(null)
				.hasNext(false)
				.totalCount(0)
				.sortBy("createdAt")
				.sortDirection("DESCENDING")
				.build();
		}
		DirectMessageRoom room = maybeRoom.get();

		// 대화 참여자 검증
		if (!Objects.equals(room.getUser1Id(), senderId) && !Objects.equals(room.getUser2Id(), senderId)) {
			throw new AccessDeniedException("잘못된 사용자입니다.");
		}

		// 커서 파싱(등록시간역순으로 MAX_LIMIT까지)
		Instant beforeCreatedAt = parseCursor(cursor);
		int size = Math.max(1, Math.min(limit, MAX_LIMIT));
		Pageable pageable = PageRequest.of(0, size + 1);

		// 페이지 쿼리
		List<DirectMessage> rows;
		if (beforeCreatedAt == null) {
			rows = messageRepository.firstPageDesc(room.getId(), pageable);
		} else if (idAfter == null) {
			rows = messageRepository.pageBeforeCreatedAtDesc(room.getId(), beforeCreatedAt, pageable);
		} else {
			rows = messageRepository.pageBeforeDesc(room.getId(), beforeCreatedAt, idAfter, pageable);
		}

		boolean hasNext = rows.size() > size;
		if (hasNext) rows = rows.subList(0, size);

		if (rows.isEmpty()) {
			return DirectMessageDtoCursorResponse.builder()
				.data(List.of())
				.nextCursor(null)
				.nextIdAfter(null)
				.hasNext(false)
				.totalCount(messageRepository.countByDirectMessageRoom_Id(room.getId()))
				.sortBy("createdAt")
				.sortDirection("DESCENDING")
				.build();
		}


		//  N+1 방지: 참여자 정보/프로필 일괄 로드
		Set<UUID> userIds = new HashSet<>();
		if (room.getUser1Id() != null) userIds.add(room.getUser1Id());
		if (room.getUser2Id() != null) userIds.add(room.getUser2Id());


		// 유저 맵핑 (id -> User)
		Map<UUID, User> users = userRepository.findAllById(userIds).stream()
			.collect(Collectors.toMap(User::getId, u -> u));

		// 프로필 URL 맵핑
		Map<UUID, String> profileUrlByUserId = new HashMap<>();
		UUID u1 = room.getUser1Id();    // 참여자 1
		UUID u2 = room.getUser2Id();    // 참여자 2
		if (u1 != null) {               // null(탈퇴한 유저) 방지
			profileRepository.findByUserId(u1)
				.ifPresent(p -> profileUrlByUserId.put(u1, p.getProfileImageUrl())); // 있으면 URL 저장
		}
		if (u2 != null) {               // null(탈퇴한 유저) 방지
			profileRepository.findByUserId(u2)
				.ifPresent(p -> profileUrlByUserId.put(u2, p.getProfileImageUrl())); // 있으면 URL 저장
		}


		// 7) DTO 리스트 변환
		List<DirectMessageDto> data = rows.stream()
			.map(dm -> {
				UUID dmSenderId   = dm.getSenderId();
				UUID dmReceiverId = resolveReceiverId(room, dmSenderId);
				ParticipantDto sender   = toParticipant(dmSenderId, users, profileUrlByUserId);
				ParticipantDto receiver = toParticipant(dmReceiverId, users, profileUrlByUserId);
				return DirectMessageDto.toDto(dm, sender, receiver);
			})
			.toList();


		UUID nextId = rows.get(rows.size() - 1).getId();

		return DirectMessageDtoCursorResponse.builder()
			.data(data)
			.nextCursor(null)
			.nextIdAfter(nextId)
			.hasNext(hasNext)
			.totalCount(messageRepository.countByDirectMessageRoom_Id(room.getId()))
			.sortBy("createdAt")
			.sortDirection("DESCENDING")
			.build();
	}

	// === 내부 헬퍼들 ===

	private ParticipantDto toParticipant(
		UUID userId,
		Map<UUID, User> users,
		Map<UUID, String> profileUrlByUserId
	) {
		if (userId == null) {                                   // 탈퇴 사용자 처리
			return ParticipantDto.builder()
				.userId(null)
				.name("탈퇴한 사용자")
				.profileImageUrl(null)
				.build();
		}

		User u = users.get(userId);                             // 미리 로드된 사용자
		String url = profileUrlByUserId.get(userId);            // 미리 로드된 프로필 URL(없으면 null)

		return ParticipantDto.builder()
			.userId(userId)                                     // 사용자 ID
			.name(u != null ? u.getName() : "탈퇴한 사용자")    // 이름 or 탈퇴 사용자
			.profileImageUrl(u != null ? url : null)            // 프로필 URL(없으면 null)
			.build();
	}

	// 이메일 로그인 전제: UUID → 이메일 → 실패 시 명확한 예외
	private UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new IllegalStateException("로그인 사용자를 확인할 수 없습니다.");
		}

		Object principal = auth.getPrincipal();

		// 1) 커스텀 UserDetails
		if (principal instanceof OotdUserDetails ud) {
			return ud.getUserId();
		}

		// 2) 일반 UserDetails (username이 UUID 또는 이메일이라고 가정)
		if (principal instanceof UserDetails ud) {
			String username = ud.getUsername();
			UUID parsed = tryParseUuid(username);
			if (parsed != null) return parsed;

			// 이메일만 허용
			if (looksLikeEmail(username)) {
				return userRepository.findByEmail(username)
					.map(User::getId)
					.orElseThrow(() -> new IllegalStateException("이메일로 사용자를 찾을 수 없습니다: " + username));
			}
			throw new IllegalStateException("username이 UUID/이메일 형식이 아닙니다: " + username);
		}

		// 3) JWT (sub이 UUID 또는 이메일이라고 가정)
		if (auth instanceof JwtAuthenticationToken jwt) {
			String sub = jwt.getToken().getClaimAsString("sub");
			UUID parsed = tryParseUuid(sub);
			if (parsed != null) return parsed;

			if (looksLikeEmail(sub)) {
				return userRepository.findByEmail(sub)
					.map(User::getId)
					.orElseThrow(() -> new IllegalStateException("JWT sub(이메일)로 사용자를 찾을 수 없습니다: " + sub));
			}
			throw new IllegalStateException("JWT sub이 UUID/이메일 형식이 아닙니다: " + sub);
		}

		// 4) 최종 폴백: auth.getName() (UUID 또는 이메일만 허용)
		String name = auth.getName();
		UUID parsed = tryParseUuid(name);
		if (parsed != null) return parsed;

		if (looksLikeEmail(name)) {
			return userRepository.findByEmail(name)
				.map(User::getId)
				.orElseThrow(() -> new IllegalStateException("인증 주체 이메일로 사용자를 찾을 수 없습니다: " + name));
		}
		throw new IllegalStateException("인증 주체가 UUID/이메일 형식이 아닙니다: " + name);
	}

	private boolean looksLikeEmail(String s) {
		return s != null && s.contains("@"); // 간단 체크(필요시 정규식 강화 가능)
	}

	private UUID tryParseUuid(String s) {
		try { return UUID.fromString(s); } catch (Exception e) { return null; }
	}

	private String dmKeyOf(UUID a, UUID b) {
		String s1 = a.toString(), s2 = b.toString();
		return (s1.compareTo(s2) <= 0) ? (s1 + "_" + s2) : (s2 + "_" + s1);
	}

	private Instant parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) return null;
		try { return Instant.parse(cursor); }
		catch (Exception ignore) {
			try { return Instant.ofEpochMilli(Long.parseLong(cursor)); }
			catch (Exception e) { log.warn("invalid cursor: {}", cursor); return null; }
		}
	}

	private UUID resolveReceiverId(DirectMessageRoom room, UUID senderId) {
		UUID u1 = room.getUser1Id(), u2 = room.getUser2Id();
		if (u1 == null && u2 == null) return null;
		if (senderId == null) return (u1 != null ? u1 : u2);
		if (Objects.equals(senderId, u1)) return u2;
		if (Objects.equals(senderId, u2)) return u1;
		return (u1 != null ? u1 : u2);
	}
}
