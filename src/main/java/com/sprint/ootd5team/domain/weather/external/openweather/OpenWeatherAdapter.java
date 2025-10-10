package com.sprint.ootd5team.domain.weather.external.openweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.util.DateTimeUtils;
import com.sprint.ootd5team.domain.weather.exception.WeatherOpenFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherOpenParseException;
import com.sprint.ootd5team.domain.weather.external.WeatherExternalAdapter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
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
public class OpenWeatherAdapter implements WeatherExternalAdapter<OpenWeatherResponse> {

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
    private final WebClient openWeatherClient;
    private final ObjectMapper mapper;

    public OpenWeatherAdapter(
        @Qualifier("openWeatherClient") WebClient openWeatherClient, ObjectMapper mapper) {
        this.openWeatherClient = openWeatherClient;
        this.mapper = mapper;
    }

    @Override
    public OpenWeatherResponse getWeather(BigDecimal latitude, BigDecimal longitude,
        String issueDate, String issueTime, int limit) {
        // 1. fetchRowData
        String responseJson = fetchRawData(latitude, longitude);
        // 2. parseToResponse
        OpenWeatherResponse response = parseToResponseDto(responseJson);
        //3. validate response
        validateData(response);
        return response;
    }

    // reference 시간과 가장 가까운 발행 시각(reference보다 이전 시간 가져옴)
    @Override
    public LocalTime resolveIssueTime(ZonedDateTime reference) {
        LocalTime issueTime = ISSUE_TIMES[ISSUE_TIMES.length - 1];
        for (LocalTime candidate : ISSUE_TIMES) {
            ZonedDateTime candidateTime = ZonedDateTime
                .of(reference.toLocalDate(), candidate, DateTimeUtils.SEOUL_ZONE_ID);
            if (reference.isAfter(candidateTime.plusMinutes(10))) {
                issueTime = candidate;
                break;
            }
        }
        log.debug("[OpenWeather] 발행 시각 계산 결정 reference:{}, issueTime:{}", reference,
            issueTime);
        return issueTime;
    }

    // 현재시간과 가장 가까운 예보 시각(reference보다 이후 시간 가져옴)
    public LocalTime resolveTargetTime(ZonedDateTime reference) {
        List<LocalTime> sortedTimes = Arrays.stream(ISSUE_TIMES)
            .sorted()
            .toList();

        for (LocalTime candidate : sortedTimes) {
            ZonedDateTime candidateTime = ZonedDateTime.of(reference.toLocalDate(), candidate,
                DateTimeUtils.SEOUL_ZONE_ID);
            if (reference.isBefore(candidateTime)) {
                log.debug("[OpenWeather] 다음 예보 기준 시각 결정 reference:{}, issueTime:{}",
                    reference,
                    candidate);
                return candidate;
            }
        }

        // 오늘 모든 issueTime이 지났으면 → 마지막 시간 21:00 반환
        return ISSUE_TIMES[0];
    }


    private String fetchRawData(BigDecimal latitude, BigDecimal longitude) {
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


}
