<template>
  <div class="mobile-notification-card" :class="{'notification-unread': !notification.isRead}">
    <div class="notification-header">
      <div class="avatar-container">
        <el-avatar :size="48" :src="notification.senderAvatar || defaultAvatar">
          {{ notification.senderName ? notification.senderName.substring(0, 1) : '系' }}
        </el-avatar>
        <div class="unread-dot" v-if="!notification.isRead"></div>
      </div>
      <div class="header-content">
        <div class="notification-meta">
          <span class="sender-name">{{ notification.senderName || '系统通知' }}</span>
          <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
        </div>
        <div class="notification-title">{{ notification.title }}</div>
      </div>
      <div class="notification-actions">
        <el-dropdown trigger="click" @command="(command: string) => handleCommand(command, notification)">
          <el-icon><MoreFilled /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="markAsRead" v-if="!notification.isRead">
                <el-icon><Check /></el-icon>标为已读
              </el-dropdown-item>
              <el-dropdown-item command="delete">
                <el-icon><Delete /></el-icon>删除通知
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="notification-content">
      {{ notification.content }}
    </div>
    <div class="notification-footer">
      <el-tag size="small" :type="getTypeTagType(notification.type)">{{ getTypeLabel(notification.type) }}</el-tag>
      <el-tag size="small" v-if="!notification.isRead" type="danger">未读</el-tag>
      <el-tag size="small" v-else type="info">已读</el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MoreFilled, Check, Delete } from '@element-plus/icons-vue'
import { defineProps, defineEmits } from 'vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import type { NotificationItem } from '@/types/notification'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

defineProps({
  notification: {
    type: Object as () => NotificationItem,
    required: true
  },
  defaultAvatar: {
    type: String,
    default: '/avatar-placeholder.png'
  }
})

const emit = defineEmits(['mark-as-read', 'delete', 'click'])

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 获取类型标签颜色
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'SYSTEM_ANNOUNCEMENT': 'danger',
    'SYSTEM_ACTIVITY': 'warning',
    'HELP_NEW_APPLICATION': 'primary',
    'HELP_APPLICATION_ACCEPTED': 'success',
    'HELP_APPLICATION_REJECTED': 'danger',
    'HELP_COMPLETED': 'success',
    'HELP_NEW_REVIEW': 'info',
    'GROUP_JOIN_APPLICATION': 'primary',
    'GROUP_JOIN_APPROVED': 'success',
    'GROUP_JOIN_REJECTED': 'danger',
    'GROUP_INVITATION': 'primary',
    'GROUP_ANNOUNCEMENT': 'warning',
    'GROUP_ADMIN_ASSIGNED': 'success',
    'POST_STATUS': 'info',
    'POST_DELETE': 'danger',
    'FRIEND_REQUEST': 'primary', 
    'FRIEND_REQUEST_ACCEPTED': 'success',
    'FRIEND_REQUEST_REJECTED': 'danger',
    'FRIEND_REMOVED': 'danger',
  }
  return typeMap[type] || 'info'
}

// 获取类型标签文本
const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    'SYSTEM_ANNOUNCEMENT': '系统公告',
    'SYSTEM_ACTIVITY': '活动通知',
    'HELP_NEW_APPLICATION': '互助申请',
    'HELP_APPLICATION_ACCEPTED': '申请通过',
    'HELP_APPLICATION_REJECTED': '申请拒绝',
    'HELP_COMPLETED': '互助完成',
    'HELP_NEW_REVIEW': '新评价',
    'GROUP_JOIN_APPLICATION': '申请加入',
    'GROUP_JOIN_APPROVED': '加入批准',
    'GROUP_JOIN_REJECTED': '加入拒绝',
    'GROUP_INVITATION': '小组邀请',
    'GROUP_ANNOUNCEMENT': '小组公告',
    'GROUP_ADMIN_ASSIGNED': '管理员任命',
    'POST_STATUS': '帖子状态变更',
    'POST_DELETE': '帖子删除',
    'FRIEND_REQUEST': '好友申请',
    'FRIEND_REQUEST_ACCEPTED': '申请通过',
    'FRIEND_REQUEST_REJECTED': '申请拒绝',
    'FRIEND_REMOVED': '好友移除',
  }
  return typeMap[type] || type
}

// 处理下拉菜单命令
const handleCommand = (command: string, notification: NotificationItem) => {
  if (command === 'markAsRead') {
    emit('mark-as-read', notification.notificationId)
  } else if (command === 'delete') {
    emit('delete', notification.notificationId)
  }
}
</script>

<style scoped>
.mobile-notification-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid var(--el-border-color-lighter);
}

.mobile-notification-card:active {
  transform: scale(0.98);
}

.notification-unread {
  background: linear-gradient(135deg, var(--el-color-primary-light-9), #f0f9ff);
  border-left: 4px solid var(--el-color-primary);
  border-color: var(--el-color-primary-light-3);
}

.notification-header {
  display: flex;
  margin-bottom: 12px;
}

.avatar-container {
  position: relative;
  margin-right: 12px;
}

.unread-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--el-color-danger), #ff7875);
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(245, 34, 45, 0.3);
  animation: pulse 2s ease-in-out infinite;
}

.header-content {
  flex: 1;
  min-width: 0;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.sender-name {
  font-size: 14px;
  color: var(--el-color-primary);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.notification-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.notification-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 6px;
  line-height: 1.4;
  color: var(--el-text-color-primary);
}

.notification-content {
  font-size: 14px;
  line-height: 1.5;
  color: var(--el-text-color-regular);
  margin-bottom: 12px;
  word-break: break-word;
}

.notification-footer {
  display: flex;
  justify-content: flex-start;
  gap: 8px;
}

.notification-actions {
  margin-left: auto;
  padding-top: 2px;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(0.95);
  }
}

/* 暗色主题适配 */
[data-theme="dark"] .mobile-notification-card {
  background: linear-gradient(135deg, rgba(36,41,61,0.92), rgba(45,55,72,0.88));
  border: 1px solid var(--dark-border-color, #333);
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}

[data-theme="dark"] .notification-unread {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.15), rgba(36,41,61,0.92));
  border-left: 4px solid var(--primary-color-dark, #60a9ff);
}

[data-theme="dark"] .unread-dot {
  border: 2px solid rgba(36,41,61,0.92);
}

[data-theme="dark"] .notification-title {
  color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .notification-content {
  color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .sender-name {
  color: var(--primary-color-dark, #60a9ff);
}

[data-theme="dark"] .notification-time {
  color: var(--dark-text-secondary, #a3a6ad);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .mobile-notification-card {
    padding: 12px;
    margin-bottom: 12px;
  }
  
  .notification-title {
    font-size: 14px;
  }
  
  .notification-content {
    font-size: 13px;
  }
}
</style>
