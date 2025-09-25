package com.sprint.ootd5team.domain.follow.repository;

import com.sprint.ootd5team.domain.follow.dto.data.FollowProjectionDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FollowRepositoryCustom {

    List<FollowProjectionDto> findByFollowIdWithCursor(
        UUID followerId, Instant createdCursor, UUID idCursor, int limit, String nameLike
    );
}