import { apiClient } from './client';
import type {
  WeatherParams,
  WeatherDto, WeatherAPILocation,
} from './types';

/**
 * 날씨 정보 조회
 */
export const getWeather = async (params: WeatherParams): Promise<WeatherDto[]> => {
  return apiClient.get<WeatherDto[]>('/api/weathers', { params });
};

/**
 * 날씨 위치 정보 조회
 */
export const getWeatherLocation = async (params: WeatherParams): Promise<WeatherAPILocation> => {
  return apiClient.get<WeatherAPILocation>('/api/weathers/location', { params });
};