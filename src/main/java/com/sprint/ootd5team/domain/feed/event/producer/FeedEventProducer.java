package com.sprint.ootd5team.domain.feed.event.producer;

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
 * <p>피드 생성, 내용 수정, 좋아요 수 변경, 삭제와 같은 주요 이벤트를
 * Kafka 토픽으로 비동기 전송한다.
 * 각 이벤트는 JSON 문자열로 직렬화되어 {@link KafkaTemplate}을 통해 전송된다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async("eventTaskExecutor")
    public void publishFeedIndexCreatedEvent(FeedIndexCreatedEvent event) {
        send("ootd.Feeds.Created", event, "FeedCreatedEvent");
    }

    @Async("eventTaskExecutor")
    public void publishFeedContentUpdatedEvent(FeedContentUpdatedEvent event) {
        send("ootd.Feeds.ContentUpdated", event, "FeedUpdatedEvent");
    }

    @Async("eventTaskExecutor")
    public void publishLikeCountUpdatedEvent(FeedLikeCountUpdateEvent event) {
        send("ootd.Feeds.LikeUpdated", event, "FeedLikeCountUpdateEvent");
    }

    @Async("eventTaskExecutor")
    public void publishFeedDeletedEvent(FeedDeletedEvent event) {
        send("ootd.Feeds.Deleted", event, "FeedDeletedEvent");
    }

    private void send(String topic, Object event, String typeName) {
        kafkaTemplate.send(topic, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[FeedEventProducer] Kafka - {} 발행 실패", typeName, ex);
                } else {
                    log.info("[FeedEventProducer] Kafka - {} 발행 완료: {}", typeName, event);
                }
            });
    }
}