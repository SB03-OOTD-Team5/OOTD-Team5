package com.sprint.ootd5team.domain.feed.repository.feed;

import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.util.List;
import java.util.UUID;

public interface FeedRepositoryCustom {

    List<FeedDto> findFeedDtos(FeedListRequest request, UUID currentUserId);

    FeedDto findFeedDtoById(UUID feedId, UUID currentUserId);

    long countFeeds(String keywordLike, SkyStatus skyStatus, PrecipitationType precipitationType, UUID authorId);
}