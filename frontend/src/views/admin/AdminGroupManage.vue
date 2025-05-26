<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>管理员控制台 - 学习小组管理</h2>
        </div>
      </template>

      <div class="filter-area">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="关键词">
            <el-input v-model="filters.keyword" placeholder="小组名称/描述" clearable
              @keyup.enter="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label"
                :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="loading" :data="groupList" style="width: 100%" border>
        <el-table-column type="index" width="50" align="center"></el-table-column>
        <el-table-column prop="groupId" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="name" label="小组名称" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <router-link :to="`/groups/${scope.row.groupId}`" class="group-name-link">
              {{ scope.row.name }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip></el-table-column>
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
        <el-table-column prop="memberCount" label="成员数" width="80" align="center"></el-table-column>
        <el-table-column prop="creatorNickname" label="创建者" width="120"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button-group>
              <el-button type="info" size="small" link
                @click="$router.push(`/groups/${scope.row.groupId}`)">
                查看
              </el-button>
              <el-button 
                :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'" 
                size="small" 
                link
                @click="handleToggleStatus(scope.row)"
              >
                {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" size="small" link @click="confirmRemove(scope.row.groupId)">
                删除
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
          @current-change="handleCurrentChange">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { getAdminGroups, adminDeleteGroup, updateGroupStatus } from '../../api/group'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const groupList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 过滤条件
const filters = reactive({
  keyword: '',
  status: ''
})

// 状态选项
const statusOptions = [
  { value: 'ACTIVE', label: '正常' },
  { value: 'INACTIVE', label: '已禁用' }
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

  fetchGroupList()
})

// 获取小组列表
async function fetchGroupList() {
  loading.value = true

  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      admin: true // 标记这是管理员请求
    }

    const res = await getAdminGroups(params)

    if (res.data.code === 200) {
      groupList.value = res.data.data.records || res.data.data || []
      total.value = res.data.data.total || groupList.value.length
    } else {
      ElMessage.error(res.data.message || '获取列表失败')
    }
  } catch (error: any) {
    console.error('获取学习小组列表失败:', error)
    ElMessage.error('获取列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchGroupList()
}

// 重置过滤条件
function resetFilters() {
  Object.keys(filters).forEach(key => {
    filters[key as keyof typeof filters] = ''
  })
  currentPage.value = 1
  fetchGroupList()
}

// 页码变化
function handleCurrentChange(val: number) {
  currentPage.value = val
  fetchGroupList()
}

// 每页条数变化
function handleSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  fetchGroupList()
}

// 切换小组状态（启用/禁用）
async function handleToggleStatus(group: any) {
  const newStatus = group.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const actionText = newStatus === 'ACTIVE' ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}小组 "${group.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await updateGroupStatus(group.groupId, newStatus)
    
    if (res.data.code === 200) {
      ElMessage.success(`小组已${actionText}`)
      fetchGroupList()
    } else {
      ElMessage.error(res.data.message || `${actionText}失败`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}失败: ` + error.message)
    }
  }
}

// 确认删除
function confirmRemove(groupId: number) {
  ElMessageBox.confirm(
    '确定要删除这个学习小组吗？此操作将删除小组的所有帖子和文件。此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await adminDeleteGroup(groupId)

      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchGroupList()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (error: any) {
      ElMessage.error('删除失败: ' + error.message)
    }
  }).catch(() => { })
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

// 获取状态标签
function getStatusLabel(status: string) {
  const map: Record<string, string> = {
    'ACTIVE': '正常',
    'INACTIVE': '已禁用'
  }
  return map[status] || status
}

// 获取状态类型颜色
function getStatusType(status: string) {
  const map: Record<string, string> = {
    'ACTIVE': 'success',
    'INACTIVE': 'danger'
  }
  return map[status] || ''
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.filter-area {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.group-name-link {
  color: #409EFF;
  text-decoration: none;
}

.group-name-link:hover {
  text-decoration: underline;
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

[data-theme="dark"] .card-header h2 {
  color: #e5eaf3;
}
</style>
