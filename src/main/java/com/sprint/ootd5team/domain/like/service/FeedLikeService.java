package com.sprint.ootd5team.domain.like.service;

import java.util.UUID;

public interface FeedLikeService {

    void like(UUID feedId, UUID currentUserId);

    void unLike(UUID feedId, UUID currentUserId);
}