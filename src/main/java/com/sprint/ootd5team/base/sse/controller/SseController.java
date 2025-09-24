package com.sprint.ootd5team.base.sse.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.base.sse.controller.api.SseApi;
import com.sprint.ootd5team.base.sse.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE(Server-Sent Events) 연결을 제공하는 컨트롤러.
 *
 * <p>클라이언트는 {@code /api/sse} 엔드포인트로 접속하며, 응답은
 * {@code text/event-stream} 형식으로 스트리밍. 연결 처리와
 * 과거 이벤트 재전송(옵션)은 서비스 계층에 위임.</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController implements SseApi {

    private final SseService sseService;
    private final AuthService authService;

    public SseEmitter subscribe(UUID lastEventId) {
        UUID userId = authService.getCurrentUserId();
        log.info("[SseController] SSE 구독 요청 수신: userId={}, lastEventId={}", userId, lastEventId);

        SseEmitter emitter = sseService.connect(userId, lastEventId);

        log.debug("[SseController] SSE Emitter 생성 완료: userId={}, emitterHash={}", userId,
            Integer.toHexString(System.identityHashCode(emitter)));
        return emitter;

    }
}
