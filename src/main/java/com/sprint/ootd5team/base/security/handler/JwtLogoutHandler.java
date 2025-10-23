package com.sprint.ootd5team.base.security.handler;

import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdSecurityUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider tokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {
        // Clear refresh token cookie
        Cookie refreshTokenExpirationCookie = tokenProvider.genereateRefreshTokenExpirationCookie();
        response.addCookie(refreshTokenExpirationCookie);

        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length == 0) {
            log.debug("[Security] No cookies found");
            return;
        }

        Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
            .findFirst()
            .ifPresent(cookie -> {
                String refreshToken = cookie.getValue();
                UUID userId = tokenProvider.getUserId(refreshToken);
                jwtRegistry.invalidateJwtInformationByUserId(userId);
                String username = tokenProvider.getEmailFromToken(refreshToken);
                OotdSecurityUserDetails userDetails = (OotdSecurityUserDetails)userDetailsService.loadUserByUsername(username);
            });
        log.debug("[Security] JWT logout handler executed - refresh token cookie cleared");
    }
}
