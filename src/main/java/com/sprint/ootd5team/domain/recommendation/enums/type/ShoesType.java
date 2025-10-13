package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import java.util.Arrays;
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

    /** 문자열 기반 추론 (속성값 또는 이름) */
    public static ShoesType fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        String lower = value.toLowerCase();
        return Arrays.stream(values())
            .filter(type -> type.keywords.stream().anyMatch(lower::contains))
            .findFirst()
            .orElse(OTHER);
    }

    /** 날씨 기반 의상 단품 점수 */
    public double getWeatherScore(WeatherInfoDto weatherInfoDto) {
        double score = 0.0;

        double temperature = weatherInfoDto.temperature();
        PrecipitationType precip = weatherInfoDto.precipitationType();
        WindspeedLevel level = weatherInfoDto.windSpeedLevel();
        double precipitationProbability = weatherInfoDto.precipitationProbability();

        switch (this) {
            case RAIN_BOOTS -> {
                if (precip.isRainy() || precip.isSnowy()) {
                    score += 10;
                }
                if (temperature < 10) {
                    score += 3;
                }
                if (precipitationProbability > 0.5) {
                    score += 3;
                }
            }
            case BOOTS -> {
                if (precip.isRainy() || precip.isSnowy()) {
                    score += 3;
                }
                if (temperature < 5) {
                    score += 6;
                }
                if (temperature < 10) {
                    score += 5;
                }
            }
            case SANDALS -> {
                if (temperature > 25) {
                    score += 5;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 3;
                }
                if (WindspeedLevel.STRONG.equals(level)) {
                    score -= 3;
                }
            }
            case SNEAKERS -> {
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 2;
                }
                if (temperature >= 15 && temperature <= 25) {
                    score += 2;
                }
            }
            case LOAFERS -> {
                if (precip.isRainy()) {
                    score -= 1;
                }
            }
            case HEELS -> {
                if (precip.isRainy() || precip.isSnowy()) {
                    score -= 2;
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



