import { apiClient } from './client';
import type {
  ClothesListParams,
  CursorResponse,
  ClothesDto,
  ClothesCreateRequest,
  ClothesUpdateRequest
} from './types';

/**
 * 옷 목록 조회
 */
export const getClothes = async (params: ClothesListParams): Promise<CursorResponse<ClothesDto>> => {
  return apiClient.get<CursorResponse<ClothesDto>>('/api/clothes', { params });
};

/**
 * 옷 등록
 */
export const createClothes = async (
  request: ClothesCreateRequest, 
  image?: File
): Promise<ClothesDto> => {
  const formData = new FormData();

  formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));
  
  if (image) {
    formData.append('image', image);
  }
  
  return apiClient.postFormData<ClothesDto>('/api/clothes', formData);
};

/**
 * 옷 수정
 */
export const updateClothes = async (
  clothesId: string, 
  request: ClothesUpdateRequest, 
  image?: File
): Promise<ClothesDto> => {
  const formData = new FormData();
  formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));
  
  if (image) {
    formData.append('image', image);
  }
  
  return apiClient.patchFormData<ClothesDto>(`/api/clothes/${clothesId}`, formData);
};

/**
 * 옷 삭제
 */
export const deleteClothes = async (clothesId: string): Promise<void> => {
  await apiClient.delete<void>(`/api/clothes/${clothesId}`);
};

/**
 * 구매 링크로 옷 정보 불러오기
 */
export const extractByUrl = async (url: string): Promise<ClothesDto> => {
  return apiClient.get<ClothesDto>('/api/clothes/extractions', { params: { url } });
};
