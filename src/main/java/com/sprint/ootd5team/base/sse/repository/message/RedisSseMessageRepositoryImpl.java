package com.sprint.ootd5team.base.sse.repository.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.sse.SseMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * <pre>
 * Redis 기반 SSE(Server-Sent Events) 메시지 저장소 구현체
 *
 * - 메시지 영속화를 위해 두가지 Redis 자료구조 사용
 *  1. Sorted Set(ZSET): 메시지 ID를 Score(저장시각, epoch millis) 기준으로 정렬해 저장</li>
 *  2. Hash: 메시지 Id -> 직렬화된 JSON 본문을 매핑</li>
 *
 * 이 방식으로 각 메시지의 전송 순서 보장하고, TTL을 적용하여 일정 시간 후 자동 정리
 * TTL은 메시지 저장 시마다 갱신
 *
 * - 브로드캐스트 메시지와 개별 대상 메시지 모두 저장 가능</li>
 * - 재연결 시 {@code Last-Event-ID} 이후의 메시지를 순서대로 복원</li>
 * - 개별 대상 메시지는 {@code targetUserIds} 를 기준으로 수신자 필터링</li>
 * <pre>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ootd.sse.repository", havingValue = "redis")
public class RedisSseMessageRepositoryImpl implements SseMessageRepository {

    // 정렬 관리용 ZSET(id만)
    private static final String SSE_MESSAGES_KEY = "sse:messages";

    // 본문 저장용 HASH(id -> Hash 매핑)
    private static final String SSE_PAYLOADS_KEY = "sse:payloads";

    // 키 만료 시간(초). 기본값 1시간
    private static final long TTL = 60 * 60;

    // 문자열 기반 Redis 연산 템플릿. Sorted Set(ZSET) 연산에 사용
    private final StringRedisTemplate redisTemplate;

    // SseMessage 직렬화/역직렬화에 사용되는 Jackson ObjectMapper
    private final ObjectMapper objectMapper;

    /**
     * 메시지를 Redis에 저장
     *
     * <p>저장 로직:
     * <ol>
     *   <li>ZSET({@code sse:messages})에 메시지 ID를 현재 시각(millis)을 score로 하여 추가</li>
     *   <li>HASH({@code sse:payloads})에 메시지 ID → 직렬화된 JSON 본문을 저장</li>
     *   <li>두 키 모두 TTL(기본 1시간)을 갱신</li>
     * </ol>
     *
     * @param message 저장할 SSE 메시지
     * @throws RuntimeException 메시지를 JSON 직렬화할 수 없는 경우
     */
    @Override
    public void save(SseMessage message) {
        try {
            String id = message.getId().toString();
            log.debug("[RedisSseMessageRepositoryImpl] SSE 메시지 저장 시도: id={}, event={}",
                message.getId(), message.getEventName());
            String json = objectMapper.writeValueAsString(message);
            long score = System.currentTimeMillis();

            // ZSET에는 id만 저장
            redisTemplate.opsForZSet().add(SSE_MESSAGES_KEY, id, score);
            redisTemplate.expire(SSE_MESSAGES_KEY, Duration.ofSeconds(TTL));

            // HASH에는 payload 저장
            redisTemplate.opsForHash().put(SSE_PAYLOADS_KEY, id, json);
            redisTemplate.expire(SSE_PAYLOADS_KEY, Duration.ofSeconds(TTL));

            log.info("[RedisSseMessageRepositoryImpl] 저장 완료: id={}, score={}", id, score);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("SseMessage 직렬화 실패", e);
        }
    }


    /**
     * 지정된 사용자 기준으로, {@code lastEventId} 이후의 메시지를 조회
     *
     * <p>조회 로직:
     * <ol>
     *   <li>ZSET에서 {@code lastEventId} 의 rank를 조회</li>
     *   <li>해당 rank 이후의 모든 ID를 오름차순으로 가져옴</li>
     *   <li>HASH에서 각 ID에 대한 JSON 본문을 조회</li>
     *   <li>JSON을 {@link SseMessage} 로 역직렬화</li>
     *   <li>{@code targetUserIds} 가 지정된 메시지는 userId 포함 여부로 필터링</li>
     * </ol>
     *
     * @param userId      조회 요청 사용자 ID
     * @param lastEventId 마지막으로 수신한 이벤트 ID(UUID)
     * @return {@code lastEventId} 이후 사용자가 수신할 수 있는 SSE 메시지 목록
     * @throws RuntimeException 메시지 역직렬화에 실패한 경우
     */
    @Override
    public List<SseMessage> findAfter(UUID userId, UUID lastEventId) {
        if (lastEventId == null) {
            return List.of();
        }

        // lastEventId의 위치 찾기
        Long rank = redisTemplate.opsForZSet().rank(SSE_MESSAGES_KEY, lastEventId.toString());
        if (rank == null) {
            return List.of();
        }

        // lastEventId 이후의 id들 가져오기 (Redis가 순서를 보장함)
        Set<String> raw = redisTemplate.opsForZSet().range(SSE_MESSAGES_KEY, rank + 1, -1);
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        // HASH에서 payload 조회 (Collection<Object> 캐스팅 필요)
        List<Object> payloads = redisTemplate.opsForHash()
            .multiGet(SSE_PAYLOADS_KEY, new ArrayList<>(raw));

        List<String> jsons = payloads.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .toList();

        return jsons.stream()
            .map(json -> {
                try {
                    return objectMapper.readValue(json, SseMessage.class);
                } catch (Exception e) {
                    throw new RuntimeException("SseMessage 역직렬화 실패", e);
                }
            })
            .filter(m -> m.getTargetUserIds() == null || m.getTargetUserIds().contains(userId))
            .toList();
    }
}
