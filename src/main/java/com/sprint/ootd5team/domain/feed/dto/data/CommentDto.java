package com.sprint.ootd5team.domain.feed.dto.data;

import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID feedId,
    Instant createdAt,
    AuthorDto author,
    String content
) { }