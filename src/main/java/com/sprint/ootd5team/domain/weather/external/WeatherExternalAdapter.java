package com.sprint.ootd5team.domain.weather.external;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public interface WeatherExternalAdapter<R> {

    R getWeather(BigDecimal latitude, BigDecimal longitude, String issueDate, String issueTime,
        int limit);

    LocalTime resolveIssueTime(ZonedDateTime reference);
}
