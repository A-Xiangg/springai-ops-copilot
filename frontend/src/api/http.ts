import axios from 'axios';
import { message } from 'ant-design-vue';
import { clearAuthState, getAccessToken } from '../auth/auth';

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearAuthState();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }

    const errorMessage =
      error?.response?.data?.message ??
      error?.message ??
      '请求失败，请稍后重试';

    message.error(errorMessage);
    return Promise.reject(error);
  },
);
