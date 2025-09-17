package com.sprint.ootd5team.domain.feed.repository.feedClothes;

import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FeedClothesRepositoryCustom {
    Map<UUID, List<OotdDto>> findOotdsByFeedIds(List<UUID> feedIds);
}