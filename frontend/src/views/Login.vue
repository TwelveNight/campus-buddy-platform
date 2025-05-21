<template>
  <div class="login-page">
    <h2>登录</h2>
    <form @submit.prevent="onSubmit">
      <div>
        <label>用户名</label>
        <input v-model="username" required />
      </div>
      <div>
        <label>密码</label>
        <input v-model="password" type="password" required />
      </div>
      <button type="submit">登录</button>
      <div v-if="error" class="error">{{ error }}</div>
    </form>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const username = ref('')
const password = ref('')
const error = ref('')
const router = useRouter()
const auth = useAuthStore()

async function onSubmit() {
  error.value = ''
  try {
    await auth.loginAction({ username: username.value, password: password.value })
    router.push('/helpinfo')
  } catch (e: any) {
    error.value = e.message || '登录失败'
  }
}
</script>
<style scoped>
.login-page {
  max-width: 400px;
  margin: 60px auto;
}
.error {
  color: red;
  margin-top: 10px;
}
</style>
