<template>
  <div class="account-disabled-page">
    <section class="disabled-panel">
      <div class="status-mark">
        <el-icon><Lock /></el-icon>
      </div>
      <div class="disabled-copy">
        <p class="eyebrow">账号访问受限</p>
        <h1>{{ title }}</h1>
        <p class="message">{{ message }}</p>
      </div>
      <div class="actions">
        <el-button type="primary" @click="goLogin">返回登录</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Lock } from '@element-plus/icons-vue'
import {
  clearAccountUnavailable,
  getAccountUnavailable,
  getAccountUnavailableMessage
} from '@/utils/accountStatus'

const router = useRouter()
const info = computed(() => getAccountUnavailable())

const title = computed(() => {
  if (info.value?.status === 'INACTIVE') return '账号未激活'
  return '账号已被禁用'
})

const message = computed(() => info.value?.message || getAccountUnavailableMessage('UNAVAILABLE'))

const goLogin = () => {
  clearAccountUnavailable()
  router.replace('/login')
}
</script>

<style scoped>
.account-disabled-page {
  min-height: calc(100vh - 140px);
  display: grid;
  place-items: center;
  padding: 48px 20px;
  background: var(--background-color);
}

.disabled-panel {
  width: min(520px, 100%);
  padding: 40px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.08);
  text-align: center;
}

.status-mark {
  width: 72px;
  height: 72px;
  margin: 0 auto 22px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
  font-size: 34px;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--el-color-danger);
  font-size: 14px;
  font-weight: 600;
}

h1 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 30px;
  line-height: 1.25;
}

.message {
  margin: 16px 0 0;
  color: var(--el-text-color-regular);
  font-size: 16px;
  line-height: 1.7;
}

.actions {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

[data-theme="dark"] .disabled-panel {
  background: #24293d;
  border-color: var(--dark-border-color, #333);
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.32);
}

[data-theme="dark"] h1 {
  color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .message {
  color: var(--dark-text-secondary, #a3a6ad);
}

@media (max-width: 480px) {
  .account-disabled-page {
    padding: 32px 16px;
  }

  .disabled-panel {
    padding: 32px 22px;
  }

  h1 {
    font-size: 24px;
  }
}
</style>

