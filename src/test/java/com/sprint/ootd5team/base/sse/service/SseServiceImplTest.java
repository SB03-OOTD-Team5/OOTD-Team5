package com.sprint.ootd5team.base.sse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.sse.SseMessage;
import com.sprint.ootd5team.base.sse.repository.emitter.SseEmitterRepository;
import com.sprint.ootd5team.base.sse.repository.message.SseMessageRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseService 단위 테스트")
class SseServiceImplTest {

    @Mock
    private SseEmitterRepository emitterRepository;

    @Mock
    private SseMessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SseServiceImpl sseService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void Connect_존재하지_않는_사용자면_에러_발생() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sseService.connect(userId, null))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void Connect_성공시_Emitter반환_repository등록() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));

        // when
        SseEmitter emitter = sseService.connect(userId, null);

        // then
        assertThat(emitter).isNotNull();
        then(emitterRepository).should().add(eq(userId), any(SseEmitter.class));
    }

    @Test
    void Connect_lastEventId가_있으면_유실이벤트를_복원() {
        // given
        UUID lastEventId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
        given(messageRepository.findAfter(eq(userId), eq(lastEventId)))
            .willReturn(List.of(new SseMessage("test", "data")));

        // when
        SseEmitter emitter = sseService.connect(userId, lastEventId);

        // then
        assertThat(emitter).isNotNull();
        then(messageRepository).should().findAfter(eq(userId), eq(lastEventId));
    }

    @Test
    void Connect_ping전송_Emitter반환() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));

        // when
        SseEmitter emitter = sseService.connect(userId, null);

        // then
        assertThat(emitter).isNotNull();
    }

    @Test
    void broadcast_성공() {
        // given
        willDoNothing().given(messageRepository).save(any(SseMessage.class));

        Map<UUID, List<SseEmitter>> allEmitters = new HashMap<>();
        allEmitters.put(userId, List.of(new SseEmitter(1000L)));
        given(emitterRepository.findAll()).willReturn(allEmitters);

        // when
        sseService.broadcast("event", "data");

        // then
        then(messageRepository).should().save(any(SseMessage.class));
        then(emitterRepository).should().findAll();
    }

    @Test
    void broadcast_emitter전송실패시_completeWithError호출() throws Exception {
        // given
        willDoNothing().given(messageRepository).save(any(SseMessage.class));
        SseEmitter brokenEmitter = spy(new SseEmitter(1000L));
        doThrow(new IOException("fail"))
            .when(brokenEmitter).send(any(SseEmitter.SseEventBuilder.class));

        Map<UUID, List<SseEmitter>> allEmitters = new HashMap<>();
        allEmitters.put(userId, List.of(brokenEmitter));
        given(emitterRepository.findAll()).willReturn(allEmitters);

        // when
        sseService.broadcast("event", "data");

        // then
        then(messageRepository).should().save(any(SseMessage.class));
        then(messageRepository).should().save(any(SseMessage.class));
        verify(brokenEmitter).completeWithError(any(Exception.class));
    }

    @Test
    void send_성공() {
        // given
        willDoNothing().given(messageRepository).save(any(SseMessage.class));

        List<SseEmitter> emitters = List.of(new SseEmitter(1000L));
        given(emitterRepository.get(userId)).willReturn(emitters);

        // when
        sseService.send(List.of(userId), "event", "data");

        // then
        verify(messageRepository).save(any(SseMessage.class));
    }

    @Test
    void send_여러수신자에게_전송성공() {
        // given
        willDoNothing().given(messageRepository).save(any(SseMessage.class));
        UUID userId2 = UUID.randomUUID();

        given(emitterRepository.get(userId)).willReturn(List.of(new SseEmitter(1000L)));
        given(emitterRepository.get(userId2)).willReturn(List.of(new SseEmitter(1000L)));

        // when
        sseService.send(List.of(userId, userId2), "event", "data");

        // then
        then(messageRepository).should().save(any(SseMessage.class));
        then(emitterRepository).should().get(eq(userId));
        then(emitterRepository).should().get(eq(userId2));
    }

    @Test
    void cleanUp_ping성공시_emitter유지() {
        // given
        SseEmitter aliveEmitter = new SseEmitter(1000L);
        Map<UUID, List<SseEmitter>> allEmitters = new HashMap<>();
        allEmitters.put(userId, new ArrayList<>(List.of(aliveEmitter)));

        given(emitterRepository.findAll()).willReturn(allEmitters);

        // when
        sseService.cleanUp();

        // then
        assertThat(allEmitters.get(userId)).isNotEmpty();
    }

    @Test
    void cleanUp_ping에_실패한_emitter를_제거() throws IOException {
        // given
        SseEmitter brokenEmitter = spy(new SseEmitter(1000L));
        doThrow(new IOException("fail")).when(brokenEmitter)
            .send(any(SseEmitter.SseEventBuilder.class));

        Map<UUID, List<SseEmitter>> allEmitters = new HashMap<>();
        allEmitters.put(userId, new ArrayList<>(List.of(brokenEmitter)));
        given(emitterRepository.findAll()).willReturn(allEmitters);

        // when
        sseService.cleanUp();

        // then
        then(emitterRepository).should().remove(eq(userId), eq(brokenEmitter));
    }
}
