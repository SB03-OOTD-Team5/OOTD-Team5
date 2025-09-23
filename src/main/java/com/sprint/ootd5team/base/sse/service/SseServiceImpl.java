package com.sprint.ootd5team.base.sse.service;

import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.sse.SseMessage;
import com.sprint.ootd5team.base.sse.repository.emitter.SseEmitterRepository;
import com.sprint.ootd5team.base.sse.repository.message.SseMessageRepository;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SEE(Server-Sent Events) 전송을 처리하는 서비스 구현체
 * <p>
 * 클라이언트 연결 생성 및 수명관리
 * 브로드캐스트/개별 대상 이벤트 전송
 * 재연결시 누락된 이벤트 복원
 * 주기적인 무효 Emitter 정리
 * <p>
 * 인메모리 메시지 저장소 사용
 * 재연결시 Last-Event-Id를 기준으로 유실된 이벤트를 복원
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseServiceImpl implements SseService {

    // 기본 Emitter 타임아웃
    private static final long TIMEOUT = 1000L * 60 * 60;

    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRepository sseMessageRepository;
    private final UserRepository userRepository;

    /**
     * SEE 연결을 생성하고, 필요시 유실된 이벤트를 복원
     *
     * @param userId      연결한 사용자 ID
     * @param lastEventId 재연결시 마지막으로 수신한 이벤트 ID(없으면 null)
     * @return 생성된 SseEmitter
     */
    @Override
    public SseEmitter connect(UUID userId, UUID lastEventId) {
        // 사용자 존재 여부 확인
        userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.withId(userId));

        // 타임아웃이 적용된 Emitter 생성 및 저장
        SseEmitter sseEmitter = new SseEmitter(TIMEOUT);
        log.info("[SSE] 연결 등록 - userId={}, emitter={}", userId, sseEmitter);
        sseEmitterRepository.add(userId, sseEmitter);

        // 콜백 등록: 완료/타임아웃/에러 시 정리
        sseEmitter.onCompletion(() -> {
            sseEmitterRepository.remove(userId, sseEmitter);
            log.info("[SSE] 연결 종료 - userId={}", userId);
        });
        sseEmitter.onTimeout(() -> {
            sseEmitterRepository.remove(userId, sseEmitter);
            log.info("[SSE] 연결 타임아웃 - userId={}", userId);
        });
        sseEmitter.onError(e -> {
            sseEmitterRepository.remove(userId, sseEmitter);
            log.warn("[SSE] 연결 에러 - userId={}, error={}", userId, e.getMessage());
        });

        // 즉시 연결 확인용 더미 이벤트(ping) 전송
        try {
            sendToEmitter(sseEmitter, "ping", "connected");
            log.info("[SSE] connect 이벤트 전송 완료 - userId={}", userId);
        } catch (Exception e) {
            log.warn("[SSE] connect 이벤트 전송 실패 - userId={}, error={}", userId, e.getMessage());
        }

        // 재연결 시 유실 이벤트 복원
        if (lastEventId != null) {
            List<SseMessage> missed = sseMessageRepository.findAfter(lastEventId);
            log.info("[SSE] 유실 이벤트 복원 - userId={}, lastEventId={}, 복원 개수={}",
                userId, lastEventId, missed.size());
            missed.forEach(
                m -> sendToEmitter(sseEmitter, m.getEventName(), m.getData(), m.getId()));
        }

        return sseEmitter;
    }

    /**
     * 주기적으로 무효(끊긴) Emitter를 정리합니다.
     * ping 이벤트 전송 실패 시 해당 Emitter를 제거합니다.
     */
    @Scheduled(fixedDelay = 1000 * 60 * 30)
    @Override
    public void cleanUp() {
        sseEmitterRepository.findAll().forEach((userId, emitters) -> {
            int before = emitters.size();
            // ping 실패한 emitter를 제거
            emitters.removeIf(e -> !ping(e));
            int after = emitters.size();
            if (before != after) {
                log.info("[SSE] 만료 emitter 정리 - userId={}, before={}, after={}", userId, before,
                    after);
            }
        });
    }

    /**
     * 모든 사용자에게 이벤트를 브로드캐스트합니다.
     *
     * @param eventName 이벤트 이름
     * @param data      전송할 데이터(직렬화 가능 객체)
     */
    @Override
    public void broadcast(String eventName, Object data) {
        // 메시지 생성 및 저장(재전송 대비)
        SseMessage message = new SseMessage(eventName, data);
        sseMessageRepository.save(message);
        log.info("[SSE] Broadcast 이벤트 전송 - event={}, id={}", eventName, message.getId());

        // 각 사용자별 모든 emitter로 전송
        sseEmitterRepository.findAll().forEach((userId, emitters) -> {
            log.debug("[SSE] Broadcast → userId={}, targets={}", userId, emitters.size());
            emitters.forEach(e -> sendToEmitter(e, eventName, data, message.getId()));
        });
        log.debug("[SSE] 만료 emitter 정리 종료");
    }

    /**
     * 지정한 사용자 집합에게만 이벤트를 전송합니다.
     *
     * @param receiverIds 수신자 사용자 ID 목록
     * @param eventName   이벤트 이름
     * @param data        전송할 데이터
     */
    @Override
    public void send(Collection<UUID> receiverIds, String eventName, Object data) {
        // 메시지 생성 및 저장(재전송 대비)
        SseMessage message = new SseMessage(eventName, data);
        sseMessageRepository.save(message);
        log.info("[SSE] 개별 전송 이벤트 - event={}, id={}, targets={}",
            eventName, message.getId(), receiverIds.size());

        // 대상 사용자들의 emitter에 전송
        for (UUID userId : receiverIds) {
            List<SseEmitter> emitters = sseEmitterRepository.get(userId);
            log.debug("[SSE] Send → userId={}, emitters={}", userId, emitters.size());
            emitters.forEach(e -> sendToEmitter(e, eventName, data, message.getId()));
        }
    }

    /**
     * Emitter가 유효한지 확인하기 위해 ping 이벤트를 전송합니다.
     *
     * @param emitter 검사할 Emitter
     * @return 전송 성공 여부
     */
    private boolean ping(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            return true;
        } catch (Exception e) {
            log.debug("[SSE] ping 실패 - error={}", e.getMessage());
            return false;
        }
    }

    /**
     * 편의 메서드: 랜덤 ID로 이벤트 전송.
     *
     * @param emitter   대상 Emitter
     * @param eventName 이벤트 이름
     * @param data      데이터
     */
    private void sendToEmitter(SseEmitter emitter, String eventName, Object data) {
        sendToEmitter(emitter, eventName, data, UUID.randomUUID());
    }

    /**
     * 지정된 이벤트 ID로 이벤트를 전송합니다.
     *
     * @param emitter 대상 Emitter
     * @param event   이벤트 이름
     * @param data    데이터
     * @param eventId 이벤트 ID(UUID)
     */
    private void sendToEmitter(SseEmitter emitter, String event, Object data, UUID eventId) {
        try {
            emitter.send(SseEmitter.event()
                .id(eventId.toString())
                .name(event)
                .data(data, MediaType.APPLICATION_JSON));
            log.debug("[SSE] 이벤트 전송 성공 - event={}, id={}", event, eventId);
        } catch (Exception e) {
            emitter.completeWithError(e);
            log.warn("[SSE] 이벤트 전송 실패 - event={}, id={}, error={}", event, eventId, e.getMessage());
        }
    }
}
