package com.sprint.ootd5team.base.batch;

import static com.sprint.ootd5team.base.util.DateTimeUtils.DATE_FORMATTER;
import static com.sprint.ootd5team.base.util.DateTimeUtils.SEOUL_ZONE_ID;
import static com.sprint.ootd5team.base.util.DateTimeUtils.TIME_FORMATTER;

import com.sprint.ootd5team.domain.location.dto.data.LocationWithProfileIds;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.repository.LocationRepository;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.external.WeatherExternalAdapter;
import com.sprint.ootd5team.domain.weather.external.WeatherFactory;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherBatchWriter implements ItemWriter<LocationWithProfileIds> {

    private final WeatherFactory weatherFactory;
    private final WeatherExternalAdapter externalAdapter;
    private final WeatherService weatherService;
    private final NotificationService notificationService;
    private final WeatherRepository weatherRepository;
    private final LocationRepository locationRepository;

    public WeatherBatchWriter(Map<String, WeatherFactory> weatherFactories,
        Map<String, WeatherExternalAdapter> externalAdapters, WeatherService weatherService,
        NotificationService notificationService, WeatherRepository weatherRepository,
        LocationRepository locationRepository,
        @Value("${weather.api-client.provider}") String provider) {
        this.weatherFactory = resolveFactory(weatherFactories, provider);
        this.externalAdapter = resolveAdapter(externalAdapters, provider);
        this.weatherService = weatherService;
        this.notificationService = notificationService;
        this.weatherRepository = weatherRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public void write(Chunk<? extends LocationWithProfileIds> chunk) throws Exception {
        try {
            ZonedDateTime issueDate = ZonedDateTime.now(SEOUL_ZONE_ID);
            LocalTime issueTime = externalAdapter.resolveIssueTime(issueDate);
            log.info("[WeatherBatchWriter] write start chunkSize={} issueDate={} issueTime={}",
                chunk.size(), issueDate, issueTime);
            ForecastIssueContext issueContext = weatherFactory.createForecastIssueContext(
                issueDate);

            // chunk에 있는 데이터를 fetch함
            for (LocationWithProfileIds item : chunk) {
                List<UUID> profileIds = item.profileIds();

                if (item.locationId() == null) {
                    log.info("[WeatherBatchWriter] locationId 정보가 없어 건너뜀 profiles={}", profileIds);
                    continue;
                }

                Location location = locationRepository.findById(item.locationId()).orElse(null);
                if (location == null) {
                    log.info("[WeatherBatchWriter] location 엔티티를 찾을 수 없음 locationId={} profiles={}",
                        item.locationId(), profileIds);
                    continue;
                }

                // 1. 날씨 찾기
                List<Weather> weathers = weatherFactory.findWeathers(location, issueContext);
                if (weathers.isEmpty()) {
                    // 2. 날씨 데이터 불러오기
                    String baseDate = issueContext.getIssueDateTime().format(DATE_FORMATTER);
                    String baseTime = issueContext.getIssueDateTime().format(TIME_FORMATTER);
                    Object response = externalAdapter.getWeather(item.latitude(),
                        item.longitude(), baseDate, baseTime, 300);
                    log.info("[WeatherBatchWriter] 외부 API 호출 완료 locationId={} lat={} lon={}",
                        location.getId(), item.latitude(), item.longitude());

                    // 3. 날씨 생성
                    weathers = weatherFactory.createWeathers(response,
                        null,
                        issueContext,
                        location);
                    weatherRepository.saveAll(weathers);
                    log.info("[WeatherBatchWriter] 새 weather {}건 저장 locationId={}}",
                        weathers.size(),
                        location.getId());

                }

                createNotification(location, issueContext, weathers, profileIds);

            }

        } catch (Exception e) {
            log.error("[WeatherBatchWriter] 처리 실패: chunk size={} 원인={}", chunk.size(),
                e.getMessage(), e);
            throw e;
        }
    }

    private void createNotification(Location location, ForecastIssueContext issueContext,
        List<Weather> weathers, List<UUID> profileIds) {
        //  날씨정보와 마지막으로 저장된 오늘 날씨정보를 비교 -> 차이가 크면 알림 보냄
        Weather latestTodayWeather = weatherService.getLatestWeatherForLocationAndDate(
            location.getId(), LocalDate.now(SEOUL_ZONE_ID));

        //  weathers 중 다음날 날씨만 가져와서 오늘 날씨와 비교
        LocalDate targetDate = issueContext.getTargetDateTime().plusDays(1).toLocalDate();
        Weather tomorrowWeather = weathers.stream()
            .filter(
                weather -> LocalDateTime.ofInstant(weather.getForecastAt(), SEOUL_ZONE_ID)
                    .toLocalDate().isEqual(targetDate))
            .max((w1, w2) -> w1.getForecastAt().compareTo(w2.getForecastAt()))
            .orElse(null);

        if (tomorrowWeather == null) {
            log.info("[WeatherBatchWriter] 내일 예보 없음 locationId={}", location.getId());
            return;
        }

        NotificationDecision decision = evaluateNotification(latestTodayWeather,
            tomorrowWeather);

        if (!decision.shouldNotify()) {
            log.info("[WeatherBatchWriter] 알림 조건 미충족 locationId={} reason={}",
                location.getId(), decision.reason());
            return;
        }

        log.info("[WeatherBatchWriter] 알림 전송 locationId={} targetCount={} reason={}",
            location.getId(),
            profileIds.size(),
            decision.reason());
        for (UUID profileId : profileIds) {
            notificationService.createWeatherNotification(profileId, decision.reason());
        }
    }


    private NotificationDecision evaluateNotification(Weather latestTodayWeather,
        Weather tomorrowWeather) {
        log.info("[WeatherBatchWriter] 알림생성용 날씨 비교");

        if (latestTodayWeather == null || tomorrowWeather == null) {
            return NotificationDecision.create(false, null);
        }

        PrecipitationType todayPrecipitation = latestTodayWeather.getPrecipitationType();
        PrecipitationType tomorrowPrecipitation = tomorrowWeather.getPrecipitationType();
        SkyStatus todaySky = latestTodayWeather.getSkyStatus();
        SkyStatus tomorrowSky = tomorrowWeather.getSkyStatus();

        Double todayMinTemperature = latestTodayWeather.getTemperatureMin();
        Double tomorrowMinTemperature = tomorrowWeather.getTemperatureMin();
        Double temperatureDiff = (todayMinTemperature != null && tomorrowMinTemperature != null)
            ? tomorrowMinTemperature - todayMinTemperature
            : null;

        boolean todayIsWet = todayPrecipitation != null
            && todayPrecipitation != PrecipitationType.NONE;
        boolean tomorrowIsWet = tomorrowPrecipitation != null
            && tomorrowPrecipitation != PrecipitationType.NONE;
        boolean todayIsCloudy = todaySky != null && todaySky != SkyStatus.CLEAR;
        boolean tomorrowIsClear = (tomorrowSky != null && tomorrowSky == SkyStatus.CLEAR)
            && !tomorrowIsWet;
        boolean todayIsClear = (todaySky != null && todaySky == SkyStatus.CLEAR)
            && !todayIsWet;

        List<String> reasons = new ArrayList<>();

        if ((todayIsWet || todayIsCloudy) && tomorrowIsClear) {
            reasons.add("🌞 내일은 날씨가 화창해집니다.");
        }
        if (todayIsClear && tomorrowIsWet) {
            reasons.add("☔️ 내일은 비가 오네요. 우산을 챙기세요.");
        }
        if (temperatureDiff != null && temperatureDiff <= -3) {
            reasons.add("내일은 일교차가 큽니다. 두꺼운 옷을 대비하세요.");
        } else if (temperatureDiff != null && temperatureDiff >= 3) {
            reasons.add("내일은 오늘보다 더워집니다. 얇게 입으세요.");
        }

        log.info(
            "[WeatherBatchWriter] 비교 결과 todayWet={} todayCloudy={} tomorrowClear={} tomorrowWet={} tempDiff={} reasons={}"
            , todayIsWet, todayIsCloudy, tomorrowIsClear, tomorrowIsWet,
            temperatureDiff, reasons);

        if (reasons.isEmpty()) {
            return NotificationDecision.create(false, null);
        }

        return NotificationDecision.create(true, String.join(" ", reasons));
    }


    private WeatherFactory resolveFactory(Map<String, WeatherFactory> factories, String provider) {
        String beanName = switch (provider) {
            case "kma" -> "kmaWeatherFactory";
            case "openWeather" -> "openWeatherFactory";
            case "meteo" -> "openMeteoFactory";
            default -> throw new IllegalArgumentException(
                "지원하지 않는 weather provider 입니다: " + provider);
        };
        WeatherFactory factory = factories.get(beanName);
        if (factory == null) {
            throw new IllegalStateException(
                "WeatherFactory 빈을 찾을 수 없습니다. beanName=" + beanName);
        }
        return factory;
    }

    private WeatherExternalAdapter resolveAdapter(Map<String, WeatherExternalAdapter> adapters,
        String provider) {
        String beanName = switch (provider) {
            case "kma" -> "kmaApiAdapter";
            case "openWeather" -> "openWeatherAdapter";
            case "meteo" -> "openMeteoAdapter";
            default -> throw new IllegalArgumentException(
                "지원하지 않는 weather provider 입니다: " + provider);
        };
        WeatherExternalAdapter adapter = adapters.get(beanName);
        if (adapter == null) {
            throw new IllegalStateException(
                "WeatherExternalAdapter 빈을 찾을 수 없습니다. beanName=" + beanName);
        }
        return adapter;
    }

    private record NotificationDecision(boolean shouldNotify, String reason) {

        static NotificationDecision create(boolean shouldNotify, String reason) {
            return new NotificationDecision(shouldNotify, reason);
        }
    }
}
