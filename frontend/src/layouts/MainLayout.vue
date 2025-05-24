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
import NavBar from '../components/NavBar.vue'
import Footer from '../components/Footer.vue'
import DebugPanel from '../components/DebugPanel.vue'

// 通过 URL 参数控制是否显示调试面板
const showDebugPanel = ref(window.location.search.includes('debug=true'))
</script>

<style scoped>
.main-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
}

.main-content {
    flex: 1;
    padding-top: 1rem;
    padding-bottom: 3rem;
    background-color: var(--light-bg);
}

.page-container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
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
