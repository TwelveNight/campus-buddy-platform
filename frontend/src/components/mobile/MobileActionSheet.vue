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

/* 创意增强效果 */

/* 遮罩层的动态粒子效果 */
.action-sheet-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0.6));
  backdrop-filter: blur(8px);
  transition: opacity 0.3s ease;
  position: relative;
}

.action-sheet-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(103, 194, 58, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(230, 162, 60, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 90% 70%, rgba(245, 108, 108, 0.1) 0%, transparent 50%);
  animation: overlay-particles 6s ease-in-out infinite;
  pointer-events: none;
}

@keyframes overlay-particles {
  0%, 100% { 
    opacity: 0.3;
    transform: scale(1) rotate(0deg);
  }
  25% { 
    opacity: 0.6;
    transform: scale(1.1) rotate(2deg);
  }
  50% { 
    opacity: 0.4;
    transform: scale(0.9) rotate(1deg);
  }
  75% { 
    opacity: 0.7;
    transform: scale(1.05) rotate(-1deg);
  }
}

/* 面板的磁力吸附效果 */
.action-sheet-panel {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
  border-radius: 20px 20px 0 0;
  box-shadow: 
    0 -8px 32px rgba(0, 0, 0, 0.12),
    0 -2px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
}

.action-sheet-panel::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 0.03) 0%,
    transparent 30%,
    rgba(103, 194, 58, 0.03) 70%,
    transparent 100%);
  pointer-events: none;
  border-radius: 20px 20px 0 0;
}

/* 顶部拖拽指示器的动画增强 */
.action-sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  position: relative;
}

.action-sheet-header::before {
  content: '';
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  width: 36px;
  height: 4px;
  background: rgba(0, 0, 0, 0.15);
  border-radius: 2px;
  animation: handle-breathe 3s ease-in-out infinite;
}

@keyframes handle-breathe {
  0%, 100% { 
    width: 36px;
    opacity: 0.3;
  }
  50% { 
    width: 48px;
    opacity: 0.6;
  }
}

/* 标题的渐变动画 */
.action-sheet-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  background: linear-gradient(135deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: title-gradient 4s ease-in-out infinite;
}

@keyframes title-gradient {
  0%, 100% {
    background: linear-gradient(135deg, #409eff, #67c23a);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  50% {
    background: linear-gradient(135deg, #67c23a, #e6a23c);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  backdrop-filter: blur(10px);
}

.close-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  color: var(--primary-color);
  transform: scale(1.1);
}

.close-btn:active {
  transform: scale(0.95);
}

.action-sheet-description {
  padding: 16px 24px;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(0, 0, 0, 0.02);
}

.action-sheet-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  position: relative;
}

.action-sheet-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(64, 158, 255, 0.3), 
    transparent);
}

.action-sheet-content::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(103, 194, 58, 0.3), 
    transparent);
}

.action-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.action-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.1), transparent);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.action-item::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.2), transparent);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.4s ease;
  pointer-events: none;
}

.action-item:active::after {
  width: 300px;
  height: 300px;
  opacity: 0;
}

.action-item:last-child {
  border-bottom: none;
}

.action-item:hover {
  background: rgba(64, 158, 255, 0.05);
  transform: translateX(4px);
}

.action-item:hover::before {
  transform: translateX(100%);
}

.action-item:active {
  background: rgba(64, 158, 255, 0.1);
  transform: translateX(2px) scale(0.98);
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
  position: relative;
}

.action-item.primary::before {
  background: linear-gradient(90deg, 
    rgba(64, 158, 255, 0.1), 
    rgba(64, 158, 255, 0.2));
}

.action-item.primary .action-icon {
  color: var(--primary-color);
}

.action-item.danger {
  color: var(--danger-color);
  position: relative;
}

.action-item.danger::before {
  background: linear-gradient(90deg, 
    rgba(245, 108, 108, 0.1), 
    rgba(245, 108, 108, 0.2));
}

.action-item.danger .action-icon {
  color: var(--danger-color);
}

.action-icon {
  width: 28px;
  height: 28px;
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.action-icon::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, 
    rgba(64, 158, 255, 0.3),
    rgba(103, 194, 58, 0.3),
    rgba(230, 162, 60, 0.3));
  border-radius: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.action-item:hover .action-icon::before {
  opacity: 1;
  animation: icon-pulse 1s ease-in-out infinite;
}

@keyframes icon-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.action-item:hover .action-icon {
  background: rgba(64, 158, 255, 0.15);
  color: var(--primary-color);
  transform: scale(1.1);
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 500;
  color: inherit;
  margin-bottom: 2px;
  transition: color 0.3s ease;
}

.action-item:hover .action-title {
  color: var(--primary-color);
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
  padding: 16px 24px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(0, 0, 0, 0.02);
}

.cancel-btn {
  width: 100%;
  height: 52px;
  border: none;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.05), rgba(0, 0, 0, 0.08));
  border-radius: 16px;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.cancel-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.3), 
    transparent);
  transition: left 0.6s ease;
}

.cancel-btn:hover::before {
  left: 100%;
}

.cancel-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(0, 0, 0, 0.1), transparent);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.3s ease;
}

.cancel-btn:active::after {
  width: 200px;
  height: 200px;
  opacity: 0;
}

/* 动画 */
.slide-up-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.55, 0, 0.1, 1);
}

.slide-up-enter-from {
  transform: translateY(100%) scale(0.9);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(100%) scale(0.95);
  opacity: 0;
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
.dark-theme .action-sheet-panel {
  background: linear-gradient(135deg, 
    rgba(30, 30, 30, 0.95) 0%, 
    rgba(40, 40, 40, 0.9) 100%);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-theme .action-sheet-overlay {
  background-color: rgba(0, 0, 0, 0.7);
}

.dark-theme .action-sheet-overlay::before {
  background: 
    radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(103, 194, 58, 0.2) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(230, 162, 60, 0.2) 0%, transparent 50%);
}

.dark-theme .action-item {
  background: rgba(255, 255, 255, 0.03);
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.dark-theme .action-item:hover {
  background: rgba(64, 158, 255, 0.1);
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
  .action-sheet-overlay::before,
  .action-sheet-header::before,
  .action-sheet-title {
    animation: none;
  }
  
  .action-item:hover .action-icon::before {
    animation: none;
  }
  
  .slide-up-enter-active,
  .slide-up-leave-active {
    transition: transform 0.2s ease, opacity 0.2s ease;
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

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .action-item:hover {
    transform: translateX(2px) scale(1.01);
  }
  
  .action-item:hover .action-icon {
    transform: scale(1.05);
  }
}
</style>
