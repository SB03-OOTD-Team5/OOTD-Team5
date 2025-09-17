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
        // 직렬화된 JSON을 Redis에 저장할 Serializer 생성
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
