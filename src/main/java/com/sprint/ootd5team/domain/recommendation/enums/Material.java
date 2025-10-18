package com.sprint.ootd5team.domain.recommendation.enums;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public String displayName() {
        return displayName;
    }

    /** 날씨 기반 의상 단품 점수 */
    public double getWeatherScore(RecommendationInfoDto info) {
        double score = 0.0;
        double feelsLike = info.personalFeelsTemp();

        WeatherInfoDto weatherInfoDto = info.weatherInfo();
        PrecipitationType precip = weatherInfoDto.precipitationType();
        double precipitationProbability = weatherInfoDto.precipitationProbability();

        switch (this) {
            // 여름에 적합한 소재
            case LINEN, COTTON, RAYON -> {
                if (feelsLike >= 28) score += 3;
                else if (feelsLike >= 20) score += 2;
                else if (feelsLike >= 10) score += 0.5;
                else score -= 2;
            }

            // 겨울에 적합한 소재
            case WOOL, FLEECE, KNIT -> {
                if (feelsLike <= 0) score += 3;
                else if (feelsLike <= 5) score += 2;
                else if (feelsLike <= 10) score += 1;
                else if (feelsLike >= 20) score -= 2;
            }

            // 봄/가을용
            case DENIM, POLY -> {
                if (feelsLike >= 10 && feelsLike <= 22) score += 2.5;
                else if (feelsLike >= 5 && feelsLike <= 27) score += 1;
                else score -= 1.5;
            }

            // 방풍·방수 계열
            case NYLON, LEATHER -> {
                if (feelsLike <= 10) score += 3;
                if (feelsLike > 25) score -= 3;
                else score += 0.5;
            }

            default -> score += 0;
        }

        // 강수/강설 조건 추가 보정
        if (precip.isRainy() || precip.isSnowy()) {
            // 방수 재질 우대
            if (this == NYLON || this == LEATHER) score += 3;
            else if (this == POLY || this == DENIM) score += 1.5;
            else score += 0.5;
        }

        if (precipitationProbability > 0.3) {
            double factor = Math.min(precipitationProbability, 0.7);
            score += (factor - 0.3) * 2.0;
        }

        return Math.max(-5, Math.min(5, score));
    }

    /** 소재 간 조화 점수 */
    public double getCompatibilityScore(Material other) {
        if (this == OTHER || other == OTHER) {
            return 0;
        }

        if (this == other) {
            return 2.5;
        }

        if ((this == COTTON && other == LINEN) || (this == LINEN && other == COTTON) ||
            (this == POLY && other == NYLON) || (this == NYLON && other == POLY) ||
            (this == WOOL && other == KNIT) || (this == KNIT && other == WOOL) ||
            (this == DENIM && other == COTTON) || (this == COTTON && other == DENIM)) {
            return 1.5;
        }

        if ((this == WOOL && other == LINEN) || (this == LINEN && other == WOOL) ||
            (this == LEATHER && other == LINEN) || (this == LINEN && other == LEATHER) ||
            (this == LEATHER && other == FLEECE) || (this == FLEECE && other == LEATHER)) {
            return -1.5;
        }

        // 완전 비호환
        if ((this == LEATHER && other == POLY) || (this == POLY && other == LEATHER) ||
            (this == FLEECE && other == LINEN) || (this == LINEN && other == FLEECE)) {
            return -2.0;
        }

        // 기본 중립
        return 0.5;
    }
}
