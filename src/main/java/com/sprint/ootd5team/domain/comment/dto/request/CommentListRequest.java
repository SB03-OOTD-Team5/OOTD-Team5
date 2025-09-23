package com.sprint.ootd5team.domain.comment.dto.request;

import jakarta.validation.constraints.Min;
import java.util.UUID;

public record CommentListRequest(
    String cursor,
    UUID idAfter,
    @Min(1) int limit
) { }