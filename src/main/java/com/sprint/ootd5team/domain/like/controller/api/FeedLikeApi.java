package com.sprint.ootd5team.domain.like.controller.api;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface FeedLikeApi {

    @Operation(summary = "피드 좋아요")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "피드 좋아요 성공"
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 좋아요 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Void> like(
        @Parameter(description = "feedId", required = true) @PathVariable("feedId") UUID feedId
    );

    @Operation(summary = "피드 좋아요 취소")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "피드 좋아요 취소 성공"
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 좋아요 취소 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Void> unLike(
        @Parameter(description = "feedId", required = true) @PathVariable("feedId") UUID feedId
    );
}