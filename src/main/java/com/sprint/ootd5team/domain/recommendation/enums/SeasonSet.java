package com.sprint.ootd5team.domain.recommendation.enums;

import java.util.EnumSet;
import java.util.Set;

public class SeasonSet {

    private final Set<Season> seasons;

    private SeasonSet(Set<Season> seasons) {
        this.seasons = seasons;
    }

    public static SeasonSet fromString(String value) {
        if (value == null || value.isBlank()) {
            return new SeasonSet(EnumSet.of(Season.OTHER));
        }

        Set<Season> result = EnumSet.noneOf(Season.class);
        String[] parts = value.split("/");
        for (String part : parts) {
            Season s = switch (part.trim()) {
                case "봄" -> Season.SPRING;
                case "여름" -> Season.SUMMER;
                case "가을" -> Season.AUTUMN;
                case "겨울" -> Season.WINTER;
                case "사계절", "기타" -> Season.OTHER;
                default -> null;
            };
            if (s != null) {
                result.add(s);
            }
        }

        if (result.isEmpty()) {
            result.add(Season.OTHER);
        }
        return new SeasonSet(result);
    }

    /** 현재 계절이 이 의상에 포함되는지 */
    public boolean matches(Season forecast, double sensitivity) {
        return seasons.stream()
            .anyMatch(s ->
                s == Season.OTHER // ‘사계절’ 또는 ‘기타’는 항상 통과
                    || forecast.isWithinComfortRange(s, sensitivity)
            );
    }

    @Override
    public String toString() {
        return seasons.toString();
    }
}
