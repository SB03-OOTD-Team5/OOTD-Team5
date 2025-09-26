package com.sprint.ootd5team.domain.follow.service;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.enums.FollowDirection;
import com.sprint.ootd5team.domain.follow.dto.request.FollowListBaseRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.mapper.FollowMapper;
import com.sprint.ootd5team.domain.follow.repository.FollowRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final FollowMapper followMapper;

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

    private <T extends FollowListBaseRequest> FollowListResponse getFollowListCommon(
        T request,
        Function<T, UUID> idExtractor,
        FollowDirection followDirection
    ) {
        UUID userId = idExtractor.apply(request);
        int limit = request.limit();
        String nameLike = request.nameLike();

        if (!profileRepository.existsByUserId(userId)) {
            log.warn("[FollowService] 프로필이 존재하지 않습니다. userId: {}", userId);
            throw ProfileNotFoundException.withUserId(userId);
        }

        Instant createdCursor = request.cursor() != null ? Instant.parse(request.cursor()) : null;
        UUID idCursor = request.idAfter();

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
}