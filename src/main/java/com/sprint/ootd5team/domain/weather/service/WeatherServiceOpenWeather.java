package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.external.openweather.OpenWeatherFactory;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "weather.api-client", name = "provider", havingValue = "openWeather")
@RequiredArgsConstructor
public class WeatherServiceOpenWeather implements WeatherService {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private final static BigDecimal DEFAULT_LAT = BigDecimal.valueOf(37.5665); // 서울시청주소
    private final static BigDecimal DEFAULT_LON = BigDecimal.valueOf(126.9780);
    private final ProfileRepository profileRepository;
    private final OpenWeatherFactory openWeatherFactory;
    private final WeatherMapper weatherMapper;
    private final WeatherRepository weatherRepository;


    @Override
    public List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude,
        UUID userId) {
        // 사용자 프로필 조회
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(ProfileNotFoundException::new);

        // 파라미터로 위경도 넘어오지 않으면 프로필에 있는 위치값 사용, 프로필에 위치값 없으면 기본값 사용
        BigDecimal lat =
            latitude == null ? profile.getLocation().getLatitude() == null ? DEFAULT_LAT
                : profile.getLocation().getLatitude()
                : latitude;
        BigDecimal lon =
            longitude == null ? profile.getLocation().getLongitude() == null ? DEFAULT_LON
                : profile.getLocation().getLongitude()
                : longitude;
        log.info("파라미터 위경도:{}, {} \n 최종 위경도:{}, {}", latitude, longitude, lat, lon);
        List<Weather> weathers = openWeatherFactory.findOrCreateWeathers(lat, lon);

        //  최종 DTO로 변환하여 반환
        return weathers.stream()
            .map(weather -> weatherMapper.toDto(weather, new ClientCoords(latitude, longitude)))
            .toList();
    }

    //특정 location 데이터 중 해당하는 예보 시각에 맞는 최신 데이터를 가져옴
    @Override
    public Weather getLatestWeatherForLocationAndDate(UUID locationId, LocalDate targetDate) {
        Instant startOfDay = targetDate.atStartOfDay(SEOUL_ZONE_ID).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(SEOUL_ZONE_ID).toInstant();

        return weatherRepository.findTopByLocationIdAndForecastDateOrderByLatest(
            locationId, startOfDay, endOfDay);
    }

}
