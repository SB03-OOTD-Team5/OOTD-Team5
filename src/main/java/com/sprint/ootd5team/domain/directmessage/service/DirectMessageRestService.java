package com.sprint.ootd5team.domain.directmessage.service;

import com.sprint.ootd5team.base.security.OotdSecurityUserDetails;
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
import com.sprint.ootd5team.base.exception.directmessage.DirectMessageAccessDeniedException;
import com.sprint.ootd5team.base.exception.directmessage.DirectMessageAuthenticationException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
			throw DirectMessageAccessDeniedException.notParticipant(room.getId(), senderId);
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

	/**
	 * JWT 기반: DB 없이 Authentication에서 곧장 UUID 추출
	 * - principal 이 문자열 UUID 이면 그대로 사용
	 * - OotdUserDetails면 getUserId()
	 * - JwtAuthenticationToken이면 claim("userId") 또는 subject(UUID) 사용
	 */
	private UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw DirectMessageAuthenticationException.missingAuthentication();
		}

		Object principal = auth.getPrincipal();

		// 1) principal 문자열 UUID
		if (principal instanceof String s) {
			UUID parsed = tryParseUuid(s);
			if (parsed != null) return parsed;
		}

		// 2) 커스텀 UserDetails
		if (principal instanceof OotdSecurityUserDetails userDetails) {
			return userDetails.getUserId();
		}

		// 4) JWT 토큰에서 userId 또는 sub(UUID) 사용
		if (auth instanceof JwtAuthenticationToken jwt) {
			String claimUserId = jwt.getToken().getClaimAsString("userId");
			if (claimUserId != null) {
				UUID parsed = tryParseUuid(claimUserId);
				if (parsed != null) return parsed;
			}
			String subject = jwt.getToken().getSubject();
			UUID parsed = tryParseUuid(subject);
			if (parsed != null) return parsed;

			throw DirectMessageAuthenticationException.jwtWithoutIdentifiers();
		}

		// 5) 마지막 폴백: auth.getName()이 UUID일 때만
		UUID parsed = tryParseUuid(auth.getName());
		if (parsed != null) return parsed;

		throw DirectMessageAuthenticationException.unresolvablePrincipal(principal, auth.getName());
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
