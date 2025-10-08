package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WeatherService {

    List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude, UUID userId);

    // batch에서 사용 - 특정 location 데이터 중 해당하는 예보 시각에 맞는 최신 데이터를 가져옴
    Weather getLatestWeatherForLocationAndDate(UUID locationId, LocalDate targetDate);

    // batch에서 사용
    boolean existsWeatherFor(String baseDate, String baseTime, UUID locationId);
}
