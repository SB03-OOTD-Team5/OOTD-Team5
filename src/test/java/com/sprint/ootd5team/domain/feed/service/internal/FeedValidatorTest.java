package com.sprint.ootd5team.domain.feed.service.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("FeedValidator 슬라이스 테스트")
public class FeedValidatorTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private ClothesRepository clothesRepository;

    @InjectMocks
    private FeedValidator feedValidator;

    private UUID feedId;
    private UUID authorId;
    private UUID weatherId;

    @BeforeEach
    void setUp() {
        feedId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        weatherId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getFeedOrThrow() - 존재하는 feedId일 경우 정상 반환")
    void getFeedOrThrow_success() {
        // given
        Feed feed = Feed.of(authorId, weatherId, "content");
        given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));

        // when
        Feed result = feedValidator.getFeedOrThrow(feedId);

        // then
        verify(feedRepository).findById(feedId);
        assertThat(result).isEqualTo(feed);
    }

    @Test
    @DisplayName("getFeedOrThrow() - 존재하지 않는 feedId일 경우 예외 발생")
    void getFeedOrThrow_notFound() {
        // given
        given(feedRepository.findById(feedId)).willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> feedValidator.getFeedOrThrow(feedId))
            .isInstanceOf(FeedNotFoundException.class);

        verify(feedRepository).findById(feedId);
    }

    @Test
    @DisplayName("validateAuthorAndWeather() - 프로필 없음 예외 발생")
    void validateAuthorAndWeather_profileNotFound() {
        // given
        given(profileRepository.existsByUserId(authorId)).willReturn(false);

        // expect
        assertThatThrownBy(() -> feedValidator.validateAuthorAndWeather(authorId, weatherId))
            .isInstanceOf(ProfileNotFoundException.class);

        verify(profileRepository).existsByUserId(authorId);
    }

    @Test
    @DisplayName("validateAuthorAndWeather() - 날씨 데이터 없음 예외 발생")
    void validateAuthorAndWeather_weatherNotFound() {
        // given
        given(profileRepository.existsByUserId(authorId)).willReturn(true);
        given(weatherRepository.existsById(weatherId)).willReturn(false);

        // expect
        assertThatThrownBy(() -> feedValidator.validateAuthorAndWeather(authorId, weatherId))
            .isInstanceOf(WeatherNotFoundException.class);

        verify(weatherRepository).existsById(weatherId);
    }

    @Test
    @DisplayName("validateClothes() - 모든 의상 존재 시 정상 반환")
    void validateClothes_success() {
        // given
        UUID c1 = UUID.randomUUID();
        UUID c2 = UUID.randomUUID();
        Set<UUID> ids = Set.of(c1, c2);

        Clothes clothes1 = mock(Clothes.class);
        Clothes clothes2 = mock(Clothes.class);
        given(clothes1.getId()).willReturn(c1);
        given(clothes2.getId()).willReturn(c2);
        given(clothesRepository.findAllById(ids)).willReturn(List.of(clothes1, clothes2));

        // when
        List<Clothes> result = feedValidator.validateClothes(ids);

        // then
        verify(clothesRepository).findAllById(ids);
        org.assertj.core.api.Assertions.assertThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder(clothes1, clothes2);
    }

    @Test
    @DisplayName("validateClothes() - 일부 의상 누락 시 예외 발생")
    void validateClothes_missingIds() {
        // given
        UUID c1 = UUID.randomUUID();
        UUID c2 = UUID.randomUUID();
        Set<UUID> ids = Set.of(c1, c2);

        Clothes clothes1 = mock(Clothes.class);
        given(clothes1.getId()).willReturn(c1);
        given(clothesRepository.findAllById(ids)).willReturn(List.of(clothes1));

        // when & then
        assertThatThrownBy(() -> feedValidator.validateClothes(ids))
            .isInstanceOf(ClothesNotFoundException.class);

        verify(clothesRepository).findAllById(ids);
    }
}