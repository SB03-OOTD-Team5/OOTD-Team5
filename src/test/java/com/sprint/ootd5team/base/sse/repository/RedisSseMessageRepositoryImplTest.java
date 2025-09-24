package com.sprint.ootd5team.base.sse.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.sse.SseMessage;
import com.sprint.ootd5team.base.sse.repository.message.RedisSseMessageRepositoryImpl;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@DisplayName("RedisSseMessageRepositoryImpl 슬라이스 테스트")
@ExtendWith(MockitoExtension.class)
class RedisSseMessageRepositoryImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zsetOps;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void save_성공() {
        // given
        RedisSseMessageRepositoryImpl repository =
            new RedisSseMessageRepositoryImpl(redisTemplate, mapper);
        given(redisTemplate.opsForZSet()).willReturn(zsetOps);
        given(zsetOps.add(anyString(), anyString(), anyDouble())).willReturn(true);
        given(redisTemplate.expire(anyString(), any(Duration.class))).willReturn(Boolean.TRUE);

        SseMessage msg = new SseMessage("test", "hello");

        // when
        repository.save(msg);

        // then
        verify(zsetOps).add(eq("sse:messages"), anyString(), anyDouble());
    }

    @Test
    void save_직렬화_실패() throws JsonProcessingException {
        // given
        ObjectMapper brokenMapper = mock(ObjectMapper.class);
        RedisSseMessageRepositoryImpl repoWithBrokenMapper =
            new RedisSseMessageRepositoryImpl(redisTemplate, brokenMapper);

        SseMessage msg = new SseMessage("testEvent", "testData");
        given(brokenMapper.writeValueAsString(any(SseMessage.class)))
            .willThrow(new JsonProcessingException("boom") {});

        // when & then
        assertThatThrownBy(() -> repoWithBrokenMapper.save(msg))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("SseMessage 직렬화 실패");
    }

    @Test
    void findAfter_성공() throws Exception {
        // given
        RedisSseMessageRepositoryImpl repository =
            new RedisSseMessageRepositoryImpl(redisTemplate, mapper);
        given(redisTemplate.opsForZSet()).willReturn(zsetOps);

        UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

        SseMessage msg1 = new SseMessage(id1, "event1", "data1");
        SseMessage msg2 = new SseMessage(id2, "event2", "data2");

        String json1 = mapper.writeValueAsString(msg1);
        String json2 = mapper.writeValueAsString(msg2);
        given(zsetOps.range("sse:messages", 0, -1)).willReturn(Set.of(json1, json2));

        // when
        List<SseMessage> result = repository.findAfter(id1);

        // then
        assertThat(result).containsExactly(msg2);
    }

    @Test
    void findAfter_역직렬화_실패() {
        // given
        RedisSseMessageRepositoryImpl repository =
            new RedisSseMessageRepositoryImpl(redisTemplate, mapper);
        UUID lastEventId = UUID.randomUUID();

        given(redisTemplate.opsForZSet()).willReturn(zsetOps);
        given(zsetOps.range("sse:messages", 0, -1)).willReturn(Set.of("invalid-json"));

        // when & then
        assertThatThrownBy(() -> repository.findAfter(lastEventId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("[RedisSseMessageRepositoryImpl] SseMessage 역직렬화 실패");
    }
}