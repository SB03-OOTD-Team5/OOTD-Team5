package com.sprint.ootd5team.domain.recommendation.enums;

import com.sprint.ootd5team.base.util.DateTimeUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public enum Season {
    SPRING(17.0, "봄",    new String[]{"spring"}),
    SUMMER(24.0, "여름",  new String[]{"summer"}),
    AUTUMN(15.0, "가을",  new String[]{"autumn", "fall"}),
    WINTER(0.0,  "겨울",  new String[]{"winter"}),
    OTHER(20.0, "사계절", new String[]{"기타", "전체", "all", "all-season"});

    private final double averageTemperature;
    private final String displayName;
    private final String[] aliases;

    Season(double averageTemperature, String displayName, String[] aliases) {
        this.averageTemperature = averageTemperature;
        this.displayName = displayName;
        this.aliases = aliases;
    }

    /** 월 → 계절 변환 */
    public static Season from(Instant instant) {
        if (instant == null) {
            return OTHER;
        }

        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, DateTimeUtils.SEOUL_ZONE_ID);
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
    public boolean is(Season other) {
        if (other == null) {
            return false;
        }
        return switch (this) {
            case SPRING -> (other == WINTER || other == SUMMER);
            case SUMMER -> (other == SPRING || other == AUTUMN);
            case AUTUMN -> (other == SUMMER || other == WINTER);
            case WINTER -> (other == AUTUMN || other == SPRING);
            default -> false;
        };
    }
}