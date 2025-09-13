package com.sprint.ootd5team.domain.weather.dto.data;

import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.util.UUID;

public record WeatherSummaryDto (
    UUID weatherId,
    SkyStatus skyStatus,
    PrecipitationDto precipitation,
    TemperatureDto temperature
){ }
