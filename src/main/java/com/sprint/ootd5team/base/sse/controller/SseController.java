package com.sprint.ootd5team.base.sse.controller;

import com.sprint.ootd5team.base.security.OotdUserDetails;
import com.sprint.ootd5team.base.sse.service.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
public class SseController {

    private final SseService sseService;

    /**
     * SSE 구독을 시작하고 SseEmitter를 반환
     *
     * @param user        인증된 사용자 정보(필수)
     * @param lastEventId 재연결 시 마지막으로 수신한 이벤트의 UUID(옵션)
     * @return 서버가 이벤트를 푸시할 SseEmitter
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseEmitter(
        @AuthenticationPrincipal OotdUserDetails user,
        @RequestHeader(value = "LastEventId", required = false) UUID lastEventId
    ) {
        UUID userId = user.getUserId();
        log.info("[SseController] SSE 구독 요청 수신: userId={}, lastEventId={}", userId, lastEventId);

        SseEmitter emitter = sseService.connect(userId, lastEventId);

        log.debug("[SseController] SSE Emitter 생성 완료: userId={}, emitterHash={}", userId,
            Integer.toHexString(System.identityHashCode(emitter)));
        return emitter;

    }

}
