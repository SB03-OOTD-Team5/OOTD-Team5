package com.sprint.ootd5team.base.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

/**
 * OAuth2 인증 요청 정보를 쿠키에 저장하는 Repository
 *
 * OAuth2 로그인은 3단계로 진행됩니다:
 * 1. 사용자가 "Google 로그인" 클릭 → 서버가 state 생성
 * 2. 사용자가 Google에서 로그인
 * 3. Google이 서버로 리다이렉트 → 서버가 state 검증
 *
 * 1번과 3번은 서로 다른 HTTP 요청이므로, 1번에서 생성한 state를
 * 3번에서 검증하려면 어딘가에 저장해야 합니다.
 *
 * 쿠키를 사용하는 이유:
 * - 세션 방식: STATELESS 정책과 충돌, 멀티서버 환경에서 문제
 * - 쿠키 방식: 브라우저가 자동으로 가지고 다님, 서버 간 공유 불필요
 *
 */
@Component
public class CookieOAuth2Repository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    /**
     * 쿠키 이름
     * 브라우저에 저장될 때 이 이름으로 저장됩니다
     */
    private static final String COOKIE_NAME = "oauth2_auth_request";

    /**
     * 쿠키 만료 시간
     */
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    /**
     * 저장된 인증 요청을 불러옵니다
     *
     * 언제 호출되나?
     * Google이 /login/oauth2/code/google 로 리다이렉트할 때
     * Spring Security가 자동으로 이 메서드를 호출하여
     * 이전에 저장했던 state 값을 가져옵니다
     *
     * @param request HTTP 요청 (쿠키가 포함되어 있음)
     * @return 저장된 OAuth2 인증 요청 객체, 없으면 null
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        // 1. 요청에서 쿠키 찾기
        return getCookie(request)
            // 2. 쿠키가 있으면 역직렬화하여 OAuth2AuthorizationRequest 객체로 변환
            .map(this::deserialize)
            // 3. 쿠키가 없으면 null 반환
            .orElse(null);
    }

    /**
     * 인증 요청을 쿠키에 저장합니다
     *
     * 사용자가 "Google 로그인" 버튼을 클릭하여
     * /oauth2/authorization/google 로 접근할 때
     * Spring Security가 자동으로 이 메서드를 호출하여
     * state, redirect_uri 등의 정보를 저장합니다
     *
     * @param authorizationRequest 저장할 OAuth2 인증 요청 (state, redirect_uri 등 포함)
     * @param request HTTP 요청
     * @param response HTTP 응답 (쿠키를 추가)
     */
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response) {
        // null이면 쿠키 삭제 (로그아웃 등의 경우)
        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }

        // 1. OAuth2AuthorizationRequest 객체를 문자열로 직렬화
        String value = serialize(authorizationRequest);

        // 2. 쿠키 생성
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");           // 모든 경로에서 접근 가능
        cookie.setHttpOnly(true);      // JavaScript 접근 차단 (보안)
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);  // 180초 후 자동 만료

        // 3. 응답에 쿠키 추가 (브라우저로 전송)
        response.addCookie(cookie);

        // 이제 브라우저가 이 쿠키를 저장하고
        // 다음 요청(Google 리다이렉트)에서 자동으로 다시 보냅니다
    }

    /**
     * 저장된 인증 요청을 불러온 후 삭제합니다
     *
     * OAuth2 인증이 완료되거나 실패했을 때
     * Spring Security가 자동으로 호출하여 쿠키를 정리합니다
     *
     * - 보안: 사용된 state는 재사용 불가 (일회용)
     * - 용량: 불필요한 쿠키 제거
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 삭제하기 전에 불러온 인증 요청 객체
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
        HttpServletResponse response) {
        // 1. 먼저 저장된 값을 불러오고
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);

        // 2. 쿠키를 삭제한 후
        deleteCookie(response);

        // 3. 불러온 값을 반환
        return authorizationRequest;
    }

    /**
     * 요청에서 특정 이름의 쿠키 값을 찾습니다
     *
     * @param request HTTP 요청
     * @return 쿠키 값 (없으면 Optional.empty())
     */
    private java.util.Optional<String> getCookie(HttpServletRequest request) {
        // 1. 요청에서 모든 쿠키 가져오기
        Cookie[] cookies = request.getCookies();

        // 2. 쿠키가 있는지 확인
        if (cookies != null) {
            // 3. 모든 쿠키를 순회하며
            for (Cookie cookie : cookies) {
                // 4. 이름이 일치하는 쿠키 찾기
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return java.util.Optional.of(cookie.getValue());
                }
            }
        }

        // 5. 못 찾으면 빈 Optional 반환
        return java.util.Optional.empty();
    }

    /**
     * 쿠키를 삭제합니다 (정확히는 만료시킵니다)
     *
     * <p>쿠키 삭제 방법</p>
     * HTTP에서는 쿠키를 직접 삭제할 수 없습니다.
     * 대신 같은 이름의 쿠키를 MaxAge=0으로 설정하여
     * 브라우저가 즉시 삭제하도록 합니다.
     *
     * @param response HTTP 응답
     */
    private void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");  // 빈 값
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 즉시 만료
        response.addCookie(cookie);
    }

    /**
     * OAuth2AuthorizationRequest 객체를 문자열로 직렬화합니다
     *
     * 쿠키는 문자열만 저장 가능합니다.
     * OAuth2AuthorizationRequest는 복잡한 객체(state, redirect_uri, scope 등)이므로
     * 바이트 배열로 변환 → Base64로 인코딩하여 문자열로 만듭니다
     *
     * 1. Java 객체 → 바이트 배열 (SerializationUtils.serialize)
     * 2. 바이트 배열 → Base64 문자열 (URL-safe)
     *
     * @param object 직렬화할 객체
     * @return Base64 인코딩된 문자열
     */
    private String serialize(OAuth2AuthorizationRequest object) {
        return Base64.getUrlEncoder()
            .encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * 문자열을 OAuth2AuthorizationRequest 객체로 역직렬화합니다
     *
     * 과정 (serialize의 역순)
     * 1. Base64 문자열 → 바이트 배열 (Base64 디코딩)
     * 2. 바이트 배열 → Java 객체 (SerializationUtils.deserialize)
     *
     * @param value Base64 인코딩된 문자열
     * @return 복원된 OAuth2AuthorizationRequest 객체
     */
    private OAuth2AuthorizationRequest deserialize(String value) {
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
            Base64.getUrlDecoder().decode(value)
        );
    }
}