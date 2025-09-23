package com.sprint.ootd5team.domain.directmessage.dto;

import java.util.UUID;

public record DirectMessageCreateRequest(
	UUID senderId,     // 송신자 ID
	UUID receiverId,   // 수신자 ID
	String content     // 메시지 내용
) {}