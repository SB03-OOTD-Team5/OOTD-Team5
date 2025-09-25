package com.sprint.ootd5team.domain.follow.dto.data;

import java.util.UUID;

public record FollowSummaryDto(
    UUID followeeId,
    long followerCount,
    long followingCount,
    boolean followedByMe,
    UUID followedByMeId,
    boolean followingMe
) { }