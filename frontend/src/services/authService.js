import api from './api';

export const authService = {
  async login(username, password) {
    const response = await api.post('/auth/login', { username, password });
    if (response.data.token) {
      localStorage.setItem('aidb_token', response.data.token);
      localStorage.setItem('aidb_user', JSON.stringify(response.data));
    }
    return response.data;
  },

  async register(username, email, password) {
    const response = await api.post('/auth/register', { username, email, password });
    if (response.data.token) {
      localStorage.setItem('aidb_token', response.data.token);
      localStorage.setItem('aidb_user', JSON.stringify(response.data));
    }
    return response.data;
  },

  logout() {
    localStorage.removeItem('aidb_token');
    localStorage.removeItem('aidb_user');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('aidb_user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch (e) {
      return null;
    }
  }
};
