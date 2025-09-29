package com.sprint.ootd5team.domain.clothes.enums;

public enum ClothesType {
    TOP,
    BOTTOM,
    DRESS,
    OUTER,
    UNDERWEAR,
    ACCESSORY,
    SHOES,
    SOCKS,
    HAT,
    BAG,
    SCARF,
    ETC;

    public static ClothesType fromString(String raw) {
        if (raw == null || raw.isBlank()) return ETC;

        String normalized = raw.trim().toLowerCase();

        return switch (normalized) {
            // 상의
            case "상의", "top", "shirt", "t-shirt", "tee" -> TOP;
            // 하의
            case "하의", "바지", "치마", "bottom", "pants", "jeans", "skirt" -> BOTTOM;
            // 드레스 / 원피스
            case "원피스", "드레스", "dress", "onepiece" -> DRESS;
            // 아우터
            case "아우터", "코트", "자켓", "outer", "jacket", "coat" -> OUTER;
            // 속옷
            case "속옷", "underwear", "innerwear" -> UNDERWEAR;
            // 액세서리
            case "악세사리", "액세서리", "accessory" -> ACCESSORY;
            // 신발
            case "신발", "운동화", "부츠", "shoes", "sneakers", "boots" -> SHOES;
            // 양말
            case "양말", "socks" -> SOCKS;
            // 모자
            case "모자", "hat", "cap", "beanie" -> HAT;
            // 가방
            case "가방", "bag", "backpack", "handbag" -> BAG;
            // 스카프
            case "스카프", "목도리", "scarf" -> SCARF;

            default -> ETC;
        };
    }
}
