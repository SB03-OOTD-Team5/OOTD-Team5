package com.sprint.ootd5team.domain.weather.service;

import static com.sprint.ootd5team.base.util.DateTimeUtils.SEOUL_ZONE_ID;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.util.DateTimeUtils;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.external.meteo.OpenMeteoFactory;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Open-Meteo API를 이용해 일간 예보를 저장하고 제공하는 서비스 구현체입니다. 기상청(KMA) 채널이 동작하지 않을 때를 대비한 백업 경로로 사용됩니다.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "weather.api-client", name = "provider", havingValue = "meteo")
@RequiredArgsConstructor
public class WeatherServiceOpenMeteo implements WeatherService {

    private static final BigDecimal DEFAULT_LAT = BigDecimal.valueOf(
        37.5665); // 위도 파라미터가 없을 때 사용할 서울시청 좌표
    private static final BigDecimal DEFAULT_LON = BigDecimal.valueOf(
        126.9780); // 경도 파라미터가 없을 때 사용할 서울시청 좌표
    private static final Integer REFRESH_TIME_RATE =
        3 * 60;                      // 날씨정보 재조회시 강제 업데이트 기준시간(분 단위)
    private static final int MINIMUM_FORECAST_COUNT = 5; // 캐시 유효 판정을 위한 최소 예보 개수

    private final ProfileRepository profileRepository;
    private final WeatherMapper weatherMapper;
    private final WeatherRepository weatherRepository;
    private final OpenMeteoFactory openMeteoFactory;

    /**
     * 사용자 좌표(없으면 프로필 좌표)를 기준으로 일간 예보를 조회한다. 유효한 캐시가 있으면 저장된 데이터를, 없으면 Open-Meteo 응답을 반환한다.
     */
    @Override
    @Transactional
    public List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude,
        UUID userId) {
        // 사용자 프로필 조회
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(ProfileNotFoundException::new);

        BigDecimal resolvedLat = resolveLatitude(latitude, profile);
        BigDecimal resolvedLon = resolveLongitude(longitude, profile);
        log.info("[OpenMeteo] 요청 좌표({}, {}) -> 사용 좌표({}, {})", latitude, longitude,
            resolvedLat, resolvedLon);

        List<Weather> weathers = openMeteoFactory.findOrCreateWeathers(resolvedLat, resolvedLon);

        ClientCoords clientCoords = new ClientCoords(latitude, longitude);
        return weathers.stream()
            .map(weather -> weatherMapper.toDto(weather, clientCoords))
            .toList();
    }


    @Override
    public Weather getLatestWeatherForLocationAndDate(UUID locationId, LocalDate targetDate) {
        Instant startOfDay = targetDate.atStartOfDay(SEOUL_ZONE_ID).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(SEOUL_ZONE_ID).toInstant();

        return weatherRepository
            .findFirstByLocationIdAndForecastAtBetweenOrderByForecastedAtDescForecastAtDescCreatedAtDesc(
                locationId, startOfDay, endOfDay)
            .orElse(null);
    }

    @Override
    public boolean existsWeatherFor(LocalDate issueDate, LocalTime issueTime, UUID locationId) {
        Instant forecastedAt = DateTimeUtils.toInstant(issueDate, issueTime);
        return weatherRepository.existsByLocationIdAndForecastedAt(locationId, forecastedAt);
    }

    /**
     * 위도 값이 비어 있으면 프로필이나 기본 좌표를 사용한다.
     */
    private BigDecimal resolveLatitude(BigDecimal latitude, Profile profile) {
        if (latitude != null && !latitude.equals(BigDecimal.ZERO)) {
            return latitude;
        }
        if (profile.getLocation() != null && profile.getLocation().getLatitude() != null) {
            return profile.getLocation().getLatitude();
        }
        return DEFAULT_LAT;
    }

    /**
     * 경도 값이 없을 때 프로필 좌표나 기본값으로 대체한다.
     */
    private BigDecimal resolveLongitude(BigDecimal longitude, Profile profile) {
        if (longitude != null && !longitude.equals(BigDecimal.ZERO)) {
            return longitude;
        }
        if (profile.getLocation() != null && profile.getLocation().getLongitude() != null) {
            return profile.getLocation().getLongitude();
        }
        return DEFAULT_LON;
    }
}
