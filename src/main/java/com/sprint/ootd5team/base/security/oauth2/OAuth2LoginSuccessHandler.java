package com.sprint.ootd5team.base.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.base.security.JwtDto;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider tokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof OotdOauth2UserDetails) {
            try {
                OotdOauth2UserDetails oauthUser = (OotdOauth2UserDetails) authentication.getPrincipal();
                UserDto userDto = oauthUser.getUserDto();
                log.info("OAuth2 Login Success. userDto: {}", userDto.email());

                // 1. 토큰생성
                String accessToken = tokenProvider.generateAccessToken(userDto);
                String refreshToken = tokenProvider.generateRefreshToken(userDto);

                // 2. 서버 상태 등록
                jwtRegistry.registerJwtInformation(new JwtInformation(
                    userDto,
                    accessToken,
                    refreshToken
                ));

                // 3. 쿠키설정
                ResponseCookie refreshCookie = tokenProvider.generateRefreshTokenCookie(
                    refreshToken);
                response.addHeader("Set-Cookie", refreshCookie.toString());

                JwtDto jwtDto = new JwtDto(
                    userDto,
                    accessToken
                );

                // 4. 본문 전송
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
                response.sendRedirect("/#/recommendations");

                log.debug("JWT access and refresh tokens issued for user: {}", userDto.name());

            } catch (JOSEException e) {
                log.error("Failed to generate JWT token for user: {}", authentication.getName(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ErrorResponse errorResponse = new ErrorResponse(
                    new RuntimeException("Token generation failed")
                );
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            }
        }else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ErrorResponse errorResponse = new ErrorResponse(
                new RuntimeException("Authentication failed: Invalid user details")
            );
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
