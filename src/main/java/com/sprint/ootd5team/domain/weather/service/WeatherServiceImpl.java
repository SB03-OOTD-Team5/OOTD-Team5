package com.sprint.ootd5team.domain.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.exception.ProfileNotFoundException;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
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
import java.math.RoundingMode;
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
import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@RequiredArgsConstructor
@Service
public class WeatherServiceImpl implements WeatherService {

    private final WebClient kmaApiClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WeatherRepository weatherRepository;
    private final WeatherBuilder weatherBuilder;
    private final ProfileRepository profileRepository;
    private final WeatherMapper weatherMapper;
    private final String baseTime = "0200";
    private String baseDate;


    @Override
    @Transactional
    public List<WeatherDto> fetchWeatherByLocation(BigDecimal longitude, BigDecimal latitude) {
        //TODO: temp, 실제 사용자 프로필 가져오기
        Profile testProfile = profileRepository.findById(
                UUID.fromString("036220a1-1223-4ba5-943e-48452526cbe9"))
            .orElseThrow(ProfileNotFoundException::new);

        baseDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        //0. 이미 존재하는 데이터면 가져와서 전달 후 종료
        List<WeatherDto> existed = getWeatherDtosIfExist(longitude, latitude);
        if (!existed.isEmpty()) {
            return existed;
        }

        //1. 기상청 api 데이터 조회
        String response = fetchKmaApi(longitude, latitude);
        KmaResponseDto kmaResponseDto = parseToKmaResponseDto(response);
        //2. 기상청 api 데이터  valid check
        validateKmaData(kmaResponseDto);
        //3. 날짜별 데이터 묶음
        Map<String, List<WeatherItem>> itemsByDateSlots = groupByForecastSlots(
            kmaResponseDto.response().body().items().weatherItems());
        //4. 엔티티 변환 & 영속화
        List<Weather> weathers = saveWeathers(itemsByDateSlots, testProfile, latitude,
            longitude);

        return weathers.stream().map(weatherMapper::toDto).toList();
    }

    private List<WeatherDto> getWeatherDtosIfExist(BigDecimal longitude, BigDecimal latitude) {
        Instant baseDateTimeInstant = weatherBuilder.toInstantWithZone(baseDate, baseTime);

        log.debug("이미 존재하는 weather 데이터 확인 - baseDateTimeInstant:{},latitude:{},longitude:{}",
            baseDateTimeInstant, latitude,
            longitude);

        List<Weather> weathers = weatherRepository.findAllByForecastedAtAndLatitudeAndLongitude(
            baseDateTimeInstant,
            toNumeric(latitude), toNumeric(longitude));

        if (!weathers.isEmpty()) {
            log.debug("데이터 {}건 존재", weathers.size());
            return weathers.stream().map(weatherMapper::toDto).toList();
        }
        log.debug("데이터 존재 안함");
        return List.of();
    }

    private String fetchKmaApi(BigDecimal longitude, BigDecimal latitude) {
        try {
            GridXY kmaXY = convertGridXY(longitude, latitude);
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
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return response;
        } catch (Exception e) {
            throw new WeatherKmaFetchException();
        }
    }

    private GridXY convertGridXY(BigDecimal longitude, BigDecimal latitude) {
        return KmaGridConverter.toGrid(longitude, latitude);

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
        for (Map.Entry<String, List<WeatherItem>> entry : itemsByDateSlots.entrySet()) {
            Weather weather = weatherBuilder.build(testProfile, entry.getValue(), latitude,
                longitude);
            weathers.add(weather);
        }
        return weatherRepository.saveAll(weathers);
    }

    // NUMERIC(8,4) → 소수점 이하 4자리, 반올림 적용
    private BigDecimal toNumeric(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }

}
