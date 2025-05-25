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
  // 检查本地存储的主题偏好
  const savedTheme = localStorage.getItem('theme')
  
  // 检查系统偏好
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  
  // 如果有本地存储的主题，使用它；否则使用系统偏好
  isDarkMode.value = savedTheme ? savedTheme === 'dark' : prefersDark
  
  // 应用初始主题
  applyTheme(isDarkMode.value)
})

// 监听主题变化
watch(isDarkMode, (newValue) => {
  localStorage.setItem('theme', newValue ? 'dark' : 'light')
})

// 切换主题
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  applyTheme(isDarkMode.value)
}

// 应用主题
const applyTheme = (dark: boolean) => {
  if (dark) {
    document.documentElement.classList.add('dark-theme')
  } else {
    document.documentElement.classList.remove('dark-theme')
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
