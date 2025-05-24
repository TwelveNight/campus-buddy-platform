<template>
  <div class="debug-page">
    <el-card class="debug-card">
      <template #header>
        <div class="card-header">
          <h2>系统调试信息</h2>
        </div>
      </template>

      <el-tabs>
        <el-tab-pane label="用户信息">
          <h3>用户基本信息</h3>
          <el-descriptions border>
            <el-descriptions-item label="用户名">{{ authStore.user?.username || '未登录' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ authStore.user?.userId || '无' }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ authStore.user?.nickname || '无' }}</el-descriptions-item>
            <el-descriptions-item label="头像">
              <el-avatar v-if="authStore.user?.avatarUrl" :src="authStore.user.avatarUrl" />
              <span v-else>无头像</span>
            </el-descriptions-item>
          </el-descriptions>

          <h3 class="mt-4">用户角色和权限</h3>
          <el-descriptions border>
            <el-descriptions-item label="已认证状态">
              <el-tag :type="authStore.isAuthenticated ? 'success' : 'danger'">
                {{ authStore.isAuthenticated ? '已认证' : '未认证' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="角色列表">
              <div v-if="authStore.user?.roles?.length">
                <el-tag v-for="role in authStore.user.roles" :key="role" class="mr-1">{{ role }}</el-tag>
              </div>
              <span v-else>无角色</span>
            </el-descriptions-item>
            <el-descriptions-item label="管理员状态">
              <el-tag :type="authStore.isAdmin ? 'success' : 'info'">
                {{ authStore.isAdmin ? '是管理员' : '非管理员' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <h3 class="mt-4">其他用户属性</h3>
          <pre>{{ JSON.stringify(authStore.user, null, 2) }}</pre>
        </el-tab-pane>

        <el-tab-pane label="路由信息">
          <h3>当前路由</h3>
          <el-descriptions border>
            <el-descriptions-item label="路径">{{ route.path }}</el-descriptions-item>
            <el-descriptions-item label="完整路径">{{ route.fullPath }}</el-descriptions-item>
            <el-descriptions-item label="路由名称">{{ route.name || '无名称' }}</el-descriptions-item>
            <el-descriptions-item label="参数">{{ JSON.stringify(route.params) }}</el-descriptions-item>
            <el-descriptions-item label="查询参数">{{ JSON.stringify(route.query) }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="本地存储">
          <h3>本地存储数据</h3>
          <el-descriptions border>
            <el-descriptions-item label="Token">{{ getLocalStorage('token') || '无' }}</el-descriptions-item>
            <el-descriptions-item label="User">
              <pre>{{ getLocalStorage('user') ? JSON.stringify(JSON.parse(getLocalStorage('user') || '{}'), null, 2) : '无' }}</pre>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>

      <div class="actions mt-4">
        <el-button type="primary" @click="refreshUserInfo">刷新用户信息</el-button>
        <el-button type="warning" @click="checkAdminStatus">检查管理员权限</el-button>
        <el-button type="danger" @click="$router.push('/')">返回首页</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const authStore = useAuthStore()

// 获取本地存储的数据
function getLocalStorage(key: string): string | null {
  return localStorage.getItem(key)
}

// 刷新用户信息
async function refreshUserInfo() {
  try {
    await authStore.fetchCurrentUser()
    ElMessage.success('用户信息已刷新')
  } catch (error) {
    ElMessage.error('刷新用户信息失败')
    console.error('刷新用户信息失败:', error)
  }
}

// 检查管理员权限
async function checkAdminStatus() {
  try {
    const isAdmin = await authStore.checkAdminStatus();
    ElMessage.success(`管理员权限检查结果: ${isAdmin ? '是管理员' : '不是管理员'}`)
  } catch (error) {
    ElMessage.error('检查管理员权限失败')
    console.error('检查管理员权限失败:', error)
  }
}
</script>

<style scoped>
.debug-page {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
}

.debug-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.mt-4 {
  margin-top: 1rem;
}

.mr-1 {
  margin-right: 0.25rem;
}

.actions {
  display: flex;
  gap: 10px;
}

pre {
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}
</style>
