package com.sprint.ootd5team.domain.weather.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.controller.api.WeatherApi;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

    private final WeatherService weatherService;
    private final LocationService locationService;
    private final AuthService authService;


    @GetMapping
    @Override
    public ResponseEntity<List<WeatherDto>> getWeatherByLocation(BigDecimal latitude,
        BigDecimal longitude
    ) {
        UUID userId = authService.getCurrentUserId();
        List<WeatherDto> weatherDtos = weatherService.fetchWeatherByLocation(latitude,
            longitude, userId);
        return ResponseEntity.status(HttpStatus.OK).body(weatherDtos);
    }

    @GetMapping("/location")
    @Override
    public ResponseEntity<WeatherAPILocationDto> getLocation(BigDecimal latitude,
        BigDecimal longitude
    ) {
        UUID userId = authService.getCurrentUserId();
        WeatherAPILocationDto locationDto = locationService.fetchLocation(latitude,
            longitude, userId);
        return ResponseEntity.status(HttpStatus.OK).body(locationDto);

    }

}
