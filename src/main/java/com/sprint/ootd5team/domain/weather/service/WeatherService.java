package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import java.util.List;

public interface WeatherService {

    List<WeatherDto> fetchWeatherByDateAndLocation();
}
