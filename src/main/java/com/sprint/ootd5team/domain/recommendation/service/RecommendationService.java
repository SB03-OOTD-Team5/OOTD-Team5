package com.sprint.ootd5team.domain.recommendation.service;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.ProfileInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.recommendation.engine.OutfitCombinationGenerator;
import com.sprint.ootd5team.domain.recommendation.engine.SeasonFilterEngine;
import com.sprint.ootd5team.domain.recommendation.engine.SingleItemScoringEngine;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationInfoMapper;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
    private final ProfileRepository profileRepository;

    private final RecommendationMapper recommendationMapper;
    private final RecommendationInfoMapper recommendationInfoMapper;

    private final SingleItemScoringEngine singleItemScoringEngine;
    private final OutfitCombinationGenerator outfitCombinationGenerator;
    private final LlmRecommendationService llmRecommendationService;
    private final RecommendationFallbackService recommendationFallbackService;
    private final SeasonFilterEngine seasonFilterEngine;

    @Transactional(readOnly = true)
    public RecommendationDto getRecommendation(UUID weatherId, UUID userId, boolean useAi) {
        assertUserExists(userId);
        Profile profile = getProfile(userId);
        Weather weather = resolveWeather(profile, weatherId);

        ProfileInfoDto profileInfoDto = recommendationInfoMapper.toProfileInfoDto(profile);
        WeatherInfoDto weatherInfoDto = recommendationInfoMapper.toWeatherInfoDto(weather);
        RecommendationInfoDto info = recommendationInfoMapper.toDto(weatherInfoDto, profileInfoDto);

        // 의상 필터링
        List<ClothesFilteredDto> filtered = seasonFilterEngine.getFilteredClothes(userId, info);

        // 필터링 실패시 fallback
        if (filtered.isEmpty()) {
            log.warn("[RecommendationService] 필터링 결과 없음 → fallback 랜덤 추천 적용");
            return buildResult(weatherId, userId,
                recommendationFallbackService.getRandomOutfit(userId));
        }

        // 추천 방식 선택
        List<ClothesFilteredDto> selected = (useAi
            ? recommendWithAi(info, filtered)
            : recommendWithAlgorithm(info, filtered));

        if (selected.isEmpty()) {
            return buildResult(weatherId, userId,
                recommendationFallbackService.getRandomOutfit(userId));
        }

        return buildResult(weatherId, userId, selected);
    }


    /* -------------------- 추천 로직 -------------------- */

    /**
     * llm 호출
     */
    private List<ClothesFilteredDto> recommendWithAi(
        RecommendationInfoDto info,
        List<ClothesFilteredDto> candidates
    ) {
        log.debug("[RecommendationService] LLM 기반 추천 실행");

        List<UUID> response = llmRecommendationService.recommendOutfit(info, candidates);

        if (response == null || response.isEmpty()) {
            log.warn("[RecommendationService] LLM 추천 결과 없음 → fallback");
            return List.of();
        }

        List<ClothesFilteredDto> result = candidates.stream()
            .filter(c -> response.contains(c.clothesId()))
            .toList();

        if (result.isEmpty()) {
            return List.of();
        }

        return result;
    }

    /**
     * 내부 알고리즘 호출
     * - 단품 Top-N 선별 → 조합/랭킹 → 상위 조합 중 무작위 1개 선택
     */
    private List<ClothesFilteredDto> recommendWithAlgorithm(
        RecommendationInfoDto info,
        List<ClothesFilteredDto> candidates
    ) {
        log.debug("[RecommendationService] 내부 알고리즘 기반 추천 실행");

        List<ClothesScore> items = singleItemScoringEngine.getTopItemsByType(info, candidates);
        List<OutfitScore> ranked = outfitCombinationGenerator.generateWithScoring(items);

        if (ranked.isEmpty()) {
            log.warn("[RecommendationService] 내부 알고리즘 추천 결과 없음 → fallback");
            return List.of();
        }

        OutfitScore selected = ranked.get(ThreadLocalRandom.current().nextInt(ranked.size()));
        return selected.getItems().stream()
            .map(ClothesScore::item)
            .toList();
    }

    /* -------------------- 유틸 -------------------- */
    private void assertUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException.withId(userId);
        }
    }

    private Profile getProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> ProfileNotFoundException.withUserId(userId));
    }

    /**
     * - weatherId가 있으면 최우선 사용(실시간 위치값으로 간주)
     * - 없으면 프로필 위치의 최신 예보 사용
     */
    private Weather resolveWeather(Profile profile, UUID weatherId) {
        if (weatherId != null) {
            return getWeatherOrThrow(weatherId);
        }
        if (profile.getLocation() != null) {
            return weatherRepository.findFirstByLocationIdOrderByForecastedAtDesc(
                    profile.getLocation().getId())
                .orElseThrow(() -> new WeatherNotFoundException("프로필 지역에 따른 날씨 정보 없음"));
        }
        throw new WeatherNotFoundException("날씨 정보 없음");
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