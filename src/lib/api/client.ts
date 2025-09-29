import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { type  ErrorResponse } from './types';
import {useAuthStore} from "@/lib/stores/useAuthStore";
import {refreshToken} from "@/lib/api/auth";
import {useNavigate} from "react-router-dom";

const BASE_URL = import.meta.env.VITE_PUBLIC_PATH || '/';

export class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    this.client.interceptors.request.use(
      (config) => {
        const token = this.getAccessToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401 && !error.config.url?.includes('/auth/')) {
          try {
            await this.refreshToken();
            const token = this.getAccessToken();
            if (token) {
              error.config.headers.Authorization = `Bearer ${token}`;
              return this.client.request(error.config);
            }
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
          } catch (refreshError) {
            this.clearTokens();
            useNavigate()('/auth/login');
          }
        }
        return Promise.reject(this.handleApiError(error));
      }
    );
  }

  private getAccessToken(): string | null {
    return useAuthStore.getState().getAccessToken();
  }

  private setAccessToken(token: string): void {
    useAuthStore.getState().update({accessToken: token});
  }

  private clearTokens(): void {
    useAuthStore.getState().clear();
  }

  private async refreshToken(): Promise<void> {
    const jwtDto = await refreshToken();
    useAuthStore.getState().update(jwtDto);
  }

  private handleApiError(error: any): Error {
    if (error.response?.data) {
      const errorData = error.response.data as ErrorResponse;
      const message = errorData.message || 'API 요청에 실패했습니다.';
      const customError = new Error(message);
      (customError as any).response = error.response;
      (customError as any).errorData = errorData;
      return customError;
    }
    return error;
  }

  public setAuthToken(token: string): void {
    this.setAccessToken(token);
  }

  public clearAuth(): void {
    this.clearTokens();
  }

  public async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.get(url, config);
    return response.data;
  }

  public async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.post(url, data, config);
    return response.data;
  }

  public async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.put(url, data, config);
    return response.data;
  }

  public async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.patch(url, data, config);
    return response.data;
  }

  public async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.delete(url, config);
    return response.data;
  }

  public async postFormData<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.post(url, formData, {
      ...config,
      headers: {
        ...config?.headers,
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  public async patchFormData<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.patch(url, formData, {
      ...config,
      headers: {
        ...config?.headers,
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }
}

export const apiClient = new ApiClient();