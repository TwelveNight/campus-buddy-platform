<template>
    <nav class="navbar">
        <div class="container">
            <div class="logo">
                <h1>学伴</h1>
            </div>
            <div class="menu">
                <el-menu mode="horizontal" router :default-active="activeIndex">
                    <el-menu-item index="/">首页</el-menu-item>
                    <el-menu-item index="/helpinfo">互助信息</el-menu-item>
                    <el-menu-item index="/helpinfo/publish" v-if="authStore.isAuthenticated">
                        发布互助
                    </el-menu-item>
                    <el-menu-item index="/my/helpinfo" v-if="authStore.isAuthenticated">
                        我的互助
                    </el-menu-item>
                    <el-menu-item index="/applications" v-if="authStore.isAuthenticated">
                        我的申请
                    </el-menu-item>
                    <el-menu-item index="/admin/helpinfo" v-if="isAdmin">
                        管理后台
                    </el-menu-item>
                </el-menu>
            </div>
            <div class="user-actions">
                <template v-if="authStore.isAuthenticated">
                    <el-dropdown>
                        <span class="el-dropdown-link">
                            {{ authStore.user?.nickname || authStore.user?.username || '用户' }}
                            <el-icon class="el-icon--right"><arrow-down /></el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item @click="$router.push('/my/helpinfo')">我的互助</el-dropdown-item>
                                <el-dropdown-item @click="$router.push('/applications')">我的申请</el-dropdown-item>
                                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </template>
                <template v-else>
                    <el-button type="primary" @click="$router.push('/login')">登录</el-button>
                    <el-button @click="$router.push('/register')">注册</el-button>
                </template>
            </div>
        </div>
    </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ArrowDown } from '@element-plus/icons-vue'

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

const handleLogout = () => {
    authStore.logout()
    router.push('/login')
}
</script>

<style scoped>
.navbar {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
}

.logo h1 {
    font-size: 24px;
    color: #409EFF;
    margin: 0;
}

.menu {
    flex-grow: 1;
    margin-left: 40px;
}

.user-actions {
    display: flex;
    align-items: center;
    gap: 10px;
}

.el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
    color: #409EFF;
}
</style>
