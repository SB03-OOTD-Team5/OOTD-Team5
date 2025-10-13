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
        // 동일 톤
        if (this == other) {
            return 2.0;
        }

        // 무채색
        if (this == NEUTRAL || other == NEUTRAL) {
            return 1.0;
        }

        // 따뜻/차가움
        if ((this == WARM && other == COOL) || (this == COOL && other == WARM)) {
            return -2.0;
        }

        return 0.0;
    }
}
