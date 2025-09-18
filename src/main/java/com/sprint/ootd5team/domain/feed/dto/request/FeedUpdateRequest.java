package com.sprint.ootd5team.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FeedUpdateRequest(
    @NotBlank String content
) { }