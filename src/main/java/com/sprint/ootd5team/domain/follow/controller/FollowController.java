package com.sprint.ootd5team.domain.follow.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.follow.controller.api.FollowApi;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.request.FollowCreateRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.service.FollowService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/follows")
@RestController
public class FollowController implements FollowApi {

    private final FollowService followService;
    private final AuthService authService;

    @Override
    @PostMapping
    public ResponseEntity<FollowDto> follow(@Valid FollowCreateRequest followCreateRequest) {
        FollowDto followDto = followService.follow(followCreateRequest.followerId(), followCreateRequest.followeeId());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(followDto);
    }

    @Override
    @GetMapping("/followings")
    public ResponseEntity<FollowListResponse> getFollowings(@Valid FollowingListRequest followingListRequest) {
        FollowListResponse followListResponse = followService.getFollowingList(followingListRequest);

        return ResponseEntity.ok(followListResponse);
    }

    @Override
    @GetMapping("/followers")
    public ResponseEntity<FollowListResponse> getFollowers(@Valid FollowerListRequest followerListRequest) {
        FollowListResponse followListResponse = followService.getFollowerList(followerListRequest);

        return ResponseEntity.ok(followListResponse);
    }

    @Override
    @GetMapping("/summary")
    public ResponseEntity<FollowSummaryDto> getSummary(UUID userId) {
        UUID currentUserId = authService.getCurrentUserId();

        FollowSummaryDto followSummaryDto = followService.getSummary(userId, currentUserId);

        return ResponseEntity.ok(followSummaryDto);
    }
}