package com.sprint.ootd5team.base.security.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.user.dto.request.ResetPasswordRequest;
import com.sprint.ootd5team.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
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
