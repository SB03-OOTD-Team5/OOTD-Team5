package com.sprint.ootd5team.domain.follow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FollowingListRequest(
    @NotNull UUID followerId,
    String cursor,
    UUID idAfter,
    @NotNull @Min(1) int limit,
    String nameLike
) implements FollowListBaseRequest { }