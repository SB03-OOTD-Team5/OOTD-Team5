package com.sprint.ootd5team.domain.user.dto;

import com.sprint.ootd5team.domain.user.entity.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDto(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    Role role,
    List<String> linkedOAuthProviders,
    Boolean locked
)
{}
