package com.sprint.ootd5team.domain.recommendation.enums.util;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EnumParser {

    private EnumParser() {
    }

    /**
     * Enum을 안전하게 파싱
     * - 대소문자 무시
     * - displayName / aliases 지원
     * - 실패 시 defaultValue 반환
     */
    public static <E extends Enum<E>> E safeParse(
        Class<E> enumClass,
        String value,
        E defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String normalized = value.trim().toLowerCase();

        // 정확 일치 (name)
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(normalized)) {
                return constant;
            }
        }

        // displayName 정확 일치
        for (E constant : enumClass.getEnumConstants()) {
            try {
                var field = enumClass.getDeclaredField("displayName");
                field.setAccessible(true);
                Object displayName = field.get(constant);
                if (displayName != null &&
                    displayName.toString().equalsIgnoreCase(normalized)) {
                    return constant;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }

        // aliases 정확 일치
        for (E constant : enumClass.getEnumConstants()) {
            try {
                var field = enumClass.getDeclaredField("aliases");
                field.setAccessible(true);
                Object aliasField = field.get(constant);
                if (aliasField instanceof String[] aliases) {
                    if (Arrays.stream(aliases)
                        .anyMatch(a -> a.equalsIgnoreCase(normalized))) {
                        return constant;
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }

        // 부분 포함 매칭 (displayName / alias)
        E partialMatch = partialMatch(enumClass, normalized, defaultValue);
        if (partialMatch != defaultValue) {
            return partialMatch;
        }

        log.debug("[Recommendation] Enum 변환 실패: enumType={}, invalidValue={}",
            enumClass.getSimpleName(), value);
        return defaultValue;
    }

    /**
     * 속성값과 이름을 모두 고려하여 Enum 추론
     * - 속성값이 정확 일치하면 우선 사용
     * - 실패 시 의상 이름과 결합한 문자열로 부분 일치 검색
     */
    public static <E extends Enum<E>> E parseFromAttrAndName(
        Class<E> enumClass,
        String attrValue,
        String itemName,
        E defaultValue
    ) {
        // 속성값이 있다면 정확 일치 우선 시도
        if (attrValue != null && !attrValue.isBlank()) {
            E exact = safeParse(enumClass, attrValue, defaultValue);
            if (exact != defaultValue) {
                return exact;
            }
        }

        // 이름 기반 + 속성 결합으로 부분 포함 추론
        String combined = String.join(" ",
            attrValue != null ? attrValue : "",
            itemName != null ? itemName : ""
        ).trim();

        if (combined.isBlank()) {
            return defaultValue;
        }

        return partialMatch(enumClass, combined.toLowerCase(), defaultValue);
    }

    /** 내부용: 부분 일치 기반 매칭 */
    private static <E extends Enum<E>> E partialMatch(
        Class<E> enumClass,
        String normalized,
        E defaultValue
    ) {
        for (E constant : enumClass.getEnumConstants()) {
            // displayName 포함 매칭
            try {
                var field = enumClass.getDeclaredField("displayName");
                field.setAccessible(true);
                Object displayName = field.get(constant);
                if (displayName != null &&
                    normalized.contains(displayName.toString().toLowerCase())) {
                    return constant;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}

            // aliases 포함 매칭
            try {
                var field = enumClass.getDeclaredField("aliases");
                field.setAccessible(true);
                Object aliasField = field.get(constant);
                if (aliasField instanceof String[] aliases) {
                    if (Arrays.stream(aliases)
                        .anyMatch(a -> normalized.contains(a.toLowerCase()))) {
                        return constant;
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
        return defaultValue;
    }
}
