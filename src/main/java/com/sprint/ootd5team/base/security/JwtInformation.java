package com.sprint.ootd5team.base.security;

import com.sprint.ootd5team.domain.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtInformation {

    private UserDto userDto;
    private String accessToken;
    private String refreshToken;

    public void rotate(String newAccessToken, String newRefreshToken){
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
    }
}
