package com.sprint.ootd5team.domain.feed.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.scheduler.FailedEventRetryScheduler;
import com.sprint.ootd5team.domain.feed.event.type.FeedFailedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FailedEventRetryScheduler 테스트")
public class FailedEventRetrySchedulerTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    FailedEventRetryScheduler scheduler;

    private Path tempDir;
    private UUID feedId = UUID.randomUUID();
    private FeedIndexCreatedEvent feedEvent;
    private FeedFailedEvent testFeedFailedEvent;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("failed-events-test");
        feedEvent = new FeedIndexCreatedEvent(feedId, "test", Instant.now());
        testFeedFailedEvent = new FeedFailedEvent(
            Instant.now(),
            "feed-topic",
            "FeedIndexCreatedEvent",
            feedEvent,
            null,
            null
        );

        ReflectionTestUtils.setField(scheduler, "primaryPath", tempDir.toString());
        ReflectionTestUtils.setField(scheduler, "backupPath", "not-exist-path");
    }

    @Test
    @DisplayName("실패 이벤트 파일을 Kafka로 재발행 후 삭제")
    void retryFailedEvent_success() throws Exception {
        // given
        Path file = tempDir.resolve("event.json");
        Files.writeString(file, "{}");

        when(objectMapper.readValue(anyString(), any(Class.class)))
            .thenReturn(testFeedFailedEvent);

        when(objectMapper.writeValueAsString(any()))
            .thenReturn("{\"feedId\":\"" + feedId + "\"}");

        when(objectMapper.readValue(anyString(), eq(FeedIndexCreatedEvent.class)))
            .thenReturn(feedEvent);

        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // when
        scheduler.retryFailedEvents();

        // then
        verify(kafkaTemplate).send(
            eq("feed-topic"),
            eq(feedId.toString()),
            eq(feedEvent)
        );

        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    @DisplayName("알 수 없는 이벤트 타입이면 Kafka로 재발행하지 않음")
    void retryFailedEvent_unknownType() throws Exception {
        // given
        testFeedFailedEvent = new FeedFailedEvent(
            Instant.now(),
            "feed-topic",
            "UnknownEvent",
            new Object(),
            null,
            null
        );

        Path file = tempDir.resolve("unknown.json");
        Files.writeString(file, "{}");

        when(objectMapper.readValue(anyString(), any(Class.class)))
            .thenReturn(testFeedFailedEvent);

        // when
        scheduler.retryFailedEvents();

        // then
        verify(kafkaTemplate, never()).send(any(), any(), any());
        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    @DisplayName("Kafka 전송 실패 시 파일 삭제되지 않음")
    void retryFailedEvent_kafkaFail() throws Exception {
        // given
        Path file = tempDir.resolve("fail.json");
        Files.writeString(file, "{}");

        when(objectMapper.readValue(anyString(), any(Class.class)))
            .thenReturn(testFeedFailedEvent);

        when(objectMapper.writeValueAsString(any()))
            .thenReturn("{}");

        when(objectMapper.readValue(anyString(), eq(FeedIndexCreatedEvent.class)))
            .thenReturn(feedEvent);

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(future);

        // when
        scheduler.retryFailedEvents();

        // then
        verify(kafkaTemplate).send(any(), any(), any());
        assertThat(Files.exists(file)).isTrue();
    }

    @Test
    @DisplayName("디렉토리가 존재하지 않으면 작업 비실행")
    void retryFailedEvent_directoryNotExist() {
        // given
        ReflectionTestUtils.setField(scheduler, "primaryPath", "not-exist");

        // when
        scheduler.retryFailedEvents();

        // then
        verifyNoInteractions(kafkaTemplate);
    }
}