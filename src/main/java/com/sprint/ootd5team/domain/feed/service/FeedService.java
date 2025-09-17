package com.sprint.ootd5team.domain.feed.service;

import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import java.util.UUID;

public interface FeedService {

    FeedDtoCursorResponse getFeeds(FeedListRequest request, UUID currentUserId);

    FeedDto getFeed(UUID feedId, UUID currentUserId);

    void delete(UUID feedId);
}