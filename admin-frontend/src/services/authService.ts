import axios from 'axios';

/**
 * Pre-configured Axios instance with JWT interceptor.
 * All API requests should use this instance.
 */
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor — attach JWT token to every outgoing request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — handle 401 Unauthorized globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

/* ===== Auth Types ===== */

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: {
    username: string;
    role: string;
  };
}

/**
 * Decodes the payload section of a JWT (base64url) without verifying the signature.
 * Used to extract `sub` (username) and `role` from the token returned by the backend.
 */
const decodeJwtPayload = (token: string): Record<string, unknown> => {
  try {
    const payload = token.split('.')[1];
    // base64url → base64
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return {};
  }
};

/**
 * Authenticates against the Spring backend: POST /api/v1/auth/login
 * Returns a JWT token along with user info decoded from its payload.
 */
export const loginUser = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  const response = await api.post<{ token: string }>('/v1/auth/login', credentials);
  const { token } = response.data;

  const payload = decodeJwtPayload(token);
  const username = (payload.sub as string) ?? credentials.username;
  const role = (payload.role as string) ?? 'ADMIN';

  return { token, user: { username, role } };
};
