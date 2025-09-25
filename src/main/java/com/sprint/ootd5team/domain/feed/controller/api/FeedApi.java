package com.sprint.ootd5team.domain.feed.controller.api;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface FeedApi {

    @Operation(summary = "피드 등록")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "피드 등록 성공",
            content = @Content(schema = @Schema(implementation = FeedDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 등록 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<FeedDto> create(@RequestBody FeedCreateRequest feedCreateRequest);

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
    ResponseEntity<FeedDtoCursorResponse> getFeeds(@ModelAttribute FeedListRequest feedListRequest);

    @Operation(summary = "피드 수정")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "피드 수정 성공",
            content = @Content(schema = @Schema(implementation = FeedDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 수정 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<FeedDto> update(
        @Parameter(description = "feedId", required = true) @PathVariable UUID feedId,
        @RequestBody FeedUpdateRequest feedUpdateRequest
    );

    @Operation(summary = "피드 삭제")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "피드 삭제 성공"
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 삭제 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Void> delete(
        @Parameter(description = "feedId", required = true) @PathVariable UUID feedId
    );
}