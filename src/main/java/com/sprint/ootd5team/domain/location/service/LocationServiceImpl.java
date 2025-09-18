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
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public WeatherAPILocationDto fetchLocation(BigDecimal latitude, BigDecimal longitude) {

        //0. 이미 존재하는 데이터면 가져와서 전달 후 종료
        WeatherAPILocationDto existed = getLocationDtoIfExist(latitude, longitude);
        log.debug("[location] 해당 데이터 존재 유무: {}", existed != null);
        if (existed != null) {
            return existed;
        }
        //1. kakao api 데이터 조회 & validation 체크
        String response = fetchKakaoApi(latitude, longitude);
        KakaoResponseDto kakaoResponseDto = parseToKakaoResponseDto(response);
        //2. 엔티티 변환 & 영속화
        Location location = saveLocation(kakaoResponseDto, latitude, longitude);

        return locationMapper.toDto(location);
    }

    private Location saveLocation(KakaoResponseDto kakaoResponseDto, BigDecimal latitude,
        BigDecimal longitude) {
        log.debug("[Location] 위치 정보 영속화");
        //행정동 데이터 가져옴
        KakaoResponseDto.Document locationInfo = kakaoResponseDto.documents().stream()
            .filter((item) -> item.regionType().equals("H")).findFirst()
            .orElseThrow(() -> new LocationKakaoFetchException("해당하는 행정동 데이터가 없습니다."));

        Location location = Location.builder()
            .latitude(toNumeric(latitude))
            .longitude(toNumeric(longitude))
            .locationNames(locationInfo.addressName())
            .build();

        return locationRepository.save(location);
    }

    private KakaoResponseDto parseToKakaoResponseDto(String responseJson) {
        try {
            JsonNode root = mapper.readTree(responseJson);
            // 정상
            if (root.has("meta")) {
                log.debug("[Location] 위치 정보 DTO 변환 성공");
                return mapper.treeToValue(root, KakaoResponseDto.class);
            }
            // 에러
            if (root.has("code")) {
                // 에러 응답 케이스
                String code = root.get("code").asText(null);
                String msg = root.get("msg").asText(null);
                log.debug("[Location] 위치 정보 DTO 변환 실패 - 에러코드:{}, 메세지:{}", code, msg);

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

    private String fetchKakaoApi(BigDecimal latitude, BigDecimal longitude) {
        try {
            log.debug("[Location] 위치 정보 조회 요청 longitude:{},latitude:{}", longitude, latitude);

            String response = kakaoApiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .build())
                .exchangeToMono(clientResponse ->
                    clientResponse.bodyToMono(String.class)
                )
                .block(); // TODO:  타임아웃,retrieve 설정

            log.debug("[Location] 위치 정보 조회 완료:{}", response);

            return response;
        } catch (Exception e) {
            throw new LocationKakaoFetchException();
        }
    }

    private WeatherAPILocationDto getLocationDtoIfExist(BigDecimal latitude,
        BigDecimal longitude) {
        log.debug("이미 존재하는 location 데이터 확인 - latitude:{},longitude:{}", latitude, longitude);

        return locationRepository.findByLatitudeAndLongitude(toNumeric(latitude),
                toNumeric(longitude))
            .map(locationMapper::toDto).orElse(null);

    }

    //TODO: weather 도 같은 로직 사용, util로 뺄것
    // NUMERIC(8,4) → 소수점 이하 4자리, 반올림 적용
    public BigDecimal toNumeric(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}
