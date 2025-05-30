<script setup lang="ts">
import { onMounted, ref } from 'vue'
import GlobalLoading from './components/common/GlobalLoading.vue'
import PageTransition from './components/common/PageTransition.vue'
import MobileNav from './components/mobile/MobileNav.vue'
import MobileBottomNav from './components/mobile/MobileBottomNav.vue'
import './styles/theme-transition.css'
import { useAuthStore } from './store/auth'
import webSocketService from './utils/websocket'

// 主题切换动画元素
const themeTransitionActive = ref(false)
const authStore = useAuthStore()

// 初始化主题和WebSocket
onMounted(() => {
  // 初始化主题
  const savedTheme = localStorage.getItem('theme') || 'light'
  document.documentElement.setAttribute('data-theme', savedTheme)
  
  // 检测系统主题变化并应用
  const prefersDarkScheme = window.matchMedia('(prefers-color-scheme: dark)')
  const handleThemeChange = (e) => {
    if (!localStorage.getItem('theme')) {
      document.documentElement.setAttribute('data-theme', e.matches ? 'dark' : 'light')
    }
  }
  
  prefersDarkScheme.addEventListener('change', handleThemeChange)
  
  // 监听自定义主题切换事件
  window.addEventListener('themeToggle', (e: any) => {
    const { x, y } = e.detail || { x: window.innerWidth / 2, y: window.innerHeight / 2 }
    
    // 创建主题切换动画元素
    const transitionEl = document.querySelector('.theme-toggle-transition') || document.createElement('div')
    transitionEl.className = 'theme-toggle-transition'
    transitionEl.style.setProperty('--x', `${x}px`)
    transitionEl.style.setProperty('--y', `${y}px`)
    
    if (!document.querySelector('.theme-toggle-transition')) {
      document.body.appendChild(transitionEl)
    }
    
    // 触发动画
    setTimeout(() => {
      transitionEl.classList.add('active')
      
      setTimeout(() => {
        transitionEl.classList.remove('active')
      }, 1000)
    }, 50)
  })
})
</script>

<template>
  <div id="app" class="app-container">
    <!-- 移动端顶部导航 -->
    <MobileNav />
    
    <!-- 主内容区域 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <page-transition>
          <component :is="Component" />
        </page-transition>
      </router-view>
    </main>
    
    <!-- 移动端底部导航 -->
    <MobileBottomNav />
    
    <!-- 全局加载组件 -->
    <GlobalLoading />
  </div>
</template>

<style>
/* 全局应用样式 */
#app {
  min-height: 100vh;
  background-color: var(--background-color);
  color: var(--text-regular);
  transition: background-color 0.3s ease, color 0.3s ease;
}

.app-container {
  position: relative;
  min-height: 100vh;
  background-color: var(--background-color);
}

.app-main {
  position: relative;
  z-index: 1;
  background-color: var(--background-color);
  min-height: 100vh;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .app-main {
    padding-top: 60px; /* 为移动端导航栏留出空间 */
    padding-bottom: 80px; /* 为底部导航栏留出空间 */
    min-height: calc(100vh - 140px);
  }
}

@media (max-width: 480px) {
  .app-main {
    padding-top: 56px; /* 更小屏幕的导航栏高度 */
    padding-bottom: 76px; /* 更小屏幕的底部导航高度 */
    min-height: calc(100vh - 132px);
  }
}

/* 过渡动画 */
.app-container {
  transition: all 0.3s ease;
}

/* 页面滚动优化 */
html {
  scroll-behavior: smooth;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--background-color);
  color: var(--text-regular);
}

/* 暗色模式下的全局背景 */
[data-theme="dark"] html,
[data-theme="dark"] body,
[data-theme="dark"] #app,
[data-theme="dark"] .app-container,
[data-theme="dark"] .app-main {
  background-color: #1a1a1a !important;
  color: #ffffff !important;
}

/* 移除默认样式 */
* {
  box-sizing: border-box;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--background-color);
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

/* 选择文本样式 */
::selection {
  background: var(--primary-light);
  color: var(--primary-color);
}

/* 焦点样式 */
:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

/* 禁用状态 */
.disabled {
  opacity: 0.6;
  pointer-events: none;
}

/* 工具类 */
.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.hidden {
  display: none !important;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* 打印样式 */
@media print {
  .mobile-nav-wrapper,
  .el-button,
  .no-print {
    display: none !important;
  }
  
  .app-main {
    padding-top: 0 !important;
  }
  
  .el-card {
    box-shadow: none !important;
    border: 1px solid #ddd !important;
  }
}
</style>