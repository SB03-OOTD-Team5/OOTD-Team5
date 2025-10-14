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

    @Override
    public String toString() {
        return seasons.toString();
    }
}
