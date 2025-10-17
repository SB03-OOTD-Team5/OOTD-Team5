package com.sprint.ootd5team.domain.weather.external.meteo;

import static com.sprint.ootd5team.base.util.DateTimeUtils.SEOUL_ZONE_ID;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.external.WeatherFactory;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import com.sprint.ootd5team.domain.weather.external.meteo.OpenMeteoResponse.Daily;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OpenMeteoFactory implements WeatherFactory<ForecastIssueContext, OpenMeteoResponse> {

    private static final int MINIMUM_FORECAST_COUNT = 5;
    private static final Integer REFRESH_TIME_RATE =
        3 * 60;                      // 날씨정보 재조회시 강제 업데이트 기준시간(분 단위)
    private final WeatherRepository weatherRepository;
    private final LocationService locationService;
    private final OpenMeteoAdapter openMeteoAdapter;

    @Override
    public List<Weather> findOrCreateWeathers(BigDecimal latitude, BigDecimal longitude) {
        Location location = locationService.findOrCreateLocation(latitude, longitude);

        //발행날짜시간, 예보날짜시간을 객체로 저장
        ForecastIssueContext issueContext = createForecastIssueContext(
            ZonedDateTime.now(SEOUL_ZONE_ID));

        // 1. 날씨 찾기
        List<Weather> cachedWeathers = findWeathers(location, issueContext);
        if (!cachedWeathers.isEmpty()) {
            log.info("[Weather] 날씨 데이터 이미 존재: {}건", cachedWeathers.size());
            return cachedWeathers;
        }

        // 2. 날씨 데이터 불러오기
        OpenMeteoResponse response = openMeteoAdapter.getWeather(latitude, longitude, null,
            null, 0);
        // 3. 날씨 생성
        List<Weather> newWeathers = createWeathers(response, Collections.emptyList(),
            issueContext, location);
        if (newWeathers.isEmpty()) {
            throw new WeatherNotFoundException();
        }

        return weatherRepository.saveAll(newWeathers);
    }

    @Override
    public List<Weather> findWeathers(Location location, ForecastIssueContext context) {
        Instant now = Instant.now();
        Optional<Weather> latestOptional =
            weatherRepository.findFirstByLocationIdOrderByForecastedAtDesc(location.getId());
        if (latestOptional.isPresent()) {
            Weather latest = latestOptional.get();
            Instant forecastedAt = latest.getForecastedAt();
            if (forecastedAt != null && forecastedAt.plus(REFRESH_TIME_RATE, ChronoUnit.MINUTES)
                .isAfter(now)) {
//                List<Weather> cached = weatherRepository.findAllByLocationIdAndForecastedAt(
//                    location.getId(), forecastedAt);
                List<Weather> cached = weatherRepository
                    .findAllByLocationIdAndForecastedAtAndForecastAtIn(location.getId(),
                        context.getIssueAt(),
                        context.getTargetForecasts());
                log.info(" [OpenWeather] 기존 날씨 데이터 총 {}건, ids:{}", cached.size(),
                    cached.stream()
                        .map(e -> e.getId().toString())
                        .collect(Collectors.joining(", ")));
                if (!cached.isEmpty()) {
                    boolean containsTodayForecast = containsForecastForToday(cached);
                    if (cached.size() >= MINIMUM_FORECAST_COUNT && containsTodayForecast) {
                        log.info("[OpenMeteo] 캐시 사용 - forecastedAt:{} ({}건)", forecastedAt,
                            cached.size());
                        return cached;
                    }
                    log.info("[OpenMeteo] 캐시 무효 - forecastedAt:{}, size:{}, containsToday:{}",
                        forecastedAt, cached.size(), containsTodayForecast);
                }
            } else if (forecastedAt != null) {
                log.info("[OpenMeteo] 캐시 만료 - forecastedAt:{}", forecastedAt);
            }
        }

        return List.of();
    }

    /**
     * Open-Meteo 일간 응답을 Weather 엔티티로 변환한다. 같은 날짜의 기존 예보는 먼저 지우고 최신 데이터로 갱신.
     */
    @Override
    public List<Weather> createWeathers(OpenMeteoResponse response, List<Weather> existingWeathers,
        ForecastIssueContext context, Location location) {
        log.info("[OpenMeteo] 캐시 목록이 비어 있어 API 호출로 진행");
        Daily daily = response.daily();
        if (daily == null || daily.time() == null || daily.time().isEmpty()) {
            log.info("[OpenMeteo] 일간 데이터가 비어 있어 빈 리스트를 반환합니다.");
            return List.of();
        }

        ZoneId zoneId = resolveZoneId(response.timezone());
        Instant fetchedAt = ZonedDateTime.now(zoneId).toInstant();

        Map<LocalDate, DaySummary> dailySummaries = calculateDailySummaries(daily);
        Map<LocalDate, DayBaseline> comparisonBaselines = prepareBaselines(location, zoneId,
            dailySummaries);

        List<Weather> result = new ArrayList<>();
        Set<Instant> forecastSlots = new HashSet<>();
        log.info("[OpenMeteo] 일간 데이터 {}건 수신", daily.time().size());
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
            Double precipitationProbability = safeGetDouble(daily.precipitationProbabilityMax(),
                idx);
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
            forecastSlots.add(forecastAt);

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

        if (!result.isEmpty()) {
            for (Instant forecastSlot : forecastSlots) {
                long deletedCount = weatherRepository.deleteByLocationIdAndForecastAt(
                    location.getId(), forecastSlot);
                if (deletedCount > 0) {
                    log.info("[OpenMeteo] 기존 예보 {}건 삭제 - forecastAt:{}", deletedCount,
                        forecastSlot);
                }
            }
        }

        List<Weather> saved = weatherRepository.saveAll(result);
        Instant latestForecastedAt = saved.isEmpty() ? null : saved.get(0).getForecastedAt();
        log.info("[OpenMeteo] 일간 예보 {}건 저장 완료 (forecastedAt:{})", saved.size(), latestForecastedAt);
        return saved;
    }

    @Override
    public ForecastIssueContext createForecastIssueContext(ZonedDateTime reference) {
        ZonedDateTime issueDateTime = reference.truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime targetDateTime = issueDateTime.plusHours(12);
        return ForecastIssueContext.of(issueDateTime, targetDateTime, MINIMUM_FORECAST_COUNT);
    }

    /**
     * Open-Meteo가 내려주는 일간 배열을 날짜별 요약 값으로 정리한다.
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
     * 전일 대비 계산에 사용할 기준 온도와 습도를 준비한다.
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

    /**
     * 특정 날짜 하루 범위를 대상으로 가장 최근 예보를 조회한다.
     */
    private Weather findWeatherForDate(Location location, LocalDate date, ZoneId zoneId) {
        Instant startOfDay = date.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        return weatherRepository
            .findFirstByLocationIdAndForecastAtBetweenOrderByForecastAtDesc(location.getId(),
                startOfDay, endOfDay)
            .orElse(null);
    }


    /**
     * Open-Meteo가 내려준 타임존 문자열을 ZoneId로 변환한다.
     */
    private ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            log.info("[OpenMeteo] 지원되지 않는 타임존 {}, UTC로 대체합니다.", timezone);
            return ZoneId.of("UTC");
        }
    }

    /**
     * yyyy-MM-dd 형식의 문자열을 LocalDate로 바꾼다.
     */
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            log.info("[OpenMeteo] 날짜 파싱에 실패했습니다. value={}", dateString, e);
            return null;
        }
    }

    /**
     * 리스트 범위를 확인한 뒤 안전하게 Double 값을 꺼낸다.
     */
    private Double safeGetDouble(List<Double> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    /**
     * 리스트 범위를 확인한 뒤 날씨 코드 값을 꺼낸다.
     */
    private Integer safeGetCode(List<Integer> codes, int index) {
        if (codes == null || index < 0 || index >= codes.size()) {
            return null;
        }
        return codes.get(index);
    }

    /**
     * Open-Meteo 날씨 코드를 SkyStatus로 매핑한다.
     */
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

    /**
     * 날씨 코드와 강수량을 기반으로 강수 유형을 판별한다.
     */
    private PrecipitationType toPrecipitationType(Integer weatherCode, Double precipitation) {
        if ((precipitation == null || precipitation <= 0d) && weatherCode == null) {
            return PrecipitationType.NONE;
        }
        if (weatherCode == null) {
            return precipitation != null && precipitation > 0d ? PrecipitationType.RAIN
                : PrecipitationType.NONE;
        }
        return switch (weatherCode) {
            case 66, 67 -> PrecipitationType.RAIN_SNOW;
            case 71, 73, 75, 77, 85, 86 -> PrecipitationType.SNOW;
            case 95, 96, 99, 80, 81, 82 -> PrecipitationType.SHOWER;
            default -> precipitation != null && precipitation > 0d ? PrecipitationType.RAIN
                : PrecipitationType.NONE;
        };
    }

    /**
     * 풍속 값을 구간별로 나눠 바람 세기를 계산한다.
     */
    private WindspeedLevel toWindSpeedLevel(Double windspeed) {
        if (windspeed == null || windspeed < 4d) {
            return WindspeedLevel.WEAK;
        }
        if (windspeed < 9d) {
            return WindspeedLevel.MODERATE;
        }
        return WindspeedLevel.STRONG;
    }

    /**
     * 서버 현재 시간 기준으로 오늘 예보가 포함돼 있는지 확인한다.
     */
    private boolean containsForecastForToday(List<Weather> weathers) {
        ZoneId systemZone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(systemZone);
        for (Weather weather : weathers) {
            Instant forecastAt = weather.getForecastAt();
            if (forecastAt != null) {
                LocalDate forecastDate = forecastAt.atZone(systemZone).toLocalDate();
                if (forecastDate.equals(today)) {
                    return true;
                }
            }
        }
        return false;
    }


    private record DaySummary(double minTemperature, double maxTemperature, double avgTemperature,
                              Double avgHumidity) {

    }

    private record DayBaseline(Double temperature, Double humidity) {

    }
}
