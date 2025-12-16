package com.sprint.ootd5team.base.security.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdSecurityUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtLogoutHandler 단위 테스트")
public class JwtLogoutHandlerTest {

    @Mock
    JwtTokenProvider tokenProvider;

    @Mock
    JwtRegistry jwtRegistry;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    Authentication authentication;

    @InjectMocks
    JwtLogoutHandler handler;

    @Test
    @DisplayName("refresh 만료 쿠키 항상 response에 추가")
    void logout_alwaysAddsRefreshExpirationCookie() {
        // given
        Cookie expireCookie = new Cookie("REFRESH_TOKEN", "");

        when(tokenProvider.genereateRefreshTokenExpirationCookie())
            .thenReturn(expireCookie);

        when(request.getCookies())
            .thenReturn(null);

        // when
        handler.logout(request, response, authentication);

        // then
        verify(tokenProvider).genereateRefreshTokenExpirationCookie();
        verify(response).addCookie(expireCookie);
        verifyNoInteractions(jwtRegistry, userDetailsService);
    }

    @Test
    @DisplayName("request 쿠키가 null/empty면 추가 작업 없이 종료")
    void logout_returnsEarly_whenNoCookies() {
        // given
        Cookie expireCookie = new Cookie("REFRESH_TOKEN", "");
        when(tokenProvider.genereateRefreshTokenExpirationCookie())
            .thenReturn(expireCookie);

        // (1) null
        when(request.getCookies())
            .thenReturn(null);

        // when
        handler.logout(request, response, authentication);

        // then
        verify(response).addCookie(expireCookie);
        verify(jwtRegistry, never()).invalidateJwtInformationByUserId(any());
        verify(userDetailsService, never()).loadUserByUsername(any());

        reset(jwtRegistry, userDetailsService, response, request, tokenProvider);
        when(tokenProvider.genereateRefreshTokenExpirationCookie())
            .thenReturn(expireCookie);
        when(request.getCookies())
            .thenReturn(new Cookie[0]);

        handler.logout(request, response, authentication);

        verify(response).addCookie(expireCookie);
        verify(jwtRegistry, never()).invalidateJwtInformationByUserId(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    @DisplayName("refresh token 쿠키가 있으면 userId 기반 invalidate 및 userDetails 조회 수행")
    void logout_invalidatesJwt_whenRefreshTokenCookieExists() {
        // given
        Cookie expireCookie = new Cookie("REFRESH_TOKEN", "");
        when(tokenProvider.genereateRefreshTokenExpirationCookie())
            .thenReturn(expireCookie);

        String refreshToken = "refresh-token-value";
        Cookie refreshCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        when(request.getCookies()).thenReturn(new Cookie[] {
            new Cookie("OTHER", "x"),
            refreshCookie
        });

        UUID userId = UUID.randomUUID();
        String username = "user@test.com";

        when(tokenProvider.getUserId(refreshToken))
            .thenReturn(userId);
        when(tokenProvider.getEmailFromToken(refreshToken))
            .thenReturn(username);

        OotdSecurityUserDetails userDetails = mock(OotdSecurityUserDetails.class);
        when(userDetailsService.loadUserByUsername(username))
            .thenReturn(userDetails);

        // when
        handler.logout(request, response, authentication);

        // then
        verify(response).addCookie(expireCookie);
        verify(tokenProvider).getUserId(refreshToken);
        verify(jwtRegistry).invalidateJwtInformationByUserId(eq(userId));
        verify(tokenProvider).getEmailFromToken(refreshToken);
        verify(userDetailsService).loadUserByUsername(eq(username));
    }

    @Test
    @DisplayName("refresh token 쿠키가 없으면 invalidate 비수행")
    void logout_doesNothing_whenNoRefreshTokenCookie() {
        // given
        Cookie expireCookie = new Cookie("REFRESH_TOKEN", "");
        when(tokenProvider.genereateRefreshTokenExpirationCookie())
            .thenReturn(expireCookie);

        when(request.getCookies()).thenReturn(new Cookie[] {
            new Cookie("OTHER", "x")
        });

        // when
        handler.logout(request, response, authentication);

        // then
        verify(response).addCookie(expireCookie);
        verify(jwtRegistry, never()).invalidateJwtInformationByUserId(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(tokenProvider, never()).getUserId(any());
        verify(tokenProvider, never()).getEmailFromToken(any());
    }
}