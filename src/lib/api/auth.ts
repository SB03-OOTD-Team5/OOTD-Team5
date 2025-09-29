import { apiClient } from './client';
import type {
  SignInRequest, 
  ResetPasswordRequest, 
  JwtDto 
} from './types';

/**
 * 로그인
 */
export const signIn = async (request: SignInRequest): Promise<JwtDto> => {
  const formData = new FormData();
  formData.append('username', request.username);
  formData.append('password', request.password);
  
  return apiClient.postFormData<JwtDto>('/api/auth/sign-in', formData);
};

/**
 * 로그아웃
 */
export const signOut = async (): Promise<void> => {
  await apiClient.post<void>('/api/auth/sign-out');
};

/**
 * 토큰 재발급
 */
export const refreshToken = async (): Promise<JwtDto> => {
  return apiClient.post<JwtDto>('/api/auth/refresh');
};

/**
 * 비밀번호 초기화
 */
export const resetPassword = async (request: ResetPasswordRequest): Promise<void> => {
  await apiClient.post<void>('/api/auth/reset-password', request);
};

/**
 * CSRF 토큰 조회
 */
export const getCsrfToken = async (): Promise<void> => {
  await apiClient.get<void>('/api/auth/csrf-token');
};
