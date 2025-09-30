package com.sprint.ootd5team.domain.extract.service;

import com.sprint.ootd5team.domain.extract.client.JsoupClient;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

        // 본문 텍스트
        String bodyText = extractBodyText(doc);

        return new BasicClothesInfo(imageUrl, bodyText, name);
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

    /**
     * bodyText 전처리
     */
    private String extractBodyText(Document doc) {
        // 1. 상세 설명 영역 우선
        Element descEl = doc.selectFirst(".product-detail, #goods_description, .prd-detail, .detail-info");
        if (descEl != null && !descEl.text().isBlank()) {
            return cleanNoise(descEl.text());
        }

        // body 태그 전체 텍스트
        if (doc.body() != null && !doc.body().text().isBlank()) {
            return cleanNoise(doc.body().text());
        }

        // 2. title + meta
        String title = doc.title();
        String desc = doc.select("meta[name=description], meta[property=og:description]")
            .attr("content");
        String combined = (title != null ? title : "") + " " + (desc != null ? desc : "");
        if (!combined.isBlank()) {
            return cleanNoise(combined);
        }

        // 3. fallback: 상품명만
        return "상품 정보 없음";
    }

    private String cleanNoise(String text) {
        List<String> noiseKeywords = List.of(
            "쿠폰", "배송", "무료배송", "혜택", "안전거래", "에스크로",
            "고객센터", "문의", "추천 상품", "리뷰", "구매하기", "지그재그"
        );
        return Arrays.stream(text.split("[.\\n]"))
            .map(String::trim)
            .filter(s -> noiseKeywords.stream().noneMatch(s::contains))
            .collect(Collectors.joining(". "));
    }
}