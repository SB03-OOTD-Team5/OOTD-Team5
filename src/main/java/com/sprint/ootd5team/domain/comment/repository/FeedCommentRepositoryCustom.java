package com.sprint.ootd5team.domain.comment.repository;

import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FeedCommentRepositoryCustom {

    List<CommentDto> findByFeedIdWithCursor(
        UUID feedId, Instant createdAtCursor, UUID idCursor, int limit
    );
}