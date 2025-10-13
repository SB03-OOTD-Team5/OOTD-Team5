package com.sprint.ootd5team.base.sse.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.base.sse.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
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
@Tag(name = "sse-controller", description = "SSE 구독 API")
public class SseController {

    private final SseService sseService;
    private final AuthService authService;

    @Operation(
        summary = "Subscribe SSE",
        description = """
            서버로부터 Server-Sent Events(SSE)를 구독합니다.
            - 클라이언트는 `text/event-stream` 응답을 지속적으로 수신합니다.
            - 재연결 시 `LastEventId` 값을 전달하면 누락된 이벤트를 복원할 수 있습니다.
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "SSE 스트림 시작",
                content = @Content(
                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    schema = @Schema(type = "string", example = """
                        id: 550e8400-e29b-41d4-a716-446655440000
                        event: ping
                        data: connected
                        """
                    )
                )
            )
        }
    )
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
        @RequestHeader(value = "Last-Event-ID", required = false) UUID lastEventId
    ) {
        UUID userId = authService.getCurrentUserId();
        log.info("[SseController] SSE 구독 요청 수신: userId={}, lastEventId={}", userId, lastEventId);

        SseEmitter emitter = sseService.connect(userId, lastEventId);

        log.debug("[SseController] SSE Emitter 생성 완료: userId={}, emitterHash={}", userId,
            Integer.toHexString(System.identityHashCode(emitter)));
        return emitter;

    }

    @Profile("test")
    @GetMapping(value = "/test-error", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testError() {
        throw new RuntimeException("테스트용 에러 발생!");
    }
}
