package com.sprint.ootd5team.domain.feed.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.domain.feed.event.producer.FeedEventProducer;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedEventProducer 슬라이스 테스트")
public class FeedEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private FeedEventProducer producer;

    @BeforeEach
    void setUp() {
        when(kafkaTemplate.send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
            .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("FeedIndexCreatedEvent 발행 성공")
    void publishFeedIndexCreatedEvent_success() throws Exception {
        // given
        FeedIndexCreatedEvent event = new FeedIndexCreatedEvent(UUID.randomUUID(), "내용", Instant.now());

        // when
        producer.publishFeedIndexCreatedEvent(event);

        // then
        verify(kafkaTemplate).send(eq("ootd.Feeds.Created"), eq(event));
    }

    @Test
    @DisplayName("FeedContentUpdatedEvent 발행 성공")
    void publishFeedContentUpdatedEvent_success() throws Exception {
        // given
        FeedContentUpdatedEvent event = new FeedContentUpdatedEvent(UUID.randomUUID(), "새로운 내용");

        // when
        producer.publishFeedContentUpdatedEvent(event);

        // then
        verify(kafkaTemplate).send(eq("ootd.Feeds.ContentUpdated"), eq(event));
    }

    @Test
    @DisplayName("FeedLikeCountUpdateEvent 발행 성공")
    void publishLikeCountUpdatedEvent_success() throws Exception {
        // given
        FeedLikeCountUpdateEvent event =
            new FeedLikeCountUpdateEvent(UUID.randomUUID(), 42);

        // when
        producer.publishLikeCountUpdatedEvent(event);

        // then
        verify(kafkaTemplate).send(eq("ootd.Feeds.LikeUpdated"), eq(event));
    }

    @Test
    @DisplayName("FeedDeletedEvent 발행 성공")
    void publishFeedDeletedEvent_success() throws Exception {
        // given
        FeedDeletedEvent event = new FeedDeletedEvent(UUID.randomUUID());

        // when
        producer.publishFeedDeletedEvent(event);

        // then
        verify(kafkaTemplate).send(eq("ootd.Feeds.Deleted"), eq(event));
    }
}