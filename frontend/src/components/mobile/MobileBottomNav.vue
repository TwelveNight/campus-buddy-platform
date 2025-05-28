<template>
  <div class="mobile-bottom-nav" v-if="isMobile && shouldShowBottomNav">
    <div class="bottom-nav-container">
      <div
        v-for="(item, index) in navItems"
        :key="index"
        class="nav-item"
        :class="{ 
          active: isActive(item.path),
          disabled: item.disabled 
        }"
        @click="handleNavClick(item)"
      >
        <div class="nav-icon">
          <el-badge v-if="item.badge" :value="item.badge" :max="99" type="danger">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
          </el-badge>
          <el-icon v-else>
            <component :is="item.icon" />
          </el-icon>
        </div>
        <div class="nav-label">{{ item.label }}</div>
        
        <!-- 激活指示器 -->
        <div class="active-indicator" v-if="isActive(item.path)"></div>
      </div>
    </div>
    
    <!-- 安全区域占位 -->
    <div class="safe-area-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { getUnreadMessageCount } from '@/api/message'
import {
  House,
  InfoFilled,
  User,
  Setting,
  UserFilled,
  ChatDotRound
} from '@element-plus/icons-vue'

interface NavItem {
  path: string
  label: string
  icon: any
  badge?: number
  disabled?: boolean
  requireAuth?: boolean
  adminOnly?: boolean
}

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isMobile = ref(false)

// 检查是否为移动端
const checkMobile = () => {
  isMobile.value = typeof window !== 'undefined' && window.innerWidth <= 768
}

const unreadMessageCount = ref(0)

// 导航项配置
const baseNavItems: NavItem[] = [
  {
    path: '/',
    label: '首页',
    icon: House
  },
  {
    path: '/helpinfo',
    label: '互助',
    icon: InfoFilled
  },
  {
    path: '/groups',
    label: '学习小组',
    icon: UserFilled,
    requireAuth: true
  },
  {
    path: '/messages',
    label: '消息',
    icon: ChatDotRound,
    requireAuth: true
  },
  {
    path: '/profile',
    label: '个人',
    icon: User,
    requireAuth: true
  }
]

// 计算显示的导航项
const navItems = computed(() => {
  let items = baseNavItems.map(item => ({ ...item }))

  // 动态设置消息 badge
  const msgIdx = items.findIndex(i => i.path === '/messages')
  if (msgIdx !== -1 && authStore.isAuthenticated) {
    items[msgIdx].badge = unreadMessageCount.value > 0 ? unreadMessageCount.value : undefined
  }

  // 如果用户已登录，显示完整菜单
  if (authStore.isAuthenticated) {
    // 如果是管理员，添加管理后台
    if (authStore.isAdmin) {
      items.push({
        path: '/admin',
        label: '管理',
        icon: Setting,
        adminOnly: true
      })
    }
  } else {
    // 未登录时，移除需要认证的项目
    items = items.filter(item => !item.requireAuth)
    
    // 添加登录项
    items.push({
      path: '/login',
      label: '登录',
      icon: User
    })
  }
  
  return items
})

// 判断是否应该显示底部导航
const shouldShowBottomNav = computed(() => {
  // 在某些页面隐藏底部导航
  const hiddenPaths = ['/login', '/register']
  return isMobile.value && !hiddenPaths.includes(route.path)
})

// 判断导航项是否激活
const isActive = (path: string) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

// 处理导航点击
const handleNavClick = (item: NavItem) => {
  if (item.disabled) return
  
  // 如果需要登录但用户未登录，跳转到登录页
  if (item.requireAuth && !authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  
  // 如果需要管理员权限但用户不是管理员
  if (item.adminOnly && !authStore.isAdmin) {
    return
  }
  
  router.push(item.path)
}

// 监听窗口大小变化
const handleResize = () => {
  checkMobile()
}

// 获取未读消息数量
const fetchUnreadMessageCount = async () => {
  try {
    const { data } = await getUnreadMessageCount()
    if (data.code === 200) {
      unreadMessageCount.value = data.data.count || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败', error)
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', handleResize)

  // 如果用户已登录，获取未读消息数量
  if (authStore.isAuthenticated) {
    fetchUnreadMessageCount()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<!-- 只在移动端显示 -->
<style scoped>
.mobile-bottom-nav {
  display: none;
}
@media (max-width: 768px) {
  .mobile-bottom-nav {
    display: block;
  }
}

.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: var(--card-bg);
  border-top: 1px solid var(--border-lighter);
  backdrop-filter: blur(10px);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  width: 100%;
  box-sizing: border-box;
}

.bottom-nav-container {
  display: flex;
  padding: 8px 0 4px;
  max-width: 100%;
  margin: 0 auto;
  width: 100%;
  justify-content: space-around;
  box-sizing: border-box;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 0 4px;
  min-height: 60px;
}

.nav-item:hover {
  background-color: var(--hover-bg);
}

.nav-item:active {
  /* 减轻按下效果，更加微妙 */
  transform: scale(0.98);
}

.nav-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-item.disabled:hover {
  background-color: transparent;
}

.nav-item.disabled:active {
  transform: none;
}

.nav-item.active {
  color: var(--primary-color);
  background-color: var(--primary-light);
}

.nav-icon {
  position: relative;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.nav-icon .el-icon {
  font-size: 22px;
  transition: all 0.3s ease;
}

.nav-item.active .nav-icon .el-icon {
  font-size: 24px;
  /* 移除旋转效果，只保留轻微的上移动画 */
  transform: translateY(-1px);
}

.nav-label {
  font-size: 11px;
  font-weight: 500;
  line-height: 1.2;
  text-align: center;
  transition: all 0.3s ease;
  margin-top: 2px;
}

.nav-item.active .nav-label {
  font-weight: 600;
  transform: translateY(-1px);
}

.active-indicator {
  position: absolute;
  top: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background-color: var(--primary-color);
  border-radius: 3px;
  /* 使用更简单的过渡动画替代弹跳动画 */
  transition: all 0.3s ease;
}

/* 移除弹跳动画 */

.safe-area-placeholder {
  height: env(safe-area-inset-bottom, 0px);
  background-color: var(--card-bg);
}

/* 徽章样式调整 */
:deep(.el-badge__content) {
  font-size: 10px;
  padding: 0 5px;
  height: 16px;
  line-height: 16px;
  border-radius: 8px;
  min-width: 16px;
}

/* 不同屏幕尺寸适配 */
@media (max-width: 480px) {
  .bottom-nav-container {
    padding: 6px 0 2px;
  }
  
  .nav-item {
    padding: 6px 2px;
    margin: 0 2px;
    min-height: 58px;
    width: 20%;
    box-sizing: border-box;
  }
  
  .nav-icon .el-icon {
    font-size: 22px;
  }
  
  .nav-item.active .nav-icon .el-icon {
    font-size: 24px;
  }
  
  .nav-label {
    font-size: 12px;
    margin-top: 4px;
    white-space: nowrap;
  }
}

@media (min-width: 481px) and (max-width: 768px) {
  .nav-item {
    margin: 0 6px;
    border-radius: 14px;
  }
  
  .nav-icon .el-icon {
    font-size: 24px;
  }
  
  .nav-item.active .nav-icon .el-icon {
    font-size: 26px;
  }
  
  .nav-label {
    font-size: 12px;
  }
}

/* 暗色主题适配 */
[data-theme="dark"] .mobile-bottom-nav {
  background-color: var(--dark-card-bg, #1e1e1e);
  border-top-color: var(--dark-border-color, #333);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.25);
}

[data-theme="dark"] .nav-item {
  color: var(--dark-text-secondary, #aaa);
}

[data-theme="dark"] .nav-item:hover {
  background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.05));
}

[data-theme="dark"] .nav-item.active {
  background-color: var(--dark-primary-light, rgba(64, 158, 255, 0.1));
  color: var(--primary-color-dark, #79bbff);
}

[data-theme="dark"] .active-indicator {
  background-color: var(--primary-color-dark, #79bbff);
}

/* 高分辨率屏幕 */
@media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
  .nav-icon .el-icon {
    image-rendering: -webkit-optimize-contrast;
    image-rendering: crisp-edges;
  }
}

/* 横屏模式 */
@media (orientation: landscape) and (max-height: 500px) {
  .bottom-nav-container {
    padding: 4px 0 2px;
  }
  
  .nav-item {
    min-height: 48px;
    padding: 4px 2px;
  }
  
  .nav-icon {
    margin-bottom: 2px;
  }
  
  .nav-icon .el-icon {
    font-size: 18px;
  }
  
  .nav-item.active .nav-icon .el-icon {
    font-size: 20px;
  }
  
  .nav-label {
    font-size: 9px;
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .nav-item {
    min-height: 64px; /* 增加触摸目标大小 */
  }
  
  .nav-item:hover {
    background-color: transparent;
  }
}

/* 减弱动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .nav-item,
  .nav-icon,
  .nav-icon .el-icon,
  .nav-label {
    transition: none;
  }
  
  .nav-item:active {
    transform: none;
  }
  
  .nav-item.active .nav-icon .el-icon,
  .nav-item.active .nav-label {
    transform: none;
  }
  
  .active-indicator {
    animation: none;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .mobile-bottom-nav {
    border-top-width: 2px;
  }
  
  .nav-item.active {
    border: 2px solid var(--primary-color);
  }
}
</style>
