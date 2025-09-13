package com.sprint.ootd5team.domain.weather.dto.data;

public record TemperatureDto(
    Double current,
    Double comparedToDayBefore,
    Double min,
    Double max
  ) { }
