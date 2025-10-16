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
            kafkaTemplate.send("ootd.Feeds.Updated", payload);
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