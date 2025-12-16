package com.sprint.ootd5team.domain.feed.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.domain.feed.event.handler.FeedFailedEventHandler;
import com.sprint.ootd5team.domain.feed.event.producer.FeedEventProducer;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedEventProducer 슬라이스 테스트")
public class FeedEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private FeedFailedEventHandler feedFailedEventHandler;

    @InjectMocks
    private FeedEventProducer producer;

    @Test
    @DisplayName("FeedIndexCreatedEvent 발행 성공")
    void publishFeedIndexCreatedEvent_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        FeedIndexCreatedEvent event = new FeedIndexCreatedEvent(feedId, "내용", Instant.now());

        // when
        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        producer.publishFeedIndexCreatedEvent(event);

        // then
        verify(kafkaTemplate).send(
            eq("ootd.Feeds.Created"),
            eq(feedId.toString()),
            eq(event)
        );
    }

    @Test
    @DisplayName("FeedContentUpdatedEvent 발행 성공")
    void publishFeedContentUpdatedEvent_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        FeedContentUpdatedEvent event = new FeedContentUpdatedEvent(feedId, "새로운 내용");

        // when
        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        producer.publishFeedContentUpdatedEvent(event);

        // then
        verify(kafkaTemplate).send(
            eq("ootd.Feeds.ContentUpdated"),
            eq(feedId.toString()),
            eq(event)
        );
    }

    @Test
    @DisplayName("FeedLikeCountUpdateEvent 발행 성공")
    void publishLikeCountUpdatedEvent_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        FeedLikeCountUpdateEvent event = new FeedLikeCountUpdateEvent(feedId, 42);

        // when
        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        producer.publishLikeCountUpdatedEvent(event);

        // then
        verify(kafkaTemplate).send(
            eq("ootd.Feeds.LikeUpdated"),
            eq(feedId.toString()),
            eq(event)
        );
    }

    @Test
    @DisplayName("FeedDeletedEvent 발행 성공")
    void publishFeedDeletedEvent_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        FeedDeletedEvent event = new FeedDeletedEvent(feedId);

        // when
        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        producer.publishFeedDeletedEvent(event);

        // then
        verify(kafkaTemplate).send(
            eq("ootd.Feeds.Deleted"),
            eq(feedId.toString()),
            eq(event)
        );
    }

    @Test
    @DisplayName("Kafka 비동기 전송 실패 시 실패 이벤트 저장 (whenComplete 분기)")
    void publishFeedIndexCreatedEvent_asyncFailure_savesFailedEvent() {
        // given
        UUID feedId = UUID.randomUUID();
        FeedIndexCreatedEvent event =
            new FeedIndexCreatedEvent(feedId, "내용", Instant.now());

        RuntimeException kafkaException = new RuntimeException("Kafka async error");
        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(kafkaException);

        when(kafkaTemplate.send(
            eq("ootd.Feeds.Created"),
            eq(feedId.toString()),
            eq(event)
        )).thenReturn(failedFuture);

        // when
        producer.publishFeedIndexCreatedEvent(event);

        // then
        verify(feedFailedEventHandler).saveFailedEvent(
            eq("ootd.Feeds.Created"),
            eq(event),
            any(Throwable.class)
        );
    }

    @Test
    @DisplayName("Kafka send 호출 중 동기 예외 발생 시 실패 이벤트 저장 (catch 분기)")
    void publishFeedIndexCreatedEvent_syncException_savesFailedEvent() {
        // given
        UUID feedId = UUID.randomUUID();
        FeedIndexCreatedEvent event =
            new FeedIndexCreatedEvent(feedId, "내용", Instant.now());

        RuntimeException syncException = new RuntimeException("Kafka sync error");

        when(kafkaTemplate.send(
            eq("ootd.Feeds.Created"),
            eq(feedId.toString()),
            eq(event)
        )).thenThrow(syncException);

        // when
        producer.publishFeedIndexCreatedEvent(event);

        // then
        verify(feedFailedEventHandler).saveFailedEvent(
            eq("ootd.Feeds.Created"),
            eq(event),
            eq(syncException)
        );
    }
}