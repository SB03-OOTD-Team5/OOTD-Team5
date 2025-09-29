import { apiClient } from './client';
import type {
  CursorParams,
  CursorResponse,
  NotificationDto
} from './types';

/**
 * 알림 목록 조회
 */
export const getNotifications = async (params: CursorParams): Promise<CursorResponse<NotificationDto>> => {
  return apiClient.get<CursorResponse<NotificationDto>>('/api/notifications', { params });
};

/**
 * 알림 읽음 처리
 */
export const readNotification = async (notificationId: string): Promise<void> => {
  await apiClient.delete<void>(`/api/notifications/${notificationId}`);
};
