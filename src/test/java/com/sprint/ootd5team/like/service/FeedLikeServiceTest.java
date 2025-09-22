package com.sprint.ootd5team.like.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.ootd5team.base.exception.feed.AlreadyLikedException;
import com.sprint.ootd5team.base.exception.feed.LikeCountUnderflowException;
import com.sprint.ootd5team.base.exception.feed.LikeNotFoundException;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.like.entity.FeedLike;
import com.sprint.ootd5team.domain.like.repository.FeedLikeRepository;
import com.sprint.ootd5team.domain.like.service.FeedLikeServiceImpl;
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
@DisplayName("FeedLikeService 슬라이스 테스트")
@ActiveProfiles("test")
public class FeedLikeServiceTest {

    @Mock
    private FeedLikeRepository feedLikeRepository;

    @Mock
    private FeedRepository feedRepository;

    @InjectMocks
    private FeedLikeServiceImpl feedLikeService;

    private UUID feedId;
    private UUID userId;
    private Feed feed;

    @BeforeEach
    void setUp() {
        feedId = UUID.randomUUID();
        userId = UUID.randomUUID();
        feed = Feed.of(UUID.randomUUID(), UUID.randomUUID(), "테스트");
    }

    @Test
    @DisplayName("like() - 성공적으로 좋아요 등록")
    void like_success() {
        // given
        given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
        given(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(false);

        // when
        feedLikeService.like(feedId, userId);

        // then
        then(feedLikeRepository).should().save(any(FeedLike.class));
        then(feedRepository).should().incrementLikeCount(feedId);
    }

    @Test
    @DisplayName("like() - 이미 좋아요 했으면 예외 발생")
    void like_alreadyLiked() {
        // given
        given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
        given(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> feedLikeService.like(feedId, userId))
            .isInstanceOf(AlreadyLikedException.class);

        then(feedLikeRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("unLike() - 성공적으로 좋아요 취소")
    void unLike_success() {
        // given
        given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
        given(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(true);
        given(feedRepository.decrementLikeCount(feedId)).willReturn(1);

        // when
        feedLikeService.unLike(feedId, userId);

        // then
        then(feedLikeRepository).should().deleteByFeedIdAndUserId(feedId, userId);
        then(feedRepository).should().decrementLikeCount(feedId);
    }

    @Test
    @DisplayName("unLike() - 좋아요가 없으면 예외 발생")
    void unLike_notFound() {
        // given
        given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
        given(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> feedLikeService.unLike(feedId, userId))
            .isInstanceOf(LikeNotFoundException.class);

        then(feedLikeRepository).should(never()).deleteByFeedIdAndUserId(any(), any());
        then(feedRepository).should(never()).decrementLikeCount(any());
    }

    @Test
    @DisplayName("unLike() - 이미 likeCount가 0인 경우 예외 발생")
    void unLike_underflow() {
        // given
        given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
        given(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).willReturn(true);
        given(feedRepository.decrementLikeCount(feedId)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> feedLikeService.unLike(feedId, userId))
            .isInstanceOf(LikeCountUnderflowException.class);

        then(feedLikeRepository).should().deleteByFeedIdAndUserId(feedId, userId);
        then(feedRepository).should().decrementLikeCount(feedId);
    }
}