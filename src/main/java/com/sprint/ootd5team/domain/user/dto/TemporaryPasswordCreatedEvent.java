package com.sprint.ootd5team.domain.user.dto;

import java.time.Instant;

public record TemporaryPasswordCreatedEvent(
  String tempPassword,
  String email,
  String name,
  Instant expireAt
){}
