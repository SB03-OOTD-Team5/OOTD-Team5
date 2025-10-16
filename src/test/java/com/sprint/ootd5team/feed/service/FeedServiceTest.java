package com.sprint.ootd5team.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.feed.assembler.FeedDtoAssembler;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.FeedSearchResult;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.event.producer.FeedEventProducer;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.feed.search.FeedSearchService;
import com.sprint.ootd5team.domain.feed.service.FeedServiceImpl;
import com.sprint.ootd5team.domain.feed.validator.FeedValidator;
import com.sprint.ootd5team.domain.follow.repository.FollowRepository;
import com.sprint.ootd5team.domain.notification.event.type.multi.FeedCreatedEvent;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherSummaryDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
    private FeedDtoAssembler feedDtoAssembler;

    @Mock
    private FeedValidator feedValidator;

    @Mock
    private FeedEventProducer feedEventProducer;

    @Mock
    private FeedSearchService feedSearchService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private FeedServiceImpl feedService;
    private UUID userId;
    private FeedListRequest request;
    private AuthorDto author;
    private WeatherSummaryDto weather;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        request = new FeedListRequest(
            null, null, 1, "createdAt",
            SortDirection.ASCENDING, null, SkyStatus.CLOUDY, PrecipitationType.NONE, null
        );

        author = new AuthorDto(UUID.randomUUID(), "nickname", "profileUrl");
        weather = new WeatherSummaryDto(
            UUID.randomUUID(),
            SkyStatus.CLOUDY,
            new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0),
            new TemperatureDto(20.0, -1.0, 18.0, 25.0)
        );

        feedService = new FeedServiceImpl(
            feedRepository,
            feedClothesRepository,
            feedDtoAssembler,
            feedSearchService,
            feedValidator,
            feedEventProducer,
            eventPublisher,
            followRepository
        );
    }

    @Test
    @DisplayName("피드 목록 조회 성공")
    void getFeeds_success() {
        // given
        UUID feedId = UUID.randomUUID();

        FeedDto raw = dummyFeedDto(feedId);

        when(feedRepository.findFeedDtos(request, userId))
            .thenReturn(List.of(raw));
        when(feedRepository.countFeeds(any(), any(), any(), any()))
            .thenReturn(10L);
        when(feedDtoAssembler.enrich(List.of(raw)))
            .thenReturn(List.of(raw));

        // when
        FeedDtoCursorResponse feedDtoCursorResponse = feedService.getFeeds(request, userId);

        // then
        assertThat(feedDtoCursorResponse.data()).hasSize(1);
        assertThat(feedDtoCursorResponse.totalCount()).isEqualTo(10L);
        assertThat(feedDtoCursorResponse.hasNext()).isFalse();

        verify(feedRepository).findFeedDtos(request, userId);
        verify(feedDtoAssembler).enrich(List.of(raw));
    }

    @Test
    @DisplayName("키워드 검색 요청 시 FeedSearchService가 호출됨")
    void getFeeds_withKeyword_callsElasticsearch() {
        // given
        FeedListRequest feedListRequest = new FeedListRequest(
            null, null, 5, "createdAt", SortDirection.DESCENDING, "코디", null, null, null
        );
        FeedSearchResult feedSearchResult = new FeedSearchResult(
            List.of(UUID.randomUUID()), "cursor", UUID.randomUUID(), false, 10
        );

        when(feedSearchService.searchByKeyword(feedListRequest)).thenReturn(feedSearchResult);
        when(feedRepository.findFeedDtosByIds(eq(feedListRequest), anyList(), any())).thenReturn(List.of(mock(FeedDto.class)));
        when(feedDtoAssembler.enrich(anyList())).thenReturn(List.of(mock(FeedDto.class)));

        // when
        feedService.getFeeds(feedListRequest, userId);

        // then
        verify(feedSearchService).searchByKeyword(feedListRequest);
    }

    @Test
    @DisplayName("limit + 1개의 피드를 조회했을 경우 hasNext가 true가 된다")
    void getFeeds_hasNext_true() {
        // given
        UUID feedId1 = UUID.randomUUID();
        UUID feedId2 = UUID.randomUUID();

        FeedDto feed1 = new FeedDto(
            feedId1, Instant.now().minusSeconds(3600), Instant.now().minusSeconds(3600),
            author, weather, List.of(),
            "내용1", 1, 0, false
        );

        FeedDto feed2 = new FeedDto(
            feedId2, Instant.now(), Instant.now(),
            author, weather, List.of(),
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
    @DisplayName("피드 단건 조회 성공")
    void getFeed_success() {
        // given
        UUID feedId = UUID.randomUUID();
        Feed feed = createFeed(feedId);
        FeedDto mockDto = mock(FeedDto.class);

        when(feedValidator.getFeedOrThrow(feedId)).thenReturn(feed);
        when(feedRepository.findFeedDtoById(feedId, userId)).thenReturn(mockDto);
        when(feedDtoAssembler.enrich(List.of(mockDto))).thenReturn(List.of(mockDto));

        // when
        FeedDto result = feedService.getFeed(feedId, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockDto);

        verify(feedValidator).getFeedOrThrow(feedId);
        verify(feedRepository).findFeedDtoById(feedId, userId);
        verify(feedDtoAssembler).enrich(List.of(mockDto));
    }

    @Test
    @DisplayName("존재하지 않는 피드 조회 시 예외 발생")
    void getFeed_notFound() {
        // given
        UUID feedId = UUID.randomUUID();

        when(feedValidator.getFeedOrThrow(feedId))
            .thenThrow(FeedNotFoundException.withId(feedId));

        // when & then
        assertThatThrownBy(() -> feedService.getFeed(feedId, userId))
            .isInstanceOf(FeedNotFoundException.class);

        verify(feedValidator).getFeedOrThrow(feedId);
        verify(feedRepository, never()).findFeedDtoById(any(), any());
    }

    @Test
    @DisplayName("피드 생성 성공 시 FeedCreatedEvent, FeedIndexCreatedEvent 발행")
    void createFeed_success() {
        // given
        UUID authorId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        Set<UUID> clothesIds = Set.of(UUID.randomUUID());

        FeedCreateRequest feedCreateRequest = new FeedCreateRequest(authorId, weatherId, clothesIds, "test content");
        Feed feed = createFeed(UUID.randomUUID());
        FeedDto feedDto = dummyFeedDto(feed.getId());
        List<Clothes> clothesList = List.of(mock(Clothes.class));

        doNothing().when(feedValidator).validateAuthorAndWeather(authorId, weatherId);
        when(feedValidator.validateClothes(clothesIds))
            .thenReturn(clothesList);
        when(feedRepository.save(any(Feed.class)))
            .thenReturn(feed);
        when(feedRepository.findFeedDtoById(any(), any()))
            .thenReturn(feedDto);
        when(feedDtoAssembler.enrich(anyList()))
            .thenReturn(List.of(feedDto));
        when(followRepository.findFollowerIds(any()))
            .thenReturn(List.of(UUID.randomUUID()));

        // when
        FeedDto result = feedService.create(feedCreateRequest, authorId);

        // then
        assertThat(result).isNotNull();
        verify(feedRepository).save(any(Feed.class));
        verify(feedClothesRepository).saveAll(anyList());
        verify(feedEventProducer).publishFeedIndexCreatedEvent(any());
        verify(eventPublisher).publishEvent(any(FeedCreatedEvent.class));
    }

    @Test
    @DisplayName("피드 생성 실패 - 존재하지 않는 프로필")
    void createFeed_fail_profileNotFound() {
        // given
        UUID authorId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        FeedCreateRequest request = new FeedCreateRequest(
            authorId, weatherId, Set.of(UUID.randomUUID()), "내용"
        );

        doThrow(ProfileNotFoundException.withUserId(authorId))
            .when(feedValidator)
            .validateAuthorAndWeather(authorId, weatherId);

        // when & then
        assertThatThrownBy(() -> feedService.create(request, authorId))
            .isInstanceOf(ProfileNotFoundException.class);

        verify(feedValidator).validateAuthorAndWeather(authorId, weatherId);
        verify(feedValidator, never()).validateClothes(any());
        verify(feedRepository, never()).save(any());
        verify(feedEventProducer, never()).publishFeedIndexCreatedEvent(any());
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

        doNothing().when(feedValidator).validateAuthorAndWeather(authorId, weatherId);
        doThrow(ClothesNotFoundException.withIds(Set.of(clothesId2)))
            .when(feedValidator)
            .validateClothes(Set.of(clothesId1, clothesId2));

        // when & then
        assertThatThrownBy(() -> feedService.create(request, authorId))
            .isInstanceOf(ClothesNotFoundException.class);

        verify(feedValidator).validateAuthorAndWeather(authorId, weatherId);
        verify(feedValidator).validateClothes(Set.of(clothesId1, clothesId2));
        verify(feedRepository, never()).save(any());
        verify(feedEventProducer, never()).publishFeedIndexCreatedEvent(any());
    }

    @Test
    @DisplayName("피드 수정 시 FeedContentUpdatedEvent 발행")
    void updateFeed_success() {
        // given
        UUID feedId = UUID.randomUUID();
        Feed feed = createFeed(feedId);

        when(feedValidator.getFeedOrThrow(feedId)).thenReturn(feed);
        when(feedRepository.findFeedDtoById(feedId, userId)).thenReturn(mock(FeedDto.class));
        when(feedDtoAssembler.enrich(anyList())).thenReturn(List.of(mock(FeedDto.class)));

        feedService.update(feedId, new FeedUpdateRequest("new content"), userId);

        verify(feedEventProducer).publishFeedContentUpdatedEvent(any());
    }

    @Test
    @DisplayName("피드 수정 실패 - 존재하지 않는 feedId")
    void updateFeed_notFound() {
        // given
        UUID feedId = UUID.randomUUID();

        when(feedValidator.getFeedOrThrow(feedId))
            .thenThrow(FeedNotFoundException.withId(feedId));

        // when & then
        assertThatThrownBy(() -> feedService.update(feedId, new FeedUpdateRequest("내용"), userId))
            .isInstanceOf(FeedNotFoundException.class);

        verify(feedValidator).getFeedOrThrow(feedId);
        verify(feedRepository, never()).findFeedDtoById(any(), any());
        verify(feedDtoAssembler, never()).enrich(anyList());
        verify(feedEventProducer, never()).publishFeedContentUpdatedEvent(any());
    }

    @Test
    @DisplayName("피드 삭제 성공")
    void deleteFeed_success() {
        // given
        UUID feedId = UUID.randomUUID();
        Feed feed = createFeed(feedId);

        when(feedValidator.getFeedOrThrow(feedId)).thenReturn(feed);

        // when
        feedService.delete(feedId);

        // then
        verify(feedRepository).delete(feed);
        verify(feedEventProducer).publishFeedDeletedEvent(any());
    }

    @Test
    @DisplayName("피드 삭제 실패 - 존재하지 않는 feedId")
    void deleteFeed_notFound() {
        // given
        UUID feedId = UUID.randomUUID();

        when(feedValidator.getFeedOrThrow(feedId))
            .thenThrow(FeedNotFoundException.withId(feedId));

        // when & then
        assertThatThrownBy(() -> feedService.delete(feedId))
            .isInstanceOf(FeedNotFoundException.class);

        verify(feedValidator).getFeedOrThrow(feedId);
        verify(feedRepository, never()).delete(any());
        verify(feedEventProducer, never()).publishFeedDeletedEvent(any());
    }

    private Feed createFeed(UUID id) {
        Feed feed = Feed.of(UUID.randomUUID(), UUID.randomUUID(), "content");
        ReflectionTestUtils.setField(feed, "id", id);
        return feed;
    }

    private FeedDto dummyFeedDto(UUID id) {
        return new FeedDto(
            id, Instant.now(), Instant.now(),
            author, weather, List.of(), "test", 1, 0, false
        );
    }
}