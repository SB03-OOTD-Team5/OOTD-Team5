package com.sprint.ootd5team.domain.weather.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto.WeatherItem;
import com.sprint.ootd5team.domain.weather.mapper.WeatherBuilder;
import com.sprint.ootd5team.domain.weather.mapper.WeatherMapper;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
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
    // 굳이 왜 이렇게 하느거지?
    private final ObjectMapper mapper = new ObjectMapper();
    private final WeatherRepository weatherRepository;
    private final WeatherBuilder weatherBuilder;
    private final ProfileRepository profileRepository;
    private final WeatherMapper weatherMapper;


    @Override
    @Transactional
    public List<WeatherDto> fetchWeatherByDateAndLocation() {
        List<WeatherDto> weatherDtos = new ArrayList<>();
        //temp 데이터
        BigDecimal longitude = new BigDecimal("37.5665");
        BigDecimal latitude = new BigDecimal("127.34234");
        Integer xCoord = 55;
        Integer yCoord = 127;
        Profile testProfile = profileRepository.findById(
                UUID.fromString("036220a1-1223-4ba5-943e-48452526cbe9"))
            .orElseThrow(() -> new RuntimeException("프로필을 찾을수 없습니다."));

        log.debug("[Weather] 날씨 정보 조회 요청");

        String response = kmaApiClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("base_date", "20250916")
                .queryParam("base_time", "0200")
                .queryParam("nx", xCoord)
                .queryParam("ny", yCoord)
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .block();

        KmaResponseDto kmaResponseDto = parseToKmaResponseDto(response);
        // api data valid check
        validateKmaData(kmaResponseDto);

        Map<String, List<WeatherItem>> itemsByForecastDateMap = groupByForecastSlots(
            kmaResponseDto.response().body().items().weatherItems());

        List<Weather> weathers = saveWeathers(itemsByForecastDateMap, testProfile, latitude,
            longitude);

        return weathers.stream().map(weatherMapper::toDto).toList();
    }

    //  엔티티 변환 & 영속화
    private List<Weather> saveWeathers(Map<String, List<WeatherItem>> itemsByForecastDateMap,
        Profile testProfile, BigDecimal latitude, BigDecimal longitude) {
        List<Weather> weathers = new ArrayList<>();
        for (Map.Entry<String, List<WeatherItem>> entry : itemsByForecastDateMap.entrySet()) {
            Weather weather = weatherBuilder.build(testProfile, entry.getValue(), latitude,
                longitude);
            weathers.add(weather);
        }
        return weatherRepository.saveAll(weathers);
    }


    private Map<String, List<WeatherItem>> groupByForecastSlots(List<WeatherItem> weatherItems) {
        log.debug("[Weather] 그룹핑 시작");

        Set<String> TARGET_TIMES = Set.of("0600", "1500");

        return weatherItems.stream()
            .filter(it -> TARGET_TIMES.contains(it.fcstTime()))
            .peek(it -> log.trace("slot item: {}", it))
            .collect(Collectors.groupingBy(KmaResponseDto.WeatherItem::fcstDate));


    }


    private KmaResponseDto parseToKmaResponseDto(String responseJson) {
        try {
            KmaResponseDto kmaDto = mapper.readValue(responseJson, KmaResponseDto.class);
            return kmaDto;
        } catch (Exception e) {
            log.error("parse 실패");
            throw new RuntimeException();
        }
    }

    private void validateKmaData(KmaResponseDto kmaDto) {
        // FIXME : 임시. resultcode 가져와서 enum으로 셋팅해야함
        if (!kmaDto.response().header().resultCode().equals("00")) {
            throw new RuntimeException(kmaDto.response().header().resultMsg());
        }
    }


}
