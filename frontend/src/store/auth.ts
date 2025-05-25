import { defineStore } from 'pinia'
import { login, register, getCurrentUser, checkIsAdmin } from '../api/auth'
import axios from 'axios'
import webSocketService from '../utils/websocket'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null') as null | { 
      username: string;
      nickname: string;
      userId?: number;
      avatarUrl?: string;
      creditScore?: number;
      roles?: string[];
      gender?: string;
      major?: string;
      grade?: string;
      contactInfo?: string;
      skillTags?: string;
      status?: string;
      createdAt?: string;
      updatedAt?: string;
    },
    isAuthenticated: !!localStorage.getItem('token'),
    avatarUpdateTime: Date.now(), // 添加头像更新时间属性，用于刷新缓存
  }),
  getters: {
    // 判断当前用户是否为管理员
    isAdmin(): boolean {
      return !!(this.user?.roles && this.user.roles.includes('ROLE_ADMIN'));
    }
  },
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
            localStorage.setItem('user', JSON.stringify(this.user));
            
            // 立即获取完整的用户信息，包括角色信息
            try {
              await this.fetchCurrentUser();
              console.log('完整用户信息获取成功，包含角色信息:', this.user);
            } catch (fetchError) {
              console.error('获取完整用户信息失败:', fetchError);
            }
          } else {
            console.error('Invalid JWT structure: cannot decode user info.');
            this.user = null; 
            localStorage.removeItem('user');
          }
        } catch (e) {
          console.error('Error decoding JWT payload:', e);
          this.user = null; 
          localStorage.removeItem('user');
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
          localStorage.setItem('user', JSON.stringify(this.user));
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
      localStorage.removeItem('user')
      
      // 断开WebSocket连接
      try {
        webSocketService.disconnect();
      } catch (err) {
        console.error('断开WebSocket连接失败:', err);
      }
    },

    // 从后端获取用户是否为管理员
    async checkAdminStatus() {
      try {
        const res = await checkIsAdmin();
        if (res.data && res.data.code === 200) {
          // 如果用户不存在，创建一个空的用户对象
          if (!this.user) {
            this.user = {
              username: '',
              nickname: ''
            };
          }
          
          // 如果roles不存在，创建一个空数组
          if (!this.user.roles) {
            this.user.roles = [];
          }
          
          // 如果后端确认用户是管理员但前端状态中没有ROLE_ADMIN角色，则添加
          if (res.data.data === true && !this.user.roles.includes('ROLE_ADMIN')) {
            this.user.roles.push('ROLE_ADMIN');
            localStorage.setItem('user', JSON.stringify(this.user));
            console.log('已更新用户角色，添加管理员权限');
          }
          
          // 初始化WebSocket连接
          if (this.user?.userId) {
            try {
              webSocketService.connect(this.user.userId);
            } catch (err) {
              console.error('初始化WebSocket失败:', err);
            }
          }
          
          return res.data.data;
        } else {
          console.error('获取管理员状态失败:', res.data);
          return false;
        }
      } catch (error) {
        console.error('检查管理员状态出错:', error);
        return false;
      }
    }
  }
})
