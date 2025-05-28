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

        <el-tab-pane label="Redis缓存">
          <div class="cache-section">
            <h3>Redis连接状态</h3>
            <div class="connection-status">
              <el-button 
                type="primary" 
                :loading="testingConnection" 
                @click="testRedisConnection"
                icon="Connection"
              >
                测试Redis连接
              </el-button>
              <el-tag 
                v-if="connectionStatus !== null" 
                :type="connectionStatus ? 'success' : 'danger'"
                class="ml-2"
              >
                {{ connectionStatus ? 'Redis连接正常' : 'Redis连接失败' }}
              </el-tag>
            </div>

            <h3 class="mt-4">缓存统计信息</h3>
            <div class="cache-stats">
              <el-button 
                type="info" 
                :loading="loadingStats" 
                @click="loadCacheStats"
                icon="Refresh"
              >
                刷新统计
              </el-button>
              
              <div v-if="cacheStats" class="mt-2">
                <h4>应用缓存统计</h4>
                <el-descriptions border>
                  <el-descriptions-item label="用户缓存">{{ cacheStats.userCacheCount }}</el-descriptions-item>
                  <el-descriptions-item label="用户VO缓存">{{ cacheStats.userVOCacheCount }}</el-descriptions-item>
                  <el-descriptions-item label="Token缓存">{{ cacheStats.tokenCacheCount }}</el-descriptions-item>
                  <el-descriptions-item label="用户名缓存">{{ cacheStats.usernameCacheCount }}</el-descriptions-item>
                  <el-descriptions-item label="搜索缓存">{{ cacheStats.searchCacheCount }}</el-descriptions-item>
                  <el-descriptions-item label="信用分缓存">{{ cacheStats.creditScoreCacheCount }}</el-descriptions-item>
                  <el-descriptions-item label="小组帖子列表缓存">{{ cacheStats.groupPostsCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="帖子详情缓存">{{ cacheStats.postDetailCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="帖子用户缓存">{{ cacheStats.postUserCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="热门帖子缓存">{{ cacheStats.hotPostsCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="互助信息列表缓存">{{ cacheStats.helpInfoListCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="互助信息详情缓存">{{ cacheStats.helpInfoDetailCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="互助信息用户缓存">{{ cacheStats.helpInfoUserCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="互助信息管理缓存">{{ cacheStats.helpInfoAdminCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="互助信息搜索缓存">{{ cacheStats.helpInfoSearchCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="热门帖子缓存">{{ cacheStats.hotPostsCacheCount || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="总缓存数">
                    <el-tag type="primary">{{ cacheStats.totalCacheCount }}</el-tag>
                  </el-descriptions-item>
                </el-descriptions>

                <h4 class="mt-3">Redis服务器信息</h4>
                <el-descriptions v-if="cacheStats.redisInfo" border>
                  <el-descriptions-item label="Redis版本">{{ cacheStats.redisInfo.version }}</el-descriptions-item>
                  <el-descriptions-item label="运行时间">{{ formatUptime(cacheStats.redisInfo.uptime) }}</el-descriptions-item>
                  <el-descriptions-item label="连接数">{{ cacheStats.redisInfo.connectedClients }}</el-descriptions-item>
                  <el-descriptions-item label="内存使用">{{ formatMemory(cacheStats.redisInfo.usedMemory) }}</el-descriptions-item>
                  <el-descriptions-item label="数据库总Key数">
                    <el-tag type="warning">{{ cacheStats.redisInfo.totalKeys }}</el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </div>

            <h3 class="mt-4">缓存详细信息</h3>
            <div class="cache-details">
              <el-button 
                type="info" 
                :loading="loadingDetails" 
                @click="loadCacheDetails"
                icon="View"
              >
                查看详细信息
              </el-button>
              
              <div v-if="cacheDetails" class="mt-2">
                <h4>缓存类型分布</h4>
                <el-descriptions border>
                  <el-descriptions-item 
                    v-for="(count, type) in cacheDetails.typeCount" 
                    :key="type"
                    :label="type + '类型'"
                  >
                    {{ count }} 个
                  </el-descriptions-item>
                </el-descriptions>

                <h4 class="mt-3">Key详细信息</h4>
                <el-table 
                  :data="keyDetailsList" 
                  style="width: 100%" 
                  max-height="300"
                  v-if="keyDetailsList.length > 0"
                >
                  <el-table-column prop="key" label="缓存Key" min-width="200">
                    <template #default="{ row }">
                      <el-tag size="small">{{ row.key }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="type" label="类型" width="80" />
                  <el-table-column prop="ttl" label="TTL(秒)" width="80">
                    <template #default="{ row }">
                      <el-tag 
                        :type="row.ttl === -1 ? 'info' : (row.ttl > 0 ? 'success' : 'danger')"
                        size="small"
                      >
                        {{ row.ttl === -1 ? '永久' : row.ttl }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="size" label="大小" width="80">
                    <template #default="{ row }">
                      {{ row.size > 0 ? row.size + 'B' : '-' }}
                    </template>
                  </el-table-column>
                </el-table>

                <h4 class="mt-3">所有缓存Key列表</h4>
                <div class="cache-keys">
                  <el-tag 
                    v-for="key in cacheDetails.allKeys" 
                    :key="key"
                    size="small"
                    class="cache-key-tag"
                  >
                    {{ key }}
                  </el-tag>
                </div>
              </div>
            </div>

            <h3 class="mt-4">缓存管理操作</h3>
            <div class="cache-actions">
              <!-- 基础缓存操作 -->
              <el-card class="action-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Setting /></el-icon>
                    <span class="header-title">基础缓存操作</span>
                  </div>
                </template>
                
                <div class="action-grid">
                  <el-button 
                    type="danger"
                    :loading="clearingCache" 
                    @click="clearAllCache"
                    icon="Delete"
                    class="action-btn"
                  >
                    清空所有缓存
                  </el-button>
                  
                  <el-button 
                    type="warning" 
                    :loading="clearingSearchCache" 
                    @click="clearSearchCache"
                    icon="Search"
                    class="action-btn"
                  >
                    清空搜索缓存
                  </el-button>
                </div>
              </el-card>

              <!-- 用户缓存操作 -->
              <el-card class="action-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><User /></el-icon>
                    <span class="header-title">用户缓存操作</span>
                  </div>
                </template>
                
                <div class="action-grid">
                  <el-button 
                    type="primary" 
                    :loading="clearingUserCache" 
                    @click="clearCurrentUserCache"
                    icon="UserFilled"
                    :disabled="!authStore.user?.userId"
                    class="action-btn"
                  >
                    清空当前用户缓存
                  </el-button>
                  
                  <el-button 
                    type="warning" 
                    :loading="clearingCreditScoreCache" 
                    @click="clearCreditScoreCache"
                    icon="StarFilled"
                    class="action-btn"
                  >
                    清空信用分缓存
                  </el-button>
                  
                  <el-button 
                    type="warning" 
                    :loading="clearingUserCreditScoreCache" 
                    @click="clearCurrentUserCreditScoreCache"
                    icon="Medal"
                    :disabled="!authStore.user?.userId"
                    class="action-btn"
                  >
                    清空当前用户信用分
                  </el-button>
                </div>
              </el-card>

              <!-- 帖子缓存操作 -->
              <el-card class="action-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Document /></el-icon>
                    <span class="header-title">帖子缓存操作</span>
                  </div>
                </template>
                
                <div class="action-grid">
                  <el-button 
                    type="danger" 
                    :loading="clearingAllPostsCache" 
                    @click="clearAllPostsCache"
                    icon="Document"
                    class="action-btn"
                  >
                    清空所有帖子缓存
                  </el-button>
                  
                  <el-button 
                    type="warning" 
                    :loading="clearingHotPostsCache" 
                    @click="clearHotPostsCache"
                    icon="Star"
                    class="action-btn"
                  >
                    清空热门帖子缓存
                  </el-button>
                </div>

                <!-- 小组帖子缓存操作 -->
                <el-divider content-position="left">小组帖子缓存</el-divider>
                <div class="input-action-group">
                  <div class="input-with-label">
                    <label class="input-label">小组ID:</label>
                    <el-input-number 
                      v-model="groupIdInput" 
                      :min="1" 
                      placeholder="请输入小组ID"
                      class="input-field"
                      controls-position="right"
                    />
                  </div>
                  <el-button 
                    type="warning" 
                    :loading="clearingGroupPostsCache" 
                    @click="clearGroupPostsCache"
                    icon="Folder"
                    :disabled="!groupIdInput"
                    class="action-btn"
                  >
                    清空小组帖子缓存
                  </el-button>
                </div>

                <!-- 帖子详情缓存操作 -->
                <el-divider content-position="left">帖子详情缓存</el-divider>
                <div class="input-action-group">
                  <div class="input-with-label">
                    <label class="input-label">帖子ID:</label>
                    <el-input-number 
                      v-model="postIdInput" 
                      :min="1" 
                      placeholder="请输入帖子ID"
                      class="input-field"
                      controls-position="right"
                    />
                  </div>
                  <el-button 
                    type="warning" 
                    :loading="clearingPostDetailCache" 
                    @click="clearPostDetailCache"
                    icon="DocumentCopy"
                    :disabled="!postIdInput"
                    class="action-btn"
                  >
                    清空帖子详情缓存
                  </el-button>
                </div>
              </el-card>

              <!-- 互助信息缓存操作 -->
              <el-card class="action-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Help /></el-icon>
                    <span class="header-title">互助信息缓存操作</span>
                  </div>
                </template>
                
                <div class="action-grid">
                  <el-button 
                    type="danger" 
                    :loading="clearingAllHelpInfoCache" 
                    @click="clearAllHelpInfoCache"
                    icon="Delete"
                    class="action-btn"
                  >
                    清空所有互助信息缓存
                  </el-button>
                  
                  <el-button 
                    type="warning" 
                    :loading="clearingHelpInfoListCache" 
                    @click="clearHelpInfoListCache"
                    icon="List"
                    class="action-btn"
                  >
                    清空互助信息列表缓存
                  </el-button>
                </div>

                <!-- 互助信息详情缓存操作 -->
                <el-divider content-position="left">互助信息详情缓存</el-divider>
                <div class="input-action-group">
                  <div class="input-with-label">
                    <label class="input-label">互助ID:</label>
                    <el-input-number 
                      v-model="helpInfoIdInput" 
                      :min="1" 
                      placeholder="请输入互助信息ID"
                      class="input-field"
                      controls-position="right"
                    />
                  </div>
                  <el-button 
                    type="warning" 
                    :loading="clearingHelpInfoDetailCache" 
                    @click="clearHelpInfoDetailCache"
                    icon="DocumentCopy"
                    :disabled="!helpInfoIdInput"
                    class="action-btn"
                  >
                    清空互助信息详情缓存
                  </el-button>
                </div>
              </el-card>
            </div>
          </div>
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
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, User, Document } from '@element-plus/icons-vue'
import { cacheApi, type CacheStats, type CacheDetails } from '../api/cache'

const route = useRoute()
const authStore = useAuthStore()

// Redis缓存相关状态
const testingConnection = ref(false)
const connectionStatus = ref<boolean | null>(null)
const loadingStats = ref(false)
const loadingDetails = ref(false)
const clearingCache = ref(false)
const clearingUserCache = ref(false)
const clearingSearchCache = ref(false)
const clearingCreditScoreCache = ref(false)
const clearingUserCreditScoreCache = ref(false)
// 帖子缓存相关状态
const clearingAllPostsCache = ref(false)
const clearingGroupPostsCache = ref(false)
const clearingPostDetailCache = ref(false)
const clearingHotPostsCache = ref(false)
const groupIdInput = ref<number | null>(null)
const postIdInput = ref<number | null>(null)
// 互助信息缓存相关状态
const clearingAllHelpInfoCache = ref(false)
const clearingHelpInfoListCache = ref(false)
const clearingHelpInfoDetailCache = ref(false)
const helpInfoIdInput = ref<number | null>(null)
const cacheStats = ref<CacheStats | null>(null)
const cacheDetails = ref<CacheDetails | null>(null)

// 计算属性：将key详细信息转换为表格数据
const keyDetailsList = computed(() => {
  if (!cacheDetails.value?.keyDetails) return []
  
  return Object.entries(cacheDetails.value.keyDetails).map(([key, details]) => ({
    key,
    type: details.type,
    ttl: details.ttl,
    size: details.size
  }))
})

// 格式化运行时间
function formatUptime(seconds: number): string {
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}小时`
  return `${Math.floor(seconds / 86400)}天`
}

// 格式化内存大小
function formatMemory(bytes: number): string {
  if (bytes < 1024) return `${bytes}B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)}MB`
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)}GB`
}

// 获取本地存储的数据
function getLocalStorage(key: string): string | null {
  return localStorage.getItem(key)
}

// 测试Redis连接
async function testRedisConnection() {
  testingConnection.value = true
  try {
    await cacheApi.testConnection()
    connectionStatus.value = true
    ElMessage.success('Redis连接正常')
  } catch (error) {
    connectionStatus.value = false
    ElMessage.error('Redis连接测试失败')
    console.error('Redis连接测试失败:', error)
  } finally {
    testingConnection.value = false
  }
}

// 加载缓存统计信息
async function loadCacheStats() {
  loadingStats.value = true
  try {
    const data = await cacheApi.getStats()
    cacheStats.value = data // API已经提取了实际数据
    ElMessage.success('缓存统计信息已刷新')
  } catch (error) {
    ElMessage.error('获取缓存统计信息失败')
    console.error('获取缓存统计信息失败:', error)
  } finally {
    loadingStats.value = false
  }
}

// 加载缓存详细信息
async function loadCacheDetails() {
  loadingDetails.value = true
  try {
    const data = await cacheApi.getDetails()
    cacheDetails.value = data // API已经提取了实际数据
    ElMessage.success('缓存详细信息已加载')
  } catch (error) {
    ElMessage.error('获取缓存详细信息失败')
    console.error('获取缓存详细信息失败:', error)
  } finally {
    loadingDetails.value = false
  }
}

// 清空所有缓存
async function clearAllCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有Redis缓存吗？此操作将清除所有用户缓存数据。',
      '确认清空缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingCache.value = true
    await cacheApi.clearAll()
    ElMessage.success('所有缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空缓存失败')
      console.error('清空缓存失败:', error)
    }
  } finally {
    clearingCache.value = false
  }
}

// 清空当前用户缓存
async function clearCurrentUserCache() {
  if (!authStore.user?.userId) {
    ElMessage.warning('当前用户ID不存在')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清空用户ID ${authStore.user.userId} 的缓存吗？`,
      '确认清空用户缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingUserCache.value = true
    await cacheApi.clearUser(authStore.user.userId)
    ElMessage.success('当前用户缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空用户缓存失败')
      console.error('清空用户缓存失败:', error)
    }
  } finally {
    clearingUserCache.value = false
  }
}

// 清空搜索缓存
async function clearSearchCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有搜索缓存吗？',
      '确认清空搜索缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingSearchCache.value = true
    await cacheApi.clearSearch()
    ElMessage.success('搜索缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空搜索缓存失败')
      console.error('清空搜索缓存失败:', error)
    }
  } finally {
    clearingSearchCache.value = false
  }
}

// 清空信用分缓存
async function clearCreditScoreCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有信用分缓存吗？',
      '确认清空信用分缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingCreditScoreCache.value = true
    await cacheApi.clearCreditScore()
    ElMessage.success('信用分缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空信用分缓存失败')
      console.error('清空信用分缓存失败:', error)
    }
  } finally {
    clearingCreditScoreCache.value = false
  }
}

// 清空当前用户信用分缓存
async function clearCurrentUserCreditScoreCache() {
  if (!authStore.user?.userId) {
    ElMessage.warning('当前用户ID不存在')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清空用户ID ${authStore.user.userId} 的信用分缓存吗？`,
      '确认清空用户信用分缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingUserCreditScoreCache.value = true
    await cacheApi.clearUserCreditScore(authStore.user.userId)
    ElMessage.success('当前用户信用分缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空用户信用分缓存失败')
      console.error('清空用户信用分缓存失败:', error)
    }
  } finally {
    clearingUserCreditScoreCache.value = false
  }
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

// 清空所有帖子缓存
async function clearAllPostsCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有帖子相关缓存吗？这将包括帖子列表、详情、用户信息等。',
      '确认清空所有帖子缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingAllPostsCache.value = true
    await cacheApi.clearAllPosts()
    ElMessage.success('所有帖子缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空帖子缓存失败')
      console.error('清空帖子缓存失败:', error)
    }
  } finally {
    clearingAllPostsCache.value = false
  }
}

// 清空热门帖子缓存
async function clearHotPostsCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清空热门帖子缓存吗？',
      '确认清空热门帖子缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingHotPostsCache.value = true
    await cacheApi.clearHotPosts()
    ElMessage.success('热门帖子缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空热门帖子缓存失败')
      console.error('清空热门帖子缓存失败:', error)
    }
  } finally {
    clearingHotPostsCache.value = false
  }
}

// 清空小组帖子缓存
async function clearGroupPostsCache() {
  if (!groupIdInput.value) {
    ElMessage.warning('请输入小组ID')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清空小组ID ${groupIdInput.value} 的帖子列表缓存吗？`,
      '确认清空小组帖子缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingGroupPostsCache.value = true
    await cacheApi.clearGroupPosts(groupIdInput.value)
    ElMessage.success(`小组ID ${groupIdInput.value} 的帖子列表缓存已清空`)
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空小组帖子缓存失败')
      console.error('清空小组帖子缓存失败:', error)
    }
  } finally {
    clearingGroupPostsCache.value = false
  }
}

// 清空帖子详情缓存
async function clearPostDetailCache() {
  if (!postIdInput.value) {
    ElMessage.warning('请输入帖子ID')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清空帖子ID ${postIdInput.value} 的详情缓存吗？`,
      '确认清空帖子详情缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingPostDetailCache.value = true
    await cacheApi.clearPostDetail(postIdInput.value)
    ElMessage.success(`帖子ID ${postIdInput.value} 的详情缓存已清空`)
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空帖子详情缓存失败')
      console.error('清空帖子详情缓存失败:', error)
    }
  } finally {
    clearingPostDetailCache.value = false
  }
}

// 清空所有互助信息缓存
async function clearAllHelpInfoCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有互助信息相关缓存吗？这将包括互助信息列表、详情、用户信息等。',
      '确认清空所有互助信息缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingAllHelpInfoCache.value = true
    await cacheApi.clearAllHelpInfo()
    ElMessage.success('所有互助信息缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空互助信息缓存失败')
      console.error('清空互助信息缓存失败:', error)
    }
  } finally {
    clearingAllHelpInfoCache.value = false
  }
}

// 清空互助信息列表缓存
async function clearHelpInfoListCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清空互助信息列表缓存吗？',
      '确认清空互助信息列表缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingHelpInfoListCache.value = true
    await cacheApi.clearHelpInfoList()
    ElMessage.success('互助信息列表缓存已清空')
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空互助信息列表缓存失败')
      console.error('清空互助信息列表缓存失败:', error)
    }
  } finally {
    clearingHelpInfoListCache.value = false
  }
}

// 清空互助信息详情缓存
async function clearHelpInfoDetailCache() {
  if (!helpInfoIdInput.value) {
    ElMessage.warning('请输入互助信息ID')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清空互助信息ID ${helpInfoIdInput.value} 的详情缓存吗？`,
      '确认清空互助信息详情缓存',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    clearingHelpInfoDetailCache.value = true
    await cacheApi.clearHelpInfoDetail(helpInfoIdInput.value)
    ElMessage.success(`互助信息ID ${helpInfoIdInput.value} 的详情缓存已清空`)
    // 刷新统计信息
    await loadCacheStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空互助信息详情缓存失败')
      console.error('清空互助信息详情缓存失败:', error)
    }
  } finally {
    clearingHelpInfoDetailCache.value = false
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

.mt-2 {
  margin-top: 0.5rem;
}

.ml-2 {
  margin-left: 0.5rem;
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

/* Redis缓存相关样式 */
.cache-section {
  margin-bottom: 2rem;
}

.connection-status {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.cache-stats {
  margin-bottom: 1.5rem;
}

.cache-details {
  margin-bottom: 1.5rem;
}

.cache-keys {
  margin-top: 1rem;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fafafa;
}

.cache-key-tag {
  margin: 2px 4px;
  font-size: 12px;
  font-family: 'Courier New', monospace;
}

.cache-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 10px;
}

.action-card {
  flex: 1 1 320px;
  min-width: 300px;
  margin-bottom: 0;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: none;
}

.action-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
  color: #409eff;
}

.header-title {
  margin-left: 4px;
}

.action-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.input-action-group {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.input-with-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-label {
  font-size: 14px;
  color: #666;
  min-width: 56px;
}

.input-field {
  width: 120px;
}

.action-btn {
  min-width: 120px;
}

@media (max-width: 900px) {
  .cache-actions {
    flex-direction: column;
    gap: 16px;
  }

  .action-card {
    min-width: 0;
    width: 100%;
  }

  .action-grid {
    flex-direction: column;
    gap: 8px;
  }

  .input-action-group {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .debug-page {
    padding: 0 10px;
  }
  
  .cache-actions {
    flex-direction: column;
  }
  
  .cache-actions .el-button {
    width: 100%;
  }
}
</style>
