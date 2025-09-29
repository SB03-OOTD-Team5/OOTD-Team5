package com.sprint.ootd5team.domain.comment.service;

import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.ootd5team.domain.comment.dto.request.CommentListRequest;
import com.sprint.ootd5team.domain.comment.dto.response.CommentDtoCursorResponse;
import java.util.UUID;

public interface FeedCommentService {

    CommentDtoCursorResponse getComments(UUID feedId, CommentListRequest request);

    CommentDto create(UUID feedId, CommentCreateRequest commentCreateRequest);
}