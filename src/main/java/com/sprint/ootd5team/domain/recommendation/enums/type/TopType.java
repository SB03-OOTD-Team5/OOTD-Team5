package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
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

    /** 날씨 기반 점수 */
    public double getWeatherScore(RecommendationInfoDto info) {
        double feels = info.personalFeelsTemp();
        double score = 0;

        switch (this) {
            case T_SHIRT, BLOUSE -> {
                if (feels >= 23) score += 5;
                else if (feels >= 18) score += 2;
                else score -= 3;
            }

            case SHIRT -> {
                if (feels >= 16 && feels <= 25) score += 4;
                else if (feels < 10) score -= 2;
            }

            case KNIT -> {
                if (feels <= 15) score += 4;
                else if (feels <= 10) score += 5;
                else score -= 2;
            }

            // 후드/맨투맨 (간절기)
            case HOODIE, SWEATSHIRT -> {
                if (feels >= 10 && feels <= 18) score += 4;
                else if (feels < 8) score += 2;
                else score -= 2;
            }

            default -> {
            }
        }

        return score;
    }
}
