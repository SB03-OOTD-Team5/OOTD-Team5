package com.sprint.ootd5team.domain.feed.controller;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.feed.controller.api.FeedApi;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.service.FeedService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
@RestController
public class FeedController implements FeedApi {

    private final FeedService feedService;
    private final AuthService authService;

    @Override
    @PostMapping
    public ResponseEntity<FeedDto> create(@Valid @RequestBody FeedCreateRequest feedCreateRequest) {
        UUID userId = authService.getCurrentUserId();
        FeedDto feedDto = feedService.create(feedCreateRequest, userId);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(feedDto);
    }

    @Override
    @GetMapping
    public ResponseEntity<FeedDtoCursorResponse> getFeeds(@Valid @ModelAttribute FeedListRequest feedListRequest) {
        UUID userId = authService.getCurrentUserId();
        FeedDtoCursorResponse feeds = feedService.getFeeds(feedListRequest, userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(feeds);
    }

    @GetMapping("/{feedId}")
    public ResponseEntity<FeedDto> getFeed(
        @PathVariable UUID feedId,
        @RequestParam(required = false) UUID currentUserId
    ) {
        FeedDto feed = feedService.getFeed(feedId, currentUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(feed);
    }

    @Override
    @PatchMapping(path = "/{feedId}")
    public ResponseEntity<FeedDto> update(
        @PathVariable UUID feedId,
        @Valid @RequestBody FeedUpdateRequest feedUpdateRequest
    ) {
        UUID userId = authService.getCurrentUserId();
        FeedDto updatedFeedDto = feedService.update(feedId, feedUpdateRequest, userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedFeedDto);
    }

    //TODO: 사용자 접근 권한 추가
    @Override
    @DeleteMapping(path = "/{feedId}")
    public ResponseEntity<Void> delete(@PathVariable UUID feedId) {
        feedService.delete(feedId);

        return ResponseEntity.noContent().build();
    }
}