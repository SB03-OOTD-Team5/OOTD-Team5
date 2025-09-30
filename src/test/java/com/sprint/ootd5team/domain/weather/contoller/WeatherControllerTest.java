package com.sprint.ootd5team.domain.weather.contoller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.controller.WeatherController;
import com.sprint.ootd5team.domain.weather.dto.data.HumidityDto;
import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.dto.data.WindSpeedDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = WeatherController.class)
@DisplayName("WeatherController 슬라이스 테스트")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private AuthService authService;

    private WeatherDto createDummy() {
        return WeatherDto.builder()
            .id(UUID.randomUUID())
            .forecastedAt(Instant.now())
            .forecastAt(Instant.now())
            .location(new WeatherAPILocationDto(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                60,
                127,
                new String[]{"서울특별시", "중구"},
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
            ))
            .skyStatus(SkyStatus.CLEAR)
            .precipitation(new PrecipitationDto(
                PrecipitationType.RAIN,
                12.5,
                80.0
            ))
            .humidity(new HumidityDto(
                65.0,
                -2.0
            ))
            .temperature(new TemperatureDto(
                23.5,
                1.2,
                18.0,
                28.0
            ))
            .windSpeed(new WindSpeedDto(
                3.5,
                WindspeedLevel.WEAK
            ))
            .build();
    }

    @Nested
    @DisplayName("GET /api/weathers")
    class GetWeathers {


        @Test
        @DisplayName("성공: 위도/경도로 조회 성공 -> 200과 데이터 반환")
        void 위도_경도로_날씨정보를_가져온다() throws Exception {
            // given
            List<WeatherDto> mockList = List.of(
                createDummy(), createDummy()
            );
            UUID userId = UUID.randomUUID();
            given(authService.getCurrentUserId()).willReturn(userId);
            given(
                weatherService.fetchWeatherByLocation(any(BigDecimal.class), any(BigDecimal.class),
                    eq(userId)))
                .willReturn(mockList);

            // when
            ResultActions result = mockMvc.perform(
                get("/api/weathers")
                    .param("longitude", "127.0000")
                    .param("latitude", "37.5000")
            );

            // then
            result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/weathers/location")
    class GetLocation {

        @Test
        @DisplayName("실패: 예외 발생 시 500 반환")
        void 위도_경도로_날씨정보_실패한다() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            given(authService.getCurrentUserId()).willReturn(userId);
            given(
                weatherService.fetchWeatherByLocation(any(BigDecimal.class), any(BigDecimal.class),
                    eq(userId)))
                .willThrow(new RuntimeException("외부 API 오류"));

            // when
            ResultActions result = mockMvc.perform(
                get("/api/weathers")
                    .param("longitude", "127.0000")
                    .param("latitude", "37.5000")
            );

            // then
            result.andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("성공: 위치 정보 조회")
        void 위도_경도로_지역정보를_조회한다() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            WeatherAPILocationDto dto = new WeatherAPILocationDto(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                60,
                127,
                new String[]{"서울특별시", "중구"},
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
            );
            given(authService.getCurrentUserId()).willReturn(userId);
            given(locationService.fetchLocation(any(BigDecimal.class), any(BigDecimal.class),
                eq(userId)))
                .willReturn(dto);

            // when
            ResultActions result = mockMvc.perform(
                get("/api/weathers/location")
                    .param("longitude", "127.0000")
                    .param("latitude", "37.5000")
            );

            // then
            result.andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(37.5665));
        }

        @Test
        @DisplayName("실패: 위치 정보 조회 중 예외 발생")
        void 위도_경도로_지역정보_실패한다() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            given(authService.getCurrentUserId()).willReturn(userId);
            given(locationService.fetchLocation(any(BigDecimal.class), any(BigDecimal.class),
                eq(userId)))
                .willThrow(new RuntimeException("외부 API 오류"));

            // when
            ResultActions result = mockMvc.perform(
                get("/api/weathers/location")
                    .param("longitude", "127.0000")
                    .param("latitude", "37.5000")
            );

            // then
            result.andExpect(status().isInternalServerError());
        }
    }

}
