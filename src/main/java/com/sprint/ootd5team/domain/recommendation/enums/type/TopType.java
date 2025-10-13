package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import java.util.Arrays;
import java.util.List;

/**
 * 상의 종류 Enum
 * - 문자열 기반 자동 분류
 * - 날씨(온도, 습도, 강수) 기반 점수 계산
 */
public enum TopType {
    T_SHIRT(List.of("티셔츠", "tshirt", "티", "t-shirt")),
    SHIRT(List.of("셔츠", "남방", "shirt")),
    KNIT(List.of("니트", "스웨터", "knit", "sweater")),
    HOODIE(List.of("후드", "hood")),
    SWEATSHIRT(List.of("맨투맨", "sweat")),
    BLOUSE(List.of("블라우스", "blouse")),
    OTHER(List.of());

    private final List<String> keywords;

    TopType(List<String> keywords) {
        this.keywords = keywords;
    }

    public static TopType fromString(String value) {
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
            case T_SHIRT, BLOUSE -> {
                if (temp >= 23) {
                    score += 5;
                }
                if (temp < 15) {
                    score -= 3;
                }
            }
            case SHIRT -> {
                if (temp >= 17 && temp <= 25) {
                    score += 3;
                }
                if (rainProb > 0.5) {
                    score -= 1;
                }
            }
            case KNIT -> {
                if (temp <= 15) {
                    score += 4;
                }
                if (humidity > 80) {
                    score -= 1;
                }
            }
            case HOODIE, SWEATSHIRT -> {
                if (temp <= 18 && temp >= 10) {
                    score += 3;
                }
                if (temp < 10) {
                    score += 1;
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
