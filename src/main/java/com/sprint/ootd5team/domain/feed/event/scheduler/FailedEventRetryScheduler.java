package com.sprint.ootd5team.domain.feed.event.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedFailedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Kafka 발행에 실패한 이벤트들을
 * 파일 기반으로 재시도 처리하는 스케줄러
 *
 * <p>
 * FeedFailedEventHandler 에 의해 파일로 저장된 실패 이벤트들을
 * 주기적으로 읽어 Kafka로 재발행을 시도한다.
 * </p>
 *
 * <p>
 * 재발행 성공 시 해당 파일은 삭제되며,
 * 실패 시 다음 스케줄까지 파일이 유지된다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FailedEventRetryScheduler {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${failed-events.path:/var/log/app/failed-events}")
    private String primaryPath;

    @Value("${failed-events.backup-path:/tmp/failed-events}")
    private String backupPath;

    /**
     * 실패 이벤트 재시도 스케줄러 (1분 주기로 실행, 10초 대기)
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void retryFailedEvents() {
        log.debug("[FailedEventRetryScheduler] 실패 이벤트 재시도 시작");

        int retried = retryDirectory(primaryPath);
        retried += retryDirectory(backupPath);

        if (retried > 0) {
            log.info("[FailedEventRetryScheduler] {} 이벤트 재시도 완료", retried);
        }
    }

    /**
     * 지정된 디렉토리 내 실패 이벤트 파일들을 순회하며 재시도한다.
     *
     * @param basePath 실패 이벤트 파일들이 저장된 디렉토리
     * @return 재발행에 성공한 이벤트 수
     */
    private int retryDirectory(String basePath) {
        int successCount = 0;

        try {
            Path dir = Path.of(basePath);
            if (!Files.exists(dir)) {
                return 0;
            }

            var files = Files.list(dir)
                .filter(path -> path.toString().endsWith(".json"))
                .toList();

            for (Path file : files) {
                if (retryFile(file)) {
                    successCount++;
                }
            }

        } catch (IOException e) {
            log.error("[FailedEventRetryScheduler] 디렉토리 읽기 실패: {}", basePath, e);
        }

        return successCount;
    }

    /**
     * 단일 실패 이벤트 파일을 읽어 Kafka 재발행을 시도한다.
     *
     * <p>
     * 성공 시 파일은 삭제되며,
     * 실패 시 파일은 유지되어 다음 스케줄에서 재시도된다.
     * </p>
     *
     * @param file 실패 이벤트 JSON 파일
     * @return 재발행 성공 여부
     */
    private boolean retryFile(Path file) {
        try {
            String json = Files.readString(file);
            FeedFailedEvent feedFailedEvent = objectMapper.readValue(json, FeedFailedEvent.class);

            Object event = deserializeEvent(feedFailedEvent);
            if (event == null) {
                log.warn("[FailedEventRetryScheduler] 알 수 없는 이벤트 타입: {}", feedFailedEvent.eventType());
                return false;
            }

            String key = extractPartitionKey(event);

            kafkaTemplate.send(feedFailedEvent.topic(), key, event)
                .get(5, TimeUnit.SECONDS);

            Files.delete(file);
            log.info("[FailedEventRetryScheduler] 재시도 성공 및 파일 삭제: {}", file.getFileName());
            return true;

        } catch (Exception e) {
            log.warn("[FailedEventRetryScheduler] 재시도 실패: {}", file.getFileName());
            return false;
        }
    }

    /**
     * FeedFailedEvent 에 저장된 eventType 을 기준으로
     * 실제 도메인 이벤트 객체로 역직렬화한다.
     *
     * @param feedFailedEvent 실패 이벤트 레코드
     * @return 복원된 이벤트 객체 (알 수 없는 타입이면 null)
     */
    private Object deserializeEvent(FeedFailedEvent feedFailedEvent) throws IOException {
        String eventJson = objectMapper.writeValueAsString(feedFailedEvent.event());

        return switch (feedFailedEvent.eventType()) {
            case "FeedIndexCreatedEvent" ->
                objectMapper.readValue(eventJson, FeedIndexCreatedEvent.class);
            case "FeedContentUpdatedEvent" ->
                objectMapper.readValue(eventJson, FeedContentUpdatedEvent.class);
            case "FeedLikeCountUpdateEvent" ->
                objectMapper.readValue(eventJson, FeedLikeCountUpdateEvent.class);
            case "FeedDeletedEvent" ->
                objectMapper.readValue(eventJson, FeedDeletedEvent.class);
            default -> null;
        };
    }

    /**
     * Kafka 메시지의 Partition Key 로 사용할 feedId 를 추출한다.
     *
     * @param event 실제 도메인 이벤트
     * @return feedId 문자열 (알 수 없는 타입이면 null)
     */
    private String extractPartitionKey(Object event) {
        if (event instanceof FeedIndexCreatedEvent e) {
            return e.getFeedId().toString();
        } else if (event instanceof FeedContentUpdatedEvent e) {
            return e.getFeedId().toString();
        } else if (event instanceof FeedLikeCountUpdateEvent e) {
            return e.getFeedId().toString();
        } else if (event instanceof FeedDeletedEvent e) {
            return e.getFeedId().toString();
        }
        return null;
    }
}