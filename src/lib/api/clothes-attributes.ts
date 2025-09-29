import { apiClient } from './client';
import type {
  ClothesAttributeDefListParams,
  ClothesAttributeDefDto,
  ClothesAttributeDefCreateRequest,
  ClothesAttributeDefUpdateRequest
} from './types';

/**
 * 의상 속성 정의 목록 조회
 */
export const getClothesAttributeDef = async (
  params: ClothesAttributeDefListParams
): Promise<ClothesAttributeDefDto[]> => {
  return apiClient.get<ClothesAttributeDefDto[]>('/api/clothes/attribute-defs', { params });
};

/**
 * 의상 속성 정의 등록
 */
export const createClothesAttributeDef = async (
  request: ClothesAttributeDefCreateRequest
): Promise<ClothesAttributeDefDto> => {
  return apiClient.post<ClothesAttributeDefDto>('/api/clothes/attribute-defs', request);
};

/**
 * 의상 속성 정의 수정
 */
export const updateClothesAttributeDef = async (
  definitionId: string, 
  request: ClothesAttributeDefUpdateRequest
): Promise<ClothesAttributeDefDto> => {
  return apiClient.patch<ClothesAttributeDefDto>(`/api/clothes/attribute-defs/${definitionId}`, request);
};

/**
 * 의상 속성 정의 삭제
 */
export const deleteClothesAttributeDef = async (definitionId: string): Promise<void> => {
  await apiClient.delete<void>(`/api/clothes/attribute-defs/${definitionId}`);
};
