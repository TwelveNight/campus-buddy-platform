<template>
  <div class="global-loading" v-show="uiStore.isLoading">
    <div class="loading-content">
      <div class="spinner">
        <svg viewBox="25 25 50 50">
          <circle cx="50" cy="50" r="20" fill="none" stroke-width="3" stroke-miterlimit="10" />
        </svg>
      </div>
      <p class="loading-text">{{ uiStore.loadingText }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUiStore } from '../../store/ui'

const uiStore = useUiStore()
</script>

<style scoped>
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}

.loading-content {
  background-color: var(--card-bg);
  border-radius: 8px;
  padding: 24px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: var(--shadow-light);
  min-width: 160px;
  animation: fadeIn 0.3s;
}

.loading-text {
  margin-top: 16px;
  color: var(--text-regular);
  font-size: 14px;
}

.spinner {
  width: 40px;
  height: 40px;
}

.spinner svg {
  animation: rotate 2s linear infinite;
  transform-origin: center center;
}

.spinner circle {
  stroke: var(--primary-color);
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
