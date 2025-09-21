package com.sprint.ootd5team.base.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * Jsoup 라이브러리 호출을 감싸는 클라이언트
 * url로 부터 html 문서를 가져와 Document 객체를 반환한다
 * - 외부 HTTP 요청 + html 파싱 담당
 * - 서비스/도메인에서 Jsoup 라이브러리 의존성을 최소화
 * - 테스트 환경에서는 이 컴포넌트를 mock 처리하여 실제 HTTP요청 없이 파싱 동작 검증 가능
 */
@Component
public class JsoupClient {

    public Document get(String url) throws Exception {
        return Jsoup.connect(url).get();
    }
}