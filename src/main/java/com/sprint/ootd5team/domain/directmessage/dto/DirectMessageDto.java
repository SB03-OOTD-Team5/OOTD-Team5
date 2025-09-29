package com.sprint.ootd5team.domain.directmessage.dto;

import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DirectMessageDto(
	UUID id,                 // 메시지 UUID를 문자열로 응답한다면 String
	Instant createdAt,
	ParticipantDto sender,
	ParticipantDto receiver,
	String content
) {	// Service가 이미 sender/receiver를 만들어 전달 → DTO는 “조립”만
	public static DirectMessageDto toDto(
		DirectMessage dm,
		ParticipantDto sender,
		ParticipantDto receiver
	) {
		return DirectMessageDto.builder()
			.id(dm.getId())      // DTO 계약이 String이면 toString()
			.createdAt(dm.getCreatedAt())
			.sender(sender)
			.receiver(receiver)
			.content(dm.getContent())
			.build();
	}
}
