package com.sprint.ootd5team.domain.weather.external.kma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaParseException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class KmaApiAdapterTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("getKmaWeather - 정상 응답을 파싱한다")
    void 정상응답이면_DTO로_파싱한다() {
        String json = """
            {
              \"response\": {
                \"header\": {\"resultCode\": \"00\", \"resultMsg\": \"NORMAL_SERVICE\"},
                \"body\": {
                  \"dataType\": \"JSON\",
                  \"items\": {
                    \"item\": [
                      {\"baseDate\": \"20250925\", \"baseTime\": \"0600\", \"category\": \"TMP\", \"fcstDate\": \"20250926\", \"fcstTime\": \"0900\", \"fcstValue\": \"23\", \"nx\": 60, \"ny\": 127}
                    ]
                  },
                  \"pageNo\":1, \"numOfRows\":1, \"totalCount\":1
                }
              }
            }
            """;

        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> {
                return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build());
            })
            .build();

        KmaApiAdapter adapter = new KmaApiAdapter(webClient, OBJECT_MAPPER);

        KmaResponse dto = adapter.getKmaWeather("20250925", "0600",
            BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), 10);

        assertEquals("00", dto.response().header().resultCode());
        assertEquals(1, dto.response().body().items().weatherItems().size());
    }

    @Test
    @DisplayName("getKmaWeather - 결과 코드가 00이 아니면 예외")
    void 응답코드가_성공이_아니면_예외를_던진다() {
        String json = """
            {
              \"response\": {
                \"header\": {\"resultCode\": \"10\", \"resultMsg\": \"NO_DATA\"},
                \"body\": {\"items\": {\"item\": []}}
              }
            }
            """;

        WebClient webClient = WebClient.builder()
            .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .build()))
            .build();

        KmaApiAdapter adapter = new KmaApiAdapter(webClient, OBJECT_MAPPER);

        assertThrows(WeatherKmaParseException.class, () ->
            adapter.getKmaWeather("20250925", "0600",
                BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0), 10)
        );
    }
}
