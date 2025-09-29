package com.sprint.ootd5team.domain.weather.external.meteo;

import com.sprint.ootd5team.domain.weather.exception.WeatherMeteoFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherMeteoParseException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Open-Meteo API와 통신해 일간 예보 데이터를 조회하는 어댑터.
 *  - 외부 HTTP 호출만 담당하고, 서비스 계층은 도메인 변환에 집중할 수 있도록 분리한다.
 *  - 호출 실패 시 전용 예외를 던져 상위 레이어에서 적절히 처리할 수 있게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMeteoAdapter {

    private static final String FORECAST_PATH = "/forecast";
    private static final String DAILY_PARAMS = String.join(",",
        "weather_code",
        "temperature_2m_max",
        "temperature_2m_min",
        "precipitation_sum",
        "precipitation_probability_max",
        "wind_speed_10m_max",
        "relative_humidity_2m_mean"
    );
    private static final int FORECAST_DAYS = 5; // 백업 API는 5일치만 가져오면 충분하므로 기간을 고정한다.

    @Qualifier("openMeteoWebClient")
    private final WebClient openMeteoWebClient;

    /**
     * 지정한 위경도를 기준으로 Open-Meteo의 일간 예보(최대 5일)를 조회한다.
     *
     * @param latitude  조회할 위도
     * @param longitude 조회할 경도
     * @return Open-Meteo 응답 DTO
     */
    public OpenMeteoResponse getDailyForecast(BigDecimal latitude, BigDecimal longitude) {
        try {
            return openMeteoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(FORECAST_PATH)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("daily", DAILY_PARAMS)
                    .queryParam("forecast_days", FORECAST_DAYS)
                    .queryParam("timezone", "auto") // 위치에 맞춰 제공되는 타임존을 그대로 사용한다.
                    .build())
                .retrieve()
                .bodyToMono(OpenMeteoResponse.class)
                .blockOptional()
                .orElseThrow(WeatherMeteoFetchException::new);
        } catch (WebClientResponseException e) {
            log.warn("[OpenMeteo] API 호출 실패 status:{} body:{}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new WeatherMeteoFetchException(e.getMessage());
        } catch (WeatherMeteoFetchException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[OpenMeteo] 응답 파싱 실패", e);
            throw new WeatherMeteoParseException(e.getMessage());
        }
    }
}