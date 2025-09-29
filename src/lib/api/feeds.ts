import { apiClient } from './client';
import type {
  FeedListParams,
  CursorResponse,
  FeedDto,
  FeedCreateRequest,
  FeedUpdateRequest,
  CommentDto,
  CommentCreateRequest, FeedCommentParams
} from './types';

/**
 * 피드 목록 조회
 */
export const getFeedList = async (params: FeedListParams): Promise<CursorResponse<FeedDto>> => {
  return apiClient.get<CursorResponse<FeedDto>>('/api/feeds', { params });
};

/**
 * 피드 등록
 */
export const createFeed = async (request: FeedCreateRequest): Promise<FeedDto> => {
  return apiClient.post<FeedDto>('/api/feeds', request);
};

/**
 * 피드 수정
 */
export const updateFeed = async (feedId: string, request: FeedUpdateRequest): Promise<FeedDto> => {
  return apiClient.patch<FeedDto>(`/api/feeds/${feedId}`, request);
};

/**
 * 피드 삭제
 */
export const deleteFeed = async (feedId: string): Promise<void> => {
  await apiClient.delete<void>(`/api/feeds/${feedId}`);
};

/**
 * 피드 좋아요
 */
export const likeFeed = async (feedId: string): Promise<FeedDto> => {
  return apiClient.post<FeedDto>(`/api/feeds/${feedId}/like`);
};

/**
 * 피드 좋아요 취소
 */
export const unlikeFeed = async (feedId: string): Promise<void> => {
  await apiClient.delete<void>(`/api/feeds/${feedId}/like`);
};

/**
 * 피드 댓글 조회
 */
export const getFeedComments = async (params: FeedCommentParams): Promise<CursorResponse<CommentDto>> => {
  return apiClient.get<CursorResponse<CommentDto>>(`/api/feeds/${params.feedId}/comments`, { params });
};

/**
 * 피드 댓글 등록
 */
export const createFeedComment = async (
  feedId: string, 
  request: CommentCreateRequest
): Promise<CommentDto> => {
  return apiClient.post<CommentDto>(`/api/feeds/${feedId}/comments`, request);
};
