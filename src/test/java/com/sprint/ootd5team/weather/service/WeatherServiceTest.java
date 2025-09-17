package com.sprint.ootd5team.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaFetchException;
import com.sprint.ootd5team.domain.weather.mapper.WeatherBuilder;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import com.sprint.ootd5team.domain.weather.service.WeatherServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService 단위 테스트")
@ActiveProfiles("test")
public class WeatherServiceTest {

    private static final UUID FIXED_PROFILE_ID = UUID.fromString(
        "036220a1-1223-4ba5-943e-48452526cbe9");
    @Mock
    WebClient kmaApiClient;
    @Mock
    WeatherRepository weatherRepository;
    @Mock
    WeatherBuilder weatherBuilder;
    @Mock
    ProfileRepository profileRepository;
    @Mock
    WeatherMapper weatherMapper;
    // WebClient 체인 모킹용
    @Mock
    WebClient.RequestHeadersUriSpec<?> uriSpec;
    @Mock
    WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock
    WebClient.ResponseSpec responseSpec;
    @InjectMocks
    WeatherServiceImpl service;

    @Test
    @DisplayName("이미 저장된 데이터가 있으면 외부 호출 없이 데이터를 반환한다")
    void 데이터가_이미_존재하면_외부_API_호출하지_않는다() {
        // given
        Profile dummyProfile = new Profile();
        given(profileRepository.findById(eq(FIXED_PROFILE_ID))).willReturn(
            Optional.of(dummyProfile));
        // 테스트를 위해 고정값
        Instant baseDateTimeInstant = Instant.parse("2025-01-01T00:00:00Z");
        given(weatherBuilder.toInstantWithZone(anyString(), anyString()))
            .willReturn(baseDateTimeInstant);

        Weather w1 = mock(Weather.class);
        Weather w2 = mock(Weather.class);

        given(weatherRepository.findAllByForecastedAtAndLatitudeAndLongitude(
            any(),
            any(),
            any())).willReturn(List.of(w1, w2));

        WeatherDto d1 = mock(WeatherDto.class);
        WeatherDto d2 = mock(WeatherDto.class);
        given(weatherMapper.toDto(w1)).willReturn(d1);
        given(weatherMapper.toDto(w2)).willReturn(d2);

        // when
        var longitude = new BigDecimal("127.123456");
        var latitude = new BigDecimal("37.98765");
        List<WeatherDto> result = service.fetchWeatherByLocation(longitude, latitude);

        // then
        // 외부호출없음
        assertThat(result).hasSize(2);
        verifyNoInteractions(kmaApiClient);
    }

    @Test
    @DisplayName(" 저장된 데이터가 없으면 외부 API 호출 한다.")
    void 데이터가_없으면_외부_API_호출한다() {
        // given
        Profile dummyProfile = new Profile();
        given(profileRepository.findById(eq(FIXED_PROFILE_ID))).willReturn(
            Optional.of(dummyProfile));
        // 테스트를 위해 고정값
        Instant baseDateTimeInstant = Instant.parse("2025-01-01T00:00:00Z");
        given(weatherBuilder.toInstantWithZone(anyString(), anyString()))
            .willReturn(baseDateTimeInstant);

        // 저장된 데이터 없음
        given(weatherRepository.findAllByForecastedAtAndLatitudeAndLongitude(
            any(),
            any(),
            any())).willReturn(List.of());

        // when
        // 서비스 실제 호출 -> 예외 던짐
        assertThatThrownBy(() ->
            service.fetchWeatherByLocation(new BigDecimal("0"), new BigDecimal("0"))
        ).isInstanceOf(WeatherKmaFetchException.class);

        // then
        // 외부호출있음(예외 던진것도 반영됨)
        verify(kmaApiClient, atLeastOnce()).get();
    }

    @Test
    @DisplayName("외부 호출 실패 시 WeatherKmaFetchException을 던진다")
    void fetch_throws_whenExternalCallFails() {
        // given
        Profile dummyProfile = new Profile();
        given(profileRepository.findById(eq(FIXED_PROFILE_ID))).willReturn(
            Optional.of(dummyProfile));
        // 테스트를 위해 고정값
        Instant baseDateTimeInstant = Instant.parse("2025-01-01T00:00:00Z");
        given(weatherBuilder.toInstantWithZone(anyString(), anyString()))
            .willReturn(baseDateTimeInstant);

        // 저장된 데이터 없음
        given(weatherRepository.findAllByForecastedAtAndLatitudeAndLongitude(
            any(),
            any(),
            any())).willReturn(List.of());

        // when & then
        // 서비스 실제 호출 -> 예외 던짐
        assertThatThrownBy(() ->
            service.fetchWeatherByLocation(new BigDecimal("0"), new BigDecimal("0"))
        ).isInstanceOf(WeatherKmaFetchException.class);

    }

    @Test
    @DisplayName("위경도가 NUMERIC(8,4) 반올림되어 Repository에 전달된다")
    void toNumeric_rounding_isAppliedOnRepositoryArgs() {
        // given
        Profile dummyProfile = new Profile();
        given(profileRepository.findById(eq(FIXED_PROFILE_ID))).willReturn(
            Optional.of(dummyProfile));
        // 테스트를 위해 고정값
        Instant baseDateTimeInstant = Instant.parse("2025-01-01T00:00:00Z");
        given(weatherBuilder.toInstantWithZone(anyString(), anyString()))
            .willReturn(baseDateTimeInstant);

        given(weatherRepository.findAllByForecastedAtAndLatitudeAndLongitude(
            any(),
            any(),
            any())).willReturn(List.of());

        // when
        var longitude = new BigDecimal("127.123456");
        var latitude = new BigDecimal("37.98765");
        assertThatThrownBy(() ->
            service.fetchWeatherByLocation(longitude, latitude)
        ).isInstanceOf(WeatherKmaFetchException.class);

        // then: 첫 번째 레포지토리 호출에서 전달된 위/경도 확인
        ArgumentCaptor<BigDecimal> latCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> lonCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(weatherRepository).findAllByForecastedAtAndLatitudeAndLongitude(
            eq(baseDateTimeInstant),
            latCaptor.capture(),
            lonCaptor.capture()
        );

        assertThat(lonCaptor.getValue()).isEqualByComparingTo(new BigDecimal("127.1235"));
        assertThat(latCaptor.getValue()).isEqualByComparingTo(new BigDecimal("37.9877"));
    }
}
