package com.sprint.ootd5team.domain.recommendation.enums;

public enum ClothesStyle {
    CASUAL("캐주얼"),
    FORMAL("포멀"),
    STREET("스트릿"),
    SPORTY("스포티"),
    CLASSIC("클래식"),
    VINTAGE("빈티지"),
    OTHER("기타");

    private final String displayName;

    ClothesStyle(String displayName) {
        this.displayName = displayName;
    }

    private static boolean isPair(ClothesStyle a, ClothesStyle b, ClothesStyle x, ClothesStyle y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    /** 기본 스타일 궁합 점수 */
    public double getHarmonyScore(ClothesStyle other) {
        if (this == OTHER || other == OTHER) {
            return 0.0;
        }
        if (this == other) {
            return 2.5;
        }

        // 유사 계열
        if (isPair(this, other, CASUAL, STREET) ||
            isPair(this, other, CASUAL, SPORTY) ||
            isPair(this, other, CLASSIC, FORMAL)) {
            return 1.5;
        }

        // 중간
        if (isPair(this, other, CASUAL, FORMAL) ||
            isPair(this, other, STREET, VINTAGE)) {
            return 0.5;
        }

        // 부조화
        if (isPair(this, other, FORMAL, STREET) ||
            isPair(this, other, FORMAL, SPORTY) ||
            isPair(this, other, CLASSIC, STREET)) {
            return -2.0;
        }

        return 0.0;
    }
}
