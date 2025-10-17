package com.sprint.ootd5team.domain.weather.external.openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * OpenWeather 5일/3시간 예보 API 응답 DTO (record) - 온도 기본 단위: Kelvin (쿼리에 units=metric 추가 시 °C) - 시간:
 * dt(UTC epoch seconds), dtTxt(UTC ISO 문자열)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
    String cod,                 // 내부 코드 ex) 200
    Integer message,            // 내부 메시지
    Integer cnt,                // 리스트 길이
    List<ForecastItem> list,    // 3시간 간격 예보 리스트
    City city                   // 도시/위치 정보
) {

    /**
     * 개별 예보 아이템(3시간 단위)
     */
    public record ForecastItem(
        Long dt,                       // 예보 시각(유닉스 초, UTC)
        Main main,                     // 기온/기압/습도 등
        List<Weather> weather,         // 날씨 코드/설명/아이콘 (배열)
        Clouds clouds,                 // 구름량(%)
        Wind wind,                     // 바람 정보
        Integer visibility,            // 평균 가시거리(미터, 최대 10,000)
        Double pop,                // 강수 확률(0~1, 0%=0, 100%=1)
        Rain rain,                     // 3시간 강수량(mm) - 있을 때만 제공
        Snow snow,                     // 3시간 강설량(mm) - 있을 때만 제공
        Sys sys,                       // 주/야 파트 정보(pod)
        @JsonProperty("dt_txt")
        String dtTxt                   // 예보 시각(ISO 문자열, UTC)
    ) {

    }

    /**
     * 주요 기상 지표
     */
    public record Main(
        Double temp,               // 기온
        @JsonProperty("feels_like")
        Double feelsLike,          // 체감온도
        @JsonProperty("temp_min")
        Double tempMin,            // 최저기온(예보 최소)
        @JsonProperty("temp_max")
        Double tempMax,            // 최고기온(예보 최대)
        Integer pressure,              // 기압(hPa, 해수면 기준)
        @JsonProperty("sea_level")
        Integer seaLevel,              // 해수면 기압(hPa) - 제공 시
        @JsonProperty("grnd_level")
        Integer grndLevel,             // 지상 기압(hPa) - 제공 시
        Double humidity,              // 습도(%)
        @JsonProperty("temp_kf")
        Double tempKf              // 내부 파라미터(온도 보정값)
    ) {

    }

    /**
     * 날씨 현상 코드/설명
     */
    public record Weather(
        Integer id,                    // 날씨 코드 ID
        String main,                   // 그룹(Rain/Snow/Clouds 등)
        String description,            // 상세 설명(로컬라이즈 가능)
        String icon                    // 아이콘 ID(예: "10d")
    ) {

    }

    /**
     * 구름
     */
    public record Clouds(
        Integer all                    // 전운량(%)
    ) {

    }

    /**
     * 바람
     */
    public record Wind(
        Double speed,              // 풍속(m/s 기본; metric도 m/s)
        Integer deg,                   // 풍향(도, 0~360)
        Double gust                // 돌풍(m/s) - 제공 시
    ) {

    }

    /**
     * 비(최근 3시간 누적 강수량)
     */
    public record Rain(
        @JsonProperty("3h")
        Double threeHour           // 3시간(mm) - 키가 "3h"라 @JsonProperty 필요
    ) {

    }

    /**
     * 눈(최근 3시간 누적 강설량)
     */
    public record Snow(
        @JsonProperty("3h")
        Double threeHour           // 3시간(mm)
    ) {

    }

    /**
     * 주/야 파트 정보
     */
    public record Sys(
        String pod                     // "d"(day) 또는 "n"(night)
    ) {

    }

    /**
     * 도시 메타데이터
     */
    public record City(
        Long id,                       // 도시 ID
        String name,                   // 도시명
        Coord coord,                   // 위경도
        String country,                // 국가 코드(예: "IT")
        Integer population,            // 인구(옵션)
        Integer timezone,              // UTC 오프셋(초). 예: KST=32400
        Long sunrise,                  // 일출(유닉스 초, UTC)
        Long sunset                    // 일몰(유닉스 초, UTC)
    ) {

    }

    /**
     * 위경도 좌표
     */
    public record Coord(
        BigDecimal lat,                // 위도
        BigDecimal lon                 // 경도
    ) {

    }
}
