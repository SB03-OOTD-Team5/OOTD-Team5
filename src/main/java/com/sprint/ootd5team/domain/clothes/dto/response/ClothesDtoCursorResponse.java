package com.sprint.ootd5team.domain.clothes.dto.response;

import java.util.List;

public record ClothesDtoCursorResponse(
    List<ClothesDto> data,
    String nextCursor,
    String nextIdAfter,
    boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

}
