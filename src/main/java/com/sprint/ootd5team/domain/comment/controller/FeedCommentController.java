package com.sprint.ootd5team.domain.comment.controller;

import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.ootd5team.domain.comment.dto.request.CommentListRequest;
import com.sprint.ootd5team.domain.comment.dto.response.CommentDtoCursorResponse;
import com.sprint.ootd5team.domain.comment.service.FeedCommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
@RestController
public class FeedCommentController {

    private final FeedCommentService feedCommentService;

    @GetMapping("/{feedId}/comments")
    public ResponseEntity<CommentDtoCursorResponse> getComments(
        @PathVariable UUID feedId,
        @Valid @ModelAttribute CommentListRequest commentListRequest
        ) {
        CommentDtoCursorResponse response = feedCommentService.getComments(feedId, commentListRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @PostMapping("/{feedId}/comments")
    public ResponseEntity<CommentDto> create(@Valid @RequestBody CommentCreateRequest commentCreateRequest) {
        CommentDto commentDto = feedCommentService.create(commentCreateRequest);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentDto);
    }
}