package com.sprint.ootd5team.domain.weather.external.openweather;

/* - Group 2xx: Thunderstorm
 * - Group 3xx: Drizzle
 * - Group 5xx: Rain
 * - Group 6xx: Snow
 * - Group 7xx: Atmosphere
 * - Group 800: Clear
 * - Group 80x: Clouds
 * */
public enum OpenWeatherCategoryType {
    THUNDERSTORM,
    DRIZZLE,
    RAIN,
    SNOW,
    ATMOSPHERE,
    CLEAR,
    CLOUDS,
    UNKNOWN;

    public static OpenWeatherCategoryType of(String type) {
        if (type == null) {
            return OpenWeatherCategoryType.UNKNOWN;
        }
        try {
            return OpenWeatherCategoryType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OpenWeatherCategoryType.UNKNOWN;
        }
    }
}
