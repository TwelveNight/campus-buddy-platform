import request from '@/utils/request'

// 获取平台概览数据
export function getAdminOverview() {
  return request.get('/api/admin/dashboard/overview')
}

// 获取用户统计数据
export function getAdminUserStats() {
  return request.get('/api/admin/dashboard/users')
}

// 获取帖子统计数据
export function getAdminPostStats() {
  return request.get('/api/admin/dashboard/posts')
}

// 获取群组统计数据
export function getAdminGroupStats() {
  return request.get('/api/admin/dashboard/groups')
}
