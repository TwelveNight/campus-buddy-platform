<template>
    <nav class="navbar" :class="{ 'auth-page': isAuthPage }">
        <!-- 这里是现有的NavBar组件模板 -->
    </nav>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';
import {
    getNotifications,
    getUnreadNotificationCount,
    markNotificationAsRead,
    markAllNotificationsAsRead
} from '@/api/notification';
import { getUnreadMessageCount } from '@/api/message';
import type { NotificationItem } from '@/types/notification';
import webSocketService from '@/utils/websocket';
import { ElMessage, ElMessageBox } from 'element-plus';
import ThemeSwitch from './ThemeSwitch.vue';

// 设置dayjs
dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 状态
const unreadCount = ref(0);
const unreadMessageCount = ref(0);
const recentNotifications = ref<NotificationItem[]>([]);
const notificationsLoading = ref(false);
const notificationPollingInterval = ref<number | null>(null);

// 计算属性
const activeIndex = computed(() => route.path);
const isAdmin = computed(() => authStore.isAdmin);
const isAuthPage = computed(() => route.path === '/login' || route.path === '/register');
const avatarUrl = computed(() => {
    const url = authStore.user?.avatarUrl;
    if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
    const timestamp = authStore.avatarUpdateTime || Date.now();
    return `${url}?v=${timestamp}`;
});

// 格式化时间
const formatTime = (time: string) => {
    if (!time) return '';
    return dayjs(time).fromNow();
};

// 获取未读通知数量
const fetchUnreadCount = async () => {
    try {
        const res = await getUnreadNotificationCount();
        if (res.data.code === 200) {
            unreadCount.value = res.data.data.count || 0;
        }
    } catch (error) {
        console.error('获取未读通知数量失败', error);
    }
};

// 获取未读消息数量
const fetchUnreadMessageCount = async () => {
    try {
        const res = await getUnreadMessageCount();
        if (res.data.code === 200) {
            unreadMessageCount.value = res.data.data.count || 0;
        }
    } catch (error) {
        console.error('获取未读消息数量失败', error);
    }
};

// 获取最近通知列表
const fetchRecentNotifications = async () => {
    notificationsLoading.value = true;
    try {
        const res = await getNotifications({ page: 1, size: 10, type: 'all' });
        if (res.data.code === 200) {
            recentNotifications.value = res.data.data.records || [];
        }
    } catch (error) {
        console.error('获取最近通知失败', error);
    } finally {
        notificationsLoading.value = false;
    }
};

// 处理通知点击
const handleNotificationClick = (notification: NotificationItem) => {
    if (!notification.isRead) {
        markAsRead(notification.notificationId);
    }
    if (notification.relatedLink) {
        router.push(notification.relatedLink);
    }
};

// 标记为已读
const markAsRead = async (notificationId: number) => {
    try {
        const res = await markNotificationAsRead(notificationId);
        if (res.data.code === 200) {
            const index = recentNotifications.value.findIndex(item => item.notificationId === notificationId);
            if (index !== -1) {
                recentNotifications.value[index].isRead = true;
            }
            fetchUnreadCount();
        }
    } catch (error) {
        console.error('标记已读失败', error);
    }
};

// 标记全部为已读
const markAllAsRead = async () => {
    try {
        const res = await markAllNotificationsAsRead();
        if (res.data.code === 200) {
            recentNotifications.value.forEach(item => {
                item.isRead = true;
            });
            unreadCount.value = 0;
            ElMessage.success('已将全部通知标记为已读');
            fetchRecentNotifications();
        }
    } catch (error) {
        console.error('标记全部已读失败', error);
        ElMessage.error('操作失败');
    }
};

// 处理通知下拉菜单切换
const handleNotificationDropdownToggle = (visible: boolean) => {
    if (visible) {
        fetchRecentNotifications();
    }
};

// ===== WebSocket相关功能 =====

// 处理WebSocket收到的通知
const handleWebSocketNotification = (data: any) => {
    if (!data || data.type !== 'NOTIFICATION') return;

    // 更新未读通知计数
    fetchUnreadCount();

    // 如果通知下拉菜单是打开的，更新通知列表
    if (document.querySelector('.notification-dropdown')?.parentElement?.style.display !== 'none') {
        fetchRecentNotifications();
    }

    // 注意：声音通知已移除以解决兼容性问题
};

// 处理WebSocket收到的私信
const handleWebSocketMessage = (data: any) => {
    if (!data || data.type !== 'PRIVATE_MESSAGE') return;
    
    // 更新未读消息计数
    fetchUnreadMessageCount();

    // 注意：声音通知已移除以解决兼容性问题
};

// 初始化WebSocket连接
const initWebSocket = () => {
    if (authStore.isAuthenticated && authStore.user?.userId) {
        // 连接WebSocket
        webSocketService.connect(authStore.user.userId);

        // 添加通知监听器
        webSocketService.addNotificationListener(handleWebSocketNotification);

        // 添加消息监听器
        webSocketService.addMessageListener(handleWebSocketMessage);

        // 添加连接状态监听器
        webSocketService.addConnectionListener((status: boolean) => {
            console.log('WebSocket连接状态:', status ? '已连接' : '已断开');
        });
    }
};

// 设置定期轮询未读通知数量
const setupNotificationPolling = () => {
    if (authStore.isAuthenticated) {
        fetchUnreadCount();
        notificationPollingInterval.value = window.setInterval(() => {
            fetchUnreadCount();
        }, 60000); // 每分钟检查一次
    }
};

// 清除轮询
const clearNotificationPolling = () => {
    if (notificationPollingInterval.value) {
        clearInterval(notificationPollingInterval.value);
        notificationPollingInterval.value = null;
    }
};

// 退出登录
const handleLogout = () => {
    authStore.logout();
    router.push('/login');
};

// 导航到小组页面的不同标签
const navigateToGroups = (tab: string) => {
    if ((tab === 'joined' || tab === 'created') && !authStore.isAuthenticated) {
        ElMessageBox.alert('请先登录后再查看小组', '提示', {
            confirmButtonText: '确定',
            callback: () => {
                router.push('/login');
            }
        });
        return;
    }

    router.push({
        path: '/groups',
        query: { tab: tab }
    });
};

// 显示创建小组对话框
const showCreateGroupDialog = () => {
    if (!authStore.isAuthenticated) {
        ElMessageBox.alert('请先登录后再创建小组', '提示', {
            confirmButtonText: '确定',
            callback: () => {
                router.push('/login');
            }
        });
        return;
    }

    router.push({
        path: '/groups',
        query: { action: 'create' }
    }).catch(err => {
        console.error('导航失败:', err);
    });
};

// 监听用户认证状态
watch(() => authStore.isAuthenticated, (isAuthenticated) => {
    if (isAuthenticated) {
        fetchUnreadCount();
        fetchUnreadMessageCount();
        initWebSocket();
    } else {
        webSocketService.disconnect();
    }
});

// 生命周期钩子
onMounted(() => {
    // 组件挂载时检查管理员状态
    if (authStore.isAuthenticated) {
        try {
            authStore.checkAdminStatus();
        } catch (error) {
            console.error('NavBar组件中检查管理员状态失败:', error);
        }
    }

    // 设置通知轮询
    setupNotificationPolling();

    // 获取未读消息数量
    if (authStore.isAuthenticated) {
        fetchUnreadMessageCount();
    }

    // 初始化WebSocket连接
    initWebSocket();
});

onBeforeUnmount(() => {
    clearNotificationPolling();

    // 断开WebSocket连接
    webSocketService.disconnect();
});
</script>

<style scoped>
/* 保留原有样式 */
</style>
