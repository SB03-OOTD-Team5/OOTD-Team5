package com.sprint.ootd5team.domain.extract.service;

import com.sprint.ootd5team.domain.extract.client.JsoupClient;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MetadataExtractionServiceTest {

    @Mock
    JsoupClient jsoupClient;

    @InjectMocks
    MetadataExtractionService service;

    @Test
    void og_메타태그로_이미지_조회() {
        String url = "https://dummy.com/product/1";
        String html = """
            <html>
              <head>
                <title>테스트 상품</title>
                <meta property="og:image" content="https://dummy.com/og.png"/>
              </head>
              <body>본문 설명</body>
            </html>
            """;
        Document doc = Jsoup.parse(html, url);
        given(jsoupClient.get(url)).willReturn(doc);

        BasicClothesInfo result = service.extract(url);

        assertThat(result.imageUrl()).isEqualTo("https://dummy.com/og.png");
        assertThat(result.bodyText()).contains("본문 설명");
    }

    @Test
    void og_이미지가_없으면_img_src를_사용() {
        String url = "https://dummy.com/product/2";
        String html = """
            <html>
              <body>
                <img src="https://dummy.com/fallback.png"/>
              </body>
            </html>
            """;
        Document doc = Jsoup.parse(html, url);
        given(jsoupClient.get(url)).willReturn(doc);

        BasicClothesInfo result = service.extract(url);

        assertThat(result.imageUrl()).isEqualTo("https://dummy.com/fallback.png");
    }

    @Test
    void bodyText가_비어있으면_title과_description을_사용() {
        String url = "https://dummy.com/product/3";
        String html = """
            <html>
              <head>
                <title>타이틀 이름</title>
                <meta name="description" content="설명 텍스트"/>
              </head>
              <body></body>
            </html>
            """;
        Document doc = Jsoup.parse(html, url);
        given(jsoupClient.get(url)).willReturn(doc);

        BasicClothesInfo result = service.extract(url);

        assertThat(result.bodyText()).contains("타이틀 이름").contains("설명 텍스트");
    }
}
