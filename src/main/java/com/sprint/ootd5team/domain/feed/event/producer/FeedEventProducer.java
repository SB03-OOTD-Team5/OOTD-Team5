package com.sprint.ootd5team.domain.feed.event.producer;

import com.sprint.ootd5team.domain.feed.event.handler.FeedFailedEventHandler;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 피드 도메인 관련 Kafka 이벤트를 발행하는 프로듀서
 *
 * <p>피드 생성, 내용 수정, 좋아요 수 변경, 삭제와 같은 주요 이벤트를 Kafka 토픽으로 비동기 전송한다.
 * Kafka 발행 실패 시 {@link FeedFailedEventHandler}를 통해 파일로 저장한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FeedFailedEventHandler feedFailedEventHandler;

    @Async("eventTaskExecutor")
    public void publishFeedIndexCreatedEvent(FeedIndexCreatedEvent event) {
        log.debug("[FeedEventProducer] 피드 생성 이벤트 발행 요청 받음: {}", event);
        String topic = "ootd.Feeds.Created";
        send(topic, event.getFeedId().toString(), event, "FeedCreatedEvent");
    }

    @Async("eventTaskExecutor")
    public void publishFeedContentUpdatedEvent(FeedContentUpdatedEvent event) {
        String topic = "ootd.Feeds.ContentUpdated";
        send(topic, event.getFeedId().toString(), event, "FeedUpdatedEvent");
    }

    @Async("eventTaskExecutor")
    public void publishLikeCountUpdatedEvent(FeedLikeCountUpdateEvent event) {
        String topic = "ootd.Feeds.LikeUpdated";
        send(topic, event.getFeedId().toString(), event, "FeedLikeCountUpdateEvent");
    }

    @Async("eventTaskExecutor")
    public void publishFeedDeletedEvent(FeedDeletedEvent event) {
        String topic = "ootd.Feeds.Deleted";
        send(topic, event.getFeedId().toString(), event, "FeedDeletedEvent");
    }

    private void send(String topic, String key, Object event, String typeName) {
        log.info("[FeedEventProducer] ===== Kafka 발행 시작 =====");
        try {
            kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[FeedEventProducer] Kafka - {} 발행 실패", typeName, ex);
                        feedFailedEventHandler.saveFailedEvent(topic, event, ex);
                    } else {
                        log.info("[FeedEventProducer] Kafka - {} 발행 완료", typeName);
                    }
                });
        } catch (Exception e) {
            log.error("[FeedEventProducer] Kafka 전송 중 동기 예외 발생", e);
            feedFailedEventHandler.saveFailedEvent(topic, event, e);
        }
    }
}