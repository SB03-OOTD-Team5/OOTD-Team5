package com.sprint.ootd5team.domain.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private WeatherFactory weatherFactory;

    @Mock
    private WeatherMapper weatherMapper;

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private Profile profile;
    private Location location;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = new User("tester", "tester@example.com", "pwd", Role.USER);
        location = Location.builder()
            .latitude(BigDecimal.valueOf(37.1))
            .longitude(BigDecimal.valueOf(127.1))
            .locationNames("테스트위치")
            .build();
        ReflectionTestUtils.setField(location, "id", UUID.randomUUID());

        profile = new Profile(user, "닉네임", null, null, null, location, 2);
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("fetchWeatherByLocation - 파라미터 위경도를 그대로 사용한다")
    void 요청_위경도가_있으면_그대로_사용한다() {
        BigDecimal requestLat = BigDecimal.valueOf(35.0);
        BigDecimal requestLon = BigDecimal.valueOf(128.0);

        Weather weather = Weather.builder()
            .location(location)
            .forecastAt(Instant.now())
            .forecastedAt(Instant.now())
            .build();

        WeatherDto dto = WeatherDto.builder()
            .forecastAt(weather.getForecastAt())
            .forecastedAt(weather.getForecastedAt())
            .build();

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(weatherFactory.findOrCreateWeathers(requestLat, requestLon)).thenReturn(List.of(weather));
        when(weatherMapper.toDto(eq(weather), any())).thenReturn(dto);

        List<WeatherDto> result = weatherService.fetchWeatherByLocation(requestLat, requestLon,
            userId);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));

        ArgumentCaptor<com.sprint.ootd5team.domain.location.dto.data.ClientCoords> captor =
            ArgumentCaptor.forClass(com.sprint.ootd5team.domain.location.dto.data.ClientCoords.class);
        verify(weatherMapper).toDto(eq(weather), captor.capture());
        assertEquals(requestLat, captor.getValue().clientLatitude());
        assertEquals(requestLon, captor.getValue().clientLongitude());
    }

    @Test
    @DisplayName("fetchWeatherByLocation - 요청 위경도가 없으면 프로필 위치를 사용한다")
    void 요청_위경도가_없으면_프로필_위치를_사용한다() {
        BigDecimal profileLat = location.getLatitude();
        BigDecimal profileLon = location.getLongitude();

        Weather weather = Weather.builder()
            .location(location)
            .forecastAt(Instant.now())
            .forecastedAt(Instant.now())
            .build();
        WeatherDto dto = WeatherDto.builder()
            .forecastAt(weather.getForecastAt())
            .forecastedAt(weather.getForecastedAt())
            .build();

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(weatherFactory.findOrCreateWeathers(profileLat, profileLon)).thenReturn(List.of(weather));
        when(weatherMapper.toDto(eq(weather), any())).thenReturn(dto);

        List<WeatherDto> result = weatherService.fetchWeatherByLocation(null, null, userId);

        assertEquals(1, result.size());
        verify(weatherFactory).findOrCreateWeathers(profileLat, profileLon);

        ArgumentCaptor<com.sprint.ootd5team.domain.location.dto.data.ClientCoords> captor =
            ArgumentCaptor.forClass(com.sprint.ootd5team.domain.location.dto.data.ClientCoords.class);
        verify(weatherMapper).toDto(eq(weather), captor.capture());
        assertEquals(null, captor.getValue().clientLatitude());
        assertEquals(null, captor.getValue().clientLongitude());
    }

    @Test
    @DisplayName("getLastestPerLocationId - 리포지토리 위임")
    void 최신날씨_조회는_리포지토리에_위임한다() {
        UUID locationId = UUID.randomUUID();
        Weather expected = Weather.builder()
            .location(location)
            .forecastAt(Instant.now())
            .forecastedAt(Instant.now())
            .build();
        when(weatherRepository.findTopByLocationIdOrderByForecastedAtDescForecastAtDescCreatedAtDesc(locationId))
            .thenReturn(expected);

        Weather result = weatherService.getLastestPerLocationId(locationId);
        assertSame(expected, result);
    }
}
