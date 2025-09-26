package com.sprint.ootd5team.domain.like.service;

import com.sprint.ootd5team.base.exception.feed.AlreadyLikedException;
import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.base.exception.feed.LikeCountUnderflowException;
import com.sprint.ootd5team.base.exception.feed.LikeNotFoundException;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.like.entity.FeedLike;
import com.sprint.ootd5team.domain.like.repository.FeedLikeRepository;
import com.sprint.ootd5team.domain.notification.event.type.single.FeedLikedEvent;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FeedLikeServiceImpl implements FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void like(UUID feedId, UUID currentUserId) {
        log.info("[FeedLikeService] 피드 좋아요 활성화 시작 - feedId = {}, currentUserId = {}", feedId,
            currentUserId);

        Feed feed = validateFeed(feedId);
        validateNotLiked(feedId, currentUserId);

        FeedLike feedLike = new FeedLike(feedId, currentUserId);
        feedLikeRepository.save(feedLike);
        log.debug("[FeedLikeService] 저장된 FeedLike: {}", feedLike);

        feedRepository.incrementLikeCount(feedId);

        // 알림 전송
        // 좋아요 누른 사람 이름 가져오기
        String username = userRepository.findUserNameById(currentUserId);
        eventPublisher.publishEvent(
            new FeedLikedEvent(feed.getId(), feed.getAuthorId(), feed.getContent(), username)
        );
    }

    @Transactional
    public void unLike(UUID feedId, UUID currentUserId) {
        log.info("[FeedLikeService] 피드 좋아요 비활성화 시작 - feedId = {}, currentUserId = {}", feedId,
            currentUserId);

        validateFeed(feedId);
        validateLiked(feedId, currentUserId);

        feedLikeRepository.deleteByFeedIdAndUserId(feedId, currentUserId);

        int updatedRows = feedRepository.decrementLikeCount(feedId);
        if (updatedRows == 0) {
            log.error("[FeedLikeService] 좋아요 수 감소 실패");
            throw LikeCountUnderflowException.withFeedId(feedId);
        }
    }

    private Feed validateFeed(UUID feedId) {
        return feedRepository.findById(feedId)
            .orElseThrow(() -> {
                log.warn("[FeedLikeService] 피드가 존재하지 않습니다.");
                return FeedNotFoundException.withId(feedId);
            });
    }

    private void validateNotLiked(UUID feedId, UUID currentUserId) {
        boolean exists = feedLikeRepository.existsByFeedIdAndUserId(feedId, currentUserId);
        if (exists) {
            log.warn("[FeedLikeService] 이미 좋아요 처리 된 피드입니다.");
            throw AlreadyLikedException.withIds(feedId, currentUserId);
        }
    }

    private void validateLiked(UUID feedId, UUID currentUserId) {
        if (!feedLikeRepository.existsByFeedIdAndUserId(feedId, currentUserId)) {
            log.warn("[FeedLikeService] 존재하지 않는 좋아요입니다.");
            throw LikeNotFoundException.withIds(feedId, currentUserId);
        }
    }
}