<template>
  <div class="skeleton-container" :class="{ 'with-animation': animated }">
    <div v-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-header"></div>
      <div class="skeleton-content">
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line medium"></div>
      </div>
    </div>
    
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div v-for="i in count" :key="i" class="skeleton-list-item">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-line short"></div>
          <div class="skeleton-line medium"></div>
        </div>
      </div>
    </div>
    
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div class="skeleton-table-header">
        <div class="skeleton-line"></div>
      </div>
      <div v-for="i in count" :key="i" class="skeleton-table-row">
        <div class="skeleton-line"></div>
      </div>
    </div>
    
    <div v-else-if="type === 'avatar'" class="skeleton-avatar" :style="{ width: `${size}px`, height: `${size}px` }"></div>
    
    <div v-else-if="type === 'image'" class="skeleton-image" :style="imageStyle"></div>
    
    <div v-else class="skeleton-text">
      <div v-for="i in count" :key="i" class="skeleton-line" :class="{ 
        short: i % 4 === 0, 
        medium: i % 3 === 0,
        full: i % 5 === 0
      }"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'text', // 'text', 'card', 'list', 'table', 'avatar', 'image'
    validator: (value: string) => ['text', 'card', 'list', 'table', 'avatar', 'image'].includes(value)
  },
  count: {
    type: Number,
    default: 3
  },
  animated: {
    type: Boolean,
    default: true
  },
  size: {
    type: Number,
    default: 40 // 头像大小
  },
  width: {
    type: String,
    default: '100%' // 图片宽度
  },
  height: {
    type: String,
    default: '200px' // 图片高度
  },
  rounded: {
    type: Boolean,
    default: false // 是否圆角
  }
})

const imageStyle = computed(() => ({
  width: props.width,
  height: props.height,
  borderRadius: props.rounded ? '8px' : '0'
}))
</script>

<style scoped>
.skeleton-container {
  --skeleton-bg: #e0e0e0;
  --skeleton-highlight: #f5f5f5;
  width: 100%;
}

.dark-theme .skeleton-container {
  --skeleton-bg: #3a3a3c;
  --skeleton-highlight: #505052;
}

.skeleton-line {
  height: 16px;
  border-radius: 4px;
  background-color: var(--skeleton-bg);
  margin-bottom: 12px;
  width: 100%;
}

.skeleton-line.short {
  width: 40%;
}

.skeleton-line.medium {
  width: 75%;
}

.skeleton-line.full {
  width: 100%;
}

.skeleton-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--card-bg);
  box-shadow: var(--shadow-light);
}

.skeleton-header {
  height: 150px;
  background-color: var(--skeleton-bg);
}

.skeleton-content {
  padding: 16px;
}

.skeleton-list-item {
  display: flex;
  padding: 12px 0;
  align-items: center;
  border-bottom: 1px solid var(--border-lighter);
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--skeleton-bg);
  margin-right: 12px;
  flex-shrink: 0;
}

.skeleton-table {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.skeleton-table-header {
  padding: 12px 16px;
  background-color: var(--skeleton-bg);
  opacity: 0.7;
}

.skeleton-table-row {
  padding: 12px 16px;
  border-top: 1px solid var(--border-lighter);
}

.skeleton-image {
  background-color: var(--skeleton-bg);
}

/* 动画效果 */
.with-animation .skeleton-line,
.with-animation .skeleton-header,
.with-animation .skeleton-avatar,
.with-animation .skeleton-image {
  position: relative;
  overflow: hidden;
  background-color: var(--skeleton-bg);
}

.with-animation .skeleton-line::after,
.with-animation .skeleton-header::after,
.with-animation .skeleton-avatar::after,
.with-animation .skeleton-image::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    transparent, 
    var(--skeleton-highlight), 
    transparent
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
