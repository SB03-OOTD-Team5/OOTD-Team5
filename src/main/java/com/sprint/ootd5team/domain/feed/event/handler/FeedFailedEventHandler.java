package com.sprint.ootd5team.domain.feed.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.type.FeedFailedEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Kafka 이벤트 발행 실패 시 이벤트를 안전하게 보존하기 위한 핸들러
 *
 * <p>저장 우선순위:
 * <ol>
 *   <li>Primary 파일 경로</li>
 *   <li>Backup 파일 경로</li>
 *   <li>메모리 큐</li>
 *   <li>stderr 출력</li>
 * </ol>
 *
 * <p>파일 시스템 장애나 일시적인 오류 상황에서도
 * 이벤트 유실을 최대한 방지하는 것을 목표로 한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedFailedEventHandler {

    private final ObjectMapper objectMapper;

    @Value("${failed-events.path:./logs/failed-events}")
    private String primaryPath;

    @Value("${failed-events.backup-path:./logs/failed-events-backup}")
    private String backupPath;

    @Value("${failed-events.memory-queue-size:1000}")
    private int maxMemoryQueueSize;

    private final Queue<FeedFailedEvent> memoryQueue = new ConcurrentLinkedQueue<>();

    /**
     * Kafka 발행 실패 이벤트를 저장한다.
     *
     * <p>Primary → Backup → Memory → stderr 순서로 fallback 처리한다.</p>
     */
    public void saveFailedEvent(String topic, Object event, Throwable error) {
        log.error("[FeedFeedFailedEventHandler] Kafka 발행 실패 - topic: {}, eventType: {}",
            topic, event.getClass().getSimpleName(), error);

        if (saveToPath(primaryPath, topic, event, error)) {
            return;
        }

        log.warn("[FeedFeedFailedEventHandler] Primary path 실패, backup path 시도");
        if (saveToPath(backupPath, topic, event, error)) {
            return;
        }

        log.warn("[FeedFailedEventHandler] 파일 시스템 실패, 메모리 큐 사용");
        if (saveToMemoryQueue(topic, event, error)) {
            return;
        }

        log.error("[FeedFailedEventHandler] 모든 fallback 실패, stderr 출력");
        logToStderr(topic, event, error);
    }

    /**
     * 지정된 경로에 실패 이벤트를 JSON 파일로 저장한다.
     *
     * @return 저장 성공 여부
     */
    private boolean saveToPath(String basePath, String topic, Object event, Throwable error) {
        try {
            Path dir = Path.of(basePath);

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            if (!Files.isWritable(dir)) {
                log.error("FailedFeedEventHandler] 디렉토리 쓰기 불가: {}", basePath);
                return false;
            }

            String filename = generateFilename(event);
            Path file = dir.resolve(filename);

            FeedFailedEvent feedFailedEvent = new FeedFailedEvent(
                Instant.now(),
                topic,
                event.getClass().getSimpleName(),
                event,
                error.getMessage(),
                getStackTrace(error)
            );

            String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(feedFailedEvent);

            Files.writeString(file, json, StandardOpenOption.CREATE_NEW);

            log.info("[FeedFailedEventHandler] 파일 저장 성공: {}", file);
            return true;

        } catch (IOException e) {
            log.error("[FeedFailedEventHandler] 파일 저장 실패: {}", basePath, e);
            return false;
        }
    }

    /**
     * 파일 저장이 불가능할 경우 메모리 큐에 임시 보관한다.
     *
     * @return 큐 저장 성공 여부
     */
    private boolean saveToMemoryQueue(String topic, Object event, Throwable error) {
        try {
            if (memoryQueue.size() >= maxMemoryQueueSize) {
                log.error("[FeedFailedEventHandler] 메모리 큐 가득 찼음 ({})", maxMemoryQueueSize);
                return false;
            }

            FeedFailedEvent feedFailedEvent = new FeedFailedEvent(
                Instant.now(),
                topic,
                event.getClass().getSimpleName(),
                event,
                error.getMessage(),
                getStackTrace(error)
            );

            memoryQueue.offer(feedFailedEvent);
            log.warn("[FeedFailedEventHandler] 메모리 큐 저장 (size: {})", memoryQueue.size());
            return true;

        } catch (Exception e) {
            log.error("[FeedFailedEventHandler] 메모리 큐 저장 실패", e);
            return false;
        }
    }

    /**
     * stderr로 이벤트 정보를 출력한다.
     */
    private void logToStderr(String topic, Object event, Throwable error) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                "level", "CRITICAL_KAFKA_FAILURE",
                "timestamp", Instant.now().toString(),
                "topic", topic,
                "eventType", event.getClass().getSimpleName(),
                "event", event,
                "error", error.getMessage()
            ));

            System.err.println("KAFKA_FAILURE: " + json);

        } catch (Exception e) {
            System.err.printf(
                "FATAL_KAFKA_FAILURE: topic=%s, eventType=%s, error=%s%n",
                topic,
                event.getClass().getSimpleName(),
                error.getMessage()
            );
        }
    }

    /**
     * 메모리 큐에 쌓인 이벤트들을 주기적으로 파일로 flush 시도한다.
     */
    @Scheduled(fixedDelay = 10000) // 10초마다
    public void flushMemoryQueueToFile() {
        if (memoryQueue.isEmpty()) {
            return;
        }

        log.info("[FeedFailedEventHandler] 메모리 큐 flush 시도 (size: {})", memoryQueue.size());

        int flushed = 0;
        while (!memoryQueue.isEmpty()) {
            FeedFailedEvent feedFailedEvent = memoryQueue.peek();

            if (feedFailedEvent == null) {
                break;
            }

            if (saveToPath(primaryPath, feedFailedEvent.topic(), feedFailedEvent.event(),
                new RuntimeException(feedFailedEvent.error()))) {
                memoryQueue.poll();
                flushed++;
            } else {
                break;
            }
        }

        if (flushed > 0) {
            log.info("[FeedFailedEventHandler] 메모리 큐에서 {} 이벤트 파일로 flush 완료", flushed);
        }
    }

    /**
     * 오래된 실패 이벤트 파일을 주기적으로 정리한다. (7일 초과 파일 삭제)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupOldFiles() {
        cleanupDirectory(primaryPath);
        cleanupDirectory(backupPath);
    }

    /**
     * 단일 디렉토리 내 오래된 JSON 파일 정리
     */
    private void cleanupDirectory(String basePath) {
        try {
            Path dir = Path.of(basePath);
            if (!Files.exists(dir)) {
                return;
            }

            Instant cutoff = Instant.now().minusSeconds(7 * 24 * 60 * 60);

            long deleted = Files.list(dir)
                .filter(path -> path.toString().endsWith(".json"))
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant().isBefore(cutoff);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .peek(path -> {
                    try {
                        Files.delete(path);
                        log.debug("[FeedFailedEventHandler] 오래된 파일 삭제: {}", path.getFileName());
                    } catch (IOException e) {
                        log.warn("[FeedFailedEventHandler] 파일 삭제 실패: {}", path.getFileName(), e);
                    }
                })
                .count();

            if (deleted > 0) {
                log.info("[FeedFailedEventHandler] {} 오래된 파일 삭제 완료", deleted);
            }

            long fileCount = Files.list(dir)
                .filter(path -> path.toString().endsWith(".json"))
                .count();

            if (fileCount > 100) {
                log.warn("[FeedFailedEventHandler] 실패 이벤트 파일 과다: {} 개", fileCount);
            }

        } catch (IOException e) {
            log.error("[FeedFailedEventHandler] 파일 정리 실패: {}", basePath, e);
        }
    }

    private String generateFilename(Object event) {
        String timestamp = Instant.now().toString()
            .replace(":", "-")
            .replace(".", "-");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String eventType = event.getClass().getSimpleName();

        return String.format("failed-%s-%s-%s.json", eventType, timestamp, uuid);
    }

    private String getStackTrace(Throwable error) {
        if (error == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(error.toString()).append("\n");
        for (StackTraceElement element : error.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}