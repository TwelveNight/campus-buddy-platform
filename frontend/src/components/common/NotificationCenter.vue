<template>
    <div class="notification-center">
        <div class="notification-header">
            <h2>
                {{ props.title }}
                <span class="unread-count-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
            </h2>
            <div class="notification-actions">
                <el-button type="primary" size="small" @click="markAllAsRead" :disabled="unreadCount === 0">
                    <el-icon><Check /></el-icon>全部标为已读
                </el-button>
            </div>
        </div>

        <el-divider />

        <div class="notification-list">
            <template v-if="loading">
                <el-skeleton :rows="3" animated v-for="i in 3" :key="i" />
            </template>
            <template v-else-if="notifications.length === 0">
                <el-empty description="暂无通知" />
            </template>
            <template v-else>
                <div 
                    v-for="notification in notifications" 
                    :key="notification.notificationId" 
                    :class="['notification-item', notification.isRead ? 'notification-read' : 'notification-unread']"
                    @click="handleNotificationClick(notification)"
                >
                    <div class="notification-avatar">
                        <el-avatar :size="48" :src="notification.senderAvatar || defaultAvatar">
                            {{ notification.senderName ? notification.senderName.substring(0, 1) : '系' }}
                        </el-avatar>
                        <div class="unread-dot" v-if="!notification.isRead"></div>
                    </div>
                    <div class="notification-content">
                        <div class="notification-header-info">
                            <div class="notification-title">
                                {{ notification.title }}
                                <span v-if="!notification.isRead" class="unread-badge">未读</span>
                                <span v-else class="read-badge">已读</span>
                            </div>
                            <div class="notification-meta">
                                <span class="sender-name">{{ notification.senderName || '系统通知' }}</span>
                                <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
                            </div>
                        </div>
                        <div class="notification-body">{{ notification.content }}</div>
                        <div class="notification-type-tag">
                            <el-tag size="small" :type="getTypeTagType(notification.type)">{{ getTypeLabel(notification.type) }}</el-tag>
                        </div>
                    </div>
                    <div class="notification-actions" @click.stop>
                        <el-dropdown trigger="click" @command="(command: string) => handleCommand(command, notification)">
                            <span class="el-dropdown-link">
                                <el-icon><MoreFilled /></el-icon>
                            </span>
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
            </template>
        </div>

        <div class="notification-pagination" v-if="total > 0">
            <el-pagination
                background
                layout="prev, pager, next, sizes"
                :page-sizes="[5, 10, 20, 50]"
                :page-size="pageSize"
                :current-page="currentPage"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled, Check, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { getNotifications, markNotificationAsRead, markAllNotificationsAsRead, deleteNotification } from '@/api/notification'
import type { NotificationItem } from '@/types/notification'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
    title: {
        type: String,
        default: '消息通知'
    },
    type: {
        type: String,
        default: 'all' // 默认all，确保支持多类型和未读优先
    }
})

const router = useRouter()
const notifications = ref<NotificationItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultAvatar = ref('/avatar-placeholder.png')

// 计算未读数量
const unreadCount = computed(() => {
    return notifications.value.filter(item => !item.isRead).length
})

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
        'GROUP_JOIN_APPROVED': 'success',
        'GROUP_JOIN_REJECTED': 'danger',
        'GROUP_INVITATION': 'primary',
        'GROUP_ANNOUNCEMENT': 'warning',
        'GROUP_ADMIN_ASSIGNED': 'success'
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
        'GROUP_JOIN_APPROVED': '加入批准',
        'GROUP_JOIN_REJECTED': '加入拒绝',
        'GROUP_INVITATION': '小组邀请',
        'GROUP_ANNOUNCEMENT': '小组公告',
        'GROUP_ADMIN_ASSIGNED': '管理员任命'
    }
    return typeMap[type] || type
}

// 获取通知列表
const fetchNotifications = async () => {
    loading.value = true
    try {
        const { data } = await getNotifications({
            page: currentPage.value,
            size: pageSize.value,
            type: props.type
        })
        notifications.value = data.data.records
        total.value = data.data.total
    } catch (error) {
        console.error('获取通知失败:', error)
        ElMessage.error('获取通知列表失败')
        notifications.value = []
        total.value = 0
    } finally {
        loading.value = false
    }
}

// 说明：未读优先、type多类型、数量限制等逻辑由后端保证，前端只需正确传参。

// 处理通知点击
const handleNotificationClick = (notification: NotificationItem) => {
    // 如果未读，标记为已读
    if (!notification.isRead) {
        markAsRead(notification.notificationId)
    }

    // 如果有相关链接，跳转
    if (notification.relatedLink) {
        router.push(notification.relatedLink)
    }
}

// 标记为已读
const markAsRead = async (notificationId: number) => {
    try {
        const { data } = await markNotificationAsRead(notificationId)
        if (data.code === 200) {
            const index = notifications.value.findIndex(item => item.notificationId === notificationId)
            if (index !== -1) {
                notifications.value[index].isRead = true
            }
            ElMessage.success('已标记为已读')
        }
    } catch (error) {
        ElMessage.error('标记已读失败')
    }
}

// 标记全部为已读
const markAllAsRead = async () => {
    try {
        const { data } = await markAllNotificationsAsRead()
        if (data.code === 200) {
            notifications.value.forEach(item => {
                item.isRead = true
            })
            ElMessage.success('已全部标记为已读')
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

// 删除通知
const handleDelete = async (notificationId: number) => {
    try {
        await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        const { data } = await deleteNotification(notificationId)
        if (data.code === 200) {
            notifications.value = notifications.value.filter(item => item.notificationId !== notificationId)
            total.value -= 1
            ElMessage.success('删除成功')
        }
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
        }
    }
}

// 处理下拉菜单命令
const handleCommand = (command: string, notification: NotificationItem) => {
    if (command === 'markAsRead') {
        markAsRead(notification.notificationId)
    } else if (command === 'delete') {
        handleDelete(notification.notificationId)
    }
}

// 处理分页变化
const handleSizeChange = (val: number) => {
    pageSize.value = val
    fetchNotifications()
}

const handleCurrentChange = (val: number) => {
    currentPage.value = val
    fetchNotifications()
}

// 监听类型变化
watch(() => props.type, () => {
    currentPage.value = 1
    fetchNotifications()
})

onMounted(() => {
    fetchNotifications()
})
</script>

<style scoped>
.notification-center {
    padding: 20px;
    background-color: var(--el-bg-color);
    height: 100%;
    display: flex;
    flex-direction: column;
}

.notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.notification-header h2 {
    margin: 0;
    color: var(--el-text-color-primary);
    font-size: 20px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 12px;
}

.unread-count-badge {
    background: linear-gradient(135deg, var(--el-color-danger), #ff7875);
    color: white;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 12px;
    font-weight: 500;
    box-shadow: 0 2px 4px rgba(245, 34, 45, 0.3);
    animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {

    0%,
    20%,
    50%,
    80%,
    100% {
        transform: translateY(0);
    }

    40% {
        transform: translateY(-3px);
    }

    60% {
        transform: translateY(-2px);
    }
}

.notification-actions {
    display: flex;
    align-items: center;
}

.notification-list {
    flex: 1;
    overflow-y: auto;
    padding-right: 4px;
}

.notification-list::-webkit-scrollbar {
    width: 6px;
}

.notification-list::-webkit-scrollbar-track {
    background: var(--el-fill-color-lighter);
    border-radius: 3px;
}

.notification-list::-webkit-scrollbar-thumb {
    background: var(--el-fill-color-dark);
    border-radius: 3px;
}

.notification-list::-webkit-scrollbar-thumb:hover {
    background: var(--el-color-primary-light-3);
}

.notification-item {
    display: flex;
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 12px;
    transition: all 0.3s ease;
    position: relative;
    background-color: var(--el-bg-color-page);
    cursor: pointer;
    border: 1px solid var(--el-border-color-lighter);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.notification-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: var(--el-color-primary-light-5);
}

.notification-unread {
    background: linear-gradient(135deg, var(--el-color-primary-light-9), #f0f9ff);
    border-left: 4px solid var(--el-color-primary);
    font-weight: 500;
    border-color: var(--el-color-primary-light-3);
}

.notification-unread:hover {
    background: linear-gradient(135deg, var(--el-color-primary-light-8), #e6f7ff);
    border-left-color: var(--el-color-primary);
}

.notification-read {
    background-color: var(--el-bg-color-page);
    opacity: 0.9;
}

.notification-avatar {
    margin-right: 16px;
    position: relative;
    flex-shrink: 0;
}

.notification-avatar .el-avatar {
    border: 2px solid var(--el-border-color-light);
    transition: all 0.3s ease;
}

.notification-unread .notification-avatar .el-avatar {
    border-color: var(--el-color-primary-light-3);
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
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

.notification-content {
    flex: 1;
    min-width: 0;
}

.notification-header-info {
    margin-bottom: 8px;
}

.notification-title {
    font-weight: 600;
    margin-bottom: 6px;
    color: var(--el-text-color-primary);
    display: flex;
    align-items: center;
    gap: 8px;
    line-height: 1.4;
}

.notification-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
}

.sender-name {
    font-size: 13px;
    color: var(--el-color-primary);
    font-weight: 500;
    background-color: var(--el-color-primary-light-9);
    padding: 2px 8px;
    border-radius: 6px;
}

.notification-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
}

.notification-body {
    color: var(--el-text-color-regular);
    line-height: 1.5;
    margin-bottom: 10px;
    font-size: 14px;
    word-break: break-word;
}

.notification-type-tag {
    margin-top: 8px;
}

.unread-indicator {
    color: var(--el-color-primary);
    font-size: 14px;
    margin-right: 4px;
    animation: pulse 2s ease-in-out infinite;
}

.read-indicator {
    color: var(--el-text-color-placeholder);
    font-size: 14px;
    margin-right: 4px;
}

.unread-badge {
    background: linear-gradient(135deg, var(--el-color-danger), #ff7875);
    color: white;
    font-size: 10px;
    padding: 3px 8px;
    border-radius: 10px;
    font-weight: 500;
    flex-shrink: 0;
    box-shadow: 0 2px 4px rgba(245, 34, 45, 0.3);
}

.read-badge {
    background-color: var(--el-fill-color);
    color: var(--el-text-color-secondary);
    font-size: 10px;
    padding: 3px 8px;
    border-radius: 10px;
    font-weight: 400;
    flex-shrink: 0;
}

.notification-read .notification-title {
    color: var(--el-text-color-secondary);
    font-weight: 500;
}

.notification-read .notification-body {
    color: var(--el-text-color-placeholder);
}

.notification-read .notification-time {
    color: var(--el-text-color-placeholder);
}

.notification-read .sender-name {
    color: var(--el-text-color-secondary);
    background-color: var(--el-fill-color-light);
}

@keyframes pulse {

    0%,
    100% {
        opacity: 1;
        transform: scale(1);
    }

    50% {
        opacity: 0.7;
        transform: scale(0.95);
    }
}

.notification-pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    padding: 16px 0;
    border-top: 1px solid var(--el-border-color-lighter);
}

.notification-actions {
    display: flex;
    align-items: flex-start;
    margin-left: 12px;
    flex-shrink: 0;
}

.el-dropdown-link {
    cursor: pointer;
    color: var(--el-text-color-secondary);
    display: flex;
    align-items: center;
    padding: 4px;
    border-radius: 4px;
    transition: all 0.3s ease;
}

.el-dropdown-link:hover {
    color: var(--el-color-primary);
    background-color: var(--el-fill-color-light);
}

/* 暗色主题适配 */
html.dark .notification-unread {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.15), rgba(64, 158, 255, 0.1));
    border-left-color: var(--el-color-primary);
}

html.dark .notification-unread:hover {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(64, 158, 255, 0.15));
}

html.dark .sender-name {
    background-color: rgba(64, 158, 255, 0.15);
    color: var(--el-color-primary-light-3);
}

html.dark .notification-read .sender-name {
    background-color: var(--el-fill-color-dark);
    color: var(--el-text-color-secondary);
}

/* 空状态样式优化 */
.el-empty {
    padding: 60px 20px;
}

/* 骨架屏样式优化 */
.el-skeleton {
    padding: 16px;
}
</style>
