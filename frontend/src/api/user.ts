import axios from '../utils/request'

// 获取用户个人信息
export function getUserProfile() {
  return axios.get('/api/user/me')
}

// 根据ID获取用户信息
export function getUserById(userId: number) {
  if (!userId || isNaN(userId)) {
    return Promise.reject(new Error('Invalid user ID'))
  }
  return axios.get(`/api/user/${userId}`)
}

// 批量获取用户信息
export function getUsersByIds(userIds: number[]): Promise<any> {
  return axios.post('/api/user/batch', { userIds });
}

// 搜索用户
export function searchUsers(params: {
  keyword: string;
  page?: number;
  size?: number;
}) {
  return axios.get('/api/user/search', { params })
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
