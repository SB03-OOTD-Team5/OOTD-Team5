package com.sprint.ootd5team.domain.comment.dto.data;

import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.UUID;

public record CommentDto(
    UUID id,
    Instant createdAt,
    UUID feedId,
    AuthorDto author,
    String content
) { }