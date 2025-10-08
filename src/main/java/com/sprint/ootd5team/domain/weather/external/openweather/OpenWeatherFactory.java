package com.sprint.ootd5team.domain.weather.external.openweather;

import static com.sprint.ootd5team.base.util.DateTimeUtils.SEOUL_ZONE_ID;
import static com.sprint.ootd5team.base.util.DateTimeUtils.getZonedDateTime;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.external.WeatherFactory;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import com.sprint.ootd5team.domain.weather.external.openweather.OpenWeatherResponse.ForecastItem;
import com.sprint.ootd5team.domain.weather.external.openweather.OpenWeatherResponse.Rain;
import com.sprint.ootd5team.domain.weather.external.openweather.OpenWeatherResponse.Snow;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
@Component
@RequiredArgsConstructor
public class OpenWeatherFactory implements
    WeatherFactory<ForecastIssueContext, OpenWeatherResponse> {

    private static final int WEATHER_REQUESTED_CNT = 5;
    private final WeatherRepository weatherRepository;
    private final OpenWeatherAdapter openWeatherAdapter;
    private final LocationService locationService;

    @Override
    public List<Weather> findOrCreateWeathers(BigDecimal latitude, BigDecimal longitude) {
        Location location = locationService.findOrCreateLocation(latitude, longitude);

        //발행날짜시간, 예보날짜시간을 객체로 저장
        ForecastIssueContext issueContext = createForecastIssueContext(
            ZonedDateTime.now(SEOUL_ZONE_ID));

        List<Weather> cachedWeathers = findWeathers(location, issueContext);
        log.debug("[Weather] 기존 날씨 데이터 총 {}건, ids:{}", cachedWeathers.size(),
            cachedWeathers.stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.joining(", ")));

        if (cachedWeathers.size() == WEATHER_REQUESTED_CNT) {
            return cachedWeathers;
        }

        OpenWeatherResponse openWeatherResponse = openWeatherAdapter.getWeather(
            location.getLatitude(), location.getLongitude(), null, null, 0);
        List<Weather> newWeathers = createWeathers(openWeatherResponse, cachedWeathers,
            issueContext, location);
        weatherRepository.saveAll(newWeathers);
        log.debug("[Weather] 신규 생성 건수:{}", newWeathers.size());

        return findWeathers(location, issueContext);
    }

    // 5일치 데이터 찾기
    @Override
    public List<Weather> findWeathers(Location location, ForecastIssueContext issueContext) {
        Instant issueAt = issueContext.getIssueAt();
        List<Instant> targetAtList = issueContext.getTargetForecasts();
        log.debug(
            "[findWeathers] 날씨 존재하는지 확인 forecastedAt:{}, location:{}, targetAtList:{}",
            issueAt, location.getId(), targetAtList);

        List<Weather> exactMatches = weatherRepository
            .findAllByLocationIdAndForecastedAtAndForecastAtIn(location.getId(), issueAt,
                targetAtList);

        log.debug("[findWeathers] 목표 건수:{}건, 정확히 매치하는 건수:{}건 ", targetAtList.size(),
            targetAtList.size());
        if (exactMatches.size() == targetAtList.size()) {
            return exactMatches;
        }

        // 타켓 시간에 맞는 정확한 예보가 없을때, 근사한 날짜 가져옴
        Set<Instant> foundForecasts = exactMatches.stream()
            .map(Weather::getForecastAt)
            .collect(Collectors.toCollection(HashSet::new));

        Map<LocalDate, List<Weather>> dailyCache = new HashMap<>();
        List<Weather> enrichedResults = new ArrayList<>(exactMatches);

        for (Instant targetAt : targetAtList) {
            if (foundForecasts.contains(targetAt)) {
                continue;
            }

            ZonedDateTime targetDateTime = targetAt.atZone(SEOUL_ZONE_ID);
            LocalDate targetDate = targetDateTime.toLocalDate();
            List<Weather> dailyCandidates = dailyCache.computeIfAbsent(targetDate, date -> {
                Instant dayStart = date.atStartOfDay(SEOUL_ZONE_ID).toInstant();
                Instant dayEnd = date.plusDays(1).atStartOfDay(SEOUL_ZONE_ID).toInstant();
                return weatherRepository
                    .findAllByLocationIdAndForecastedAtAndForecastAtBetween(location.getId(),
                        issueAt,
                        dayStart, dayEnd);
            });

            Weather nearest = dailyCandidates.stream()
                .min(Comparator.comparingLong(candidate -> Math.abs(
                    candidate.getForecastAt().toEpochMilli() - targetAt.toEpochMilli())))
                .orElse(null);

            if (nearest != null && foundForecasts.add(nearest.getForecastAt())) {
                enrichedResults.add(nearest);
            }
        }

        log.debug("[findWeathers] 최종반환 건수:{}건 ", enrichedResults.size());

        return enrichedResults;
    }

    @Override
    public List<Weather> createWeathers(OpenWeatherResponse response,
        List<Weather> existingWeatherList, ForecastIssueContext issueContext, Location location) {
        log.debug("[Weather] 날씨 dto 생성 시작");
        List<ForecastItem> allItems = response.list().stream()
            .sorted(Comparator.comparing(ForecastItem::dt)).toList();
        if (allItems.isEmpty()) {
            throw new WeatherNotFoundException();
        }

        Instant issueAt = issueContext.getIssueAt();
        List<Weather> result = new ArrayList<>();

        log.debug("[Weather] 기존 예보 시각 수:{}", existingWeatherList.size());

        for (ForecastItem item : allItems) {
            Optional<Weather> weather = convertToWeather(item, issueContext, location, issueAt,
                existingWeatherList);
            if (weather.isPresent()) {
                Weather created = weather.get();
                result.add(created);
                log.debug("[Weather] 신규 예보 생성 forecastAt:{}", created.getForecastAt());
            }
        }

        return result;
    }

    @Override
    public ForecastIssueContext createForecastIssueContext(ZonedDateTime reference) {
        LocalDate referenceDate = reference.toLocalDate();
        // 발행 시각 base time 구함
        LocalTime issueTime = openWeatherAdapter.resolveIssueTime(reference);
        ZonedDateTime issueDateTime = getZonedDateTime(referenceDate, issueTime);
        // 예보 시각 base time 구함
        LocalTime targetTime = openWeatherAdapter.resolveTargetTime(reference);
        ZonedDateTime targetDateTime = getZonedDateTime(referenceDate, targetTime);
        ForecastIssueContext issueContext = ForecastIssueContext.of(issueDateTime, targetDateTime,
            WEATHER_REQUESTED_CNT);
        log.debug("[Weather] issueContext - issueDateTime:{}, targetDateTime:{}",
            issueContext.getIssueDateTime(), issueContext.getTargetDateTime());
        return issueContext;
    }

    private Optional<Weather> convertToWeather(ForecastItem item, ForecastIssueContext issueContext,
        Location location, Instant issueAt, List<Weather> existingWeatherList) {
        if (item == null) {
            log.debug("[Weather] forecast item null, skip");
            return Optional.empty();
        }
        Instant targetAt = extractForecastAt(item.dt());
        if (targetAt == null) {
            log.debug("[Weather] forecastAt 추출 실패, skip");
            return Optional.empty();
        }
        if (!existingWeatherList.stream()
            .filter((weather -> weather.getForecastAt().equals(targetAt)
                && weather.getForecastedAt().equals(issueAt))).toList().isEmpty()) {
            log.debug("[Weather] 이미 존재하는 forecastAt:{}, forecastedAt:{}", targetAt, issueAt);
            return Optional.empty();
        }

        OpenWeatherResponse.Main main = item.main();
        if (main == null) {
            log.debug("[Weather] main 데이터 없음 forecastAt:{}", targetAt);
            return Optional.empty();
        }
        Weather yesterdayWeather = findYesterdayWeather(location,
            LocalDate.ofInstant(targetAt, SEOUL_ZONE_ID));

        ComparisonMetrics comparisonMetrics = toComparisonMetrics(main, yesterdayWeather);
        PrecipitationType precipitationType = toPrecipitationType(item);
        double windspeed = toWindSpeed(item);

        Weather weather = Weather.builder()
            .forecastedAt(issueAt)
            .forecastAt(targetAt)
            .skyStatus(toSkyStatus(item, precipitationType))
            .precipitationType(precipitationType)
            .precipitationAmount(toPrecipitationAmount(item))
            .precipitationProbability(toPrecipitationProbability(item))
            .humidity(main.humidity())
            .humidityCompared(comparisonMetrics.humidityDiff())
            .temperature(main.temp())
            .temperatureCompared(comparisonMetrics.tempDiff())
            .temperatureMin(Optional.ofNullable(main.tempMin()).orElse(0.0))
            .temperatureMax(Optional.ofNullable(main.tempMax()).orElse(0.0))
            .windspeed(windspeed)
            .windspeedLevel(toWindSpeedLevel(windspeed))
            .location(location)
            .build();

        return Optional.of(weather);
    }

    private ComparisonMetrics toComparisonMetrics(OpenWeatherResponse.Main current,
        Weather yesterday) {
        if (current == null || yesterday == null) {
            return new ComparisonMetrics(null, null);
        }

        Double tempDiff = (current.temp() != null && yesterday.getTemperature() != null)
            ? current.temp() - yesterday.getTemperature()
            : null;

        Double humidityDiff = (current.humidity() != null && yesterday.getHumidity() != null)
            ? current.humidity() - yesterday.getHumidity()
            : null;

        return new ComparisonMetrics(tempDiff, humidityDiff);
    }

    private Double toPrecipitationAmount(ForecastItem item) {
        double rain = Optional.ofNullable(item.rain()).map(Rain::threeHour).orElse(0d);
        double snow = Optional.ofNullable(item.snow()).map(Snow::threeHour).orElse(0d);
        return Math.max(rain, snow);
    }


    private double toPrecipitationProbability(ForecastItem item) {
        return item.pop() != null ? Math.min(Math.max(item.pop() * 100, 0d), 100d) : 0d;
    }

    private double toWindSpeed(ForecastItem item) {
        return item.wind() != null && item.wind().speed() != null ? item.wind().speed() : 0d;
    }

    // 어제 날씨를 DB에서 조회하는 로직(일단 지금은 날짜만 같으면 최신꺼 하나만 가져옴) 시간 비교 x
    private Weather findYesterdayWeather(Location location, LocalDate baseDate) {
        LocalDate yesterday = baseDate.minusDays(1);
        Instant startOfYesterday =
            yesterday.atStartOfDay(SEOUL_ZONE_ID).toInstant();
        Instant endOfYesterday =
            yesterday.atTime(LocalTime.MAX).atZone(SEOUL_ZONE_ID).toInstant();

        return weatherRepository.findFirstByLocationIdAndForecastAtBetweenOrderByForecastAtDesc(
            location.getId(),
            startOfYesterday,
            endOfYesterday
        ).orElse(null);
    }


    private Instant extractForecastAt(Long dt) {
        if (dt != null) {
            return Instant.ofEpochSecond(dt);
        }
        return null;
    }

    private PrecipitationType toPrecipitationType(ForecastItem item) {

        double rain = Optional.ofNullable(item.rain()).map(Rain::threeHour)
            .orElse(Double.MIN_VALUE);
        double snow = Optional.ofNullable(item.snow()).map(Snow::threeHour)
            .orElse(Double.MIN_VALUE);
        if (rain > 0d && snow > 0d) {
            return PrecipitationType.RAIN_SNOW;
        }
        if (snow > 0d) {
            return PrecipitationType.SNOW;
        }
        if (rain > 0d) {
            return PrecipitationType.RAIN;
        }
        OpenWeatherCategoryType category = extractCategory(item);
        return switch (category) {
            case THUNDERSTORM -> PrecipitationType.SHOWER;
            case DRIZZLE, RAIN -> PrecipitationType.RAIN;
            case SNOW -> PrecipitationType.SNOW;
            default -> PrecipitationType.NONE;
        };
    }

    private SkyStatus toSkyStatus(ForecastItem item,
        PrecipitationType precipitationType) {
        if (precipitationType != PrecipitationType.NONE) {
            return SkyStatus.CLOUDY;
        }

        Integer cloudiness = item.clouds() != null ? item.clouds().all() : null;
        if (cloudiness != null) {
            if (cloudiness < 30) {
                return SkyStatus.CLEAR;
            }
            if (cloudiness < 70) {
                return SkyStatus.MOSTLY_CLOUDY;
            }
            return SkyStatus.CLOUDY;
        }

        OpenWeatherCategoryType category = extractCategory(item);
        return switch (category) {
            case CLEAR -> SkyStatus.CLEAR;
            case CLOUDS -> SkyStatus.MOSTLY_CLOUDY;
            default -> SkyStatus.CLOUDY;
        };
    }

    private OpenWeatherCategoryType extractCategory(OpenWeatherResponse.ForecastItem item) {
        List<OpenWeatherResponse.Weather> weathers = item.weather();
        if (weathers == null || weathers.isEmpty()) {
            return OpenWeatherCategoryType.UNKNOWN;
        }
        return OpenWeatherCategoryType.of(weathers.get(0).main());
    }

    private WindspeedLevel toWindSpeedLevel(Double windspeed) {
        if (windspeed == null || windspeed < 4) {
            return WindspeedLevel.WEAK;
        }
        if (windspeed < 9) {
            return WindspeedLevel.MODERATE;
        }
        return WindspeedLevel.STRONG;
    }


    private record ComparisonMetrics(Double tempDiff, Double humidityDiff) {

    }
}
