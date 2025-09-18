package com.sprint.ootd5team.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record FeedCreateRequest(
    @NotNull UUID authorId,
    @NotNull UUID weatherId,
    @NotEmpty Set<@NotNull UUID> clothesIds,
    @NotBlank String content
) { }