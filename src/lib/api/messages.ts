import {apiClient} from './client';
import type {CursorResponse, DirectMessageDto, DirectMessageParams} from './types';

/**
 * DM 목록 조회
 */
export const getDms = async (params: DirectMessageParams): Promise<CursorResponse<DirectMessageDto>> => {
  return apiClient.get<CursorResponse<DirectMessageDto>>('/api/direct-messages', { 
    params
  });
};
