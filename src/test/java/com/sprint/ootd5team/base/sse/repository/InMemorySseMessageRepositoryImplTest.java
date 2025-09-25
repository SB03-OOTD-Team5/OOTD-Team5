package com.sprint.ootd5team.base.sse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.sse.SseMessage;
import com.sprint.ootd5team.base.sse.repository.message.InMemorySseMessageRepositoryImpl;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InMemorySseMessageRepositoryImpl 슬라이스 테스트")
class InMemorySseMessageRepositoryImplTest {

    private InMemorySseMessageRepositoryImpl repository;
    private UUID userId;

    @BeforeEach
    void setUp() {
        repository = new InMemorySseMessageRepositoryImpl();
        userId = UUID.randomUUID();
    }

    @Test
    void save_성공() {
        // given
        SseMessage msg = new SseMessage("event1", "data1");

        // when
        repository.save(msg);

        // then
        List<SseMessage> result = repository.findAfter(userId, msg.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void findAfter_성공() {
        // given
        SseMessage msg1 = new SseMessage(UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "event1", "data1");
        SseMessage msg2 = new SseMessage(UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "event2", "data2");
        SseMessage msg3 = new SseMessage(UUID.fromString("00000000-0000-0000-0000-000000000003"),
            "event3", "data3");

        repository.save(msg1);
        repository.save(msg2);
        repository.save(msg3);

        // when
        List<SseMessage> result = repository.findAfter(userId, msg1.getId());

        // then
        assertThat(result).containsExactly(msg2, msg3);
    }

    @Test
    void findAfter_존재하지않는_lastEventId면_빈리스트를_반환() {
        // given
        SseMessage msg1 = new SseMessage("event1", "data1");
        repository.save(msg1);

        // when
        List<SseMessage> result = repository.findAfter(userId, UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAfter_null인_lastEventId면_빈리스트를_반환() {
        // given
        SseMessage msg1 = new SseMessage("event1", "data1");
        repository.save(msg1);

        // when
        List<SseMessage> result = repository.findAfter(userId, null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 최대_메시지_개수를_초과하면_가장_오래된_항목_제거() {
        // given
        SseMessage first = null;
        SseMessage second = null;
        for (int i = 0; i < 1001; i++) {
            SseMessage m = new SseMessage("event" + i, "data" + i);
            if (i == 0) {
                first = m;
            }
            if (i == 1) {
                second = m;
            }
            repository.save(m);
        }

        // then
        // 첫 번째 저장된 메시지는 큐/맵에서 제거되어 더 이상 이후 메시지 조회 기준으로 사용할 수 없음
        assertThat(repository.findAfter(userId, first.getId())).isEmpty();
        // 두 번째 이후로 999개가 남아야 함(총 1000개 보존, 두 번째 이후 = 999)
        assertThat(repository.findAfter(userId, second.getId())).hasSize(999);

    }

    @Test
    void targetUserIds가_null이면_모든사용자가_조회가능() {
        // given
        SseMessage broadcastMsg = new SseMessage("broadcast", "data");
        repository.save(broadcastMsg);

        // when
        List<SseMessage> result = repository.findAfter(userId, broadcastMsg.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void targetUserIds가_포함된_사용자만_조회된다() {
        // given
        UUID targetUser = UUID.randomUUID();
        SseMessage msg = SseMessage.builder()
            .eventName("private")
            .data("secret")
            .targetUserIds(Set.of(targetUser))
            .build();

        repository.save(msg);

        // when
        List<SseMessage> resultForTarget = repository.findAfter(targetUser, msg.getId());
        List<SseMessage> resultForOther = repository.findAfter(userId, msg.getId());

        // then
        assertThat(resultForTarget).isEmpty();
        assertThat(resultForOther).isEmpty();
    }
}
