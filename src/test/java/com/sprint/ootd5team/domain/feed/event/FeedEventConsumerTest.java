package com.sprint.ootd5team.domain.feed.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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
        FeedIndexCreatedEvent event =
            new FeedIndexCreatedEvent(UUID.randomUUID(), "내용", Instant.now());

        // when
        when(objectMapper.readValue(payload, FeedIndexCreatedEvent.class))
            .thenReturn(event);

        // then
        consumer.consumeFeedIndexCreatedEvent(payload);

        verify(indexer).create(event);
    }

    @Test
    @DisplayName("FeedContentUpdatedEvent 수신 시 ES update 호출")
    void consumeFeedContentUpdatedEvent_success() throws Exception {
        // given
        FeedContentUpdatedEvent event =
            new FeedContentUpdatedEvent(UUID.randomUUID(), "새로운 내용");

        // when
        when(objectMapper.readValue(payload, FeedContentUpdatedEvent.class))
            .thenReturn(event);

        // then
        consumer.consumeFeedContentUpdatedEvent(payload);

        verify(indexer).updateContent(event);
    }

    @Test
    @DisplayName("FeedLikeCountUpdateEvent 수신 시 ES update 호출")
    void consumeFeedLikeCountUpdatedEvent_success() throws Exception {
        // given
        FeedLikeCountUpdateEvent event =
            new FeedLikeCountUpdateEvent(UUID.randomUUID(), 42);

        // when
        when(objectMapper.readValue(payload, FeedLikeCountUpdateEvent.class))
            .thenReturn(event);

        // then
        consumer.consumeFeedLikeCountUpdatedEvent(payload);

        verify(indexer).updateLikeCount(event);
    }

    @Test
    @DisplayName("FeedDeletedEvent 수신 시 ES delete 호출")
    void consumeFeedDeletedEvent_success() throws Exception {
        // given
        FeedDeletedEvent event = new FeedDeletedEvent(UUID.randomUUID());

        // when
        when(objectMapper.readValue(payload, FeedDeletedEvent.class))
            .thenReturn(event);

        // then
        consumer.consumeFeedDeletedEvent(payload);

        verify(indexer).delete(event);
    }

    @Test
    @DisplayName("역직렬화 실패 시 RuntimeException 발생")
    void handleEvent_deserializationFailure_throwsException() throws Exception {
        // when
        when(objectMapper.readValue(payload, FeedIndexCreatedEvent.class))
            .thenThrow(new RuntimeException("역직렬화 실패"));

        // then
        assertThatThrownBy(() -> consumer.consumeFeedIndexCreatedEvent(payload))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Kafka 메시지 처리 실패");

        verify(indexer, never()).create(any());
    }

    @Test
    @DisplayName("이중 직렬화 감지 시 unwrap 후 재역직렬화")
    void handleEvent_doubleEncodedJson_unwrapAndProcess() throws Exception {
        // given
        String unwrapped = "{\"feedId\":\"" + UUID.randomUUID() + "\",\"content\":\"내용\",\"createdAt\":\"" + Instant.now() + "\"}";
        String doubleEncoded = "\"" + unwrapped.replace("\"", "\\\"") + "\"";

        FeedIndexCreatedEvent event =
            new FeedIndexCreatedEvent(UUID.randomUUID(), "내용", Instant.now());

        // when
        when(objectMapper.readValue(doubleEncoded, FeedIndexCreatedEvent.class))
            .thenThrow(mock(MismatchedInputException.class));

        when(objectMapper.readValue(doubleEncoded, String.class))
            .thenReturn(unwrapped);

        when(objectMapper.readValue(unwrapped, FeedIndexCreatedEvent.class))
            .thenReturn(event);

        // then
        consumer.consumeFeedIndexCreatedEvent(doubleEncoded);

        verify(indexer).create(event);
    }

    @Test
    @DisplayName("MismatchedInputException이지만 double-encoded가 아니면 실패 처리")
    void handleEvent_mismatchedButNotDoubleEncoded_throwsException() throws Exception {
        // when
        when(objectMapper.readValue(payload, FeedIndexCreatedEvent.class))
            .thenThrow(mock(MismatchedInputException.class));

        // then
        assertThatThrownBy(() -> consumer.consumeFeedIndexCreatedEvent(payload))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Kafka 메시지 처리 실패");

        verify(indexer, never()).create(any());
    }

    @Test
    @DisplayName("document missing 예외 시 예외를 삼키고 정상 종료")
    void handleEvent_documentMissing_shouldReturnWithoutThrowing() throws Exception {
        // given
        FeedDeletedEvent event = new FeedDeletedEvent(UUID.randomUUID());

        // when
        when(objectMapper.readValue(payload, FeedDeletedEvent.class))
            .thenReturn(event);

        doThrow(new RuntimeException("document missing"))
            .when(indexer).delete(event);

        // then
        assertThatCode(() -> consumer.consumeFeedDeletedEvent(payload))
            .doesNotThrowAnyException();

        verify(indexer).delete(event);
    }
}
