package com.sprint.ootd5team.domain.comment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentListRequest(
    String cursor,
    UUID idAfter,
    @NotNull @Min(1) int limit
) { }