<template>
  <div class="helpinfo-detail-page">
    <h2>互助信息详情</h2>
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="info">
      <h3>{{ info.title }}</h3>
      <div>类型：{{ info.type }}</div>
      <div>状态：{{ info.status }}</div>
      <div>描述：{{ info.description }}</div>
      <div>悬赏：{{ info.rewardAmount || '无' }}</div>
      <div>发布者：{{ info.publisherNickname }}</div>
      <div>发布时间：{{ info.createdAtFormatted || info.createdAt }}</div>
      <div>浏览量：{{ info.viewCount }}</div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { fetchHelpInfoDetail } from '../api/helpinfo'

const route = useRoute()
const info = ref<any>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const id = Number(route.params.id)
    const res = await fetchHelpInfoDetail(id)
    if (res.data.code === 200) {
      info.value = res.data.data
    } else {
      error.value = res.data.message
    }
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
})
</script>
<style scoped>
.helpinfo-detail-page {
  max-width: 600px;
  margin: 40px auto;
}
.error {
  color: red;
  margin: 10px 0;
}
</style>
