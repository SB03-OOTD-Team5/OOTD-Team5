package com.sprint.ootd5team.domain.recommendation.enums;

public enum ColorTone {
    NEUTRAL("무채색"),
    WARM("따뜻한색"),
    COOL("차가운색"),
    OTHER("기타");

    private final String displayName;

    ColorTone(String displayName) {
        this.displayName = displayName;
    }

    /** 두 색상 간 조화 점수 */
    public double getHarmonyScore(ColorTone other) {
        double score = 0;
        // 동일 톤
        if (this == other) {
            score += 1.0;
        }

        // 무채색
        if (this == NEUTRAL && other == NEUTRAL) score += 2;
        if (this == NEUTRAL || other == NEUTRAL) score += 2;

        // 따뜻/차가움
        if ((this == WARM && other == COOL) || (this == COOL && other == WARM)) {
            score += 0.5;
        }

        return Math.max(-5, Math.min(5, score));
    }
}
