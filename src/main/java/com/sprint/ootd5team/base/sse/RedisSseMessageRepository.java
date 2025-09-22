package com.sprint.ootd5team.base.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "spring.data.redis.repositories.enabled", havingValue = "true", matchIfMissing = true)
@Repository
@RequiredArgsConstructor
public class RedisSseMessageRepository implements SseMessageRepository {

    private static final String KEY = "sse:messages";
    private static final long TTL = 60 * 60; // 1시간 (초 단위)
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(SseMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            long score = System.currentTimeMillis();

            // SortedSet에 저장
            redisTemplate.opsForZSet().add(KEY, json, score);
            // TTL 설정
            redisTemplate.expire(KEY, Duration.ofSeconds(TTL));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("SseMessage 직렬화 실패", e);
        }
    }

    @Override
    public List<SseMessage> findAfter(UUID lastEventId) {
        try {
            // 전체 메시지를 가져와서 lastEventId 이후만 필터링
            Set<String> jsonMessages = redisTemplate.opsForZSet()
                .range(KEY, 0, -1);

            if (jsonMessages == null) {
                return List.of();
            }

            List<SseMessage> messages = new ArrayList<>();
            for (String json : jsonMessages) {
                SseMessage m = objectMapper.readValue(json, SseMessage.class);
                if (m.getId().compareTo(lastEventId) > 0) {
                    messages.add(m);
                }
            }
            return messages;
        } catch (Exception e) {
            throw new RuntimeException("SseMessage 역직렬화 실패", e);
        }
    }
}
