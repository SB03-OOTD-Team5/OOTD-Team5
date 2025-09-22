package com.sprint.ootd5team.domain.weather.service;

import com.sprint.ootd5team.base.util.CoordinateUtils;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.service.LocationQueryService;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.exception.ProfileNotFoundException;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.external.kma.KmaApiAdapter;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto.WeatherItem;
import com.sprint.ootd5team.domain.weather.mapper.WeatherBuilder;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final WeatherBuilder weatherBuilder;
    private final ProfileRepository profileRepository;
    private final WeatherMapper weatherMapper;
    private final LocationQueryService locationQueryService;
    private final KmaApiAdapter kmaApiAdapter;
    private final String baseTime = "0200";


    @Override
    @Transactional
    public List<WeatherDto> fetchWeatherByLocation(BigDecimal latitude, BigDecimal longitude,
        UUID userId) {
        final String baseDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        log.debug(" userId: {}", userId);
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(ProfileNotFoundException::new);

        List<Weather> weathers = findOrCreateWeathers(latitude, longitude, baseDate, profile);
        return weathers.stream()
            .map((weather) -> weatherMapper.toDto(weather, new ClientCoords(latitude, longitude)))
            .toList();
    }

    private List<Weather> findOrCreateWeathers(BigDecimal latitude,
        BigDecimal longitude, String baseDate, Profile profile) {
        //0. 이미 존재하는 데이터면 가져와서 전달
        List<Weather> cached = getWeathersIfExist(baseDate, latitude, longitude);
        log.debug("[Weather] 해당 데이터 존재 유무: {}", !cached.isEmpty());
        if (!cached.isEmpty()) {
            return cached;
        }
        //1. 기상청 api 데이터 조회
        KmaResponseDto dto = kmaApiAdapter.fetchWeatherFromKma(baseDate, latitude, longitude);

        //2. 날짜별 데이터 묶음
        Map<String, List<WeatherItem>> itemsByDateSlots = groupByForecastSlots(
            dto.response().body().items().weatherItems());
        //3. 엔티티 변환 & 영속화
        return saveWeathers(itemsByDateSlots, profile, latitude, longitude);
    }

    private List<Weather> getWeathersIfExist(String baseDate, BigDecimal latitude,
        BigDecimal longitude
    ) {
        Instant baseDateTimeInstant = weatherBuilder.toInstantWithZone(baseDate, baseTime);

        log.debug("이미 존재하는 weather 데이터 확인 - baseDateTimeInstant:{},latitude:{},longitude:{}",
            baseDateTimeInstant, latitude,
            longitude);

        List<Weather> weathers = weatherRepository.findAllByForecastedAtAndLatitudeAndLongitude(
            baseDateTimeInstant,
            CoordinateUtils.toNumeric(latitude), CoordinateUtils.toNumeric(longitude));

        if (!weathers.isEmpty()) {
            log.debug("[Weather] 데이터 {}건 존재", weathers.size());
            return weathers;
        }
        log.debug("[Weather] 데이터 존재 안함");
        return List.of();
    }

    private Map<String, List<WeatherItem>> groupByForecastSlots(List<WeatherItem> weatherItems) {
        log.debug("[Weather] 그룹핑 시작");
        Set<String> TARGET_TIMES = Set.of("0600", "1500");

        return weatherItems.stream()
            .filter(it -> TARGET_TIMES.contains(it.fcstTime()))
            .peek(it -> log.trace("slot item: {}", it))
            .collect(Collectors.groupingBy(KmaResponseDto.WeatherItem::fcstDate));
    }

    private List<Weather> saveWeathers(Map<String, List<WeatherItem>> itemsByDateSlots,
        Profile testProfile, BigDecimal latitude, BigDecimal longitude) {
        List<Weather> weathers = new ArrayList<>();
        String locationNames = locationQueryService.getLocationNames(latitude, longitude);
        for (Map.Entry<String, List<WeatherItem>> entry : itemsByDateSlots.entrySet()) {
            Weather weather = weatherBuilder.build(testProfile, entry.getValue(),
                CoordinateUtils.toNumeric(latitude),
                CoordinateUtils.toNumeric(longitude), locationNames);
            weathers.add(weather);
        }
        return weatherRepository.saveAll(weathers);
    }


}
