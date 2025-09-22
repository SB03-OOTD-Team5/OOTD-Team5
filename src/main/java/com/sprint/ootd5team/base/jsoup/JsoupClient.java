package com.sprint.ootd5team.base.jsoup;

import com.sprint.ootd5team.base.exception.jsoup.JsoupFetchException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
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
            InetAddress addr = InetAddress.getByName(uri.getHost());

            if (isBlockedAddress(addr)) {
                throw new SecurityException("SSRF 차단: 접근 불가 host=" + uri.getHost());
            }

            return Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeoutMillis)
                .followRedirects(false) // 리다이렉트도 나중에 검증 후 허용
                .get();

        } catch (IOException e) {
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