package com.sprint.ootd5team.domain.feed.event.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 피드 도메인 관련 Kafka 이벤트를 발행하는 프로듀서
 *
 * <p>피드 생성, 내용 수정, 좋아요 수 변경, 삭제와 같은 주요 이벤트를
 * Kafka 토픽으로 비동기 전송한다.
 * 각 이벤트는 JSON 문자열로 직렬화되어 {@link KafkaTemplate}을 통해 전송된다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("eventTaskExecutor")
    public void publishFeedIndexCreatedEvent(FeedIndexCreatedEvent event) {
        log.info("[FeedEventProducer] FeedIndexCreatedEvent 이벤트 발행 시작");

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ootd.Feeds.Created", payload);
            log.info("[FeedEventProducer] Kafka - FeedCreatedEvent 발행 완료: {}", payload);

        } catch (Exception e) {
            log.error("[FeedEventProducer] Kafka - FeedCreatedEvent 발행 실패", e);
        }
    }

    @Async("eventTaskExecutor")
    public void publishFeedContentUpdatedEvent(FeedContentUpdatedEvent event) {
        log.info("[FeedEventProducer] FeedUpdatedEvent 이벤트 발행 시작");

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ootd.Feeds.ContentUpdated", payload);
            log.info("[FeedEventProducer] Kafka - FeedUpdatedEvent 발행 완료: {}", payload);

        } catch (Exception e) {
            log.error("[FeedEventProducer] Kafka - FeedUpdatedEvent 발행 실패", e);
        }
    }

    @Async("eventTaskExecutor")
    public void publishLikeCountUpdatedEvent(FeedLikeCountUpdateEvent event) {
        log.info("[FeedEventProducer] FeedLikeCountUpdateEvent 이벤트 발행 시작");

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ootd.Feeds.LikeUpdated", payload);
            log.info("[FeedEventProducer] Kafka - LikeChangedEvent 발행 완료: {}", payload);

        } catch (Exception e) {
            log.error("[FeedEventProducer] Kafka - FeedLikeChangedEvent 직렬화 실패", e);
        }
    }

    @Async("eventTaskExecutor")
    public void publishFeedDeletedEvent(FeedDeletedEvent event) {
        log.info("[FeedEventProducer] FeedDeletedEvent 이벤트 발행 시작");

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("ootd.Feeds.Deleted", payload);
            log.info("[FeedEventProducer] Kafka - FeedDeletedEvent 발행 완료: {}", payload);
        } catch (Exception e) {
            log.error("[FeedEventProducer] Kafka - FeedDeletedEvent 발행 실패", e);
        }
    }
}