package com.sprint.ootd5team.domain.extract.service;

import com.sprint.ootd5team.domain.extract.client.JsoupClient;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * 메타데이터 기반 의상 정보 추출 서비스.
 *
 * <p>
 * 주어진 상품 상세 URL을 파싱하여 다음 정보를 추출
 * <ul>
 *   <li>대표 이미지 URL: 우선순위
 *     <ol>
 *       <li>Open Graph 메타태그 (<code>og:image</code>)</li>
 *       <li>페이지 내 첫 번째 <code>&lt;img&gt;</code> 태그</li>
 *     </ol>
 *   </li>
 *   <li>본문 텍스트: 우선순위
 *     <ol>
 *       <li>HTML body 전체 텍스트</li>
 *       <li><code>&lt;title&gt;</code> + <code>description</code> 메타태그</li>
 *       <li>본문 텍스트는 Extractor에서 llm을 호출하여 속성값을 파싱</li>
 *     </ol>
 *   </li>
 * </ul>
 *
 * <p>이미지는 LLM 추론 대신 원본 메타데이터를 신뢰하여 추출
 * (대표 이미지 정확성, 속도, 서비스 일관성을 보장하기 위함)
 */
@Service
@RequiredArgsConstructor
public class MetadataExtractionService {

    private final JsoupClient jsoupClient;

    public BasicClothesInfo extract(String url) {
        Document doc = jsoupClient.get(url);

        // 이미지 추출
        String rawImageUrl = doc.select("meta[property~=(?i)og:image], meta[name~=(?i)og:image]")
            .attr("content");
        String imageUrl = toAbsoluteUrl(url, rawImageUrl);

        if (imageUrl == null || imageUrl.isBlank()) {
            var firstImg = doc.selectFirst("img");
            imageUrl = (firstImg != null) ? firstImg.absUrl("src") : null;
        }

        // 이름
        String name = doc.select("meta[property=og:title], meta[name=title]").attr("content");
        if (name == null || name.isBlank()) {
            name = doc.title();
        }

        // 브랜드 (사이트별 selector 다를 수 있음 → 무신사 기준 예시) (없을 수 있음)
        String brand = doc.select(".product_title .brand, meta[property=product:brand]").text();

        // 카테고리 (없을 수 있음)
        String category = doc.select(".breadcrumb a").text();

        // 본문 텍스트
        String bodyText = doc.body() != null ? doc.body().text() : "";
        if (bodyText.isBlank()) {
            String title = doc.title();
            String desc = doc.select("meta[name=description], meta[property=og:description]")
                .attr("content");
            bodyText = (title != null ? title : "") + " " + (desc != null ? desc : "");
        }
        if (bodyText.isBlank()) {
            bodyText = "상품 정보 없음";
        }

        return new BasicClothesInfo(imageUrl, bodyText, name, url);
    }

    /**
     * 상대 경로를 절대 URL로 변환
     */
    private static String toAbsoluteUrl(String baseUri, String maybeRelative) {
        if (maybeRelative == null || maybeRelative.isBlank()) {
            return maybeRelative;
        }
        if (maybeRelative.startsWith("http://") || maybeRelative.startsWith("https://")) {
            return maybeRelative;
        }
        try {
            return new java.net.URL(new java.net.URL(baseUri), maybeRelative).toString();
        } catch (Exception e) {
            return maybeRelative; // 변환 실패 시 원문 유지
        }
    }

}