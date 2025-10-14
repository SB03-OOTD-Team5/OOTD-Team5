package com.sprint.ootd5team.domain.recommendation.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 기상청 공식 기반 체감온도 계산용 DTO
 * - 여름철(5~9월): 습구온도 기반 공식
 * - 겨울철(10~4월): 풍속 기반 공식
 * - 참고) https://data.kma.go.kr/climate/windChill/selectWindChillChart.do
 */
public record ApparentTemperatureDto(
    double temperature,   // 기온
    double humidity,      // 습도
    double windspeed,     // 풍속
    Instant forecastAt    // 예보
) {

    /** 체감온도 계산 (계절 구분) */
    public double calculateFeelsLike() {
        if (forecastAt == null) return temperature;

        int month = LocalDateTime.ofInstant(forecastAt, ZoneId.of("Asia/Seoul")).getMonthValue();
        if (month >= 5 && month <= 9) {
            return calculateSummerFeelsLike(temperature, humidity);
        } else {
            return calculateWinterFeelsLike(temperature, windspeed);
        }
    }

    /** 여름철 체감온도 (기상청 2022.6.2 개정 공식) */
    private static double calculateSummerFeelsLike(double ta, double rh) {
        double tw = calculateWetBulbTemperature(ta, rh);
        double at = -0.2442 + 0.55399 * tw + 0.45535 * ta
            - 0.0022 * Math.pow(tw, 2)
            + 0.00278 * tw * ta + 3.0;
        return round(at);
    }

    /** 겨울철 체감온도 (기상청 공식)
     * 기온 10°C 이하, 풍속 1.3m/s 이상일 때만 산출
     */
    private static double calculateWinterFeelsLike(double ta, double windspeed) {
        if (ta > 10 || windspeed < 1.3) return ta;
        double v = windspeed * 3.6; // m/s → km/h 변환
        double at = 13.12 + 0.6215 * ta
            - 11.37 * Math.pow(v, 0.16)
            + 0.3965 * Math.pow(v, 0.16) * ta;
        return round(at);
    }

    /** Stull 습구온도 추정식 */
    private static double calculateWetBulbTemperature(double ta, double rh) {
        return ta * Math.atan(0.151977 * Math.sqrt(rh + 8.313659))
            + Math.atan(ta + rh)
            - Math.atan(rh - 1.67633)
            + 0.00391838 * Math.pow(rh, 1.5) * Math.atan(0.023101 * rh)
            - 4.686035;
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /** 개인 민감도 반영한 체감온도 */
    public double calculatePersonalFeelsLike(int sensitivity) {
        double baseFeels = calculateFeelsLike();

        double bias = switch (sensitivity) {
            case 1 -> +1.5;  // 추위 많이탐
            case 2 -> +0.8;
            case 4 -> -0.8;
            case 5 -> -1.5;  // 더위 많이탐
            default -> 0.0;
        };

        return Math.round((baseFeels + bias) * 10.0) / 10.0;
    }
}
