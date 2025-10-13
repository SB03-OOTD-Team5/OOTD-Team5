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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public List<Weather> findOrCreateWeathers(BigDecimal latitude, BigDecimal longitude) {
        Location location = locationService.findOrCreateLocation(latitude, longitude);

        //발행날짜시간, 예보날짜시간을 객체로 저장
        ForecastIssueContext issueContext = createForecastIssueContext(
            ZonedDateTime.now(SEOUL_ZONE_ID));

        List<Weather> cachedWeathers = findWeathers(location, issueContext);
        if (!cachedWeathers.isEmpty()) {
            return cachedWeathers;
        }

        // 2. 날씨 데이터 불러오기
        OpenWeatherResponse openWeatherResponse = openWeatherAdapter.getWeather(
            location.getLatitude(), location.getLongitude(), null, null, 0);
        // 3. 날씨 생성
        List<Weather> newWeathers = createWeathers(openWeatherResponse, cachedWeathers,
            issueContext, location);
        weatherRepository.saveAll(newWeathers);
        log.debug(" [OpenWeather] 신규 생성 건수:{}", newWeathers.size());

        return findWeathers(location, issueContext);
    }

    // 5일치 데이터 찾기
    @Override
    public List<Weather> findWeathers(Location location, ForecastIssueContext issueContext) {
        log.debug(" [OpenWeather] findWeathers 시작. issueContext:{}, targetForecasts:{}",
            issueContext.getIssueAt().atZone(ZoneId.of("Asia/Seoul")),
            issueContext.getTargetForecasts().stream()
                .map(i -> i.atZone(ZoneId.of("Asia/Seoul")).toString())
                .toList()
        );

        List<Weather> cached = weatherRepository
            .findAllByLocationIdAndForecastedAtAndForecastAtIn(location.getId(),
                issueContext.getIssueAt(),
                issueContext.getTargetForecasts());
        log.debug(" [OpenWeather] 기존 날씨 데이터 총 {}건, ids:{}", cached.size(),
            cached.stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.joining(", ")));
        return cached;
    }

    @Override
    public List<Weather> createWeathers(OpenWeatherResponse response,
        List<Weather> cachedWeathers, ForecastIssueContext issueContext, Location location) {
        log.debug(" [OpenWeather] 날씨 dto 생성 시작");
        List<ForecastItem> allItems = response.list().stream()
            .sorted(Comparator.comparing(ForecastItem::dt)).toList();
        if (allItems.isEmpty()) {
            throw new WeatherNotFoundException();
        }
        List<Weather> result = new ArrayList<>();
        Map<LocalDate, Weather> cachedByDate = new HashMap<>();

        for (ForecastItem item : allItems) {
            Instant targetAt = extractForecastAt(item.dt());
            if (targetAt == null) {
                log.debug(" [OpenWeather] forecastAt 추출 실패, skip");
                continue;
            }

            Weather yesterdayWeather = findYesterdayWeather(cachedByDate, location,
                LocalDate.ofInstant(targetAt, SEOUL_ZONE_ID));
            log.debug(" [OpenWeather] yesterdayWeather 객체:{}", yesterdayWeather);

            Optional<Weather> weather = convertToWeather(item, location, issueContext, targetAt,
                yesterdayWeather
            );

            if (weather.isEmpty()) {
                continue;
            }

            Weather created = weather.get();
            cachedByDate.put(LocalDate.ofInstant(targetAt, SEOUL_ZONE_ID), created);

            boolean alreadyExists = Optional.ofNullable(cachedWeathers)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)                      // 내부 요소 null 제거
                .anyMatch(existing -> isExistedWeather(existing, created));
            if (alreadyExists) {
                continue;
            }
            result.add(created);
            log.debug(" [OpenWeather] 신규 예보 생성 forecastedAt:{}, forecastAt:{}",
                created.getForecastedAt(), created.getForecastAt());
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
        log.debug(
            " [OpenWeather] issueContext - issueDateTime:{}, targetDateTime:{}, targetDates:{}",
            issueContext.getIssueDateTime(), issueContext.getTargetDateTime(),
            issueContext.getTargetForecasts().stream().map(
                LocalDateTime::from));
        return issueContext;
    }

    private Optional<Weather> convertToWeather(ForecastItem item, Location location,
        ForecastIssueContext issueContext, Instant targetAt, Weather yesterdayWeather) {
        if (item == null) {
            log.debug(" [OpenWeather] forecast item null, skip");
            return Optional.empty();
        }
        if (targetAt == null) {
            log.debug(" [OpenWeather] forecastAt null, skip");
            return Optional.empty();
        }

        Instant issueAt = issueContext.getIssueAt();
        OpenWeatherResponse.Main main = item.main();
        if (main == null) {
            log.debug(" [OpenWeather] main 데이터 없음 forecastAt:{}", targetAt);
            return Optional.empty();
        }

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

    private boolean isExistedWeather(Weather existing, Weather created) {
        if (existing == null || created == null) {
            return false;
        }
        log.info("cached weather id:{},forecastedAt:{},forecastAt:{}", existing.getId(),
            existing.getForecastedAt(), existing.getForecastAt());
        log.info("new weather id:{},forecastedAt:{},forecastAt:{}", created.getId(),
            created.getForecastedAt(), created.getForecastAt());

        if (existing.getLocation() == null || created.getLocation() == null) {
            return false;
        }

        //시간을 제외하고 년월일까지만 비교
        LocalDate existingForecastDate = existing.getForecastAt() == null ? null
            : LocalDate.ofInstant(existing.getForecastAt(), SEOUL_ZONE_ID);
        LocalDate createdForecastDate = created.getForecastAt() == null ? null
            : LocalDate.ofInstant(created.getForecastAt(), SEOUL_ZONE_ID);

        return Objects.equals(existing.getLocation().getId(), created.getLocation().getId())
            && Objects.equals(existing.getForecastedAt(), created.getForecastedAt())
            && Objects.equals(existingForecastDate, createdForecastDate);
    }

    // 어제 날씨를 DB에서 조회하는 로직(일단 지금은 날짜만 같으면 최신꺼 하나만 가져옴) 시간 비교 x
    private Weather findYesterdayWeather(Map<LocalDate, Weather> cachedWeather, Location location,
        LocalDate baseDate) {
        LocalDate yesterday = baseDate.minusDays(1);
        log.debug(" [OpenWeather] yesterday 날짜:{}", yesterday);

        if (cachedWeather.containsKey(yesterday)) {
            return cachedWeather.get(yesterday);
        }

        Instant startOfYesterday = yesterday.atStartOfDay(SEOUL_ZONE_ID).toInstant();
        Instant endOfYesterday = yesterday.atTime(LocalTime.MAX).atZone(SEOUL_ZONE_ID).toInstant();

        Weather found = weatherRepository.findFirstByLocationIdAndForecastAtBetweenOrderByForecastAtDesc(
            location.getId(), startOfYesterday, endOfYesterday
        ).orElse(null);

        cachedWeather.put(yesterday, found);
        return found;
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
