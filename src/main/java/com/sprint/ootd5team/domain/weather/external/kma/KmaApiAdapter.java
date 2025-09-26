package com.sprint.ootd5team.domain.weather.external.kma;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.weather.exception.ConvertCoordFailException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaParseException;
import com.sprint.ootd5team.domain.weather.external.kma.KmaGridConverter.GridXY;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @class KmaApiAdapter
 * @brief 기상청(KMA) API를 호출해 좌표 변환 및 날씨 데이터를 조회/파싱하는 어댑터
 */
@Slf4j
@Component
public class KmaApiAdapter {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String[] baseTimes = {"2300", "2000", "1700", "1400", "1100", "0800",
        "0500", "0200"};
    private final WebClient kmaWebClient;
    private final ObjectMapper mapper;

    public KmaApiAdapter(@Qualifier("kmaWebClient") WebClient kmaWebClient,
        ObjectMapper mapper) {
        this.kmaWebClient = kmaWebClient;
        this.mapper = mapper;
    }

    /**
     * @param baseDate  예보 기준 날짜(yyyyMMdd)
     * @param baseTime  예보 기준 시간(HHmm)
     * @param latitude  위도
     * @param longitude 경도
     * @return 파싱된 기상청 응답 DTO
     * @brief 주어진 날짜/시간과 좌표로 기상청 API에서 날씨 예보 데이터를 조회한다
     */
    public KmaResponseDto getKmaWeather(String baseDate, String baseTime, BigDecimal latitude,
        BigDecimal longitude) {
        String responseJson = requestJsonFromKma(baseDate, baseTime, latitude, longitude);
        KmaResponseDto kmaResponseDto = parseToKmaResponseDto(responseJson);
        validateKmaData(kmaResponseDto);
        return kmaResponseDto;
    }

    /**
     * @param baseDate 예보 기준 날짜(yyyyMMdd)
     * @return 기상청에서 요구하는 base time 문자열
     * @brief 현재 시각을 기준으로 기상청 API 호출에 사용할 base time을 계산한다
     */
    public String getBaseTime(String baseDate) {
        // 발표 시간 파라미터 현재 시간 기준으로 자동 설정
        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE_ID);

        String baseTime = "0200";
        for (String t : baseTimes) {
            int hh = Integer.parseInt(t.substring(0, 2));
            int mm = Integer.parseInt(t.substring(2, 4));
            LocalDateTime cand = now.withHour(hh).withMinute(mm).withSecond(0).withNano(0);
            if (now.isAfter(cand.plusMinutes(10))) {
                baseTime = t;
                break;
            }
        }

        return baseTime;
    }


    /**
     * @param baseDate  예보 기준 날짜(yyyyMMdd)
     * @param baseTime  예보 기준 시간(HHmm)
     * @param latitude  위도
     * @param longitude 경도
     * @return 기상청 응답 JSON 문자열
     * @brief 기상청 API를 호출해 원시 JSON 문자열을 가져온다
     */
    private String requestJsonFromKma(String baseDate, String baseTime, BigDecimal latitude,
        BigDecimal longitude) {
        GridXY kmaXY = convertGridXY(latitude, longitude);

        try {
            log.debug(
                "[Weather] 날씨 정보 조회 요청 longitude:{},latitude:{},x:{},y:{},base date:{},base time:{}",
                longitude, latitude,
                kmaXY.x(), kmaXY.y(), baseDate, baseTime);

            String response = kmaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", kmaXY.x())
                    .queryParam("ny", kmaXY.y())
                    .build())
                .exchangeToMono(
                    clientResponse -> clientResponse.bodyToMono(String.class)
                )
                .block();
            log.debug("[Weather] 날씨 정보 Fetch 완료");

            return response;
        } catch (Exception e) {
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
            log.debug("[Weather] 좌표변환완료: x:{},y:{}", gridXY.x(), gridXY.y());
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
    private KmaResponseDto parseToKmaResponseDto(String responseJson) {
        try {
            return mapper.readValue(responseJson, KmaResponseDto.class);
        } catch (Exception e) {
            throw new WeatherKmaParseException(e.getMessage());
        }
    }

    /**
     * @param kmaDto 기상청 응답 DTO
     * @brief 기상청 응답 코드가 성공인지 검증한다
     */
    private void validateKmaData(KmaResponseDto kmaDto) {
        if (!kmaDto.response().header().resultCode().equals("00")) {
            throw new WeatherKmaParseException(kmaDto.response().header().resultMsg());
        }
    }
}
