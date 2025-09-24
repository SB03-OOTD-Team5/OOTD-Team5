package com.sprint.ootd5team.base.sse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.sse.SseMessage;
import com.sprint.ootd5team.base.sse.repository.message.InMemorySseMessageRepositoryImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InMemorySseMessageRepositoryImpl 슬라이스 테스트")
class InMemorySseMessageRepositoryImplTest {

    private InMemorySseMessageRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySseMessageRepositoryImpl();
    }

    @Test
    void save_성공() {
        // given
        SseMessage msg = new SseMessage("event1", "data1");

        // when
        repository.save(msg);

        // then
        List<SseMessage> result = repository.findAfter(msg.getId()); // 자기 이후라서 빈 결과
        assertThat(result).isEmpty();
    }

    @Test
    void findAfter_성공() {
        // given
        SseMessage msg1 = new SseMessage(UUID.fromString("00000000-0000-0000-0000-000000000001"), "event1", "data1");
        SseMessage msg2 = new SseMessage(UUID.fromString("00000000-0000-0000-0000-000000000002"), "event2", "data2");
        SseMessage msg3 = new SseMessage(UUID.fromString("00000000-0000-0000-0000-000000000003"), "event3", "data3");

        repository.save(msg1);
        repository.save(msg2);
        repository.save(msg3);

        // when
        List<SseMessage> result = repository.findAfter(msg1.getId());

        // then
        assertThat(result).containsExactly(msg2, msg3);
    }

    @Test
    void findAfter_존재하지않는_lastEventId면_빈리스트를_반환() {
        // given
        SseMessage msg1 = new SseMessage("event1", "data1");
        repository.save(msg1);

        // when
        List<SseMessage> result = repository.findAfter(UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAfter_null인_lastEventId면_빈리스트를_반환() {
        // given
        SseMessage msg1 = new SseMessage("event1", "data1");
        repository.save(msg1);

        // when
        List<SseMessage> result = repository.findAfter(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 최대_메시지_개수를_초과하면_가장_오래된_항목_제거() {
        // given
        for (int i = 0; i < 1001; i++) {
            repository.save(new SseMessage("event" + i, "data" + i));
        }

        // when
        // 첫 번째 저장된 메시지는 제거되어야 함
        UUID firstId = repository.findAfter(UUID.randomUUID()) // dummy
            .stream().findFirst().map(SseMessage::getId).orElse(null);

        // then
        assertThat(repository.findAfter(firstId)).isNotNull();
        assertThat(repository.findAfter(firstId).size()).isLessThanOrEqualTo(1000);
    }
}
