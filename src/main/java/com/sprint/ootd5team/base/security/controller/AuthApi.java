package com.sprint.ootd5team.base.security.controller;

import com.sprint.ootd5team.base.security.JwtDto;
import com.sprint.ootd5team.domain.user.dto.request.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

public interface AuthApi {

    ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken);

    ResponseEntity<JwtDto> refresh(String refreshToken,
        HttpServletResponse response);

    ResponseEntity<Void> resetPassword(ResetPasswordRequest request);

}
