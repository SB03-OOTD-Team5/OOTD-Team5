package com.sprint.ootd5team.domain.user.dto;

public record TemporaryPasswordCreatedEvent(
  String tempPassword,
  String email,
  String name
){}
