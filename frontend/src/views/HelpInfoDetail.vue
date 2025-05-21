<template>
  <div class="helpinfo-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-// 判断当前用户是否为发布者
const isPublisher = computed(() => {
    if (!info.value || !authStore.user || !authStore.user.userId) return false
    return info.value.publisherId === authStore.user.userId
})r">
          <h2>{{ info?.title || '互助信息详情' }}</h2>
          <div class="header-actions" v-if="info && isPublisher">
            <el-button-group>
              <el-button size="small" type="primary" :disabled="!canChangeStatus"
                @click="statusDialogVisible = true">修改状态</el-button>
              <el-button size="small" :disabled="!canEdit">编辑</el-button>
              <el-button size="small" type="danger" :disabled="!canDelete" @click="confirmDelete">删除</el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <div v-if="error" class="error-container">
        <el-alert :title="error" type="error" :closable="false" />
      </div>

      <div v-else-if="info" class="info-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="发布者">
            <div class="publisher-info">
              <el-avatar :size="30"
                :src="info.publisherAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
              <span>{{ info.publisherName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(info.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag>{{ getTypeLabel(info.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(info.status)">{{ getStatusLabel(info.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预期时间">{{ info.expectedTime }}</el-descriptions-item>
          <el-descriptions-item label="预期地点">{{ info.expectedLocation }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ info.contactMethod }}</el-descriptions-item>
          <el-descriptions-item label="悬赏金额" v-if="info.rewardAmount">{{ info.rewardAmount }} 元</el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ info.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            <div class="description" v-html="info.description"></div>
          </el-descriptions-item>
          <el-descriptions-item label="相关图片" :span="2" v-if="imageList.length > 0">
            <div class="image-gallery">
              <el-image v-for="(url, index) in imageList" :key="index" :src="url" :preview-src-list="imageList"
                :initial-index="index" fit="cover" class="gallery-image">
              </el-image>
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 申请列表 - 仅发布者可见 -->
        <div class="applications-section" v-if="isPublisher && applications.length > 0">
          <h3>申请列表</h3>
          <el-table :data="applications" style="width: 100%">
            <el-table-column prop="applicantNickname" label="申请人" width="120"></el-table-column>
            <el-table-column prop="message" label="申请消息"></el-table-column>
            <el-table-column prop="contactInfo" label="联系方式" width="150"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getApplicationStatusType(scope.row.status)">
                  {{ getApplicationStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" v-if="info.status === 'OPEN'">
              <template #default="scope">
                <el-button-group v-if="scope.row.status === 'PENDING'">
                  <el-button size="small" type="success" @click="handleAcceptApplication(scope.row.id)">接受</el-button>
                  <el-button size="small" type="danger" @click="handleRejectApplication(scope.row.id)">拒绝</el-button>
                </el-button-group>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 进度信息 - 当互助信息状态为处理中时显示 -->
        <div class="progress-section" v-if="info.status === 'IN_PROGRESS' && acceptedApplication">
          <h3>处理进度</h3>
          <el-alert title="此互助信息正在处理中" type="warning" :closable="false"
            :description="`由 ${acceptedApplication.applicantNickname} 提供帮助`">
          </el-alert>

          <div class="action-buttons" v-if="isPublisher">
            <el-button type="success" @click="handleComplete">标记为已解决</el-button>
            <el-button @click="confirmCancel">取消合作</el-button>
          </div>
        </div>

        <!-- 操作按钮 - 非发布者且互助信息状态为进行中可见 -->
        <div class="action-container" v-if="info.status === 'OPEN' && !isPublisher">
          <el-button type="primary" @click="applyDialogVisible = true">申请帮助</el-button>
        </div>
      </div>
    </el-card>

    <!-- 申请帮助对话框 -->
    <ApplyHelpDialog v-if="info" v-model:visible="applyDialogVisible" :help-info-id="Number(route.params.id)"
      @success="fetchApplications" />

    <!-- 状态修改对话框 -->
    <el-dialog v-model="statusDialogVisible" title="修改状态" width="400px">
      <el-form label-position="top">
        <el-form-item label="选择新状态">
          <el-select v-model="newStatus" placeholder="请选择状态" style="width: 100%">
            <el-option v-for="status in availableStatuses" :key="status.value" :label="status.label"
              :value="status.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleStatusUpdate" :loading="updateLoading">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useHelpInfoStore } from '../store/helpinfo'
import { useAuthStore } from '../store/auth'
import {
  getApplications,
  acceptApplication,
  rejectApplication,
  completeHelpInfo,
  closeHelpInfo
} from '../api/helpApplication'
import { deleteHelpInfo, updateHelpInfo, incrementHelpInfoViewCount } from '../api/helpinfo'
import ApplyHelpDialog from '../components/ApplyHelpDialog.vue'

const route = useRoute()
const router = useRouter()
const helpInfoStore = useHelpInfoStore()
const authStore = useAuthStore()
const info = computed(() => helpInfoStore.detail)
const loading = computed(() => helpInfoStore.loading)
const error = ref('')
const applications = ref<any[]>([])
const applyDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const newStatus = ref('')
const updateLoading = ref(false)

// 判断当前用户是否为发布者
const isPublisher = computed(() => {
  if (!info.value || !authStore.user) return false
  return info.value.publisherId === authStore.user.id
})

// 获取已接受的申请
const acceptedApplication = computed(() => {
  return applications.value.find(app => app.status === 'ACCEPTED')
})

// 解析图片列表
const imageList = computed(() => {
  if (!info.value || !info.value.imageUrls) return []
  try {
    return JSON.parse(info.value.imageUrls)
  } catch (e) {
    return info.value.imageUrls ? [info.value.imageUrls] : []
  }
})

// 权限控制
const canEdit = computed(() => info.value?.status === 'OPEN')
const canDelete = computed(() => info.value?.status === 'OPEN')
const canChangeStatus = computed(() => ['OPEN', 'IN_PROGRESS'].includes(info.value?.status))

// 可选状态列表
const availableStatuses = computed(() => {
  const statuses = []
  if (info.value) {
    if (info.value.status === 'OPEN') {
      statuses.push({ label: '关闭', value: 'CLOSED' })
    } else if (info.value.status === 'IN_PROGRESS') {
      statuses.push({ label: '已解决', value: 'RESOLVED' })
      statuses.push({ label: '关闭', value: 'CLOSED' })
    }
  }
  return statuses
})

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    if (isNaN(id)) {
      error.value = '非法的互助信息ID'
      return
    }
    await helpInfoStore.fetchDetail(id)
    await fetchApplications()

    // 增加浏览量
    try {
      await incrementHelpInfoViewCount(id)
    } catch (e) {
      console.error('增加浏览量失败', e)
    }
  } catch (e: any) {
    error.value = e.message || '获取详情失败'
  }
})

// 获取申请列表
async function fetchApplications() {
  if (!isPublisher.value) return

  try {
    const id = Number(route.params.id)
    const res = await getApplications(id)
    if (res.data.code === 200) {
      applications.value = res.data.data || []
    }
  } catch (e: any) {
    ElMessage.error('获取申请列表失败')
  }
}

// 接受申请
async function handleAcceptApplication(applicationId: number) {
  try {
    const id = Number(route.params.id)
    const res = await acceptApplication(id, applicationId)
    if (res.data.code === 200) {
      ElMessage.success('已接受申请')
      await helpInfoStore.fetchDetail(id) // 刷新互助信息状态
      await fetchApplications() // 刷新申请列表
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 拒绝申请
async function handleRejectApplication(applicationId: number) {
  try {
    const id = Number(route.params.id)
    const res = await rejectApplication(id, applicationId)
    if (res.data.code === 200) {
      ElMessage.success('已拒绝申请')
      await fetchApplications() // 刷新申请列表
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 完成互助
async function handleComplete() {
  try {
    const id = Number(route.params.id)
    const res = await completeHelpInfo(id)
    if (res.data.code === 200) {
      ElMessage.success('互助已完成')
      await helpInfoStore.fetchDetail(id)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 确认取消合作
function confirmCancel() {
  ElMessageBox.confirm(
    '确定要取消当前合作吗？这将重新开放互助信息。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const id = Number(route.params.id)
      // 将状态重置为OPEN
      const res = await updateHelpInfo(id, { status: 'OPEN' })
      if (res.data.code === 200) {
        ElMessage.success('已取消合作')
        await helpInfoStore.fetchDetail(id)
        await fetchApplications()
      }
    } catch (e: any) {
      ElMessage.error('操作失败')
    }
  }).catch(() => { })
}

// 确认删除
function confirmDelete() {
  ElMessageBox.confirm(
    '确定要删除该互助信息吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const id = Number(route.params.id)
      const res = await deleteHelpInfo(id)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        router.push('/helpinfo')
      }
    } catch (e: any) {
      ElMessage.error('删除失败')
    }
  }).catch(() => { })
}

// 更新互助信息状态
async function handleStatusUpdate() {
  if (!newStatus.value) {
    ElMessage.warning('请选择状态')
    return
  }

  updateLoading.value = true
  try {
    const id = Number(route.params.id)
    let res

    if (newStatus.value === 'RESOLVED') {
      res = await completeHelpInfo(id)
    } else if (newStatus.value === 'CLOSED') {
      res = await closeHelpInfo(id)
    }

    if (res && res.data.code === 200) {
      ElMessage.success('状态更新成功')
      statusDialogVisible.value = false
      await helpInfoStore.fetchDetail(id)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '状态更新失败')
  } finally {
    updateLoading.value = false
  }
}

function getTypeLabel(type: string) {
  const typeMap: Record<string, string> = {
    'COURSE_TUTORING': '课程辅导',
    'SKILL_EXCHANGE': '技能交换',
    'ITEM_BORROW': '物品借用'
  }
  return typeMap[type] || type
}

function getStatusLabel(status: string) {
  const statusMap: Record<string, string> = {
    'OPEN': '进行中',
    'IN_PROGRESS': '处理中',
    'RESOLVED': '已解决',
    'CLOSED': '已关闭',
    'EXPIRED': '已过期'
  }
  return statusMap[status] || status
}

function getStatusType(status: string) {
  const statusMap: Record<string, string> = {
    'OPEN': 'success',
    'IN_PROGRESS': 'warning',
    'RESOLVED': 'info',
    'CLOSED': '',
    'EXPIRED': 'danger'
  }
  return statusMap[status] || ''
}

function getApplicationStatusLabel(status: string) {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'ACCEPTED': '已接受',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

function getApplicationStatusType(status: string) {
  const statusMap: Record<string, string> = {
    'PENDING': 'warning',
    'ACCEPTED': 'success',
    'REJECTED': 'info'
  }
  return statusMap[status] || ''
}

// 格式化日期
function formatDate(dateString: string | Date) {
  if (!dateString) return '';

  try {
    const date = typeof dateString === 'string' ? new Date(dateString) : dateString;
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (error) {
    return String(dateString);
  }
}
</script>

<style scoped>
.helpinfo-detail-page {
  max-width: 800px;
  margin: 30px auto;
  padding: 0 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.error-container {
  margin: 20px 0;
}

.info-content {
  margin-top: 10px;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.description {
  white-space: pre-line;
  line-height: 1.6;
  margin-top: 5px;
}

.action-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.applications-section,
.progress-section {
  margin-top: 30px;
}

.applications-section h3,
.progress-section h3 {
  margin-bottom: 15px;
  font-size: 1.2rem;
  color: #606266;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.gallery-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  object-fit: cover;
  cursor: pointer;
}
</style>
