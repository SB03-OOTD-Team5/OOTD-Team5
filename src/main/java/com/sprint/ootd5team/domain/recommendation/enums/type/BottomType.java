package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import java.util.Arrays;
import java.util.List;

/**
 * 하의 종류 Enum
 * - 문자열 기반 자동 분류
 * - 날씨(온도, 습도, 강수) 기반 점수 계산
 */
public enum BottomType {
    JEANS(List.of("청바지", "데님", "jean", "denim")),
    SLACKS(List.of("슬랙스", "slacks")),
    SKIRT(List.of("스커트", "치마", "skirt")),
    SHORTS(List.of("반바지", "shorts")),
    JOGGER(List.of("조거", "트레이닝", "training", "jogger")),
    WIDE_PANTS(List.of("와이드", "wide")),
    OTHER(List.of());

    private final List<String> keywords;

    BottomType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static BottomType fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        String lower = value.toLowerCase();
        return Arrays.stream(values())
            .filter(t -> t.keywords.stream().anyMatch(lower::contains))
            .findFirst()
            .orElse(OTHER);
    }

    /** 날씨 기반 점수 */
    public double getWeatherScore(WeatherInfoDto w) {
        double temp = w.temperature();
        double humidity = w.humidity();
        double rainProb = w.precipitationProbability();
        PrecipitationType precip = w.precipitationType();

        double score = 0;

        switch (this) {
            case SHORTS -> {
                if (temp >= 25) {
                    score += 6;
                }
                if (temp < 20) {
                    score -= 3;
                }
                if (rainProb > 0.5) {
                    score -= 1;
                }
            }
            case SKIRT -> {
                if (temp >= 22) {
                    score += 5;
                }
                if (temp < 15) {
                    score -= 3;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 2;
                }
            }
            case JEANS -> {
                if (temp >= 10 && temp <= 20) {
                    score += 4;
                }
                if (humidity > 85) {
                    score -= 1;
                }
            }
            case SLACKS -> {
                if (temp >= 15 && temp <= 25) {
                    score += 3;
                }
                if (rainProb > 0.4) {
                    score -= 1;
                }
            }
            case JOGGER -> {
                if (temp <= 15) {
                    score += 4;
                }
                if (temp < 5) {
                    score += 1;
                }
            }
            case WIDE_PANTS -> {
                if (temp >= 15 && temp <= 25) {
                    score += 3;
                }
                if (precip.isRainy()) {
                    score -= 1;
                }
            }
            default -> {
            }
        }

        return score;
    }
}
