package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;

/**
 * 하의 종류 Enum
 * - 문자열 기반 자동 분류
 * - 날씨(온도, 습도, 강수) 기반 점수 계산
 */
public enum BottomType {
    JEANS("데님팬츠", new String[]{"데님", "jean", "denim", "진", "흑청", "청바지"}),
    SLACKS("슬랙스", new String[]{"slacks"}),
    COTTON_PANTS("코튼팬츠", new String[]{"cotton", "코튼", "치노"}),
    SKIRT("스커트", new String[]{"치마", "skirt"}),
    SHORTS("반바지", new String[]{"shorts", "숏", "쇼츠"}),
    JOGGER("조거팬츠", new String[]{"트레이닝", "training", "jogger", "조거", "스웻", "스웨트", "카고", "트랙"}),
    OTHER("기타", new String[]{});

    private final String displayName;
    private final String[] aliases;

    BottomType(String displayName, String[] aliases) {
        this.displayName = displayName;
        this.aliases = aliases;
    }

    /** 날씨 기반 점수 */
    public double getWeatherScore(RecommendationInfoDto info) {
        if (info == null || info.weatherInfo() == null) {
            return 0.0;
        }
        double feels = info.personalFeelsTemp();

        double score = 0;

        switch (this) {
            case SHORTS -> {
                if (feels >= 26) {
                    score += 3;
                } else if (feels >= 22) {
                    score += 2;
                } else {
                    score -= 3;
                }
            }
            case SKIRT -> {
                if (feels >= 22) {
                    score += 3;
                } else if (feels >= 18) {
                    score += 2;
                } else {
                    score -= 3;
                }
            }
            default -> {}
        }

        return Math.max(-5, Math.min(5, score));
    }
}
