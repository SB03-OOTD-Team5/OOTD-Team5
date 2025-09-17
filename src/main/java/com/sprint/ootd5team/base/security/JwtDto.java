package com.sprint.ootd5team.base.security;

import com.sprint.ootd5team.domain.user.dto.UserDto;

public record JwtDto(
    UserDto userDto,
    String accessToken
) {

}
