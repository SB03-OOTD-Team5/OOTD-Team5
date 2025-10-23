package com.sprint.ootd5team.domain.directmessage.dto.event;

import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;

public record DirectMessageCommittedEvent(
	String destination,          // "/topic/direct-messages_<dmKey>"
	DirectMessageDto payload     // 보낼 DTO
) {}
