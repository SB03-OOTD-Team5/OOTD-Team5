package com.sprint.ootd5team.base.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * SPA 환경에서 사용할 CSRF 토큰 처리 핸들러
 *
 * - 기본 동작:
 *   1) 응답 시에는 XorCsrfTokenRequestAttributeHandler를 사용하여 BREACH 공격 방어
 *   2) 로그인 직후 등 필요한 경우 csrfToken.get()을 강제로 호출해서
 *      CookieCsrfTokenRepository가 CSRF 토큰 쿠키(XSRF-TOKEN)를 생성하도록 유도
 *   3) 요청 시에는 헤더에 토큰이 오면 plain 핸들러로 처리,
 *      파라미터로 오면 xor 핸들러로 처리 (SPA + 전통 MVC 모두 대응 가능)
 */
public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    // 단순히 request attribute에서 토큰을 꺼내는 핸들러 (헤더 기반 처리)
    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    // XOR 연산을 적용한 핸들러 (폼 파라미터 기반 처리, BREACH 방어 목적)
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    /**
     * 응답 처리 시 실행 - XOR 기반 핸들러를 사용하여 응답에 CSRF 토큰을 노출 (BREACH 방어) - csrfToken.get()을 호출해서
     * 쿠키(XSRF-TOKEN)가 항상 생성되도록 강제
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        Supplier<CsrfToken> csrfToken) {
        // 1. 응답 본문에 토큰을 노출할 때는 XOR 기반 핸들러 사용 (보안 강화)
        this.xor.handle(request, response, csrfToken);
        // 2. csrfToken.get()을 호출 → 지연 로딩된 토큰을 강제로 초기화
        //    이렇게 해야 CookieCsrfTokenRepository가 "Set-Cookie: XSRF-TOKEN=..." 내려줌
        csrfToken.get();
    }

    /**
     * 요청에서 CSRF 토큰 값을 추출할 때 실행 - 헤더에 CSRF 토큰이 있으면 plain 핸들러 사용 (SPA: 쿠키→헤더 복사 전략) - 그렇지 않으면 xor 핸들러 사용 (전통적인 form submit 방식)
     */
    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        // 요청 헤더에서 토큰을 가져옴
        String headerValue = request.getHeader(csrfToken.getHeaderName());

        // 헤더가 있으면 plain 처리 (SPA 케이스)
        // 헤더가 없으면 xor 처리 (폼 submit 케이스)
        return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
            .resolveCsrfTokenValue(request, csrfToken);
    }
}
