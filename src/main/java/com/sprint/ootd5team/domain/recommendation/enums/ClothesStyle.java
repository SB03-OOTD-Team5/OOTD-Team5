package com.sprint.ootd5team.domain.recommendation.enums;

import java.util.Arrays;

public enum ClothesStyle {
    CASUAL("캐주얼"),
    FORMAL("포멀"),
    STREET("스트릿"),
    SPORTY("스포티"),
    CLASSIC("클래식"),
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

    public boolean isHarmoniousWith(ClothesStyle other) {
        if (this == other) {
            return true;
        }

        // 캐주얼은 대부분 어울림
        if (this == CASUAL || other == CASUAL) {
            return true;
        }

        // 강한 대비 (감점)
        if ((this == FORMAL && other == STREET)
            || (this == FORMAL && other == SPORTY)
            || (this == CLASSIC && other == STREET)) {
            return false;
        }

        return true;
    }
}
