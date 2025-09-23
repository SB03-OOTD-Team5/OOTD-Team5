package com.sprint.ootd5team.base.sse.repository.message;

import com.sprint.ootd5team.base.sse.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Repository;

/**
 * 인메모리 SSE(Server-Sent Events) 메시지 저장소 구현체
 * <p>
 * 순서 보존을 위해 최근 이벤트 ID를 deque로 관리
 * 이벤트 본문은 빠른 조회를 위해 ConcurrentHashMap에 저장
 * 메모리 사용 제한을 위해 최대 보관 개수를 초과하면 가장 오래된 항목을 제거
 */
@Slf4j
@Repository
@ConditionalOnMissingBean(SseMessageRepository.class)
public class InMemorySseMessageRepositoryImpl implements SseMessageRepository {

    // 메모리에 보관할 SSE 메시지의 최대 개수. 초과 시 가장 오래된 메시지를 제거
    private static final int MAX_MESSAGES = 1000;

    // 저장 순서 유지를 위한 이벤트 ID 덱
    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();

    // 이벤트 ID -> 메시지 본문 매핑
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    /**
     * 메시지를 저장소에 추가하고, 최대 개수 초과시 가장 오래된 항목 제거
     *
     * @param message 저장할 SEE 메시지
     */
    @Override
    public void save(SseMessage message) {
        UUID eventId = message.getId();
        log.debug("[InMemorySseMessageRepositoryImpl] SSE 메시지 저장 시도: id={}, event={}", eventId,
            message.getEventName());

        // 1) ID 순서 컬렉션에 추가
        eventIdQueue.addLast(eventId);
        // 2) 본문 매핑 저장
        messages.put(eventId, message);

        // 3) 최대 보관 개수 초과 시 가장 오래된 항목 제거
        if (eventIdQueue.size() > MAX_MESSAGES) {
            UUID oldest = eventIdQueue.pollFirst();
            if (oldest != null) {
                messages.remove(oldest);
                log.info(
                    "[InMemorySseMessageRepositoryImpl] SSE 보존 한도 초과로 제거: oldestId={}, currentQueueSize={}",
                    oldest, eventIdQueue.size());
            }
        }
        log.info(
            "[InMemorySseMessageRepositoryImpl] SSE 메시지 저장 완료: id={}, queueSize={}, mapSize={}",
            eventId, eventIdQueue.size(), messages.size());
    }

    /**
     * 저장된 마지막 이벤트 ID 이후에 발생한 모든 메시지를 반환
     */
    @Override
    public List<SseMessage> findAfter(UUID lastEventId) {
        if (lastEventId == null) {
            log.debug("[InMemorySseMessageRepositoryImpl] SSE 조회: lastEventId가 없어 빈 결과 반환");
            return List.of();
        }
        log.debug("[InMemorySseMessageRepositoryImpl] SSE 조회 시작: lastEventId={}", lastEventId);
        List<SseMessage> result = new ArrayList<>();

        for (UUID id : eventIdQueue) {
            if (id.equals(lastEventId)) {
                boolean start = false;
                for (UUID laterId : eventIdQueue) {
                    if (start) {
                        result.add(messages.get(laterId));
                    }
                    if (laterId.equals(id)) {
                        start = true;
                    }
                }
                break;
            }
        }

        log.info(
            "[InMemorySseMessageRepositoryImpl] SSE 조회 완료: lastEventId={}, returned={}, queueSize={}, mapSize={}",
            lastEventId, result.size(), eventIdQueue.size(), messages.size());
        return result;
    }
}
