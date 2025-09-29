package com.sprint.ootd5team.domain.follow.service;

import com.sprint.ootd5team.domain.follow.dto.data.FollowSummaryDto;
import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;
import java.util.UUID;

public interface FollowService {

    FollowListResponse getFollowingList(FollowingListRequest followingListRequest);

    FollowListResponse getFollowerList(FollowerListRequest followerListRequest);

    FollowSummaryDto getSummary(UUID userId, UUID currentUserId);
}