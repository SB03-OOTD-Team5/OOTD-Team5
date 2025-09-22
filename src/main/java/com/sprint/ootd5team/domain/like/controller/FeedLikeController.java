package com.sprint.ootd5team.domain.like.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.like.controller.api.FeedLikeApi;
import com.sprint.ootd5team.domain.like.service.FeedLikeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/feeds/")
@Validated
@RequiredArgsConstructor
@RestController
public class FeedLikeController implements FeedLikeApi {

    private final FeedLikeService feedLikeService;
    private final AuthService authService;

    @Override
    @PostMapping("/{feedId}/like")
    public ResponseEntity<Void> like(@PathVariable("feedId") UUID feedId) {
        UUID currentUserId = authService.getCurrentUserId();
        feedLikeService.like(feedId, currentUserId);

        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{feedId}/like")
    public ResponseEntity<Void> unLike(@PathVariable("feedId") UUID feedId) {
        UUID currentUserId = authService.getCurrentUserId();
        feedLikeService.unLike(feedId, currentUserId);

        return ResponseEntity.noContent().build();
    }
}