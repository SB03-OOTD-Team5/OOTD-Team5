package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WeatherService {

    List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude, UUID userId);
}
