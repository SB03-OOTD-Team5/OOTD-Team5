package com.sprint.ootd5team.base.config;

import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/* spring security 비활성화 (개발용) */
@Configuration
@ConditionalOnProperty(name = "app.security.enabled", matchIfMissing = true, havingValue = "false")
public class NoSecurityConfig {

    @Bean
    public SecurityFilterChain noSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .headers(h -> h.frameOptions(f -> f.disable()))
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(JwtRegistry.class)
    public JwtRegistry noopJwtRegistry() {
        return new JwtRegistry() {
            @Override
            public void registerJwtInformation(JwtInformation jwtInformation) {
                // no-op
            }

            @Override
            public void invalidateJwtInformationByUserId(UUID userId) {
                // no-op
            }

            @Override
            public boolean hasActiveJwtInformationByUserId(UUID userId) {
                return false;
            }

            @Override
            public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
                return false;
            }

            @Override
            public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
                return false;
            }

            @Override
            public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
                // no-op
            }

            @Override
            public void clearExpiredJwtInformation() {
                // no-op
            }
        };
    }
}
