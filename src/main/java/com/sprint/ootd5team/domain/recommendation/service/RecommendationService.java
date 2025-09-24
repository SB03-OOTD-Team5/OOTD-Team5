package com.sprint.ootd5team.domain.recommendation.service;

import com.sprint.ootd5team.base.entity.BaseEntity;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int MAX_RECOMMEND_CNT = 4;
    private final WeatherRepository weatherRepository;
    private final ClothesRepository clothesRepository;
    private final RecommendationMapper recommendationMapper;

    public RecommendationDto getRecommendation(UUID weatherId, UUID userId) {

        log.info("[RecommendationController] 추천 조회 시작: weatherId={}, userId={} ", weatherId,
            userId);

        // 해당 weather 정보 가져옴
        Weather weather = weatherRepository.findById(weatherId).orElseThrow(
            WeatherNotFoundException::new);
        log.info("weather.getPrecipitationType():{}, weather.getSkyStatus():{}",
            weather.getPrecipitationType(), weather.getSkyStatus());
        // 임시 추천 로직) 해당 weather의 강수타입 or 하늘 흐림 정도를 기준으로 추천 옷 선별
        List<Weather> candidate = weatherRepository.findAllByPrecipitationTypeEqualsOrSkyStatusEquals(
            weather.getPrecipitationType(), weather.getSkyStatus());

        List<UUID> candiateIds = candidate.stream().map(BaseEntity::getId).toList();

        List<Clothes> clothesList = clothesRepository.findClothesInWeatherIds(
            candiateIds);

        // 추첨된 옷 중 limit에 맞게 랜덤으로 옷을 가져옴
        List<Clothes> selectedClothesList;
        if (clothesList.size() > MAX_RECOMMEND_CNT) {
            selectedClothesList = getRandomClothes(clothesList, MAX_RECOMMEND_CNT);
        } else {

            /* 추첨된 옷이 limit 보다 작으면 DB(4배수)에서 가져옴
             - 추첨된 옷이 limit과 같으면 반만 랜덤으로 가져옴 (`다른옷 추천 기능` 때 새로운 옷 보여주기위해)
            * */
            int remain = clothesList.size() == MAX_RECOMMEND_CNT ? MAX_RECOMMEND_CNT / 2
                : MAX_RECOMMEND_CNT - clothesList.size();

            selectedClothesList = clothesList.subList(0, clothesList.size() - remain);
            List<UUID> includedIds = selectedClothesList.stream().map(BaseEntity::getId).toList();

            List<Clothes> fallbackClothes = clothesRepository.findByIdNotIn(includedIds,
                Limit.of(remain * 4));
            selectedClothesList = getRandomClothes(fallbackClothes, remain);
        }

        List<RecommendationClothesDto> list = selectedClothesList.stream()
            .map(recommendationMapper::toDto).toList();

        // 총 MAX_RECOMMEND_CNT 개의 옷 전달
        return RecommendationDto.builder()
            .clothes(list)
            .userId(userId)
            .weatherId(weatherId)
            .build();

    }

    private List<Clothes> getRandomClothes(List<Clothes> clothesList, int limit) {
        if (clothesList.isEmpty() || limit <= 0) {
            return List.of();
        }

        List<Clothes> shuffled = new ArrayList<>(clothesList);
        Collections.shuffle(shuffled);

        if (shuffled.size() <= limit) {
            return shuffled;
        }

        return new ArrayList<>(shuffled.subList(0, limit));
    }
}
