package com.sprint.ootd5team.domain.recommendation.service;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.extract.extractor.WebClothesExtractor;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.engine.OutfitCombinationGenerator;
import com.sprint.ootd5team.domain.recommendation.engine.SingleItemScoringEngine;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.ScoredClothes;
import com.sprint.ootd5team.domain.recommendation.enums.Season;
import com.sprint.ootd5team.domain.recommendation.enums.SeasonSet;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationInfoMapper;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 의상 추천 서비스
 * <p>
 * 내부 알고리즘 + LLM 기반 추천 + 필터링 실패 시 랜덤 조합 추천
 * 사용자가 llm 추천 선택시 LLM 기반 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesRepository clothesRepository;
    private final ProfileRepository profileRepository;

    private final RecommendationMapper recommendationMapper;
    private final RecommendationInfoMapper recommendationInfoMapper;

    private final SingleItemScoringEngine singleItemScoringEngine;
    private final OutfitCombinationGenerator outfitCombinationGenerator;
    private final LlmRecommendationService llmRecommendationService;
    private final RecommendationFallbackService recommendationFallbackService;

    // 의상 속성 캐시 조회를 위한 의존성
    private final WebClothesExtractor webClothesExtractor;

    @Transactional(readOnly = true)
    public RecommendationDto getRecommendation(UUID weatherId, UUID userId, boolean useAi) {
        Profile profile = getProfile(userId);
        Weather weather = resolveWeather(userId, weatherId);

        // 의상 필터링
        List<ClothesFilteredDto> filtered = getFilteredClothes(userId, profile, weather);

        // 필터링 실패시 fallback
        if (filtered.isEmpty()) {
            log.warn("[RecommendationService] 필터링 결과 없음 → fallback 랜덤 추천 적용");
            return buildResult(weatherId, userId,
                recommendationFallbackService.getRandomOutfit(userId));
        }

        // 추천 방식 선택
        List<ClothesFilteredDto> selected = useAi
            ? recommendWithAi(filtered, profile, weather)
            : recommendWithAlgorithm(userId, filtered, weather);

        return buildResult(weatherId, userId, selected);
    }


    /* -------------------- 추천 로직 -------------------- */

    /**
     * llm 호출
     */
    private List<ClothesFilteredDto> recommendWithAi(
        List<ClothesFilteredDto> dtoList,
        Profile profile,
        Weather weather
    ) {
        log.debug("[RecommendationService] LLM 기반 추천 실행");

        RecommendationInfoDto info = new RecommendationInfoDto(
            recommendationInfoMapper.toWeatherInfoDto(weather),
            recommendationInfoMapper.toProfileInfoDto(profile)
        );

        List<UUID> response = llmRecommendationService.recommendOutfit(info, dtoList);

        if (response == null || response.isEmpty()) {
            log.warn("[RecommendationService] LLM 추천 결과 없음 → fallback 랜덤 추천 적용");
            return recommendationFallbackService.getRandomOutfit(profile.getUser().getId());
        }

        return dtoList.stream()
            .filter(c -> response.contains(c.clothesId()))
            .toList();
    }

    /**
     * 내부 알고리즘 호출
     */
    private List<ClothesFilteredDto> recommendWithAlgorithm(
        UUID userId,
        List<ClothesFilteredDto> dtoList,
        Weather weather
    ) {
        log.debug("[RecommendationService] 내부 알고리즘 기반 추천 실행");

        List<ScoredClothes> outfits = singleItemScoringEngine.getTopItemsByType(dtoList, weather,
            10);
        List<OutfitScore> ranked = outfitCombinationGenerator.generateWithScoring(outfits);

        if (ranked.isEmpty()) {
            log.warn("[RecommendationService] 내부 알고리즘 추천 결과 없음 → fallback 랜덤 추천 적용");
            return recommendationFallbackService.getRandomOutfit(userId);
        }

        OutfitScore selected = ranked.get(new Random().nextInt(ranked.size()));
        return selected.outfit();
    }

    /* -------------------- 필터링 로직 -------------------- */
    @Transactional(readOnly = true)
    public List<ClothesFilteredDto> getFilteredClothes(UUID userId, Profile profile,
        Weather weather) {
        return clothesRepository.findByOwner_Id(userId).stream()
            .filter(c -> matchesUserProfile(c, profile, weather))
            .map(recommendationMapper::toFilteredDto)
            .toList();
    }

    private boolean matchesUserProfile(Clothes c, Profile p, Weather w) {
        // 1. 날씨 예보 시점 기준 계절 판단
        Season forecastSeason = Season.from(w.getForecastAt());

        // 2. 온도 민감도
        double sensitivity = p.getTemperatureSensitivity();

        // 3. 필터링할 옷 속성 가져오기(계절)
        String seasonValue = extractAttribute(c, "계절");
        SeasonSet itemSeasons = SeasonSet.fromString(seasonValue);

        // 4. 필터(온도민감도에 따른 계절만)
        boolean match = itemSeasons.matches(forecastSeason, sensitivity);
        if (match) {
            log.debug(
                "[RecommendationService] filter: clothesName={}, season(userForecast={}, item={}) -> {}",
                c.getName(), forecastSeason, itemSeasons, match);
        }
        return match;
    }

    private String extractAttribute(Clothes clothes, String attributeName) {
        Map<String, ClothesAttribute> cache = webClothesExtractor.getAttributeCache();
        if (cache == null) {
            log.warn("[RecommendationService] 캐시 미초기화: {}", attributeName);
            return null;
        }

        ClothesAttribute def = cache.get(attributeName);
        if (def == null) {
            log.warn("[RecommendationService] '{}' 속성 정의 없음", attributeName);
            return null;
        }

        UUID defId = def.getId();
        return clothes.getClothesAttributeValues().stream()
            .filter(v -> v.getAttribute() != null
                && defId.equals(v.getAttribute().getId()))
            .map(ClothesAttributeValue::getDefValue)
            .filter(s -> s != null && !s.isBlank())
            .findFirst()
            .orElse(null);
    }

    /* -------------------- 유틸 -------------------- */
    private Profile getProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> ProfileNotFoundException.withUserId(userId));
    }

    private Weather resolveWeather(UUID userId, UUID weatherId) {
        userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));

        Profile profile = getProfile(userId);

        if (profile.getLocation() != null) {
            return weatherRepository.findFirstByLocationIdOrderByForecastedAtDesc(
                    profile.getLocation().getId())
                .orElseGet(() -> getWeatherOrThrow(weatherId));
        }
        return getWeatherOrThrow(weatherId);
    }

    private Weather getWeatherOrThrow(UUID weatherId) {
        return weatherRepository.findById(weatherId)
            .orElseThrow(() -> new WeatherNotFoundException(weatherId.toString()));
    }

    private RecommendationDto buildResult(UUID weatherId, UUID userId,
        List<ClothesFilteredDto> clothes) {
        List<RecommendationClothesDto> converted = clothes.stream()
            .map(recommendationMapper::toDto)
            .toList();

        return RecommendationDto.builder()
            .weatherId(weatherId)
            .userId(userId)
            .clothes(converted)
            .build();
    }
}