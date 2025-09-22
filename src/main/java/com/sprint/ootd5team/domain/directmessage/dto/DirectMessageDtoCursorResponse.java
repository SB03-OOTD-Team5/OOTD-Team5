package com.sprint.ootd5team.domain.directmessage.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DirectMessageDtoCursorResponse(
	List<DirectMessageDto> data, // 메시지 목록
	String nextCursor,           // 다음 페이지 커서 값
	UUID nextIdAfter,            // 다음 메시지 ID 기준 커서
	boolean hasNext,             // 다음 페이지 여부
	long totalCount,             // 전체 메시지 수
	String sortBy,               // 정렬 기준 (예: createdAt)
	String sortDirection         // 정렬 방향 (ASC / DESC)
) {}