import { defineStore } from 'pinia'
import { login, register, getCurrentUser } from '../api/auth'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as null | { 
      username: string;
      nickname: string;
      userId?: number;
      avatarUrl?: string;
      creditScore?: number;
      roles?: string[];
    },
    isAuthenticated: !!localStorage.getItem('token'),
  }),
  actions: {
    async loginAction(payload: { username: string; password: string }) {
      const res = await login(payload)
      console.log('Login API Response:', res.data); 
      // Check if the response is successful and contains the token directly in res.data.data
      if (res.data && res.data.code === 200 && typeof res.data.data === 'string' && res.data.data.length > 0) {
        this.token = res.data.data; // The token is directly in res.data.data
        this.isAuthenticated = true;
        localStorage.setItem('token', this.token);
        console.log('Token stored in localStorage:', localStorage.getItem('token'));

        // 临时获取基本信息 - 这将稍后被 fetchCurrentUser 更新的完整信息替代
        try {
          const tokenParts = this.token.split('.');
          if (tokenParts.length === 3) {
            const payloadData = JSON.parse(atob(tokenParts[1]));
            this.user = {
              username: payloadData.sub, // 'sub' usually holds the username
              nickname: payloadData.sub // 暂时使用用户名作为昵称，会在获取完整信息时更新
            };
          } else {
            console.error('Invalid JWT structure: cannot decode user info.');
            this.user = null; 
          }
        } catch (e) {
          console.error('Error decoding JWT payload:', e);
          this.user = null; 
        }

      } else {
        // More detailed error messages
        let errorMessage = '登录失败';
        if (res.data && res.data.message) {
          errorMessage = res.data.message;
        } else if (!res.data || typeof res.data.data !== 'string' || res.data.data.length === 0) {
          errorMessage = '未从服务器获取到有效的Token';
          console.error('Token not found in response, or response structure is incorrect, or token is empty:', res.data);
        }
        throw new Error(errorMessage);
      }
    },
    async registerAction(payload: { username: string; password: string; nickname: string }) {
      const res = await register(payload)
      if (res.data.code !== 200) {
        throw new Error(res.data.message)
      }
    },
    async fetchCurrentUser() {
      try {
        const res = await getCurrentUser();
        if (res.data && res.data.code === 200 && res.data.data) {
          this.user = res.data.data;
          this.isAuthenticated = true;
          return this.user;
        } else {
          throw new Error(res.data?.message || '获取用户信息失败');
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        // 如果请求失败（如401未授权），清除登录状态
        if (axios.isAxiosError(error) && error.response?.status === 401) {
          this.logout();
        }
        throw error;
      }
    },
    logout() {
      this.token = ''
      this.user = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    }
  }
})
