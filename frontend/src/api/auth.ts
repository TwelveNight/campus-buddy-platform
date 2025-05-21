import axios from 'axios'

// 用户登录
export function login(data: { username: string; password: string }) {
  return axios.post('/api/user/login', data)
}

// 用户注册
export function register(data: { username: string; password: string; nickname: string }) {
  return axios.post('/api/user/register', data)
}
