package com.sprint.ootd5team.domain.recommendation.enums;

import java.util.Arrays;

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

    public static ClothesStyle fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        return Arrays.stream(values())
            .filter(s -> value.contains(s.displayName))
            .findFirst()
            .orElse(OTHER);
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
        if ((this == CASUAL && other == STREET) ||
            (this == CASUAL && other == SPORTY) ||
            (this == CLASSIC && other == FORMAL)) {
            return 1.5;
        }

        // 중간 정도 어울림
        if ((this == CASUAL && other == FORMAL) ||
            (this == STREET && other == VINTAGE) ||
            (this == SPORTY && other == CASUAL)) {
            return 0.5;
        }

        // 부조화
        if ((this == FORMAL && other == STREET) ||
            (this == FORMAL && other == SPORTY) ||
            (this == CLASSIC && other == STREET)) {
            return -2.0;
        }

        return 0.0;
    }
}
