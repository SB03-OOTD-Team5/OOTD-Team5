package com.sprint.ootd5team.domain.weather.external.context;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class ForecastIssueContext {

    private final ZonedDateTime issueDateTime;
    private final Instant issueAt;
    private final ZonedDateTime targetDateTime;
    private final Instant targetAt;
    private final List<Instant> targetForecasts;
    private final Set<Instant> targetForecastSet;

    public ForecastIssueContext(ZonedDateTime issueDateTime, ZonedDateTime targetDateTime,
        int forecastCount) {
        this.issueDateTime = Objects.requireNonNull(issueDateTime,
            "issueDateTime must not be null");
        this.issueAt = this.issueDateTime.toInstant(); // forecastedAt

        this.targetDateTime = Objects.requireNonNull(targetDateTime,
            "targetDateTime must not be null");
        this.targetAt = this.targetDateTime.toInstant(); // forecastAt

        List<Instant> generatedTargets = IntStream.range(0, Math.max(0, forecastCount))
            .mapToObj(i -> this.targetDateTime.plusDays(i).toInstant())
            .toList();
        this.targetForecasts = List.copyOf(generatedTargets);
        this.targetForecastSet = java.util.Collections.unmodifiableSet(
            new LinkedHashSet<>(this.targetForecasts));
    }

    public static ForecastIssueContext of(ZonedDateTime issueDateTime, ZonedDateTime targetDateTime,
        int forecastCount) {
        return new ForecastIssueContext(issueDateTime, targetDateTime, forecastCount);
    }

    LocalDate getIssueDate() {
        return issueDateTime.toLocalDate();
    }

    LocalTime getIssueTime() {
        return issueDateTime.toLocalTime();
    }

    LocalDate getTargetDate() {
        return targetDateTime.toLocalDate();
    }

    LocalTime getTargetTime() {
        return targetDateTime.toLocalTime();
    }

    public boolean isTargetForecast(Instant targetAt) {
        return targetForecastSet.contains(targetAt);
    }
}
