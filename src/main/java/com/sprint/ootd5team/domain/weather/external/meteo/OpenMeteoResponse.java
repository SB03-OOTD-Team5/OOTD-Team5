package com.sprint.ootd5team.domain.weather.external.meteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** 응답 전체 중 필요한 필드만 정의 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(
    String timezone,                   // 예: "Asia/Seoul"
    Hourly hourly,                     // 시간별 데이터(사용하지 않을 수도 있음)
    Daily daily                        // 일간 데이터 묶음
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
        List<String> time,             // "2025-09-29T10:00" 형식(로컬 타임존 기준)
        @JsonProperty("temperature_2m")
        List<Double> temperature2m,
        @JsonProperty("relative_humidity_2m")
        List<Integer> relativeHumidity2m,
        List<Double> precipitation,    // mm
        @JsonProperty("precipitation_probability")
        List<Integer> precipitationProbability, // %
        @JsonProperty("wind_speed_10m")
        List<Double> windSpeed10m,     // 기본 m/s
        @JsonProperty("weather_code")
        List<Integer> weatherCode
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
        List<String> time,                         // "2025-09-29" 형식
        @JsonProperty("weather_code")
        List<Integer> weatherCode,
        @JsonProperty("temperature_2m_max")
        List<Double> temperatureMax,
        @JsonProperty("temperature_2m_min")
        List<Double> temperatureMin,
        @JsonProperty("precipitation_sum")
        List<Double> precipitationSum,
        @JsonProperty("precipitation_probability_max")
        List<Double> precipitationProbabilityMax,
        @JsonProperty("wind_speed_10m_max")
        List<Double> windSpeed10mMax,
        @JsonProperty("relative_humidity_2m_mean")
        List<Double> relativeHumidity2mMean
    ) {}
}