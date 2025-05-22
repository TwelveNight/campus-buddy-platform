import axios from 'axios'

// 配置axios默认值
axios.defaults.baseURL = 'http://localhost:8080' // 根据后端地址调整
axios.defaults.timeout = 10000

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
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      // 处理400请求体格式错误
      else if (error.response.status === 400) {
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
