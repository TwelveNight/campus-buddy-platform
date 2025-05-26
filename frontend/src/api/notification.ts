import request from '@/utils/request'
import type { NotificationListResponse, UnreadCountResponse, CommonResponse, PageResult, NotificationItem } from '@/types/notification'

/**
 * 获取当前用户的通知列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.type] - 通知类型（可选）
 * @param {boolean} [params.isRead] - 已读状态（可选）
 * @returns {Promise<PageResult<NotificationItem>>} 通知列表数据 (PageResult)
 */
export const getNotifications = (params: { page: number; size: number; type?: string; isRead?: boolean }): Promise<PageResult<NotificationItem>> => {
  return request({
    url: '/api/notifications',
    method: 'get',
    params
  })
}

/**
 * 获取未读通知数量
 * @returns {Promise<{count: number}>} 未读通知数量
 */
export const getUnreadNotificationCount = (): Promise<{count: number}> => {
  return request({
    url: '/api/notifications/unread/count',
    method: 'get'
  })
}

/**
 * 标记通知为已读
 * @param {number} notificationId - 通知ID
 * @returns {Promise<null>} 操作结果
 */
export const markNotificationAsRead = (notificationId: number): Promise<null> => {
  return request({
    url: `/api/notifications/${notificationId}/read`,
    method: 'put'
  })
}

/**
 * 标记所有通知为已读
 * @returns {Promise<{count: number}>} 操作结果
 */
export const markAllNotificationsAsRead = (): Promise<{count: number}> => {
  return request({
    url: '/api/notifications/read/all',
    method: 'put'
  })
}

/**
 * 删除通知
 * @param {number} notificationId - 通知ID
 * @returns {Promise<null>} 操作结果
 */
export const deleteNotification = (notificationId: number): Promise<null> => {
  return request({
    url: `/api/notifications/${notificationId}`,
    method: 'delete'
  })
}

/**
 * 管理员发布系统通知（公告）
 * @param {Object} data - 公告内容
 * @param {string} data.type - 通知类型（如 SYSTEM_ANNOUNCEMENT）
 * @param {string} data.title - 公告标题
 * @param {string} data.content - 公告内容
 * @returns {Promise<any>} 结果
 */
export const sendSystemNotification = (data: { type: string; title: string; content: string; recipientId?: number; relatedId?: number }): Promise<any> => {
  // 为系统公告添加当前管理员自己的ID作为接收者，后端逻辑应该处理系统公告的发送
  const requestData = { ...data };
  return request({
    url: '/api/notifications/system',
    method: 'post',
    data: requestData
  })
}
