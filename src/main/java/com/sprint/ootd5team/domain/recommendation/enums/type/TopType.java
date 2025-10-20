package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;

/**
 * 상의 종류 Enum
 * - 문자열 기반 자동 분류
 * - 날씨(온도, 습도, 강수) 기반 점수 계산
 */
public enum TopType {
    T_SHIRT("티셔츠", new String[]{"tshirt", "t-shirt"}),
    SHIRT("셔츠", new String[]{"남방", "shirt"}),
    KNIT("니트", new String[]{"스웨터", "knit", "sweater"}),
    HOODIE("후드티", new String[]{"후드", "hood"}),
    SWEATSHIRT("맨투맨", new String[]{"sweatshirt", "sweat"}),
    BLOUSE("블라우스", new String[]{"blouse"}),
    OTHER("기타", new String[]{});

    private final String displayName;
    private final String[] aliases;

    TopType(String displayName, String[] aliases) {
        this.displayName = displayName;
        this.aliases = aliases;
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
                if (feels <= 10) score += 5;
                else if (feels <= 15) score += 4;
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

        return Math.max(-5, Math.min(5, score));
    }
}
