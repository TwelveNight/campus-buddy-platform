<template>
  <div class="register-page">
    <h2>注册</h2>
    <form @submit.prevent="onSubmit">
      <div>
        <label>用户名</label>
        <input v-model="username" required />
      </div>
      <div>
        <label>昵称</label>
        <input v-model="nickname" required />
      </div>
      <div>
        <label>密码</label>
        <input v-model="password" type="password" required />
      </div>
      <button type="submit">注册</button>
      <div v-if="error" class="error">{{ error }}</div>
      <div v-if="success" class="success">注册成功，请前往登录</div>
    </form>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const username = ref('')
const nickname = ref('')
const password = ref('')
const error = ref('')
const success = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function onSubmit() {
  error.value = ''
  success.value = false
  try {
    await auth.registerAction({ username: username.value, password: password.value, nickname: nickname.value })
    success.value = true
    setTimeout(() => router.push('/login'), 1200)
  } catch (e: any) {
    error.value = e.message || '注册失败'
  }
}
</script>
<style scoped>
.register-page {
  max-width: 400px;
  margin: 60px auto;
}
.error {
  color: red;
  margin-top: 10px;
}
.success {
  color: green;
  margin-top: 10px;
}
</style>
