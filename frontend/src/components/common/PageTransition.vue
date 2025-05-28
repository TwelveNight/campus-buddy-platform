<template>
  <transition 
    name="fade-transform" 
    mode="out-in"
    v-if="uiStore.pageTransition"
  >
    <slot></slot>
  </transition>
  <slot v-else></slot>
</template>

<script setup lang="ts">
import { useUiStore } from "../../store/ui";

const uiStore = useUiStore();
</script>

<style>
/* 页面过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.25s; /* 减短过渡时间 */
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(5px); /* 减小位移效果 */
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-5px); /* 减小位移效果 */
}
</style>
