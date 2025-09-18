package com.sprint.ootd5team.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record FeedCreateRequest(
    @NotNull UUID authorId,
    @NotNull UUID weatherId,
    @NotNull Set<UUID> clothesIds,
    @NotBlank String content
) { }