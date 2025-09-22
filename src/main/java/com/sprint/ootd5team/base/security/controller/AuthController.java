package com.sprint.ootd5team.base.security.controller;

import com.sprint.ootd5team.base.security.JwtDto;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.user.dto.request.ResetPasswordRequest;
import com.sprint.ootd5team.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi{

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * CSRF 토큰 발급
     * @param csrfToken CSRF토큰 자동발급
     * @return 완료상태 반환
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    /**
     * 리프레시 토큰 재발급
     * @return 재발급된 리프레쉬 토큰
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(@CookieValue("REFRESH_TOKEN") String refreshToken,
        HttpServletResponse response){
        JwtInformation jwtInformation = authService.refreshToken(refreshToken);
        ResponseCookie refreshCookie = jwtTokenProvider.generateRefreshTokenCookie(
            jwtInformation.getRefreshToken());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        JwtDto jwtDto = new JwtDto(
            jwtInformation.getUserDto(),
            jwtInformation.getAccessToken()
        );
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(jwtDto);
    }

    /**
     * 비밀번호 변경을 요청하는 메서드
     * @return void
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

}
