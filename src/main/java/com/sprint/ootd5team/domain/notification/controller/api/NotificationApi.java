package com.sprint.ootd5team.domain.notification.controller.api;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(
        summary = "알림 목록 조회",
        description = "조건에 맞는 알림 목록을 조회합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = NotificationDtoCursorResponse.class))),
        @ApiResponse(responseCode = "400", description = "알림 목록 조회 실패",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<NotificationDtoCursorResponse> getNotifications(
        @Parameter(description = "커서(createdAt)", example = "2025-09-23T00:00:00Z") @RequestParam(required = false) Instant cursor,
        @Parameter(description = "보조 커서(UUID)") @RequestParam(required = false) UUID idAfter,
        @Parameter(description = "페이지 크기", example = "20") @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) int limit,
        @Parameter(description = "정렬 방향", example = "DESC") @RequestParam(name = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection
    );

    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림을 읽음 처리(삭제)합니다.",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공"),
        @ApiResponse(responseCode = "400", description = "알림 읽음 처리 실패",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{notificationId}")
    ResponseEntity<Void> delete(
        @Parameter(description = "알림 ID") @PathVariable UUID notificationId
    );
}