package com.sprint.ootd5team.domain.recommendation.enums;

import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import java.util.Arrays;

public enum Material {
    COTTON("면"),
    LINEN("린넨"),
    WOOL("울"),
    POLY("폴리"),
    NYLON("나일론"),
    RAYON("레이온"),
    FLEECE("기모"),
    KNIT("니트"),
    LEATHER("가죽"),
    DENIM("데님"),
    OTHER("기타");

    private final String displayName;

    Material(String displayName) {
        this.displayName = displayName;
    }

    public static Material fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        return Arrays.stream(values())
            .filter(m -> value.contains(m.displayName))
            .findFirst()
            .orElse(OTHER);
    }

    public String displayName() {
        return displayName;
    }

    /** 날씨 기반 의상 단품 점수 */
    public double getWeatherScore(WeatherInfoDto weatherInfoDto) {
        double score = 0.0;
        double temperature = weatherInfoDto.temperature();
        PrecipitationType precip = weatherInfoDto.precipitationType();
        double precipitationProbability = weatherInfoDto.precipitationProbability();
        double humidity = weatherInfoDto.humidity();

        switch (this) {
            // 여름에 적합한 소재
            case LINEN, COTTON, RAYON -> {
                if (temperature >= 25) {
                    score += 5;
                } else if (temperature >= 15) {
                    score += 2;
                } else {
                    score -= 2;
                }
                if (humidity >= 70) {
                    score += 3;
                }
            }

            // 겨울에 적합한 소재
            case WOOL, FLEECE, KNIT -> {
                if (temperature <= 5) {
                    score += 6;
                } else if (temperature <= 10) {
                    score += 3;
                } else {
                    score -= 2;
                }
                if (humidity <= 30) {
                    score -= 1;
                }
            }

            // 중간 계절용
            case DENIM, POLY -> {
                if (temperature >= 10 && temperature <= 20) {
                    score += 3;
                } else {
                    score -= 1;
                }
            }

            // 방풍·방수 계열
            case NYLON, LEATHER -> {
                if (temperature <= 10) {
                    score += 3;
                }
                if (temperature > 25) {
                    score -= 3;
                }
                if (precip.isRainy() || precip.isSnowy()) {
                    score += 5;
                }
                if (precipitationProbability > 0.5) {
                    score += 3;
                }
                if (humidity >= 70) {
                    score -= 2;
                }
            }

            default -> {
                score += 0;
            }
        }

        return score;
    }

    /** 소재 간 조화 점수 */
    public double getCompatibilityScore(Material other) {
        if (this == OTHER || other == OTHER) {
            return 0;
        }

        if (this == other) {
            return 2.5;
        }

        if ((this == COTTON && other == LINEN) ||
            (this == POLY && other == NYLON) ||
            (this == WOOL && other == KNIT) ||
            (this == DENIM && other == COTTON)) {
            return 1.5;
        }

        if ((this == WOOL && other == LINEN) ||
            (this == LEATHER && other == LINEN) ||
            (this == LEATHER && other == FLEECE)) {
            return -1.5;
        }

        // 완전 비호환
        if ((this == LEATHER && other == POLY) ||
            (this == FLEECE && other == LINEN)) {
            return -2.0;
        }

        // 기본 중립
        return 0.5;
    }
}
