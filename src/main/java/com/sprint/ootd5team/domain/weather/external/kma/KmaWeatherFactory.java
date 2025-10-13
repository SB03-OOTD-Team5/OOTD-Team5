package com.sprint.ootd5team.domain.weather.external.kma;

import static com.sprint.ootd5team.base.util.DateTimeUtils.DATE_FORMATTER;
import static com.sprint.ootd5team.base.util.DateTimeUtils.SEOUL_ZONE_ID;
import static com.sprint.ootd5team.base.util.DateTimeUtils.TIME_FORMATTER;
import static com.sprint.ootd5team.base.util.DateTimeUtils.getZonedDateTime;
import static com.sprint.ootd5team.base.util.DateTimeUtils.toInstant;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.service.LocationService;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.external.WeatherFactory;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponse.WeatherItem;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

;

@Slf4j
@Component
@RequiredArgsConstructor
public class KmaWeatherFactory implements WeatherFactory<ForecastIssueContext, KmaResponse> {

    private static final int WEATHER_REQUESTED_CNT = 5;
    private final WeatherRepository weatherRepository;
    private final KmaApiAdapter kmaApiAdapter;
    private final LocationService locationService;

    @Override
    public List<Weather> findOrCreateWeathers(BigDecimal latitude, BigDecimal longitude) {
        Location location = locationService.findOrCreateLocation(latitude, longitude);

        //발행날짜시간, 예보날짜시간을 객체로 저장
        ForecastIssueContext issueContext = createForecastIssueContext(
            ZonedDateTime.now(SEOUL_ZONE_ID));

        // 1. 날씨 찾기
        List<Weather> cachedWeathers = findWeathers(location, issueContext);

        if (!cachedWeathers.isEmpty()) {
            return cachedWeathers;
        }
        // 2. 날씨 데이터 불러오기
        String baseDate = issueContext.getIssueDateTime().format(DATE_FORMATTER);
        String baseTime = issueContext.getIssueDateTime().format(TIME_FORMATTER);
        KmaResponse kmaResponse = kmaApiAdapter.getWeather(location.getLatitude(),
            location.getLongitude(), baseDate, baseTime, 1000);
        // 3. 날씨 생성
        List<Weather> newWeathers = createWeathers(kmaResponse, cachedWeathers,
            issueContext, location);
        weatherRepository.saveAll(newWeathers);
        log.debug("[KMA] 신규 생성 건수:{}", newWeathers.size());

        return findWeathers(location, issueContext);
    }

    @Override
    public ForecastIssueContext createForecastIssueContext(ZonedDateTime reference) {
        LocalDate referenceDate = reference.toLocalDate();
        // 발행 시각 base time 구함
        LocalTime issueTime = kmaApiAdapter.resolveIssueTime(reference); // 기상청 API BaseTime
        ZonedDateTime issueDateTime = getZonedDateTime(
            referenceDate, issueTime);
        // 예보 시각 base time 구함
        LocalTime targetTime = kmaApiAdapter.resolveTargetTime(reference);
        ZonedDateTime targetDateTime = getZonedDateTime(
            referenceDate, targetTime);
        ForecastIssueContext issueContext = ForecastIssueContext.of(issueDateTime, targetDateTime,
            WEATHER_REQUESTED_CNT);
        log.debug(
            "[KMA] issueContext - issueDateTime:{}, targetDateTime:{}, targetDates:{}",
            issueContext.getIssueDateTime(), issueContext.getTargetDateTime(),
            issueContext.getTargetForecasts().stream().map(
                LocalDateTime::from));
        return issueContext;
    }

    // 5일치 데이터 찾기 + 근사값 포함
    @Override
    public List<Weather> findWeathers(Location location, ForecastIssueContext issueContext) {
        Instant issueAt = issueContext.getIssueAt();
        List<Instant> targetAtList = issueContext.getTargetForecasts();

        // 시간도 정확한 데이터
        List<Weather> exactMatches = weatherRepository
            .findAllByLocationIdAndForecastedAtAndForecastAtIn(location.getId(), issueAt,
                targetAtList);

        log.debug("[KMA] 시간포함 일치하는 날씨 데이터 총 {}건, ids:{}", exactMatches.size(),
            exactMatches.stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.joining(", ")));

        if (exactMatches.size() >= targetAtList.size()) {
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

        enrichedResults.sort(Comparator.comparing(Weather::getForecastAt));

        log.debug("[KMA] 최종반환 건수:{}건 ", enrichedResults.size());

        return enrichedResults;
    }

    @Override
    public List<Weather> createWeathers(KmaResponse response, List<Weather> cachedWeathers,
        ForecastIssueContext issueContext, Location location) {
        log.debug("[KMA] 날씨 dto 생성 시작");

        List<WeatherItem> allItems = response.response().body().items().weatherItems();
        if (allItems == null || allItems.isEmpty()) {
            throw new WeatherNotFoundException();
        }
        Map<Instant, List<WeatherItem>> itemsByForecastAt = allItems.stream()
            .collect(Collectors.groupingBy(
                item -> extractForecastAt(item.fcstDate(), item.fcstTime()),
                TreeMap::new,               // Instant로 정렬
                Collectors.toList()
            ));
        List<Weather> result = new ArrayList<>();
        Map<LocalDate, Weather> cachedByDate = new HashMap<>();

        for (Map.Entry<Instant, List<WeatherItem>> entry : itemsByForecastAt.entrySet()) {
            Instant targetAt = entry.getKey();
            List<WeatherItem> items = entry.getValue();
            if (items.isEmpty()) {
                continue;
            }

            Weather yesterdayWeather = findYesterdayWeather(cachedByDate, location,
                LocalDate.ofInstant(targetAt, SEOUL_ZONE_ID));
            log.debug("[KMA] yesterdayWeather 객체:{}", yesterdayWeather);

            Weather created = convertToWeather(items, location,
                issueContext, yesterdayWeather);

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
            log.debug("[KMA] 신규 예보 생성 forecastedAt:{}, forecastAt:{}",
                created.getForecastedAt(), created.getForecastAt());
        }

        return result;
    }

    private boolean isExistedWeather(Weather existing, Weather created) {
        if (existing == null || created == null) {
            return false;
        }
        if (existing.getLocation() == null || created.getLocation() == null) {
            return false;
        }

        //시간을 제외하고 년월일까지만 비교
        LocalDate existingForecastDate = existing.getForecastAt() == null ? null
            : LocalDate.ofInstant(existing.getForecastAt(), SEOUL_ZONE_ID);
        LocalDate createdForecastDate = created.getForecastAt() == null ? null
            : LocalDate.ofInstant(created.getForecastAt(), SEOUL_ZONE_ID);

        boolean isExist =
            Objects.equals(existing.getLocation().getId(), created.getLocation().getId())
                && Objects.equals(existing.getForecastedAt(), created.getForecastedAt())
                && Objects.equals(existingForecastDate, createdForecastDate);

        log.debug("[KMA] 기존 예보와 중복 확인.\n"
                + "<기존> forecastedAt:{}, forecastAt:{}, id:{}, 계산된날:{}\n"
                + "<NEW>  forecastedAt:{}, forecastAt:{},        계산된날:{}\n"
                + "isExist:{}"
            , existing.getForecastedAt().atZone(SEOUL_ZONE_ID)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx")),
            existing.getForecastAt().atZone(SEOUL_ZONE_ID)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx")),
            existing.getId(), existingForecastDate
            , created.getForecastedAt().atZone(SEOUL_ZONE_ID)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx")),
            created.getForecastAt().atZone(SEOUL_ZONE_ID)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx")), createdForecastDate,
            isExist);
        return isExist;
    }

    /**
     * @param date yyyyMMdd 형식 (예: 20251010)
     * @param time HHmm 형식 (예: 0930)
     * @return Asia/Seoul 기준 Instant
     */
    private Instant extractForecastAt(String date, String time) {
        if (date == null || time == null) {
            return null;
        }

        LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);

        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        return localDateTime.atZone(SEOUL_ZONE_ID).toInstant();
    }

    // 해당 날짜 최저/최고온도 구하는 로직
    private double[] getDailyTemperatures(List<WeatherItem> dailyItems) {
        Optional<Double> tmn = Optional.empty();
        Optional<Double> tmx = Optional.empty();

        // 1. TMX, TMN 카테고리에서 값 검색
        for (WeatherItem item : dailyItems) {
            KmaCategoryType categoryType = KmaCategoryType.of(item.category());
            if (categoryType == KmaCategoryType.TMN) {
                tmn = Optional.of(parseDouble(item.fcstValue()));
            }
            if (categoryType == KmaCategoryType.TMX) {
                tmx = Optional.of(parseDouble(item.fcstValue()));
            }
        }

        // 2. TMX 또는 TMN 값이 없다면, TMP 카테고리에서 계산
        if (tmn.isEmpty() || tmx.isEmpty()) {
            List<Double> tmpValues = dailyItems.stream()
                .filter(item -> KmaCategoryType.of(item.category()) == KmaCategoryType.TMP)
                .map(item -> parseDouble(item.fcstValue()))
                .toList();

            if (!tmpValues.isEmpty()) {
                if (tmn.isEmpty()) {
                    tmn = tmpValues.stream().min(Double::compare);
                }
                if (tmx.isEmpty()) {
                    tmx = tmpValues.stream().max(Double::compare);
                }
            }
        }
        // 최종적으로 값이 없으면 0.0으로 처리
        return new double[]{tmn.orElse(0.0), tmx.orElse(0.0)};
    }

    // 어제 날씨를 DB에서 조회하는 로직(일단 지금은 날짜만 같으면 최신꺼 하나만 가져옴) 시간 비교 x
    private Weather findYesterdayWeather(Map<LocalDate, Weather> cachedWeather, Location location,
        LocalDate baseDate) {
        LocalDate yesterday = baseDate.minusDays(1);
        log.debug("[KMA] yesterday 날짜:{}", yesterday);

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

    private Weather convertToWeather(
        List<WeatherItem> targetDateItems, Location location,
        ForecastIssueContext issueContext, Weather yesterdayWeather) {
        log.debug("[KMA] convertToWeather 시작");
        WeatherItem anyItem = targetDateItems.get(0);
        Instant forecastedAt = issueContext.getIssueAt();
        Instant forecastAt = toInstant(anyItem.fcstDate(),
            String.format("%04d", Integer.parseInt(
                anyItem.fcstTime())));

        SkyStatus skyStatus = SkyStatus.CLEAR;
        PrecipitationType precipitationType = PrecipitationType.NONE;
        double precipitationAmount = 0d;
        double precipitationProbability = 0d;
        double humidity = 0d;
        double windspeed = 0d;

        for (WeatherItem item : targetDateItems) {
            String value = item.fcstValue();
            KmaCategoryType categoryType = KmaCategoryType.of(item.category());

            switch (categoryType) {
                case SKY -> skyStatus = toSkyStatus(value);
                case PTY -> precipitationType = toPrecipitationType(value);
                case POP -> precipitationProbability = parseDouble(value);
                case PCP -> precipitationAmount = parseDouble(value);
                case REH -> humidity = parseDouble(value);
//                case TMN -> temperatureMin = parseDouble(value);
//                case TMX -> temperatureMax = parseDouble(value);
                case WSD -> windspeed = parseDouble(value);
                default -> log.trace("사용하지 않는 KMA category: {}", item.category());
            }
        }
        // 해당 날짜 최저/최고 온도
        double[] dailyTemperatures = getDailyTemperatures(targetDateItems);
        double temperatureMin = dailyTemperatures[0];
        double temperatureMax = dailyTemperatures[1];

        // 비교값
        double temperatureCompared = 0d;
        double humidityCompared = 0d;
        double currentAvgTemperature = (temperatureMin + temperatureMax) / 2;

        if (yesterdayWeather != null) {
            temperatureCompared = currentAvgTemperature - yesterdayWeather.getTemperature();
            humidityCompared = humidity - yesterdayWeather.getHumidity();
        }
        return Weather.builder()
            .forecastedAt(forecastedAt)
            .forecastAt(forecastAt)
            .skyStatus(skyStatus)
            .precipitationType(precipitationType)
            .precipitationAmount(precipitationAmount)
            .precipitationProbability(precipitationProbability)
            .humidity(humidity)
            .humidityCompared(humidityCompared)
            .temperature(currentAvgTemperature)
            .temperatureCompared(temperatureCompared)
            .temperatureMin(temperatureMin)
            .temperatureMax(temperatureMax)
            .windspeed(windspeed)
            .windspeedLevel(toWindSpeedLevel(windspeed))
            .location(location)
            .build();
    }

    // 오늘날짜의 날씨는 현재시간과 가장 가까운 시간을 보여주고, 다른 날짜의 날씨는 관측된 첫번째 날씨를 보여줌
    private String pickNearestPastOrFirst(List<String> sortedTimesAsc, String currentTime) {
        String target = null;
        for (String time : sortedTimesAsc) {
            if (time.compareTo(currentTime) <= 0) {
                target = time;
            } else {
                break;
            }
        }
        return (target != null) ? target : sortedTimesAsc.get(0);
    }

    private WindspeedLevel toWindSpeedLevel(Double windspeed) {
        if (windspeed < 4) {
            return WindspeedLevel.WEAK;
        }
        if (windspeed < 9) {
            return WindspeedLevel.MODERATE;
        }
        return WindspeedLevel.STRONG;
    }

    private SkyStatus toSkyStatus(String fcstValue) {
        try {
            return switch (Integer.parseInt(fcstValue.trim())) {
                case 1 -> SkyStatus.CLEAR;
                case 3 -> SkyStatus.MOSTLY_CLOUDY;
                case 4 -> SkyStatus.CLOUDY;
                default -> SkyStatus.CLEAR;
            };
        } catch (NumberFormatException e) {
            return SkyStatus.CLEAR;
        }
    }

    private PrecipitationType toPrecipitationType(String fcstValue) {
        try {
            return switch (Integer.parseInt(fcstValue.trim())) {
                case 1 -> PrecipitationType.RAIN;
                case 2 -> PrecipitationType.RAIN_SNOW;
                case 3 -> PrecipitationType.SNOW;
                case 4 -> PrecipitationType.SHOWER;
                default -> PrecipitationType.NONE;
            };
        } catch (NumberFormatException e) {
            return PrecipitationType.NONE;
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank() || "-".equals(value) || "강수없음".equals(value)) {
            return 0d;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }
}
