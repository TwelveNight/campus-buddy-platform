<template>
  <div class="helpinfo-publish-page">
    <h2>发布互助信息</h2>
    <form @submit.prevent="onSubmit">
      <div>
        <label>标题</label>
        <input v-model="title" required />
      </div>
      <div>
        <label>类型</label>
        <select v-model="type" required>
          <option value="COURSE_TUTORING">课程辅导</option>
          <option value="SKILL_EXCHANGE">技能交换</option>
          <option value="ITEM_BORROW">物品借用</option>
        </select>
      </div>
      <div>
        <label>描述</label>
        <textarea v-model="description" required></textarea>
      </div>
      <div>
        <label>悬赏金额</label>
        <input v-model.number="rewardAmount" type="number" min="0" />
      </div>
      <button type="submit">发布</button>
      <div v-if="error" class="error">{{ error }}</div>
      <div v-if="success" class="success">发布成功！</div>
    </form>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { publishHelpInfo } from '../api/helpinfo'
import { useAuthStore } from '../store/auth'

const title = ref('')
const type = ref('COURSE_TUTORING')
const description = ref('')
const rewardAmount = ref(0)
const error = ref('')
const success = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function onSubmit() {
  error.value = ''
  success.value = false
  if (!auth.token) {
    error.value = '请先登录'
    return
  }
  try {
    const res = await publishHelpInfo({ title: title.value, type: type.value, description: description.value, rewardAmount: rewardAmount.value }, auth.token)
    if (res.data.code === 200 || res.data.code === 201) {
      success.value = true
      setTimeout(() => router.push('/helpinfo'), 1200)
    } else {
      error.value = res.data.message
    }
  } catch (e: any) {
    error.value = e.message || '发布失败'
  }
}
</script>
<style scoped>
.helpinfo-publish-page {
  max-width: 500px;
  margin: 40px auto;
}
.error {
  color: red;
  margin: 10px 0;
}
.success {
  color: green;
  margin: 10px 0;
}
form > div {
  margin-bottom: 16px;
}
</style>
