package com.sprint.ootd5team.domain.directmessage.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DirectMessageDtoCursorResponse (
	List<DirectMessageDto> data, // 메시지 목록
	String nextCursor,               // 커서 문자열(미사용이면 null)
	UUID nextIdAfter,                // 다음 요청용 id 커서
	boolean hasNext,                 // 다음 페이지 존재 여부
	long totalCount,                  // 총 개수
	String sortBy,                   // "createdAt"
	String sortDirection            // "ASCENDING" | "DESCENDING"
){}
