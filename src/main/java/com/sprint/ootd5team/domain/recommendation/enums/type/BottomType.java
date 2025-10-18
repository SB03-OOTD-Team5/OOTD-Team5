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
    JEANS("청바지", new String[]{"데님", "jean", "denim"}),
    SLACKS("슬랙스", new String[]{"slacks"}),
    SKIRT("스커트", new String[]{"치마", "skirt"}),
    SHORTS("반바지", new String[]{"shorts"}),
    JOGGER("조거팬츠", new String[]{"트레이닝", "training", "jogger"}),
    WIDE_PANTS("와이드팬츠", new String[]{"wide"}),
    OTHER("기타", new String[]{});

    private final String displayName;
    private final String[] aliases;

    BottomType(String displayName, String[] aliases) {
        this.displayName = displayName;
        this.aliases = aliases;
    }

    /** 날씨 기반 점수 */
    public double getWeatherScore(RecommendationInfoDto info) {
        if (info == null || info.weatherInfo() == null) return 0.0;
        double feels = info.personalFeelsTemp();

        WeatherInfoDto w = info.weatherInfo();
        double rainProb = w.precipitationProbability();
        PrecipitationType precip = w.precipitationType();

        boolean isRainy = precip != null && precip.isRainy();
        boolean isSnowy = precip != null && precip.isSnowy();

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
                if (isRainy || rainProb > 0.4) {
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
                if (isRainy || isSnowy) {
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

        return Math.max(-5, Math.min(5, score));
    }
}
