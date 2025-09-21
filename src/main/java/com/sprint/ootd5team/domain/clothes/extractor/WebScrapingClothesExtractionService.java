package com.sprint.ootd5team.domain.clothes.extractor;

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

    @Override
    public ClothesDto extractByUrl(String url) {
        try {
            Document doc = jsoupClient.get(url);

            String name = doc.title();
            String imageUrl = doc.select("meta[property=og:image]").attr("content");

            if (imageUrl == null || imageUrl.isBlank()) {
                imageUrl = doc.select("img").first().absUrl("src");
            }

            return ClothesDto.builder()
                .name(name != null ? name : "이름 없음")
                .imageUrl(imageUrl)
                .type(null)
                .build();
        } catch (Exception e) {
            log.error("웹스크래핑 실패: {}", url, e);
            throw new RuntimeException("옷 정보 추출 실패: " + e.getMessage(), e);
        }
    }
}