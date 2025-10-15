package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
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

    /** 날씨 기반 점수 계산 */
    public double getWeatherScore(RecommendationInfoDto info) {
        double score = 0.0;

        double feels = info.personalFeelsTemp();

        WeatherInfoDto w = info.weatherInfo();
        WindspeedLevel windSpeedLevel = w.windspeedLevel();
        double rainProb = w.precipitationProbability();
        PrecipitationType precip = w.precipitationType();

        switch (this) {
            case CARDIGAN -> {
                if (feels >= 12 && feels <= 22) {
                    score += 4;
                } else if (feels < 10) {
                    score -= 2;
                }
                if (rainProb > 0.5) {
                    score -= 1;
                }
            }
            case JACKET -> {
                if (feels >= 9 && feels <= 17) {
                    score += 4;
                } else if (feels < 7) {
                    score += 1;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 2;
                }
            }
            case TRENCH_COAT -> {
                if (feels >= 8 && feels <= 12) {
                    score += 5;
                }
                if (precip.isRainy() || rainProb > 0.4) {
                    score += 2;
                }
            }
            case COAT -> {
                if (feels <= 8 && feels >= 2) {
                    score += 4;
                } else if (feels < 2) {
                    score += 2;
                } else if (feels > 15) {
                    score -= 2;
                }
                if (precip.isSnowy()) {
                    score += 1;
                }
            }
            case PADDING -> {
                if (feels < 5) {
                    score += 6;
                } else if (feels < 10) {
                    score += 3;
                } else {
                    score -= 3;
                }
                if (precip.isSnowy() || precip.isRainy()) {
                    score += 1;
                }
                if (WindspeedLevel.STRONG.equals(windSpeedLevel)) {
                    score += 1;
                }
            }

            default -> {
            }
        }

        return score;
    }
}
