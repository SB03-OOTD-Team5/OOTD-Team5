package com.sprint.ootd5team.domain.feed.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import com.sprint.ootd5team.domain.feed.indexer.ElasticsearchFeedIndexer;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedEventConsumer {

    private final ObjectMapper objectMapper;
    private final ElasticsearchFeedIndexer feedIndexer;
    private final ElasticsearchOperations operations;

    @KafkaListener(topics = "ootd.Feeds.Created", groupId = "ootd.feed-indexer")
    public void consumeFeedIndexCreatedEvent(String message) {
        try {
            FeedIndexCreatedEvent event = objectMapper.readValue(message, FeedIndexCreatedEvent.class);
            log.info("[FeedEventConsumer] Kafka - FeedIndexCreatedEvent 수신: {}", event);

            feedIndexer.index(event);

        } catch (Exception e) {
            log.error("[FeedEventConsumer] Kafka - FeedIndexCreatedEvent 처리 실패: {}", message, e);
        }
    }

    @KafkaListener(topics = "ootd.Feeds.ContentUpdated", groupId = "ootd.feed-indexer")
    public void consumeFeedContentUpdatedEvent(String message) {
        try {
            FeedContentUpdatedEvent event = objectMapper.readValue(message, FeedContentUpdatedEvent.class);
            log.info("[FeedEventConsumer] Kafka - FeedUpdatedEvent 수신: {}", event);

            Map<String, Object> doc = Map.of("content", event.getContent());
            UpdateQuery query = UpdateQuery.builder(event.getFeedId().toString())
                .withDocument(Document.from(doc))
                .build();

            operations.update(query, IndexCoordinates.of("feeds-v5"));
            log.info("[FeedEventConsumer] Elasticsearch 피드 업데이트 완료: feedId={}, content={}",
                event.getFeedId(), event.getContent()
            );

        } catch (Exception e) {
            log.error("[FeedEventConsumer] FeedUpdatedEvent 처리 실패", e);
        }
    }

    @KafkaListener(topics = "ootd.Feeds.LikeUpdated", groupId = "ootd.feed-indexer")
    public void consumeFeedLikeCountUpdatedEvent(String message) {
        try {
            FeedLikeCountUpdateEvent event = objectMapper.readValue(message, FeedLikeCountUpdateEvent.class);
            log.info("[FeedEventConsumer] Kafka - FeedLikeCountUpdateEvent 수신: {}", event);

            UUID feedId = event.getFeedId();
            long newLikeCount = event.getNewLikeCount();

            Map<String, Object> updateDoc = Map.of("likeCount", newLikeCount);
            UpdateQuery updateQuery = UpdateQuery.builder(feedId.toString())
                .withDocument(Document.from(updateDoc))
                .build();

            operations.update(updateQuery, IndexCoordinates.of("feeds-v5"));
            log.info("[FeedEventConsumer] Elasticsearch likeCount 업데이트 완료: feedId={}, likeCount={}",
                event.getFeedId(), newLikeCount
            );

        } catch (Exception e) {
            log.error("[FeedEventConsumer] FeedLikeCountUpdateEvent 처리 실패", e);
        }
    }

    @KafkaListener(topics = "ootd.Feeds.Deleted", groupId = "ootd.feed-indexer")
    public void consumeFeedDeletedEvent(String message) {
        try {
            FeedDeletedEvent event = objectMapper.readValue(message, FeedDeletedEvent.class);
            log.info("[FeedEventConsumer] Kafka - FeedDeletedEvent 수신: {}", event);

            operations.delete(event.getFeedId().toString(), IndexCoordinates.of("feeds-v5"));
            log.info("[FeedEventConsumer] Elasticsearch 피드 삭제 완료: {}", event.getFeedId());

        } catch (Exception e) {
            log.error("[FeedEventConsumer] FeedDeletedEvent 처리 실패", e);
        }
    }
}