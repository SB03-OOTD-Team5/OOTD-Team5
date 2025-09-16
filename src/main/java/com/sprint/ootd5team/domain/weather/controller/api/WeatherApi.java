package com.sprint.ootd5team.domain.weather.controller.api;

import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "날씨", description = "날씨 관련 API")
public interface WeatherApi {

    @GetMapping
    ResponseEntity<List<WeatherDto>> getWeatherByLocation(BigDecimal longitude,
        BigDecimal latitude);
}
