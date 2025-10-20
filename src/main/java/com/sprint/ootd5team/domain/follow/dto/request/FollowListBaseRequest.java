package com.sprint.ootd5team.domain.follow.dto.request;

import java.util.UUID;

public interface FollowListBaseRequest {
    String cursor();
    UUID idAfter();
    int limit();
    String nameLike();
}