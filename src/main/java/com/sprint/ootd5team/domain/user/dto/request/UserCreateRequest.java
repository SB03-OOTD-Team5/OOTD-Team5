package com.sprint.ootd5team.domain.user.dto.request;

public record UserCreateRequest(
    String name,
    String email,
    String password
) {

}
