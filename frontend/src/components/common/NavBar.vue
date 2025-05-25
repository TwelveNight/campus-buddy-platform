<template>
    <nav class="navbar" :class="{ 'auth-page': isAuthPage }">
        <div class="container">
            <router-link to="/" class="logo">
                <img src="../../assets/vue.svg" alt="学伴" />
                <h1>学伴</h1>
            </router-link>

            <div class="menu" v-if="!isAuthPage">
                <el-menu mode="horizontal" router :default-active="activeIndex" background-color="transparent"
                    text-color="var(--text-primary)" active-text-color="var(--primary-color)">

                    <!-- 首页 -->
                    <el-menu-item index="/">
                        <el-icon>
                            <House />
                        </el-icon>
                        首页
                    </el-menu-item>

                    <!-- 互助任务下拉菜单 -->
                    <el-sub-menu index="/helpinfo">
                        <template #title>
                            <el-icon class="menu-icon">
                                <Service />
                            </el-icon>
                            校园互助
                        </template>
                        <el-menu-item index="/helpinfo">
                            <el-icon>
                                <Collection />
                            </el-icon>
                            浏览互助
                        </el-menu-item>
                        <el-menu-item index="/helpinfo/publish" v-if="authStore.isAuthenticated">
                            <el-icon>
                                <Plus />
                            </el-icon>
                            发布互助
                        </el-menu-item>
                        <el-menu-item index="/my/helpinfo">
                            <el-icon>
                                <Document />
                            </el-icon>
                            我的互助
                        </el-menu-item>
                        <el-menu-item index="/applications">
                            <el-icon>
                                <List />
                            </el-icon>
                            我的申请
                        </el-menu-item>
                        <el-menu-item index="/reviews">
                            <el-icon>
                                <Star />
                            </el-icon>
                            我的评价
                        </el-menu-item>
                    </el-sub-menu>

                    <!-- 学习小组 -->
                    <el-sub-menu index="/groups">
                        <template #title>
                            <el-icon class="menu-icon">
                                <UserFilled />
                            </el-icon>
                            学习小组
                        </template>
                        <el-menu-item index="/groups" @click="navigateToGroups('all')">
                            <el-icon>
                                <Collection />
                            </el-icon>
                            浏览小组
                        </el-menu-item>
                        <el-menu-item v-if="authStore.isAuthenticated" @click="navigateToGroups('joined')">
                            <el-icon>
                                <Star />
                            </el-icon>
                            我加入的小组
                        </el-menu-item>
                        <el-menu-item v-if="authStore.isAuthenticated" @click="navigateToGroups('created')">
                            <el-icon>
                                <Edit />
                            </el-icon>
                            我创建的小组
                        </el-menu-item>
                        <el-menu-item v-if="authStore.isAuthenticated" @click="showCreateGroupDialog">
                            <el-icon>
                                <Plus />
                            </el-icon>
                            创建小组
                        </el-menu-item>
                    </el-sub-menu>

                    <!-- 管理后台 -->
                    <el-menu-item index="/admin/helpinfo" v-if="isAdmin">
                        <el-icon>
                            <Setting />
                        </el-icon>
                        管理后台
                    </el-menu-item>
                </el-menu>
            </div>

            <div class="user-actions">
                <ThemeSwitch />
                <template v-if="authStore.isAuthenticated">
                    <!-- 私信图标 -->
                    <div class="message-icon">
                        <el-badge :value="unreadMessageCount" :max="99" :hidden="unreadMessageCount === 0"
                            type="danger">
                            <router-link to="/messages">
                                <el-icon class="icon-button">
                                    <ChatDotRound />
                                </el-icon>
                            </router-link>
                        </el-badge>
                    </div>
                    <!-- 通知图标 -->
                    <div class="notification-icon">
                        <el-dropdown trigger="click" @visible-change="handleNotificationDropdownToggle">
                            <div>
                                <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" type="danger">
                                    <el-icon class="bell-icon">
                                        <Bell />
                                    </el-icon>
                                </el-badge>
                            </div>
                            <template #dropdown>
                                <el-dropdown-menu class="notification-dropdown">
                                    <div class="notification-dropdown-header">
                                        <h3>通知
                                            <span v-if="unreadCount > 0" class="unread-count-badge">{{ unreadCount
                                                }}</span>
                                        </h3>
                                        <el-button type="text" @click="markAllAsRead"
                                            :disabled="recentNotifications.length === 0 || unreadCount === 0">
                                            全部已读
                                        </el-button>
                                    </div>
                                    <el-divider class="m-0" />
                                    <div class="notification-list" v-loading="notificationsLoading">
                                        <template v-if="recentNotifications.length > 0">
                                            <el-dropdown-item v-for="notification in recentNotifications"
                                                :key="notification.notificationId"
                                                @click="handleNotificationClick(notification)" class="notification-item"
                                                :class="{
                                                    'notification-unread': !notification.isRead,
                                                    'notification-read': notification.isRead
                                                }">
                                                <div class="notification-content">
                                                    <div class="notification-title">
                                                        <span v-if="!notification.isRead"
                                                            class="unread-indicator">●</span>
                                                        <span v-else class="read-indicator">○</span>
                                                        {{ notification.title }}
                                                        <span v-if="!notification.isRead" class="unread-badge">未读</span>
                                                        <span v-else class="read-badge">已读</span>
                                                    </div>
                                                    <div class="notification-body">{{ notification.content }}</div>
                                                    <div class="notification-time">{{ formatTime(notification.createdAt)
                                                        }}</div>
                                                </div>
                                            </el-dropdown-item>
                                        </template>
                                        <el-empty v-else description="暂无通知" :image-size="60" />
                                    </div>
                                    <el-divider class="m-0" />
                                    <div class="notification-footer">
                                        <router-link to="/notifications">查看全部通知</router-link>
                                    </div>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                    <div class="user-avatar-container">
                        <el-dropdown trigger="hover">
                            <div class="avatar-wrapper">
                                <el-avatar :size="36" :src="avatarUrl"></el-avatar>
                                <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username || '用户'
                                }}</span>
                                <el-icon class="dropdown-icon">
                                    <ArrowDown />
                                </el-icon>
                            </div>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="$router.push('/profile')">
                                        <el-icon>
                                            <User />
                                        </el-icon>个人中心
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="navigateToGroups('joined')">
                                        <el-icon>
                                            <UserFilled />
                                        </el-icon>我的小组
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="$router.push('/my/helpinfo')">
                                        <el-icon>
                                            <List />
                                        </el-icon>我的互助
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="$router.push('/applications')">
                                        <el-icon>
                                            <Document />
                                        </el-icon>我的申请
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="$router.push('/reviews')">
                                        <el-icon>
                                            <Star />
                                        </el-icon>我的评价
                                    </el-dropdown-item>
                                    <el-dropdown-item divided @click="handleLogout">
                                        <el-icon>
                                            <SwitchButton />
                                        </el-icon>退出登录
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </template>
                <template v-else>
                    <el-button type="primary" class="login-btn" round @click="$router.push('/login')">登录</el-button>
                    <el-button class="register-btn" round @click="$router.push('/register')">注册</el-button>
                </template>
            </div>
        </div>
    </nav>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
import ThemeSwitch from './ThemeSwitch.vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { getNotifications, getUnreadNotificationCount, markAllNotificationsAsRead, markNotificationAsRead } from '@/api/notification'
import { getUnreadMessageCount } from '@/api/message'
import type { NotificationItem } from '@/types/notification'
import webSocketService from '@/utils/websocket'
import {
    User,
    House,
    Service,
    Setting,
    Document,
    List,
    SwitchButton,
    UserFilled,
    Plus,
    Star,
    Edit,
    Collection,
    Bell,
    ArrowDown,
    ChatDotRound
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeIndex = computed(() => route.path)

// 使用authStore中的isAdmin getter
const isAdmin = computed(() => {
    return authStore.isAdmin;
})

// 组件挂载时进行初始化
// 判断是否为认证页面（登录/注册）
const isAuthPage = computed(() => {
    return route.path === '/login' || route.path === '/register'
})

// 创建计算属性，当avatarUpdateTime变化时会自动更新
const avatarUrl = computed(() => {
    const url = authStore.user?.avatarUrl;
    if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

    // 使用avatarUpdateTime属性作为时间戳，确保头像更新时刷新缓存
    const timestamp = authStore.avatarUpdateTime || Date.now();
    return `${url}?v=${timestamp}`;
});

// 通知相关状态
const unreadCount = ref(0)
const unreadMessageCount = ref(0)
const recentNotifications = ref<any[]>([])
const notificationsLoading = ref(false)
const notificationPollingInterval = ref<any>(null)

// 格式化时间
const formatTime = (time: string) => {
    if (!time) return ''
    return dayjs(time).fromNow()
}

// 获取未读通知数量
const fetchUnreadCount = async () => {
    try {
        const res = await getUnreadNotificationCount()
        if (res.data.code === 200) {
            unreadCount.value = res.data.data.count || 0
        }
    } catch (error) {
        console.error('获取未读通知数量失败', error)
    }
}

// 获取未读消息数量
const fetchUnreadMessageCount = async () => {
    try {
        const res = await getUnreadMessageCount()
        if (res.data.code === 200) {
            unreadMessageCount.value = res.data.data.count || 0
        }
    } catch (error) {
        console.error('获取未读消息数量失败', error)
        // 添加重试机制，5秒后重试
        setTimeout(() => {
            if (authStore.isAuthenticated) {
                fetchUnreadMessageCount()
            }
        }, 5000)
    }
}

// 获取最近通知列表
const fetchRecentNotifications = async () => {
    notificationsLoading.value = true
    try {
        // 获取最新的10条通知，type传'all'，后端已做未读优先排序
        const res = await getNotifications({ page: 1, size: 10, type: 'all' })
        if (res.data.code === 200) {
            // 直接展示，未读在前，已读在后
            recentNotifications.value = res.data.data.records || []
        }
    } catch (error) {
        console.error('获取最近通知失败', error)
    } finally {
        notificationsLoading.value = false
    }
}

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
        const res = await markNotificationAsRead(notificationId)
        if (res.data.code === 200) {
            // 更新本地状态
            const index = recentNotifications.value.findIndex(item => item.notificationId === notificationId)
            if (index !== -1) {
                recentNotifications.value[index].isRead = true
            }
            // 刷新未读数量
            fetchUnreadCount()
        }
    } catch (error) {
        console.error('标记已读失败', error)
    }
}

// 标记全部为已读
const markAllAsRead = async () => {
    try {
        const res = await markAllNotificationsAsRead()
        if (res.data.code === 200) {
            // 更新本地状态
            recentNotifications.value.forEach(item => {
                item.isRead = true
            })
            unreadCount.value = 0
            ElMessage.success('已将全部通知标记为已读')
            // 刷新通知列表
            fetchRecentNotifications()
        }
    } catch (error) {
        console.error('标记全部已读失败', error)
        ElMessage.error('操作失败')
    }
}

// 处理通知下拉菜单切换
const handleNotificationDropdownToggle = (visible: boolean) => {
    if (visible) {
        fetchRecentNotifications()
    }
}

// 设置定期轮询未读通知数量
const setupNotificationPolling = () => {
    if (authStore.isAuthenticated) {
        fetchUnreadCount(); // 立即获取一次
        fetchUnreadMessageCount(); // 立即获取一次
        notificationPollingInterval.value = setInterval(() => {
            fetchUnreadCount();
            fetchUnreadMessageCount();
        }, 60000); // 每分钟检查一次
    }
}

// 清除轮询
const clearNotificationPolling = () => {
    if (notificationPollingInterval.value) {
        clearInterval(notificationPollingInterval.value)
        notificationPollingInterval.value = null
    }
}

// ===== WebSocket相关功能 =====

// 处理WebSocket收到的通知
const handleWebSocketNotification = (data: any) => {
    console.log('收到WebSocket通知:', data);
    
    if (!data || data.type !== 'NOTIFICATION') {
        console.warn('收到无效通知数据:', data);
        return;
    }

    try {
        // 更新未读通知计数
        fetchUnreadCount();

        // 如果通知下拉菜单是打开的，更新通知列表
        if (document.querySelector('.notification-dropdown')?.parentElement?.style.display !== 'none') {
            fetchRecentNotifications();
        }

        // 显示通知提示
        ElMessage({
            message: data.content || '您有一条新通知',
            type: 'info',
            duration: 3000
        });

        // 播放通知提示音
        import('@/utils/sound').then(({ playNotificationSound }) => {
            playNotificationSound();
        }).catch(err => console.error('加载声音模块失败:', err));
    } catch (error) {
        console.error('处理WebSocket通知时出错:', error);
    }
};

// 处理WebSocket收到的私信
const handleWebSocketMessage = (data: any) => {
    console.log('收到WebSocket私信:', data);
    
    if (!data || data.type !== 'PRIVATE_MESSAGE') {
        console.warn('收到无效私信数据:', data);
        return;
    }
    
    try {
        // 更新未读消息计数
        fetchUnreadMessageCount();

        // 显示私信提示
        if (data.sender && data.content) {
            ElMessage({
                message: `${data.sender.nickname || data.sender.username || '用户'}: ${data.content}`,
                type: 'success',
                duration: 5000
            });
        }

        // 播放消息提示音
        import('@/utils/sound').then(({ playMessageSound }) => {
            playMessageSound();
        }).catch(err => console.error('加载声音模块失败:', err));
    } catch (error) {
        console.error('处理WebSocket私信时出错:', error);
    }
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

onMounted(async () => {
    // 如果已登录，初始化WebSocket和获取用户数据
    if (authStore.isAuthenticated && authStore.user) {
        try {
            // 检查管理员状态
            await authStore.checkAdminStatus();
            // 初始化WebSocket连接
            initWebSocket();
            // 获取通知和消息数量
            fetchUnreadCount();
            fetchUnreadMessageCount();
            // 设置通知轮询
            setupNotificationPolling();
            console.log('NavBar组件已初始化WebSocket和通知系统');
        } catch (error) {
            console.error('NavBar组件初始化失败:', error);
        }
    } else {
        // 设置通知轮询
        setupNotificationPolling();
    }
})

onBeforeUnmount(() => {
    clearNotificationPolling();
    // 断开WebSocket连接
    if (authStore.isAuthenticated) {
        webSocketService.disconnect();
        console.log('NavBar组件卸载，WebSocket连接已断开');
    }
    
    // 清除所有可能的定时器
    const timers = [notificationPollingInterval.value];
    timers.forEach(timer => {
        if (timer) {
            clearInterval(timer);
        }
    });
})

// 退出登录
const handleLogout = () => {
    // 显示确认框
    ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            await authStore.logout();
            router.push('/login');
            ElMessage.success('已退出登录');
        } catch (error) {
            console.error('退出登录失败', error);
            ElMessage.error('退出登录失败');
        }
    }).catch(() => { });
};

// 导航到小组页面的不同标签
const navigateToGroups = (tab: string) => {
    // 确保用户已登录（对于需要身份验证的标签）
    if ((tab === 'joined' || tab === 'created') && !authStore.isAuthenticated) {
        ElMessageBox.alert('请先登录后再查看小组', '提示', {
            confirmButtonText: '确定',
            callback: () => {
                router.push('/login');
            }
        });
        return;
    }

    // 设置重定向标志，以便在 GroupList 组件中捕获
    router.push({
        path: '/groups',
        query: { tab: tab }
    });
}

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

    // 导航到小组页面并传递创建参数
    router.push({
        path: '/groups',
        query: { action: 'create' }
    }).then(() => {
    }).catch(err => {
        console.error('导航失败:', err);
    });
}
</script>

<style scoped>
.navbar {
    background-color: var(--card-bg);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    padding: 0.5rem 0;
    position: sticky;
    top: 0;
    z-index: 1000;
    transition: all 0.3s ease;
}

.navbar.auth-page {
    background-color: transparent;
    box-shadow: none;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    height: 60px;
}

.logo {
    display: flex;
    align-items: center;
    text-decoration: none;
    gap: 10px;
    transition: transform 0.3s ease;
}

.logo:hover {
    transform: scale(1.05);
}

.logo img {
    height: 32px;
    width: auto;
}

.logo h1 {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--primary-color);
    margin: 0;
}

.menu {
    flex-grow: 1;
    margin-left: 40px;
}

/* Override Element Plus menu styles */
:deep(.el-menu--horizontal) {
    border-bottom: none !important;
    background-color: transparent !important;
}

:deep(.el-menu--horizontal > .el-menu-item) {
    height: 60px;
    line-height: 60px;
    padding: 0 16px;
    border-bottom: 2px solid transparent !important;
    transition: all 0.3s ease;
}

:deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title) {
    height: 60px;
    line-height: 60px;
    padding: 0 16px;
    border-bottom: 2px solid transparent !important;
    transition: all 0.3s ease;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active),
:deep(.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title) {
    border-bottom: 2px solid var(--primary-color) !important;
    font-weight: 500;
    color: var(--primary-color) !important;
}

:deep(.el-menu--horizontal > .el-menu-item:hover),
:deep(.el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title) {
    color: var(--primary-color) !important;
    background-color: rgba(64, 158, 255, 0.05) !important;
}

/* 子菜单样式优化 */
:deep(.el-menu--popup) {
    min-width: 160px;
    padding: 8px 0;
    border-radius: 8px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

:deep(.el-menu--popup .el-menu-item) {
    height: 40px;
    line-height: 40px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    border-radius: 4px;
    margin: 0 5px;
    transition: all 0.2s ease;
}

:deep(.el-menu--popup .el-menu-item:hover) {
    background-color: rgba(64, 158, 255, 0.1) !important;
}

:deep(.el-menu--popup .el-menu-item.is-active) {
    background-color: rgba(64, 158, 255, 0.15) !important;
    color: var(--primary-color) !important;
    font-weight: 500;
}

/* 隐藏箭头图标 */
:deep(.el-menu--horizontal .el-sub-menu__icon-arrow) {
    display: none !important;
}

.menu-icon {
    margin-right: 4px;
}

.user-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.notification-icon {
    cursor: pointer;
    padding: 8px;
    border-radius: 50%;
    transition: background-color 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
}

.notification-icon:hover {
    background-color: rgba(0, 0, 0, 0.05);
}

.bell-icon {
    font-size: 20px;
    color: var(--text-secondary);
}

.message-icon {
    cursor: pointer;
    padding: 8px;
    border-radius: 50%;
    transition: background-color 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
}

.message-icon:hover {
    background-color: rgba(0, 0, 0, 0.05);
}

.icon-button {
    font-size: 20px;
    color: var(--text-secondary);
}

.user-avatar-container {
    display: flex;
    align-items: center;
}

.avatar-wrapper {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 6px 10px;
    border-radius: 20px;
    transition: all 0.3s;
}

.avatar-wrapper:hover {
    background-color: rgba(0, 0, 0, 0.05);
    transform: translateY(-2px);
}

.user-name {
    margin: 0 8px;
    font-size: 14px;
    color: var(--text-primary);
}

.dropdown-icon {
    color: var(--text-secondary);
    font-size: 12px;
    transition: transform 0.3s;
}

.avatar-wrapper:hover .dropdown-icon {
    transform: rotate(180deg);
}

.login-btn {
    background-color: var(--primary-color);
    border-color: var(--primary-color);
    font-weight: 500;
    transition: all 0.3s;
    padding: 8px 20px;
}

.login-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.register-btn {
    color: var(--primary-color);
    border-color: var(--primary-color);
    background: transparent;
    transition: all 0.3s;
    padding: 8px 20px;
}

.register-btn:hover {
    transform: translateY(-2px);
    background-color: rgba(64, 158, 255, 0.1);
}

:deep(.el-dropdown-menu) {
    border-radius: 8px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
    padding: 8px 0;
}

:deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    transition: all 0.2s;
    border-radius: 4px;
    margin: 0 4px;
    font-size: 14px;
}

:deep(.el-dropdown-menu__item:hover) {
    background-color: rgba(64, 158, 255, 0.1);
    color: var(--primary-color);
}

:deep(.el-dropdown-menu__item i) {
    font-size: 16px;
}

/* 响应式调整 */
@media (max-width: 992px) {
    .user-name {
        display: none;
    }

    .dropdown-icon {
        display: none;
    }

    .avatar-wrapper {
        padding: 4px;
    }

    .login-btn,
    .register-btn {
        padding: 6px 14px;
    }
}

/* 在移动端隐藏整个导航栏 */
@media (max-width: 768px) {
    .navbar {
        display: none !important;
    }
}

/* 平板端优化 */
@media (min-width: 769px) and (max-width: 1024px) {
    .container {
        padding: 0 16px;
    }

    .menu {
        margin-left: 20px;
    }

    :deep(.el-menu--horizontal > .el-menu-item),
    :deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title) {
        padding: 0 12px;
    }

    .logo h1 {
        font-size: 1.4rem;
    }

    .user-name {
        font-size: 13px;
    }
}

/* 桌面端显示类 */
@media (min-width: 769px) {
    .navbar {
        display: flex !important;
    }
}

/* 暗色主题适配 */
[data-theme="dark"] .navbar {
    background-color: var(--dark-bg, #1e1e20);
    /* 使用 theme.css 变量，并提供备用值 */
    box-shadow: var(--dark-shadow-light, 0 4px 12px rgba(0, 0, 0, 0.3));
}

[data-theme="dark"] .navbar.auth-page {
    background-color: transparent;
    box-shadow: none;
}

[data-theme="dark"] .logo h1 {
    color: var(--primary-color);
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-menu-item) {
    color: var(--dark-text-primary, #e5eaf3) !important;
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title) {
    color: var(--dark-text-primary, #e5eaf3) !important;
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-menu-item:hover),
[data-theme="dark"] :deep(.el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title) {
    color: var(--primary-color) !important;
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.05)) !important;
    /* 使用 theme.css 变量 */
}

/* 更新 el-menu--popup 的暗色主题样式 */
[data-theme="dark"] :deep(.el-menu--popup) {
    background-color: var(--dark-bg-secondary, #2a2a2e) !important;
    /* 使用 theme.css 变量 */
    border: 1px solid var(--dark-border-color, #4c4d4f) !important;
    /* 使用 theme.css 变量 */
    box-shadow: var(--dark-shadow-regular, 0 6px 16px rgba(0, 0, 0, 0.5)) !important;
    /* 使用 theme.css 变量 */
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item) {
    background-color: transparent !important;
    /* 子菜单项背景透明，依赖父级 popup 背景 */
    color: var(--dark-text-regular, #cfd3dc) !important;
    /* 使用 theme.css 变量 */
    border-bottom: none !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item .el-icon) {
    color: var(--dark-text-regular, #cfd3dc) !important;
    /* 确保图标颜色与文本一致 */
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item:hover) {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1)) !important;
    /* 保持现有 hover 效果或统一为 theme.css 的 var(--hover-bg) */
    color: var(--primary-color) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item:hover .el-icon) {
    color: var(--primary-color) !important;
    /* 确保 hover 时图标颜色也改变 */
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item.is-active) {
    background-color: var(--primary-color-dark-transparent, rgba(64, 158, 255, 0.2)) !important;
    /* 使用 theme.css 变量 */
    color: var(--primary-color) !important;
}

[data-theme="dark"] .notification-icon:hover {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1));
}

[data-theme="dark"] .bell-icon {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .message-icon:hover {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1));
}

[data-theme="dark"] .icon-button {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .avatar-wrapper:hover {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1));
}

[data-theme="dark"] .user-name {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .dropdown-icon {
    color: var(--dark-text-secondary, #a3a6ad);
}

/* 更新 el-dropdown-menu 的暗色主题样式 */
[data-theme="dark"] :deep(.el-dropdown-menu) {
    background-color: var(--dark-bg-secondary, #2a2a2e) !important;
    /* 使用 theme.css 变量 */
    border: 1px solid var(--dark-border-color, #4c4d4f) !important;
    /* 使用 theme.css 变量 */
    box-shadow: var(--dark-shadow-regular, 0 6px 16px rgba(0, 0, 0, 0.5)) !important;
    /* 使用 theme.css 变量 */
}

[data-theme="dark"] :deep(.el-dropdown-menu__item) {
    color: var(--dark-text-regular, #cfd3dc) !important;
    /* 使用 theme.css 变量 */
}

[data-theme="dark"] :deep(.el-dropdown-menu__item .el-icon) {
    color: var(--dark-text-regular, #cfd3dc) !important;
    /* 确保图标颜色与文本一致 */
}

[data-theme="dark"] :deep(.el-dropdown-menu__item:hover) {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1)) !important;
    /* 保持现有 hover 效果或统一为 theme.css 的 var(--hover-bg) */
    color: var(--primary-color) !important;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item:hover .el-icon) {
    color: var(--primary-color) !important;
    /* 确保 hover 时图标颜色也改变 */
}

/* 通知下拉菜单样式 */
:deep(.notification-dropdown) {
    width: 350px;
    max-height: 500px;
    overflow: hidden;
}

.notification-dropdown-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: var(--card-bg);
}

.notification-dropdown-header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    display: flex;
    align-items: center;
    gap: 8px;
}

.unread-count-badge {
    background-color: var(--el-color-danger);
    color: white;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 12px;
    font-weight: 500;
    min-width: 20px;
    text-align: center;
}

.notification-list {
    max-height: 300px;
    overflow-y: auto;
    min-height: 100px;
}

.notification-item {
    padding: 0 !important;
    border-bottom: 1px solid var(--border-color);
    cursor: pointer;
    transition: background-color 0.2s;
}

.notification-item:hover {
    background-color: var(--bg-hover) !important;
}

.notification-item.notification-unread {
    background-color: rgba(64, 158, 255, 0.1);
    position: relative;
    font-weight: 500;
    border-left: 4px solid var(--primary-color);
}

.notification-item.notification-unread::before {
    content: '';
    position: absolute;
    left: -4px;
    top: 0;
    bottom: 0;
    width: 4px;
    background-color: var(--primary-color);
}

.notification-item.notification-read {
    background-color: var(--bg-color);
    opacity: 0.85;
}

.notification-content {
    padding: 12px 16px;
    width: 100%;
}

.notification-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 4px;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.unread-indicator {
    color: var(--primary-color);
    font-size: 12px;
    margin-right: 6px;
    animation: pulse 1.5s ease-in-out infinite;
}

.read-indicator {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    margin-right: 6px;
    opacity: 0.6;
}

.unread-badge {
    background-color: var(--el-color-danger);
    color: white;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 10px;
    font-weight: 500;
    margin-left: auto;
    flex-shrink: 0;
}

.read-badge {
    background-color: var(--el-color-success);
    color: white;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 10px;
    font-weight: 500;
    margin-left: auto;
    flex-shrink: 0;
    opacity: 0.8;
}

@keyframes pulse {

    0%,
    100% {
        opacity: 1;
    }

    50% {
        opacity: 0.5;
    }
}

.notification-body {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 4px;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
}

.notification-read .notification-title {
    color: var(--text-secondary);
    font-weight: normal;
}

.notification-read .notification-body {
    color: var(--text-tertiary);
}

.notification-read .notification-time {
    color: var(--text-placeholder);
}

.notification-time {
    font-size: 12px;
    color: var(--text-tertiary);
}

.notification-footer {
    padding: 8px 16px;
    text-align: center;
    border-top: 1px solid var(--border-color);
    background-color: var(--card-bg);
}

.notification-footer a {
    color: var(--primary-color);
    text-decoration: none;
    font-size: 14px;
    font-weight: 500;
    transition: color 0.2s;
}

.notification-footer a:hover {
    color: var(--primary-color-hover);
}

/* 暗色主题通知样式 */
[data-theme="dark"] .notification-dropdown-header {
    background-color: var(--dark-bg-secondary, #2a2a2e);
}

[data-theme="dark"] .notification-dropdown-header h3 {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .unread-count-badge {
    background-color: var(--el-color-danger);
    color: white;
}

[data-theme="dark"] .notification-item {
    border-bottom-color: var(--dark-border-color, #4c4d4f);
}

[data-theme="dark"] .notification-item:hover {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1)) !important;
}

[data-theme="dark"] .notification-item.notification-unread {
    background-color: rgba(64, 158, 255, 0.15);
    border-left-color: var(--primary-color);
}

[data-theme="dark"] .notification-item.notification-read {
    background-color: var(--dark-bg-secondary);
    opacity: 0.8;
}

[data-theme="dark"] .unread-indicator {
    color: var(--primary-color);
}

[data-theme="dark"] .read-indicator {
    color: var(--dark-text-secondary, #a3a6ad);
    opacity: 0.6;
}

[data-theme="dark"] .unread-badge {
    background-color: var(--el-color-danger);
    color: white;
}

[data-theme="dark"] .read-badge {
    background-color: var(--el-color-success);
    color: white;
    opacity: 0.8;
}

[data-theme="dark"] .notification-title {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .notification-body {
    color: var(--dark-text-regular, #cfd3dc);
}

[data-theme="dark"] .notification-time {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .notification-read .notification-title {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .notification-read .notification-body {
    color: var(--dark-text-placeholder, #6c6e72);
}

[data-theme="dark"] .notification-read .notification-time {
    color: var(--dark-text-placeholder, #6c6e72);
}

[data-theme="dark"] .notification-footer {
    background-color: var(--dark-bg-secondary, #2a2a2e);
    border-top-color: var(--dark-border-color, #4c4d4f);
}
</style>
