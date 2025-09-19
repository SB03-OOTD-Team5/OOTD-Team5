package com.sprint.ootd5team.domain.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.util.CoordinateUtils;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.service.LocationQueryService;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.exception.ProfileNotFoundException;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.exception.ConvertCoordFailException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaFetchException;
import com.sprint.ootd5team.domain.weather.exception.WeatherKmaParseException;
import com.sprint.ootd5team.domain.weather.external.kma.KmaGridConverter;
import com.sprint.ootd5team.domain.weather.external.kma.KmaGridConverter.GridXY;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    private final WebClient kmaApiClient;
    private final ObjectMapper mapper;
    private final WeatherRepository weatherRepository;
    private final WeatherBuilder weatherBuilder;
    private final ProfileRepository profileRepository;
    private final WeatherMapper weatherMapper;
    private final String baseTime = "0200";
    private final LocationQueryService locationQueryService;


    public WeatherServiceImpl(@Qualifier("kmaApiClient") WebClient kmaApiClient,
        WeatherRepository weatherRepository,
        WeatherBuilder weatherBuilder, ProfileRepository profileRepository,
        WeatherMapper weatherMapper, LocationQueryService locationQueryService) {
        this.kmaApiClient = kmaApiClient;
        this.mapper = new ObjectMapper();
        this.weatherRepository = weatherRepository;
        this.weatherBuilder = weatherBuilder;
        this.profileRepository = profileRepository;
        this.weatherMapper = weatherMapper;
        this.locationQueryService = locationQueryService;
    }

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
        KmaResponseDto dto = parseToKmaResponseDto(fetchKmaApi(baseDate, latitude, longitude));
        //2.   validation 체크
        validateKmaData(dto);
        //3. 날짜별 데이터 묶음
        Map<String, List<WeatherItem>> itemsByDateSlots = groupByForecastSlots(
            dto.response().body().items().weatherItems());
        //4. 엔티티 변환 & 영속화
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

    private String fetchKmaApi(String baseDate, BigDecimal latitude, BigDecimal longitude) {
        GridXY kmaXY = convertGridXY(latitude, longitude);

        try {
            log.debug(
                "[Weather] 날씨 정보 조회 요청 longitude:{},latitude:{},x:{},y:{},base date:{},base time:{}",
                longitude, latitude,
                kmaXY.x(), kmaXY.y(), baseDate, baseTime);

            String response = kmaApiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", kmaXY.x())
                    .queryParam("ny", kmaXY.y())
                    .build())
                .exchangeToMono(
                    clientResponse -> clientResponse.bodyToMono(String.class)
                )
                .block(); // TODO:  타임아웃,retrieve 설정
            log.debug("[Weather] 날씨 정보 조회 완료");

            return response;
        } catch (Exception e) {
            throw new WeatherKmaFetchException();
        }
    }

    private GridXY convertGridXY(BigDecimal latitude, BigDecimal longitude) {
        try {
            GridXY gridXY = KmaGridConverter.toGrid(longitude, latitude);
            log.debug("[Weather] 좌표변환완료: x:{},y:{}", gridXY.x(), gridXY.y());
            return gridXY;
        } catch (Exception e) {
            ConvertCoordFailException ex = new ConvertCoordFailException();
            ex.addDetail("latitude", latitude);
            ex.addDetail("longitude", longitude);
            throw e;
        }
    }

    private KmaResponseDto parseToKmaResponseDto(String responseJson) {
        try {
            return mapper.readValue(responseJson, KmaResponseDto.class);
        } catch (Exception e) {
            throw new WeatherKmaParseException();
        }
    }

    private void validateKmaData(KmaResponseDto kmaDto) {
        // TODO: 임시. resultcode 가져와서 enum으로 셋팅해야함
        if (!kmaDto.response().header().resultCode().equals("00")) {
            throw new WeatherKmaParseException(kmaDto.response().header().resultMsg());
        }
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
