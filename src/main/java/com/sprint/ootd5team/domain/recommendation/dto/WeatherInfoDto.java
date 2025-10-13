package com.sprint.ootd5team.domain.recommendation.dto;

import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;

public record WeatherInfoDto(
    PrecipitationType precipitationType,
    double precipitationProbability,

    double humidity,

    double temperature,
    double temperatureMin,
    double temperatureMax,

    WindspeedLevel windSpeedLevel
) {

}
