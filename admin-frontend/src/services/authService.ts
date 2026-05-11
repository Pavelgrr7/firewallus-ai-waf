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

/* ===== Mock Authentication Service ===== */

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
 * Mock login — accepts any credentials and returns a fake JWT.
 * In production, this would call a real backend endpoint.
 */
export const loginUser = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  // Simulate network latency
  await new Promise((resolve) => setTimeout(resolve, 800));

  // Accept any credentials for demonstration
  const fakeToken =
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
    btoa(JSON.stringify({ sub: credentials.username, role: 'admin', iat: Date.now() })) +
    '.mock-signature';

  return {
    token: fakeToken,
    user: {
      username: credentials.username,
      role: 'admin',
    },
  };
};
