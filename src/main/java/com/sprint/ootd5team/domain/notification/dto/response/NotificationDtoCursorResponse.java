package com.sprint.ootd5team.domain.notification.dto.response;

import java.util.List;

public record NotificationDtoCursorResponse(
    List<NotificationDto> data,
    String nextCursor,
    String nextIdAfter,
    boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

}
