package com.sprint.ootd5team.domain.weather.external.meteo;

import com.sprint.ootd5team.domain.weather.exception.WeatherMeteoFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherMeteoParseException;
import com.sprint.ootd5team.domain.weather.external.WeatherExternalAdapter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

/**
 * Open-Meteo API와 통신해 일간 예보 데이터를 조회하는 어댑터. - 외부 HTTP 호출만 담당하고, 서비스 계층은 도메인 변환에 집중할 수 있도록 분리한다. - 호출
 * 실패/지연 시 전용 예외를 던져 상위 레이어에서 적절히 처리하도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMeteoAdapter implements WeatherExternalAdapter<OpenMeteoResponse> {

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
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(6); // 타임아웃 대기시간 설정

    @Qualifier("openMeteoWebClient")
    private final WebClient openMeteoWebClient;

    /**
     * 지정한 위경도를 기준으로 Open-Meteo의 일간 예보(최대 5일)를 조회한다.
     *
     * @param latitude  조회할 위도
     * @param longitude 조회할 경도
     * @param issueDate
     * @param issueTime
     * @return Open-Meteo 응답 DTO
     */
    @Override
    public OpenMeteoResponse getWeather(BigDecimal latitude, BigDecimal longitude, String issueDate,
        String issueTime, int limit) {
        return getDailyForecast(latitude, longitude);
    }

    // reference 시간과 가장 가까운 발행 시각(reference보다 이전 시간 가져옴)
    public LocalTime resolveIssueTime(ZonedDateTime reference) {
        return null;
    }

    public OpenMeteoResponse getDailyForecast(BigDecimal latitude, BigDecimal longitude) {
        try {
            return openMeteoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(FORECAST_PATH)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("daily", DAILY_PARAMS)
                    .queryParam("forecast_days", FORECAST_DAYS)
                    .queryParam("timezone", "auto")
                    .build())
                .retrieve()
                .bodyToMono(OpenMeteoResponse.class)
                .timeout(REQUEST_TIMEOUT)
                .blockOptional()
                .orElseThrow(WeatherMeteoFetchException::new);
        } catch (WebClientResponseException e) {
            log.warn("[OpenMeteo] API 호출 실패 status:{} body:{}", e.getStatusCode(),
                e.getResponseBodyAsString(), e);
            throw new WeatherMeteoFetchException(e.getMessage());
        } catch (WeatherMeteoFetchException e) {
            throw e;
        } catch (RuntimeException e) {
            Throwable cause = Exceptions.unwrap(e);
            if (cause instanceof TimeoutException) {
                log.warn("[OpenMeteo] API 호출 타임아웃", cause);
                throw new WeatherMeteoFetchException("Open-Meteo 응답이 지연되었습니다.");
            }
            log.warn("[OpenMeteo] 응답 처리 실패", e);
            throw new WeatherMeteoParseException(e.getMessage());
        } catch (Exception e) {
            log.warn("[OpenMeteo] 응답 파싱 실패", e);
            throw new WeatherMeteoParseException(e.getMessage());
        }
    }
}
