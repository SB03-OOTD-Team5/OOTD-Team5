package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import java.util.Arrays;
import java.util.List;

/**
 * 아우터 종류 Enum
 * - 문자열 기반 자동 분류
 * - 날씨(온도, 강수, 풍속) 기반 점수 계산
 */
public enum OuterType {
    CARDIGAN(List.of("가디건", "cardigan")),
    COAT(List.of("코트", "coat")),
    TRENCH_COAT(List.of("트렌치", "trench")),
    PADDING(List.of("패딩", "다운", "점퍼", "padded")),
    JACKET(List.of("자켓", "재킷", "블레이저", "jacket")),
    OTHER(List.of());

    private final List<String> keywords;

    OuterType(List<String> keywords) {
        this.keywords = keywords;
    }

    /** 문자열 기반 추론 (속성값 또는 이름) */
    public static OuterType fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        String lower = value.toLowerCase();
        return Arrays.stream(values())
            .filter(type -> type.keywords.stream().anyMatch(lower::contains))
            .findFirst()
            .orElse(OTHER);
    }

    /** 날씨 기반 점수 계산 */
    public double getWeatherScore(WeatherInfoDto w) {
        double score = 0.0;
        double temp = w.temperature();
        double humidity = w.humidity();
        WindspeedLevel windSpeedLevel = w.windSpeedLevel();
        double rainProb = w.precipitationProbability();
        PrecipitationType precip = w.precipitationType();

        switch (this) {
            case CARDIGAN -> {
                if (temp >= 12 && temp <= 22) {
                    score += 4;
                }
                if (rainProb > 0.5) {
                    score -= 2;
                }
            }
            case JACKET -> {
                if (temp >= 9 && temp <= 17) {
                    score += 3;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 2;
                }
            }
            case TRENCH_COAT -> {
                if (temp >= 9 && temp <= 11) {
                    score += 4;
                }
                if (rainProb > 0.4 || precip.isRainy()) {
                    score += 1;
                }
            }
            case COAT -> {
                if (temp >= 5 && temp <= 8) {
                    score += 4;
                }
                if (temp < 5) {
                    score += 2;
                }
                if (humidity > 80) {
                    score -= 1;
                }
            }
            case PADDING -> {
                if (temp < 5) {
                    score += 6;
                }
                if (WindspeedLevel.STRONG.equals(windSpeedLevel)) {
                    score += 2;
                }
                if (precip.isSnowy() || precip.isRainy()) {
                    score += 1.5;
                }
            }
            default -> {
            }
        }

        return score;
    }
}
