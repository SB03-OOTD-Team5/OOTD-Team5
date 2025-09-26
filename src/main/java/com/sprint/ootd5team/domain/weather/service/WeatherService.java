package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WeatherService {

    List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude, UUID userId);

    //  1. weather에서 location에 해당하는 최신 데이터 한개 가져옴
    Weather getLastestPerLocationId(UUID locationId);
}
