<template>
  <div class="theme-switch">
    <el-tooltip content="切换主题" placement="bottom">
      <div class="theme-toggle" @click="toggleTheme">
        <el-icon v-if="isDarkMode"><Moon /></el-icon>
        <el-icon v-else><Sunny /></el-icon>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Moon, Sunny } from '@element-plus/icons-vue'

const isDarkMode = ref(false)

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme')
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  isDarkMode.value = savedTheme ? savedTheme === 'dark' : prefersDark
  applyTheme(isDarkMode.value)
})

watch(isDarkMode, (newValue) => {
  localStorage.setItem('theme', newValue ? 'dark' : 'light')
  applyTheme(newValue)
})

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
}

const applyTheme = (dark: boolean) => {
  if (dark) {
    document.documentElement.setAttribute('data-theme', 'dark')
  } else {
    document.documentElement.setAttribute('data-theme', 'light')
  }
}
</script>

<style scoped>
.theme-switch {
  display: flex;
  align-items: center;
  margin-right: 8px;
}

.theme-toggle {
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle:hover {
  background-color: var(--hover-bg);
}
</style>
