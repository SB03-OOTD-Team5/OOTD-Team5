import { apiClient } from './client';
import type {
  FollowCreateRequest,
  FollowDto,
  FollowSummaryDto,
  FollowListResponse, FollowingListParam, FollowerListParam
} from './types';

/**
 * 팔로우 생성
 */
export const createFollow = async (request: FollowCreateRequest): Promise<FollowDto> => {
  return apiClient.post<FollowDto>('/api/follows', request);
};

/**
 * 팔로우 취소
 */
export const cancelFollow = async (followId: string): Promise<void> => {
  await apiClient.delete<void>(`/api/follows/${followId}`);
};

/**
 * 팔로우 요약 정보 조회
 */
export const getFollowSummary = async ({userId}: {userId: string}): Promise<FollowSummaryDto> => {
  return apiClient.get<FollowSummaryDto>('/api/follows/summary', { params: { userId } });
};

/**
 * 팔로잉 목록 조회
 */
export const getFollowings = async (params: FollowingListParam): Promise<FollowListResponse> => {
  return apiClient.get<FollowListResponse>('/api/follows/followings', { 
    params
  });
};

/**
 * 팔로워 목록 조회
 */
export const getFollowers = async (params: FollowerListParam): Promise<FollowListResponse> => {
  return apiClient.get<FollowListResponse>('/api/follows/followers', { 
    params
  });
};
