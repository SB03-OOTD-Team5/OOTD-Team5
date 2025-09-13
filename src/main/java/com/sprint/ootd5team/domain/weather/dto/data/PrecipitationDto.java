package com.sprint.ootd5team.domain.weather.dto.data;

import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;

public record PrecipitationDto(
    PrecipitationType type,
    Double amount,
    Double probability
) { }
