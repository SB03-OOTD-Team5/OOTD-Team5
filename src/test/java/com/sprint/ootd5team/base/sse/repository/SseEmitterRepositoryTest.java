package com.sprint.ootd5team.base.sse.repository;

import com.sprint.ootd5team.base.sse.repository.emitter.SseEmitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SseEmitterRepository 슬라이스 테스트")
class SseEmitterRepositoryTest {

    private SseEmitterRepository repository;
    private UUID userId;

    @BeforeEach
    void setUp() {
        repository = new SseEmitterRepository();
        userId = UUID.randomUUID();
    }

    @Test
    void Emitter_추가_및_조회성공() {
        // given
        SseEmitter emitter = new SseEmitter();

        // when
        repository.add(userId, emitter);
        List<SseEmitter> result = repository.get(userId);

        // then
        assertThat(result).containsExactly(emitter);
    }

    @Test
    void 존재하지_않는_사용자_조회시_빈_리스트_반환() {
        // when
        List<SseEmitter> result = repository.get(UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void Emitter_제거_후_비워지면_사용자키도_제거() {
        // given
        SseEmitter emitter = new SseEmitter();
        repository.add(userId, emitter);
        assertThat(repository.get(userId)).hasSize(1);

        // when
        repository.remove(userId, emitter);

        // then
        assertThat(repository.get(userId)).isEmpty();
        assertThat(repository.findAll()).doesNotContainKey(userId);
    }

    @Test
    void Emitter_다수존재시_추가_후_하나만_남기고_제거() {
        // given
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();
        repository.add(userId, emitter1);
        repository.add(userId, emitter2);

        // when
        repository.remove(userId, emitter1);

        // then
        List<SseEmitter> remaining = repository.get(userId);
        assertThat(remaining).containsExactly(emitter2);
        assertThat(repository.findAll()).containsKey(userId);
    }

    @Test
    void 사용자가_존재하지_않으면_제거요청에서_무시() {
        // when
        repository.remove(UUID.randomUUID(), new SseEmitter());

        // then (no exception, repository still empty)
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void 전체_사용자와_Emitter_매핑_조회() {
        // given
        SseEmitter emitter = new SseEmitter();
        repository.add(userId, emitter);

        // when
        Map<UUID, List<SseEmitter>> all = repository.findAll();

        // then
        assertThat(all).containsKey(userId);
        assertThat(all.get(userId)).containsExactly(emitter);
    }
}
