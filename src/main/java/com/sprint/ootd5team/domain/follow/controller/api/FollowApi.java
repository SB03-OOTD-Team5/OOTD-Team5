package com.sprint.ootd5team.domain.follow.controller.api;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.request.FollowCreateRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

public interface FollowApi {

    @Operation(summary = "팔로우 생성")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "팔로우 생성 성공",
            content = @Content(schema = @Schema(implementation = FollowDto.class))
        ),
        @ApiResponse (
            responseCode = "400", description = "팔로우 생성 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<FollowDto> follow(@RequestBody FollowCreateRequest followCreateRequest);

    @Operation(summary = "팔로잉 목록 조회")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", description = "팔로잉 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = FollowListResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "팔로잉 목록 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<FollowListResponse> getFollowings(@ParameterObject @ModelAttribute FollowingListRequest followingListRequest);


    @Operation(summary = "팔로워 목록 조회")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "팔로워 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = FollowListResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "팔로워 목록 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<FollowListResponse> getFollowers(@ParameterObject @ModelAttribute FollowerListRequest followerListRequest);

    @Operation(summary = "팔로우 요약 정보 조회")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "팔로우 요약 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = FollowSummaryDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "팔로우 조회 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<FollowSummaryDto> getSummary(
        @Parameter(description = "userId", required = true) @RequestParam UUID userId
    );
}