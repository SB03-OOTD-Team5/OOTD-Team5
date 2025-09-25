package com.sprint.ootd5team.domain.comment.controller;

import com.sprint.ootd5team.domain.comment.controller.api.FeedCommentApi;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/feeds")
@RestController
public class FeedCommentController implements FeedCommentApi {

    private final FeedCommentService feedCommentService;

    @Override
    @GetMapping("/{feedId}/comments")
    public ResponseEntity<CommentDtoCursorResponse> getComments(
        UUID feedId,
        @Valid CommentListRequest commentListRequest
        ) {
        CommentDtoCursorResponse response = feedCommentService.getComments(feedId, commentListRequest);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    //ToDo: 추후에 AuthorId를 Dto가 아닌 AuthService에서 받아오도록 수정할 수 있음.
    @Override
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<CommentDto> create(
        UUID feedId,
        @Valid CommentCreateRequest commentCreateRequest
    ) {
        CommentDto commentDto = feedCommentService.create(feedId, commentCreateRequest);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentDto);
    }
}