package com.sprint.ootd5team.domain.feed.dto.request;

import java.util.UUID;

public record CommentCreateRequest(
    UUID feedId,
    UUID authorId,
    String content
) { }