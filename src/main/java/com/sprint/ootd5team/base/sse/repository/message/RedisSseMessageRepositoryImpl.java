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
 * Redis 기반 SSE(Server-Sent Events) 메시지 저장소 구현체
 * <p>
 * Redis Sorted Set(ZSET)을 사용하여 메서지를 저장
 * 메시지는 JSON으로 직렬화되어 값으로 저장됨
 * 키에는 TTL이 적용되며, 저장시마다 TTL이 갱신됨
 */
@Slf4j
@ConditionalOnProperty(name = "spring.data.redis.repositories.enabled", havingValue = "true", matchIfMissing = true)
@Repository
@RequiredArgsConstructor
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
     * 메시지를 Redis ZSET에 저장합니다.
     * <p>
     * SseMessage를 JSON으로 직렬화
     * 현재 시간(밀리초)을 score로 사용하여 ZSET에 추가
     * 키의 TTL을 갱신
     *
     * @param message 저장할 SSE 메시지
     * @throws RuntimeException 메시지 직렬화에 실패한 경우
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
     * 주어진 마지막 이벤트 ID 이후의 메시지를 조회
     * <p>
     * ZSET 전체 범위를 조회하여(JSON 문자열)
     * SseMessage로 역직렬화하고
     * UUID 비교로 lastEventId 보다 큰 메시지만 필터링
     *
     * @param lastEventId 마지막으로 전달된 이벤트의 UUID
     * @return {@code lastEventId} 이후의 메시지 목록(저장 순서 오름차순)
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

        // lastEventId 이후의 id들 가져오기
        Set<String> ids = redisTemplate.opsForZSet().range(SSE_MESSAGES_KEY, rank + 1, -1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        // HASH에서 payload 조회
        List<String> jsons = redisTemplate.opsForHash()
            .multiGet(SSE_PAYLOADS_KEY, new ArrayList<>(ids))
            .stream()
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
