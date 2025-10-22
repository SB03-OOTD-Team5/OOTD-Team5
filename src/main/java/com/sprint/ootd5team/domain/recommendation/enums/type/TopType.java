package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;

/**
 * 상의 종류 Enum
 * - 문자열 기반 자동 분류
 * - 날씨(온도, 습도, 강수) 기반 점수 계산
 */
public enum TopType {
    SLEEVELESS("나시", new String[]{"민소매", "sleeveless", "tanktop", "tank", "나시티"}),
    SHORT_SLEEVE("반팔티", new String[]{"반팔", "halfsleeve", "하프슬리브", "반소매", "short"}),
    T_SHIRT("티셔츠", new String[]{"tshirt", "t-shirt", "롱슬리브", "longsleeeve", "긴팔티"}),
    SHIRT("셔츠", new String[]{"남방", "shirt"}),
    KNIT("니트", new String[]{"스웨터", "knit", "sweater"}),
    HOODIE("후드티", new String[]{"후드", "hood", "후디", "hoodie", "후디드"}),
    SWEATSHIRT("맨투맨", new String[]{"sweatshirt", "sweat", "스웨트셔츠", "하프집업", "스웻셔츠"}),
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
            case BLOUSE -> {
                if (feels >= 23) score += 3;
                else if (feels >= 18) score += 2;
                else score -= 3;
            }

            case SHIRT -> {
                if (feels >= 16 && feels <= 25) score += 3;
                else if (feels < 10) score -= 2;
            }

            case KNIT -> {
                if (feels <= 10) score += 3;
                else if (feels <= 15) score += 2;
                else score -= 2;
            }

            // 후드/맨투맨 (간절기)
            case HOODIE, SWEATSHIRT -> {
                if (feels >= 10 && feels <= 18) score += 3;
                else if (feels < 8) score += 2;
                else score -= 2;
            }

            default -> {
                score += 1.5;
            }
        }

        return Math.max(-5, Math.min(5, score));
    }
}
