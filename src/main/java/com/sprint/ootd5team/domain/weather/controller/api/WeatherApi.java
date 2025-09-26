package com.sprint.ootd5team.domain.weather.controller.api;

import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "날씨", description = "날씨 관련 API")
public interface WeatherApi {

    @GetMapping
    @Operation(summary = "위치 기반 날씨 조회", description = "경도/위도를 쿼리 파라미터로 받아 KMA 데이터를 조회합니다.")
    ResponseEntity<List<WeatherDto>> getWeatherByLocation(
        @Parameter(description = "경도(−180~180)") @RequestParam(value = "longitude", required = false) BigDecimal longitude,
        @Parameter(description = "위도(−90~90)") @RequestParam(value = "latitude", required = false) BigDecimal latitude);

    @GetMapping("/location")
    @Operation(summary = "위치 기반 지역 정보 조회", description = "경도/위도를 쿼리 파라미터로 받아 카카오 데이터를 조회합니다.")
    ResponseEntity<WeatherAPILocationDto> getLocation(@RequestParam BigDecimal longitude,
        @RequestParam BigDecimal latitude);
}
