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
import java.util.ArrayList;
import java.util.Collections;
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
            log.debug("[Weather] 날씨 데이터 이미 존재: {}건", cachedWeathers.size());
            return cachedWeathers;
        }
        // 2. 날씨 데이터 불러오기
        String baseDate = issueContext.getIssueDateTime().format(DATE_FORMATTER);
        String baseTime = issueContext.getIssueDateTime().format(TIME_FORMATTER);
        log.debug("[Weather] 날씨 데이터 존재 X");
        KmaResponse kmaResponse = kmaApiAdapter.getWeather(location.getLatitude(),
            location.getLongitude(), baseDate, baseTime, 1000);
        // 3. 날씨 생성
        List<Weather> newWeathers = createWeathers(kmaResponse, Collections.emptyList(),
            issueContext, location);
        return weatherRepository.saveAll(newWeathers);
    }

    @Override
    public ForecastIssueContext createForecastIssueContext(ZonedDateTime reference) {
        LocalDate referenceDate = reference.toLocalDate();
        // 발행 시각 base time 구함
        LocalTime issueTime = kmaApiAdapter.resolveIssueTime(reference); // 기상청 API BaseTime
        ZonedDateTime issueDateTime = getZonedDateTime(
            referenceDate, issueTime);
        // 예보 시각 base time 구함
        LocalTime targetTime = kmaApiAdapter.resolveIssueTime(reference);
        ZonedDateTime targetDateTime = getZonedDateTime(
            referenceDate, targetTime);
        ForecastIssueContext issueContext = ForecastIssueContext.of(issueDateTime, targetDateTime,
            WEATHER_REQUESTED_CNT);
        log.debug("[KMA] issueContext - issueDateTime:{}, targetDateTime:{}",
            issueContext.getIssueDateTime(), issueContext.getTargetDateTime());
        return issueContext;
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
    public List<Weather> createWeathers(KmaResponse kmaResponse, List<Weather> existingWeathers,
        ForecastIssueContext issueContext, Location location) {
        log.debug("[Weather] 날씨 dto 생성 시작");
        String baseDate = issueContext.getIssueDateTime().toLocalDate()
            .format(DATE_FORMATTER);
        return buildWeathersFromKmaResponse(kmaResponse, baseDate, location, issueContext);
    }

    private List<Weather> buildWeathersFromKmaResponse(KmaResponse kmaResponse, String baseDate,
        Location location, ForecastIssueContext issueContext) {

        List<WeatherItem> allItems = kmaResponse.response().body().items().weatherItems();
        if (allItems == null || allItems.isEmpty()) {
            throw new WeatherNotFoundException();
        }
        List<Weather> result = new ArrayList<>();
        String currentTime = LocalDateTime.now(SEOUL_ZONE_ID)
            .format(TIME_FORMATTER);

        log.debug("[Weather] buildWeathersFromKmaResponse 시작. currentTime:{}", currentTime);

        Map<String, List<WeatherItem>> itemsByDate = allItems.stream()
            .collect(Collectors.groupingBy(WeatherItem::fcstDate));
        // 날짜 오름차순으로 정렬
        List<String> sortedDates = itemsByDate.keySet().stream().sorted().toList();
        // 첫 예보일(오늘)의 비교 대상인 '어제' 날씨를 DB에서 조회
        Weather yesterdayWeather = findYesterdayWeather(location, sortedDates.get(0));

        for (Map.Entry<String, List<WeatherItem>> entry : itemsByDate.entrySet()) {
            List<WeatherItem> itemsOfDate = entry.getValue();
            if (itemsOfDate.isEmpty()) {
                continue;
            }

            // 데이터에 존재하는 날짜 뽑기
            List<String> availableTimes = itemsOfDate.stream()
                .map(WeatherItem::fcstTime)
                .distinct()
                .sorted()
                .toList();
            if (availableTimes.isEmpty()) {
                continue;
            }

            String forecastDate = entry.getKey();
            boolean isToday = baseDate.equals(forecastDate);

            // 오늘 날짜만 현재 시간과 비교해서 가장 가까운 시간대 선택
            String targetTime = isToday
                ? pickNearestPastOrFirst(availableTimes, currentTime)
                : availableTimes.get(0);
            log.debug(
                "[Weather] forecastDate:{}, availableTimes:{}, targetTime:{}",
                forecastDate,
                availableTimes,
                targetTime
            );

            List<WeatherItem> itemsForTargetSlot = itemsOfDate.stream()
                .filter(it -> targetTime.equals(it.fcstTime()))
                .toList();

            log.debug("[Weather] itemsForTargetSlot:{}", itemsForTargetSlot.toString());

            if (itemsForTargetSlot.isEmpty()) {
                continue;
            }

            Weather currentWeather = buildSingleWeather(itemsOfDate, itemsForTargetSlot, location,
                yesterdayWeather, issueContext);
            result.add(currentWeather);

            // 다음날 날씨에 현재 날씨 전달
            yesterdayWeather = currentWeather;
        }
        return result;
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

    // 어제 날씨를 DB에서 조회하는 로직
    private Weather findYesterdayWeather(Location location, String todayDateStr) {
        LocalDate yesterday = LocalDate.parse(todayDateStr,
            DATE_FORMATTER).minusDays(1);
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

    private Weather buildSingleWeather(List<WeatherItem> dailyItems,
        List<WeatherItem> targetDateItems, Location location, Weather yesterdayWeather,
        ForecastIssueContext issueContext) {
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
        double[] dailyTemperatures = getDailyTemperatures(dailyItems);
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
