package com.sprint.ootd5team.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.feed.service.FeedServiceImpl;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherSummaryDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedService 슬라이스 테스트")
@ActiveProfiles("test")
public class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedClothesRepository feedClothesRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private ClothesRepository clothesRepository;

    @InjectMocks
    private FeedServiceImpl feedService;

    private UUID userId;
    private FeedListRequest request;
    private AuthorDto testAuthor;
    private WeatherSummaryDto testWeather;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        request = new FeedListRequest(
            null,
            null,
            1,
            "createdAt",
            SortDirection.ASCENDING,
            null,
            SkyStatus.CLOUDY,
            PrecipitationType.NONE,
            null
        );

        testAuthor = new AuthorDto(UUID.randomUUID(), "nickname", "profileUrl");
        testWeather = new WeatherSummaryDto(
            UUID.randomUUID(),
            SkyStatus.CLOUDY,
            new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0),
            new TemperatureDto(20.0, -1.0, 18.0, 25.0)
        );
    }

    @Test
    @DisplayName("피드 목록 조회 성공")
    void getFeeds_success() {
        // given
        UUID feedId = UUID.randomUUID();
        FeedDto feedDto = new FeedDto(
            feedId,
            Instant.now(),
            Instant.now(),
            testAuthor,
            testWeather,
            List.of(),
            "content test",
            5,
            2,
            false
        );

        OotdDto ootdDto = new OotdDto(
            UUID.randomUUID(),
            "아디다스 트레이닝 팬츠",
            "https://image.url/adidasPants.jpg",
            "하의",
            List.of(new ClothesAttributeWithDefDto(
                UUID.randomUUID(),
                "색상",
                List.of("빨강", "파랑", "초록"),
                "초록"
            ))
        );

        when(feedRepository.findFeedDtos(request, userId))
            .thenReturn(List.of(feedDto));
        when(feedRepository.countFeeds(any(), any(), any(), any()))
            .thenReturn(10L);
        when(feedClothesRepository.findOotdsByFeedIds(anyList()))
            .thenReturn(Map.of(feedId, List.of(ootdDto)));

        // when
        FeedDtoCursorResponse response = feedService.getFeeds(request, userId);

        // then
        assertThat(response.data()).hasSize(1);
        assertThat(response.totalCount()).isEqualTo(10L);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.data().get(0).ootds()).hasSize(1);

        verify(feedRepository, times(1)).findFeedDtos(request, userId);
        verify(feedRepository, times(1)).countFeeds(any(), any(), any(), any());
        verify(feedClothesRepository, times(1)).findOotdsByFeedIds(anyList());
    }

    @Test
    @DisplayName("limit + 1개의 피드를 조회했을 경우 hasNext가 true가 된다")
    void getFeeds_hasNext_true() {
        // given
        UUID feedId1 = UUID.randomUUID();
        UUID feedId2 = UUID.randomUUID();

        FeedDto feed1 = new FeedDto(
            feedId1, Instant.now().minusSeconds(3600), Instant.now().minusSeconds(3600),
            testAuthor, testWeather, List.of(),
            "내용1", 1, 0, false
        );

        FeedDto feed2 = new FeedDto(
            feedId2, Instant.now(), Instant.now(),
            testAuthor, testWeather, List.of(),
            "내용2", 2, 0, false
        );

        when(feedRepository.findFeedDtos(request, userId))
            .thenReturn(List.of(feed1, feed2));
        when(feedRepository.countFeeds(any(), any(), any(), any()))
            .thenReturn(20L);

        // when
        FeedDtoCursorResponse response = feedService.getFeeds(request, userId);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(feed1.createdAt().toString());
        assertThat(response.nextIdAfter()).isEqualTo(feed1.id());
    }

    @Test
    @DisplayName("피드 삭제 성공")
    void deleteFeed_success() {
        // given
        UUID feedId = UUID.randomUUID();
        Feed feed = createFeed(feedId, userId, UUID.randomUUID(), "테스트 피드");

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // when
        feedService.delete(feedId);

        // then
        verify(feedRepository).findById(feedId);
        verify(feedRepository).delete(feed);
    }

    @Test
    @DisplayName("피드 삭제 실패 - 존재하지 않는 feedId")
    void deleteFeed_notFound() {
        // given
        UUID feedId = UUID.randomUUID();

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.delete(feedId))
            .isInstanceOf(FeedNotFoundException.class)
            .satisfies(ex -> {
                FeedNotFoundException fnf = (FeedNotFoundException) ex;
                assertThat(fnf.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_FOUND);
                assertThat(fnf.getDetails()).containsEntry("feedId", feedId);
            });

        verify(feedRepository).findById(feedId);
        verify(feedRepository, never()).delete(any());
    }

    @Test
    @DisplayName("피드 수정 성공")
    void updateFeed_success() {
        // given
        UUID feedId = UUID.randomUUID();
        Feed feed = createFeed(feedId, userId, UUID.randomUUID(), "수정 전 내용");
        FeedUpdateRequest request = new FeedUpdateRequest("수정된 내용");

        FeedDto updatedDto = new FeedDto(
            feedId,
            Instant.now(),
            Instant.now(),
            testAuthor,
            testWeather,
            List.of(),
            "수정된 내용",
            10,
            2,
            true
        );

        OotdDto ootd = new OotdDto(
            UUID.randomUUID(),
            "나이키 반팔 티셔츠",
            "https://image.url/nikeShirt.jpg",
            "상의",
            List.of()
        );

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedRepository.findFeedDtoById(feedId, userId)).thenReturn(updatedDto);
        when(feedClothesRepository.findOotdsByFeedIds(List.of(feedId)))
            .thenReturn(Map.of(feedId, List.of(ootd)));

        // when
        FeedDto result = feedService.update(feedId, request, userId);

        // then
        assertThat(feed.getContent()).isEqualTo("수정된 내용");
        assertThat(result.content()).isEqualTo("수정된 내용");
        assertThat(result.ootds()).hasSize(1);

        verify(feedRepository, times(1)).findById(feedId);
        verify(feedRepository, times(1)).findFeedDtoById(feedId, userId);
        verify(feedClothesRepository, times(1)).findOotdsByFeedIds(List.of(feedId));
    }

    @Test
    @DisplayName("피드 수정 실패 - 존재하지 않는 feedId")
    void updateFeed_notFound() {
        // given
        UUID feedId = UUID.randomUUID();
        FeedUpdateRequest request = new FeedUpdateRequest("수정된 내용");

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.update(feedId, request, userId))
            .isInstanceOf(FeedNotFoundException.class)
            .satisfies(ex -> {
                FeedNotFoundException fnf = (FeedNotFoundException) ex;
                assertThat(fnf.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_FOUND);
                assertThat(fnf.getDetails()).containsEntry("feedId", feedId);
            });

        verify(feedRepository).findById(feedId);
        verify(feedRepository, never()).findFeedDtoById(any(), any());
        verify(feedClothesRepository, never()).findOotdsByFeedIds(anyList());
    }

    @Test
    @DisplayName("피드 생성 성공")
    void createFeed_success() {
        // given
        UUID authorId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        UUID clothesId1 = UUID.randomUUID();
        UUID clothesId2 = UUID.randomUUID();

        FeedCreateRequest request = new FeedCreateRequest(
            authorId,
            weatherId,
            Set.of(clothesId1, clothesId2),
            "오늘의 피드"
        );

        when(profileRepository.existsByUserId(authorId)).thenReturn(true);
        when(weatherRepository.existsById(weatherId)).thenReturn(true);

        Clothes clothes1 = createClothes(clothesId1, "상의", ClothesType.TOP);
        Clothes clothes2 = createClothes(clothesId2, "하의", ClothesType.BOTTOM);

        when(clothesRepository.findAllById(Set.of(clothesId1, clothesId2)))
            .thenReturn(List.of(clothes1, clothes2));

        UUID feedId = UUID.randomUUID();
        Feed feed = createFeed(feedId, authorId, weatherId, request.content());

        when(feedRepository.save(any())).thenAnswer(invocation -> {
            Feed saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", feedId);
            return saved;
        });

        FeedDto projectedDto = new FeedDto(
            feed.getId(),
            Instant.now(),
            Instant.now(),
            testAuthor,
            testWeather,
            List.of(),
            "오늘의 피드",
            0L,
            0L,
            false
        );
        when(feedRepository.findFeedDtoById(feed.getId(), authorId)).thenReturn(projectedDto);

        OotdDto ootd1 = new OotdDto(
            clothes1.getId(),
            clothes1.getName(),
            clothes1.getImageUrl(),
            clothes1.getType().name(),
            List.of()
        );

        OotdDto ootd2 = new OotdDto(
            clothes2.getId(),
            clothes2.getName(),
            clothes2.getImageUrl(),
            clothes2.getType().name(),
            List.of()
        );
        when(feedClothesRepository.findOotdsByFeedIds(List.of(feed.getId())))
            .thenReturn(Map.of(feed.getId(), List.of(ootd1, ootd2)));

        // when
        FeedDto result = feedService.create(request, authorId);

        // then
        assertThat(result.id()).isEqualTo(feedId);
        assertThat(result.ootds()).hasSize(2);

        verify(feedRepository).save(any(Feed.class));
        verify(feedClothesRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("피드 생성 실패 - 존재하지 않는 프로필")
    void createFeed_fail_profileNotFound() {
        // given
        UUID authorId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        FeedCreateRequest request = new FeedCreateRequest(
            authorId,
            weatherId,
            Set.of(UUID.randomUUID()),
            "내용"
        );

        when(profileRepository.existsByUserId(authorId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> feedService.create(request, authorId))
            .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    @DisplayName("피드 생성 실패 - 일부 옷 ID 없음")
    void createFeed_fail_clothesNotFound() {
        // given
        UUID authorId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        UUID clothesId1 = UUID.randomUUID();
        UUID clothesId2 = UUID.randomUUID();

        FeedCreateRequest request = new FeedCreateRequest(
            authorId,
            weatherId,
            Set.of(clothesId1, clothesId2),
            "내용"
        );

        when(profileRepository.existsByUserId(authorId)).thenReturn(true);
        when(weatherRepository.existsById(weatherId)).thenReturn(true);

        Clothes clothes1 = createClothes(clothesId1, "상의", ClothesType.TOP);
        when(clothesRepository.findAllById(anySet())).thenReturn(List.of(clothes1));

        // when & then
        assertThatThrownBy(() -> feedService.create(request, authorId))
            .isInstanceOf(ClothesNotFoundException.class);
    }

    private Clothes createClothes(UUID id, String name, ClothesType type) {
        User dummyOwner = mock(User.class);
        Clothes clothes = Clothes.builder()
            .owner(dummyOwner)
            .name(name)
            .type(type)
            .imageUrl(name + ".png")
            .build();
        ReflectionTestUtils.setField(clothes, "id", id);
        return clothes;
    }

    private Feed createFeed(UUID id, UUID authorId, UUID weatherId, String content) {
        Feed feed = Feed.of(authorId, weatherId, content);
        ReflectionTestUtils.setField(feed, "id", id);
        return feed;
    }
}