package com.sprint.ootd5team.base.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.RedisJwtRegistry;
import com.sprint.ootd5team.base.security.RedisLockProvider;
import com.sprint.ootd5team.base.security.handler.Http403ForbiddenAccessDeniedHandler;
import com.sprint.ootd5team.base.security.JwtAuthenticationFilter;
import com.sprint.ootd5team.base.security.handler.JwtLoginSuccessHandler;
import com.sprint.ootd5team.base.security.handler.JwtLogoutHandler;
import com.sprint.ootd5team.base.security.handler.LoginFailureHandler;
import com.sprint.ootd5team.domain.user.entity.Role;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(name = "app.security.enabled", matchIfMissing = true, havingValue = "true")
public class SecurityConfig {

    /**
     * 현재 적용된 필터체인 목록 표시
     * @param filterChain 시큐리티 필터체인
     * @return 필터체인 로그 목록
     */
    @Bean
    public CommandLineRunner debugFilterChain(SecurityFilterChain filterChain) {
        return args -> {
            int filterSize = filterChain.getFilters().size();

            List<String> filterNames = IntStream.range(0, filterSize)
                .mapToObj(idx -> String.format("\t[%s/%s] %s", idx + 1, filterSize,
                    filterChain.getFilters().get(idx).getClass()))
                .toList();

            System.out.println("현재 적용된 필터 체인 목록:");
            filterNames.forEach(System.out::println);
        };
    }

    /**
     * 비밀번호를 BCrypt로 암호화
     * @return 암호화된 비밀번호
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 시큐리티 필터체인
     * @param http
     * @param jwtLoginSuccessHandler 로그인 성공 핸들러
     * @param loginFailureHandler 로그인 실패 핸들러
     * @param objectMapper 직렬화
     * @param jwtAuthenticationFilter 커스텀 인증 필터
     * @param jwtLogoutHandler 로그아웃 핸들러
     * @return 다음 시큐리티 필터체인
     */
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtLoginSuccessHandler jwtLoginSuccessHandler,
        LoginFailureHandler loginFailureHandler,
        ObjectMapper objectMapper,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        JwtLogoutHandler jwtLogoutHandler
    )
        throws Exception {
        http
            // csrf 설정
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            // 로그인 설정
            .formLogin(login -> login
                .loginProcessingUrl("api/auth/sign-in")
                .successHandler(jwtLoginSuccessHandler)
                .failureHandler(loginFailureHandler)
            )
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("api/auth/sign-out")
                .addLogoutHandler(jwtLogoutHandler)
                .logoutSuccessHandler(
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)
                )
            )
            // 해당 엔드포인트는 인증 없이 허용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/sign-out").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                .anyRequest().permitAll()//TODO 개발환경은는 모두 허용, 빌드시에는 authenticated()으로 수정필요
            )
            // 예외처리
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                .accessDeniedHandler(new Http403ForbiddenAccessDeniedHandler(objectMapper))
            )
            // 세션 관리
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            //다음 필터체인
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ADMIN은 USER 역할을 포함한다.
     * @return 역할 계층구조
     */
    @Bean
    public RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.withDefaultRolePrefix()
            .role(Role.ADMIN.name())
            .implies(Role.USER.name())
            .build();
    }

    /**
     * roleHierarchy 등록
     * @param roleHierarchy 등록
     * @return 결과
     */
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider,
        UserDetailsService userDetailsService, JwtRegistry registry,
        ObjectMapper objectMapper) {
        return new JwtAuthenticationFilter(tokenProvider,userDetailsService,registry,objectMapper);
    }

    @Bean
    public JwtRegistry jwtRegistry(
        JwtTokenProvider jwtTokenProvider,
        ApplicationEventPublisher publisher,
        RedisTemplate<String, Object> redisTemplate,
        RedisLockProvider redisLockProvider
    ) {
        return new RedisJwtRegistry(1, jwtTokenProvider, publisher, redisTemplate, redisLockProvider);
    }

}
