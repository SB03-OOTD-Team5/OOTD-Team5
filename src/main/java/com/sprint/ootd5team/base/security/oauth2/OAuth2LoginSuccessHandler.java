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

        if (authentication.getPrincipal() instanceof OotdOAuth2UserDetails) {
            try {
                OotdOAuth2UserDetails oauthUser = (OotdOAuth2UserDetails) authentication.getPrincipal();
                UserDto userDto = oauthUser.getUserDto();

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

                // 4. 중간페이지를 통해 HTML 응답 후 자동 리다이렉트
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/html;charset=UTF-8");

                String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>로그인 성공</title>
                </head>
                <body>
                    <script>
                        const tokenData = %s;
                        localStorage.setItem('accessToken', tokenData.accessToken);
                        localStorage.setItem('userId', tokenData.userDto.id);
                        window.location.href = '/#/recommendations';
                    </script>
                </body>
                </html>
                """, objectMapper.writeValueAsString(jwtDto));

                response.getWriter().write(html);

                log.info("[Security] OAuth2 로그인 성공. userId: {}", userDto.id());

            } catch (JOSEException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("""
                <html><body>
                    <script>
                        alert('토큰 생성에 실패했습니다.');
                        window.location.href = '/#/login';
                    </script>
                </body></html>
                """);
                log.error("[Security] OAuth2 JWT 토큰 생성 실패", e);
            }
        }else{
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("""
            <html><body>
                <script>
                    alert('인증에 실패했습니다.');
                    window.location.href = '/#/login';
                </script>
            </body></html>
            """);
            log.error("[Security] 유효하지 않은 유저 정보입니다. Authentication type: {}", authentication.getClass().getSimpleName());
        }
    }
}
