<template>
  <div class="helpinfo-list-page">
    <h2>互助信息列表</h2>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <div v-if="error" class="error">{{ error }}</div>
      <ul v-else>
        <li v-for="item in list" :key="item.id">
          <router-link :to="`/helpinfo/${item.id}`">
            <strong>{{ item.title }}</strong>（{{ item.type }}）
          </router-link>
          <span style="margin-left: 8px; color: #888;">{{ item.status }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchHelpInfoList } from '../api/helpinfo'

const list = ref<any[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchHelpInfoList({})
    if (res.data.code === 200) {
      list.value = res.data.data.records || res.data.data || []
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
.helpinfo-list-page {
  max-width: 700px;
  margin: 40px auto;
}
.error {
  color: red;
  margin: 10px 0;
}
ul {
  padding-left: 0;
}
li {
  list-style: none;
  margin-bottom: 12px;
  font-size: 16px;
}
</style>
