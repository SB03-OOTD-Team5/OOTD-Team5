package com.sprint.ootd5team.domain.follow.service;

import com.sprint.ootd5team.domain.follow.dto.request.FollowerListRequest;
import com.sprint.ootd5team.domain.follow.dto.request.FollowingListRequest;
import com.sprint.ootd5team.domain.follow.dto.response.FollowListResponse;

public interface FollowService {

    FollowListResponse getFollowingList(FollowingListRequest followingListRequest);

    FollowListResponse getFollowerList(FollowerListRequest followerListRequest);
}