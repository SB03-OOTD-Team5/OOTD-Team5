import { apiClient } from './client';
import type {
  UserCreateRequest,
  UserDto,
  UserListParams,
  CursorResponse,
  ProfileDto,
  ProfileUpdateRequest,
  ChangePasswordRequest,
  UserRoleUpdateRequest,
  UserLockUpdateRequest
} from './types';

/**
 * 사용자 등록(회원가입)
 */
export const createUser = async (request: UserCreateRequest): Promise<UserDto> => {
  return apiClient.post<UserDto>('/api/users', request);
};

/**
 * 계정 목록 조회
 */
export const getUserList = async (params: UserListParams): Promise<CursorResponse<UserDto>> => {
  return apiClient.get<CursorResponse<UserDto>>('/api/users', { params });
};

/**
 * 프로필 조회
 */
export const getProfile = async ({userId}: { userId: string }): Promise<ProfileDto> => {
  return apiClient.get<ProfileDto>(`/api/users/${userId}/profiles`);
};

/**
 * 프로필 업데이트
 */
export const updateProfile = async (
  userId: string, 
  request: ProfileUpdateRequest, 
  image?: File
): Promise<ProfileDto> => {
  const formData = new FormData();
  formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));
  
  if (image) {
    formData.append('image', image);
  }
  
  return apiClient.patchFormData<ProfileDto>(`/api/users/${userId}/profiles`, formData);
};

/**
 * 비밀번호 변경
 */
export const changePassword = async (
  userId: string, 
  request: ChangePasswordRequest
): Promise<void> => {
  await apiClient.patch<void>(`/api/users/${userId}/password`, request);
};

/**
 * 권한 수정
 */
export const updateRole = async (
  userId: string, 
  request: UserRoleUpdateRequest
): Promise<UserDto> => {
  return apiClient.patch<UserDto>(`/api/users/${userId}/role`, request);
};

/**
 * 계정 잠금 상태 변경
 */
export const updateUserLock = async (
  userId: string, 
  request: UserLockUpdateRequest
): Promise<UserDto> => {
  return apiClient.patch<UserDto>(`/api/users/${userId}/lock`, request);
};
