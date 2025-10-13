package com.sprint.ootd5team.domain.recommendation.enums;

import java.util.Arrays;

public enum Material {
    COTTON("면"),
    LINEN("린넨"),
    WOOL("울"),
    POLY("폴리"),
    NYLON("나일론"),
    RAYON("레이온"),
    FLEECE("기모"),
    KNIT("니트"),
    LEATHER("가죽"),
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

        // 기타
        if (this == OTHER || other == OTHER) {
            return true;
        }

        // 질감이 유사한 조합 (긍정)
        if ((isLightFabric(this) && isLightFabric(other)) ||   // 면 ↔ 린넨 ↔ 레이온
            (isSynthetic(this) && isSynthetic(other)) ||       // 폴리 ↔ 나일론
            (isWarmFabric(this) && isWarmFabric(other)) ||     // 울 ↔ 니트 ↔ 기모
            (isStructuredFabric(this) && isStructuredFabric(other))) { // 가죽 ↔ 데님
            return true;
        }

        // 질감 대비
        if ((isLightFabric(this) && isStructuredFabric(other)) ||
            (isStructuredFabric(this) && isLightFabric(other)) ||
            (this == LEATHER && other == LINEN) ||
            (this == LEATHER && other == RAYON) ||
            (this == WOOL && other == LINEN)) {
            return false;
        }

        return true;
    }

    private boolean isLightFabric(Material m) {
        return m == COTTON || m == LINEN || m == RAYON;
    }

    private boolean isSynthetic(Material m) {
        return m == POLY || m == NYLON;
    }

    private boolean isWarmFabric(Material m) {
        return m == WOOL || m == FLEECE || m == KNIT;
    }

    private boolean isStructuredFabric(Material m) {
        return m == LEATHER || m == DENIM;
    }
}
