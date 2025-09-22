package com.sprint.ootd5team.domain.user.dto.response;

import com.sprint.ootd5team.domain.user.dto.UserDto;
import java.util.List;
import java.util.UUID;

public record UserDtoCursorResponse(
  List<UserDto> data,
  String nextCursor,
  UUID nextIdAfter,
  boolean hasNext,
  Long totalCount,
  String sortBy,
  String sortDirection
) {

}
