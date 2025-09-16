package com.sprint.ootd5team.base.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // RedisTemplate을 Spring Bean으로 등록
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
        RedisConnectionFactory connectionFactory, // Redis 연결을 생성하는 팩토리
        @Qualifier("redisSerializer") GenericJackson2JsonRedisSerializer redisSerializer // 값을 JSON으로 직렬화할 Serializer
    ) {
        // 새로운 RedisTemplate 생성
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // Redis 연결 팩토리 설정
        template.setConnectionFactory(connectionFactory);

        // Key와 Hash Key를 문자열(String)로 직렬화
        template.setKeySerializer(new StringRedisSerializer()); // Redis 키를 보기 좋게 문자열로 저장
        template.setHashKeySerializer(new StringRedisSerializer()); // Hash 구조의 Key도 문자열로 저장

        // Value와 Hash Value를 JSON으로 직렬화
        template.setValueSerializer(redisSerializer); // 값은 JSON으로 변환해 저장
        template.setHashValueSerializer(redisSerializer); // Hash 구조의 Value도 JSON으로 변환

        // 모든 설정이 끝난 후 초기화
        template.afterPropertiesSet();

        // Bean으로 등록할 RedisTemplate 반환
        return template;
    }

    // Redis에 저장할 때 JSON 직렬화를 담당하는 Serializer Bean 등록
    @Bean("redisSerializer")
    public GenericJackson2JsonRedisSerializer redisSerializer(ObjectMapper objectMapper) {
        // 기존 ObjectMapper 복사 (기존 설정 유지)
        ObjectMapper redisObjectMapper = objectMapper.copy();

        // 다형성 타입 처리 활성화
        redisObjectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance, // 타입 검증자, 모든 서브 타입 허용
            DefaultTyping.EVERYTHING,             // 모든 객체 타입에 대해 타입 정보를 추가
            As.PROPERTY                            // JSON 속성으로 타입 정보를 저장
        );

        // 직렬화된 JSON을 Redis에 저장할 Serializer 생성
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }
}
