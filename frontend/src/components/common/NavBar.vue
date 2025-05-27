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
                    <el-sub-menu index="/admin" v-if="isAdmin">
                        <template #title>
                            <el-icon class="menu-icon">
                                <Setting />
                            </el-icon>
                            管理后台
                        </template>
                        <el-menu-item index="/admin/dashboard">
                            <el-icon>
                                <Histogram />
                            </el-icon>
                            数据统计
                        </el-menu-item>
                        <el-menu-item index="/admin/users">
                            <el-icon>
                                <User />
                            </el-icon>
                            用户管理
                        </el-menu-item>
                        <el-menu-item index="/admin/helpinfo">
                            <el-icon>
                                <Document />
                            </el-icon>
                            互助任务管理
                        </el-menu-item>
                        <el-menu-item index="/admin/groups">
                            <el-icon>
                                <UserFilled />
                            </el-icon>
                            小组管理
                        </el-menu-item>
                        <el-menu-item index="/admin/posts">
                            <el-icon>
                                <List />
                            </el-icon>
                            帖子管理
                        </el-menu-item>
                        <el-menu-item @click="navigateToPublishAnnouncement">
                            <el-icon>
                                <Bell />
                            </el-icon>
                            发布公告
                        </el-menu-item>
                    </el-sub-menu>
                </el-menu>
            </div>

            <div class="user-actions">
                <ThemeSwitch />
                <template v-if="authStore.isAuthenticated">
                    <!-- 搜索用户图标 -->
                    <div class="search-user-icon">
                        <el-icon class="icon-button" @click="openUserSearch">
                            <Search />
                        </el-icon>
                    </div>
                    <!-- 好友图标 -->
                    <div class="friends-icon">
                        <router-link to="/friends">
                            <el-icon class="icon-button">
                                <UserFilled />
                            </el-icon>
                        </router-link>
                    </div>
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

    <!-- 用户搜索对话框 -->
    <UserSearchDialog ref="userSearchDialogRef" @message-user="handleMessageUser" @add-friend="handleAddFriend" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
import ThemeSwitch from './ThemeSwitch.vue'
import UserSearchDialog from './UserSearchDialog.vue'
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
    ChatDotRound,
    Histogram
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userSearchDialogRef = ref<InstanceType<typeof UserSearchDialog> | null>(null); // 用户搜索对话框引用

const activeIndex = computed(() => route.path)

// 使用authStore中的isAdmin getter
const isAdmin = computed(() => {
    return authStore.isAdmin;
})

const navigateToPublishAnnouncement = () => {
  router.push({ path: '/notifications', query: { action: 'publish-announcement' } });
};

// 打开用户搜索对话框
const openUserSearch = () => {
    userSearchDialogRef.value?.open();
}

// 处理私信用户
const handleMessageUser = (user: any) => {
    router.push(`/messages/${user.userId}`);
}

// 处理添加好友
const handleAddFriend = (user: any) => {
    ElMessage.success(`已向${user.nickname || user.username}发送好友申请`);
    // 这里可以刷新好友申请列表或者其他操作
}

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

// 处理服务器与客户端的时间差异
const serverTimeOffset = ref(0);

// 同步服务器时间（当收到PONG响应时调用）
const syncServerTime = (serverTimestamp: number) => {
    const clientTime = Date.now();
    serverTimeOffset.value = serverTimestamp - clientTime;
    console.log(`服务器时间偏移: ${serverTimeOffset.value}ms`);
};

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
            console.log('未读消息数量:', unreadMessageCount.value);
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
        }, 30000); // 每30秒检查一次，缩短轮询间隔
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

// 私信消息类型定义
interface PrivateMessagePayload {
    type: string;
    title: string;
    senderId: number;
    senderName: string;
    content: string;
    messageId: number;
    timestamp: number;
}

// 处理WebSocket收到的通知
const handleWebSocketNotification = (data: any) => {
    console.log('收到WebSocket通知:', data);

    if (!data || data.type !== 'NOTIFICATION') {
        console.warn('收到无效通知数据:', data);
        return;
    }

    try {
        // 立即增加未读通知计数（无需等待API调用）
        unreadCount.value += 1;

        // 异步刷新未读通知计数（确保数据一致性）
        setTimeout(() => {
            fetchUnreadCount();
        }, 1000);

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
        // 立即增加未读消息计数（无需等待API调用）
        unreadMessageCount.value += 1;

        // 异步刷新未读消息计数（确保数据一致性）
        setTimeout(() => {
            fetchUnreadMessageCount();
        }, 1000);

        // 显示私信提示
        if (data.senderName && data.content) {
            ElMessage({
                message: `${data.senderName || '用户'}: ${data.content}`,
                type: 'success',
                duration: 5000
            });
        }
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
            // 连接成功后立即获取未读消息和通知数量
            if (status) {
                fetchUnreadCount();
                fetchUnreadMessageCount();
            }
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
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    background: linear-gradient(135deg, 
        rgba(255, 255, 255, 0.95) 0%, 
        rgba(248, 250, 252, 0.9) 50%, 
        rgba(241, 245, 249, 0.85) 100%);
    backdrop-filter: blur(25px) saturate(180%);
    -webkit-backdrop-filter: blur(25px) saturate(180%);
    border-bottom: 1px solid rgba(148, 163, 184, 0.2);
    box-shadow: 
        0 1px 3px rgba(0, 0, 0, 0.05),
        0 4px 20px rgba(59, 130, 246, 0.08),
        inset 0 1px 0 rgba(255, 255, 255, 0.6);
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    position: relative;
    overflow: hidden;
}

/* 添加粒子动画背景 */
.navbar::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.08) 0%, transparent 50%),
        radial-gradient(circle at 40% 80%, rgba(16, 185, 129, 0.06) 0%, transparent 50%);
    animation: particlesFloat 20s ease-in-out infinite;
    pointer-events: none;
}

.navbar.auth-page {
    background-color: transparent;
    box-shadow: none;
}

.container {
    max-width: 1280px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    height: 68px;
    position: relative;
    z-index: 2;
}

.logo {
    display: flex;
    align-items: center;
    text-decoration: none;
    gap: 12px;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    margin-right: 10px;
    position: relative;
    padding: 8px 16px;
    border-radius: 16px;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.logo::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.1) 0%, 
        rgba(139, 92, 246, 0.05) 100%);
    border-radius: 16px;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.logo:hover::before {
    opacity: 1;
}

.logo:hover {
    transform: translateY(-2px) scale(1.02);
    box-shadow: 0 8px 25px rgba(59, 130, 246, 0.2);
}

.logo img {
    height: 36px;
    width: auto;
    filter: drop-shadow(0 2px 4px rgba(59, 130, 246, 0.2));
    animation: logoFloat 4s ease-in-out infinite;
}

.logo h1 {
    font-size: 1.6rem;
    font-weight: 700;
    background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin: 0;
    position: relative;
    z-index: 1;
}

.menu {
    flex-grow: 1;
    margin-left: 50px;
    /* 增加左侧边距 */
}

/* Override Element Plus menu styles */
:deep(.el-menu--horizontal) {
    border-bottom: none !important;
    background-color: transparent !important;
}

:deep(.el-menu--horizontal > .el-menu-item) {
    height: 68px;
    line-height: 68px;
    padding: 0 20px;
    border-bottom: 3px solid transparent !important;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    border-radius: 12px 12px 0 0;
    margin: 0 4px;
    position: relative;
    overflow: hidden;
}

:deep(.el-menu--horizontal > .el-menu-item::before) {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.05) 0%, 
        rgba(139, 92, 246, 0.03) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
}

:deep(.el-menu--horizontal > .el-menu-item:hover::before) {
    opacity: 1;
}

:deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title) {
    height: 68px;
    line-height: 68px;
    padding: 0 20px;
    border-bottom: 3px solid transparent !important;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    border-radius: 12px 12px 0 0;
    margin: 0 4px;
    position: relative;
    overflow: hidden;
}

:deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title::before) {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.05) 0%, 
        rgba(139, 92, 246, 0.03) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
}

:deep(.el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title::before) {
    opacity: 1;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active),
:deep(.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title) {
    border-bottom: 3px solid var(--primary-color) !important;
    font-weight: 600;
    color: var(--primary-color) !important;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.1) 0%, 
        rgba(139, 92, 246, 0.05) 100%);
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.2);
}

:deep(.el-menu--horizontal > .el-menu-item:hover),
:deep(.el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title) {
    color: var(--primary-color) !important;
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(59, 130, 246, 0.15);
}

/* 子菜单样式优化 */
:deep(.el-menu--popup) {
    min-width: 180px;
    /* 增加宽度 */
    padding: 10px 0;
    /* 增加内边距 */
    border-radius: 10px;
    /* 增加圆角 */
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    /* 增强阴影 */
}

:deep(.el-menu--popup .el-menu-item) {
    height: 44px;
    /* 增加高度 */
    line-height: 44px;
    padding: 0 20px;
    /* 增加内边距 */
    display: flex;
    align-items: center;
    gap: 10px;
    /* 增加图标与文字的间距 */
    border-radius: 6px;
    /* 增加圆角 */
    margin: 0 6px;
    /* 增加外边距 */
    transition: all 0.2s ease;
    font-size: 15px;
    /* 增加字体大小 */
}

:deep(.el-menu--popup .el-menu-item:hover) {
    background-color: rgba(64, 158, 255, 0.1) !important;
    transform: translateX(4px);
    /* 添加悬浮效果 */
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
    gap: 20px;
    position: relative;
    z-index: 2;
}

.notification-icon,
.message-icon,
.search-user-icon,
.friends-icon {
    cursor: pointer;
    padding: 12px;
    border-radius: 50%;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
    border: 1px solid rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
}

.notification-icon::before,
.message-icon::before,
.search-user-icon::before,
.friends-icon::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.2) 0%, 
        rgba(139, 92, 246, 0.1) 100%);
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.notification-icon:hover::before,
.message-icon:hover::before,
.search-user-icon:hover::before,
.friends-icon:hover::before {
    opacity: 1;
}

.notification-icon:hover,
.message-icon:hover,
.search-user-icon:hover,
.friends-icon:hover {
    transform: translateY(-3px) scale(1.1);
    box-shadow: 0 10px 25px rgba(59, 130, 246, 0.3);
}

.bell-icon,
.icon-button {
    font-size: 22px;
    color: var(--text-secondary);
    position: relative;
    z-index: 1;
    transition: color 0.3s ease;
}

.notification-icon:hover .bell-icon,
.message-icon:hover .icon-button,
.search-user-icon:hover .icon-button,
.friends-icon:hover .icon-button {
    color: var(--primary-color);
}

.user-avatar-container {
    display: flex;
    align-items: center;
    margin-left: 8px;
}

.avatar-wrapper {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 8px 16px;
    border-radius: 25px;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
    border: 1px solid rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    position: relative;
    overflow: hidden;
}

.avatar-wrapper::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.1) 0%, 
        rgba(139, 92, 246, 0.05) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.avatar-wrapper:hover::before {
    opacity: 1;
}

.avatar-wrapper:hover {
    transform: translateY(-2px) scale(1.02);
    box-shadow: 0 8px 25px rgba(59, 130, 246, 0.2);
}

:deep(.el-avatar) {
    border: 2px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.2);
    transition: all 0.3s ease;
}

.avatar-wrapper:hover :deep(.el-avatar) {
    border-color: var(--primary-color);
    box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

.user-name {
    margin: 0 10px;
    font-size: 15px;
    color: var(--text-primary);
    font-weight: 600;
    position: relative;
    z-index: 1;
}

.dropdown-icon {
    color: var(--text-secondary);
    font-size: 12px;
    transition: all 0.3s ease;
    position: relative;
    z-index: 1;
}

.avatar-wrapper:hover .dropdown-icon {
    transform: rotate(180deg);
    color: var(--primary-color);
}

.login-btn {
    background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
    border: none;
    font-weight: 600;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    padding: 12px 28px;
    font-size: 15px;
    position: relative;
    overflow: hidden;
}

.login-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.6s ease;
}

.login-btn:hover::before {
    left: 100%;
}

.login-btn:hover {
    transform: translateY(-3px) scale(1.05);
    box-shadow: 0 10px 30px rgba(59, 130, 246, 0.4);
}

.register-btn {
    color: var(--primary-color);
    border: 2px solid var(--primary-color);
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
    backdrop-filter: blur(10px);
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    padding: 12px 28px;
    font-size: 15px;
    font-weight: 600;
    position: relative;
    overflow: hidden;
}

.register-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.register-btn:hover::before {
    opacity: 1;
}

.register-btn:hover {
    transform: translateY(-3px) scale(1.05);
    color: white;
    border-color: transparent;
    box-shadow: 0 10px 30px rgba(59, 130, 246, 0.4);
}

.register-btn span {
    position: relative;
    z-index: 1;
}

:deep(.el-dropdown-menu) {
    border-radius: 10px;
    /* 增加圆角 */
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    /* 增强阴影 */
    padding: 10px 0;
    /* 增加内边距 */
}

:deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 10px;
    /* 增加图标与文字的间距 */
    padding: 12px 20px;
    /* 增加内边距 */
    transition: all 0.2s;
    border-radius: 6px;
    /* 增加圆角 */
    margin: 0 6px;
    /* 增加外边距 */
    font-size: 15px;
    /* 增加字体大小 */
}

:deep(.el-dropdown-menu__item:hover) {
    background-color: rgba(64, 158, 255, 0.1);
    color: var(--primary-color);
    transform: translateX(4px);
    /* 添加悬浮效果 */
}

:deep(.el-dropdown-menu__item i) {
    font-size: 18px;
    /* 增加图标大小 */
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
    background: linear-gradient(135deg, rgba(30,30,32,0.98) 0%, rgba(26,26,30,0.92) 50%, rgba(22,22,26,0.88) 100%);
    border-bottom: 1.5px solid rgba(76,77,79,0.25);
    box-shadow: 0 2px 16px 0 rgba(0,0,0,0.25), 0 1.5px 0 rgba(255,255,255,0.04) inset;
    backdrop-filter: blur(12px);
}

[data-theme="dark"] .navbar::before {
    background: 
        radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.08) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.06) 0%, transparent 50%),
        radial-gradient(circle at 40% 80%, rgba(16, 185, 129, 0.04) 0%, transparent 50%);
}

[data-theme="dark"] .navbar.auth-page {
    background-color: transparent;
    box-shadow: none;
}

[data-theme="dark"] .navbar .logo h1 {
    background: linear-gradient(135deg, #8b5cf6, #2c65dd, #10b981);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    color: transparent;
    filter: drop-shadow(0 2px 8px #8b5cf6cc);
}

[data-theme="dark"] .navbar .logo img {
    filter: drop-shadow(0 2px 8px #8b5cf6cc) brightness(0.9);
}

[data-theme="dark"] .navbar .menu {
    background: transparent;
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-menu-item),
[data-theme="dark"] :deep(.el-menu--horizontal > .el-sub-menu__title) {
    color: #e5eaf3 !important;
    text-shadow: 0 1px 8px #23294655;
    transition: color 0.2s;
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-menu-item.is-active),
[data-theme="dark"] :deep(.el-menu--horizontal > .el-menu-item:hover),
[data-theme="dark"] :deep(.el-menu--horizontal > .el-sub-menu__title:hover) {
    color: #8b5cf6 !important;
    text-shadow: 0 2px 12px #8b5cf6cc;
}

[data-theme="dark"] .navbar .user-actions {
    background: linear-gradient(135deg, rgba(45,55,72,0.7), rgba(36,41,61,0.7));
    border-radius: 18px;
    box-shadow: 0 2px 12px rgba(139,92,246,0.08);
}

[data-theme="dark"] .navbar .user-avatar {
    box-shadow: 0 2px 8px #23294655;
    border: 2px solid #8b5cf6;
}

[data-theme="dark"] .navbar .user-name {
    color: #e5eaf3;
}

[data-theme="dark"] .navbar .login-btn,
[data-theme="dark"] .navbar .register-btn {
    background: linear-gradient(90deg, #8b5cf6 0%, #2c65dd 100%);
    color: #fff;
    box-shadow: 0 2px 8px #8b5cf655;
    border: none;
}

[data-theme="dark"] .navbar .login-btn:hover,
[data-theme="dark"] .navbar .register-btn:hover {
    background: linear-gradient(90deg, #2c65dd 0%, #10b981 100%);
    color: #fff;
    box-shadow: 0 4px 16px #10b98155;
}

/* 暗色主题子菜单样式 */
[data-theme="dark"] :deep(.el-menu--popup) {
    background-color: var(--dark-bg-secondary, #2a2a2e) !important;
    border: 1px solid var(--dark-border-color, #4c4d4f) !important;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.4) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item) {
    background-color: transparent !important;
    color: var(--dark-text-regular, #cfd3dc) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item .el-icon) {
    color: var(--dark-text-regular, #cfd3dc) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item:hover) {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1)) !important;
    color: var(--primary-color) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item:hover .el-icon) {
    color: var(--primary-color) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item.is-active) {
    background-color: rgba(59, 130, 246, 0.2) !important;
    color: var(--primary-color) !important;
}

/* 暗色主题用户操作区域 */
[data-theme="dark"] .notification-icon,
[data-theme="dark"] .message-icon,
[data-theme="dark"] .search-user-icon,
[data-theme="dark"] .friends-icon {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.03));
    border-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .notification-icon::before,
[data-theme="dark"] .message-icon::before,
[data-theme="dark"] .search-user-icon::before,
[data-theme="dark"] .friends-icon::before {
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.3) 0%, 
        rgba(139, 92, 246, 0.15) 100%);
}

[data-theme="dark"] .bell-icon,
[data-theme="dark"] .icon-button {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .notification-icon:hover .bell-icon,
[data-theme="dark"] .message-icon:hover .icon-button,
[data-theme="dark"] .search-user-icon:hover .icon-button,
[data-theme="dark"] .friends-icon:hover .icon-button {
    color: var(--primary-color);
}

/* 暗色主题用户头像区域 */
[data-theme="dark"] .avatar-wrapper {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.03));
    border-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .avatar-wrapper::before {
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.15) 0%, 
        rgba(139, 92, 246, 0.08) 100%);
}

[data-theme="dark"] .user-name {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .dropdown-icon {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .avatar-wrapper:hover .dropdown-icon {
    color: var(--primary-color);
}

/* 暗色主题下拉菜单 */
[data-theme="dark"] :deep(.el-dropdown-menu) {
    background-color: var(--dark-bg-secondary, #2a2a2e) !important;
    border: 1px solid var(--dark-border-color, #4c4d4f) !important;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.4) !important;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item) {
    color: var(--dark-text-regular, #cfd3dc) !important;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item .el-icon) {
    color: var(--dark-text-regular, #cfd3dc) !important;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item:hover) {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1)) !important;
    color: var(--primary-color) !important;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item:hover .el-icon) {
    color: var(--primary-color) !important;
}

/* 暗色主题登录注册按钮 */
[data-theme="dark"] .login-btn {
    background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
}

[data-theme="dark"] .login-btn:hover {
    box-shadow: 0 10px 30px rgba(59, 130, 246, 0.5);
}

[data-theme="dark"] .register-btn {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.03));
    border-color: var(--primary-color);
    color: var(--primary-color);
}

[data-theme="dark"] .register-btn::before {
    background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
}

[data-theme="dark"] .register-btn:hover {
    box-shadow: 0 10px 30px rgba(59, 130, 246, 0.5);
}

/* 暗色主题通知下拉菜单 */
[data-theme="dark"] :deep(.notification-dropdown) {
    background-color: var(--dark-bg-secondary, #2a2a2e) !important;
    border: 1px solid var(--dark-border-color, #4c4d4f) !important;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4) !important;
}

[data-theme="dark"] .notification-dropdown-header {
    background-color: var(--dark-bg-secondary, #2a2a2e);
    border-bottom-color: var(--dark-border-color, #4c4d4f);
}

[data-theme="dark"] .notification-dropdown-header h3 {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .notification-item {
    border-bottom-color: var(--dark-border-color, #4c4d4f);
}

[data-theme="dark"] .notification-item:hover {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.1)) !important;
}

[data-theme="dark"] .notification-item.notification-unread {
    background-color: rgba(59, 130, 246, 0.15);
    border-left-color: var(--primary-color);
}

[data-theme="dark"] .notification-item.notification-read {
    background-color: var(--dark-bg-secondary);
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

[data-theme="dark"] .notification-footer {
    background-color: var(--dark-bg-secondary, #2a2a2e);
    border-top-color: var(--dark-border-color, #4c4d4f);
}

[data-theme="dark"] .notification-footer a:hover {
    background-color: rgba(59, 130, 246, 0.15);
}

/* 动画关键帧 */
@keyframes particlesFloat {
    0%, 100% {
        transform: translateY(0px) rotate(0deg);
        opacity: 0.8;
    }
    33% {
        transform: translateY(-20px) rotate(5deg);
        opacity: 1;
    }
    66% {
        transform: translateY(-10px) rotate(-3deg);
        opacity: 0.9;
    }
}

@keyframes logoFloat {
    0%, 100% {
        transform: translateY(0px) rotate(0deg);
    }
    50% {
        transform: translateY(-3px) rotate(2deg);
    }
}

@keyframes pulse {
    0%, 100% {
        opacity: 1;
    }
    50% {
        opacity: 0.5;
    }
}
</style>
