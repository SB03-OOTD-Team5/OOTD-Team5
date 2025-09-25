package com.sprint.ootd5team.domain.follow.dto.data;

import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.UUID;

public record FollowProjectionDto(
    UUID id,
    Instant createdAt,
    AuthorDto followee,
    AuthorDto follower
) { }