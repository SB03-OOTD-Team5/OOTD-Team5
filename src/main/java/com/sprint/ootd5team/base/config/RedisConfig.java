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

/**
 * Redis 설정 클래스
 *
 * - Key, Set 값 : StringRedisSerializer (그대로 문자열 저장)
 * - List, Hash 값 : GenericJackson2JsonRedisSerializer (객체 JSON 직렬화)
 *
 * 이렇게 분리해야 토큰 같은 단순 문자열이 "\"token\"" 으로 저장되는 문제를 방지할 수 있음.
 */
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

        // 주의: Set에 단순 문자열(Token) 저장할 경우
        // redisTemplate.opsForSet().add() 시 자동으로 StringRedisSerializer가 적용되도록
        // 아래처럼 기본 ValueSerializer와 별개로 override 가능
        template.setDefaultSerializer(new StringRedisSerializer());

        // 모든 설정이 끝난 후 초기화
        template.afterPropertiesSet();

        // Bean으로 등록할 RedisTemplate 반환
        return template;
    }

    /**
     * Redis에서 사용할 JSON 직렬화기 Bean 등록
     *
     * - 객체를 JSON으로 직렬화/역직렬화
     * - 타입 정보를 함께 저장해서 역직렬화 시 원래 타입을 잃지 않도록 보장
     */
    @Bean("redisSerializer")
    public GenericJackson2JsonRedisSerializer redisSerializer(ObjectMapper objectMapper) {
        // 기존 ObjectMapper를 그대로 쓰지 않고 복사본을 만들어서 Redis 전용 설정 적용
        ObjectMapper redisObjectMapper = objectMapper.copy();

        // 기본 타입 정보를 JSON에 포함시켜서 역직렬화 시 타입 손실 방지
        //   - LaissezFaireSubTypeValidator: 하위 타입 검증기 (거의 모든 타입 허용)
        //   - DefaultTyping.EVERYTHING: 모든 객체 타입에 대해 타입 정보 포함
        //   - As.PROPERTY: JSON 속성에 타입 정보(@class 같은 형태)로 기록
        redisObjectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            DefaultTyping.EVERYTHING,
            As.PROPERTY
        );
        // 직렬화된 JSON을 Redis에 저장할 Serializer 생성
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }
}
