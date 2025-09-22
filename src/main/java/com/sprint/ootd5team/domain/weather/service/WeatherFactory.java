package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.base.util.CoordinateUtils;
import com.sprint.ootd5team.domain.location.service.LocationQueryService;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.external.kma.KmaApiAdapter;
import com.sprint.ootd5team.domain.weather.external.kma.KmaCategoryType;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto.WeatherItem;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyyMMddHHmm");
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private final WeatherRepository weatherRepository;
    private final KmaApiAdapter kmaApiAdapter;
    private final LocationQueryService locationQueryService;

    public List<Weather> findOrCreateWeathers(BigDecimal latitude, BigDecimal longitude,
        Profile profile) {
        String baseDate = LocalDate.now(SEOUL_ZONE_ID).format(DATE_FORMATTER);
        String baseTime = kmaApiAdapter.getBaseTime(baseDate); // 기상청 API BaseTime

        List<Weather> cachedWeathers = findWeathersInCache(baseDate, baseTime, latitude, longitude);
        if (!cachedWeathers.isEmpty()) {
            log.debug("[Weather] 날씨 데이터 이미 존재: {}건", cachedWeathers.size());
            return cachedWeathers;
        }
        log.debug("[Weather] 날씨 데이터 이미 존재 X");
        KmaResponseDto kmaResponse = kmaApiAdapter.fetchWeatherFromKma(baseDate, baseTime, latitude,
            longitude);
        String locationNames = locationQueryService.getLocationNames(latitude, longitude);

        List<Weather> newWeathers = buildWeathersFromKmaResponse(kmaResponse, profile, latitude,
            longitude, locationNames);

        return weatherRepository.saveAll(newWeathers);
    }

    private List<Weather> findWeathersInCache(String baseDate, String baseTime, BigDecimal latitude,
        BigDecimal longitude) {
        Instant forcastedAt = toInstant(baseDate, baseTime);
        log.debug("[Weather repository] 날씨 존재하는지 확인중 forcastedAt:{}, lat: {}, lon: {}",
            forcastedAt, latitude, longitude);
        return weatherRepository.findAllByForecastedAtAndLatitudeAndLongitude(
            forcastedAt,
            CoordinateUtils.toNumeric(latitude),
            CoordinateUtils.toNumeric(longitude)
        );
    }

    private List<Weather> buildWeathersFromKmaResponse(KmaResponseDto kmaResponse, Profile profile,
        BigDecimal latitude, BigDecimal longitude, String locationNames) {

        List<WeatherItem> allItems = kmaResponse.response().body().items().weatherItems();
        if (allItems == null || allItems.isEmpty()) {
            throw new WeatherNotFoundException();
        }
        List<Weather> result = new ArrayList<>();
        String currentTime = LocalDateTime.now(SEOUL_ZONE_ID)
            .format(DateTimeFormatter.ofPattern("HHmm"));

        log.debug("[Weather] buildWeathersFromKmaResponse 시작. currentTime:{}", currentTime);

        Map<String, List<WeatherItem>> itemsByDate = allItems.stream()
            .collect(Collectors.groupingBy(WeatherItem::fcstDate));
        // 날짜 오름차순으로 정렬
        List<String> sortedDates = itemsByDate.keySet().stream().sorted().toList();

        // 첫 예보일(오늘)의 비교 대상인 '어제' 날씨를 DB에서 조회
        Weather yesterdayWeather = findYesterdayWeather(latitude, longitude, sortedDates.get(0));

        for (Map.Entry<String, List<WeatherItem>> entry : itemsByDate.entrySet()) {
            List<WeatherItem> itemsOfDate = entry.getValue();
            if (itemsOfDate.isEmpty()) {
                continue;
            }

            // 데이터에 존재하는 날짜 뽑기
            List<String> availableTimes = itemsOfDate.stream().map(WeatherItem::fcstTime).distinct()
                .sorted().toList();

            // 현재 시간과 비교해서 가장 가까운 시간대 가져오기
            String targetTime = pickNearestPastOrFirst(availableTimes, currentTime);
            log.debug("[Weather] availableTimes:{}, targetTime:{}", availableTimes, targetTime);

            List<WeatherItem> itemsForTargetSlot = itemsOfDate.stream()
                .filter(it -> targetTime.equals(it.fcstTime()))
                .toList();

            log.debug("[Weather] itemsForTargetSlot:{}", itemsForTargetSlot.toString());

            if (itemsForTargetSlot.isEmpty()) {
                continue;
            }

            Weather currentWeather = buildSingleWeather(itemsOfDate, itemsForTargetSlot, profile,
                latitude,
                longitude, locationNames, yesterdayWeather);
            result.add(currentWeather);

            // 다음날 날씨에 현재 날시 전달
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
    private Weather findYesterdayWeather(BigDecimal latitude, BigDecimal
        longitude, String todayDateStr) {
        LocalDate yesterday = LocalDate.parse(todayDateStr,
            DATE_FORMATTER).minusDays(1);
        Instant startOfYesterday =
            yesterday.atStartOfDay(SEOUL_ZONE_ID).toInstant();
        Instant endOfYesterday =
            yesterday.atTime(LocalTime.MAX).atZone(SEOUL_ZONE_ID).toInstant();

        return weatherRepository.findFirstByLatitudeAndLongitudeAndForecastAtBetweenOrderByForecastAtDesc(
            CoordinateUtils.toNumeric(latitude),
            CoordinateUtils.toNumeric(longitude),
            startOfYesterday,
            endOfYesterday
        ).orElse(null);
    }

    private Weather buildSingleWeather(List<WeatherItem> dailyItems,
        List<WeatherItem> targetDateItems, Profile profile,
        BigDecimal latitude, BigDecimal longitude, String locationNames, Weather yesterdayWeather) {
        WeatherItem anyItem = targetDateItems.get(0);
        Instant forecastedAt = toInstant(anyItem.baseDate(), anyItem.baseTime());
        Instant forecastAt = toInstant(anyItem.fcstDate(), anyItem.fcstTime());

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
            .profile(profile)
            .forecastedAt(forecastedAt)
            .forecastAt(forecastAt)
            .skyStatus(skyStatus)
            .latitude(latitude)
            .longitude(longitude)
            .xCoord(anyItem.nx())
            .yCoord(anyItem.ny())
            .locationNames(locationNames)
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
            .build();
    }

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

    private Instant toInstant(String date, String time) {
        LocalDateTime ldt = LocalDateTime.parse(date + time, DATETIME_FORMATTER);
        return ldt.atZone(SEOUL_ZONE_ID).toInstant();
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
