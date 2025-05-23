<template>
  <div class="helpinfo-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>{{ info?.title || '互助信息详情' }}</h2>
          <div class="header-actions" v-if="info && isPublisher">
            <el-button-group>
              <el-button size="small" type="primary" :disabled="!canChangeStatus"
                @click="statusDialogVisible = true">修改状态</el-button>
              <el-button size="small" :disabled="!canEdit" @click="handleEdit">编辑</el-button>
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
          <el-descriptions-item label="帮助者" v-if="info.acceptedApplicantNickname">
            <div class="helper-info">
              <span>{{ info.acceptedApplicantNickname }}</span>
              <el-tag size="small" type="success" class="role-tag">帮助方</el-tag>
            </div>
          </el-descriptions-item>
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
            <el-table-column prop="message" label="申请消息" min-width="220">
              <template #default="scope">
                <div class="message-content" v-html="formatMessage(scope.row.message)"></div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getApplicationStatusType(scope.row.status)">
                  {{ getApplicationStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" v-if="info.status === 'OPEN' || info.status === 'IN_PROGRESS'">
              <template #default="scope">
                <div class="action-buttons">
                  <template v-if="scope.row.status === 'PENDING'">
                    <el-button size="small" type="success" @click="handleAcceptApplication(scope.row)">接受</el-button>
                    <el-button size="small" type="danger" @click="handleRejectApplication(scope.row)">拒绝</el-button>
                  </template>
                  <template v-else>
                    <span>-</span>
                  </template>
                </div>
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
        <div class="action-container" v-if="info.status === 'OPEN' && (!authStore.user || !isPublisher)">
          <!-- 错误信息 -->
          <el-alert v-if="!hasToken" title="请先登录" type="warning" show-icon :closable="false"
            style="margin-bottom: 10px" />

          <template v-if="hasApplied && myApplication">
            <el-button-group>
              <template v-if="myApplication.status === 'PENDING'">
                <el-button type="info">申请处理中</el-button>
                <el-button @click="handleCancelApplication">取消申请</el-button>
              </template>
              <template v-else-if="myApplication.status === 'ACCEPTED'">
                <el-button type="success">已被接受</el-button>
              </template>
              <template v-else-if="myApplication.status === 'REJECTED'">
                <el-button type="danger">已被拒绝</el-button>
                <el-button @click="applyDialogVisible = true">重新申请</el-button>
              </template>
              <template v-else-if="myApplication.status === 'CANCELED'">
                <el-button type="info" disabled style="margin-right: 10px;">已取消</el-button>
                <el-button type="primary" @click="applyDialogVisible = true">重新申请</el-button>
              </template>
            </el-button-group>
          </template>
          <template v-else>
            <el-button type="primary" @click="handleApplyClick">申请帮助</el-button>
          </template>
        </div>

        <!-- 提示信息 - 发布者查看自己发布的互助信息时显示 -->
        <div class="info-section" v-if="isPublisher && info.status === 'OPEN'">
          <el-alert title="这是您发布的互助信息" type="info" description="您不能申请自己发布的互助信息，请等待其他用户申请帮助。" show-icon
            :closable="false">
          </el-alert>
        </div>

        <!-- 评价入口：支持双向评价 - 只有发布者或帮助者才能看到 -->
        <div class="review-section animate-enter"
          v-if="(isPublisher || (reviewInfo.helperId === authStore.user?.userId)) && (reviewInfo.showPublisherReview || reviewInfo.showHelperReview || reviewInfo.publisherHasReviewed || reviewInfo.helperHasReviewed)">
          <h3>评价中心</h3>

          <div class="reviewer-info" v-if="info.status === 'RESOLVED' && reviewInfo.helperId">
            <div class="publisher-helper-info">
              <div class="info-row">
                <strong>发布者：</strong> {{ info.publisherName }}
                <el-tag size="small" type="primary" class="role-tag">求助方</el-tag>
              </div>
              <div class="info-row">
                <strong>帮助者：</strong> {{ reviewInfo.helperName || '未知用户' }}
                <el-tag size="small" type="success" class="role-tag">帮助方</el-tag>
              </div>
            </div>
          </div>

          <div class="review-buttons">
            <!-- 发布者评价帮助者 -->
            <el-button v-if="isPublisher && reviewInfo.showPublisherReview" type="primary" @click="openPublisherReview"
              class="review-btn" icon="Star">
              评价帮助者
            </el-button>
            <el-button v-if="isPublisher && reviewInfo.publisherHasReviewed" type="info" disabled class="reviewed-btn"
              icon="Check">
              已评价帮助者
            </el-button>

            <!-- 帮助者评价发布者 -->
            <el-button v-if="!isPublisher && reviewInfo.showHelperReview" type="success" @click="openHelperReview"
              class="review-btn" icon="Star">
              评价发布者
            </el-button>
            <el-button v-if="!isPublisher && reviewInfo.helperHasReviewed" type="info" disabled class="reviewed-btn"
              icon="Check">
              已评价发布者
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 申请帮助对话框 -->
    <ApplyHelpDialog v-if="info" v-model:visible="applyDialogVisible" :help-info-id="Number(route.params.id)"
      @success="handleApplySuccess" />

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

    <!-- 评价对话框 -->
    <ReviewDialog v-if="info && reviewDialogVisible" :visible="reviewDialogVisible" :help-info-id="info.infoId"
      :reviewed-user-id="reviewedUserId || 0" :reviewer-user-id="authStore.user?.userId || 0"
      :review-type="currentReviewType" :title="reviewDialogTitle" @update:visible="reviewDialogVisible = $event"
      @review-submitted="onReviewSubmitted" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useHelpInfoStore } from '../store/helpinfo'
import { useAuthStore } from '../store/auth'
import {
  getApplications,
  acceptApplication,
  rejectApplication,
  completeHelpInfo,
  reopenHelpInfo,
  getMyApplications,
  cancelApplication,
  closeHelpInfo
} from '../api/helpApplication'
import { deleteHelpInfo, incrementHelpInfoViewCount } from '../api/helpinfo'
import { getUserById } from '../api/user'
import ApplyHelpDialog from '../components/ApplyHelpDialog.vue'
import ReviewDialog from '../components/ReviewDialog.vue'
import { getUserReviewStatus } from '../api/review'

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
const hasApplied = ref(false)
const myApplication = ref<any>(null)
const reviewDialogVisible = ref(false)
const reviewedUserId = ref<number | null>(null)
const reviewDialogTitle = ref('提交评价')
const currentReviewType = ref<'PUBLISHER_TO_HELPER' | 'HELPER_TO_PUBLISHER'>('PUBLISHER_TO_HELPER')

// 双向评价状态
const reviewInfo = ref({
  showPublisherReview: false,
  showHelperReview: false,
  publisherHasReviewed: false,
  helperHasReviewed: false,
  helperId: null as number | null,
  helperName: ''
})

// 检查用户是否有token
const hasToken = computed(() => {
  return !!localStorage.getItem('token')
})

// 判断当前用户是否为发布者
const isPublisher = computed(() => {
  if (!info.value || !authStore.user || !authStore.user.userId) return false
  return info.value.publisherId === authStore.user.userId
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
      statuses.push({ label: '不满意', value: 'UNSATISFIED' })
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

    // 先尝试获取用户信息
    const token = localStorage.getItem('token')
    if (token && (!authStore.user || !authStore.user.userId)) {
      try {
        await authStore.fetchCurrentUser()
      } catch {
        console.error('获取当前用户信息失败')
      }
    }

    // 检查当前用户是否已申请
    if (authStore.user && authStore.user.userId) {
      try {
        await checkUserApplication()
      } catch {
        console.error('检查用户申请状态失败')
      }
    }

    // 增加浏览量
    try {
      await incrementHelpInfoViewCount(id)
    } catch (e) {
      console.error('增加浏览量失败', e)
    }

    // 加载评价状态
    await loadReviewStatus()
  } catch (e: any) {
    error.value = e.message || '获取详情失败'
  }
})

// 监听info变化，重新检查用户申请状态
watch(() => info.value, async (newInfo) => {
  if (newInfo && !isPublisher.value) {
    await checkUserApplication()
  }
}, { deep: true })

// 监听info变化和用户变化，自动检查评价状态
watch([info, () => authStore.user?.userId], () => {
  loadReviewStatus()
})

// 获取申请列表
async function fetchApplications() {
  if (!isPublisher.value && info.value?.status !== 'IN_PROGRESS') return;

  try {
    const id = Number(route.params.id);
    const res = await getApplications(id);
    if (res.data.code === 200) {
      let apps = res.data.data || [];
      // 如果是发布者，或者互助信息在进行中，则需要获取申请人昵称
      if (isPublisher.value || info.value?.status === 'IN_PROGRESS') {
        const userPromises = apps.map(async (app: any) => {
          if (app.applicantId && !app.applicantNickname) {
            try {
              const userRes = await getUserById(app.applicantId);
              if (userRes.data.code === 200 && userRes.data.data) {
                app.applicantNickname = userRes.data.data.nickname;
                app.applicantAvatar = userRes.data.data.avatarUrl; // 同时获取头像
              }
            } catch (e) {
              console.error(`获取用户 ${app.applicantId} 信息失败:`, e);
              app.applicantNickname = '未知用户';
            }
          }
          return app;
        });
        applications.value = await Promise.all(userPromises);
      } else {
        applications.value = apps;
      }
    }
  } catch (e: any) {
    ElMessage.error('获取申请列表失败');
  }
}

// 接受申请
async function handleAcceptApplication(application: any) {
  try {
    const id = Number(route.params.id)
    // 兼容 application.id/application.applicationId
    const applicationId = application.applicationId || application.id
    if (!applicationId) {
      ElMessage.error('申请ID无效')
      return
    }
    const res = await acceptApplication(id, applicationId)
    if (res.data.code === 200) {
      ElMessage.success('已接受申请')
      await helpInfoStore.fetchDetail(id) // 刷新互助信息状态
      await fetchApplications() // 刷新申请列表
      // 如果接受成功，并且当前用户是发布者，则重新检查用户（自己）的申请状态，以更新视图
      if (isPublisher.value) {
        await checkUserApplication();
      }
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 拒绝申请
async function handleRejectApplication(application: any) {
  try {
    const id = Number(route.params.id)
    // 兼容 application.id/application.applicationId
    const applicationId = application.applicationId || application.id
    if (!applicationId) {
      ElMessage.error('申请ID无效')
      return
    }
    const res = await rejectApplication(id, applicationId)
    if (res.data.code === 200) {
      ElMessage.success('已拒绝申请')
      await fetchApplications() // 刷新申请列表
      // 如果拒绝成功，并且当前用户是发布者，则重新检查用户（自己）的申请状态
      if (isPublisher.value) {
        await checkUserApplication();
      }
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
      ElMessage.success('已标记为已解决')
      await helpInfoStore.fetchDetail(id) // 刷新互助信息状态
      await fetchApplications() // 刷新申请列表
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 确认取消合作
async function confirmCancel() {
  ElMessageBox.confirm(
    '确定要取消与当前帮助者的合作吗？取消后，该互助信息将重新开放接受申请，原帮助者的申请状态将变为已拒绝。',
    '确认取消',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const id = Number(route.params.id)
        const currentAcceptedApp = acceptedApplication.value // Store before state changes

        // 1. 重新开放互助信息
        const reopenRes = await reopenHelpInfo(id)
        if (reopenRes.data.code === 200) {
          ElMessage.success('已取消合作，互助信息重新开放')
          // 重新拉取互助信息详情，确保 acceptedApplicationId 变为 null
          await helpInfoStore.fetchDetail(id)
          // 不直接赋值 info.value，依赖响应式

          // 2. 将原接受的申请标记为已拒绝
          if (currentAcceptedApp && (currentAcceptedApp.applicationId || currentAcceptedApp.id)) {
            const originalAcceptedAppId = currentAcceptedApp.applicationId || currentAcceptedApp.id
            try {
              const rejectRes = await rejectApplication(id, originalAcceptedAppId)
              if (rejectRes.data.code === 200) {
                ElMessage.info('原帮助者的申请已标记为已拒绝。')
              } else {
                ElMessage.error(rejectRes.data.message || '更新原帮助者申请状态失败。')
              }
            } catch (rejectError: any) {
              ElMessage.error(rejectError.message || '更新原帮助者申请状态时出错。')
            }
          }
          await fetchApplications() // 重新获取申请列表以更新视图
          if (!isPublisher.value) {
            await checkUserApplication();
          }

        } else {
          ElMessage.error(reopenRes.data.message || '取消合作失败')
        }
      } catch (e: any) {
        ElMessage.error(e.message || '取消合作失败')
      }
    })
    .catch(() => {
      // 用户取消操作
      ElMessage.info('已取消操作')
    })
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

// 处理编辑按钮点击
function handleEdit() {
  if (!info.value) return

  // 检查状态，只有当状态为OPEN时才能编辑
  if (info.value.status !== 'OPEN') {
    ElMessage.warning('只有处于"进行中"状态的互助信息才能被编辑')
    return
  }

  const id = Number(route.params.id)
  // 跳转到编辑页面，将当前互助信息ID传递过去
  router.push(`/helpinfo/edit/${id}`)
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

// 格式化消息，将消息中的联系方式部分高亮显示
function formatMessage(message: string): string {
  if (!message) return ''

  // 如果消息中包含联系方式,使用HTML格式化显示
  const contactInfoMatch = message.match(/联系方式[:：](.+)$/m)
  if (contactInfoMatch) {
    const mainMessage = message.replace(/联系方式[:：](.+)$/m, '')
    return mainMessage + '<div class="contact-info">联系方式: ' + contactInfoMatch[1].trim() + '</div>'
  }

  return message.replace(/\n/g, '<br>')
}

// 检查用户的申请状态
async function checkUserApplication() {
  // 重置状态
  hasApplied.value = false
  myApplication.value = null

  // 检查token和用户信息
  if (hasToken.value) {
    if (!authStore.user || !authStore.user.userId) {
      try {
        await authStore.fetchCurrentUser()
      } catch {
        console.error('获取当前用户信息失败')
        return
      }
    }
  } else {
    return
  }

  if (!authStore.user || !authStore.user.userId) {
    return
  }

  try {
    const res = await getMyApplications()

    if (res.data.code === 200) {
      const apps = res.data.data || []
      const currentInfoId = Number(route.params.id)

      // 筛选出针对当前互助信息的所有申请
      const userAppsForCurrentInfo = apps.filter((app: any) => app.infoId === currentInfoId || app.infoId === Number(currentInfoId))

      if (userAppsForCurrentInfo.length > 0) {
        // 定义状态优先级
        const statusPriority: Record<string, number> = {
          'ACCEPTED': 1,
          'PENDING': 2,
          'REJECTED': 3,
          'CANCELED': 4
        };

        // 根据优先级排序申请，优先级数字越小越高
        userAppsForCurrentInfo.sort((a: any, b: any) => {
          const priorityA = statusPriority[a.status] || 99;
          const priorityB = statusPriority[b.status] || 99;
          return priorityA - priorityB;
        });

        myApplication.value = userAppsForCurrentInfo[0]; // 选择优先级最高的申请
        hasApplied.value = true;
      } else {
        hasApplied.value = false
        myApplication.value = null
      }
    }
  } catch (e) {
    hasApplied.value = false
    myApplication.value = null
    console.error('获取申请信息失败', e)
  }
}

// 加载评价状态
async function loadReviewStatus() {
  if (!info.value || !authStore.user?.userId) {
    return
  }

  try {
    const helpInfoId = info.value.infoId
    const userId = authStore.user.userId

    const res = await getUserReviewStatus(userId, helpInfoId)

    if (res.data && res.data.canPublisherReview !== undefined) {
      const statusData = res.data

      // 获取互助信息中的帮助者信息
      let helperId = null
      let helperName = ''

      // 优先：如果是已解决/不满意，且 applications 有 ACCEPTED
      if (info.value.status === 'RESOLVED' || info.value.status === 'UNSATISFIED') {
        const acceptedApp = applications.value.find(app => app.status === 'ACCEPTED')
        if (acceptedApp) {
          helperId = acceptedApp.applicantId
          helperName = acceptedApp.applicantNickname || '未知用户'
        }
      }
      // 补充：如果当前用户就是帮助者（即 acceptedApplicationId 对应的申请人）
      if (!helperId && info.value.acceptedApplicationId && info.value.acceptedApplicantNickname) {
        // acceptedApplicantNickname 一定有，acceptedApplicationId 也有
        // 只要当前用户是帮助者，就赋值
        if (info.value.acceptedApplicantNickname && userId) {
          helperId = userId
          helperName = info.value.acceptedApplicantNickname
        }
      }

      reviewInfo.value = {
        showPublisherReview: statusData.canPublisherReview || false,
        showHelperReview: statusData.canHelperReview || false,
        publisherHasReviewed: statusData.publisherHasReviewed || false,
        helperHasReviewed: statusData.helperHasReviewed || false,
        helperId: helperId,
        helperName: helperName || ''
      }
    }
  } catch (e) {
    console.error('获取评价状态失败:', e)
  }
}

// 打开发布者评价对话框
function openPublisherReview() {
  if (!info.value || !authStore.user?.userId) return

  currentReviewType.value = 'PUBLISHER_TO_HELPER'
  reviewedUserId.value = reviewInfo.value.helperId
  reviewDialogTitle.value = '评价帮助者'
  reviewDialogVisible.value = true
}

// 打开帮助者评价对话框
function openHelperReview() {
  if (!info.value || !authStore.user?.userId) return

  currentReviewType.value = 'HELPER_TO_PUBLISHER'
  reviewedUserId.value = info.value.publisherId
  reviewDialogTitle.value = '评价发布者'
  reviewDialogVisible.value = true
}

// 评价提交后刷新状态
function onReviewSubmitted() {
  reviewDialogVisible.value = false
  ElMessage.success('评价成功！')

  // 根据当前评价类型更新状态
  if (currentReviewType.value === 'PUBLISHER_TO_HELPER') {
    reviewInfo.value.publisherHasReviewed = true
  } else {
    reviewInfo.value.helperHasReviewed = true
  }

  // 重新获取评价状态
  loadReviewStatus()
}

// 处理取消申请
async function handleCancelApplication() {
  if (!myApplication.value) {
    ElMessage.error('无法找到您的申请信息')
    return
  }

  // 根据实体定义，applicationId 是 id 或 applicationId
  const applicationId = myApplication.value.applicationId || myApplication.value.id

  if (!applicationId) {
    ElMessage.error('无法获取申请ID')
    console.error('申请对象缺少ID:', myApplication.value)
    return
  }

  ElMessageBox.confirm(
    '确定要取消申请吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await cancelApplication(applicationId)
      if (res.data.code === 200) {
        ElMessage.success('申请已取消')
        hasApplied.value = false
        myApplication.value = null

        // 重新获取页面数据
        await checkUserApplication()
      } else {
        ElMessage.error(res.data.message || '取消申请失败')
      }
    } catch (e: any) {
      ElMessage.error(e.message || '取消申请失败')
    }
  }).catch(() => { })
}

// 申请成功后的处理
async function handleApplySuccess() {
  applyDialogVisible.value = false
  ElMessage.success('申请已提交')
  // 重新检查用户申请状态以更新按钮显示
  await checkUserApplication()
}

// 获取申请状态标签
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
    'UNSATISFIED': '不满意',
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
    'UNSATISFIED': 'danger',
    'CLOSED': '',
    'EXPIRED': 'danger'
  }
  return statusMap[status] || ''
}

function getApplicationStatusLabel(status: string) {
  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'ACCEPTED': '已接受',
    'REJECTED': '已拒绝',
    'CANCELED': '已取消'
  }
  return statusMap[status] || status
}

function getApplicationStatusType(status: string) {
  const statusMap: Record<string, string> = {
    'PENDING': 'warning',
    'ACCEPTED': 'success',
    'REJECTED': 'info',
    'CANCELED': 'info'
  }
  return statusMap[status] || ''
}

// 格式化日期
function formatDate(dateString: string | Date | number) {
  if (!dateString) return '';

  try {
    // 处理数字类型的时间戳（毫秒）
    const date = typeof dateString === 'number' ? new Date(dateString) :
      (typeof dateString === 'string' ? new Date(dateString) : dateString);

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

// 处理申请按钮点击
function handleApplyClick() {
  // 判断用户是否登录
  if (!hasToken.value) {
    ElMessage.warning('请先登录后再申请')
    router.push('/login') // 跳转到登录页面
    return
  }

  // 尝试获取用户信息
  if (!authStore.user || !authStore.user.userId) {
    authStore.fetchCurrentUser().then(() => {
      // 成功获取用户信息后显示申请对话框
      applyDialogVisible.value = true
    }).catch(() => {
      ElMessage.error('获取用户信息失败，请重新登录')
    })
  } else {
    // 已有用户信息，直接显示申请对话框
    applyDialogVisible.value = true
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

.message-content {
  white-space: pre-line;
  line-height: 1.5;
  color: #606266;
}

.contact-info {
  margin-top: 5px;
  padding: 4px 8px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
  font-size: 0.9em;
  display: inline-block;
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

.info-section {
  margin-top: 20px;
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

/* 评价中心相关样式 */
.review-section {
  margin-top: 30px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f9f9f9;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.review-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 1.2rem;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-section h3::before {
  content: "★";
  color: #FFC107;
  font-size: 1.1rem;
}

.reviewer-info {
  margin-bottom: 20px;
  padding: 15px;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid #ebeef5;
}

.publisher-helper-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.role-tag {
  font-size: 0.75rem;
  padding: 0 8px;
  height: 20px;
  line-height: 18px;
  transform: scale(0.9);
  transform-origin: left center;
  margin-left: 4px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  animation: fadeInRight 0.5s ease;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(10px) scale(0.9);
  }

  to {
    opacity: 1;
    transform: translateX(0) scale(0.9);
  }
}

.review-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 15px;
}

.review-btn,
.reviewed-btn {
  padding: 8px 16px;
  font-size: 0.9rem;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.review-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.review-btn:hover::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 120%;
  height: 120%;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  animation: ripple 0.6s ease-out;
}

@keyframes ripple {
  to {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0;
  }
}

@media screen and (max-width: 576px) {
  .review-buttons {
    flex-direction: column;
    gap: 10px;
  }

  .reviewer-info {
    padding: 10px;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>
