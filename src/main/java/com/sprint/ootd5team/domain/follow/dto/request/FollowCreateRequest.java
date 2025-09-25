package com.sprint.ootd5team.domain.follow.dto.request;

import java.util.UUID;

public record FollowCreateRequest(
    UUID followeeId,
    UUID followerId
) { }