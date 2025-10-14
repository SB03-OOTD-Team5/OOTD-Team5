package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
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

    /** 날씨 기반 점수 */
    public double getWeatherScore(RecommendationInfoDto info) {
        double feels = info.personalFeelsTemp();

        WeatherInfoDto w = info.weatherInfo();
        double rainProb = w.precipitationProbability();
        PrecipitationType precip = w.precipitationType();

        double score = 0;

        switch (this) {
            case SHORTS -> {
                if (feels >= 26) {
                    score += 6;
                } else if (feels >= 22) {
                    score += 3;
                } else {
                    score -= 3;
                }
                if (precip.isRainy() || rainProb > 0.4) {
                    score -= 1;
                }
            }
            case SKIRT -> {
                if (feels >= 22) {
                    score += 5;
                } else if (feels >= 18) {
                    score += 2;
                } else {
                    score -= 3;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 2;
                }
            }
            case JEANS -> {
                if (feels >= 10 && feels <= 20) {
                    score += 4;
                } else if (feels < 8) {
                    score -= 1;
                } else if (feels > 25) {
                    score -= 2;
                }
            }
            case SLACKS -> {
                if (feels >= 15 && feels <= 25) {
                    score += 3;
                } else if (feels < 10 || feels > 28) {
                    score -= 2;
                }
                if (rainProb > 0.5) {
                    score -= 1;
                }
            }
            case JOGGER -> {
                if (feels <= 15) {
                    score += 4;
                } else if (feels < 5) {
                    score += 1;
                } else {
                    score -= 2;
                }
            }
            case WIDE_PANTS -> {
                if (feels >= 15 && feels <= 25) {
                    score += 3;
                } else if (feels < 10) {
                    score -= 2;
                }
                if (precip.isRainy() || rainProb > 0.4) {
                    score -= 1;
                }
            }

            default -> {
            }
        }

        return score;
    }
}
