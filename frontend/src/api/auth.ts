import axios from '../utils/request'

// 用户登录（支持：用户名+密码 / 邮箱+密码 / 邮箱+验证码）
export function login(data: {
  username?: string;
  password?: string;
  loginType?: 'PASSWORD' | 'CODE';
  email?: string;
  code?: string;
}) {
  return axios.post('/api/user/login', data)
}

// 用户注册（email / emailCode 为选填）
export function register(data: {
  username: string;
  password: string;
  nickname: string;
  email?: string;
  emailCode?: string;
}) {
  return axios.post('/api/user/register', data)
}

// 获取当前用户信息
export function getCurrentUser() {
  return axios.get('/api/user/me')
}

// 检查当前用户是否为管理员
export function checkIsAdmin() {
  return axios.get('/api/user/is-admin')
}
