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

// 导航项类型定义，补充 badge/disabled/requireAuth/adminOnly
export interface NavItem {
  path: string
  label: string
  icon: any
  badge?: number
  disabled?: boolean
  requireAuth?: boolean
  adminOnly?: boolean
}

export function useMobileNav() {
  const router = useRouter()
  const route = useRoute()
  const authStore = useAuthStore()
  const isMobile = ref(false)
  const unreadMessageCount = ref(0)

  const checkMobile = () => {
    isMobile.value = typeof window !== 'undefined' && window.innerWidth <= 768
  }

  const baseNavItems: NavItem[] = [
    { path: '/', label: '首页', icon: House },
    { path: '/helpinfo', label: '互助', icon: InfoFilled },
    { path: '/groups', label: '学习小组', icon: UserFilled, requireAuth: true },
    { path: '/messages', label: '消息', icon: ChatDotRound, requireAuth: true },
    { path: '/profile', label: '个人', icon: User, requireAuth: true }
  ]

  const navItems = computed<NavItem[]>(() => {
    let items = baseNavItems.map(item => ({ ...item }))
    const msgIdx = items.findIndex(i => i.path === '/messages')
    if (msgIdx !== -1 && authStore.isAuthenticated) {
      items[msgIdx].badge = unreadMessageCount.value > 0 ? unreadMessageCount.value : undefined
    }
    if (authStore.isAuthenticated) {
      if (authStore.isAdmin) {
        items.push({ path: '/admin', label: '管理', icon: Setting, adminOnly: true })
      }
    } else {
      items = items.filter(item => !item.requireAuth)
      items.push({ path: '/login', label: '登录', icon: User })
    }
    return items
  })

  const shouldShowBottomNav = computed(() => {
    const hiddenPaths = ['/login', '/register']
    return isMobile.value && !hiddenPaths.includes(route.path)
  })

  const isActive = (path: string) => {
    if (path === '/') return route.path === '/'
    return route.path.startsWith(path)
  }

  const handleNavClick = (item: NavItem) => {
    if (item.disabled) return
    if (item.requireAuth && !authStore.isAuthenticated) {
      router.push('/login')
      return
    }
    if (item.adminOnly && !authStore.isAdmin) return
    router.push(item.path)
  }

  const handleResize = () => checkMobile()

  const fetchUnreadMessageCount = async () => {
    try {
      const { data } = await getUnreadMessageCount()
      if (data.code === 200) unreadMessageCount.value = data.data.count || 0
    } catch (e) {
      // ignore
    }
  }

  onMounted(() => {
    checkMobile()
    window.addEventListener('resize', handleResize)
    if (authStore.isAuthenticated) fetchUnreadMessageCount()
  })
  onUnmounted(() => window.removeEventListener('resize', handleResize))

  return {
    navItems,
    isMobile,
    shouldShowBottomNav,
    isActive,
    handleNavClick
  }
}
