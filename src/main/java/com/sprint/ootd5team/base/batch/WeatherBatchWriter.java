package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.domain.location.dto.data.LocationWithProfileIds;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.repository.LocationRepository;
import com.sprint.ootd5team.domain.notification.service.NotificationService;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.external.kma.KmaApiAdapter;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import com.sprint.ootd5team.domain.weather.service.WeatherFactory;
import com.sprint.ootd5team.domain.weather.service.WeatherService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherBatchWriter implements ItemWriter<LocationWithProfileIds> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private final WeatherFactory weatherFactory;
    private final KmaApiAdapter kmaApiAdapter;
    private final WeatherService weatherService;
    private final NotificationService notificationService;
    private final WeatherRepository weatherRepository;
    private final LocationRepository locationRepository;
    //    private final String BASE_TIME = "2300";
    private final String BASE_TIME = "2000"; // 테스트용


    @Override
    public void write(Chunk<? extends LocationWithProfileIds> chunk) throws Exception {
        try {
            String baseDate = LocalDate.now(SEOUL_ZONE_ID).format(DATE_FORMATTER);
            String baseTime = kmaApiAdapter.getBaseTime(baseDate);
            log.info("[WeatherBatchWriter] write start chunkSize={} baseDate={} baseTime={}",
                chunk.size(), baseDate, baseTime);

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

                Weather latestWeather = weatherService.getLastestPerLocationId(location.getId());

                log.info("[WeatherBatchWriter] 외부 API 호출 locationId={} lat={} lon={}",
                    location.getId(), item.latitude(), item.longitude());
                KmaResponseDto kmaResponseDto = kmaApiAdapter.getKmaWeather(baseDate, BASE_TIME,
                    item.latitude(), item.longitude(), 300);

                List<Weather> newWeathers = weatherFactory.createWeathers(kmaResponseDto, baseDate,
                    location);

                weatherRepository.saveAll(newWeathers);
                log.info("[WeatherBatchWriter] 새 예보 저장 locationId={} savedCount={}",
                    location.getId(), newWeathers.size());

                LocalDate targetDate = LocalDate.parse(baseDate, DATE_FORMATTER).plusDays(1);
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

                boolean shouldNotify = isShouldNotify(latestWeather, tomorrowWeather);

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

    private boolean isShouldNotify(Weather latestWeather, Weather tomorrowWeather) {
        if (latestWeather == null) {
            log.info("[WeatherBatchWriter] 기존 예보 없음 -> 신규 데이터로 알림 전송 예정");
            return true;
        }

        boolean changedToRain = latestWeather.getPrecipitationType() == PrecipitationType.NONE
            && tomorrowWeather.getPrecipitationType() != PrecipitationType.NONE;

        Double latestMinTemp = latestWeather.getTemperatureMin();
        Double tomorrowMinTemp = tomorrowWeather.getTemperatureMin();
        boolean temperatureChanged = latestMinTemp != null && tomorrowMinTemp != null
            && Math.abs(tomorrowMinTemp - latestMinTemp) >= 3;

        log.info("[WeatherBatchWriter] 비교 결과 precipChange={} tempDiff={} changedTemp={}",
            changedToRain,
            (latestMinTemp != null && tomorrowMinTemp != null)
                ? Math.abs(tomorrowMinTemp - latestMinTemp)
                : null,
            temperatureChanged);

        return changedToRain || temperatureChanged;
    }
}
