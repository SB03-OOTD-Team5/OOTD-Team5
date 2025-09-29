package com.sprint.ootd5team.domain.comment.dto.request;

import java.util.UUID;

public record CommentCreateRequest(
    UUID authorId,
    String content
) { }