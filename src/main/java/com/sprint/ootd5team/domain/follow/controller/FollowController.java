package com.sprint.ootd5team.domain.follow.controller;

import com.sprint.ootd5team.domain.follow.controller.api.FollowApi;
import com.sprint.ootd5team.domain.follow.dto.request.FollowListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import com.sprint.ootd5team.domain.follow.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/follows")
@RestController
public class FollowController implements FollowApi {

    private final FollowService followService;

    @Override
    @GetMapping("/followings")
    public ResponseEntity<FollowListResponse> getFollowings(@Valid @ModelAttribute FollowListRequest followListRequest) {
        FollowListResponse followListResponse = followService.getFollowingList(followListRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(followListResponse);
    }
}