<template>
  <div class="mobile-help-card" :class="{ featured: helpInfo.featured }">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-content">
        <h3 class="help-title">{{ helpInfo.title }}</h3>
        <div class="help-type">
          <el-tag 
            :type="getTypeTagType(helpInfo.type)" 
            size="small"
            round
          >
            {{ getTypeText(helpInfo.type) }}
          </el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-button 
          circle 
          size="small" 
          @click="toggleFavorite"
          :type="isFavorited ? 'danger' : 'default'"
          class="favorite-btn"
        >
          <el-icon>
            <StarFilled v-if="isFavorited" />
            <Star v-else />
          </el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 卡片内容 -->
    <div class="card-content">
      <!-- 描述 -->
      <div class="help-description" v-if="helpInfo.description">
        <p class="description-text">{{ truncatedDescription }}</p>
        <button 
          v-if="helpInfo.description.length > maxDescriptionLength" 
          @click="toggleExpanded"
          class="expand-btn"
        >
          {{ isExpanded ? '收起' : '展开' }}
        </button>
      </div>
      
      <!-- 元信息 -->
      <div class="help-meta">
        <div class="meta-item">
          <el-icon><User /></el-icon>
          <span>{{ helpInfo.publisherName }}</span>
        </div>
        <div class="meta-item">
          <el-icon><Clock /></el-icon>
          <span>{{ formatTime(helpInfo.publishTime) }}</span>
        </div>
        <div class="meta-item" v-if="helpInfo.location">
          <el-icon><LocationFilled /></el-icon>
          <span>{{ helpInfo.location }}</span>
        </div>
        <div class="meta-item" v-if="helpInfo.reward">
          <el-icon><Money /></el-icon>
          <span>{{ helpInfo.reward }}积分</span>
        </div>
      </div>
      
      <!-- 标签 -->
      <div class="help-tags" v-if="helpInfo.tags && helpInfo.tags.length > 0">
        <el-tag
          v-for="tag in helpInfo.tags.slice(0, 3)"
          :key="tag"
          size="small"
          class="tag-item"
        >
          {{ tag }}
        </el-tag>
        <span v-if="helpInfo.tags.length > 3" class="more-tags">
          +{{ helpInfo.tags.length - 3 }}
        </span>
      </div>
    </div>
    
    <!-- 卡片底部操作 -->
    <div class="card-actions">
      <div class="action-info">
        <div class="status-info">
          <el-tag
            :type="getStatusTagType(helpInfo.status)"
            size="small"
            round
          >
            {{ getStatusText(helpInfo.status) }}
          </el-tag>
        </div>
        <div class="apply-count" v-if="helpInfo.applicationCount > 0">
          <el-icon><User /></el-icon>
          <span>{{ helpInfo.applicationCount }}人申请</span>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button
          size="small"
          @click="viewDetail"
          class="detail-btn"
        >
          查看详情
        </el-button>
        <el-button
          v-if="canApply"
          type="primary"
          size="small"
          @click="applyHelp"
          :loading="applying"
          class="apply-btn"
        >
          {{ applying ? '申请中...' : '我要帮助' }}
        </el-button>
      </div>
    </div>
    
    <!-- 紧急标识 */
    <div v-if="helpInfo.urgent" class="urgent-badge">
      <el-icon><Warning /></el-icon>
      <span>紧急</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Star,
  StarFilled,
  User,
  Clock,
  LocationFilled,
  Money,
  Warning
} from '@element-plus/icons-vue'

interface HelpInfo {
  id: number
  title: string
  description?: string
  type: string
  status: string
  publisherName: string
  publishTime: string
  location?: string
  reward?: number
  tags?: string[]
  urgent?: boolean
  featured?: boolean
  applicationCount?: number
}

interface Props {
  helpInfo: HelpInfo
  maxDescriptionLength?: number
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  maxDescriptionLength: 100,
  showActions: true
})

const emit = defineEmits<{
  apply: [helpInfo: HelpInfo]
  favorite: [helpInfo: HelpInfo, favorited: boolean]
  view: [helpInfo: HelpInfo]
}>()

const router = useRouter()

const isExpanded = ref(false)
const isFavorited = ref(false)
const applying = ref(false)

// 计算属性
const truncatedDescription = computed(() => {
  if (!props.helpInfo.description) return ''
  
  if (isExpanded.value || props.helpInfo.description.length <= props.maxDescriptionLength) {
    return props.helpInfo.description
  }
  
  return props.helpInfo.description.substring(0, props.maxDescriptionLength) + '...'
})

const canApply = computed(() => {
  return props.helpInfo.status === 'published' && props.showActions
})

// 方法
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

const toggleFavorite = () => {
  isFavorited.value = !isFavorited.value
  emit('favorite', props.helpInfo, isFavorited.value)
  
  ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
}

const viewDetail = () => {
  emit('view', props.helpInfo)
  router.push(`/help-info/${props.helpInfo.id}`)
}

const applyHelp = async () => {
  if (applying.value) return
  
  applying.value = true
  try {
    emit('apply', props.helpInfo)
    ElMessage.success('申请成功')
  } catch (error) {
    ElMessage.error('申请失败')
  } finally {
    applying.value = false
  }
}

const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    study: '学习辅导',
    life: '生活帮助',
    activity: '活动互助',
    other: '其他'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    study: 'primary',
    life: 'success',
    activity: 'warning',
    other: 'info'
  }
  return typeMap[type] || 'default'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    published: '进行中',
    completed: '已完成',
    cancelled: '已取消',
    expired: '已过期'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    published: 'success',
    completed: 'info',
    cancelled: 'danger',
    expired: 'warning'
  }
  return statusMap[status] || 'default'
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString()
}
</script>

<style scoped>
.mobile-help-card {
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  margin-bottom: 16px;
  overflow: hidden;
  position: relative;
  transition: all 0.3s ease;
  border: 1px solid var(--border-lighter);
}

.mobile-help-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-regular);
}

.mobile-help-card.featured {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.card-header {
  padding: 16px 16px 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.header-content {
  flex: 1;
  min-width: 0;
}

.help-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
  line-height: 1.4;
  word-break: break-word;
}

.help-type {
  display: inline-block;
}

.header-actions {
  flex-shrink: 0;
}

.favorite-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-light);
}

.card-content {
  padding: 0 16px;
}

.help-description {
  margin-bottom: 16px;
}

.description-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-regular);
  margin: 0 0 8px;
  word-break: break-word;
}

.expand-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
}

.help-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-item .el-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.help-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.tag-item {
  font-size: 12px;
  border-radius: 12px;
}

.more-tags {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--background-color);
  padding: 2px 8px;
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.card-actions {
  padding: 12px 16px 16px;
  background: var(--background-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.action-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-info {
  display: flex;
  align-items: center;
}

.apply-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.apply-count .el-icon {
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.detail-btn,
.apply-btn {
  height: 32px;
  padding: 0 16px;
  font-size: 14px;
  border-radius: 6px;
}

.detail-btn {
  border-color: var(--border-color);
  color: var(--text-regular);
}

.apply-btn {
  min-width: 80px;
}

.urgent-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--danger-color);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 2px;
  z-index: 1;
}

.urgent-badge .el-icon {
  font-size: 12px;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .mobile-help-card {
    margin-bottom: 12px;
    border-radius: 10px;
  }
  
  .card-header {
    padding: 14px 14px 10px;
  }
  
  .help-title {
    font-size: 15px;
  }
  
  .card-content {
    padding: 0 14px;
  }
  
  .help-meta {
    gap: 10px;
    margin-bottom: 14px;
  }
  
  .meta-item {
    font-size: 12px;
  }
  
  .card-actions {
    padding: 10px 14px 14px;
    gap: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 6px;
  }
  
  .detail-btn,
  .apply-btn {
    width: 100%;
    min-width: auto;
  }
}

/* 暗色主题适配 */
.dark-theme .mobile-help-card {
  background: var(--card-bg);
  border-color: var(--border-color);
}

.dark-theme .mobile-help-card.featured {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.dark-theme .card-actions {
  background: var(--background-color);
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .mobile-help-card:hover {
    transform: none;
    box-shadow: var(--shadow-light);
  }
  
  .favorite-btn,
  .detail-btn,
  .apply-btn {
    min-height: 44px;
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .mobile-help-card {
    border-width: 2px;
  }
  
  .urgent-badge {
    border: 2px solid white;
  }
}
</style>
