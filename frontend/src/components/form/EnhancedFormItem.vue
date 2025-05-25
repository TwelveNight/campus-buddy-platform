<template>
  <div class="enhanced-form-item" :class="{ 'has-error': hasError, 'is-focused': isFocused }">
    <label v-if="label" :for="inputId" class="form-label" :class="{ required: isRequired }">
      {{ label }}
      <span v-if="tooltip" class="label-tooltip">
        <el-tooltip :content="tooltip" placement="top">
          <el-icon><QuestionFilled /></el-icon>
        </el-tooltip>
      </span>
    </label>
    
    <div class="form-input-wrapper">
      <slot 
        :input-id="inputId"
        :on-focus="handleFocus"
        :on-blur="handleBlur"
        :on-input="handleInput"
        :error="error"
        :has-error="hasError"
      />
      
      <!-- 错误状态图标 -->
      <transition name="error-icon">
        <el-icon v-if="hasError && showErrorIcon" class="error-icon">
          <CircleCloseFilled />
        </el-icon>
      </transition>
      
      <!-- 成功状态图标 -->
      <transition name="success-icon">
        <el-icon v-if="showSuccessIcon && !hasError && value" class="success-icon">
          <CircleCheckFilled />
        </el-icon>
      </transition>
      
      <!-- 加载状态 -->
      <transition name="loading">
        <el-icon v-if="loading" class="loading-icon">
          <Loading />
        </el-icon>
      </transition>
    </div>
    
    <!-- 错误消息 -->
    <transition name="error-message">
      <div v-if="hasError" class="error-message">
        <el-icon><WarningFilled /></el-icon>
        <span>{{ error }}</span>
      </div>
    </transition>
    
    <!-- 帮助文本 -->
    <div v-if="helpText && !hasError" class="help-text">
      {{ helpText }}
    </div>
    
    <!-- 字符计数 */
    <div v-if="showCharCount && maxLength" class="char-count" :class="{ 'near-limit': isNearLimit }">
      {{ charCount }} / {{ maxLength }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { QuestionFilled, CircleCloseFilled, CircleCheckFilled, Loading, WarningFilled } from '@element-plus/icons-vue'

interface Props {
  label?: string
  tooltip?: string
  error?: string
  helpText?: string
  value?: string | number
  required?: boolean
  loading?: boolean
  showErrorIcon?: boolean
  showSuccessIcon?: boolean
  showCharCount?: boolean
  maxLength?: number
  autoFocus?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showErrorIcon: true,
  showSuccessIcon: true,
  showCharCount: false,
  autoFocus: false
})

const emit = defineEmits<{
  focus: []
  blur: []
  input: [value: string | number]
}>()

const isFocused = ref(false)
const inputId = ref(`form-input-${Math.random().toString(36).substr(2, 9)}`)

// 计算属性
const hasError = computed(() => !!props.error)
const isRequired = computed(() => props.required)

const charCount = computed(() => {
  if (typeof props.value === 'string') {
    return props.value.length
  }
  if (typeof props.value === 'number') {
    return props.value.toString().length
  }
  return 0
})

const isNearLimit = computed(() => {
  if (!props.maxLength) return false
  return charCount.value >= props.maxLength * 0.8
})

// 事件处理
const handleFocus = () => {
  isFocused.value = true
  emit('focus')
}

const handleBlur = () => {
  isFocused.value = false
  emit('blur')
}

const handleInput = (value: string | number) => {
  emit('input', value)
}

// 自动聚焦到错误字段
const focusOnError = async () => {
  if (hasError.value && props.autoFocus) {
    await nextTick()
    const input = document.getElementById(inputId.value)
    if (input) {
      input.focus()
      input.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }
}

onMounted(() => {
  focusOnError()
})
</script>

<style scoped>
.enhanced-form-item {
  margin-bottom: 24px;
  position: relative;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  transition: color 0.3s ease;
}

.form-label.required::after {
  content: ' *';
  color: var(--danger-color);
  font-weight: bold;
}

.label-tooltip {
  margin-left: 4px;
  color: var(--text-secondary);
  cursor: help;
}

.label-tooltip:hover {
  color: var(--primary-color);
}

.form-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.error-icon,
.success-icon,
.loading-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  pointer-events: none;
}

.error-icon {
  color: var(--danger-color);
  font-size: 16px;
}

.success-icon {
  color: var(--success-color);
  font-size: 16px;
}

.loading-icon {
  color: var(--primary-color);
  font-size: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: translateY(-50%) rotate(0deg); }
  to { transform: translateY(-50%) rotate(360deg); }
}

.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background-color: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.3);
  border-radius: 6px;
  color: var(--danger-color);
  font-size: 13px;
  line-height: 1.4;
}

.error-message .el-icon {
  flex-shrink: 0;
  font-size: 14px;
}

.help-text {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.char-count {
  margin-top: 6px;
  text-align: right;
  font-size: 12px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.char-count.near-limit {
  color: var(--warning-color);
  font-weight: 500;
}

/* 状态样式 */
.enhanced-form-item.has-error .form-label {
  color: var(--danger-color);
}

.enhanced-form-item.is-focused .form-label {
  color: var(--primary-color);
}

/* 动画 */
.error-message-enter-active,
.error-message-leave-active {
  transition: all 0.3s ease;
}

.error-message-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.error-message-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.error-icon-enter-active,
.error-icon-leave-active,
.success-icon-enter-active,
.success-icon-leave-active,
.loading-enter-active,
.loading-leave-active {
  transition: all 0.3s ease;
}

.error-icon-enter-from,
.error-icon-leave-to,
.success-icon-enter-from,
.success-icon-leave-to,
.loading-enter-from,
.loading-leave-to {
  opacity: 0;
  transform: translateY(-50%) scale(0.8);
}

/* 悬停效果 */
.enhanced-form-item:hover .form-label {
  color: var(--primary-color);
}

/* 响应式 */
@media (max-width: 768px) {
  .enhanced-form-item {
    margin-bottom: 20px;
  }
  
  .form-label {
    font-size: 15px;
    margin-bottom: 10px;
  }
  
  .error-message {
    padding: 10px 14px;
    font-size: 14px;
  }
  
  .help-text {
    font-size: 13px;
    margin-top: 8px;
  }
  
  .char-count {
    font-size: 13px;
    margin-top: 8px;
  }
}

/* 暗色主题适配 */
.dark-theme .error-message {
  background-color: rgba(245, 108, 108, 0.15);
  border-color: rgba(245, 108, 108, 0.4);
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .form-label.required::after {
    font-size: 16px;
  }
  
  .error-message {
    border-width: 2px;
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .error-message-enter-active,
  .error-message-leave-active,
  .error-icon-enter-active,
  .error-icon-leave-active,
  .success-icon-enter-active,
  .success-icon-leave-active,
  .loading-enter-active,
  .loading-leave-active {
    transition: none;
  }
  
  .loading-icon {
    animation: none;
  }
}
</style>
