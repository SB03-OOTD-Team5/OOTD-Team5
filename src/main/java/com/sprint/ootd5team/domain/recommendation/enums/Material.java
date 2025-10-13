package com.sprint.ootd5team.domain.recommendation.enums;

import java.util.Arrays;

public enum Material {
    COTTON("면"),
    LINEN("린넨"),
    WOOL("울"),
    POLY("폴리"),
    NYLON("나일론"),
    LEATHER("가죽"),
    DOWN("패딩"),
    DENIM("데님"),
    OTHER("기타");

    private final String displayName;

    Material(String displayName) {
        this.displayName = displayName;
    }

    public static Material fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        return Arrays.stream(values())
            .filter(m -> value.contains(m.displayName))
            .findFirst()
            .orElse(OTHER);
    }

    public String displayName() {
        return displayName;
    }

    /** 소재 간 궁합 평가 */
    public boolean isCompatibleWith(Material other) {
        if (this == other) {
            return true;
        }

        // 예시) 소재 질감이 비슷한 경우
        if ((this == COTTON && other == LINEN)
            || (this == POLY && other == NYLON)
            || (this == WOOL && other == COTTON)) {
            return true;
        }

        // 안 어울리는 조합
        if ((this == WOOL && other == LINEN)
            || (this == DOWN && other == COTTON)
            || (this == LEATHER && other == LINEN)) {
            return false;
        }

        return true; // 기본은 허용
    }
}
