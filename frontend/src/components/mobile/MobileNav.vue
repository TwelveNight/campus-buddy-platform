<template>
  <div class="mobile-nav-wrapper">
    <!-- 移动端导航栏 -->
    <div class="mobile-nav-bar" v-if="isMobile">
      <div class="mobile-nav-header">
        <div class="nav-brand">
          <router-link to="/" class="brand-link">
            <i class="el-icon-location-outline"></i>
            <span>校园互助</span>
          </router-link>
        </div>
        <div class="nav-actions">
          <el-button 
            circle 
            size="small"
            @click="toggleTheme"
            class="theme-toggle-btn"
          >
            <el-icon>
              <component :is="isDarkMode ? 'sunny' : 'moon'" />
            </el-icon>
          </el-button>
          <el-button 
            circle 
            size="small"
            @click="toggleMenu"
            class="menu-toggle-btn"
          >
            <el-icon>
              <component :is="isMenuOpen ? 'close' : 'operation'" />
            </el-icon>
          </el-button>
        </div>
      </div>
      
      <!-- 移动端菜单抽屉 -->
      <el-drawer
        v-model="isMenuOpen"
        direction="rtl"
        :size="280"
        :show-close="false"
        class="mobile-nav-drawer"
      >
        <template #header>
          <div class="drawer-header">
            <div class="user-profile" v-if="authStore.isAuthenticated">
              <el-avatar 
                :src="authStore.user?.avatarUrl" 
                :size="50"
                class="user-avatar"
              >
                <i class="el-icon-user-solid"></i>
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ authStore.user?.nickname || authStore.user?.username || '用户' }}</div>
                <div class="user-role">{{ getRoleText() }}</div>
              </div>
            </div>
            <div v-else class="login-prompt">
              <el-button type="primary" @click="goToLogin">立即登录</el-button>
            </div>
          </div>
        </template>
        
        <div class="mobile-menu-content">
          <el-menu
            :default-active="activeMenu"
            :router="true"
            mode="vertical"
            class="mobile-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <el-menu-item index="/helpinfo">
              <el-icon><InfoFilled /></el-icon>
              <span>校园互助</span>
            </el-menu-item>
            
            <template v-if="authStore.isAuthenticated">
              <el-menu-item index="/my/helpinfo">
                <el-icon><Document /></el-icon>
                <span>我的任务</span>
              </el-menu-item>
              
              <el-menu-item index="/applications">
                <el-icon><List /></el-icon>
                <span>我的申请</span>
              </el-menu-item>
              
              <el-menu-item index="/reviews">
                <el-icon><Star /></el-icon>
                <span>我的评价</span>
              </el-menu-item>
              
              <el-menu-item index="/groups">
                <el-icon><UserFilled /></el-icon>
                <span>我的学习小组</span>
              </el-menu-item>
              
              <el-menu-item index="/profile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-menu-item>
              
              <template v-if="authStore.isAdmin">
                <el-divider />
                <el-menu-item index="/admin/dashboard">
                  <el-icon><Histogram /></el-icon>
                  <span>数据统计</span>
                </el-menu-item>
                <el-menu-item index="/admin/users">
                  <el-icon><User /></el-icon>
                  <span>用户管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/helpinfo">
                  <el-icon><Document /></el-icon>
                  <span>互助任务管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/groups">
                  <el-icon><UserFilled /></el-icon>
                  <span>小组管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/posts">
                  <el-icon><List /></el-icon>
                  <span>帖子管理</span>
                </el-menu-item>
              </template>
            </template>
          </el-menu>
          
          <div class="mobile-menu-footer" v-if="authStore.isAuthenticated">
            <el-button 
              type="danger" 
              plain 
              @click="handleLogout"
              class="logout-btn"
            >
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </div>
      </el-drawer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { 
  House, 
  InfoFilled, 
  Document, 
  List, 
  Star, 
  User, 
  Setting, 
  SwitchButton,
  UserFilled,
  Histogram
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isMenuOpen = ref(false)
const isMobile = ref(false)
const isDarkMode = ref(false)

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 检测是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 检查主题模式
const checkTheme = () => {
  isDarkMode.value = document.documentElement.classList.contains('dark-theme')
}

// 切换主题
const toggleTheme = () => {
  const html = document.documentElement
  const isDark = html.classList.contains('dark-theme')
  
  if (isDark) {
    html.classList.remove('dark-theme')
    localStorage.setItem('theme', 'light')
  } else {
    html.classList.add('dark-theme')
    localStorage.setItem('theme', 'dark')
  }
  
  isDarkMode.value = !isDark
}

// 切换菜单
const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

// 菜单选择处理
const handleMenuSelect = (index: string) => {
  isMenuOpen.value = false
  router.push(index)
}

// 获取角色文本
const getRoleText = () => {
  if (authStore.isAdmin) {
    return '管理员'
  }
  // 如果有其他角色判断逻辑，可以在这里添加
  return '用户'
}

// 跳转到登录页
const goToLogin = () => {
  isMenuOpen.value = false
  router.push('/login')
}

// 处理登出
const handleLogout = async () => {
  try {
    authStore.logout()
    isMenuOpen.value = false
    ElMessage.success('退出成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('退出失败')
  }
}

// 监听窗口大小变化
const handleResize = () => {
  checkMobile()
}

onMounted(() => {
  checkMobile()
  checkTheme()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.mobile-nav-wrapper {
  position: relative;
}

.mobile-nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-light);
}

.mobile-nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  height: 60px;
}

.nav-brand {
  flex: 1;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.brand-link i {
  font-size: 24px;
  color: var(--primary-color);
}

.nav-actions {
  display: flex;
  gap: 8px;
}

.theme-toggle-btn,
.menu-toggle-btn {
  width: 40px;
  height: 40px;
  border: 1px solid var(--border-color);
  background-color: var(--card-bg);
  color: var(--text-regular);
}

.theme-toggle-btn:hover,
.menu-toggle-btn:hover {
  background-color: var(--hover-bg);
}

.mobile-nav-drawer :deep(.el-drawer) {
  background-color: var(--card-bg);
}

.mobile-nav-drawer :deep(.el-drawer__header) {
  padding: 0;
  margin-bottom: 0;
}

.drawer-header {
  padding: 24px 20px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-role {
  font-size: 14px;
  opacity: 0.9;
}

.login-prompt {
  text-align: center;
}

.login-prompt .el-button {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.login-prompt .el-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.mobile-menu-content {
  padding: 0;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.mobile-menu {
  flex: 1;
  border: none;
  background-color: transparent;
}

.mobile-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  padding: 0 20px;
  margin: 4px 12px;
  border-radius: 8px;
  color: var(--text-regular);
  transition: all 0.3s ease;
}

.mobile-menu .el-menu-item:hover {
  background-color: var(--hover-bg);
}

.mobile-menu .el-menu-item.is-active {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

.mobile-menu .el-menu-item .el-icon {
  width: 20px;
  margin-right: 12px;
}

.mobile-menu-footer {
  padding: 20px;
  border-top: 1px solid var(--border-lighter);
}

.logout-btn {
  width: 100%;
  height: 44px;
}

.el-divider {
  margin: 8px 0;
  background-color: var(--border-lighter);
}

/* 暗色主题适配 */
.dark-theme .mobile-nav-bar {
  background-color: var(--card-bg);
  border-bottom-color: var(--border-color);
}

.dark-theme .mobile-menu .el-menu-item.is-active {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

/* 响应式调整 */
@media (max-width: 480px) {
  .mobile-nav-header {
    padding: 8px 12px;
    height: 56px;
  }
  
  .brand-link {
    font-size: 16px;
  }
  
  .brand-link i {
    font-size: 20px;
  }
  
  .theme-toggle-btn,
  .menu-toggle-btn {
    width: 36px;
    height: 36px;
  }
  
  .drawer-header {
    padding: 20px 16px;
  }
  
  .mobile-menu .el-menu-item {
    height: 48px;
    line-height: 48px;
    margin: 2px 8px;
  }
}

/* 安卓和iOS特定样式调整 */
@supports (-webkit-touch-callout: none) {
  /* iOS Safari */
  .mobile-nav-bar {
    padding-top: env(safe-area-inset-top);
  }
}

@media screen and (-webkit-min-device-pixel-ratio: 2) {
  /* 高分辨率屏幕 */
  .brand-link i {
    transform: scale(0.9);
  }
}
</style>
