<template>
  <div class="mobile-action-sheet" :class="{ visible: visible }">
    <!-- 遮罩层 -->
    <div class="action-sheet-overlay" @click="handleClose" v-if="visible"></div>
    
    <!-- 操作面板 -->
    <transition name="slide-up">
      <div v-if="visible" class="action-sheet-panel">
        <!-- 头部 -->
        <div class="action-sheet-header" v-if="title">
          <h3 class="action-sheet-title">{{ title }}</h3>
          <button class="close-btn" @click="handleClose">
            <el-icon><Close /></el-icon>
          </button>
        </div>
        
        <!-- 描述 -->
        <div class="action-sheet-description" v-if="description">
          {{ description }}
        </div>
        
        <!-- 操作项列表 -->
        <div class="action-sheet-content">
          <div
            v-for="(action, index) in actions"
            :key="index"
            class="action-item"
            :class="{
              'danger': action.type === 'danger',
              'primary': action.type === 'primary',
              'disabled': action.disabled
            }"
            @click="handleActionClick(action)"
          >
            <div class="action-icon" v-if="action.icon">
              <el-icon>
                <component :is="action.icon" />
              </el-icon>
            </div>
            <div class="action-content">
              <div class="action-title">{{ action.title }}</div>
              <div class="action-subtitle" v-if="action.subtitle">{{ action.subtitle }}</div>
            </div>
            <div class="action-arrow" v-if="action.arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
        
        <!-- 取消按钮 -->
        <div class="action-sheet-cancel" v-if="showCancel">
          <button class="cancel-btn" @click="handleClose">
            {{ cancelText }}
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { Close, ArrowRight } from '@element-plus/icons-vue'

export interface ActionItem {
  title: string
  subtitle?: string
  icon?: any
  type?: 'default' | 'primary' | 'danger'
  disabled?: boolean
  arrow?: boolean
  handler?: () => void | Promise<void>
}

interface Props {
  visible: boolean
  title?: string
  description?: string
  actions: ActionItem[]
  showCancel?: boolean
  cancelText?: string
  closeOnOverlay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showCancel: true,
  cancelText: '取消',
  closeOnOverlay: true
})

const emit = defineEmits<{
  close: []
  action: [action: ActionItem, index: number]
}>()

const handleClose = () => {
  emit('close')
}

const handleActionClick = async (action: ActionItem) => {
  if (action.disabled) return
  
  try {
    if (action.handler) {
      await action.handler()
    }
    
    const index = props.actions.indexOf(action)
    emit('action', action, index)
    
    // 执行操作后自动关闭
    handleClose()
  } catch (error) {
    console.error('Action execution failed:', error)
  }
}
</script>

<style scoped>
.mobile-action-sheet {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  pointer-events: none;
}

.mobile-action-sheet.visible {
  pointer-events: auto;
}

.action-sheet-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.action-sheet-panel {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: var(--card-bg);
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.action-sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 20px 12px;
  border-bottom: 1px solid var(--border-lighter);
}

.action-sheet-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--background-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: var(--hover-bg);
  color: var(--text-primary);
}

.action-sheet-description {
  padding: 12px 20px;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
  border-bottom: 1px solid var(--border-lighter);
}

.action-sheet-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid var(--border-lighter);
}

.action-item:last-child {
  border-bottom: none;
}

.action-item:hover {
  background-color: var(--hover-bg);
}

.action-item:active {
  background-color: var(--primary-light);
  transform: scale(0.98);
}

.action-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-item.disabled:hover {
  background-color: transparent;
}

.action-item.disabled:active {
  transform: none;
}

.action-item.primary {
  color: var(--primary-color);
}

.action-item.primary .action-icon {
  color: var(--primary-color);
}

.action-item.danger {
  color: var(--danger-color);
}

.action-item.danger .action-icon {
  color: var(--danger-color);
}

.action-icon {
  width: 24px;
  height: 24px;
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--text-secondary);
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 500;
  color: inherit;
  margin-bottom: 2px;
}

.action-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.3;
}

.action-arrow {
  width: 16px;
  height: 16px;
  color: var(--text-placeholder);
  font-size: 14px;
}

.action-sheet-cancel {
  padding: 12px 20px 20px;
  border-top: 1px solid var(--border-lighter);
}

.cancel-btn {
  width: 100%;
  height: 50px;
  border: none;
  background: var(--background-color);
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: var(--hover-bg);
}

.cancel-btn:active {
  transform: scale(0.98);
}

/* 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.slide-up-enter-from {
  transform: translateY(100%);
}

.slide-up-leave-to {
  transform: translateY(100%);
}

/* 安全区域适配 */
@supports (padding: max(0px)) {
  .action-sheet-panel {
    padding-bottom: max(20px, env(safe-area-inset-bottom));
  }
}

/* 横屏适配 */
@media (orientation: landscape) and (max-height: 600px) {
  .action-sheet-panel {
    max-height: 90vh;
    border-radius: 12px;
    margin: 20px;
    bottom: auto;
    top: 50%;
    transform: translateY(-50%);
  }
  
  .slide-up-enter-from,
  .slide-up-leave-to {
    transform: translateY(-50%) scale(0.9);
    opacity: 0;
  }
}

/* 暗色主题适配 */
.dark-theme .action-sheet-overlay {
  background-color: rgba(0, 0, 0, 0.7);
}

/* 大屏幕适配 - 居中显示 */
@media (min-width: 768px) {
  .action-sheet-panel {
    max-width: 400px;
    margin: 0 auto;
    bottom: 60px;
    border-radius: 16px;
  }
  
  .slide-up-enter-from,
  .slide-up-leave-to {
    transform: translateY(20px);
    opacity: 0;
  }
}

/* 减弱动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .slide-up-enter-active,
  .slide-up-leave-active {
    transition: opacity 0.2s ease;
  }
  
  .slide-up-enter-from,
  .slide-up-leave-to {
    transform: none;
    opacity: 0;
  }
  
  .action-item:active {
    transform: none;
  }
  
  .cancel-btn:active {
    transform: none;
  }
}
</style>
