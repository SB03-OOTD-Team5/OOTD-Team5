package com.sprint.ootd5team.domain.follow.controller.api;

import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface FollowApi {

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
}