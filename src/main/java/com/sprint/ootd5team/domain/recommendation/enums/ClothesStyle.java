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

    /**
     * 스타일 조화 판단
     */
    public boolean isHarmoniousWith(ClothesStyle other) {
        if (this == other) {
            return true;
        }

        // 캐주얼은 대부분 어울림
        if (this == CASUAL || other == CASUAL) {
            return true;
        }

        // 기타는 통과
        if (this == OTHER || other == OTHER) {
            return true;
        }

        // 보너스 조합
        if ((this == STREET && other == SPORTY)
            || (this == SPORTY && other == STREET)
            || (this == VINTAGE && other == CLASSIC)
            || (this == CLASSIC && other == VINTAGE)) {
            return true;
        }

        // 감점 조합
        if ((this == FORMAL && other == STREET)
            || (this == FORMAL && other == SPORTY)
            || (this == CLASSIC && other == STREET)
            || (this == STREET && other == CLASSIC)
            || (this == FORMAL && other == VINTAGE)
            || (this == VINTAGE && other == FORMAL)) {
            return false;
        }

        return true;
    }
}
