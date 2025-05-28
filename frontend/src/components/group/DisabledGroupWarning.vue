<!-- 
  禁用小组警告提示组件
  在帖子详情页面添加小组被禁用的警告提示
-->
<template>
  <div class="disabled-group-warning animated-warning" v-if="isDisabled">
    <el-alert
      title="小组已被管理员禁用"
      type="warning"
      description="该小组已被管理员禁用，您可以浏览内容和下载文件，但不能发帖、评论或上传文件。"
      show-icon
      :closable="false"
    />
  </div>
</template>

<script setup lang="ts">
defineProps({
  isDisabled: {
    type: Boolean,
    default: false
  }
});
</script>

<style scoped>
.disabled-group-warning {
  margin: 20px 0;
}

.animated-warning {
  animation: bounceIn 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes bounceIn {
  0%, 20%, 40%, 60%, 80%, 100% {
    transition-timing-function: cubic-bezier(0.215, 0.610, 0.355, 1.000);
  }
  0% {
    opacity: 0;
    transform: scale3d(0.3, 0.3, 0.3);
  }
  20% {
    transform: scale3d(1.1, 1.1, 1.1);
  }
  40% {
    transform: scale3d(0.9, 0.9, 0.9);
  }
  60% {
    opacity: 1;
    transform: scale3d(1.03, 1.03, 1.03);
  }
  80% {
    transform: scale3d(0.97, 0.97, 0.97);
  }
  100% {
    opacity: 1;
    transform: scale3d(1, 1, 1);
  }
}

/* 暗色模式适配 */
[data-theme="dark"] .disabled-group-warning :deep(.el-alert) {
  background-color: var(--el-color-warning-dark-2);
  color: var(--el-color-black);
}

[data-theme="dark"] .disabled-group-warning :deep(.el-alert__title) {
  color: var(--el-color-black);
}

[data-theme="dark"] .disabled-group-warning :deep(.el-alert__description) {
  color: var(--el-color-black);
}
</style>
