<template>
    <div class="home-page">
        <!-- 英雄区域 -->
        <section class="hero-section">
            <div class="hero-content">
                <h1>"学伴" <span>校园互助平台</span></h1>
                <p class="hero-description">连接校园资源，互助共享，共同成长</p>
                <p class="hero-sub-description">让校园生活更便捷，让学习之路更精彩</p>
                <div class="cta-buttons">
                    <el-button type="primary" size="large" round @click="navigateTo('/helpinfo')">
                        <el-icon>
                            <Promotion />
                        </el-icon>浏览互助任务
                    </el-button>
                    <el-button size="large" round class="secondary-btn" @click="navigateTo('/helpinfo/publish')"
                        v-if="authStore.isAuthenticated">
                        <el-icon>
                            <Edit />
                        </el-icon>发布互助
                    </el-button>
                    <el-button size="large" round class="accent-btn" @click="navigateTo('/groups?tab=all')"
                        v-if="authStore.isAuthenticated">
                        <el-icon>
                            <UserFilled />
                        </el-icon>学习小组
                    </el-button>
                </div>
            </div>
            <div class="hero-image">
                <img src="../assets/vue.svg" alt="学伴" />
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
                    <div class="feature-icon">
                        <el-icon>
                            <Connection />
                        </el-icon>
                    </div>
                    <h3>互助合作</h3>
                    <p>发布求助、提供帮助，让校园互助更便捷</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon accent">
                        <el-icon>
                            <Trophy />
                        </el-icon>
                    </div>
                    <h3>信用积分</h3>
                    <p>完成互助任务获取积分，提升个人信用等级</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon secondary">
                        <el-icon>
                            <ChatLineRound />
                        </el-icon>
                    </div>
                    <h3>实时沟通</h3>
                    <p>便捷的沟通工具，使互助过程更高效</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon tertiary">
                        <el-icon>
                            <Reading />
                        </el-icon>
                    </div>
                    <h3>学习小组</h3>
                    <p>创建和加入学习小组，共同学习，共同进步</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon quaternary">
                        <el-icon>
                            <Star />
                        </el-icon>
                    </div>
                    <h3>评价反馈</h3>
                    <p>完善的评价系统，为优质互助提供保障</p>
                </div>
            </div>
        </section>

        <!-- 平台功能区域 -->
        <section class="modules-section">
            <h2 class="section-title">平台功能</h2>
            <el-row :gutter="30" class="module-row">
                <el-col :xs="24" :sm="12" :md="8">
                    <el-card class="module-card active" shadow="hover" @click="navigateTo('/helpinfo')">
                        <el-icon class="module-icon primary">
                            <Service />
                        </el-icon>
                        <h3>校园互助</h3>
                        <p>浏览和发布互助任务，包括课程辅导、技能交换、物品借用等</p>
                        <div class="card-action">
                            <el-button type="primary" text round>立即体验<el-icon>
                                    <ArrowRight />
                                </el-icon></el-button>
                        </div>
                    </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8">
                    <el-card class="module-card active" shadow="hover" @click="navigateTo('/groups?tab=all')">
                        <el-icon class="module-icon secondary">
                            <UserFilled />
                        </el-icon>
                        <h3>学习小组</h3>
                        <p>创建或加入学习小组，进行协作学习和讨论</p>
                        <div class="card-tag">
                            <el-tag size="small" effect="dark" type="success">新上线</el-tag>
                        </div>
                        <div class="card-action">
                            <el-button type="success" text round>立即体验<el-icon>
                                    <ArrowRight />
                                </el-icon></el-button>
                        </div>
                    </el-card>
                </el-col>
                <el-col :xs="24" :sm="12" :md="8">
                    <el-card class="module-card disabled" shadow="hover">
                        <el-icon class="module-icon accent">
                            <Files />
                        </el-icon>
                        <h3>资源共享</h3>
                        <p>分享和获取学习资料、二手物品等校园资源</p>
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
            <h2 class="section-title">使用指南</h2>
            <div class="guide-container">
                <div class="guide-step">
                    <div class="step-number">1</div>
                    <div class="step-icon"><el-icon><User /></el-icon></div>
                    <h3>注册账号</h3>
                    <p>简单注册，完善个人信息</p>
                </div>
                <div class="guide-arrow"><el-icon><ArrowRight /></el-icon></div>
                <div class="guide-step">
                    <div class="step-number">2</div>
                    <div class="step-icon"><el-icon><Promotion /></el-icon></div>
                    <h3>浏览信息</h3>
                    <p>查看互助任务或学习小组</p>
                </div>
                <div class="guide-arrow"><el-icon><ArrowRight /></el-icon></div>
                <div class="guide-step">
                    <div class="step-number">3</div>
                    <div class="step-icon"><el-icon><ChatDotRound /></el-icon></div>
                    <h3>互动交流</h3>
                    <p>申请帮助或加入小组</p>
                </div>
                <div class="guide-arrow"><el-icon><ArrowRight /></el-icon></div>
                <div class="guide-step">
                    <div class="step-number">4</div>
                    <div class="step-icon"><el-icon><Star /></el-icon></div>
                    <h3>评价反馈</h3>
                    <p>完成互助后进行评价</p>
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
}

/* 英雄区域样式 */
.hero-section {
    display: flex;
    align-items: center;
    padding: 50px 0;
    margin-bottom: 60px;
    background: linear-gradient(135deg, var(--primary-color) 0%, #2c65dd 100%);
    color: white;
    border-radius: 0 0 30px 30px;
    position: relative;
    overflow: hidden;
}

.hero-content {
    flex: 1;
    padding: 0 40px;
    max-width: 600px;
    z-index: 2;
}

.hero-content h1 {
    font-size: 2.8rem;
    margin-bottom: 20px;
    font-weight: 800;
    line-height: 1.2;
}

.hero-content h1 span {
    color: #ffeb3b;
    display: block;
}

.hero-description {
    font-size: 1.25rem;
    margin-bottom: 20px;
    opacity: 0.9;
    line-height: 1.6;
}

.hero-sub-description {
    font-size: 1.1rem;
    margin-bottom: 40px;
    opacity: 0.8;
    line-height: 1.5;
}

.hero-image {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1;
    position: relative;
}

.hero-image img {
    max-width: 100%;
    height: 220px;
    filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.3));
    animation: float 6s ease-in-out infinite;
}

.floating-icon {
    position: absolute;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    animation: float 6s ease-in-out infinite;
}

.floating-icon :deep(svg) {
    font-size: 24px;
    color: white;
}

.icon-1 {
    top: 20%;
    left: 10%;
    animation-delay: 0s;
}

.icon-2 {
    bottom: 15%;
    right: 15%;
    animation-delay: 2s;
}

.icon-3 {
    top: 30%;
    right: 20%;
    animation-delay: 4s;
}

.cta-buttons {
    display: flex;
    gap: 16px;
}

.secondary-btn {
    background-color: rgba(255, 255, 255, 0.2);
    color: white;
    border: none;
}

.secondary-btn:hover {
    background-color: rgba(255, 255, 255, 0.3);
}

.accent-btn {
    background-color: rgba(255, 255, 255, 0.2);
    color: white;
    border: none;
}

.accent-btn:hover {
    background-color: rgba(255, 255, 255, 0.3);
}

/* 平台特色区域 */
.features-section {
    padding: 30px 40px 60px;
    text-align: center;
    max-width: 1200px;
    margin: 0 auto;
}

.section-title {
    font-size: 2rem;
    color: var(--text-primary);
    margin-bottom: 50px;
    position: relative;
    display: inline-block;
}

.section-title::after {
    content: '';
    position: absolute;
    width: 60px;
    height: 4px;
    background-color: var(--primary-color);
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    border-radius: 2px;
}

.features-grid {
    display: flex;
    justify-content: center;
    gap: 30px;
    margin-bottom: 40px;
    flex-wrap: wrap;
}

.feature-card {
    background-color: var(--card-bg);
    border-radius: 16px;
    padding: 30px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
    flex: 1 1 300px;
    max-width: 350px;
    transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
}

.feature-icon {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    background-color: rgba(62, 123, 250, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
}

.feature-icon :deep(svg) {
    font-size: 32px;
    color: var(--primary-color);
}

.feature-icon.accent {
    background-color: rgba(255, 112, 112, 0.15);
}

.feature-icon.accent :deep(svg) {
    color: var(--accent-color);
}

.feature-icon.secondary {
    background-color: rgba(87, 199, 142, 0.15);
}

.feature-icon.secondary :deep(svg) {
    color: var(--secondary-color);
}

.feature-icon.tertiary {
    background-color: rgba(108, 92, 231, 0.15);
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
    max-width: 1200px;
    margin: 0 auto;
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
    max-width: 1200px;
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
@media (max-width: 992px) {
    .hero-section {
        flex-direction: column;
        padding: 40px 0;
    }

    .hero-content {
        text-align: center;
        margin-bottom: 30px;
        padding: 0 20px;
    }

    .cta-buttons {
        justify-content: center;
    }

    .features-grid {
        gap: 20px;
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

@media (max-width: 768px) {
    .hero-content h1 {
        font-size: 2.2rem;
    }

    .hero-description {
        font-size: 1.1rem;
    }

    .section-title {
        font-size: 1.8rem;
    }
}
</style>
