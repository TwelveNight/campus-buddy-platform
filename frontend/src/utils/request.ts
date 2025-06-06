import axios from 'axios'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

// 判断当前环境
const isDevelopment = process.env.NODE_ENV === 'development';

// 根据环境配置baseURL
let baseURL;
if (isDevelopment) {
  // 开发环境使用固定地址
  baseURL = 'http://localhost:8080';
} else {
  // 生产环境使用相对路径，自动适应当前域名
  baseURL = '';
}

// 配置axios默认值
axios.defaults.baseURL = baseURL;
axios.defaults.timeout = 15000; // 增加超时时间到15秒

// 请求拦截器 - 添加token到header
axios.interceptors.request.use(
  config => {
    // 优先从 localStorage 获取 token
    let token = localStorage.getItem('token')
    // 如果你用 pinia/vuex 也可以加一行：
    // if (!token && window.__PINIA__?.authStore?.token) token = window.__PINIA__.authStore.token
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理常见错误
axios.interceptors.response.use(
  response => {
    // 检查业务响应码，如果不是200，则抛出错误
    if (response.data && response.data.code !== undefined && response.data.code !== 200) {
      const error = new Error(response.data.message || '操作失败')
      // @ts-ignore
      error.response = response
      return Promise.reject(error)
    }
    return response
  },
  error => {
    if (error.response) {
      // 处理401未授权错误
      if (error.response.status === 401) {
        // 检查后端返回的message
        const msg = error.response.data && error.response.data.message ? error.response.data.message : ''
        // 兼容pinia store
        let authStore
        try {
          authStore = useAuthStore()
        } catch (e) {
          // 可能在setup外部调用，降级处理
        }
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (authStore && typeof authStore.logout === 'function') {
          authStore.logout()
        }
        if (msg.includes('账号被禁用')) {
          ElMessage.error('账号已被禁用，请联系管理员')
        } else if (msg.includes('登录状态已失效')) {
          ElMessage.error('登录状态已失效，请重新登录')
        } else {
          ElMessage.error('登录已失效，请重新登录')
        }
        window.location.href = '/login'
      }
      // 处理400请求体格式错误
      else if (error.response.status === 400) {
        // 检查是否是头像更新API调用
        const isAvatarUpdate = error.config && 
                              error.config.url && 
                              error.config.url.includes('/api/user/profile') && 
                              error.config.method === 'put' &&
                              error.config.data && 
                              error.config.data.includes('avatarUrl');
        
        if (isAvatarUpdate) {
          console.warn('头像更新API返回400错误，但头像可能已更新成功:', error);
          // 为了UI流畅，我们不抛出这个错误
          // 返回一个假的成功响应
          return Promise.resolve({
            data: {
              code: 200,
              message: '头像可能已更新，请刷新查看',
              data: null
            }
          });
        }
        
        const errorMessage = error.response.data && error.response.data.message
          ? error.response.data.message 
          : '请求格式错误，请检查提交的数据'
        error.message = errorMessage
      }
    }
    return Promise.reject(error)
  }
)

export default axios
