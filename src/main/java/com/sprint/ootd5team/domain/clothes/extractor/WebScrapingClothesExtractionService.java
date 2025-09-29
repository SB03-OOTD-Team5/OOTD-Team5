package com.sprint.ootd5team.domain.clothes.extractor;

import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import com.sprint.ootd5team.base.jsoup.JsoupClient;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * 웹 페이지(상품 상세 URL)를 크롤링 하여 의상 기본 정보를 추출하는 서비스
 * - Jsoup 라이브러리를 통해 HTML 문서를 파싱
 * - Open Graph(og) 메타 태그에서 상품 이름과 이미지를 가져옴
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebScrapingClothesExtractionService implements ClothesExtractionService {

    private final JsoupClient jsoupClient;

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

    @Override
    public ClothesDto extractByUrl(String url) {
        try {
            Document doc = jsoupClient.get(url);

            // 이름: og:title > <title> > 기본값
            String name = doc.select("meta[property~=(?i)og:title], meta[name~=(?i)og:title]")
                .attr("content");
            if (name == null || name.isBlank()) {
                name = doc.title();
            }
            if (name == null || name.isBlank()) {
                name = "이름 없음";
            }

            // 이미지: og:image(절대화) > 첫 img(존재 시만)
            String imageUrl = doc.select("meta[property~=(?i)og:image], meta[name~=(?i)og:image]")
                .attr("content");
            if (imageUrl != null && !imageUrl.isBlank()) {
                imageUrl = toAbsoluteUrl(doc.baseUri(), imageUrl);
            } else {
                var firstImg = doc.selectFirst("img");
                if (firstImg != null) {
                    imageUrl = firstImg.absUrl("src");
                } else {
                    imageUrl = null; // 이미지가 전혀 없는 케이스 허용
                }
            }

            return ClothesDto.builder()
                .name(name != null ? name : "이름 없음")
                .imageUrl(imageUrl)
                .type(null)
                .build();
        } catch (Exception e) {
            log.error("웹스크래핑 실패: {}", url, e);
            throw ClothesExtractionFailedException.withUrl(url);
        }
    }
}