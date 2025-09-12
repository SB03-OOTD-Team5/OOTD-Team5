package com.sprint.ootd5team.domain.weather.dto.data;

public record HumidityDto(
    Double current,
    Double comparedToDayBefore
) { }
