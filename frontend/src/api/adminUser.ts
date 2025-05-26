import request from '@/utils/request'
import type { UserVO } from '@/types/user'
import type { Page } from '@/types/common'

// 分页获取用户列表
export function getAdminUserPage(params: { page: number; size: number; keyword?: string; status?: string }) {
  return request.get<Page<UserVO>>('/api/admin/user/page', { params })
}

// 启用/禁用用户
export function updateAdminUserStatus(userId: number, status: string) {
  return request.post(`/api/admin/user/${userId}/status`, null, { params: { status } })
}

// 重置用户密码
export function resetAdminUserPassword(userId: number) {
  return request.post(`/api/admin/user/${userId}/reset-password`)
}
