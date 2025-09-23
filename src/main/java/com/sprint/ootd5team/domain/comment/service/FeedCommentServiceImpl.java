package com.sprint.ootd5team.domain.comment.service;

import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.ootd5team.domain.comment.dto.request.CommentListRequest;
import com.sprint.ootd5team.domain.comment.dto.response.CommentDtoCursorResponse;
import com.sprint.ootd5team.domain.comment.repository.FeedCommentRepository;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FeedCommentServiceImpl implements FeedCommentService {

    private final FeedCommentRepository feedCommentRepository;
    private final FeedRepository feedRepository;

    public CommentDtoCursorResponse getComments(UUID feedId, CommentListRequest request) {
        log.info("[FeedCommentService] 댓글 목록 조회 요청 시작 - feedId={}", feedId);

        if (!feedRepository.existsById(feedId)) {
            log.warn("조회되지 않는 피드입니다. feedId={}", feedId);
            throw FeedNotFoundException.withId(feedId);
        }

        Instant createdAtCursor = request.cursor() != null ? Instant.parse(request.cursor()) : null;
        UUID idCursor = request.idAfter();

        List<CommentDto> comments = feedCommentRepository.findByFeedIdWithCursor(
            feedId,
            createdAtCursor,
            idCursor,
            request.limit()
        );

        boolean hasNext = comments.size() > request.limit();
        if (hasNext) {
            comments = comments.subList(0, request.limit());
        }

        CommentDto last = comments.isEmpty() ? null : comments.get(comments.size() - 1);

        return new CommentDtoCursorResponse(
            comments,
            last != null ? last.createdAt().toString() : null,
            last != null ? last.id() : null,
            hasNext,
            feedCommentRepository.countByFeedId(feedId),
            "createdAt",
            "DESCENDING"
        );
    }

    //Todo
    public CommentDto create(CommentCreateRequest commentCreateRequest) {
        CommentDto commentDto = null;

        return commentDto;
    }
}