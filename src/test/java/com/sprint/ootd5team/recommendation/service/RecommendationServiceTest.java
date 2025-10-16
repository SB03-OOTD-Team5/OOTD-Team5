package com.sprint.ootd5team.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.recommendation.engine.OutfitCombinationGenerator;
import com.sprint.ootd5team.domain.recommendation.engine.SeasonFilterEngine;
import com.sprint.ootd5team.domain.recommendation.engine.SingleItemScoringEngine;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.engine.model.OutfitScore;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationInfoMapper;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.domain.recommendation.service.LlmRecommendationService;
import com.sprint.ootd5team.domain.recommendation.service.RecommendationFallbackService;
import com.sprint.ootd5team.domain.recommendation.service.RecommendationService;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("RecommendationService 단위 테스트")
class RecommendationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WeatherRepository weatherRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private RecommendationMapper recommendationMapper;
    @Mock private RecommendationInfoMapper recommendationInfoMapper;
    @Mock private SingleItemScoringEngine singleItemScoringEngine;
    @Mock private OutfitCombinationGenerator outfitCombinationGenerator;
    @Mock private LlmRecommendationService llmRecommendationService;
    @Mock private RecommendationFallbackService recommendationFallbackService;
    @Mock private SeasonFilterEngine seasonFilterEngine;

    @InjectMocks
    private RecommendationService recommendationService;

    private UUID userId;
    private UUID weatherId;
    private User user;
    private Profile profile;
    private Weather weather;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        weatherId = UUID.randomUUID();

        user = new User("테스트", "test@test.com", "pw", Role.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        profile = new Profile(user, "테스트 프로필", "MALE", LocalDate.of(2000, 1, 1), null, null, 0);

        weather = Weather.builder()
            .forecastedAt(Instant.parse("2025-03-01T00:00:00Z"))
            .forecastAt(Instant.parse("2025-03-01T03:00:00Z"))
            .skyStatus(SkyStatus.CLEAR)
            .precipitationType(PrecipitationType.NONE)
            .temperature(15.0)
            .temperatureMin(12.0)
            .temperatureMax(18.0)
            .build();
    }

    private ClothesFilteredDto sampleClothes() {
        return new ClothesFilteredDto(UUID.randomUUID(), "테스트 셔츠", null, ClothesType.TOP, List.of());
    }

    /* ------------------------ 테스트 시작 ------------------------ */

    @Test
    void ai추천_정상_동작() {
        // given
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(weatherRepository.findById(weatherId)).willReturn(Optional.of(weather));
        given(seasonFilterEngine.getFilteredClothes(any(), any())).willReturn(List.of(sampleClothes()));

        given(llmRecommendationService.recommendOutfit(any(), anyList()))
            .willReturn(List.of(UUID.randomUUID()));

        // when
        RecommendationDto result = recommendationService.getRecommendation(weatherId, userId, true);

        // then
        assertThat(result).isNotNull();
        then(llmRecommendationService).should().recommendOutfit(any(), anyList());
    }

    @Test
    void 알고리즘기반_추천_정상_동작() {
        // given
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(weatherRepository.findById(weatherId)).willReturn(Optional.of(weather));
        given(seasonFilterEngine.getFilteredClothes(any(), any()))
            .willReturn(List.of(sampleClothes()));

        ClothesScore score = new ClothesScore(sampleClothes(), 0.9, null, null, null);
        OutfitScore outfit = new OutfitScore(List.of(score), 0.9, List.of(), List.of(), List.of());
        given(singleItemScoringEngine.getTopItemsByType(any(), anyList())).willReturn(List.of(score));
        given(outfitCombinationGenerator.generateWithScoring(any())).willReturn(List.of(outfit));

        // when
        RecommendationDto result = recommendationService.getRecommendation(weatherId, userId, false);

        // then
        assertThat(result).isNotNull();
        then(singleItemScoringEngine).should().getTopItemsByType(any(), anyList());
        then(outfitCombinationGenerator).should().generateWithScoring(any());
    }

    @Test
    void 필터링결과없으면_랜덤추천() {
        // given
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(weatherRepository.findById(weatherId)).willReturn(Optional.of(weather));
        given(seasonFilterEngine.getFilteredClothes(any(), any())).willReturn(List.of());

        // when
        recommendationService.getRecommendation(weatherId, userId, false);

        // then
        then(recommendationFallbackService).should().getRandomOutfit(userId);
    }

    @Test
    void Ai추천결과없으면_랜덤추천() {
        // given
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(weatherRepository.findById(weatherId)).willReturn(Optional.of(weather));
        given(seasonFilterEngine.getFilteredClothes(any(), any())).willReturn(List.of(sampleClothes()));

        given(llmRecommendationService.recommendOutfit(any(), anyList()))
            .willReturn(List.of()); // 결과 없음
        given(recommendationFallbackService.getRandomOutfit(any()))
            .willReturn(List.of(sampleClothes()));

        // when
        RecommendationDto result = recommendationService.getRecommendation(weatherId, userId, true);

        // then
        assertThat(result.clothes()).hasSize(1);
        then(recommendationFallbackService).should().getRandomOutfit(any());
    }

    @Test
    void user없음_예외() {
        given(userRepository.existsById(userId)).willReturn(false);
        assertThatThrownBy(() -> recommendationService.getRecommendation(weatherId, userId, false))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void profile없음_예외() {
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.empty());
        assertThatThrownBy(() -> recommendationService.getRecommendation(weatherId, userId, false))
            .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test void 프로필_위치_미_존재시_예외() {
        // given
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

        // when & then
        assertThatThrownBy(() -> recommendationService.getRecommendation(weatherId, userId, false))
            .isInstanceOf(WeatherNotFoundException.class);
    }


    @Test void 프로필_위치기반_날씨_미_존재시_예외() {
        // given
        profile = mock(Profile.class);
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(profile.getLocation()).willReturn(mock(Location.class));
        given(weatherRepository.findFirstByLocationIdOrderByForecastedAtDesc(any()))
            .willReturn(Optional.empty()); UUID LocationWeatherId = null;

        // when & then
        assertThatThrownBy(() -> recommendationService.getRecommendation(LocationWeatherId, userId, false))
            .isInstanceOf(WeatherNotFoundException.class);
    }

    @Test
    void 날씨없음_예외() {
        given(userRepository.existsById(userId)).willReturn(true);
        given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));
        given(weatherRepository.findById(weatherId)).willReturn(Optional.empty());
        assertThatThrownBy(() -> recommendationService.getRecommendation(weatherId, userId, false))
            .isInstanceOf(WeatherNotFoundException.class);
    }
}
