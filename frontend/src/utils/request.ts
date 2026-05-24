import axios from 'axios'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'
import {
  ACCOUNT_DISABLED_PATH,
  inferAccountUnavailableStatus,
  isAccountUnavailableMessage,
  saveAccountUnavailable
} from '@/utils/accountStatus'

const redirectToAccountDisabled = (message?: string) => {
  saveAccountUnavailable(inferAccountUnavailableStatus(message), message || '账号当前不可用，请联系管理员')
  if (window.location.pathname !== ACCOUNT_DISABLED_PATH) {
    window.location.href = ACCOUNT_DISABLED_PATH
  }
}

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
      const message = response.data.message || '操作失败'
      if (isAccountUnavailableMessage(message)) {
        redirectToAccountDisabled(message)
      }
      const error = new Error(message)
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
        const isUnavailable = isAccountUnavailableMessage(msg)
        if (isUnavailable) {
          saveAccountUnavailable(inferAccountUnavailableStatus(msg), msg || '账号当前不可用，请联系管理员')
        }
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (authStore && typeof authStore.logout === 'function') {
          authStore.logout()
        }
        if (isUnavailable) {
          ElMessage.error(msg || '账号已被禁用，请联系管理员')
        } else if (msg.includes('登录状态已失效')) {
          ElMessage.error('登录状态已失效，请重新登录')
        } else {
          ElMessage.error('登录已失效，请重新登录')
        }
        if (isUnavailable) {
          if (window.location.pathname !== ACCOUNT_DISABLED_PATH) {
            window.location.href = ACCOUNT_DISABLED_PATH
          }
        } else {
          window.location.href = '/login'
        }
      }
      // 处理400请求体格式错误
      else if (error.response.status === 400) {
        const errorMessage = error.response.data && error.response.data.message
          ? error.response.data.message 
          : '请求格式错误，请检查提交的数据'
        error.message = errorMessage
        if (isAccountUnavailableMessage(errorMessage)) {
          redirectToAccountDisabled(errorMessage)
        }
      }
    }
    return Promise.reject(error)
  }
)

export default axios
