package com.sprint.ootd5team.domain.recommendation.enums;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public enum Season {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER,
    OTHER;

    /**
     * 월 → 계절 변환
     * (예보 시점을 기준으로 계절 판단)
     */
    public static Season from(Instant instant) {
        if (instant == null) {
            return OTHER;
        }

        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        int month = dateTime.getMonthValue();

        return switch (month) {
            case 3, 4, 5 -> SPRING;
            case 6, 7, 8 -> SUMMER;
            case 9, 10, 11 -> AUTUMN;
            case 12, 1, 2 -> WINTER;
            default -> OTHER;
        };
    }

    /** 인접 계절인지 판단 */
    public boolean isAdjacentTo(Season other) {
        return (this == SPRING && other == SUMMER)
            || (this == SUMMER && other == AUTUMN)
            || (this == AUTUMN && other == WINTER)
            || (this == WINTER && other == SPRING);
    }

    /** 2단계 인접 계절 (봄→겨울, 여름→봄 등) */
    public boolean isSecondAdjacentTo(Season other) {
        return switch (this) {
            case SPRING -> (other == WINTER || other == SUMMER);
            case SUMMER -> (other == SPRING || other == AUTUMN);
            case AUTUMN -> (other == SUMMER || other == WINTER);
            case WINTER -> (other == AUTUMN || other == SPRING);
            default -> false;
        };
    }

    /**
     * 현재 Season(=기준 계절, 예보된 계절) 기준으로
     * 의상 Season이 사용자의 온도 민감도 범위 내에 있는지 판단
     */
    public boolean isWithinComfortRange(Season itemSeason, double sensitivity) {
        if (itemSeason == null) {
            return false;
        }

        // 민감도 높음 → 동일 계절만 허용
        if (sensitivity == 5) {
            return this == itemSeason;
        }

        // 민감도 중간 → 인접 계절 허용
        if (sensitivity >= 2 && sensitivity <= 4) {
            return this == itemSeason || this.isAdjacentTo(itemSeason);
        }

        // 민감도 낮음 → 2단계 인접까지 허용
        if (sensitivity == 1) {
            return this == itemSeason
                || this.isAdjacentTo(itemSeason)
                || this.isSecondAdjacentTo(itemSeason);
        }

        // 기본값 (정의되지 않은 감도일 경우 동일 계절만 허용)
        return this == itemSeason;
    }
}