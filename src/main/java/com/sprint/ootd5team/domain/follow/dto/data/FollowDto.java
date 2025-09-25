package com.sprint.ootd5team.domain.follow.dto.data;

import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.util.UUID;

public record FollowDto(
    UUID id,
    AuthorDto followee,
    AuthorDto follower
) { }