package com.sprint.ootd5team.base.batch;

import static com.sprint.ootd5team.base.util.DateTimeUtils.DATE_FORMATTER;
import static com.sprint.ootd5team.base.util.DateTimeUtils.SEOUL_ZONE_ID;
import static com.sprint.ootd5team.base.util.DateTimeUtils.TIME_FORMATTER;

import com.sprint.ootd5team.domain.location.dto.data.LocationWithProfileIds;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.repository.LocationRepository;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.external.WeatherExternalAdapter;
import com.sprint.ootd5team.domain.weather.external.WeatherFactory;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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

                // 새롭게 fetch된 내일 날씨정보와 마지막으로 저장된 오늘 날씨정보를 비교 -> 차이가 크면 알림 보냄
                Weather latestTodayWeather = weatherService.getLatestWeatherForLocationAndDate(
                    location.getId(), LocalDate.now(SEOUL_ZONE_ID));

                String baseDate = issueContext.getIssueDateTime().format(DATE_FORMATTER);
                String baseTime = issueContext.getIssueDateTime().format(TIME_FORMATTER);
                Object response = externalAdapter.getWeather(item.latitude(),
                    item.longitude(), baseDate, baseTime, 300);

                log.info("[WeatherBatchWriter] 외부 API 호출 완료 locationId={} lat={} lon={}",
                    location.getId(), item.latitude(), item.longitude());

                List<Weather> cached = weatherFactory.findWeathers(location, issueContext);
                List<Weather> newWeathers = weatherFactory.createWeathers(response,
                    cached,
                    issueContext,
                    location);

                log.info("[WeatherBatchWriter] weather {}건 생성완료", newWeathers.size());

                weatherRepository.saveAll(newWeathers);
                log.info("[WeatherBatchWriter] 새 weather {}건 저장 locationId={}}", newWeathers.size(),
                    location.getId());

                // 새로 생성된 weather 중 다음날 날씨만 가져와서 오늘 날씨와 비교
                LocalDate targetDate = issueContext.getTargetDateTime().plusDays(1).toLocalDate();
                Weather tomorrowWeather = newWeathers.stream()
                    .filter(
                        weather -> LocalDateTime.ofInstant(weather.getForecastAt(), SEOUL_ZONE_ID)
                            .toLocalDate().isEqual(targetDate))
                    .max((w1, w2) -> w1.getForecastAt().compareTo(w2.getForecastAt()))
                    .orElse(null);

                if (tomorrowWeather == null) {
                    log.info("[WeatherBatchWriter] 내일 예보 없음 locationId={}", location.getId());
                    continue;
                }

                boolean shouldNotify = isShouldNotify(latestTodayWeather, tomorrowWeather);

                if (!shouldNotify) {
                    log.info("[WeatherBatchWriter] 알림 조건 미충족 locationId={}", location.getId());
                    continue;
                }

                log.info("[WeatherBatchWriter] 알림 전송 locationId={} targetCount={}",
                    location.getId(),
                    profileIds.size());
                for (UUID profileId : profileIds) {
                    notificationService.createWeatherNotification(profileId, "날씨알림이 있습니다");
                }
            }

        } catch (Exception e) {
            log.error("[WeatherBatchWriter] 처리 실패: chunk size={} 원인={}", chunk.size(),
                e.getMessage(), e);
            throw e;
        }
    }

    private boolean isShouldNotify(Weather latestTodayWeather, Weather tomorrowWeather) {
        if (latestTodayWeather == null) {
            log.info("[WeatherBatchWriter] 기존 예보 없음");
            return true;
        }

        log.info("[WeatherBatchWriter] 기존 예보 있음 -> latestWeatherId={}, locationId={}",
            latestTodayWeather.getId(), latestTodayWeather.getLocation().getId());

        // 알림 조건 1. 강수타입이 달라짐
        boolean changedToPrecipitation =
            latestTodayWeather.getPrecipitationType() != tomorrowWeather.getPrecipitationType();
        Double latestMinTemp = latestTodayWeather.getTemperatureMin();
        Double tomorrowMinTemp = tomorrowWeather.getTemperatureMin();

        // 알림 조건 2. 온도가 3도이상 차이남
        boolean temperatureChanged = latestMinTemp != null && tomorrowMinTemp != null
            && Math.abs(tomorrowMinTemp - latestMinTemp) >= 3;

        log.info("[WeatherBatchWriter] 비교 결과 precipChange={} tempDiff={} changedTemp={}",
            changedToPrecipitation,
            (latestMinTemp != null && tomorrowMinTemp != null)
                ? Math.abs(tomorrowMinTemp - latestMinTemp)
                : null,
            temperatureChanged);

        return changedToPrecipitation || temperatureChanged;
    }

    private WeatherFactory resolveFactory(Map<String, WeatherFactory> factories, String provider) {
        String beanName = switch (provider) {
            case "kma" -> "kmaWeatherFactory";
            case "openWeather" -> "openWeatherFactory";
            case "meteo" -> "meteoWeatherFactory";
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
}
