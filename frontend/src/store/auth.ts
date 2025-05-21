import { defineStore } from 'pinia'
import { login, register } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as null | { username: string; nickname: string },
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

        // Attempt to decode user information from token
        try {
          const tokenParts = this.token.split('.');
          if (tokenParts.length === 3) {
            const payloadData = JSON.parse(atob(tokenParts[1]));
            this.user = {
              username: payloadData.sub, // 'sub' usually holds the username
              // Attempt to get nickname, fallback to username if not present
              nickname: payloadData.nickname || payloadData.name || payloadData.sub 
            };
            console.log('User info from token:', this.user);
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
    logout() {
      this.token = ''
      this.user = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    }
  }
})
