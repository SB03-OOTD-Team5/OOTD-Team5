package com.sprint.ootd5team.domain.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.util.CoordinateUtils;
import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.location.exception.LocationKakaoFetchException;
import com.sprint.ootd5team.domain.location.mapper.LocationMapper;
import com.sprint.ootd5team.domain.location.repository.LocationRepository;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.external.kakao.KakaoResponseDto;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class LocationServiceImpl implements LocationService, LocationQueryService {

    private final WebClient kakaoWebClient;
    private final LocationRepository locationRepository;
    private final ObjectMapper mapper;
    private final LocationMapper locationMapper;
    private final ProfileRepository profileRepository;

    public LocationServiceImpl(@Qualifier("kakaoWebClient") WebClient kakaoWebClient,
        LocationRepository locationRepository, LocationMapper locationMapper,
        ProfileRepository profileRepository) {
        this.kakaoWebClient = kakaoWebClient;
        this.locationRepository = locationRepository;
        this.mapper = new ObjectMapper();
        this.locationMapper = locationMapper;
        this.profileRepository = profileRepository;

    }

    @Override
    @Transactional
    public WeatherAPILocationDto fetchLocation(BigDecimal latitude, BigDecimal longitude,
        UUID userId) {
        log.debug(" userId: {}", userId);
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(ProfileNotFoundException::new);

        Location location = findOrCreateLocation(latitude, longitude);
        // 프로필 위치 업데이트
        profile.relocate(location.getLatitude(), location.getLongitude(),
            location.getLocationNames());

        return locationMapper.toDto(location, new ClientCoords(latitude, longitude));
    }

    /* 날씨 데이터 요청 시 지명만 전달 */
    @Transactional
    @Override
    public String getLocationNames(BigDecimal latitude, BigDecimal longitude) {
        Location location = findOrCreateLocation(latitude, longitude);
        return location.getLocationNames();
    }


    private Location findOrCreateLocation(BigDecimal latitude, BigDecimal longitude) {
        //0. 이미 존재하는 데이터면 가져와서 전달
        Location cached = getLocationIfExist(latitude, longitude);
        log.debug("[location] 해당 데이터 존재 유무: {}", cached != null);
        if (cached != null) {
            return cached;
        }
        //1. kakao api 데이터 조회 & validation 체크
        KakaoResponseDto dto = parseToKakaoResponseDto(fetchKakaoApi(latitude, longitude));
        //2. 영속화
        return saveLocation(dto, latitude, longitude);
    }

    private Location getLocationIfExist(BigDecimal latitude,
        BigDecimal longitude) {
        log.debug("이미 존재하는 location 데이터 확인 - latitude:{},longitude:{}", latitude, longitude);
        return locationRepository.findByLatitudeAndLongitude(CoordinateUtils.toNumeric(latitude),
            CoordinateUtils.toNumeric(longitude)).orElse(null);

    }

    private Location saveLocation(KakaoResponseDto kakaoResponseDto, BigDecimal latitude,
        BigDecimal longitude) {
        log.debug("[Location] 위치 정보 영속화");
        //행정동 데이터 가져옴
        KakaoResponseDto.Document locationInfo = kakaoResponseDto.documents().stream()
            .filter((item) -> item.regionType().equals("H")).findFirst()
            .orElseThrow(() -> new LocationKakaoFetchException("해당하는 행정동 데이터가 없습니다."));

        Location location = Location.builder()
            .latitude(CoordinateUtils.toNumeric(latitude))
            .longitude(CoordinateUtils.toNumeric(longitude))
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

            String response = kakaoWebClient.get()
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


}
