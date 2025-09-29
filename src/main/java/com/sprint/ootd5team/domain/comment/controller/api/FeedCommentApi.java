package com.sprint.ootd5team.domain.comment.controller.api;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.ootd5team.domain.comment.dto.request.CommentListRequest;
import com.sprint.ootd5team.domain.comment.dto.response.CommentDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "피드 관리", description = "피드 관련 API")
public interface FeedCommentApi {

    @Operation(summary = "피드 댓글 조회")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "피드 댓글 조회 성공",
            content = @Content(schema = @Schema(implementation = CommentDtoCursorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 댓글 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<CommentDtoCursorResponse> getComments(
        @Parameter(description = "feedId", required = true) @PathVariable UUID feedId,
        @ParameterObject @ModelAttribute CommentListRequest commentListRequest
    );

    @Operation(summary = "피드 댓글 등록")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "피드 댓글 등록 성공",
            content = @Content(schema = @Schema(implementation = CommentDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "피드 댓글 등록 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<CommentDto> create(
        @Parameter(description = "feedId", required = true) @PathVariable UUID feedId,
        @RequestBody CommentCreateRequest commentCreateRequest
    );
}