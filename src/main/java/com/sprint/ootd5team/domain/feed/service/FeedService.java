package com.sprint.ootd5team.domain.feed.service;

import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import java.util.UUID;

public interface FeedService {

    FeedDto create(FeedCreateRequest request);

    FeedDtoCursorResponse getFeeds(FeedListRequest request, UUID currentUserId);

    FeedDto getFeed(UUID feedId, UUID currentUserId);

    FeedDto update(UUID feedId, FeedUpdateRequest request, UUID currentUserId);

    void delete(UUID feedId);
}