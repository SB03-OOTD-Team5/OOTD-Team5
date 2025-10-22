package com.sprint.ootd5team.base.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final OotdUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 커스텀 로그인 핸들러를 만들어서 임시 비밀번호도 확인
     */
    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            OotdSecurityUserDetails userDetails = (OotdSecurityUserDetails) userDetailsService.loadUserByUsername(
                email);

            // 1. 계정 잠금 체크
            if (userDetails.getUserDto().locked() == true) {
                log.info("[Security] 로그인실패 - 계정이 잠겨있습니다 email: {}", email);
                throw new LockedException("User account is locked");
            }

            // 2. 일반 비밀번호 체크
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(userDetails, password,
                    userDetails.getAuthorities());
            }

            // 3. 임시 비밀번호 체크
            if (userDetailsService.authenticateTemporaryPassword(email, password)) {
                return new UsernamePasswordAuthenticationToken(userDetails, password,
                    userDetails.getAuthorities());
            }

            log.info("[Security] 로그인실패 - ID 또는 비밀번호 불일치 email: {}", email);
            throw new BadCredentialsException("비밀번호 / 아이디 일치 오류");

        } catch (UsernameNotFoundException e) {
            log.error("[Security] 로그인실패 - 사용자를 찾을 수 없음 email: {}", email);
            throw new BadCredentialsException("사용자를 찾을 수 없음");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
