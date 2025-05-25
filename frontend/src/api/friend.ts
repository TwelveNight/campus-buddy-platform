// 好友相关API接口
import axios from '../utils/request'

// 申请添加好友
export function applyFriend(userId: number) {
  return axios.post('/api/friend/apply', { recipientId: userId })
}

// 接受好友申请
export function acceptFriendRequest(requestId: number) {
  return axios.post(`/api/friend/accept/${requestId}`)
}

// 拒绝好友申请
export function rejectFriendRequest(requestId: number) {
  return axios.post(`/api/friend/reject/${requestId}`)
}

// 获取好友列表
export function getFriendList(params?: { 
  page?: number; 
  size?: number;
  keyword?: string; 
}) {
  return axios.get('/api/friend/list', { params })
}

// 获取好友申请列表
export function getFriendRequests(params?: { 
  page?: number; 
  size?: number;
  status?: 'PENDING' | 'ACCEPTED' | 'REJECTED';
}) {
  return axios.get('/api/friend/requests', { params })
}

// 搜索好友
export function searchFriends(keyword: string, params?: {
  page?: number;
  size?: number;
}) {
  return axios.get('/api/friend/search', { 
    params: { 
      keyword, 
      ...params 
    } 
  })
}

// 删除好友
export function deleteFriend(userId: number) {
  return axios.delete(`/api/friend/${userId}`)
}
