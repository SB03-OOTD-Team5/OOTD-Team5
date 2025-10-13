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

    /** 문자열 색상명 → 톤 분류 */
    public static ColorTone fromColorName(String name) {
        if (name == null || name.isBlank()) {
            return OTHER;
        }

        String c = name.toLowerCase();
        if (c.contains("빨강") || c.contains("주황") || c.contains("노랑") || c.contains("베이지")) {
            return WARM;
        }
        if (c.contains("파랑") || c.contains("남색") || c.contains("보라") || c.contains("민트")) {
            return COOL;
        }
        if (c.contains("검정") || c.contains("회색") || c.contains("흰색")) {
            return NEUTRAL;
        }
        return OTHER;
    }

    /** 두 색상 간 조화 여부 (따뜻/차가움 상호작용 기반) */
    public boolean isHarmoniousWith(ColorTone other) {
        if (this == other) {
            return true;
        }
        if (this == NEUTRAL || other == NEUTRAL) {
            return true; // 무채색은 다 잘 어울림
        }

        // 보색 관계 (살짝 감점)
        if ((this == WARM && other == COOL) || (this == COOL && other == WARM)) {
            return false;
        }

        return true;
    }

    /** 밝은 색인지 여부 (화이트, 베이지, 연색 계열) */
    public boolean isBright(String colorName) {
        if (colorName == null) {
            return false;
        }
        String c = colorName.toLowerCase();
        return c.contains("흰") || c.contains("화이트") || c.contains("연") || c.contains("베이지");
    }

    /** 어두운 색인지 여부 (검정, 남색, 진한 회색 등) */
    public boolean isDark(String colorName) {
        if (colorName == null) {
            return false;
        }
        String c = colorName.toLowerCase();
        return c.contains("검") || c.contains("블랙") || c.contains("네이비") || c.contains("진")
            || c.contains("그레이");
    }
}
