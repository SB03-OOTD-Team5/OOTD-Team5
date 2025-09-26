package com.sprint.ootd5team.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationMapper;
import com.sprint.ootd5team.domain.recommendation.service.RecommendationService;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService 슬라이스 테스트")
@ActiveProfiles("test")
class RecommendationServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private ClothesRepository clothesRepository;

    @Mock
    private RecommendationMapper recommendationMapper;

    @InjectMocks
    private RecommendationService recommendationService;

    private UUID weatherId;
    private UUID userId;
    private Weather weather;
    private List<Weather> weatherCandidates;

    @BeforeEach
    void setUp() {
        weatherId = UUID.randomUUID();
        userId = UUID.randomUUID();
        User user = new User("테스트 프로필", "test@test.com","pw", Role.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        Location location = new Location(
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780),
            60,
            127,
            "서울특별시 중구",
            null);

        Profile profile = new Profile(
            user,
            "테스트 프로필",
            "MALE",
            LocalDate.now(),
            null,
            location,
            2
        );

        weather = Weather.builder()
            .forecastedAt(Instant.parse("2024-04-01T00:00:00Z"))
            .forecastAt(Instant.parse("2024-04-01T03:00:00Z"))
            .skyStatus(SkyStatus.CLEAR)
            .precipitationType(PrecipitationType.NONE)
            .latitude(BigDecimal.valueOf(37.5665))
            .longitude(BigDecimal.valueOf(126.9780))
            .temperature(20.0)
            .temperatureMin(18.0)
            .temperatureMax(23.0)
            .profile(profile)
            .build();

        Weather candidate = Weather.builder()
            .forecastedAt(Instant.parse("2024-04-01T00:00:00Z"))
            .forecastAt(Instant.parse("2024-04-01T06:00:00Z"))
            .skyStatus(SkyStatus.CLEAR)
            .precipitationType(PrecipitationType.NONE)
            .latitude(BigDecimal.valueOf(37.5665))
            .longitude(BigDecimal.valueOf(126.9780))
            .temperature(21.0)
            .temperatureMin(19.0)
            .temperatureMax(24.0)
            .profile(profile)
            .build();

        weatherCandidates = List.of(candidate);
    }

    @Test
    @DisplayName("추천 요청 시 날씨 조건에 맞는 의상을 조회한다")
    void 추천_요청시_날씨_조건에_맞는_의상을_조회한다() {
        // given
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
        when(weatherRepository.findAllByPrecipitationTypeEqualsOrSkyStatusEquals(
            weather.getPrecipitationType(), weather.getSkyStatus()
        )).thenReturn(weatherCandidates);
        List<Clothes> abundantClothes = createClothesList(5, "상세");
        when(clothesRepository.findClothesInWeatherIds(anyList())).thenReturn(abundantClothes);
        when(recommendationMapper.toDto(any(Clothes.class))).thenAnswer(invocation ->
            toRecommendationDto(invocation.getArgument(0))
        );

        // when
        RecommendationDto result = recommendationService.getRecommendation(weatherId, userId);

        // then
        assertThat(result.weatherId()).isEqualTo(weatherId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.clothes()).hasSize(4);
        verify(weatherRepository).findById(weatherId);
        verify(weatherRepository).findAllByPrecipitationTypeEqualsOrSkyStatusEquals(
            weather.getPrecipitationType(), weather.getSkyStatus()
        );
        verify(clothesRepository).findClothesInWeatherIds(anyList());
    }

    @Test
    @DisplayName("추천 대상이 부족하면 랜덤 의상을 추가로 선택한다")
    void 추천_대상이_부족하면_랜덤_의상을_추가로_선택한다() {
        // given
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
        when(weatherRepository.findAllByPrecipitationTypeEqualsOrSkyStatusEquals(
            weather.getPrecipitationType(), weather.getSkyStatus()
        )).thenReturn(weatherCandidates);
        List<Clothes> limitedClothes = createClothesList(2, "기본");
        when(clothesRepository.findClothesInWeatherIds(anyList())).thenReturn(limitedClothes);
        when(clothesRepository.findRandomClothes(any(Limit.class))).thenReturn(
            createClothesList(4, "랜덤")
        );
        when(recommendationMapper.toDto(any(Clothes.class))).thenAnswer(invocation ->
            toRecommendationDto(invocation.getArgument(0))
        );

        // when
        RecommendationDto result = recommendationService.getRecommendation(weatherId, userId);

        // then
        assertThat(result.clothes()).hasSize(2);
        verify(clothesRepository).findRandomClothes(any(Limit.class));
    }

    @Test
    @DisplayName("존재하지 않는 날씨이면 예외를 발생시킨다")
    void 존재하지_않는_날씨이면_예외를_발생시킨다() {
        // given
        when(weatherRepository.findById(weatherId)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> recommendationService.getRecommendation(weatherId, userId))
            .isInstanceOf(WeatherNotFoundException.class);
    }

    private List<Clothes> createClothesList(int count, String prefix) {
        User owner = new User("추천오너", prefix + "@example.com", "password", Role.USER);
        return IntStream.range(0, count)
            .mapToObj(i -> createClothes(owner, prefix + i))
            .toList();
    }

    private Clothes createClothes(User owner, String name) {
        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name(name)
            .type(ClothesType.TOP)
            .build();
        ReflectionTestUtils.setField(clothes, "id", UUID.randomUUID());
        return clothes;
    }

    private RecommendationClothesDto toRecommendationDto(Clothes clothes) {
        return new RecommendationClothesDto(
            clothes.getId(),
            "추천-" + clothes.getId(),
            null,
            null,
            List.of()
        );
    }
}
