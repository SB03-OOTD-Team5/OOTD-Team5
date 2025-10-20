package com.sprint.ootd5team.domain.weather.external.kma;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.util.DateTimeUtils;
import com.sprint.ootd5team.domain.weather.exception.ConvertCoordFailException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaParseException;
import com.sprint.ootd5team.domain.weather.external.WeatherExternalAdapter;
import com.sprint.ootd5team.domain.weather.external.kma.KmaGridConverter.GridXY;
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

/**
 * @class KmaApiAdapter
 * @brief 기상청(KMA) API를 호출해 좌표 변환 및 날씨 데이터를 조회/파싱하는 어댑터
 */
@Slf4j
@Component
public class KmaApiAdapter implements WeatherExternalAdapter<KmaResponse> {

    private static final LocalTime[] ISSUE_TIMES = {
        LocalTime.of(23, 0),
        LocalTime.of(20, 0),
        LocalTime.of(17, 0),
        LocalTime.of(14, 0),
        LocalTime.of(11, 0),
        LocalTime.of(8, 0),
        LocalTime.of(5, 0),
        LocalTime.of(2, 0)};
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(6); // 타임아웃 대기시간 설정
    private final WebClient kmaWebClient;
    private final ObjectMapper mapper;

    public KmaApiAdapter(@Qualifier("kmaWebClient") WebClient kmaWebClient,
        ObjectMapper mapper) {
        this.kmaWebClient = kmaWebClient;
        this.mapper = mapper;
    }

    /**
     * @param latitude  위도
     * @param longitude 경도
     * @param issueDate 예보 기준 날짜(yyyyMMdd)
     * @param issueTime 예보 기준 시간(HHmm)
     * @return 파싱된 기상청 응답 DTO
     * @brief 주어진 날짜/시간과 좌표로 기상청 API에서 날씨 예보 데이터를 조회한다
     */
    @Override
    public KmaResponse getWeather(BigDecimal latitude, BigDecimal longitude, String issueDate,
        String issueTime, int limit) {
        // 1. fetchRowData
        String responseJson = fetchRawData(issueDate, issueTime, latitude, longitude, limit);
        // 2. parseToResponse
        KmaResponse response = parseToKmaResponseDto(responseJson);
        //3. validate response
        validateKmaData(response);
        return response;
    }

    // reference 시간과 가장 가까운 발행 시각(reference보다 이전 시간 가져옴)
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
        log.debug("발행 시각 계산 결정 reference:{}, issueTime:{}", reference, issueTime);
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
                log.debug("[OpenWeatherAdapter] 다음 예보 기준 시각 결정 reference:{}, issueTime:{}",
                    reference,
                    candidate);
                return candidate;
            }
        }

        // 오늘 모든 issueTime이 지났으면 → 마지막 시간 21:00 반환
        return ISSUE_TIMES[0];
    }

    /**
     * @param baseDate  예보 기준 날짜(yyyyMMdd)
     * @param baseTime  예보 기준 시간(HHmm)
     * @param latitude  위도
     * @param longitude 경도
     * @return 기상청 응답 JSON 문자열
     * @brief 기상청 API를 호출해 원시 JSON 문자열을 가져온다
     */
    private String fetchRawData(String baseDate, String baseTime, BigDecimal latitude,
        BigDecimal longitude, int limit) {
        GridXY kmaXY = convertGridXY(latitude, longitude);
        try {
            log.info(
                "[KMA] 날씨 호출 longitude:{},latitude:{},x:{},y:{},base date:{},base time:{}",
                longitude, latitude,
                kmaXY.x(), kmaXY.y(), baseDate, baseTime);

            return kmaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", limit)
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", kmaXY.x())
                    .queryParam("ny", kmaXY.y())
                    .build())
                .exchangeToMono(
                    clientResponse -> clientResponse.bodyToMono(String.class)
                )
                .block();

        } catch (RuntimeException e) {
            Throwable cause = Exceptions.unwrap(e);
            if (cause instanceof TimeoutException) {
                log.warn("[KMA] API 호출 타임아웃", cause);
                throw new WeatherKmaFetchException("KMA 응답이 지연되었습니다.");
            }
            throw new WeatherKmaFetchException();
        }
    }


    /**
     * @param latitude  위도
     * @param longitude 경도
     * @return 기상청 격자 좌표
     * @brief 위/경도를 기상청 격자 좌표로 변환한다
     */
    private GridXY convertGridXY(BigDecimal latitude, BigDecimal longitude) {
        try {
            GridXY gridXY = KmaGridConverter.toGrid(longitude, latitude);
            log.debug("[KMA] 좌표변환완료: x:{},y:{}", gridXY.x(), gridXY.y());
            return gridXY;
        } catch (Exception e) {
            ConvertCoordFailException ex = new ConvertCoordFailException();
            ex.addDetail("latitude", latitude);
            ex.addDetail("longitude", longitude);
            throw ex;
        }
    }

    /**
     * @param responseJson 기상청 응답 JSON 문자열
     * @return 파싱된 기상청 DTO
     * @brief 기상청 응답 JSON을 DTO로 파싱한다
     */
    private KmaResponse parseToKmaResponseDto(String responseJson) {
        try {
            return mapper.readValue(responseJson, KmaResponse.class);
        } catch (Exception e) {
            throw new WeatherKmaParseException(e.getMessage());
        }
    }

    /**
     * @param kmaDto 기상청 응답 DTO
     * @brief 기상청 응답 코드가 성공인지 검증한다
     */
    private void validateKmaData(KmaResponse kmaDto) {
        if (!kmaDto.response().header().resultCode().equals("00")) {
            throw new WeatherKmaParseException(kmaDto.response().header().resultMsg());
        }
    }
}
