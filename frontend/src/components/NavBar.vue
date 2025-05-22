<template>
    <nav class="navbar" :class="{ 'auth-page': isAuthPage }">
        <div class="container">
            <router-link to="/" class="logo">
                <img src="../assets/vue.svg" alt="学伴" />
                <h1>学伴</h1>
            </router-link>
            
            <div class="menu" v-if="!isAuthPage">
                <el-menu 
                    mode="horizontal" 
                    router 
                    :default-active="activeIndex"
                    background-color="transparent"
                    text-color="var(--text-primary)"
                    active-text-color="var(--primary-color)">
                    
                    <!-- 首页 -->
                    <el-menu-item index="/">
                        <el-icon><House /></el-icon>
                        首页
                    </el-menu-item>
                    
                    <!-- 互助信息下拉菜单 -->
                    <el-sub-menu index="/helpinfo">
                        <template #title>
                            <el-icon><Service /></el-icon>
                            互助信息
                        </template>
                        <el-menu-item index="/helpinfo">浏览互助</el-menu-item>
                        <el-menu-item index="/helpinfo/publish" v-if="authStore.isAuthenticated">
                            发布互助
                        </el-menu-item>
                    </el-sub-menu>
                    
                    <!-- 我的中心 -->
                    <el-sub-menu index="my" v-if="authStore.isAuthenticated">
                        <template #title>
                            <el-icon><User /></el-icon>
                            个人中心
                        </template>
                        <el-menu-item index="/my/helpinfo">我的互助</el-menu-item>
                        <el-menu-item index="/applications">我的申请</el-menu-item>
                        <el-menu-item index="/profile">个人资料</el-menu-item>
                    </el-sub-menu>
                    
                    <!-- 管理后台 -->
                    <el-menu-item index="/admin/helpinfo" v-if="isAdmin">
                        <el-icon><Setting /></el-icon>
                        管理后台
                    </el-menu-item>
                </el-menu>
            </div>
            
            <div class="user-actions">
                <template v-if="authStore.isAuthenticated">
                    <div class="user-avatar-container">
                        <el-dropdown trigger="click">
                            <div class="avatar-wrapper">
                                <el-avatar :size="36" :src="getUserAvatar()"></el-avatar>
                                <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username || '用户' }}</span>
                                <el-icon class="el-icon--right"><arrow-down /></el-icon>
                            </div>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="$router.push('/profile')">
                                        <el-icon><User /></el-icon>个人中心
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="$router.push('/my/helpinfo')">
                                        <el-icon><List /></el-icon>我的互助
                                    </el-dropdown-item>
                                    <el-dropdown-item @click="$router.push('/applications')">
                                        <el-icon><Document /></el-icon>我的申请
                                    </el-dropdown-item>
                                    <el-dropdown-item divided @click="handleLogout">
                                        <el-icon><SwitchButton /></el-icon>退出登录
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { 
    ArrowDown, 
    User, 
    House, 
    Service, 
    Setting,
    Document,
    List,
    SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeIndex = computed(() => route.path)

// 判断当前用户是否是管理员
const isAdmin = computed(() => {
    return authStore.user &&
        authStore.user.roles &&
        authStore.user.roles.includes('ROLE_ADMIN')
})

// 判断是否为认证页面（登录/注册）
const isAuthPage = computed(() => {
    return route.path === '/login' || route.path === '/register'
})

// 获取用户头像
const getUserAvatar = () => {
    return authStore.user?.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
}

const handleLogout = () => {
    authStore.logout()
    router.push('/login')
}
</script>

<style scoped>
.navbar {
    background-color: var(--card-bg);
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
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
}

:deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title) {
    height: 60px;
    line-height: 60px;
    padding: 0 16px;
    border-bottom: 2px solid transparent !important;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active),
:deep(.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title) {
    border-bottom: 2px solid var(--primary-color) !important;
    font-weight: 500;
}

.user-actions {
    display: flex;
    align-items: center;
    gap: 16px;
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
    transition: background-color 0.3s;
}

.avatar-wrapper:hover {
    background-color: rgba(0, 0, 0, 0.05);
}

.user-name {
    margin: 0 8px;
    font-size: 14px;
    color: var(--text-primary);
}

.login-btn {
    background-color: var(--primary-color);
    border-color: var(--primary-color);
    font-weight: 500;
}

.register-btn {
    color: var(--primary-color);
    border-color: var(--primary-color);
    background: transparent;
}

:deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
}

.el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
    color: #409EFF;
}
</style>
