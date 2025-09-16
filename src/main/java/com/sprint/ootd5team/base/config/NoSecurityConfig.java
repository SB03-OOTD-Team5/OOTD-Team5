package com.sprint.ootd5team.base.config;

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

}