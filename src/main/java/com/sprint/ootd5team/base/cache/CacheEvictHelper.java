package com.sprint.ootd5team.base.cache;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

/**
 * Redis 캐시 중 특정 prefix(ownerId:)로 시작하는 항목만 제거하는 유틸리티
 * <p>
 * Spring Cache의 @CacheEvict는 와일드카드 키 삭제를 지원하지 않기 때문에
 * SCAN 명령을 이용해 직접 키를 탐색하고 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class CacheEvictHelper {

    private static final String CACHE_NAME = "clothesByUser";
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 특정 ownerId의 clothesByUser 캐시만 제거
     *
     * @param ownerId 캐시 프리픽스 기준 owner UUID
     */
    public void evictClothesByOwner(UUID ownerId) {
        // 전역 prefix 유무/값(예: "ootd:")과 무관하게 매칭되도록 선행 와일드카드 사용
        String pattern = "*" + CACHE_NAME + "::" + ownerId + ":*";
        Set<String> keysToDelete = scanKeys(pattern);

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("[CacheEvictHelper] Removed {} cache entries for ownerId={}",
                keysToDelete.size(), ownerId);
        } else {
            log.debug("[CacheEvictHelper] No cache entries found for ownerId={}", ownerId);
        }
    }

    /**
     * Redis SCAN 명령을 이용해 pattern에 매칭되는 키 목록을 찾는다.
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) (RedisConnection connection) -> {
            try (Cursor<byte[]> cursor = connection.scan(
                ScanOptions.scanOptions().match(pattern).count(500).build()
            )) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                log.error("[CacheEvictHelper] Failed to scan keys for pattern: {}", pattern, e);
                throw new RuntimeException("Failed to scan Redis keys for cache eviction", e);
            }
            return null;
        });
        return keys;
    }

}
