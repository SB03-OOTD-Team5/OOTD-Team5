package com.sprint.ootd5team.domain.follow.service;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.request.FollowListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.mapper.FollowMapper;
import com.sprint.ootd5team.domain.follow.repository.FollowRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
    public FollowListResponse getFollowingList(FollowListRequest request) {
        log.info("[FollowService] 팔로우 목록 조회 요청 시작");

        UUID followerId = request.followerId();
        int limit = request.limit();

        if (!profileRepository.existsByUserId(followerId)) {
            log.warn("[FollowService] 프로필이 존재하지 않습니다. userId: {}", followerId);
            throw ProfileNotFoundException.withUserId(followerId);
        }

        Instant createdCursor = request.cursor() != null ? Instant.parse(request.cursor()) : null;
        UUID idCursor = request.idAfter();

        List<FollowProjectionDto> follows = followRepository.findByFollowIdWithCursor(
            followerId,
            createdCursor,
            idCursor,
            limit,
            request.nameLike()
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
            followRepository.countByFollowerId(followerId),
            "createdAt",
            SortDirection.DESCENDING
        );
    }
}