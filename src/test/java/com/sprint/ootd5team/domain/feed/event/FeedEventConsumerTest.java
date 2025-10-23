package com.sprint.ootd5team.domain.feed.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.consumer.FeedEventConsumer;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import com.sprint.ootd5team.domain.feed.indexer.ElasticsearchFeedIndexer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedEventConsumer 슬라이스 테스트")
public class FeedEventConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ElasticsearchFeedIndexer indexer;

    @InjectMocks
    private FeedEventConsumer consumer;

    private String payload;

    @BeforeEach
    void setUp() {
        payload = "{\"feedId\":\"" + UUID.randomUUID() + "\"}";
    }

    @Test
    @DisplayName("FeedIndexCreatedEvent 수신 시 인덱서 호출")
    void consumeFeedIndexCreatedEvent_success() throws Exception {
        // given
        FeedIndexCreatedEvent feedIndexCreatedEvent = new FeedIndexCreatedEvent(UUID.randomUUID(), "내용", Instant.now());

        when(objectMapper.readValue(payload, FeedIndexCreatedEvent.class))
            .thenReturn(feedIndexCreatedEvent);

        // when
        consumer.consumeFeedIndexCreatedEvent(payload);

        // then
        verify(indexer).create(feedIndexCreatedEvent);
    }

    @Test
    @DisplayName("FeedContentUpdatedEvent 수신 시 ES update 호출")
    void consumeFeedContentUpdatedEvent_success() throws Exception {
        // given
        FeedContentUpdatedEvent feedContentUpdatedEvent = new FeedContentUpdatedEvent(UUID.randomUUID(), "새로운 내용");

        when(objectMapper.readValue(payload, FeedContentUpdatedEvent.class))
            .thenReturn(feedContentUpdatedEvent);

        // when
        consumer.consumeFeedContentUpdatedEvent(payload);

        // then
        verify(indexer).updateContent(feedContentUpdatedEvent);
    }

    @Test
    @DisplayName("FeedLikeCountUpdateEvent 수신 시 ES update 호출")
    void consumeFeedLikeCountUpdatedEvent_success() throws Exception {
        // given
        FeedLikeCountUpdateEvent feedLikeCountUpdateEvent = new FeedLikeCountUpdateEvent(UUID.randomUUID(), 42);

        when(objectMapper.readValue(payload, FeedLikeCountUpdateEvent.class))
            .thenReturn(feedLikeCountUpdateEvent);

        // when
        consumer.consumeFeedLikeCountUpdatedEvent(payload);

        // then
        verify(indexer).updateLikeCount(feedLikeCountUpdateEvent);
    }

    @Test
    @DisplayName("FeedDeletedEvent 수신 시 ES delete 호출")
    void consumeFeedDeletedEvent_success() throws Exception {
        // given
        FeedDeletedEvent feedDeletedEvent = new FeedDeletedEvent(UUID.randomUUID());

        when(objectMapper.readValue(payload, FeedDeletedEvent.class))
            .thenReturn(feedDeletedEvent);

        // when
        consumer.consumeFeedDeletedEvent(payload);

        // then
        verify(indexer).delete(feedDeletedEvent);
    }

    @Test
    @DisplayName("역직렬화 실패 시 RuntimeException 발생")
    void handleEvent_deserializationFailure_throwsException() throws Exception {
        when(objectMapper.readValue(payload, FeedIndexCreatedEvent.class))
            .thenThrow(new RuntimeException("역직렬화 실패"));

        assertThatThrownBy(() -> consumer.consumeFeedIndexCreatedEvent(payload))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Kafka 메시지 처리 실패");

        verify(indexer, never()).create(any());
    }
}