<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>管理员控制台 - 帖子内容管理</h2>
        </div>
      </template>

      <div class="filter-area">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="关键词">
            <el-input v-model="filters.keyword" placeholder="标题/内容" clearable
              @keyup.enter="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="小组">
            <el-select v-model="filters.groupId" placeholder="所属小组" clearable filterable remote
              :remote-method="searchGroups" :loading="groupsLoading">
              <el-option v-for="group in groupOptions" :key="group.groupId" :label="group.name"
                :value="group.groupId"></el-option>
            </el-select>
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

      <el-table v-loading="loading" :data="postList" style="width: 100%" border>
        <el-table-column type="index" width="50" align="center"></el-table-column>
        <el-table-column prop="postId" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <router-link :to="`/groups/${scope.row.groupId}/posts/${scope.row.postId}`" class="post-title-link">
              {{ scope.row.title }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="所属小组" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <router-link :to="`/groups/${scope.row.groupId}`" class="group-link">
              {{ scope.row.groupName }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authorNickname" label="作者" width="120"></el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞" width="80" align="center"></el-table-column>
        <el-table-column prop="commentCount" label="评论" width="80" align="center"></el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button-group>
              <el-button type="info" size="small" link
                @click="viewPost(scope.row)">
                查看
              </el-button>
              <el-button 
                :type="scope.row.status === 'NORMAL' ? 'danger' : 'success'" 
                size="small" 
                link
                @click="handleToggleStatus(scope.row)"
              >
                {{ scope.row.status === 'NORMAL' ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" size="small" link @click="confirmRemove(scope.row.postId)">
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

    <!-- 帖子详情对话框 -->
    <el-dialog
      v-model="postDialogVisible"
      title="帖子详情"
      width="70%"
      destroy-on-close
    >
      <div class="post-detail-dialog" v-if="currentPost">
        <h3 class="post-title">{{ currentPost.title }}</h3>
        <div class="post-meta">
          <span>作者: {{ currentPost.authorNickname }}</span>
          <span>发布时间: {{ formatDate(currentPost.createdAt) }}</span>
          <span>状态: 
            <el-tag :type="getStatusType(currentPost.status)">
              {{ getStatusLabel(currentPost.status) }}
            </el-tag>
          </span>
        </div>
        <div class="post-content">
          <div v-if="currentPost.contentType === 'MARKDOWN'" v-html="renderMarkdown(currentPost.content)"></div>
          <div v-else v-html="renderText(currentPost.content)"></div>
        </div>
        <div class="post-actions">
          <el-button-group>
            <el-button 
              :type="currentPost.status === 'NORMAL' ? 'danger' : 'success'" 
              @click="handleToggleStatus(currentPost)"
            >
              {{ currentPost.status === 'NORMAL' ? '禁用帖子' : '启用帖子' }}
            </el-button>
            <el-button type="danger" @click="confirmRemove(currentPost.postId)">
              删除帖子
            </el-button>
          </el-button-group>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { getAdminPosts, deletePost, updatePostStatus } from '../../api/groupPost'
import { getGroups } from '../../api/group'
import { marked } from 'marked'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const postList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const groupsLoading = ref(false)
const groupOptions = ref<any[]>([])
const postDialogVisible = ref(false)
const currentPost = ref<any>(null)

// 过滤条件
const filters = reactive({
  keyword: '',
  groupId: '',
  status: ''
})

// 状态选项
const statusOptions = [
  { value: 'NORMAL', label: '正常' },
  { value: 'HIDDEN', label: '已禁用' }
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

  fetchPostList()
  loadInitialGroups()
})

// 获取帖子列表
async function fetchPostList() {
  loading.value = true

  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filters.keyword || undefined,
      groupId: filters.groupId || undefined,
      status: filters.status || undefined
    }

    const res = await getAdminPosts(params)

    if (res.data.code === 200) {
      postList.value = res.data.data.records || res.data.data || []
      total.value = res.data.data.total || postList.value.length
    } else {
      ElMessage.error(res.data.message || '获取列表失败')
    }
  } catch (error: any) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error('获取列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载初始小组列表
async function loadInitialGroups() {
  groupsLoading.value = true
  try {
    const res = await getGroups({ page: 1, size: 20 })
    if (res.data.code === 200) {
      groupOptions.value = res.data.data.records || res.data.data || []
    }
  } catch (error) {
    console.error('加载小组列表失败:', error)
  } finally {
    groupsLoading.value = false
  }
}

// 搜索小组
async function searchGroups(query: string) {
  if (!query) return
  groupsLoading.value = true
  try {
    const res = await getGroups({ keyword: query, page: 1, size: 20 })
    if (res.data.code === 200) {
      groupOptions.value = res.data.data.records || res.data.data || []
    }
  } catch (error) {
    console.error('搜索小组失败:', error)
  } finally {
    groupsLoading.value = false
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchPostList()
}

// 重置过滤条件
function resetFilters() {
  Object.keys(filters).forEach(key => {
    filters[key as keyof typeof filters] = ''
  })
  currentPage.value = 1
  fetchPostList()
}

// 页码变化
function handleCurrentChange(val: number) {
  currentPage.value = val
  fetchPostList()
}

// 每页条数变化
function handleSizeChange(val: number) {
  pageSize.value = val
  currentPage.value = 1
  fetchPostList()
}

// 查看帖子详情
function viewPost(post: any) {
  currentPost.value = post
  postDialogVisible.value = true
}

// 渲染Markdown内容
function renderMarkdown(content: string) {
  if (!content) return ''
  try {
    return marked(content)
  } catch (error) {
    console.error('Markdown渲染错误:', error)
    return content
  }
}

// 渲染纯文本内容
function renderText(content: string) {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 切换帖子状态（启用/禁用）
async function handleToggleStatus(post: any) {
  const newStatus = post.status === 'NORMAL' ? 'HIDDEN' : 'NORMAL'
  const actionText = newStatus === 'NORMAL' ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}帖子 "${post.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await updatePostStatus(post.postId, newStatus)
    
    if (res.data.code === 200) {
      ElMessage.success(`帖子已${actionText}`)
      // 更新当前查看的帖子状态
      if (currentPost.value && currentPost.value.postId === post.postId) {
        currentPost.value.status = newStatus
      }
      fetchPostList()
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
function confirmRemove(postId: number) {
  ElMessageBox.confirm(
    '确定要删除这篇帖子吗？此操作将同时删除该帖子的所有评论。此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deletePost(postId)

      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        // 如果当前正在查看这篇帖子，关闭对话框
        if (postDialogVisible.value && currentPost.value && currentPost.value.postId === postId) {
          postDialogVisible.value = false
        }
        fetchPostList()
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
    'NORMAL': '正常',
    'HIDDEN': '已禁用'
  }
  return map[status] || status
}

// 获取状态类型颜色
function getStatusType(status: string) {
  const map: Record<string, string> = {
    'NORMAL': 'success',
    'HIDDEN': 'danger'
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

.post-title-link, .group-link {
  color: #409EFF;
  text-decoration: none;
}

.post-title-link:hover, .group-link:hover {
  text-decoration: underline;
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.post-detail-dialog {
  padding: 10px;
}

.post-title {
  font-size: 1.5rem;
  margin-bottom: 10px;
  color: #303133;
}

.post-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  color: #606266;
  font-size: 0.9rem;
}

.post-content {
  padding: 15px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  background-color: #FAFAFA;
  margin-bottom: 20px;
  min-height: 100px;
}

.post-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

[data-theme="dark"] .card-header h2,
[data-theme="dark"] .post-title {
  color: #e5eaf3;
}

[data-theme="dark"] .post-meta {
  color: #a6a9ad;
}

[data-theme="dark"] .post-content {
  background-color: #2c2e30;
  border-color: #4c4e50;
}
</style>
