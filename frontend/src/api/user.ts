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
  gender?: string;
  major?: string;
  grade?: string;
  contactInfo?: string;
  skillTags?: string;
}) {
  // 对于仅包含头像URL的更新，使用特定格式
  if (Object.keys(data).length === 1 && data.avatarUrl) {
    // 尝试使用后端可能期望的格式
    return axios.put('/api/user/profile', { 
      "avatarUrl": data.avatarUrl 
    });
  }
  
  // 其他情况使用原始格式
  return axios.put('/api/user/profile', data);
}

// 修改密码
export function changePassword(data: {
  oldPassword: string;
  newPassword: string;
}) {
  return axios.put('/api/user/password', data)
}
