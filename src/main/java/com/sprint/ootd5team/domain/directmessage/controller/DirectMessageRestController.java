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
public class DirectMessageRestController {

	private final DirectMessageRestService restService;

	@GetMapping
	public DirectMessageDtoCursorResponse list(        // 히스토리 페이지네이션 응답
		@RequestParam UUID userId,                 // 상대방 UUID (receiverId로 사용)
		@RequestParam(required = false) String cursor,   // 마지막 createdAt(ISO-8601 or epochMillis)
		@RequestParam(required = false) UUID idAfter,    // 마지막 메시지 id
		@RequestParam(defaultValue = "20") int limit     // 페이지 크기
	) {
		log.debug("[REST DM Service] DM 메세지 페이지 조회 요청: RecieverId={}",userId);
		return restService.listByPartner(userId, cursor, idAfter, limit);
	}
}
