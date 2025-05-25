<template>
  <div 
    class="ripple-container" 
    :class="{ disabled: disabled }"
    @click="createRipple"
  >
    <slot></slot>
    <span v-for="(ripple, index) in ripples" 
      :key="index" 
      class="ripple" 
      :style="ripple.style"
    ></span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Ripple {
  style: {
    left: string
    top: string
    backgroundColor: string
    opacity: number
  }
  timestamp: number
}

defineProps({
  color: {
    type: String,
    default: '#fff'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const ripples = ref<Ripple[]>([])

// 创建波纹效果
const createRipple = (event: MouseEvent) => {
  if (disabled) return
  
  const target = event.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  
  // 计算点击位置相对于元素的坐标
  const left = event.clientX - rect.left
  const top = event.clientY - rect.top
  
  // 创建新的波纹
  const ripple: Ripple = {
    style: {
      left: `${left}px`,
      top: `${top}px`,
      backgroundColor: color,
      opacity: 0.4
    },
    timestamp: Date.now()
  }
  
  ripples.value.push(ripple)
  
  // 限制波纹数量，避免内存泄漏
  if (ripples.value.length > 10) {
    ripples.value.shift()
  }
  
  // 在动画结束后移除波纹
  setTimeout(() => {
    const index = ripples.value.findIndex(r => r.timestamp === ripple.timestamp)
    if (index !== -1) {
      ripples.value.splice(index, 1)
    }
  }, 1000)
}
</script>

<style scoped>
.ripple-container {
  position: relative;
  overflow: hidden;
  user-select: none;
  cursor: pointer;
}

.ripple-container.disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.ripple {
  position: absolute;
  border-radius: 50%;
  transform: scale(0);
  animation: ripple 800ms linear;
  pointer-events: none;
}

@keyframes ripple {
  to {
    transform: scale(4);
    opacity: 0;
  }
}
</style>
