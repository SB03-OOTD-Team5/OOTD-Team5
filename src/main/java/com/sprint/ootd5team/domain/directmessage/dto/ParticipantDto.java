package com.sprint.ootd5team.domain.directmessage.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ParticipantDto (
	UUID userId,             // 사용자 UUID
	String name,             // 사용자명 (없으면 "탈퇴한 사용자")
	String profileImageUrl  // 프로필 이미지 URL (없으면 null)
){}