<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>管理员控制台 - 用户管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="refreshData">刷新数据</el-button>
          </div>
        </div>
      </template>

      <div class="filter-area">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="关键词">
            <el-input v-model="filters.keyword" placeholder="用户名/昵称" clearable @keyup.enter="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="loading" :data="userList" style="width: 100%" border>
        <el-table-column type="index" width="50" align="center"></el-table-column>
        <el-table-column prop="userId" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120"></el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120"></el-table-column>
        <el-table-column prop="contactInfo" label="联系信息" min-width="120"></el-table-column>
        <el-table-column label="头像" width="80" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatarUrl || '/avatar-placeholder.png'"></el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="角色" width="180">
          <template #default="scope">
            <el-tag 
              v-for="role in scope.row.roles" 
              :key="role" 
              :type="role.includes('ADMIN') ? 'danger' : 'info'"
              style="margin-right: 4px"
            >
              {{ formatRoleName(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button-group>
              <el-button 
                :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'" 
                size="small" 
                @click="handleToggleStatus(scope.row)"
              >
                {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
              </el-button>
              <el-button 
                type="warning" 
                size="small" 
                @click="handleResetPassword(scope.row)"
              >
                重置密码
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
        </el-pagination>
      </div>
    </el-card>

    <!-- 重置密码弹窗 -->
    <el-dialog
      v-model="resetPasswordVisible"
      title="重置密码"
      width="400px"
    >
      <div v-if="newPassword" class="reset-password-content">
        <p>用户 <strong>{{ currentUser?.nickname || currentUser?.username }}</strong> 的密码已重置为：</p>
        <div class="password-box">
          <span>{{ newPassword }}</span>
          <el-button type="primary" size="small" @click="copyPassword" style="margin-left: 8px">
            复制
          </el-button>
        </div>
        <div class="password-warning">
          <el-alert
            title="请妥善保管此密码，关闭此窗口后将无法再次查看！"
            type="warning"
            :closable="false"
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUserPage, updateAdminUserStatus, resetAdminUserPassword } from '@/api/adminUser'
import type { UserVO } from '@/types/user'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const userList = ref<UserVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const resetPasswordVisible = ref(false)
const newPassword = ref('')
const currentUser = ref<UserVO | null>(null)

// 过滤条件
const filters = reactive({
  keyword: '',
  status: ''
})

// 状态选项
const statusOptions = [
  { value: 'ACTIVE', label: '正常' },
  { value: 'INACTIVE', label: '未激活' },
  { value: 'BANNED', label: '已禁用' }
]

onMounted(async () => {
  // 检查用户角色是否为管理员
  try {
    // 直接从后端获取管理员状态
    const isAdmin = await authStore.checkAdminStatus();
    
    if (!isAdmin) {
      ElMessage.error('权限不足，请使用管理员账号登录')
      router.push('/')
      return
    }
  } catch (error) {
    console.error('管理员状态检查失败:', error);
    ElMessage.error('管理员权限验证失败，请重新登录')
    router.push('/')
    return
  }

  fetchUserList()
})

// 获取用户列表
async function fetchUserList() {
  loading.value = true

  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filters.keyword || undefined,
      status: filters.status || undefined
    }

    const res = await getAdminUserPage(params)

    if (res.data.code === 200) {
      userList.value = res.data.data.records
      total.value = res.data.data.total
    } else {
      ElMessage.error(res.data.message || '获取列表失败')
    }
  } catch (error: any) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 刷新数据
function refreshData() {
  fetchUserList()
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchUserList()
}

// 重置过滤条件
function resetFilters() {
  filters.keyword = ''
  filters.status = ''
  currentPage.value = 1
  fetchUserList()
}

// 页码变化
function handleCurrentChange(val: number) {
  currentPage.value = val
  fetchUserList()
}

// 每页条数变化
function handleSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  fetchUserList()
}

// 启用/禁用用户
async function handleToggleStatus(user: UserVO) {
  const actionText = user.status === 'ACTIVE' ? '禁用' : '启用'
  const newStatus = user.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户 "${user.nickname || user.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await updateAdminUserStatus(user.userId, newStatus)
    
    if (res.data.code === 200) {
      ElMessage.success(`用户已${actionText}`)
      fetchUserList()
    } else {
      ElMessage.error(res.data.message || `${actionText}失败`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}失败: ` + error.message)
    }
  }
}

// 重置用户密码
async function handleResetPassword(user: UserVO) {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${user.nickname || user.username}" 的密码吗？`, '警告', {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    currentUser.value = user
    const res = await resetAdminUserPassword(user.userId)
    
    if (res.data.code === 200) {
      newPassword.value = res.data.data.newPassword
      resetPasswordVisible.value = true
    } else {
      ElMessage.error(res.data.message || '重置密码失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败: ' + error.message)
    }
  }
}

// 复制密码
function copyPassword() {
  if (!newPassword.value) return
  
  navigator.clipboard.writeText(newPassword.value)
    .then(() => {
      ElMessage.success('密码已复制到剪贴板')
    })
    .catch(err => {
      console.error('复制失败:', err)
      ElMessage.error('复制失败，请手动复制')
    })
}

// 获取状态标签类型
function getStatusType(status: string) {
  const map: Record<string, string> = {
    'ACTIVE': 'success',
    'INACTIVE': 'info',
    'BANNED': 'danger'
  }
  return map[status] || ''
}

// 获取状态标签文本
function getStatusLabel(status: string) {
  const map: Record<string, string> = {
    'ACTIVE': '正常',
    'INACTIVE': '未激活',
    'BANNED': '已禁用'
  }
  return map[status] || status
}

// 格式化角色名称
function formatRoleName(role: string) {
  if (role === 'ROLE_ADMIN') return '管理员'
  if (role === 'ROLE_USER') return '普通用户'
  return role.replace('ROLE_', '')
}

// 格式化日期
function formatDate(dateString: string | Date | number) {
  if (!dateString) return ''
  try {
    // 处理数字类型的时间戳（毫秒）
    const date = typeof dateString === 'number' ? new Date(dateString) :
      (typeof dateString === 'string' ? new Date(dateString) : dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    console.error('日期格式化错误:', error, dateString)
    return String(dateString)
  }
}
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}
.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.card-header h2 {
  margin: 0;
  color: #303133;
}
[data-theme="dark"] .card-header h2 {
  color: #e5eaf3;
}
</style>
