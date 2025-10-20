package com.sprint.ootd5team.domain.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import com.sprint.ootd5team.domain.weather.external.kma.KmaApiAdapter;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponse;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponse.Body;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponse.Header;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponse.Items;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponse.Response;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponse.WeatherItem;
import com.sprint.ootd5team.domain.weather.external.kma.KmaWeatherFactory;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class KmaWeatherFactoryTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private KmaApiAdapter kmaApiAdapter;

    @Mock
    private LocationService locationService;

    @Test
    @DisplayName("findOrCreateWeathers - 캐시가 있으면 재사용한다")
    void 캐시된_날씨가_있으면_재사용한다() {
        BigDecimal lat = BigDecimal.valueOf(37.5);
        BigDecimal lon = BigDecimal.valueOf(127.0);

        Location location = Location.builder()
            .latitude(lat)
            .longitude(lon)
            .locationNames("테스트")
            .build();
        UUID locationId = UUID.randomUUID();
        ReflectionTestUtils.setField(location, "id", locationId);

        Weather cached = Weather.builder()
            .location(location)
            .forecastAt(Instant.now())
            .forecastedAt(Instant.now())
            .build();
        ReflectionTestUtils.setField(cached, "id", UUID.randomUUID());

        KmaWeatherFactory factory = new KmaWeatherFactory(weatherRepository, kmaApiAdapter,
            locationService);

        when(locationService.findOrCreateLocation(lat, lon)).thenReturn(location);
        when(kmaApiAdapter.resolveIssueTime(any())).thenReturn(LocalTime.of(6, 0));
        when(kmaApiAdapter.resolveTargetTime(any())).thenReturn(LocalTime.of(9, 0));
        when(weatherRepository.findAllByLocationIdAndForecastedAtAndForecastAtIn(eq(locationId),
            any(),
            any())).thenReturn(List.of(cached, cached, cached, cached, cached));

        List<Weather> result = factory.findOrCreateWeathers(lat, lon);

        assertEquals(5, result.size());
        verify(weatherRepository).findAllByLocationIdAndForecastedAtAndForecastAtIn(eq(locationId),
            any(), any());
        verify(kmaApiAdapter, never()).getWeather(
            eq(lat), eq(lon), anyString(),
            anyString(),
            eq(1000)
        );
        verify(weatherRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("findOrCreateWeathers - 캐시가 없으면 API 호출 후 저장한다")
    void 캐시가_없으면_API를_호출하고_저장한다() {
        BigDecimal lat = BigDecimal.valueOf(37.5);
        BigDecimal lon = BigDecimal.valueOf(127.0);
        Location location = Location.builder()
            .latitude(lat)
            .longitude(lon)
            .locationNames("테스트")
            .build();
        UUID locationId = UUID.randomUUID();
        ReflectionTestUtils.setField(location, "id", locationId);

        Weather newWeather = Weather.builder()
            .location(location)
            .forecastAt(Instant.now())
            .forecastedAt(Instant.now())
            .build();
        ReflectionTestUtils.setField(newWeather, "id", UUID.randomUUID());

        KmaWeatherFactory factory = new KmaWeatherFactory(weatherRepository, kmaApiAdapter,
            locationService);

        String baseDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = "2300";

        when(locationService.findOrCreateLocation(lat, lon)).thenReturn(location);
        when(weatherRepository.findAllByLocationIdAndForecastedAtAndForecastAtIn(eq(locationId),
            any(),
            any())).thenReturn(Collections.emptyList(), List.of(newWeather));
        when(kmaApiAdapter.resolveIssueTime(any())).thenReturn(LocalTime.of(23, 0));
        when(kmaApiAdapter.resolveTargetTime(any())).thenReturn(LocalTime.of(9, 0));
        KmaResponse dto = buildSimpleResponse(baseDate, baseTime, baseDate, "0000");
        when(kmaApiAdapter.getWeather(eq(lat), eq(lon), anyString(), anyString(), eq(1000)))
            .thenReturn(dto);
        when(weatherRepository.saveAll(any())).thenReturn(List.of(newWeather));
        when(
            weatherRepository.findAllByLocationIdAndForecastedAtAndForecastAtBetween(eq(locationId),
                any(),
                any(), any())).thenReturn(Collections.emptyList());

        List<Weather> result = factory.findOrCreateWeathers(lat, lon);

        assertEquals(1, result.size());
        assertEquals(newWeather, result.get(0));
        verify(kmaApiAdapter).getWeather(eq(lat), eq(lon), anyString(), anyString(), eq(1000));
        verify(weatherRepository).saveAll(any());
    }

    @Test
    @DisplayName("createWeathers - 응답이 비어 있으면 예외")
    void 응답이_비어있으면_예외가_발생한다() {
        Location location = Location.builder()
            .latitude(BigDecimal.ONE)
            .longitude(BigDecimal.ONE)
            .build();
        ReflectionTestUtils.setField(location, "id", UUID.randomUUID());

        KmaResponse dto = new KmaResponse(new Response(
            new Header("00", "OK"),
            new Body("JSON", new Items(Collections.emptyList()), 1, 1, 0)
        ));

        KmaWeatherFactory factory = new KmaWeatherFactory(weatherRepository, kmaApiAdapter,
            locationService);

        when(kmaApiAdapter.resolveIssueTime(any())).thenReturn(LocalTime.of(2, 0));
        when(kmaApiAdapter.resolveTargetTime(any())).thenReturn(LocalTime.of(9, 0));
        ForecastIssueContext issueContext = factory.createForecastIssueContext(
            ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
        assertThrows(WeatherNotFoundException.class,
            () -> factory.createWeathers(dto, Collections.emptyList(), issueContext, location));
    }


    private KmaResponse buildSimpleResponse(String baseDate, String baseTime,
        String forecastDate,
        String forecastTime) {
        WeatherItem tmp = new WeatherItem(baseDate, baseTime, "TMP", forecastDate, forecastTime,
            "23", 60, 127);
        WeatherItem pop = new WeatherItem(baseDate, baseTime, "POP", forecastDate, forecastTime,
            "10", 60, 127);
        WeatherItem pty = new WeatherItem(baseDate, baseTime, "PTY", forecastDate, forecastTime,
            "0", 60, 127);
        WeatherItem reh = new WeatherItem(baseDate, baseTime, "REH", forecastDate, forecastTime,
            "50", 60, 127);
        WeatherItem wsd = new WeatherItem(baseDate, baseTime, "WSD", forecastDate, forecastTime,
            "3", 60, 127);
        WeatherItem tmx = new WeatherItem(baseDate, baseTime, "TMX", forecastDate, forecastTime,
            "25", 60, 127);
        WeatherItem tmn = new WeatherItem(baseDate, baseTime, "TMN", forecastDate, forecastTime,
            "15", 60, 127);
        return new KmaResponse(new Response(
            new Header("00", "OK"),
            new Body("JSON", new Items(List.of(tmp, pop, pty, reh, wsd, tmx, tmn)), 1, 1, 7)
        ));
    }
}
