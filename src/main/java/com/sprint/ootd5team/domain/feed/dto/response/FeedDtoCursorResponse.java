package com.sprint.ootd5team.domain.feed.dto.response;

import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import java.util.List;
import java.util.UUID;

public record FeedDtoCursorResponse(
    List<FeedDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    String sortDirection
) {

    public static FeedDtoCursorResponse empty(String sortBy, String sortDirection) {
        return new FeedDtoCursorResponse(
            List.of(),
            null,
            null,
            false,
            0L,
            sortBy,
            sortDirection
        );
    }
}