<template>
    <div class="main-layout">
        <NavBar />
        <MobileNav />
        <MobileBottomNav />
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
import MobileNav from '../components/mobile/MobileNav.vue'
import MobileBottomNav from '../components/mobile/MobileBottomNav.vue'

// 通过 URL 参数控制是否显示调试面板
const showDebugPanel = ref(window.location.search.includes('debug=true'))
</script>

<style scoped>
.main-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: var(--background-color);
    width: 100%;
    box-sizing: border-box;
    overflow-x: hidden; /* 防止水平溢出 */
}

.main-content {
    flex: 1;
    padding-top: 1rem;
    padding-bottom: 3rem;
    background-color: var(--background-color);
    width: 100%;
    box-sizing: border-box;
}

.page-container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    background-color: var(--background-color);
    box-sizing: border-box;
}

@media (max-width: 768px) {
    .page-container {
        padding: 0 15px;
        max-width: 100%;
    }

    .main-content {
        padding-top: 0.5rem;
        padding-bottom: 70px; /* 为底部导航留出空间 */
    }
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
