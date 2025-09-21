package com.sprint.ootd5team.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.base.jsoup.JsoupClient;
import com.sprint.ootd5team.domain.clothes.extractor.WebScrapingClothesExtractionService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebScrapingService 단위 테스트")
public class WebScrapingClothesExtractionServiceTest {

    @Mock
    private JsoupClient jsoupClient;

    @InjectMocks
    private WebScrapingClothesExtractionService service;

    @BeforeEach
    void setUp() {
        jsoupClient = mock(JsoupClient.class);
        service = new WebScrapingClothesExtractionService(jsoupClient);
    }

    @Test
    void og_메타태그로_기본_조회한다() throws Exception {
        // given
        String url = "https://dummy.com/product/1";
        String html = """
            <html>
              <head>
                <title>테스트 상품</title>
                <meta property="og:image" content="https://dummy.com/og.png"/>
              </head>
            </html>
            """;
        Document doc = Jsoup.parse(html, url);

        given(jsoupClient.get(url)).willReturn(doc);

        // when
        ClothesDto result = service.extractByUrl(url);

        // then
        assertThat(result.name()).isEqualTo("테스트 상품");
        assertThat(result.imageUrl()).isEqualTo("https://dummy.com/og.png");
    }

    @Test
    void og_메타태그가_없으면_src로_조회한다() throws Exception {
        // given
        String url = "https://dummy.com/product/2";
        String html = """
            <html>
              <head>
                <title>이미지 없는 상품</title>
              </head>
              <body>
                <img src="https://dummy.com/fallback.png"/>
              </body>
            </html>
            """;
        Document doc = Jsoup.parse(html, url);

        given(jsoupClient.get(url)).willReturn(doc);

        // when
        ClothesDto result = service.extractByUrl(url);

        // then
        assertThat(result.name()).isEqualTo("이미지 없는 상품");
        assertThat(result.imageUrl()).isEqualTo("https://dummy.com/fallback.png");
    }

    @Test
    void JsoupClient에서_예외가_발생하면_RuntimeException을_던진다() throws Exception {
        // given
        String url = "https://dummy.com/product/3";
        given(jsoupClient.get(url)).willThrow(new RuntimeException("연결 실패"));

        // when
        Throwable thrown = catchThrowable(() -> service.extractByUrl(url));

        // then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("옷 정보 추출 실패");
    }
}