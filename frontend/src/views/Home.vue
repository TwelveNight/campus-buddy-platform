<template>
    <div class="home-page">
        <!-- 英雄区域 -->
        <section class="hero-section">
            <div class="hero-content">
                <h1>学伴 <span>校园互助与成长平台</span></h1>
                <p class="hero-description">让每一位同学都能在这里找到帮助、收获成长、结识伙伴</p>
                <p class="hero-sub-description">互助 · 共享 · 进步 | 校园生活新方式</p>
                <div class="cta-buttons">
                    <el-button type="primary" size="large" round @click="navigateTo('/helpinfo')">
                        <el-icon><Promotion /></el-icon>浏览互助任务
                    </el-button>
                    <el-button size="large" round class="secondary-btn" @click="navigateTo('/helpinfo/publish')" v-if="authStore.isAuthenticated">
                        <el-icon><Edit /></el-icon>发布互助
                    </el-button>
                    <el-button size="large" round class="accent-btn" @click="navigateTo('/groups?tab=all')" v-if="authStore.isAuthenticated">
                        <el-icon><UserFilled /></el-icon>学习小组
                    </el-button>
                </div>
            </div>
            <div class="hero-image">
                <img src="../assets/vue.svg" alt="学伴LOGO" />
                <div class="floating-icon icon-1"><el-icon><ChatLineRound /></el-icon></div>
                <div class="floating-icon icon-2"><el-icon><Files /></el-icon></div>
                <div class="floating-icon icon-3"><el-icon><Trophy /></el-icon></div>
            </div>
        </section>

        <!-- 平台特色部分 -->
        <section class="features-section">
            <h2 class="section-title">平台特色</h2>
            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon"><el-icon><Connection /></el-icon></div>
                    <h3>高效互助</h3>
                    <p>一键发布/接收互助任务，涵盖学业、生活、技能等多场景</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon accent"><el-icon><Trophy /></el-icon></div>
                    <h3>信用积分</h3>
                    <p>完成互助任务可获积分，积分提升个人信誉与特权</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon secondary"><el-icon><ChatLineRound /></el-icon></div>
                    <h3>实时沟通</h3>
                    <p>内置私信与群聊，沟通无障碍，协作更高效</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon tertiary"><el-icon><Reading /></el-icon></div>
                    <h3>学习小组</h3>
                    <p>组队学习、资料共享、共同进步，打造专属学习圈</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon quaternary"><el-icon><Star /></el-icon></div>
                    <h3>评价反馈</h3>
                    <p>完善的互评体系，保障互助体验与平台健康</p>
                </div>
            </div>
        </section>

        <!-- 平台功能区域 -->
        <section class="modules-section">
            <h2 class="section-title">核心功能</h2>
            <el-row :gutter="30" class="module-row">
                <el-col :xs="24" :sm="12" :md="8">
                    <el-card class="module-card active" shadow="hover" @click="navigateTo('/helpinfo')">
                        <el-icon class="module-icon primary"><Service /></el-icon>
                        <h3>校园互助</h3>
                        <p>浏览/发布互助任务，涵盖学业辅导、技能交换、生活互助等</p>
                        <div class="card-action">
                            <el-button type="primary" text round>立即体验<el-icon><ArrowRight /></el-icon></el-button>
                        </div>
                    </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8">
                    <el-card class="module-card active" shadow="hover" @click="navigateTo('/groups?tab=all')">
                        <el-icon class="module-icon secondary"><UserFilled /></el-icon>
                        <h3>学习小组</h3>
                        <p>创建/加入小组，协作学习、资料共享、组队成长</p>
                        <div class="card-tag">
                            <el-tag size="small" effect="dark" type="success">新功能</el-tag>
                        </div>
                        <div class="card-action">
                            <el-button type="success" text round>立即体验<el-icon><ArrowRight /></el-icon></el-button>
                        </div>
                    </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8">
                    <el-card class="module-card disabled" shadow="hover">
                        <el-icon class="module-icon accent"><Files /></el-icon>
                        <h3>资源共享</h3>
                        <p>资料、二手物品等校园资源自由分享与获取</p>
                        <div class="card-tag">
                            <el-tag size="small" effect="dark" type="warning">即将上线</el-tag>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </section>

        <!-- 统计数据展示 -->
        <section class="stats-section">
            <h2 class="section-title">平台数据</h2>
            <div class="stats-container">
                <div class="stat-item">
                    <div class="stat-number">3,200+</div>
                    <div class="stat-label">注册用户</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">5,600+</div>
                    <div class="stat-label">互助任务</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">1,200+</div>
                    <div class="stat-label">学习小组</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">9,800+</div>
                    <div class="stat-label">互助成功</div>
                </div>
            </div>
        </section>

        <!-- 使用指南 -->
        <section class="guide-section">
            <h2 class="section-title">新手指南</h2>
            <div class="guide-container">
                <div class="guide-step">
                    <div class="step-number">1</div>
                    <div class="step-icon"><el-icon><User /></el-icon></div>
                    <h3>注册/登录</h3>
                    <p>快速注册，完善个人信息，开启互助之旅</p>
                </div>
                <div class="guide-arrow"><el-icon><ArrowRight /></el-icon></div>
                <div class="guide-step">
                    <div class="step-number">2</div>
                    <div class="step-icon"><el-icon><Promotion /></el-icon></div>
                    <h3>浏览/发布互助</h3>
                    <p>查找你需要的帮助，或主动发布互助任务</p>
                </div>
                <div class="guide-arrow"><el-icon><ArrowRight /></el-icon></div>
                <div class="guide-step">
                    <div class="step-number">3</div>
                    <div class="step-icon"><el-icon><ChatDotRound /></el-icon></div>
                    <h3>沟通协作</h3>
                    <p>与同学实时沟通，协作解决问题</p>
                </div>
                <div class="guide-arrow"><el-icon><ArrowRight /></el-icon></div>
                <div class="guide-step">
                    <div class="step-number">4</div>
                    <div class="step-icon"><el-icon><Star /></el-icon></div>
                    <h3>互评反馈</h3>
                    <p>完成互助后互相评价，积累信用积分</p>
                </div>
            </div>
        </section>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import {
    Connection,
    UserFilled,
    Files,
    Service,
    ChatLineRound,
    Trophy,
    Promotion,
    Edit,
    ArrowRight,
    Star,
    Reading,
    User,
    ChatDotRound
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

function navigateTo(path: string) {
    router.push(path)
}
</script>

<style scoped>
.home-page {
    width: 100%;
    margin: 0 auto;
    max-width: 1440px;
    min-width: 320px;
    padding: 0 32px;
    box-sizing: border-box;
}

/* 英雄区域样式 */
.hero-section {
    display: flex;
    align-items: center;
    padding: 80px 0;
    margin-bottom: 60px;
    background: linear-gradient(135deg, 
        var(--primary-color) 0%, 
        #2c65dd 35%, 
        #8b5cf6 70%, 
        #10b981 100%);
    color: white;
    border-radius: 0 0 48px 48px;
    position: relative;
    overflow: hidden;
    min-height: 520px;
    padding-left: 64px;
    padding-right: 64px;
}

@media (max-width: 1200px) {
    .hero-section {
        padding-left: 24px;
        padding-right: 24px;
    }
}

@media (max-width: 768px) {
    .hero-section {
        padding-left: 8px;
        padding-right: 8px;
        min-height: 340px;
        border-radius: 0 0 24px 24px;
    }
}

/* 添加动态粒子背景 */
.hero-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 40%),
        radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.08) 0%, transparent 35%),
        radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.06) 0%, transparent 30%),
        radial-gradient(circle at 90% 20%, rgba(255, 255, 255, 0.04) 0%, transparent 25%);
    animation: heroParticles 15s ease-in-out infinite;
    pointer-events: none;
}

/* 添加光线效果 */
.hero-section::after {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: conic-gradient(
        from 0deg at 50% 50%,
        transparent 0deg,
        rgba(255, 255, 255, 0.05) 45deg,
        transparent 90deg,
        rgba(255, 255, 255, 0.03) 135deg,
        transparent 180deg,
        rgba(255, 255, 255, 0.05) 225deg,
        transparent 270deg,
        rgba(255, 255, 255, 0.03) 315deg,
        transparent 360deg
    );
    animation: heroRotate 20s linear infinite;
    pointer-events: none;
}

.hero-content {
    flex: 1;
    padding: 0 40px;
    max-width: 600px;
    z-index: 3;
    position: relative;
}

.hero-content h1 {
    font-size: 3.2rem;
    margin-bottom: 20px;
    font-weight: 800;
    line-height: 1.2;
    text-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
    animation: heroTextGlow 3s ease-in-out infinite alternate;
}

.hero-content h1 span {
    background: linear-gradient(135deg, #ffeb3b, #ffc107, #ff9800);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    display: block;
    filter: drop-shadow(0 2px 4px rgba(255, 193, 7, 0.3));
}

.hero-description {
    font-size: 1.3rem;
    margin-bottom: 20px;
    opacity: 0.95;
    line-height: 1.6;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.hero-sub-description {
    font-size: 1.15rem;
    margin-bottom: 40px;
    opacity: 0.85;
    line-height: 1.5;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.hero-image {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 2;
    position: relative;
}

.hero-image img {
    max-width: 100%;
    height: 240px;
    filter: drop-shadow(0 15px 25px rgba(0, 0, 0, 0.4));
    animation: heroFloat 6s ease-in-out infinite;
    transition: transform 0.3s ease;
}

.hero-image img:hover {
    transform: scale(1.1) rotate(5deg);
}

.floating-icon {
    position: absolute;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, 
        rgba(255, 255, 255, 0.25), 
        rgba(255, 255, 255, 0.1));
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    animation: floatingIconPulse 4s ease-in-out infinite;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.floating-icon :deep(svg) {
    font-size: 28px;
    color: white;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.icon-1 {
    top: 15%;
    left: 5%;
    animation-delay: 0s;
    animation-duration: 5s;
}

.icon-2 {
    bottom: 10%;
    right: 10%;
    animation-delay: 1.5s;
    animation-duration: 6s;
}

.icon-3 {
    top: 25%;
    right: 15%;
    animation-delay: 3s;
    animation-duration: 4.5s;
}

.cta-buttons {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
}

.cta-buttons :deep(.el-button) {
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    position: relative;
    overflow: hidden;
    font-weight: 600;
    padding: 14px 28px;
    font-size: 16px;
    border-radius: 25px;
}

.cta-buttons :deep(.el-button::before) {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.6s ease;
}

.cta-buttons :deep(.el-button:hover::before) {
    left: 100%;
}

.cta-buttons :deep(.el-button:hover) {
    transform: translateY(-3px) scale(1.05);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
}

.secondary-btn {
    background: linear-gradient(135deg, 
        rgba(255, 255, 255, 0.25), 
        rgba(255, 255, 255, 0.15)) !important;
    color: white !important;
    border: 1px solid rgba(255, 255, 255, 0.3) !important;
    backdrop-filter: blur(10px);
}

.accent-btn {
    background: linear-gradient(135deg, 
        rgba(255, 255, 255, 0.25), 
        rgba(255, 255, 255, 0.15)) !important;
    color: white !important;
    border: 1px solid rgba(255, 255, 255, 0.3) !important;
    backdrop-filter: blur(10px);
}

/* 平台特色区域 */
.features-section {
    padding: 60px 40px 80px;
    text-align: center;
    max-width: 1380px;
    margin-left: auto;
    margin-right: auto;
    padding-left: 48px;
    padding-right: 48px;
    position: relative;
}

.features-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.03) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.02) 0%, transparent 50%);
    pointer-events: none;
}

.section-title {
    font-size: 2.5rem;
    color: var(--text-primary);
    margin-bottom: 60px;
    position: relative;
    display: inline-block;
    font-weight: 700;
    z-index: 2;
}

.section-title::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 120%;
    height: 120%;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.05), 
        rgba(139, 92, 246, 0.03));
    border-radius: 20px;
    z-index: -1;
}

.section-title::after {
    content: '';
    position: absolute;
    width: 80px;
    height: 4px;
    background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
    bottom: -15px;
    left: 50%;
    transform: translateX(-50%);
    border-radius: 2px;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 40px;
    margin-bottom: 40px;
    position: relative;
    z-index: 2;
}

.feature-card {
    background: linear-gradient(135deg, 
        rgba(255, 255, 255, 0.9), 
        rgba(248, 250, 252, 0.8));
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    padding: 40px 30px;
    box-shadow: 
        0 8px 25px rgba(0, 0, 0, 0.08),
        0 2px 10px rgba(0, 0, 0, 0.04);
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    position: relative;
    overflow: hidden;
}

.feature-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.05), 
        rgba(139, 92, 246, 0.03));
    opacity: 0;
    transition: opacity 0.3s ease;
}

.feature-card:hover::before {
    opacity: 1;
}

.feature-card:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 
        0 20px 40px rgba(0, 0, 0, 0.12),
        0 8px 20px rgba(59, 130, 246, 0.1);
}

.feature-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, 
        rgba(59, 130, 246, 0.15), 
        rgba(59, 130, 246, 0.08));
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 25px;
    position: relative;
    z-index: 2;
    box-shadow: 0 8px 20px rgba(59, 130, 246, 0.2);
    transition: all 0.3s ease;
}

.feature-icon::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.feature-card:hover .feature-icon::before {
    opacity: 0.1;
}

.feature-icon :deep(svg) {
    font-size: 36px;
    color: var(--primary-color);
    position: relative;
    z-index: 1;
    filter: drop-shadow(0 2px 4px rgba(59, 130, 246, 0.3));
}

.feature-icon.accent {
    background: linear-gradient(135deg, 
        rgba(239, 68, 68, 0.15), 
        rgba(239, 68, 68, 0.08));
    box-shadow: 0 8px 20px rgba(239, 68, 68, 0.2);
}

.feature-icon.accent :deep(svg) {
    color: #ef4444;
    filter: drop-shadow(0 2px 4px rgba(239, 68, 68, 0.3));
}

.feature-icon.secondary {
    background: linear-gradient(135deg, 
        rgba(16, 185, 129, 0.15), 
        rgba(16, 185, 129, 0.08));
    box-shadow: 0 8px 20px rgba(16, 185, 129, 0.2);
}

.feature-icon.secondary :deep(svg) {
    color: #10b981;
    filter: drop-shadow(0 2px 4px rgba(16, 185, 129, 0.3));
}

.feature-icon.tertiary {
    background: linear-gradient(135deg, 
        rgba(139, 92, 246, 0.15), 
        rgba(139, 92, 246, 0.08));
    box-shadow: 0 8px 20px rgba(139, 92, 246, 0.2);
}

.feature-icon.tertiary :deep(svg) {
    color: #8b5cf6;
    filter: drop-shadow(0 2px 4px rgba(139, 92, 246, 0.3));
}

.feature-icon.quaternary {
    background: linear-gradient(135deg, 
        rgba(245, 158, 11, 0.15), 
        rgba(245, 158, 11, 0.08));
    box-shadow: 0 8px 20px rgba(245, 158, 11, 0.2);
}

.feature-icon.quaternary :deep(svg) {
    color: #f59e0b;
    filter: drop-shadow(0 2px 4px rgba(245, 158, 11, 0.3));
}

.feature-card h3 {
    font-size: 1.4rem;
    color: var(--text-primary);
    margin-bottom: 15px;
    font-weight: 600;
    position: relative;
    z-index: 2;
}

.feature-card p {
    color: var(--text-secondary);
    line-height: 1.6;
    font-size: 15px;
    position: relative;
    z-index: 2;
}

.feature-icon.tertiary :deep(svg) {
    color: #6c5ce7;
}

.feature-icon.quaternary {
    background-color: rgba(253, 150, 68, 0.15);
}

.feature-icon.quaternary :deep(svg) {
    color: #fd9644;
}

.feature-card h3 {
    font-size: 1.4rem;
    margin: 15px 0;
    color: var(--text-primary);
}

.feature-card p {
    color: var(--text-secondary);
    line-height: 1.7;
}

/* 模块区域 */
.modules-section {
    padding: 40px 40px 80px;
    background-color: var(--light-bg);
    text-align: center;
    max-width: 1380px;
    margin-left: auto;
    margin-right: auto;
    padding-left: 48px;
    padding-right: 48px;
}

.module-row {
    margin-top: 40px;
}

.module-card {
    height: 280px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    margin-bottom: 30px;
    padding: 30px;
    border-radius: 16px !important;
    overflow: hidden;
    position: relative;
    transition: all 0.3s;
}

.module-card.active {
    cursor: pointer;
}

.module-card.active:hover {
    transform: translateY(-8px);
}

.module-card.disabled {
    opacity: 0.7;
    cursor: default;
}

.module-card h3 {
    font-size: 1.5rem;
    margin: 15px 0;
    color: var(--text-primary);
}

.module-card p {
    color: var(--text-secondary);
    line-height: 1.7;
    margin-bottom: 20px;
}

.module-icon {
    font-size: 40px;
    margin-bottom: 15px;
}

.module-icon.primary {
    color: var(--primary-color);
}

.module-icon.secondary {
    color: var(--secondary-color);
}

.module-icon.accent {
    color: var(--accent-color);
}

.card-action {
    margin-top: auto;
}

.card-tag {
    position: absolute;
    top: 16px;
    right: 16px;
}

/* 统计数据区域 */
.stats-section {
    padding: 60px 40px;
    background-color: var(--primary-color);
    color: white;
    text-align: center;
    margin: 60px 0;
    max-width: 1380px;
    margin-left: auto;
    margin-right: auto;
    padding-left: 48px;
    padding-right: 48px;
    position: relative;
    overflow: hidden;
}

.stats-section .section-title {
    color: white;
    margin-bottom: 40px;
}

.stats-section .section-title::after {
    background-color: white;
}

.stats-container {
    display: flex;
    justify-content: space-around;
    flex-wrap: wrap;
    max-width: 1200px;
    margin: 0 auto;
}

.stat-item {
    padding: 20px;
    flex: 1;
    min-width: 200px;
    margin: 10px;
}

.stat-number {
    font-size: 3rem;
    font-weight: 700;
    margin-bottom: 10px;
    background: linear-gradient(45deg, #ffffff, #ffffffaa);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
}

.stat-label {
    font-size: 1.2rem;
    opacity: 0.9;
}

/* 使用指南区域 */
.guide-section {
    padding: 60px 40px;
    text-align: center;
    max-width: 1380px;
    margin-left: auto;
    margin-right: auto;
    padding-left: 48px;
    padding-right: 48px;
    margin: 0 auto 60px;
}

.guide-container {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    margin-top: 40px;
}

.guide-step {
    background-color: var(--card-bg);
    border-radius: 16px;
    padding: 30px 20px;
    width: 200px;
    margin: 10px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
    position: relative;
    transition: transform 0.3s;
}

.guide-step:hover {
    transform: translateY(-8px);
}

.step-number {
    position: absolute;
    top: -15px;
    left: 50%;
    transform: translateX(-50%);
    width: 30px;
    height: 30px;
    background-color: var(--primary-color);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
}

.step-icon {
    width: 60px;
    height: 60px;
    background-color: rgba(62, 123, 250, 0.15);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
}

.step-icon :deep(svg) {
    font-size: 28px;
    color: var(--primary-color);
}

.guide-step h3 {
    font-size: 1.2rem;
    margin: 15px 0 10px;
    color: var(--text-primary);
}

.guide-step p {
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.guide-arrow {
    font-size: 24px;
    color: var(--text-secondary);
    margin: 0 -5px;
}

/* 浮动动画 */
@keyframes float {
    0% {
        transform: translateY(0px);
    }
    50% {
        transform: translateY(-15px);
    }
    100% {
        transform: translateY(0px);
    }
}

/* 响应式调整 */
@media (max-width: 1200px) {
    .features-section,
    .modules-section,
    .stats-section,
    .guide-section {
        max-width: 98vw;
        padding-left: 18px;
        padding-right: 18px;
    }
    .home-page {
        padding: 0 8px;
    }
}

@media (max-width: 768px) {
    .features-section,
    .modules-section,
    .stats-section,
    .guide-section {
        padding-left: 4px;
        padding-right: 4px;
    }
    .hero-content h1 {
        font-size: 2.2rem;
    }

    .hero-description {
        font-size: 1.1rem;
    }

    .section-title {
        font-size: 1.8rem;
    }

    .guide-arrow {
        display: none;
    }

    .guide-step {
        width: 100%;
        max-width: 250px;
        margin-bottom: 30px;
    }
}

/* 暗色主题适配 */
[data-theme="dark"] .home-page {
    background-color: var(--background-color);
}

[data-theme="dark"] .hero-section {
    background: linear-gradient(135deg, #1a365d 0%, #2d3748 35%, #553c9a 70%, #1a5f5f 100%);
    color: #fff;
}

[data-theme="dark"] .hero-section::before {
    background: 
        radial-gradient(circle at 20% 30%, rgba(255,255,255,0.08) 0%, transparent 40%),
        radial-gradient(circle at 80% 70%, rgba(255,255,255,0.06) 0%, transparent 35%),
        radial-gradient(circle at 40% 80%, rgba(255,255,255,0.04) 0%, transparent 30%),
        radial-gradient(circle at 90% 20%, rgba(255,255,255,0.03) 0%, transparent 25%);
}

[data-theme="dark"] .hero-section::after {
    background: conic-gradient(
        from 0deg at 50% 50%,
        transparent 0deg,
        rgba(255,255,255,0.08) 45deg,
        transparent 90deg,
        rgba(255,255,255,0.05) 135deg,
        transparent 180deg,
        rgba(255,255,255,0.08) 225deg,
        transparent 270deg,
        rgba(255,255,255,0.05) 315deg,
        transparent 360deg
    );
}

[data-theme="dark"] .hero-content h1 {
    color: #ffffff;
    text-shadow: 0 4px 15px rgba(0, 0, 0, 0.5);
}

[data-theme="dark"] .hero-content h1 span {
    background: linear-gradient(135deg, #fbbf24, #f59e0b, #d97706);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    filter: drop-shadow(0 2px 4px rgba(251,191,36,0.4));
}

[data-theme="dark"] .hero-description,
[data-theme="dark"] .hero-sub-description {
    color: rgba(255,255,255,0.9);
}

[data-theme="dark"] .floating-icon {
    background: linear-gradient(135deg, rgba(59,130,246,0.18), rgba(139,92,246,0.12));
    border: 1px solid rgba(255,255,255,0.18);
    box-shadow: 0 8px 25px rgba(0,0,0,0.25);
}

[data-theme="dark"] .floating-icon :deep(svg) {
    color: #fff;
}

[data-theme="dark"] .secondary-btn,
[data-theme="dark"] .accent-btn {
    background: linear-gradient(135deg, rgba(59,130,246,0.18), rgba(139,92,246,0.12)) !important;
    color: #fff !important;
    border: 1px solid rgba(255,255,255,0.18) !important;
}

[data-theme="dark"] .features-section {
    background: linear-gradient(135deg, #232946 0%, #2d3748 100%);
}

[data-theme="dark"] .features-section::before {
    background: 
        radial-gradient(circle at 20% 50%, rgba(59,130,246,0.08) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(139,92,246,0.06) 0%, transparent 50%);
}

[data-theme="dark"] .feature-card {
    background: linear-gradient(135deg, rgba(36,41,61,0.95), rgba(45,55,72,0.92));
    border: 1px solid rgba(255,255,255,0.08);
    box-shadow: 0 8px 25px rgba(0,0,0,0.18);
}

[data-theme="dark"] .feature-card:hover {
    box-shadow: 0 20px 40px rgba(0,0,0,0.28), 0 8px 20px rgba(59,130,246,0.18);
}

[data-theme="dark"] .feature-card h3 {
    color: #fff;
}

[data-theme="dark"] .feature-card p {
    color: #cbd5e1;
}

[data-theme="dark"] .feature-icon {
    background: linear-gradient(135deg, rgba(59,130,246,0.22), rgba(59,130,246,0.12));
    box-shadow: 0 8px 20px rgba(59,130,246,0.18);
}

[data-theme="dark"] .feature-icon.accent {
    background: linear-gradient(135deg, rgba(239,68,68,0.22), rgba(239,68,68,0.12));
    box-shadow: 0 8px 20px rgba(239,68,68,0.18);
}

[data-theme="dark"] .feature-icon.secondary {
    background: linear-gradient(135deg, rgba(16,185,129,0.22), rgba(16,185,129,0.12));
    box-shadow: 0 8px 20px rgba(16,185,129,0.18);
}

[data-theme="dark"] .feature-icon.tertiary {
    background: linear-gradient(135deg, rgba(139,92,246,0.22), rgba(139,92,246,0.12));
    box-shadow: 0 8px 20px rgba(139,92,246,0.18);
}

[data-theme="dark"] .feature-icon.quaternary {
    background: linear-gradient(135deg, rgba(245,158,11,0.22), rgba(245,158,11,0.12));
    box-shadow: 0 8px 20px rgba(245,158,11,0.18);
}

[data-theme="dark"] .modules-section {
    background: linear-gradient(135deg, #232946 0%, #2d3748 100%);
}

[data-theme="dark"] .module-card {
    background: linear-gradient(135deg, rgba(36,41,61,0.95), rgba(45,55,72,0.92));
    border: 1px solid rgba(255,255,255,0.08);
    box-shadow: 0 8px 25px rgba(0,0,0,0.18);
}

[data-theme="dark"] .module-card h3 {
    color: #fff;
}

[data-theme="dark"] .module-card p {
    color: #cbd5e1;
}

[data-theme="dark"] .module-icon.primary,
[data-theme="dark"] .module-icon.secondary,
[data-theme="dark"] .module-icon.accent {
    filter: drop-shadow(0 2px 8px rgba(59,130,246,0.25));
}

[data-theme="dark"] .stats-section {
    background: linear-gradient(135deg, 
        rgba(26, 32, 44, 0.95) 0%, 
        rgba(45, 55, 72, 0.9) 100%);
}

[data-theme="dark"] .stat-number {
    background: linear-gradient(45deg, #ffffff, #e2e8f0);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
}

[data-theme="dark"] .stat-label {
    color: rgba(255, 255, 255, 0.8);
}

[data-theme="dark"] .guide-step {
    background: linear-gradient(145deg, 
        var(--card-bg) 0%, 
        rgba(45, 55, 72, 0.8) 100%);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .guide-step:hover {
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.4);
}

[data-theme="dark"] .guide-step h3 {
    color: var(--text-primary);
}

[data-theme="dark"] .guide-step p {
    color: var(--text-secondary);
}

[data-theme="dark"] .guide-arrow {
    color: var(--text-secondary);
}

[data-theme="dark"] .step-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 动画关键帧定义 */
@keyframes heroParticles {
    0%, 100% {
        transform: translateY(0) scale(1);
        opacity: 0.8;
    }
    50% {
        transform: translateY(-20px) scale(1.1);
        opacity: 1;
    }
}

@keyframes heroRotate {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

@keyframes heroTextGlow {
    0% {
        text-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
    }
    100% {
        text-shadow: 0 4px 20px rgba(255, 255, 255, 0.2),
                     0 8px 30px rgba(0, 0, 0, 0.4);
    }
}

@keyframes heroFloat {
    0%, 100% {
        transform: translateY(0) rotate(0deg);
    }
    50% {
        transform: translateY(-10px) rotate(5deg);
    }
}

@keyframes floatingIconPulse {
    0%, 100% {
        transform: scale(1) translateY(0);
        box-shadow: 0 4px 15px rgba(255, 255, 255, 0.2);
    }
    50% {
        transform: scale(1.1) translateY(-5px);
        box-shadow: 0 8px 25px rgba(255, 255, 255, 0.4);
    }
}

@keyframes cardShimmer {
    0% {
        background-position: -200% center;
    }
    100% {
        background-position: 200% center;
    }
}

@keyframes iconRotate {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

@keyframes buttonScan {
    0% {
        background-position: -200% center;
    }
    100% {
        background-position: 200% center;
    }
}
</style>
