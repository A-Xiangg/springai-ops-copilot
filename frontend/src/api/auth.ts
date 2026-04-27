import { http } from './http';

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResult {
  userId: number;
  username: string;
  nickname: string | null;
  token: string;
  loginAt: string;
}

export interface CurrentUserResult {
  userId: number;
  username: string;
  nickname: string | null;
  loginAt: string;
}

export function login(payload: LoginPayload) {
  return http.post<LoginResult>('/auth/login', payload).then((response) => response.data);
}

export function getCurrentUser() {
  return http.get<CurrentUserResult>('/auth/me').then((response) => response.data);
}

export function logout() {
  return http.post('/auth/logout').then((response) => response.data);
}
