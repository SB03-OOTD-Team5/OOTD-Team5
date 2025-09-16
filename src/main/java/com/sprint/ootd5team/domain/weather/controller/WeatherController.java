package com.sprint.ootd5team.domain.weather.controller;

import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping()
    public ResponseEntity<List<WeatherDto>> getWeatherByLocation() {
        List<WeatherDto> weatherDtos = weatherService.fetchWeatherByDateAndLocation();
        return ResponseEntity.ok().body(weatherDtos);
    }

}
