import { apiClient } from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from './types';

export async function login(request: LoginRequest) {
  const response = await apiClient.post<AuthResponse>('/auth/login', request);
  return response.data;
}

export async function register(request: RegisterRequest) {
  const response = await apiClient.post<AuthResponse>('/auth/register', request);
  return response.data;
}
