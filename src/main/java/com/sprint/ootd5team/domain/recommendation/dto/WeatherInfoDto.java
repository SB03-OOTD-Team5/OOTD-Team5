package com.sprint.ootd5team.domain.recommendation.dto;

import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;

public record WeatherInfoDto(
    PrecipitationType precipitationType,
    double precipitationAmount,

    double currentHumidity,

    double currentTemperature,
    double minTemperature,
    double maxTemperature,

    WindspeedLevel windSpeedLevel
) {

}
