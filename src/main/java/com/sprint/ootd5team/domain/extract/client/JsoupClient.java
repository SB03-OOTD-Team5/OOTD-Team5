package com.sprint.ootd5team.domain.extract.client;

import com.sprint.ootd5team.base.exception.jsoup.JsoupFetchException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Jsoup 기반 HTML fetch 클라이언트
 *
 * <p>외부 URL에서 HTML 문서를 가져와 {@link Document}를 반환
 * <br>SSRF 방어를 위해 로컬/사설망/메타데이터 IP(127.0.0.1, 10.x.x.x, 169.254.169.254 등) 접근은 차단
 *
 * <p>설정값:
 * <ul>
 *   <li>{@code ootd.jsoup.timeout} – 요청 타임아웃(ms)</li>
 *   <li>{@code ootd.jsoup.user-agent} – HTTP User-Agent</li>
 * </ul>
 */
@Component
@Slf4j
public class JsoupClient {

    private final int timeoutMillis;
    private final String userAgent;

    public JsoupClient(
        @Value("${ootd.jsoup.timeout}") int timeoutMillis,
        @Value("${ootd.jsoup.user-agent}") String userAgent
    ) {
        this.timeoutMillis = timeoutMillis;
        this.userAgent = userAgent;
    }

    public Document get(String url) {
        try {
            URI uri = URI.create(url);

            // 1. 스킴 검증
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new JsoupFetchException(url, new IllegalArgumentException("unsupported scheme: " + scheme));
            }

            // 2. 호스트 검증
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new JsoupFetchException(url, new IllegalArgumentException("missing host"));
            }

            // 3. 포트 검증
            int port = uri.getPort();
            if (port != -1 && port != 80 && port != 443) {
                throw new JsoupFetchException(url, new IllegalArgumentException("blocked port: " + port));
            }

            // 4. 모든 IP 레코드 검사 (DNS rebinding 방어)
            for (InetAddress a : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(a)) {
                    throw new JsoupFetchException(url, new IOException("SSRF blocked host=" + host + " addr=" + a.getHostAddress()));
                }
            }

            // 5. Jsoup 요청
            return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeoutMillis)
                .maxBodySize(2 * 1024 * 1024) // 2MB 제한 예시
                .followRedirects(false)
                .get();

        } catch (IOException e) {
            log.error("[JsoupClient] SSRF 방어 차단: url={}, reason={}", url, e.getMessage());
            throw new JsoupFetchException(url, e);
        }
    }

    private boolean isBlockedAddress(InetAddress addr) {
        // IPv4/IPv6 로컬 및 사설망
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isSiteLocalAddress()) {
            return true;
        }

        // AWS/GCP/Azure 메타데이터 서비스 (169.254.169.254)
        String hostAddress = addr.getHostAddress();
        if (hostAddress.equals("169.254.169.254")) {
            return true;
        }

        // IPv6 link-local (fe80::/10)
        if (addr.isLinkLocalAddress()) {
            return true;
        }

        return false;
    }
}