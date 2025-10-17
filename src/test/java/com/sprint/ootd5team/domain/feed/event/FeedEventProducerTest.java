package com.sprint.ootd5team.domain.feed.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.producer.FeedEventProducer;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import java.time.Instant;
import java.util.UUID;
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
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FeedEventProducer producer;

    @Test
    @DisplayName("FeedIndexCreatedEvent 발행 성공")
    void publishFeedIndexCreatedEvent_success() throws Exception {
        // given
        FeedIndexCreatedEvent feedIndexCreatedEvent = new FeedIndexCreatedEvent(UUID.randomUUID(), "내용", Instant.now());

        when(objectMapper.writeValueAsString(feedIndexCreatedEvent))
            .thenReturn("payload");

        // when
        producer.publishFeedIndexCreatedEvent(feedIndexCreatedEvent);

        // then
        verify(objectMapper).writeValueAsString(feedIndexCreatedEvent);
        verify(kafkaTemplate).send(eq("ootd.Feeds.Created"), eq("payload"));
    }

    @Test
    @DisplayName("FeedContentUpdatedEvent 발행 성공")
    void publishFeedContentUpdatedEvent_success() throws Exception {
        // given
        FeedContentUpdatedEvent feedContentUpdatedEvent = new FeedContentUpdatedEvent(UUID.randomUUID(), "새로운 내용");

        when(objectMapper.writeValueAsString(feedContentUpdatedEvent))
            .thenReturn("payload");

        // when
        producer.publishFeedContentUpdatedEvent(feedContentUpdatedEvent);

        // then
        verify(objectMapper).writeValueAsString(feedContentUpdatedEvent);
        verify(kafkaTemplate).send(eq("ootd.Feeds.ContentUpdated"), eq("payload"));
    }

    @Test
    @DisplayName("FeedLikeCountUpdateEvent 발행 성공")
    void publishLikeCountUpdatedEvent_success() throws Exception {
        // given
        FeedLikeCountUpdateEvent event = new FeedLikeCountUpdateEvent(UUID.randomUUID(), 42);
        when(objectMapper.writeValueAsString(event))
            .thenReturn("payload");

        // when
        producer.publishLikeCountUpdatedEvent(event);

        // then
        verify(objectMapper).writeValueAsString(event);
        verify(kafkaTemplate).send(eq("ootd.Feeds.LikeUpdated"), eq("payload"));
    }

    @Test
    @DisplayName("FeedDeletedEvent 발행 성공")
    void publishFeedDeletedEvent_success() throws Exception {
        // given
        FeedDeletedEvent event = new FeedDeletedEvent(UUID.randomUUID());
        when(objectMapper.writeValueAsString(event))
            .thenReturn("payload");

        // when
        producer.publishFeedDeletedEvent(event);

        // then
        verify(objectMapper).writeValueAsString(event);
        verify(kafkaTemplate).send(eq("ootd.Feeds.Deleted"), eq("payload"));
    }
}