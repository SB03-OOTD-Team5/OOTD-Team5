package com.sprint.ootd5team.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.exception.FeedNotFoundException;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.feed.service.FeedServiceImpl;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherSummaryDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedService 슬라이스 테스트")
@ActiveProfiles("test")
public class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedClothesRepository feedClothesRepository;

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
    @DisplayName("피드 목록을 정상적으로 조회하고 페이지네이션 정보를 반환한다")
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
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo("ASCENDING");
        assertThat(response.hasNext()).isFalse();

        FeedDto resultFeed = response.data().get(0);
        assertThat(resultFeed.ootds()).hasSize(1);
        assertThat(resultFeed.ootds().get(0).type()).isEqualTo("하의");
        assertThat(resultFeed.ootds().get(0).name()).isEqualTo("아디다스 트레이닝 팬츠");
        assertThat(resultFeed.ootds().get(0).attributes()).hasSize(1);

        verify(feedRepository, times(1)).findFeedDtos(request, userId);
        verify(feedRepository, times(1)).countFeeds(any(), any(), any(), any());
        verify(feedClothesRepository, times(1)).findOotdsByFeedIds(anyList());
    }

    @Test
    @DisplayName("limit + 1개의 피드를 조회했을 경우 hasNext가 true가 되고 마지막 피드로 nextCursor가 계산된다")
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
        when(feedClothesRepository.findOotdsByFeedIds(anyList()))
            .thenReturn(Collections.emptyMap());

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
        Feed feed = new Feed(
            userId,
            UUID.randomUUID(),
            "테스트 피드",
            0L,
            0L
        );

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // when
        feedService.delete(feedId);

        // then
        verify(feedRepository, times(1)).findById(feedId);
        verify(feedRepository, times(1)).delete(feed);
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
                assertThat(fnf.getFeedId()).isEqualTo(feedId);
            });

        verify(feedRepository, times(1)).findById(feedId);
        verify(feedRepository, never()).delete(any());
    }
}