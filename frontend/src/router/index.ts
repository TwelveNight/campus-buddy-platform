import type { RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import HelpInfoList from '../views/HelpInfoList.vue'
import HelpInfoDetail from '../views/HelpInfoDetail.vue'
import HelpInfoPublish from '../views/HelpInfoPublish.vue'
import MyApplications from '../views/MyApplications.vue'
import MyHelpInfo from '../views/MyHelpInfo.vue'
import Profile from '../views/Profile.vue'
import Reviews from '../views/Reviews.vue'
import AdminHelpInfo from '../views/AdminHelpInfo.vue'
import Debug from '../views/Debug.vue' // 添加调试页面
// 动态导入组件
const GroupList = () => import('../views/GroupList.vue')
const GroupDetail = () => import('../views/GroupDetail.vue')
const GroupPreview = () => import('../views/GroupPreview.vue')
import { useAuthStore } from '../store/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { 
        path: '', 
        component: Home,
        meta: { requiresAuth: true }
      },
      { 
        path: 'login', 
        component: Login,
        meta: { guestOnly: true }
      },
      { 
        path: 'register', 
        component: Register,
        meta: { guestOnly: true } 
      },
      { 
        path: 'helpinfo', 
        component: HelpInfoList,
        meta: { requiresAuth: true }
      },
      { 
        path: 'helpinfo/publish', 
        component: HelpInfoPublish,
        meta: { requiresAuth: true }
      },
      { 
        path: 'helpinfo/:id', 
        component: HelpInfoDetail, 
        props: true,
        meta: { requiresAuth: true }
      },
      { 
        path: 'helpinfo/edit/:id', 
        component: HelpInfoPublish,
        props: true,
        meta: { requiresAuth: true }
      },
      {
        path: 'applications',
        component: MyApplications,
        meta: { requiresAuth: true }
      },
      {
        path: 'my/helpinfo',
        component: MyHelpInfo,
        meta: { requiresAuth: true }
      },
      {
        path: 'profile',
        component: Profile,
        meta: { requiresAuth: true }
      },
      {
        path: 'reviews',
        component: Reviews,
        meta: { requiresAuth: true }
      },
      {
        path: 'admin/helpinfo',
        component: AdminHelpInfo,
        meta: { requiresAuth: true }
      },
      {
        path: 'user/:userId',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue')
      },
      {
        path: 'groups',
        component: GroupList,
        meta: { requiresAuth: true }
      },
      {
        path: 'groups/:id',
        component: GroupPreview,
        props: true,
        meta: { requiresAuth: true }
      },
      {
        path: 'groups/:id/detail',
        component: GroupDetail,
        props: true,
        meta: { requiresAuth: true }
      },
      {
        path: 'debug',
        component: Debug,
        name: 'Debug',
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // 需要认证但未登录，重定向到登录页
    next('/login')
  } else if (to.meta.guestOnly && authStore.isAuthenticated) {
    // 已登录用户不允许访问的页面(如登录页)，重定向到首页
    next('/')
  } else {
    next()
  }
})

export default router
