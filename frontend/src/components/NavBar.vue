<template>
    <nav class="navbar" :class="{ 'auth-page': isAuthPage }">
        <div class="container">
            <router-link to="/" class="logo">
                <img src="../assets/vue.svg" alt="学伴" />
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

                    <!-- 互助信息下拉菜单 -->
                    <el-sub-menu index="/helpinfo">
                        <template #title>
                            <el-icon class="menu-icon">
                                <Service />
                            </el-icon>
                            校园互助
                        </template>
                        <el-menu-item index="/helpinfo">
                            <el-icon><Collection /></el-icon>
                            浏览互助
                        </el-menu-item>
                        <el-menu-item index="/helpinfo/publish" v-if="authStore.isAuthenticated">
                            <el-icon><Plus /></el-icon>
                            发布互助
                        </el-menu-item>
                        <el-menu-item index="/my/helpinfo">
                            <el-icon><Document /></el-icon>
                            我的互助
                        </el-menu-item>
                        <el-menu-item index="/applications">
                            <el-icon><List /></el-icon>
                            我的申请
                        </el-menu-item>
                        <el-menu-item index="/reviews">
                            <el-icon><Star /></el-icon>
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
                            <el-icon><Collection /></el-icon>
                            浏览小组
                        </el-menu-item>
                        <el-menu-item v-if="authStore.isAuthenticated" @click="navigateToGroups('joined')">
                            <el-icon><Star /></el-icon>
                            我加入的小组
                        </el-menu-item>
                        <el-menu-item v-if="authStore.isAuthenticated" @click="navigateToGroups('created')">
                            <el-icon><Edit /></el-icon>
                            我创建的小组
                        </el-menu-item>
                        <el-menu-item v-if="authStore.isAuthenticated" @click="showCreateGroupDialog">
                            <el-icon><Plus /></el-icon>
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
                    <div class="notification-icon">
                        <el-badge :value="0" :max="99" :hidden="true" type="danger">
                            <el-icon class="bell-icon"><Bell /></el-icon>
                        </el-badge>
                    </div>
                    <div class="user-avatar-container">
                        <el-dropdown trigger="hover">
                            <div class="avatar-wrapper">
                                <el-avatar :size="36" :src="avatarUrl"></el-avatar>
                                <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username || '用户' }}</span>
                                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
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
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import ThemeSwitch from './ThemeSwitch.vue'
import { ElMessageBox } from 'element-plus'
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
    ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeIndex = computed(() => route.path)

// 使用authStore中的isAdmin getter
const isAdmin = computed(() => {
    // 添加调试日志
    console.log('NavBar - 当前用户信息:', authStore.user);
    console.log('NavBar - 用户角色:', authStore.user?.roles);
    console.log('NavBar - isAdmin值:', authStore.isAdmin);
    return authStore.isAdmin;
})

// 组件挂载时检查管理员状态
onMounted(async () => {
    if (authStore.isAuthenticated) {
        try {
            const isAdmin = await authStore.checkAdminStatus();
            console.log('NavBar组件中检查管理员状态:', isAdmin);
        } catch (error) {
            console.error('NavBar组件中检查管理员状态失败:', error);
        }
    }
})

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
    console.log('导航栏获取头像URL:', `${url}?v=${timestamp}`);
    return `${url}?v=${timestamp}`;
});

// 退出登录
const handleLogout = () => {
    authStore.logout()
    router.push('/login')
}

// 导航到小组页面的不同标签
const navigateToGroups = (tab: string) => {
    console.log(`导航到小组页面，标签: ${tab}`);
    
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
    console.log('尝试打开创建小组对话框');
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
        console.log('已导航到小组页面，带有创建参数');
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
    
    .login-btn, .register-btn {
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
    background-color: #1a1a1a !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .navbar.auth-page {
    background-color: transparent;
    box-shadow: none;
}

[data-theme="dark"] .logo h1 {
    color: var(--primary-color);
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-menu-item) {
    color: #ffffff !important;
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title) {
    color: #ffffff !important;
}

[data-theme="dark"] :deep(.el-menu--horizontal > .el-menu-item:hover),
[data-theme="dark"] :deep(.el-menu--horizontal > .el-sub-menu:hover .el-sub-menu__title) {
    color: var(--primary-color) !important;
    background-color: rgba(255, 255, 255, 0.1) !important;
}

[data-theme="dark"] :deep(.el-menu--popup) {
    background-color: #1a1a1a !important;
    border: 1px solid #333333 !important;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.5) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item) {
    background-color: #1a1a1a !important;
    color: #ffffff !important;
    border-bottom: none !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item:hover) {
    background-color: #333333 !important;
    color: #ffffff !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item.is-active) {
    background-color: #333333 !important;
    color: var(--primary-color) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item:hover) {
    background-color: rgba(255, 255, 255, 0.1) !important;
    color: var(--primary-color) !important;
}

[data-theme="dark"] :deep(.el-menu--popup .el-menu-item.is-active) {
    background-color: rgba(64, 158, 255, 0.2) !important;
    color: var(--primary-color) !important;
}

[data-theme="dark"] .notification-icon:hover {
    background-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .bell-icon {
    color: #ffffff;
}

[data-theme="dark"] .avatar-wrapper:hover {
    background-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .user-name {
    color: #ffffff;
}

[data-theme="dark"] .dropdown-icon {
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-dropdown-menu) {
    background-color: #1a1a1a !important;
    border: 1px solid #333333 !important;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.5) !important;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item) {
    color: #ffffff !important;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item:hover) {
    background-color: rgba(255, 255, 255, 0.1) !important;
    color: var(--primary-color) !important;
}
</style>
