package com.sprint.ootd5team.domain.location.dto.data;

import java.math.BigDecimal;

public record WeatherAPILocationDto(
    BigDecimal latitude,
    BigDecimal longitude,
    Integer xCoord,
    Integer yCoord,
    String locationNames
) { }
