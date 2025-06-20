import type { RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Home from '../views/Home.vue'
import HelpInfoList from '../views/helpinfo/HelpInfoList.vue'
import HelpInfoDetail from '../views/helpinfo/HelpInfoDetail.vue'
import HelpInfoPublish from '../views/helpinfo/HelpInfoPublish.vue'
import MyApplications from '../views/user/MyApplications.vue'
import MyHelpInfo from '../views/helpinfo/MyHelpInfo.vue'
import Profile from '../views/user/Profile.vue'
import Reviews from '../views/user/Reviews.vue'
import AdminHelpInfo from '../views/admin/AdminHelpInfo.vue'
import Debug from '../views/Debug.vue' // 添加调试页面
import WebSocketDebug from '../views/WebSocketDebug.vue' // 添加WebSocket调试页面
import WebSocketSimpleDebug from '../views/WebSocketSimpleDebug.vue' // 添加简易WebSocket调试页面
import WebSocketConnectionTest from '../views/WebSocketConnectionTest.vue' // 添加WebSocket连接测试页面
// 动态导入组件
const GroupList = () => import('../views/group/GroupList.vue')
const GroupDetail = () => import('../views/group/GroupDetail.vue')
const GroupPreview = () => import('../views/group/GroupPreview.vue')
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
        path: 'notifications',
        component: () => import('../views/user/Notifications.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'admin',
        redirect: '/admin/dashboard',
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/helpinfo',
        component: AdminHelpInfo,
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/groups',
        component: () => import('../views/admin/AdminGroupManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/posts',
        component: () => import('../views/admin/AdminPostManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/dashboard',
        component: () => import('../views/admin/AdminDashboard.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/users',
        component: () => import('../views/admin/AdminUserManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'user/:userId',
        name: 'UserProfile',
        component: () => import('../views/user/UserProfile.vue')
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
        path: 'groups/:groupId/posts/:postId',
        component: () => import('../views/group/PostDetail.vue'),
        props: true,
        meta: { requiresAuth: true }
      },
      {
        path: 'debug',
        component: Debug,
        name: 'Debug',
        meta: { requiresAuth: true }
      },
      {
        path: 'ws-debug',
        component: WebSocketDebug,
        name: 'WebSocketDebug',
        meta: { requiresAuth: true }
      },
      {
        path: 'ws-simple',
        component: WebSocketSimpleDebug,
        name: 'WebSocketSimpleDebug',
        meta: { requiresAuth: true }
      },
      {
        path: 'ws-test',
        component: WebSocketConnectionTest,
        name: 'WebSocketConnectionTest',
        meta: { requiresAuth: false } // 不需要认证，便于测试
      },
      {
        path: 'messages',
        component: () => import('../views/user/Messages.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'messages/:userId',
        component: () => import('../views/user/Messages.vue'),
        props: true,
        meta: { requiresAuth: true }
      },
      {
        path: 'friends',
        component: () => import('../views/user/Friends.vue'),
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
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    // 需要管理员权限但不是管理员，重定向到首页
    next('/')
  } else if (to.meta.guestOnly && authStore.isAuthenticated) {
    // 已登录用户不允许访问的页面(如登录页)，重定向到首页
    next('/')
  } else {
    next()
  }
})

export default router
