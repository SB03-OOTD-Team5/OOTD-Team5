package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.external.meteo.OpenMeteoAdapter;
import com.sprint.ootd5team.domain.weather.external.meteo.OpenMeteoResponse;
import com.sprint.ootd5team.domain.weather.external.meteo.OpenMeteoResponse.Daily;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Open-Meteo를 백업 데이터 소스로 활용하는 날씨 서비스입니다.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "weather.api-client", name = "provider", havingValue = "meteo")
@RequiredArgsConstructor
public class WeatherServiceOpenMeteo implements WeatherService {

    private static final BigDecimal DEFAULT_LAT = BigDecimal.valueOf(37.5665); // 위도 파라미터가 없을 때 사용할 서울시청 좌표
    private static final BigDecimal DEFAULT_LON = BigDecimal.valueOf(126.9780); // 경도 파라미터가 없을 때 사용할 서울시청 좌표
    private static final Integer REFRESH_TIME_RATE = 3*60;                      // 날씨정보 재조회시 강제 업데이트 기준시간(분 단위)

    private final ProfileRepository profileRepository;
    private final LocationService locationService;
    private final WeatherMapper weatherMapper;
    private final OpenMeteoAdapter openMeteoAdapter;
    private final WeatherRepository weatherRepository;

    /**
     * 사용자 좌표(또는 프로필 기본 좌표)를 기준으로 Open-Meteo 일간 예보를 조회해 WeatherDto 목록으로 반환한다.
     */
    @Override
    @Transactional
    public List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude,
        UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(ProfileNotFoundException::new);

        BigDecimal resolvedLat = resolveLatitude(latitude, profile);
        BigDecimal resolvedLon = resolveLongitude(longitude, profile);
        log.info("[OpenMeteo] 요청 좌표({}, {}) -> 사용 좌표({}, {})", latitude, longitude,
            resolvedLat, resolvedLon);

        Location location = locationService.findOrCreateLocation(resolvedLat, resolvedLon);
        ClientCoords clientCoords = new ClientCoords(latitude, longitude);

        Instant now = Instant.now();
        Optional<Weather> latestOptional =
            weatherRepository.findFirstByLocationIdOrderByCreatedAtDesc(location.getId());
        if (latestOptional.isPresent()) {
            Weather latest = latestOptional.get();
            Instant createdAt = latest.getCreatedAt();
            if (createdAt != null && createdAt.plus(REFRESH_TIME_RATE, ChronoUnit.MINUTES).isAfter(now)) {
                List<Weather> cached = weatherRepository.findAllByLocationIdAndCreatedAt(
                    location.getId(), createdAt);
                if (!cached.isEmpty()) {
                    log.info("[OpenMeteo] 캐시 사용 - createdAt:{} ({}건)", createdAt, cached.size());
                    return cached.stream()
                        .map(weather -> weatherMapper.toDto(weather, clientCoords))
                        .toList();
                }
                log.debug("[OpenMeteo] createdAt:{} 캐시 목록이 비어 있어 API 호출로 진행", createdAt);
            } else if (createdAt != null) {
                log.info("[OpenMeteo] 캐시 만료 - createdAt:{}", createdAt);
            }
        }

        OpenMeteoResponse response = openMeteoAdapter.getDailyForecast(resolvedLat, resolvedLon);

        List<Weather> weathers = buildWeathers(response, location);
        if (weathers.isEmpty()) {
            throw new WeatherNotFoundException();
        }

        return weathers.stream()
            .map(weather -> weatherMapper.toDto(weather, clientCoords))
            .toList();
    }

    /**
     * Open-Meteo 일간 응답을 Weather 엔티티 목록으로 변환하고 DB에 저장한다.
     */
    private List<Weather> buildWeathers(OpenMeteoResponse response, Location location) {
        Daily daily = response.daily();
        if (daily == null || daily.time() == null || daily.time().isEmpty()) {
            log.debug("[OpenMeteo] 일간 데이터가 비어 있어 빈 리스트를 반환합니다.");
            return List.of();
        }

        ZoneId zoneId = resolveZoneId(response.timezone());
        Instant fetchedAt = ZonedDateTime.now(zoneId).toInstant();

        Map<LocalDate, DaySummary> dailySummaries = calculateDailySummaries(daily);
        Map<LocalDate, DayBaseline> comparisonBaselines = prepareBaselines(location, zoneId,
            dailySummaries);

        List<Weather> result = new ArrayList<>();
        log.debug("[OpenMeteo] 일간 데이터 {}건 수신", daily.time().size());
        List<String> dates = daily.time();
        for (int idx = 0; idx < dates.size(); idx++) {
            LocalDate forecastDate = parseDate(dates.get(idx));
            if (forecastDate == null) {
                continue;
            }

            DaySummary summary = dailySummaries.get(forecastDate);
            if (summary == null) {
                continue;
            }

            Integer weatherCode = safeGetCode(daily.weatherCode(), idx);
            Double precipitation = safeGetDouble(daily.precipitationSum(), idx);
            Double precipitationProbability = safeGetDouble(daily.precipitationProbabilityMax(), idx);
            Double windSpeed = safeGetDouble(daily.windSpeed10mMax(), idx);
            Double humidity = summary.avgHumidity();
            double averageTemperature = summary.avgTemperature();

            DayBaseline baseline = comparisonBaselines.get(forecastDate);
            double temperatureCompared = 0d;
            if (baseline != null && baseline.temperature() != null) {
                temperatureCompared = averageTemperature - baseline.temperature();
            }
            double humidityCompared = 0d;
            if (baseline != null && baseline.humidity() != null && humidity != null) {
                humidityCompared = humidity - baseline.humidity();
            }

            Instant forecastAt = forecastDate.atTime(LocalTime.NOON).atZone(zoneId).toInstant();

            Weather weather = Weather.builder()
                .forecastedAt(fetchedAt)
                .forecastAt(forecastAt)
                .skyStatus(toSkyStatus(weatherCode))
                .precipitationType(toPrecipitationType(weatherCode, precipitation))
                .precipitationAmount(precipitation)
                .precipitationProbability(precipitationProbability)
                .humidity(humidity)
                .humidityCompared(humidityCompared)
                .temperature(averageTemperature)
                .temperatureCompared(temperatureCompared)
                .temperatureMin(summary.minTemperature())
                .temperatureMax(summary.maxTemperature())
                .windspeed(windSpeed)
                .windspeedLevel(toWindSpeedLevel(windSpeed))
                .location(location)
                .build();

            result.add(weather);
        }

        List<Weather> saved = weatherRepository.saveAll(result);
        Instant newCreatedAt = saved.isEmpty() ? null : saved.get(0).getCreatedAt();
        log.info("[OpenMeteo] 일간 예보 {}건 저장 완료 (createdAt:{})", saved.size(), newCreatedAt);
        return saved;
    }

    /**
     * 일간 데이터를 사용해 날짜별 요약 정보를 생성한다.
     */
    private Map<LocalDate, DaySummary> calculateDailySummaries(Daily daily) {
        List<String> times = daily.time();
        if (times == null || times.isEmpty()) {
            return Map.of();
        }

        Map<LocalDate, DaySummary> summaries = new HashMap<>();
        for (int idx = 0; idx < times.size(); idx++) {
            LocalDate date = parseDate(times.get(idx));
            if (date == null) {
                continue;
            }

            Double maxTempValue = safeGetDouble(daily.temperatureMax(), idx);
            Double minTempValue = safeGetDouble(daily.temperatureMin(), idx);
            double maxTemp = maxTempValue != null ? maxTempValue : 0d;
            double minTemp = minTempValue != null ? minTempValue : 0d;
            double avgTemp;
            if (maxTempValue != null && minTempValue != null) {
                avgTemp = (maxTempValue + minTempValue) / 2;
            } else if (maxTempValue != null) {
                avgTemp = maxTemp;
            } else if (minTempValue != null) {
                avgTemp = minTemp;
            } else {
                avgTemp = 0d;
            }
            Double avgHumidity = safeGetDouble(daily.relativeHumidity2mMean(), idx);

            summaries.put(date, new DaySummary(minTemp, maxTemp, avgTemp, avgHumidity));
        }
        return summaries;
    }

    /**
     * 전일 대비 계산에 활용할 기준값(평균 기온, 평균 습도)을 준비한다.
     */
    private Map<LocalDate, DayBaseline> prepareBaselines(Location location, ZoneId zoneId,
        Map<LocalDate, DaySummary> dailySummaries) {
        if (dailySummaries.isEmpty()) {
            return Map.of();
        }
        Map<LocalDate, DayBaseline> baselines = new HashMap<>();
        List<LocalDate> sortedDates = dailySummaries.keySet().stream().sorted().toList();
        for (LocalDate date : sortedDates) {
            LocalDate previousDate = date.minusDays(1);
            Weather yesterdayWeather = findWeatherForDate(location, previousDate, zoneId);
            if (yesterdayWeather != null) {
                baselines.put(date, new DayBaseline(yesterdayWeather.getTemperature(),
                    yesterdayWeather.getHumidity()));
                continue;
            }
            DaySummary previousSummary = dailySummaries.get(previousDate);
            if (previousSummary != null) {
                baselines.put(date, new DayBaseline(previousSummary.avgTemperature(),
                    previousSummary.avgHumidity()));
            }
        }
        return baselines;
    }

    private Weather findWeatherForDate(Location location, LocalDate date, ZoneId zoneId) {
        Instant startOfDay = date.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        return weatherRepository
            .findFirstByLocationIdAndForecastAtBetweenOrderByForecastAtDesc(location.getId(),
                startOfDay, endOfDay)
            .orElse(null);
    }

    private BigDecimal resolveLatitude(BigDecimal latitude, Profile profile) {
        if (latitude != null) {
            return latitude;
        }
        if (profile.getLocation() != null && profile.getLocation().getLatitude() != null) {
            return profile.getLocation().getLatitude();
        }
        return DEFAULT_LAT;
    }

    private BigDecimal resolveLongitude(BigDecimal longitude, Profile profile) {
        if (longitude != null) {
            return longitude;
        }
        if (profile.getLocation() != null && profile.getLocation().getLongitude() != null) {
            return profile.getLocation().getLongitude();
        }
        return DEFAULT_LON;
    }

    private ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            log.debug("[OpenMeteo] 지원되지 않는 타임존 {}, UTC로 대체합니다.", timezone);
            return ZoneId.of("UTC");
        }
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            log.debug("[OpenMeteo] 날짜 파싱에 실패했습니다. value={}", dateString, e);
            return null;
        }
    }

    private Double safeGetDouble(List<Double> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    private Integer safeGetCode(List<Integer> codes, int index) {
        if (codes == null || index < 0 || index >= codes.size()) {
            return null;
        }
        return codes.get(index);
    }

    private SkyStatus toSkyStatus(Integer weatherCode) {
        if (weatherCode == null) {
            return SkyStatus.CLEAR;
        }
        return switch (weatherCode) {
            case 0 -> SkyStatus.CLEAR;
            case 1, 2 -> SkyStatus.MOSTLY_CLOUDY;
            default -> SkyStatus.CLOUDY;
        };
    }

    private PrecipitationType toPrecipitationType(Integer weatherCode, Double precipitation) {
        if ((precipitation == null || precipitation <= 0d) && weatherCode == null) {
            return PrecipitationType.NONE;
        }
        if (weatherCode == null) {
            return precipitation != null && precipitation > 0d ? PrecipitationType.RAIN : PrecipitationType.NONE;
        }
        return switch (weatherCode) {
            case 66, 67 -> PrecipitationType.RAIN_SNOW;
            case 71, 73, 75, 77, 85, 86 -> PrecipitationType.SNOW;
            case 95, 96, 99, 80, 81, 82 -> PrecipitationType.SHOWER;
            default -> precipitation != null && precipitation > 0d ? PrecipitationType.RAIN : PrecipitationType.NONE;
        };
    }

    private WindspeedLevel toWindSpeedLevel(Double windspeed) {
        if (windspeed == null || windspeed < 4d) {
            return WindspeedLevel.WEAK;
        }
        if (windspeed < 9d) {
            return WindspeedLevel.MODERATE;
        }
        return WindspeedLevel.STRONG;
    }

    private record DaySummary(double minTemperature, double maxTemperature, double avgTemperature,
        Double avgHumidity) {
    }

    private record DayBaseline(Double temperature, Double humidity) {
    }
}