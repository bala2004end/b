import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 60000, // 60s timeout for AI queries that may take longer
});

// ── Request Interceptor ──────────────────────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('aidb_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response Interceptor ─────────────────────────────────────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const requestUrl = error.config?.url || '';
      // Only redirect to login if the 401 came from a protected endpoint.
      // Avoid redirect loops on the /auth/login endpoint itself.
      const isAuthEndpoint = requestUrl.includes('/auth/login') ||
                             requestUrl.includes('/auth/register');

      if (!isAuthEndpoint) {
        localStorage.removeItem('aidb_token');
        localStorage.removeItem('aidb_user');
        // Use replace so the browser back button doesn't loop back to the 401 page
        window.location.replace('/login');
      }
    }
    return Promise.reject(error);
  }
);

export default api;
