package com.sprint.ootd5team.domain.weather.controller;

import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.controller.api.WeatherApi;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

    private final WeatherService weatherService;
    private final LocationService locationService;

    @GetMapping
    @Override
    public ResponseEntity<List<WeatherDto>> getWeatherByLocation(@RequestParam BigDecimal latitude,
        @RequestParam BigDecimal longitude
    ) {
        try {
            List<WeatherDto> weatherDtos = weatherService.fetchWeatherByLocation(latitude,
                longitude);
            return ResponseEntity.status(HttpStatus.OK).body(weatherDtos);
        } catch (Exception e) {
            //TODO: body에 ErrorResponse 넣기
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/location")
    @Override
    public ResponseEntity<WeatherAPILocationDto> getLocation(@RequestParam BigDecimal latitude,
        @RequestParam BigDecimal longitude
    ) {
        try {
            WeatherAPILocationDto locationDto = locationService.fetchLocation(latitude,
                longitude);
            return ResponseEntity.status(HttpStatus.OK).body(locationDto);
        } catch (Exception e) {
            //TODO: body에 ErrorResponse 넣기
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
