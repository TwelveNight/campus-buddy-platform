<template>
    <div class="main-layout">
        <NavBar />
        <main class="main-content">
            <div class="page-container">
                <router-view v-slot="{ Component }">
                    <transition name="fade" mode="out-in">
                        <component :is="Component" />
                    </transition>
                </router-view>
            </div>
        </main>
        <Footer />
        <!-- 添加调试面板 -->
        <DebugPanel v-if="showDebugPanel" />
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import NavBar from '../components/common/NavBar.vue'
import Footer from '../components/common/Footer.vue'
import DebugPanel from '../components/common/DebugPanel.vue'

// 通过 URL 参数控制是否显示调试面板
const showDebugPanel = ref(window.location.search.includes('debug=true'))
</script>

<style scoped>
.main-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: var(--background-color);
}

.main-content {
    flex: 1;
    padding-top: 1rem;
    padding-bottom: 3rem;
    background-color: var(--background-color);
}

.page-container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    background-color: var(--background-color);
}

/* 暗色模式适配 */
[data-theme="dark"] .main-layout {
    background-color: #1a1a1a !important;
}

[data-theme="dark"] .main-content {
    background-color: #1a1a1a !important;
}

[data-theme="dark"] .page-container {
    background-color: #1a1a1a !important;
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>
