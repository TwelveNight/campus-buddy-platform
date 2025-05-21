import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import HelpInfoList from '../views/HelpInfoList.vue'
import HelpInfoDetail from '../views/HelpInfoDetail.vue'
import HelpInfoPublish from '../views/HelpInfoPublish.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/helpinfo' },
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'helpinfo', component: HelpInfoList },
      { path: 'helpinfo/publish', component: HelpInfoPublish },
      { path: 'helpinfo/:id', component: HelpInfoDetail, props: true },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
