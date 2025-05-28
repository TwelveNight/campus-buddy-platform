import request from '@/utils/request'
import type {
  ChatSessionListResponse,
  ChatMessageListResponse,
  SendMessageResponse,
  CommonResponse
} from '@/types/message'

/**
 * 发送私信
 * @param {Object} data - 私信数据
 * @param {number} data.recipientId - 接收者ID
 * @param {string} data.content - 消息内容
 * @param {string} [data.messageType] - 消息类型：TEXT, IMAGE, EMOJI
 * @param {string} [data.imageUrl] - 图片URL（当messageType为IMAGE时）
 * @returns {Promise<{data: SendMessageResponse}>} 发送结果
 */
export const sendPrivateMessage = (data: { 
  recipientId: number; 
  content: string;
  messageType?: 'TEXT' | 'IMAGE' | 'EMOJI';
  imageUrl?: string;
}) => {
  return request({
    url: '/api/messages',
    method: 'post',
    data
  })
}

/**
 * 获取与指定用户的聊天记录
 * @param {number} userId - 对方用户ID
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @returns {Promise<{data: ChatMessageListResponse}>} 聊天记录
 */
export const getChatHistory = (userId: number, params: { page: number; size: number; }) => {
  return request({
    url: `/api/messages/chat/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 获取聊天会话列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 * @returns {Promise<{data: ChatSessionListResponse}>} 会话列表
 */
export const getChatSessions = (params: { page: number; size: number; }) => {
  return request({
    url: '/api/messages/sessions',
    method: 'get',
    params
  })
}

/**
 * 获取未读私信数量
 * @returns {Promise<{data: any}>} 未读私信数量
 */
export const getUnreadMessageCount = () => {
  return request({
    url: '/api/messages/unread/count',  // 添加/api前缀，匹配后端API路径
    method: 'get'
  })
}

/**
 * 标记与指定用户的所有私信为已读
 * @param {number} userId - 对方用户ID
 * @returns {Promise<{data: CommonResponse}>} 操作结果
 */
export const markAllMessagesAsRead = (userId: number) => {
  return request({
    url: `/api/messages/read/all/${userId}`,
    method: 'put'
  })
}

/**
 * 标记私信为已读
 * @param {number} messageId - 私信ID
 * @returns {Promise<{data: CommonResponse}>} 操作结果
 */
export const markMessageAsRead = (messageId: number) => {
  return request({
    url: `/api/messages/${messageId}/read`,
    method: 'put'
  })
}

/**
 * 删除私信
 * @param {number} messageId - 私信ID
 * @returns {Promise<{data: CommonResponse}>} 操作结果
 */
export const deleteMessage = (messageId: number) => {
  return request({
    url: `/api/messages/${messageId}`,
    method: 'delete'
  })
}
