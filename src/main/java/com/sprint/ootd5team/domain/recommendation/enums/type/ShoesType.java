package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import java.util.List;

/**
 * 신발 종류 Enum
 * - 키워드 기반 유추
 * - 날씨/온도에 따른 점수 계산
 */
public enum ShoesType {
    BOOTS(List.of("부츠", "boot")),
    SNEAKERS(List.of("운동화", "스니커즈", "sneaker")),
    SANDALS(List.of("샌들", "슬리퍼", "sand")),
    LOAFERS(List.of("로퍼", "loafer")),
    HEELS(List.of("힐", "heel", "구두")),
    RAIN_BOOTS(List.of("장화", "rain")),
    OTHER(List.of());

    private final List<String> keywords;

    ShoesType(List<String> keywords) {
        this.keywords = keywords;
    }

    /** 날씨 기반 의상 단품 점수 */
    public double getWeatherScore(RecommendationInfoDto info) {
        double score = 0.0;

        double feels = info.personalFeelsTemp();

        WeatherInfoDto weatherInfoDto = info.weatherInfo();
        PrecipitationType precip = weatherInfoDto.precipitationType();
        double rainProb = weatherInfoDto.precipitationProbability();
        WindspeedLevel wind = weatherInfoDto.windSpeedLevel();

        switch (this) {
            case RAIN_BOOTS -> {
                if (precip.isRainy() || precip.isSnowy() || rainProb > 0.4) {
                    score += 7;
                }
                if (feels < 10) {
                    score += 2;
                }
                if (feels > 25) {
                    score -= 3;
                }
            }

            case BOOTS -> {
                if (feels <= 5) {
                    score += 5;
                } else if (feels <= 10) {
                    score += 3;
                } else if (feels > 20) {
                    score -= 3;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score += 1;
                }
            }

            /** 여름용 샌들 */
            case SANDALS -> {
                if (feels >= 25) {
                    score += 5;
                } else if (feels >= 20) {
                    score += 2;
                } else {
                    score -= 2;
                }
                if (precip.isRainy() || precip.isSnowy() || rainProb > 0.4) {
                    score -= 3;
                }
                if (WindspeedLevel.STRONG.equals(wind)) {
                    score -= 2;
                }
            }

            /** 스니커즈 */
            case SNEAKERS -> {
                if (feels >= 12 && feels <= 25) {
                    score += 4;
                } else if (feels < 5 || feels > 28) {
                    score -= 1;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 2;
                }
            }

            /** 로퍼 */
            case LOAFERS -> {
                if (feels >= 15 && feels <= 25) {
                    score += 2;
                }
                if (precip.isRainy() || rainProb > 0.4) {
                    score -= 2;
                }
            }

            /** 힐 */
            case HEELS -> {
                if (feels >= 18 && feels <= 25) {
                    score += 2;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 3;
                }
                if (WindspeedLevel.STRONG.equals(wind)) {
                    score -= 1;
                }
            }

            default -> {
            }
        }

        return score;
    }

    /**
     * 스타일 기반 점수
     */
    public double getClothesStyle(ClothesStyle style) {
        return switch (this) {
            case SNEAKERS -> (style == ClothesStyle.CASUAL || style == ClothesStyle.STREET) ? 2 : 0;
            case LOAFERS -> (style == ClothesStyle.FORMAL || style == ClothesStyle.CLASSIC) ? 2 : 0;
            case HEELS -> (style == ClothesStyle.FORMAL) ? 1.5 : 0;
            default -> 0;
        };
    }
}



