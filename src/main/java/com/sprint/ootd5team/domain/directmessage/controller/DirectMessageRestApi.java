package com.sprint.ootd5team.domain.directmessage.controller;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "DirectMessage", description = "DM 메시지 REST API")
public interface DirectMessageRestApi {

    @Operation(
        summary = "DM 메시지 커서 기반 조회",
        description = "특정 사용자와 주고받은 DM 메시지를 최신순으로 페이지네이션하여 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "메시지 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = DirectMessageDtoCursorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    DirectMessageDtoCursorResponse list(
        @Parameter(
            description = "대화 상대 사용자 ID (UUID)",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        )
        @RequestParam UUID userId,
        @Parameter(
            description = "커서 기준 시각. ISO-8601 혹은 epoch millisecond 문자열",
            example = "2024-09-27T08:41:00Z"
        )
        @RequestParam(required = false) String cursor,
        @Parameter(
            description = "cursor 이전의 마지막 메시지 ID",
            example = "123e4567-e89b-12d3-a456-426614174001"
        )
        @RequestParam(required = false) UUID idAfter,
        @Parameter(
            description = "가져올 최대 항목 수 (1~100)",
            example = "20"
        )
        @RequestParam(defaultValue = "20") int limit
    );
}
