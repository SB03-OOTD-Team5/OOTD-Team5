package com.sprint.ootd5team.domain.follow.service;

import com.sprint.ootd5team.domain.follow.dto.request.FollowListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;

public interface FollowService {

    FollowListResponse getFollowingList(FollowListRequest followListRequest);
}