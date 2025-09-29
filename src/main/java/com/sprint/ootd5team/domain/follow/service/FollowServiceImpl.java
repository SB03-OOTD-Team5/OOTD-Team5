package com.sprint.ootd5team.domain.follow.service;

import com.sprint.ootd5team.base.exception.follow.FollowNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.enums.FollowDirection;
import com.sprint.ootd5team.domain.follow.dto.request.FollowListBaseRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.entity.Follow;
import com.sprint.ootd5team.domain.follow.mapper.FollowMapper;
import com.sprint.ootd5team.domain.follow.repository.FollowRepository;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final FollowMapper followMapper;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public FollowDto follow(UUID followerId, UUID followeeId) {
        log.info("[FollowService] 팔로우 등록 요청 시작");

        Profile followeeProfile = getProfileOrThrow(followeeId, "followeeId");
        Profile followerProfile = getProfileOrThrow(followerId, "followerId");

        Follow saved = followRepository.save(new Follow(followeeId, followerId));

        log.debug("[FollowService] 등록된 Follow 데이터 - {}", saved);

        return new FollowDto(
            saved.getId(),
            profileMapper.toAuthorDto(followeeProfile),
            profileMapper.toAuthorDto(followerProfile)
        );
    }

    @Override
    public FollowListResponse getFollowingList(FollowingListRequest followingListRequest) {
        log.info("[FollowService] 팔로우 목록 조회 요청 시작");

        return getFollowListCommon(
            followingListRequest,
            FollowingListRequest::followerId,
            FollowDirection.FOLLOWING
        );
    }

    @Override
    public FollowListResponse getFollowerList(FollowerListRequest followerListRequest) {
        log.info("[FollowService] 팔로워 목록 조회 요청 시작");

        return getFollowListCommon(
            followerListRequest,
            FollowerListRequest::followeeId,
            FollowDirection.FOLLOWER
        );
    }

    @Override
    public FollowSummaryDto getSummary(UUID userId, UUID currentUserId) {
        log.info("[FollowService] 팔로우 요약 정보 조회 요청 시작");

        validateProfile(userId);
        validateProfile(currentUserId);

        return followRepository.getSummary(userId, currentUserId);
    }

    @Override
    @Transactional
    public void unFollow(UUID followId) {
        log.info("[FollowService] 팔로우 취소 요청 시작 - followId: {}", followId);

        if (!followRepository.existsById(followId)) {
            log.warn("[FollowService] 팔로우가 존재하지 않습니다. followId: {}", followId);
            throw FollowNotFoundException.withId(followId);
        }

        followRepository.deleteById(followId);
    }

    private <T extends FollowListBaseRequest> FollowListResponse getFollowListCommon(
        T request,
        Function<T, UUID> idExtractor,
        FollowDirection followDirection
    ) {
        UUID userId = idExtractor.apply(request);
        int limit = request.limit();
        String nameLike = request.nameLike();
        UUID idCursor = request.idAfter();

        validateProfile(userId);

        log.debug("[FollowService] 목록 조회 요청 파라미터 - followeeId:{}, cursor:{}, idAfter:{}, limit:{}, nameLike:{}",
            userId, request.cursor(), idCursor, limit, nameLike);

        Instant createdCursor = request.cursor() != null ? Instant.parse(request.cursor()) : null;

        List<FollowProjectionDto> follows = followRepository.findByCursor(
            userId,
            createdCursor,
            idCursor,
            limit,
            nameLike,
            followDirection
        );

        boolean hasNext = follows.size() > limit;
        if (hasNext) {
            follows = follows.subList(0, limit);
        }

        FollowProjectionDto last = follows.isEmpty() ? null : follows.get(follows.size() - 1);
        log.debug("[FollowService] 팔로잉 목록 마지막 FollowDto: {}", last);

        List<FollowDto> data = followMapper.toFollowDtoList(follows);

        return new FollowListResponse(
            data,
            hasNext ? last.createdAt().toString() : null,
            hasNext ? last.id() : null,
            hasNext,
            followRepository.countByUserIdAndNameLike(userId, nameLike, followDirection),
            "createdAt",
            SortDirection.DESCENDING
        );
    }

    private void validateProfile(UUID userId) {
        if (!profileRepository.existsByUserId(userId)) {
            log.warn("[FollowService] 프로필이 존재하지 않습니다. userId: {}", userId);
            throw ProfileNotFoundException.withUserId(userId);
        }
    }

    private Profile getProfileOrThrow(UUID userId, String role) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> {
                log.warn("[FollowService] 프로필이 조회되지 않습니다. {}: {}", role, userId);
                return ProfileNotFoundException.withUserId(userId);
            });
    }
}