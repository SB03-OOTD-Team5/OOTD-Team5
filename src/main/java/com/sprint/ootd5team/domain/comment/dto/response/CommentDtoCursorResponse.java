package com.sprint.ootd5team.domain.comment.dto.response;

import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import java.util.List;
import java.util.UUID;

public record CommentDtoCursorResponse(
    List<CommentDto> data,
    String nextCursor,
    UUID nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) { }