package com.sprint.ootd5team.domain.like.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.like.entity.FeedLike;
import com.sprint.ootd5team.domain.like.repository.FeedLikeRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
@DisplayName("FeedLikeRepository 테스트")
public class FeedLikeRepositoryTest {

    @Autowired
    private FeedLikeRepository feedLikeRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Test
    @DisplayName("existsByFeedIdAndUserId - 저장된 좋아요가 있으면 true 반환")
    void existsByFeedIdAndUserId_true() {
        // given
        UUID feedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Feed feed = Feed.of(UUID.randomUUID(), UUID.randomUUID(), "테스트 피드 내용");
        feedRepository.save(feed);

        FeedLike feedLike = new FeedLike(feedId, userId);
        feedLikeRepository.save(feedLike);

        // when
        boolean exists = feedLikeRepository.existsByFeedIdAndUserId(feedId, userId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByFeedIdAndUserId - 저장된 좋아요가 없으면 false 반환")
    void existsByFeedIdAndUserId_false() {
        // given
        UUID feedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // when
        boolean exists = feedLikeRepository.existsByFeedIdAndUserId(feedId, userId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteByFeedIdAndUserId - 특정 좋아요 삭제")
    void deleteByFeedIdAndUserId() {
        // given
        UUID feedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Feed feed = Feed.of(UUID.randomUUID(), UUID.randomUUID(), "테스트 피드 내용");
        feedRepository.save(feed);

        FeedLike feedLike = new FeedLike(feedId, userId);
        feedLikeRepository.save(feedLike);

        assertThat(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).isTrue();

        // when
        feedLikeRepository.deleteByFeedIdAndUserId(feedId, userId);

        // then
        assertThat(feedLikeRepository.existsByFeedIdAndUserId(feedId, userId)).isFalse();
    }
}