package com.sprint.ootd5team.base.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@EnableCaching
@Configuration
public class CacheConfig {

    /**
     * Redis Cache 설정 추가
     * @param objectMapper objectmapper
     * @return RedisCacheConfiguration
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        // 기존 ObjectMapper를 그대로 쓰지 않고 복사해서 Redis 전용으로 사용
        ObjectMapper redisObjectMapper = objectMapper.copy();

        // 직렬화 시 다양한 타입 정보를 JSON에 포함시키도록 설정
        // -> 역직렬화할 때 원래 객체 타입을 알 수 있게 됨
        redisObjectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance, // 안전하게 서브타입 허용
            DefaultTyping.EVERYTHING,              // 모든 타입에 대해 적용
            As.PROPERTY                            // 타입 정보를 속성(property)으로 포함
        );

        // Redis 캐시 설정 객체 생성
        return RedisCacheConfiguration.defaultCacheConfig()
            // Value 직렬화 방식 지정 (JSON 직렬화)
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                )
            )
            // 캐시 키 앞에 접두사(oodt:) 붙이기 -> 키 충돌 방지 & 구분하기 좋음
            .prefixCacheNameWith("oodt:")
            // 캐시 만료시간 (TTL) 설정: 600초 (10분)
            .entryTtl(Duration.ofSeconds(600))
            // null 값은 캐싱하지 않도록 설정
            .disableCachingNullValues();
    }

    // ========== DirectMessage Chache ==========
    @Bean
    public Cache<UUID, String> dmUserNameCache() {
        return Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();
    }

    @Bean
    public Cache<UUID, Optional<String>> dmProfileUrlCache() {
        return Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();
    }

    @Bean
    public Cache<String, DirectMessageRoom> dmRoomCache() {
        return Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();
    }

}
