package com.sprint.ootd5team.domain.directmessage.controller;

import com.sprint.ootd5team.domain.directmessage.service.DirectMessageWsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectMessageWsController {

	private final DirectMessageWsService wsService; // WebSocket기반 비즈니스 로직

	// 클라이언트는 /pub/direct-messages_send 로 JSON 문자열(payload) 전송
	@MessageMapping("/direct-messages_send")
	public void send(String payload) throws Exception {
		log.debug("[Websocket DM Controller]: payload 전송 시작={}", payload);
		wsService.handleSend(payload);
	}
}
