package com.sprint.ootd5team.domain.feed.controller;

import com.sprint.ootd5team.domain.feed.controller.api.FeedApi;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.service.FeedService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    @GetMapping
    public ResponseEntity<FeedDtoCursorResponse> getFeeds(
        @Valid @ModelAttribute FeedListRequest feedListRequest,
        @RequestParam(required = false) UUID currentUserId
//        @AuthenticationPrincipal OotdUserDetails user
    ) {
//        UUID userid = user.getUserDto().id();
        FeedDtoCursorResponse feeds = feedService.getFeeds(feedListRequest, currentUserId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(feeds);
    }
}