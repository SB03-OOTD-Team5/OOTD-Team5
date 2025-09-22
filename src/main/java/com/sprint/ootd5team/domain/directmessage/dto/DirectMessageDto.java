package com.sprint.ootd5team.domain.directmessage.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DirectMessageDto(
	UUID id,
	Instant createdAt,
	UUID senderId,      // 송신자 정보
	String senderName,
	UUID receiverId,    // 수신자 정보
	String receiverName,
	String content      // 메시지 본문
) {}