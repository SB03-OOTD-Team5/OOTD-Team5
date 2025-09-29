import { apiClient } from './client';
import type {
  RecommendationParams,
  RecommendationDto
} from './types';

/**
 * 추천 조회
 */
export const getRecommendation = async (params: RecommendationParams): Promise<RecommendationDto> => {
  return apiClient.get<RecommendationDto>('/api/recommendations', { params });
};
