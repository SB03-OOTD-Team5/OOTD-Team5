package com.sprint.ootd5team.base.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtRegistry jwtRegistry;
    private final ObjectMapper objectMapper;

    @Override
    /**
     * UsernamePasswordFilter의 앞에서 수행하는 필터 체인으로 토큰을 검증하고 검증된 유저Details를 스프링 시큐리티 컨텍스트 홀더에 저장
     */
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            // 요청 헤더에서 Jwt token 추출
            String token = resolveToken(request);

            // 토큰이 존재할경우
            if (StringUtils.hasText(token)) {
                // 토큰이 유효하고, 현재 활성화 상태인지 검증
                if (jwtTokenProvider.validateAccessToken(token)
                    && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {

                    // Token에서 이메일 추출
                    String email = jwtTokenProvider.getEmailFromToken(token);

                    // 추출한 이메일을 통해 UserDto를 불러오고, userDetails로 변환하여 저장
                    OotdSecurityUserDetails userDetails = (OotdSecurityUserDetails) userDetailsService.loadUserByUsername(
                        email);

                    // 스프링 시큐리티 인증객체 생성
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                    // 요청 세부정보 붙이기
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // SecurityContext에 인증정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("[Security] JWT 토큰 인증 성공 user: {}", email);
                } else {
                    //토큰이 잘못됐을때 401에러
                    sendErrorResponse(response, "Invalid JWT token: ",
                        HttpServletResponse.SC_UNAUTHORIZED);

                }
            }
        } catch (Exception e) {
            // 예외 발생 시 → 인증 컨텍스트 초기화 + 에러 응답
            log.error("[Security] JWT 인증 실패: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, "JWT authentication failed",
                HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 인증 실패 시 JSON 형식의 에러 응답 반환
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int status)
        throws IOException {
        // 커스텀 ErrorResponse 객체 생성
        ErrorResponse errorResponse = new ErrorResponse(new RuntimeException(message));

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // JSON 직렬화 후 응답으로 작성
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

    }
}
