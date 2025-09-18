package com.sprint.ootd5team.domain.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.exception.LocationKakaoFetchException;
import com.sprint.ootd5team.domain.location.mapper.LocationMapper;
import com.sprint.ootd5team.domain.location.repository.LocationRepository;
import com.sprint.ootd5team.domain.weather.external.kakao.KakaoResponseDto;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    private final WebClient kakaoApiClient;
    private final LocationRepository locationRepository;
    private final ObjectMapper mapper;
    private final LocationMapper locationMapper;

    public LocationServiceImpl(@Qualifier("kakaoApiClient") WebClient kakaoApiClient,
        LocationRepository locationRepository, LocationMapper locationMapper) {
        this.kakaoApiClient = kakaoApiClient;
        this.locationRepository = locationRepository;
        this.mapper = new ObjectMapper();
        this.locationMapper = locationMapper;
    }

    @Override
    public WeatherAPILocationDto fetchLocation(BigDecimal latitude, BigDecimal longitude) {

        //0. 이미 존재하는 데이터면 가져와서 전달 후 종료
        WeatherAPILocationDto existed = getLocationDtoIfExist(latitude, longitude);
        if (existed != null) {
            return existed;
        }
        //1. kakao api 데이터 조회 & validation 체크
        String response = fetchKmaApi(latitude, longitude);
        KakaoResponseDto kakaoResponseDto = parseToKakaoResponseDto(response);
        //2. 엔티티 변환 & 영속화
        Location location = saveLocation(kakaoResponseDto, latitude, longitude);

        return locationMapper.toDto(location);

    }

    private Location saveLocation(KakaoResponseDto kakaoResponseDto, BigDecimal latitude,
        BigDecimal longitude) {
        //행정동 데이터 가져옴
        KakaoResponseDto.Document locationInfo = kakaoResponseDto.documents().stream()
            .filter((item) -> item.regionType().equals("H")).findFirst()
            .orElseThrow(() -> new LocationKakaoFetchException("해당하는 행정동 데이터가 없습니다."));

        return Location.builder()
            .latitude(locationInfo.x())
            .longitude(locationInfo.y())
            .locationNames(locationInfo.addressName())
            .build();
    }

    private KakaoResponseDto parseToKakaoResponseDto(String responseJson) {
        try {
            JsonNode root = mapper.readTree(responseJson);
            // 정상
            if (root.has("meta")) {
                return mapper.treeToValue(root, KakaoResponseDto.class);
            }
            // 에러
            if (root.has("code")) {
                // 에러 응답 케이스
                String code = root.get("code").asText(null);
                String msg = root.get("msg").asText(null);

                LocationKakaoFetchException ex = new LocationKakaoFetchException();
                ex.addDetail("code", code);
                ex.addDetail("msg", msg);
                throw ex;
            }
            throw new LocationKakaoFetchException("정의되지않는 카카오 API 응답입니다.");
        } catch (Exception e) {
            throw new LocationKakaoFetchException();
        }
    }

    private String fetchKmaApi(BigDecimal latitude, BigDecimal longitude) {
        try {
            log.debug("[Location] 위치 정보 조회 요청 longitude:{},latitude:{}", longitude, latitude);

            String response = kakaoApiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // TODO:  타임아웃,retrieve 설정

            return response;
        } catch (Exception e) {
            throw new LocationKakaoFetchException();
        }
    }

    private WeatherAPILocationDto getLocationDtoIfExist(BigDecimal latitude,
        BigDecimal longitude) {
        return locationRepository.findByLatitudeAndLongitude(latitude, longitude)
            .map(locationMapper::toDto).orElse(null);

    }
}
