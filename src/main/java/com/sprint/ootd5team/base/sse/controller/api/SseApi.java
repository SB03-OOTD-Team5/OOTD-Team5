package com.sprint.ootd5team.base.sse.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "sse-controller", description = "SSE 구독 API")
public interface SseApi {

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
    SseEmitter subscribe(@RequestHeader(value = "LastEventId", required = false) UUID lastEventId);
}
