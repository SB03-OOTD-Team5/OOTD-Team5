package com.sprint.ootd5team.domain.follow.repository;

import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.enums.FollowDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FollowRepositoryCustom {

    List<FollowProjectionDto> findByCursor(
        UUID followerId, Instant createdCursor, UUID idCursor, int limit, String nameLike, FollowDirection direction
    );

    long countByUserIdAndNameLike(
        UUID followerId, String nameLike, FollowDirection direction
    );

    FollowSummaryDto getSummary(UUID userId, UUID currentUserId);
}