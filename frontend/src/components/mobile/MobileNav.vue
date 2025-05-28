<template>
  <div class="mobile-nav-wrapper" v-if="isMobile">
    <!-- 移动端导航栏 -->
    <div class="mobile-nav-bar">
      <div class="mobile-nav-header">
        <div class="nav-brand">
          <router-link to="/" class="brand-link">
            <img src="../../assets/vue.svg" alt="学伴" class="brand-logo" />
            <span>学伴</span>
          </router-link>
        </div>
        <div class="nav-actions">
          <!-- 未读通知数量 -->
          <div class="notification-icon" v-if="authStore.isAuthenticated" @click="openNotifications">
            <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="nav-badge">
              <el-button circle size="default" class="action-btn">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </div>
          
          <!-- 未读消息数量 -->
          <div class="message-icon" v-if="authStore.isAuthenticated" @click="goToMessages">
            <el-badge :value="unreadMessageCount" :max="99" :hidden="unreadMessageCount === 0" class="nav-badge">
              <el-button circle size="default" class="action-btn">
                <el-icon><ChatDotRound /></el-icon>
              </el-button>
            </el-badge>
          </div>
          
          <el-button 
            circle 
            size="default"
            @click="toggleTheme"
            class="theme-toggle-btn action-btn"
          >
            <el-icon v-if="isDarkMode"><Sunny /></el-icon>
            <el-icon v-else><Moon /></el-icon>
          </el-button>
          
          <!-- 使用用户头像打开菜单 -->
          <div class="user-avatar-container" v-if="authStore.isAuthenticated" @click="toggleMenu">
            <el-avatar 
              :src="authStore.user?.avatarUrl" 
              :size="40"
              class="mobile-user-avatar"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          
          <!-- 未登录时显示菜单按钮 -->
          <el-button 
            v-else
            circle 
            size="default"
            @click="toggleMenu"
            class="action-btn"
          >
            <el-icon><User /></el-icon>
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
import { getUnreadNotificationCount } from '@/api/notification'
import { getUnreadMessageCount } from '@/api/message'
import { 
  House, 
  InfoFilled, 
  Document, 
  List, 
  Star, 
  User, 
  SwitchButton,
  UserFilled,
  Histogram,
  Bell,
  ChatDotRound,
  Moon,
  Sunny
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isMenuOpen = ref(false)
const isMobile = ref(false)
const isDarkMode = ref(false)
const unreadCount = ref(0)
const unreadMessageCount = ref(0)

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
  isDarkMode.value = document.documentElement.getAttribute('data-theme') === 'dark'
}

// 切换主题
const toggleTheme = (event) => {
  const html = document.documentElement
  const isDark = html.getAttribute('data-theme') === 'dark'
  if (isDark) {
    html.setAttribute('data-theme', 'light')
    localStorage.setItem('theme', 'light')
  } else {
    html.setAttribute('data-theme', 'dark')
    localStorage.setItem('theme', 'dark')
  }
  isDarkMode.value = !isDark
  
  // 触发带有点击位置的主题切换事件
  window.dispatchEvent(new CustomEvent('themeToggle', {
    detail: {
      x: event ? event.clientX : window.innerWidth / 2,
      y: event ? event.clientY : window.innerHeight / 2
    }
  }))
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

// 打开通知中心
const openNotifications = () => {
  router.push('/notifications')
}

// 跳转到消息页面
const goToMessages = () => {
  router.push('/messages')
}

// 获取未读通知数量
const fetchUnreadCount = async () => {
  try {
    if (authStore.isAuthenticated) {
      const response = await getUnreadNotificationCount()
      unreadCount.value = response?.count || 0
    }
  } catch (error) {
    console.error('获取未读通知数量失败', error)
  }
}

// 获取未读消息数量
const fetchUnreadMessageCount = async () => {
  try {
    if (authStore.isAuthenticated) {
      const response = await getUnreadMessageCount()
      unreadMessageCount.value = response?.data?.count || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败', error)
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
  
  // 获取未读通知和消息数量
  if (authStore.isAuthenticated) {
    fetchUnreadCount()
    fetchUnreadMessageCount()
    
    // 设置定期更新
    const pollingInterval = setInterval(() => {
      fetchUnreadCount()
      fetchUnreadMessageCount()
    }, 30000) // 每30秒更新一次
    
    // 组件销毁时清除
    onUnmounted(() => {
      clearInterval(pollingInterval)
    })
  }
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
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%, 
    rgba(248, 250, 252, 0.9) 50%,
    rgba(241, 245, 249, 0.85) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  width: 100%;
  box-sizing: border-box;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 移除粒子效果 */
.mobile-nav-bar::after {
  display: none; /* 禁用粒子效果 */
}

.mobile-nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  height: 64px;
}

.nav-brand {
  flex: 1;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #409eff, #67c23a, #e6a23c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  transition: all 0.3s ease;
}

.brand-link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  transition: width 0.3s ease;
}

.brand-link:hover::after {
  width: 100%;
}

.brand-link:hover {
  text-shadow: 
    0 0 2px rgba(64, 158, 255, 0.2), /* 减轻阴影效果 */
    0 0 4px rgba(64, 158, 255, 0.1); /* 减轻阴影效果 */
}

.brand-link:hover .brand-logo {
  animation: none; /* 移除动画 */
  filter: drop-shadow(0 2px 6px rgba(64, 158, 255, 0.3)); /* 轻微的阴影效果 */
}

.brand-logo {
  height: 32px;
  width: auto;
  filter: drop-shadow(0 1px 4px rgba(64, 158, 255, 0.2)); /* 减轻阴影效果 */
}

@keyframes gentle-float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-1px) rotate(0.5deg); } /* 减小动画幅度 */
  50% { transform: translateY(0) rotate(0deg); }
  75% { transform: translateY(-0.5px) rotate(-0.5deg); } /* 减小动画幅度 */
}

/* 磁性按钮效果 */
.nav-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  position: relative;
}

.nav-actions::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.05) 0%, transparent 70%);
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.3s ease;
  pointer-events: none;
  border-radius: 50%;
}

.nav-actions:hover::before {
  transform: translate(-50%, -50%) scale(1);
}

.action-btn,
.theme-toggle-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.9), 
    rgba(248, 250, 252, 0.8));
  color: var(--text-regular);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.1),
    0 2px 4px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
}

.action-btn::before,
.theme-toggle-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.3), transparent);
  transition: all 0.3s ease;
  transform: translate(-50%, -50%);
  border-radius: 50%;
}

.action-btn:hover,
.theme-toggle-btn:hover {
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 0.05), /* 减轻背景效果 */
    rgba(255, 255, 255, 0.9));
  transform: translateY(-1px); /* 减小上移效果 */
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.12), /* 减轻阴影效果 */
    0 2px 4px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  color: var(--primary-color);
}

.action-btn:hover::before,
.theme-toggle-btn:hover::before {
  width: 100%;
  height: 100%;
}

.action-btn:active,
.theme-toggle-btn:active {
  transform: translateY(0) scale(0.98);
}

.action-btn:active::before,
.theme-toggle-btn:active::before {
  display: none; /* 禁用涟漪动画效果 */
}

.notification-icon,
.message-icon {
  position: relative;
}

.user-avatar-container {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
}

.user-avatar-container::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: linear-gradient(135deg, #409eff, #67c23a, #e6a23c);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.user-avatar-container:hover::before {
  opacity: 0.3; /* 降低不透明度 */
}

.user-avatar-container:hover {
  transform: translateY(-1px); /* 减小上移效果 */
}

.user-avatar-container:active {
  transform: translateY(0) scale(0.95);
}

.mobile-user-avatar {
  border: 3px solid rgba(255, 255, 255, 0.8);
  box-shadow: 
    0 6px 20px rgba(0, 0, 0, 0.15),
    0 2px 6px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.nav-badge :deep(.el-badge__content) {
  font-size: 10px;
  padding: 0 5px;
  height: 16px;
  line-height: 16px;
  border-radius: 8px;
  min-width: 16px;
  animation: badge-pulse 2s ease-in-out infinite;
}

@keyframes badge-pulse {
  0%, 100% { 
    transform: scale(1);
    opacity: 1;
  }
  50% { 
    transform: scale(1.05); /* 减小缩放效果 */
    opacity: 0.9; /* 减小不透明度变化 */
  }
}

.mobile-nav-drawer :deep(.el-drawer) {
  background: linear-gradient(145deg, 
    rgba(255, 255, 255, 0.95) 0%,
    rgba(248, 250, 252, 0.9) 50%,
    rgba(241, 245, 249, 0.85) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  max-width: 85%;
  box-shadow: 
    -8px 0 32px rgba(0, 0, 0, 0.12),
    -2px 0 8px rgba(0, 0, 0, 0.08);
  transform-style: preserve-3d;
  perspective: 1000px;
}

.mobile-nav-drawer :deep(.el-drawer)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, 
    rgba(64, 158, 255, 0.05) 0%,
    transparent 30%,
    transparent 70%,
    rgba(103, 194, 58, 0.05) 100%);
  pointer-events: none;
}

.mobile-nav-drawer :deep(.el-drawer__header) {
  padding: 0;
  margin-bottom: 0;
}

.drawer-header {
  padding: 28px 24px;
  background: linear-gradient(135deg, 
    #667eea 0%, 
    #764ba2 25%,
    #409eff 50%, 
    #67c23a 75%,
    #e6a23c 100%);
  color: white;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.drawer-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.1), 
    transparent);
  animation: header-shine 3s ease-in-out infinite;
}

@keyframes header-shine {
  0% { left: -100%; }
  100% { left: 100%; }
}

.drawer-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.5), 
    transparent);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.user-avatar {
  border: 3px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.2),
    0 4px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 6px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.user-role {
  font-size: 14px;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
  backdrop-filter: blur(10px);
}

.login-prompt {
  text-align: center;
}

.login-prompt .el-button {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  position: relative;
  overflow: hidden;
}

.login-prompt .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.2), 
    transparent);
  transition: left 0.5s ease;
}

.login-prompt .el-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.login-prompt .el-button:hover::before {
  left: 100%;
}

.mobile-menu-content {
  padding: 0;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
  transform-style: preserve-3d;
}

.mobile-menu {
  flex: 1;
  border: none;
  background-color: transparent;
  width: 100%;
  box-sizing: border-box;
}

.mobile-menu .el-menu-item {
  height: 60px;
  line-height: 60px;
  padding: 0 24px;
  margin: 6px 16px;
  border-radius: 16px;
  color: var(--text-regular);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-sizing: border-box;
  width: calc(100% - 32px);
  font-size: 16px;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transform-style: preserve-3d;
}

.mobile-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: translateX(4px); /* 简化变换效果 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.mobile-menu .el-menu-item:hover::before {
  width: 3px;
}

.mobile-menu .el-menu-item.is-active {
  background: rgba(64, 158, 255, 0.15);
  color: var(--primary-color);
  font-weight: 600;
  transform: translateX(6px); /* 简化变换效果 */
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
}

.mobile-menu .el-menu-item .el-icon {
  width: 24px;
  margin-right: 16px;
  font-size: 20px;
  transition: all 0.3s ease;
}

.mobile-menu .el-menu-item:hover .el-icon {
  color: var(--primary-color);
  transform: scale(1.1);
}

.mobile-menu .el-menu-item.is-active .el-icon {
  color: var(--primary-color);
  transform: scale(1.15);
}

.mobile-menu-footer {
  padding: 20px;
  border-top: 1px solid var(--border-lighter);
}

.logout-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, 
    rgba(245, 108, 108, 0.1), 
    rgba(255, 255, 255, 0.8));
  border: 2px solid rgba(245, 108, 108, 0.3);
  color: var(--danger-color);
  font-weight: 600;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.logout-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(245, 108, 108, 0.1), 
    transparent);
  transition: left 0.5s ease;
}

.logout-btn:hover {
  background: linear-gradient(135deg, 
    rgba(245, 108, 108, 0.2), 
    rgba(255, 255, 255, 0.9));
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(245, 108, 108, 0.2),
    0 4px 8px rgba(245, 108, 108, 0.1);
  border-color: rgba(245, 108, 108, 0.5);
}

.logout-btn:hover::before {
  left: 100%;
}

.logout-btn:active {
  transform: translateY(0) scale(0.98);
}

.el-divider {
  margin: 8px 0;
  background-color: var(--border-lighter);
}

/* 暗色主题适配 */
[data-theme="dark"] .mobile-nav-bar {
  background: linear-gradient(135deg, 
    rgba(30, 30, 30, 0.95) 0%, 
    rgba(40, 40, 40, 0.9) 50%,
    rgba(50, 50, 50, 0.85) 100%);
  border-bottom-color: var(--border-color);
}

[data-theme="dark"] .mobile-nav-bar::before {
  background: linear-gradient(90deg, 
    transparent, 
    rgba(64, 158, 255, 0.2), 
    rgba(103, 194, 58, 0.2), 
    transparent);
}

[data-theme="dark"] .mobile-nav-drawer :deep(.el-drawer) {
  background: linear-gradient(145deg, 
    rgba(30, 30, 30, 0.95) 0%,
    rgba(40, 40, 40, 0.9) 50%,
    rgba(50, 50, 50, 0.85) 100%);
}

[data-theme="dark"] .mobile-menu .el-menu-item {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .mobile-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .mobile-menu .el-menu-item.is-active {
  background-color: var(--primary-light);
  color: var(--primary-color);
}

/* 响应式调整 */
@media (max-width: 480px) {
  .mobile-nav-header {
    padding: 8px 16px;
    height: 56px;
    width: 100%;
    box-sizing: border-box;
  }
  
  .brand-link {
    font-size: 16px;
  }
  
  .brand-logo {
    height: 24px;
  }
  
  .nav-brand {
    display: flex;
    align-items: center;
  }
  
  .action-btn,
  .theme-toggle-btn {
    width: 38px;
    height: 38px;
  }
  
  .mobile-user-avatar {
    width: 38px !important;
    height: 38px !important;
  }
  
  .drawer-header {
    padding: 20px 16px;
  }
  
  .mobile-menu .el-menu-item {
    height: 52px;
    line-height: 52px;
    margin: 4px 12px;
    font-size: 16px;
  }
  
  .nav-actions {
    gap: 10px;
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

/* 性能优化 - 减少动画负担 */
@media (prefers-reduced-motion: reduce) {
  .brand-logo {
    animation: none;
  }
  
  .mobile-nav-bar::before,
  .mobile-nav-bar::after {
    animation: none;
  }
  
  .nav-badge :deep(.el-badge__content) {
    animation: none;
  }
  
  .mobile-menu .el-menu-item:hover,
  .mobile-menu .el-menu-item.is-active {
    transform: translateX(8px) scale(1.02);
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .mobile-menu .el-menu-item:hover {
    transform: none;
  }
  
  .action-btn:hover,
  .theme-toggle-btn:hover {
    transform: none;
  }
  
  .user-avatar-container:hover {
    transform: none;
  }
}

/* 为页面内容添加上内边距，确保内容不被导航栏遮挡 */
:deep(.main-content) {
  padding-top: 74px;
  box-sizing: border-box;
  width: 100%;
}

@media (max-width: 480px) {
  :deep(.main-content) {
    padding-top: 66px;
    padding-left: 12px;
    padding-right: 12px;
  }
}
</style>
