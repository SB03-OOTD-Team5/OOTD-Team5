package com.sprint.ootd5team.domain.weather.external.openweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.weather.exception.WeatherOpenFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherOpenParseException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;

@Slf4j
@Component
public class OpenWeatherAdapter {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalTime[] ISSUE_TIMES = {
        LocalTime.of(21, 0),
        LocalTime.of(18, 0),
        LocalTime.of(15, 0),
        LocalTime.of(9, 0),
        LocalTime.of(6, 0),
        LocalTime.of(3, 0),
        LocalTime.MIDNIGHT
    };
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(6); // 타임아웃 대기시간 설정
    private final ObjectMapper mapper;
    private final WebClient openWeatherClient;

    public OpenWeatherAdapter(ObjectMapper mapper,
        @Qualifier("openWeatherClient") WebClient openWeatherClient) {
        this.mapper = mapper;
        this.openWeatherClient = openWeatherClient;
    }

    public OpenWeatherResponse getWeather(BigDecimal latitude, BigDecimal longitude) {
        // 1. fetchRowData
        String responseJson = fetchRowData(latitude, longitude);
        // 2. parseToResponse
        OpenWeatherResponse response = parseToResponseDto(responseJson);
        //3. validate response
        validateData(response);

        return response;
    }

    private String fetchRowData(BigDecimal latitude, BigDecimal longitude) {
        try {
            log.debug(
                "[OpenWeather] 날씨 호출 latitude:{}, longitude:{}", latitude, longitude);

            return openWeatherClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("units", "metric")
                    .build())
                .exchangeToMono(
                    clientResponse -> clientResponse.bodyToMono(String.class)
                )
                .timeout(REQUEST_TIMEOUT)
                .blockOptional()
                .orElseThrow(WeatherOpenFetchException::new);

        } catch (RuntimeException e) {
            Throwable cause = Exceptions.unwrap(e);
            if (cause instanceof TimeoutException) {
                log.warn("[OpenWeather] API 호출 타임아웃", cause);
                throw new WeatherOpenFetchException("Open-Weather 응답이 지연되었습니다.");
            }
            throw new WeatherOpenFetchException();
        }
    }


    private OpenWeatherResponse parseToResponseDto(String responseJson) {
        try {
            return mapper.readValue(responseJson, OpenWeatherResponse.class);
        } catch (Exception e) {
            throw new WeatherOpenParseException(e.getMessage());
        }
    }


    private void validateData(OpenWeatherResponse responseDto) {
        if (responseDto.cod() == null) {
            throw new WeatherOpenParseException();
        }
    }


    // 현재시간과 가장 가까운 이전 시각
    public LocalTime resolveIssueTime(ZonedDateTime reference) {
        LocalTime issueTime = ISSUE_TIMES[ISSUE_TIMES.length - 1];
        for (LocalTime candidate : ISSUE_TIMES) {
            ZonedDateTime candidateTime = ZonedDateTime
                .of(reference.toLocalDate(), candidate, SEOUL_ZONE_ID);
            if (reference.isAfter(candidateTime.plusMinutes(10))) {
                issueTime = candidate;
                break;
            }
        }
        log.debug("[OpenWeather] 이슈 기준 시각 계산 결정 reference:{}, issueTime:{}", reference, issueTime);
        return issueTime;
    }

    // 현재시간과 가장 가까운 다음 시각
    public LocalTime resolveTargetTime(ZonedDateTime reference) {
        List<LocalTime> sortedTimes = Arrays.stream(ISSUE_TIMES)
            .sorted()
            .toList();

        for (LocalTime candidate : sortedTimes) {
            ZonedDateTime candidateTime = ZonedDateTime.of(reference.toLocalDate(), candidate,
                SEOUL_ZONE_ID);
            if (reference.isBefore(candidateTime)) {
                log.debug("[OpenWeather] 다음 예보 기준 시각 결정 reference:{}, issueTime:{}", reference,
                    candidate);
                return candidate;
            }
        }

        // 오늘 모든 issueTime이 지났으면 → 마지막 시간 21:00 반환
        return ISSUE_TIMES[0];
    }


    public ZonedDateTime getZonedDateTime(LocalDate baseDate, LocalTime baseTime) {
        ZonedDateTime result = ZonedDateTime.of(baseDate, baseTime, SEOUL_ZONE_ID);
        log.debug("[OpenWeather] zone 포함 일시 생성 baseDate:{}, baseTime:{}, result:{}", baseDate,
            baseTime, result);
        return result;
    }


}
