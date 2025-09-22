package com.sprint.ootd5team.base.sse;

import com.sprint.ootd5team.base.security.OotdUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseEmitter(
        @AuthenticationPrincipal OotdUserDetails user,
        @RequestHeader(value = "LastEventId", required = false) UUID lastEventId
    ) {
        return sseService.connect(user.getUserId(), lastEventId);
    }

}
