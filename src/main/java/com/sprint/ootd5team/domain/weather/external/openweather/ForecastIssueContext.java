package com.sprint.ootd5team.domain.weather.external.openweather;

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

    // 발행시각 ZonedDateTime 타입
    private final ZonedDateTime issueDateTime;
    // 발행시각 Instant 타입
    private final Instant issueAt;
    // 예보시각 ZonedDateTime 타입
    private final ZonedDateTime targetDateTime;
    // 예보시각 Instant 타입
    private final Instant targetAt;
    // 예보시각에  따른 예보 대상 시각 목록
    private final List<Instant> targetForecasts;
    private final Set<Instant> targetForecastSet;

    public ForecastIssueContext(ZonedDateTime issueDateTime, ZonedDateTime targetDateTime,
        int forecastCount) {
        this.issueDateTime = Objects.requireNonNull(issueDateTime,
            "issueDateTime must not be null");
        this.issueAt = this.issueDateTime.toInstant();

        this.targetDateTime = Objects.requireNonNull(targetDateTime,
            "targetDateTime must not be null");
        this.targetAt = this.targetDateTime.toInstant();

        // targetDateTime부터 5일치 날짜 가져옴
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


    boolean isTargetForecast(Instant targetAt) {
        return targetForecastSet.contains(targetAt);
    }
}
