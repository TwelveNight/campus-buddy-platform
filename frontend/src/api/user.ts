import axios from '../utils/request'

// 获取用户个人信息
export function getUserProfile() {
  return axios.get('/api/user/me')
}

// 根据ID获取用户信息
export function getUserById(userId: number) {
  return axios.get(`/api/user/${userId}`)
}

// 更新用户个人信息
export function updateUserProfile(data: {
  nickname?: string;
  avatarUrl?: string;
}) {
  return axios.put('/api/user/profile', data)
}

// 修改密码
export function changePassword(data: {
  oldPassword: string;
  newPassword: string;
}) {
  return axios.put('/api/user/password', data)
}
