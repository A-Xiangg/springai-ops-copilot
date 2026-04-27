const ACCESS_TOKEN_KEY = 'ops-copilot-access-token';
const USER_KEY = 'ops-copilot-user';

export interface AuthUser {
  userId: number;
  username: string;
  nickname: string | null;
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function clearAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
}

export function getAuthUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export function setAuthUser(user: AuthUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuthUser() {
  localStorage.removeItem(USER_KEY);
}

export function isAuthenticated() {
  return Boolean(getAccessToken());
}

export function clearAuthState() {
  clearAccessToken();
  clearAuthUser();
}
