<template>
    <div class="notification-center">
        <div class="notification-header">
            <h2>
                <span class="title-icon"><el-icon><Bell /></el-icon></span>
                {{ props.title }}
                <span class="unread-count-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
            </h2>
            <div class="notification-actions">
                <el-button type="primary" size="small" @click="refreshNotifications" :icon="Refresh" class="refresh-button">
                    刷新
                </el-button>
                <el-button type="success" size="small" @click="markAllAsRead" :disabled="unreadCount === 0">
                    <el-icon><Check /></el-icon>全部标为已读
                </el-button>
            </div>
        </div>

        <div class="notification-filter">
            <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="unread">未读</el-radio-button>
                <el-radio-button label="read">已读</el-radio-button>
            </el-radio-group>
        </div>

        <el-divider />

        <div class="notification-list" ref="notificationListRef">
            <template v-if="loading">
                <div class="loading-skeleton">
                    <el-skeleton :rows="3" animated v-for="i in 3" :key="i" />
                </div>
            </template>
            <template v-else-if="notifications.length === 0">
                <el-empty 
                    description="暂无通知" 
                    :image-size="120"
                >
                    <template #image>
                        <div class="empty-icon-wrapper">
                            <el-icon><Bell /></el-icon>
                        </div>
                    </template>
                    <el-button type="primary" @click="refreshNotifications">刷新</el-button>
                </el-empty>
            </template>
            <template v-else>
                <div class="notification-timeline">
                    <div 
                        v-for="(group, date) in groupedNotifications" 
                        :key="date" 
                        class="notification-date-group"
                    >
                        <div class="date-divider">
                            <span class="date-label">{{ formatDateGroup(date) }}</span>
                        </div>
                        <div 
                            v-for="notification in group" 
                            :key="notification.notificationId" 
                            :class="['notification-item', 
                                notification.isRead ? 'notification-read' : 'notification-unread',
                                activeNotification === notification.notificationId ? 'notification-active' : '']"
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
                                        <span class="notification-time">
                                            <el-icon><Timer /></el-icon>
                                            {{ formatTime(notification.createdAt) }}
                                        </span>
                                    </div>
                                </div>
                                <div class="notification-body">{{ notification.content }}</div>
                                <div class="notification-type-tag">
                                    <el-tag size="small" :type="getTypeTagType(notification.type)" effect="light">
                                        <el-icon class="tag-icon"><component :is="getTypeIcon(notification.type)" /></el-icon>
                                        {{ getTypeLabel(notification.type) }}
                                    </el-tag>
                                    <el-tag 
                                        v-if="notification.relatedLink" 
                                        size="small" 
                                        type="info" 
                                        effect="plain"
                                        class="link-tag"
                                    >
                                        <el-icon><Link /></el-icon> 点击查看详情
                                    </el-tag>
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
                                            <el-dropdown-item command="markAsUnread" v-else>
                                                <el-icon><CircleCheck /></el-icon>标为未读
                                            </el-dropdown-item>
                                            <el-dropdown-item command="delete">
                                                <el-icon><Delete /></el-icon>删除通知
                                            </el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                            </div>
                        </div>
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
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
    MoreFilled, Check, Delete, Bell, Refresh, 
    CircleCheck, Timer, Link, InfoFilled, WarningFilled,
    ChatDotRound, User, Calendar, Setting, QuestionFilled,
    Promotion, UserFilled, Connection, Document, Service
} from '@element-plus/icons-vue'
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
const notificationListRef = ref<HTMLElement | null>(null)
const activeNotification = ref<number | null>(null)
const filterStatus = ref('all')

// 计算未读数量
const unreadCount = computed(() => {
    return notifications.value.filter(item => !item.isRead).length
})

// 根据日期分组通知
const groupedNotifications = computed(() => {
    const groups: Record<string, NotificationItem[]> = {}
    
    notifications.value.forEach(notification => {
        const date = dayjs(notification.createdAt).format('YYYY-MM-DD')
        if (!groups[date]) {
            groups[date] = []
        }
        groups[date].push(notification)
    })
    
    // 按日期降序排序
    return Object.fromEntries(
        Object.entries(groups).sort((a, b) => {
            return new Date(b[0]).getTime() - new Date(a[0]).getTime()
        })
    )
})

// 格式化时间
const formatTime = (time: string) => {
    if (!time) return ''
    const messageTime = dayjs(time)
    const now = dayjs()
    
    if (now.diff(messageTime, 'minute') < 1) {
        return '刚刚'
    } else if (now.diff(messageTime, 'hour') < 1) {
        return `${now.diff(messageTime, 'minute')}分钟前`
    } else if (now.diff(messageTime, 'day') < 1) {
        return messageTime.format('HH:mm')
    } else if (now.diff(messageTime, 'day') < 2) {
        return `昨天 ${messageTime.format('HH:mm')}`
    } else if (now.diff(messageTime, 'day') < 7) {
        return `${now.diff(messageTime, 'day')}天前`
    } else {
        return messageTime.format('MM-DD HH:mm')
    }
}

// 格式化日期分组标签
const formatDateGroup = (dateStr: string): string => {
    const date = dayjs(dateStr)
    const today = dayjs().startOf('day')
    const yesterday = dayjs().subtract(1, 'day').startOf('day')
    
    if (date.isSame(today, 'day')) {
        return '今天'
    } else if (date.isSame(yesterday, 'day')) {
        return '昨天'
    } else if (date.year() === today.year()) {
        return date.format('M月D日')
    } else {
        return date.format('YYYY年M月D日')
    }
}

// 获取类型标签图标
const getTypeIcon = (type: string) => {
    const typeMap: Record<string, any> = {
        'SYSTEM_ANNOUNCEMENT': InfoFilled,
        'SYSTEM_ACTIVITY': Calendar,
        'HELP_NEW_APPLICATION': QuestionFilled,
        'HELP_APPLICATION_ACCEPTED': Check,
        'HELP_APPLICATION_REJECTED': Delete,
        'HELP_COMPLETED': CircleCheck,
        'HELP_NEW_REVIEW': ChatDotRound,
        'GROUP_JOIN_APPLICATION': UserFilled,
        'GROUP_JOIN_APPROVED': Check,
        'GROUP_JOIN_REJECTED': Delete,
        'GROUP_INVITATION': Promotion,
        'GROUP_ANNOUNCEMENT': InfoFilled,
        'GROUP_ADMIN_ASSIGNED': Setting,
        'FRIEND_REQUEST': User, 
        'FRIEND_REQUEST_ACCEPTED': Check,
        'FRIEND_REQUEST_REJECTED': Delete,
        'FRIEND_REMOVED': Connection,
        'USER_STATUS': Setting,
        'USER_PASSWORD_RESET': User,
        'POST_STATUS': Document,
        'POST_DELETE': Delete,
        'GROUP_STATUS': UserFilled,
        'GROUP_DELETE': Delete,
        'HELPINFO_STATUS': Service,
        'HELPINFO_DELETE': Delete,
    }
    return typeMap[type] || WarningFilled
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
        'FRIEND_REQUEST': 'primary', 
        'FRIEND_REQUEST_ACCEPTED': 'success',
        'FRIEND_REQUEST_REJECTED': 'danger',
        'FRIEND_REMOVED': 'danger',
        'USER_STATUS': 'info',
        'USER_PASSWORD_RESET': 'warning',
        'POST_STATUS': 'info',
        'POST_DELETE': 'danger',
        'GROUP_STATUS': 'info',
        'GROUP_DELETE': 'danger',
        'HELPINFO_STATUS': 'info',
        'HELPINFO_DELETE': 'danger',
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
        'FRIEND_REQUEST': '好友申请',
        'FRIEND_REQUEST_ACCEPTED': '申请通过',
        'FRIEND_REQUEST_REJECTED': '申请拒绝',
        'FRIEND_REMOVED': '好友移除',
        'USER_STATUS': '账号状态变更',
        'USER_PASSWORD_RESET': '密码重置',
        'POST_STATUS': '帖子状态变更',
        'POST_DELETE': '帖子被删除',
        'GROUP_STATUS': '小组状态变更',
        'GROUP_DELETE': '小组被删除',
        'HELPINFO_STATUS': '互助任务状态变更',
        'HELPINFO_DELETE': '互助任务被删除',
    }
    return typeMap[type] || type
}

// 刷新通知列表
const refreshNotifications = () => {
    fetchNotifications()
    ElMessage.success('已刷新通知列表')
}

// 获取通知列表
const fetchNotifications = async () => {
    loading.value = true
    try {
        console.log('Fetching notifications with type:', props.type)
        let params: any = {
            page: currentPage.value,
            size: pageSize.value,
            type: props.type
        }
        
        // 根据筛选条件添加参数
        if (filterStatus.value !== 'all') {
            params.isRead = filterStatus.value === 'read'
        }
        
        const { data } = await getNotifications(params)
        console.log('API response:', data)
        console.log('Data structure:', data.data)
        notifications.value = data.data.records
        total.value = data.data.total
        console.log('Set notifications:', notifications.value)
        console.log('Set total:', total.value)
    } catch (error) {
        console.error('获取通知失败:', error)
        ElMessage.error('获取通知列表失败')
        notifications.value = []
        total.value = 0
    } finally {
        loading.value = false
    }
}

// 处理筛选变化
const handleFilterChange = () => {
    currentPage.value = 1
    fetchNotifications()
}

// 处理通知点击
const handleNotificationClick = (notification: NotificationItem) => {
    console.log('点击通知详情:', notification)
    console.log('通知类型:', notification.type)
    console.log('link', notification.relatedLink)
    
    // 高亮显示被点击的通知
    activeNotification.value = notification.notificationId
    setTimeout(() => {
        activeNotification.value = null
    }, 1000)
    
    if (!notification.isRead) {
        markAsRead(notification.notificationId)
    }
    
    if (notification.relatedLink) {
        // 管理员相关类型的跳转适配
        if (notification.type === 'USER_STATUS' || notification.type === 'USER_PASSWORD_RESET') {
            router.push('/profile')
            return
        }
        if ((notification.type === 'POST_STATUS' || notification.type === 'POST_DELETE') && notification.relatedId) {
            // 假设后端relatedLink为/posts/:id 或 /groups/:gid/posts/:id
            router.push(notification.relatedLink)
            return
        }
        if ((notification.type === 'GROUP_STATUS' || notification.type === 'GROUP_DELETE') && notification.relatedId) {
            router.push(`/groups/${notification.relatedId}/detail`)
            return
        }
        if ((notification.type === 'HELPINFO_STATUS' || notification.type === 'HELPINFO_DELETE') && notification.relatedId) {
            router.push(`/helpinfo/${notification.relatedId}`)
            return
        }
        // 其他类型默认
        router.push(notification.relatedLink)
    } else {
        console.warn('通知没有关联链接')
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

// 标记为未读
const markAsUnread = async (notificationId: number) => {
    // 这里假设后端有此API，如果没有可以添加
    try {
        // 模拟API调用
        // const { data } = await markNotificationAsUnread(notificationId)
        // 直接在前端修改状态
        const index = notifications.value.findIndex(item => item.notificationId === notificationId)
        if (index !== -1) {
            notifications.value[index].isRead = false
            ElMessage.success('已标记为未读')
        }
    } catch (error) {
        ElMessage.error('标记未读失败')
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
    } else if (command === 'markAsUnread') {
        markAsUnread(notification.notificationId)
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

// 滚动到顶部
const scrollToTop = () => {
    if (notificationListRef.value) {
        notificationListRef.value.scrollTop = 0
    }
}

// 监听类型变化
watch(() => props.type, () => {
    currentPage.value = 1
    fetchNotifications()
    nextTick(() => {
        scrollToTop()
    })
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
    position: relative;
}

.notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.notification-header h2 {
    margin: 0;
    color: var(--el-text-color-primary);
    font-size: 22px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 12px;
}

.title-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    background-color: var(--el-color-primary-light-8);
    border-radius: 50%;
    color: var(--el-color-primary);
}

[data-theme="dark"] .title-icon {
    background-color: var(--dark-card-bg);
    color: var(--el-color-primary-light-3);
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

.notification-filter {
    margin-bottom: 16px;
}

@keyframes bounce {
    0%, 20%, 50%, 80%, 100% {
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
    gap: 8px;
}

.refresh-button {
    display: flex;
    align-items: center;
    gap: 4px;
}

.notification-list {
    flex: 1;
    overflow-y: auto;
    padding-right: 4px;
    margin-bottom: 16px;
    position: relative;
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

.notification-timeline {
    position: relative;
}

.notification-date-group {
    position: relative;
    margin-bottom: 28px;
}

.date-divider {
    display: flex;
    align-items: center;
    margin: 24px 0 16px;
    position: relative;
    z-index: 1;
}

.date-divider::before {
    content: '';
    flex: 1;
    height: 1px;
    background-color: var(--el-border-color-lighter);
    margin-right: 16px;
}

.date-divider::after {
    content: '';
    flex: 1;
    height: 1px;
    background-color: var(--el-border-color-lighter);
    margin-left: 16px;
}

.date-label {
    background-color: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 13px;
    font-weight: 500;
    white-space: nowrap;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

[data-theme="dark"] .date-label {
    background-color: var(--dark-card-bg);
    color: var(--el-color-primary-light-3);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
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

.notification-active {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
    animation: highlight 1s ease-in-out;
}

@keyframes highlight {
    0% {
        background-color: var(--el-color-primary-light-8);
    }
    100% {
        background-color: var(--el-bg-color-page);
    }
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

[data-theme="dark"] .unread-dot {
    border-color: var(--dark-card-bg);
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
    flex-wrap: wrap;
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
    display: flex;
    align-items: center;
    gap: 4px;
}

.notification-time .el-icon {
    opacity: 0.7;
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
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
}

.tag-icon {
    margin-right: 4px;
}

.link-tag {
    display: flex;
    align-items: center;
    gap: 4px;
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
    0%, 100% {
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
[data-theme="dark"] .notification-unread {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.15), rgba(64, 158, 255, 0.1));
    border-left-color: var(--el-color-primary);
}

[data-theme="dark"] .notification-unread:hover {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(64, 158, 255, 0.15));
}

[data-theme="dark"] .notification-active {
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
}

[data-theme="dark"] .sender-name {
    background-color: rgba(64, 158, 255, 0.15);
    color: var(--el-color-primary-light-3);
}

[data-theme="dark"] .notification-read .sender-name {
    background-color: var(--dark-bg-secondary);
    color: var(--dark-text-secondary);
}

[data-theme="dark"] .notification-item {
    background-color: var(--dark-card-bg);
    border-color: var(--dark-border-lighter);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

[data-theme="dark"] .notification-read {
    background-color: var(--dark-card-bg);
}

[data-theme="dark"] .notification-item:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
    border-color: var(--dark-border-color);
}

[data-theme="dark"] .unread-badge {
    background: linear-gradient(135deg, var(--el-color-danger-dark), #ff6b68);
    box-shadow: 0 2px 4px rgba(255, 77, 79, 0.2);
}

[data-theme="dark"] .read-badge {
    background-color: var(--dark-bg-secondary);
    color: var(--dark-text-secondary);
}

[data-theme="dark"] .el-dropdown-link:hover {
    background-color: var(--dark-bg-hover);
    color: var(--el-color-primary-light-3);
}

[data-theme="dark"] .notification-pagination {
    border-top-color: var(--dark-border-light);
}

[data-theme="dark"] .notification-filter .el-radio-button__inner {
    background-color: var(--dark-card-bg);
    color: var(--dark-text-regular);
    border-color: var(--dark-border-color);
}

[data-theme="dark"] .notification-filter .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    background-color: var(--el-color-primary);
    color: white;
    border-color: var(--el-color-primary);
    box-shadow: -1px 0 0 0 var(--el-color-primary);
}

[data-theme="dark"] .el-divider {
    background-color: var(--dark-border-lighter);
}

/* 空状态样式优化 */
.el-empty {
    padding: 60px 20px;
}

.empty-icon-wrapper {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background-color: var(--el-color-primary-light-9);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
}

.empty-icon-wrapper .el-icon {
    font-size: 40px;
    color: var(--el-color-primary);
}

[data-theme="dark"] .empty-icon-wrapper {
    background-color: var(--dark-card-bg);
}

[data-theme="dark"] .empty-icon-wrapper .el-icon {
    color: var(--el-color-primary-light-3);
}

/* 骨架屏样式优化 */
.loading-skeleton {
    padding: 20px 0;
}

.loading-skeleton .el-skeleton {
    padding: 16px;
    margin-bottom: 16px;
    background-color: var(--el-bg-color-page);
    border-radius: 12px;
    border: 1px solid var(--el-border-color-lighter);
}

[data-theme="dark"] .loading-skeleton .el-skeleton {
    background-color: var(--dark-card-bg);
    border-color: var(--dark-border-lighter);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .notification-center {
        padding: 16px 12px;
    }
    
    .notification-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
    }
    
    .notification-actions {
        width: 100%;
        justify-content: flex-end;
    }
    
    .notification-item {
        padding: 12px;
    }
    
    .notification-avatar {
        margin-right: 12px;
    }
    
    .notification-avatar .el-avatar {
        width: 40px;
        height: 40px;
    }
    
    .notification-title {
        font-size: 14px;
    }
    
    .notification-body {
        font-size: 13px;
    }
}
</style>
