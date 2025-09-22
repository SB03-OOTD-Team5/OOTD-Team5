package com.sprint.ootd5team.base.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.base.security.JwtDto;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements
    AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider tokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (authentication.getPrincipal() instanceof OotdUserDetails userDetails) {
            try {
                // 1. 토큰생성
                String accessToken = tokenProvider.generateAccessToken(userDetails);
                String refreshToken = tokenProvider.generateRefreshToken(userDetails);

                // 2. 서버 상태 등록
                jwtRegistry.registerJwtInformation(
                    new JwtInformation(
                        userDetails.getUserDto(),
                        accessToken,
                        refreshToken
                    )
                );

                // 3. 쿠키설정
                ResponseCookie refreshCookie = tokenProvider.generateRefreshTokenCookie(refreshToken);
                response.addHeader("Set-Cookie", refreshCookie.toString());

                JwtDto jwtDto = new JwtDto(
                    userDetails.getUserDto(),
                    accessToken
                );

                // 4. 본문 전송
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(objectMapper.writeValueAsString(jwtDto));


                log.info("JWT access and refresh tokens issued for user: {}", userDetails.getUsername());

            } catch (JOSEException e) {
                log.error("Failed to generate JWT token for user: {}", userDetails.getUsername(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse errorResponse = new ErrorResponse(
                    new RuntimeException("Token generation failed")
                );
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ErrorResponse errorResponse = new ErrorResponse(
                new RuntimeException("Authentication failed: Invalid user details")
            );
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
