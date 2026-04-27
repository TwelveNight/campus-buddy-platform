<template>
    <div class="home-page">
        <section class="hero-section">
            <div class="hero-copy">
                <div class="eyebrow">Campus Mutual Aid Platform</div>
                <h1>学伴</h1>
                <p class="hero-title-sub">校园互助与资源共享平台</p>
                <p class="hero-description">
                    面向校园学习与生活场景，连接课程辅导、技能学习、物品共享和学习小组协作，让帮助更容易被看见。
                </p>
                <div class="cta-buttons">
                    <el-button type="primary" size="large" round @click="navigateTo('/helpinfo')">
                        <el-icon><Promotion /></el-icon>
                        浏览互助信息
                    </el-button>
                    <el-button v-if="authStore.isAuthenticated" size="large" round class="ghost-btn"
                        @click="navigateTo('/helpinfo/publish')">
                        <el-icon><Edit /></el-icon>
                        发布互助
                    </el-button>
                    <el-button v-else size="large" round class="ghost-btn" @click="navigateTo('/login')">
                        <el-icon><User /></el-icon>
                        登录使用
                    </el-button>
                </div>
            </div>

            <div class="hero-panel">
                <div class="panel-card primary-card">
                    <span>当前入口</span>
                    <strong>互助信息</strong>
                    <p>课程辅导、技能学习、物品借用、组队协作</p>
                </div>
                <div class="panel-grid">
                    <div class="mini-card">
                        <el-icon><UserFilled /></el-icon>
                        <span>学习小组</span>
                    </div>
                    <div class="mini-card">
                        <el-icon><ChatLineRound /></el-icon>
                        <span>私信沟通</span>
                    </div>
                    <div class="mini-card">
                        <el-icon><Star /></el-icon>
                        <span>信用评价</span>
                    </div>
                    <div class="mini-card">
                        <el-icon><Files /></el-icon>
                        <span>资源文件</span>
                    </div>
                </div>
            </div>
        </section>

        <section class="quick-actions">
            <button class="action-tile" @click="navigateTo('/helpinfo')">
                <el-icon><Service /></el-icon>
                <span>找互助</span>
                <small>按类型筛选校园帮助</small>
            </button>
            <button class="action-tile" @click="navigateTo('/helpinfo/publish')">
                <el-icon><Promotion /></el-icon>
                <span>发需求</span>
                <small>发布任务等待申请</small>
            </button>
            <button class="action-tile" @click="navigateTo('/groups?tab=all')">
                <el-icon><Reading /></el-icon>
                <span>进小组</span>
                <small>学习协作与资料共享</small>
            </button>
        </section>

        <section class="section-block">
            <div class="section-heading">
                <span>Core Modules</span>
                <h2>围绕校园互助的核心能力</h2>
            </div>
            <div class="feature-grid">
                <article class="feature-card">
                    <div class="feature-icon blue"><el-icon><Connection /></el-icon></div>
                    <h3>互助发布与申请</h3>
                    <p>支持课程辅导、技能学习、物品借用、物品交换和组队需求，形成从发布到申请处理的闭环。</p>
                </article>
                <article class="feature-card">
                    <div class="feature-icon green"><el-icon><UserFilled /></el-icon></div>
                    <h3>学习小组协作</h3>
                    <p>用户可创建或加入学习小组，围绕帖子、评论和文件开展资料共享与主题讨论。</p>
                </article>
                <article class="feature-card">
                    <div class="feature-icon amber"><el-icon><ChatLineRound /></el-icon></div>
                    <h3>消息通知联动</h3>
                    <p>好友、私信和通知中心贯穿互助流程，让申请状态、沟通反馈和系统提醒及时触达。</p>
                </article>
            </div>
        </section>

        <section class="workflow-section">
            <div class="section-heading compact">
                <span>Workflow</span>
                <h2>四步完成一次校园互助</h2>
            </div>
            <div class="workflow">
                <div class="workflow-step">
                    <strong>01</strong>
                    <h3>发布需求</h3>
                    <p>填写类型、地点、时间和联系方式。</p>
                </div>
                <div class="workflow-step">
                    <strong>02</strong>
                    <h3>申请处理</h3>
                    <p>查看申请人信息并选择合适同学。</p>
                </div>
                <div class="workflow-step">
                    <strong>03</strong>
                    <h3>私信协作</h3>
                    <p>通过站内消息沟通细节并推进完成。</p>
                </div>
                <div class="workflow-step">
                    <strong>04</strong>
                    <h3>评价沉淀</h3>
                    <p>完成后互评，积累信用记录。</p>
                </div>
            </div>
        </section>

        <section class="stats-section">
            <div class="stat-item">
                <strong>5</strong>
                <span>互助类型</span>
            </div>
            <div class="stat-item">
                <strong>4</strong>
                <span>协作场景</span>
            </div>
            <div class="stat-item">
                <strong>1</strong>
                <span>统一平台</span>
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
    Promotion,
    Edit,
    Star,
    Reading,
    User
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

function navigateTo(path: string) {
    router.push(path)
}
</script>

<style scoped>
.home-page {
    --home-ink: #132238;
    --home-muted: #637083;
    --home-line: rgba(19, 34, 56, 0.1);
    --home-blue: #2563eb;
    --home-green: #0f9f6e;
    --home-amber: #d97706;
    width: 100%;
    padding: 28px 0 56px;
}

.hero-section {
    position: relative;
    display: grid;
    grid-template-columns: minmax(0, 1.08fr) minmax(360px, 0.92fr);
    gap: 44px;
    min-height: 470px;
    padding: 64px;
    overflow: hidden;
    border-radius: 34px;
    color: #fff;
    background:
        radial-gradient(circle at 18% 18%, rgba(255, 255, 255, 0.28), transparent 28%),
        radial-gradient(circle at 80% 20%, rgba(45, 212, 191, 0.3), transparent 26%),
        linear-gradient(135deg, #0f766e 0%, #155e75 45%, #1e3a8a 100%);
    box-shadow: 0 24px 70px rgba(15, 76, 117, 0.22);
}

.hero-section::after {
    content: '';
    position: absolute;
    inset: auto -8% -38% 42%;
    height: 360px;
    background: repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.12) 0 1px, transparent 1px 42px);
    transform: rotate(-8deg);
    opacity: 0.65;
}

.hero-copy,
.hero-panel {
    position: relative;
    z-index: 1;
}

.hero-copy {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.eyebrow {
    width: fit-content;
    margin-bottom: 18px;
    padding: 7px 14px;
    border: 1px solid rgba(255, 255, 255, 0.34);
    border-radius: 999px;
    color: rgba(255, 255, 255, 0.86);
    font-size: 13px;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    background: rgba(255, 255, 255, 0.12);
}

.hero-copy h1 {
    max-width: 680px;
    margin: 0;
    font-size: clamp(2.5rem, 6vw, 4.9rem);
    line-height: 1.02;
    letter-spacing: -0.06em;
    font-weight: 900;
}

.hero-title-sub {
    margin: 16px 0 0;
    color: rgba(255, 255, 255, 0.92);
    font-size: clamp(1.35rem, 3vw, 2.2rem);
    font-weight: 800;
    letter-spacing: -0.03em;
}

.hero-description {
    max-width: 640px;
    margin: 24px 0 34px;
    color: rgba(255, 255, 255, 0.84);
    font-size: 1.12rem;
    line-height: 1.9;
}

.cta-buttons {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
}

.cta-buttons :deep(.el-button) {
    height: 46px;
    padding: 0 24px;
    font-weight: 700;
}

.ghost-btn {
    color: #fff !important;
    border-color: rgba(255, 255, 255, 0.42) !important;
    background: rgba(255, 255, 255, 0.12) !important;
}

.hero-panel {
    align-self: center;
    padding: 18px;
    border: 1px solid rgba(255, 255, 255, 0.22);
    border-radius: 28px;
    background: rgba(255, 255, 255, 0.12);
    backdrop-filter: blur(18px);
}

.panel-card {
    border-radius: 24px;
    padding: 28px;
    background: #fff;
    color: var(--home-ink);
    box-shadow: 0 18px 45px rgba(8, 47, 73, 0.22);
}

.panel-card span {
    color: var(--home-muted);
    font-size: 13px;
}

.panel-card strong {
    display: block;
    margin: 8px 0;
    font-size: 2rem;
}

.panel-card p {
    margin: 0;
    color: var(--home-muted);
    line-height: 1.7;
}

.panel-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-top: 14px;
}

.mini-card {
    display: flex;
    align-items: center;
    gap: 10px;
    min-height: 72px;
    padding: 16px;
    border-radius: 18px;
    color: #fff;
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.18);
    font-weight: 700;
}

.mini-card .el-icon {
    font-size: 22px;
}

.quick-actions {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 18px;
    margin: -36px 40px 68px;
    position: relative;
    z-index: 2;
}

.action-tile {
    display: grid;
    grid-template-columns: auto 1fr;
    grid-template-rows: auto auto;
    column-gap: 14px;
    row-gap: 4px;
    padding: 22px;
    border: 1px solid var(--home-line);
    border-radius: 22px;
    text-align: left;
    cursor: pointer;
    background: var(--card-bg, #fff);
    box-shadow: 0 14px 36px rgba(15, 23, 42, 0.08);
    transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.action-tile:hover {
    transform: translateY(-4px);
    border-color: rgba(37, 99, 235, 0.28);
    box-shadow: 0 20px 48px rgba(15, 23, 42, 0.12);
}

.action-tile .el-icon {
    grid-row: span 2;
    align-self: center;
    width: 46px;
    height: 46px;
    border-radius: 15px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    background: linear-gradient(135deg, #0f766e, #2563eb);
    font-size: 22px;
}

.action-tile span {
    color: var(--text-primary);
    font-size: 1.1rem;
    font-weight: 800;
}

.action-tile small {
    color: var(--text-secondary);
    font-size: 0.92rem;
}

.section-block,
.workflow-section {
    margin-bottom: 68px;
}

.section-heading {
    margin-bottom: 28px;
    text-align: center;
}

.section-heading span {
    color: var(--home-green);
    font-size: 12px;
    font-weight: 800;
    letter-spacing: 0.16em;
    text-transform: uppercase;
}

.section-heading h2 {
    margin: 8px 0 0;
    color: var(--text-primary);
    font-size: clamp(1.8rem, 4vw, 2.8rem);
    letter-spacing: -0.04em;
}

.feature-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 22px;
}

.feature-card {
    min-height: 245px;
    padding: 30px;
    border: 1px solid var(--home-line);
    border-radius: 28px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.86));
    box-shadow: 0 16px 42px rgba(15, 23, 42, 0.07);
}

.feature-icon {
    width: 54px;
    height: 54px;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 18px;
    color: #fff;
    font-size: 26px;
}

.feature-icon.blue {
    background: linear-gradient(135deg, #2563eb, #0ea5e9);
}

.feature-icon.green {
    background: linear-gradient(135deg, #0f9f6e, #14b8a6);
}

.feature-icon.amber {
    background: linear-gradient(135deg, #d97706, #f59e0b);
}

.feature-card h3 {
    margin: 0 0 12px;
    color: var(--text-primary);
    font-size: 1.26rem;
}

.feature-card p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.85;
}

.workflow-section {
    padding: 42px;
    border-radius: 30px;
    background:
        linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(37, 99, 235, 0.08)),
        var(--card-bg, #fff);
}

.workflow {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
}

.workflow-step {
    padding: 24px;
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(15, 118, 110, 0.14);
}

.workflow-step strong {
    color: var(--home-blue);
    font-size: 1.8rem;
}

.workflow-step h3 {
    margin: 10px 0 8px;
    color: var(--text-primary);
}

.workflow-step p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.7;
}

.stats-section {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 18px;
}

.stat-item {
    padding: 28px;
    border-radius: 24px;
    text-align: center;
    color: #fff;
    background: linear-gradient(135deg, #0f766e, #1e3a8a);
}

.stat-item strong {
    display: block;
    font-size: 3rem;
    line-height: 1;
}

.stat-item span {
    display: block;
    margin-top: 10px;
    opacity: 0.86;
    font-weight: 700;
}

[data-theme="dark"] .feature-card,
[data-theme="dark"] .action-tile,
[data-theme="dark"] .workflow-step {
    background: rgba(36, 41, 61, 0.92);
    border-color: rgba(255, 255, 255, 0.08);
}

[data-theme="dark"] .workflow-section {
    background: linear-gradient(135deg, rgba(15, 118, 110, 0.16), rgba(37, 99, 235, 0.12));
}

@media (max-width: 992px) {
    .hero-section {
        grid-template-columns: 1fr;
        padding: 46px 28px;
    }

    .quick-actions,
    .feature-grid,
    .workflow,
    .stats-section {
        grid-template-columns: 1fr;
    }

    .quick-actions {
        margin: -28px 18px 52px;
    }
}

@media (max-width: 640px) {
    .home-page {
        padding-top: 12px;
    }

    .hero-section {
        border-radius: 24px;
        min-height: auto;
    }

    .hero-description {
        font-size: 1rem;
    }

    .panel-grid {
        grid-template-columns: 1fr;
    }

    .workflow-section {
        padding: 26px 18px;
    }
}
</style>
