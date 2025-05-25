import request from '@/utils/request'

/**
 * 获取当前用户的通知列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @param {string} params.type - 通知类型（可选）
 * @returns {Promise<Object>} 通知列表数据
 */
export const getNotifications = (params: { page: number; size: number; type: string }): Promise<object> => {
  return request({
    url: '/api/notifications',
    method: 'get',
    params
  })
}

/**
 * 获取未读通知数量
 * @returns {Promise<Object>} 未读通知数量
 */
export const getUnreadNotificationCount = () => {
  return request({
    url: '/api/notifications/unread/count',
    method: 'get'
  })
}

/**
 * 标记通知为已读
 * @param {number} notificationId - 通知ID
 * @returns {Promise<Object>} 操作结果
 */
export const markNotificationAsRead = (notificationId: number): Promise<object> => {
  return request({
    url: `/api/notifications/${notificationId}/read`,
    method: 'put'
  })
}

/**
 * 标记所有通知为已读
 * @returns {Promise<Object>} 操作结果
 */
export const markAllNotificationsAsRead = () => {
  return request({
    url: '/api/notifications/read/all',
    method: 'put'
  })
}

/**
 * 删除通知
 * @param {number} notificationId - 通知ID
 * @returns {Promise<Object>} 操作结果
 */
export const deleteNotification = (notificationId: number): Promise<object> => {
  return request({
    url: `/api/notifications/${notificationId}`,
    method: 'delete'
  })
}
