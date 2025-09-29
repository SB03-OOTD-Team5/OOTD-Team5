package com.sprint.ootd5team.domain.directmessage.controller;

import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import com.sprint.ootd5team.domain.directmessage.service.DirectMessageRestService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageRestController implements DirectMessageRestApi {

    private final DirectMessageRestService restService;

    @GetMapping
    @Override
    public DirectMessageDtoCursorResponse list(
        @RequestParam UUID userId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) UUID idAfter,
        @RequestParam(defaultValue = "20") int limit
    ) {
        log.debug("[REST DM Service] DM 메세지 페이지 조회 요청: RecieverId={}", userId);
        return restService.listByPartner(userId, cursor, idAfter, limit);
    }
}
