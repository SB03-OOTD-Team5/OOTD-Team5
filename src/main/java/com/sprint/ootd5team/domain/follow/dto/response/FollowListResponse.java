package com.sprint.ootd5team.domain.follow.dto.response;

import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import java.util.List;
import java.util.UUID;

public record FollowListResponse(
    List<FollowDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    SortDirection sortDirection
) { }