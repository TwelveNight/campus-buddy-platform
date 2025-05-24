<template>
  <div class="debug-panel" v-if="isVisible">
    <h3>调试信息</h3>
    <div class="debug-content">
      <p><strong>用户名:</strong> {{ authStore.user?.username }}</p>
      <p><strong>昵称:</strong> {{ authStore.user?.nickname }}</p>
      <p><strong>用户ID:</strong> {{ authStore.user?.userId }}</p>
      <p><strong>角色:</strong> {{ authStore.user?.roles?.join(', ') || '无角色' }}</p>
      <p><strong>是否管理员:</strong> {{ authStore.isAdmin ? '是' : '否' }}</p>
      <p><strong>是否已认证:</strong> {{ authStore.isAuthenticated ? '是' : '否' }}</p>
      <p><strong>当前路由:</strong> {{ $route.path }}</p>
      <button @click="toggleVisible">隐藏调试面板</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'

const $route = useRoute()
const authStore = useAuthStore()
const isVisible = ref(true)

const toggleVisible = () => {
  isVisible.value = !isVisible.value
}
</script>

<style scoped>
.debug-panel {
  position: fixed;
  bottom: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 10px;
  border-radius: 5px;
  font-size: 12px;
  max-width: 300px;
  z-index: 9999;
}

.debug-panel h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #42b983;
}

.debug-content p {
  margin: 5px 0;
}

button {
  margin-top: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  padding: 3px 8px;
  border-radius: 3px;
  cursor: pointer;
}
</style>
