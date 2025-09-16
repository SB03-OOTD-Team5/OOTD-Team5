package com.sprint.ootd5team.domain.feed.controller.api;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface FeedApi {

    @Operation(summary = "피드 목록 조회")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "피드 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = FeedDtoCursorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 목록 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<FeedDtoCursorResponse> getFeeds(FeedListRequest feedListRequest, UUID currentUserId);
//    ResponseEntity<FeedDtoCursorResponse> getFeeds(FeedListRequest feedListRequest, OotdUserDetails user);
}