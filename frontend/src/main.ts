import { createApp } from 'vue'
import './style.css'
import './styles/dialog-fix.css' // 引入对话框修复样式
import './styles/theme.css' // 引入主题样式
import './styles/responsive.css' // 引入响应式样式
import App from './App.vue'
import router from './router/index'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { useAuthStore } from './store/auth'

const app = createApp(App)
const pinia = createPinia()

// 注册所有ElementPlus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia).use(ElementPlus)

// 全局前置导航守卫 - 在应用挂载前添加，确保获取用户信息
const authStore = useAuthStore(pinia)
router.beforeEach(async (to, from, next) => {
  // 如果已认证但没有完整用户信息，则获取用户信息
  if (authStore.isAuthenticated && (!authStore.user || !authStore.user.roles)) {
    try {
      await authStore.fetchCurrentUser()
      console.log('导航守卫中获取到用户信息:', authStore.user)
      
      // 检查管理员状态
      if (authStore.isAuthenticated) {
        const isAdmin = await authStore.checkAdminStatus();
        console.log('用户管理员状态检查结果:', isAdmin);
      }
    } catch (error) {
      console.error('导航守卫中获取用户信息失败:', error)
    }
  }
  
  // 处理需要管理员权限的路由
  if (to.path.startsWith('/admin') && !authStore.isAdmin) {
    console.warn('尝试访问管理员页面，但用户没有管理员权限')
    next('/')
    return
  }
  
  next()
})

app.use(router).mount('#app')
