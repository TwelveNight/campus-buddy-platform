<template>
    <div class="reviews-page">
        <div class="reviews-header">
            <h1>我的评价<span>查看我的互助评价记录</span></h1>
        </div>

        <div class="reviews-container">
            <!-- 信用分概览卡片 -->
            <el-row :gutter="24" class="stat-row">
                <el-col :xs="24" :md="8">
                    <el-card class="stat-card" shadow="hover">
                        <div class="stat-content credit-stat">
                            <div class="stat-icon credit-icon">
                                <el-icon>
                                    <Trophy />
                                </el-icon>
                            </div>
                            <div class="stat-info">
                                <div class="stat-value">{{ creditStats.creditScore }}</div>
                                <div class="stat-label">信用积分 <span class="credit-level">({{ creditStats.creditLevel }})</span></div>
                                <div class="stat-progress">
                                    <el-progress :percentage="getCreditPercentage()" :stroke-width="8"
                                        :color="['#f56c6c', '#e6a23c', '#5cb87a', '#1989fa', '#6f7ad3']" />
                                </div>
                                <div class="trend-info">
                                    <el-tag
                                        :type="creditStats.trend.includes('上升') ? 'success' : creditStats.trend.includes('下降') ? 'danger' : 'info'"
                                        size="small" effect="light">
                                        <el-icon v-if="creditStats.trend.includes('上升')">
                                            <CaretTop />
                                        </el-icon>
                                        <el-icon v-else-if="creditStats.trend.includes('下降')">
                                            <CaretBottom />
                                        </el-icon>
                                        <el-icon v-else>
                                            <Minus />
                                        </el-icon>
                                        {{ creditStats.trend }}
                                    </el-tag>
                                </div>
                            </div>
                        </div>
                    </el-card>
                </el-col>

                <el-col :xs="24" :md="8">
                    <el-card class="stat-card" shadow="hover">
                        <div class="stat-content">
                            <div class="stat-icon reviews-icon">
                                <el-icon>
                                    <Star />
                                </el-icon>
                            </div>
                            <div class="stat-info">
                                <div class="stat-value">{{ totalReviews }}</div>
                                <div class="stat-label">总评价数</div>
                                <div class="stat-detail">累计收到和发出的评价总数</div>
                            </div>
                        </div>
                    </el-card>
                </el-col>

                <el-col :xs="24" :md="8">
                    <el-card class="stat-card" shadow="hover">
                        <div class="stat-content">
                            <div class="stat-icon score-icon">
                                <el-icon>
                                    <Medal />
                                </el-icon>
                            </div>
                            <div class="stat-info">
                                <div class="stat-value">{{ averageScore.toFixed(1) }}</div>
                                <div class="stat-label">平均评分</div>
                                <div class="stat-detail">
                                    <el-rate v-model="averageScoreRate" disabled :max="5"
                                        :colors="['#F7BA2A', '#F7BA2A', '#67C23A']" />
                                </div>
                            </div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>

            <!-- 评价列表卡片 -->
            <el-card class="reviews-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <div class="title">
                            <el-icon>
                                <Collection />
                            </el-icon>
                            <h2>评价列表</h2>
                        </div>
                        <div class="refresh-btn">
                            <el-button type="primary" plain size="small" :loading="loading" @click="refreshData">
                                <el-icon>
                                    <RefreshRight />
                                </el-icon>刷新
                            </el-button>
                        </div>
                    </div>
                </template>

                <div class="reviews-filter">
                    <div class="filter-tabs">
                        <el-radio-group v-model="activeTab" @change="handleTabChange" size="large">
                            <el-radio-button label="all">
                                <el-icon>
                                    <Document />
                                </el-icon>全部评价
                            </el-radio-button>
                            <el-radio-button label="received">
                                <el-icon>
                                    <ChatRound />
                                </el-icon>收到的评价
                            </el-radio-button>
                            <el-radio-button label="given">
                                <el-icon>
                                    <Star />
                                </el-icon>发出的评价
                            </el-radio-button>
                        </el-radio-group>
                    </div>

                    <div class="filter-actions">
                        <el-popover placement="bottom" :width="300" trigger="click">
                            <template #reference>
                                <el-button :icon="Filter" plain>筛选</el-button>
                            </template>
                            <div class="filter-popover">
                                <div class="filter-item">
                                    <span class="filter-label">模块类型：</span>
                                    <el-select v-model="moduleFilter" placeholder="选择模块类型" clearable
                                        @change="applyFilters" style="width: 100%">
                                        <el-option label="互助服务" value="HELP"></el-option>
                                        <el-option label="学习小组" value="GROUP"></el-option>
                                        <el-option label="资源共享" value="RESOURCE"></el-option>
                                    </el-select>
                                </div>
                                <div class="filter-item">
                                    <span class="filter-label">评分筛选：</span>
                                    <el-select v-model="scoreFilter" placeholder="选择评分" clearable @change="applyFilters"
                                        style="width: 100%">
                                        <el-option v-for="i in 5" :key="i" :label="`${i}分`" :value="i">
                                            <div style="display: flex; align-items: center;">
                                                <span style="margin-right: 8px;">{{ i }}分</span>
                                                <el-rate :model-value="i" disabled :max="5"
                                                    :colors="['#F7BA2A', '#F7BA2A', '#67C23A']" />
                                            </div>
                                        </el-option>
                                    </el-select>
                                </div>
                                <div class="filter-actions">
                                    <el-button size="small" @click="resetFilters">重置</el-button>
                                    <el-button size="small" type="primary" @click="applyFilters">应用筛选</el-button>
                                </div>
                            </div>
                        </el-popover>
                    </div>
                </div>

                <div class="active-filters" v-if="hasActiveFilters">
                    <el-tag v-if="moduleFilter" closable @close="moduleFilter = ''; applyFilters()" type="info"
                        effect="light" class="filter-tag">
                        模块类型: {{ getModuleLabel(moduleFilter) }}
                    </el-tag>
                    <el-tag v-if="scoreFilter" closable @close="scoreFilter = null; applyFilters()" type="warning"
                        effect="light" class="filter-tag">
                        评分: {{ scoreFilter }}分
                    </el-tag>
                </div>

                <div v-loading="loading" class="reviews-list-container">
                    <div v-if="!loading && displayedReviews.length === 0" class="no-reviews">
                        <el-empty :image-size="120" description="暂无符合条件的评价">
                            <template #description>
                                <p>没有找到符合当前筛选条件的评价</p>
                                <p class="empty-tip" v-if="hasActiveFilters">尝试调整筛选条件或清除筛选</p>
                            </template>
                        </el-empty>
                    </div>

                    <div v-else>
                        <ReviewList :reviews="displayedReviews.map(r => ({
                            reviewId: r.reviewId || 0,
                            reviewerUserId: r.reviewerUserId,
                            reviewerNickname: r.reviewerNickname,
                            reviewedUserId: r.reviewedUserId,
                            reviewedNickname: r.reviewedNickname,
                            reviewerAvatar: r.reviewerAvatar,
                            relatedInfoId: r.relatedInfoId,
                            relatedInfoTitle: r.relatedInfoTitle,
                            relatedInfoSummary: r.relatedInfoSummary,
                            moduleType: r.moduleType,
                            score: r.score,
                            content: r.content,
                            createdAt: r.createdAt || Date.now(),
                            reviewType: r.reviewType,
                        }))" :loading="loading" :showFilter="false" :targetUserId="currentUserId">
                            <template #reviewer-name="{ review }">
                                <router-link :to="`/user/${review.reviewerUserId}`" class="reviewer-name">
                                    {{ review.reviewerNickname || ('用户 #' + review.reviewerUserId) }}
                                </router-link>
                            </template>
                            <template #reviewed-name="{ review }">
                                <router-link :to="`/user/${review.reviewedUserId}`" class="reviewed-name">
                                    {{ review.reviewedNickname || ('用户 #' + review.reviewedUserId) }}
                                </router-link>
                            </template>
                        </ReviewList>

                        <div class="pagination" v-if="totalItems > 0">
                            <el-pagination background layout="prev, pager, next" :total="totalItems"
                                :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange" />
                        </div>
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
    Star, ChatRound, Document,
    Trophy, Medal, Collection, Filter, RefreshRight,
    CaretTop, CaretBottom, Minus
} from '@element-plus/icons-vue';
import ReviewList from '../../components/common/ReviewList.vue';
import { getUserReviews, getUserCreditStats } from '../../api/review';
import type { Review, ReviewQuery } from '../../api/review';
import { useAuthStore } from '../../store/auth';

// 用户认证和路由
const authStore = useAuthStore();
const route = useRoute();
const currentUserId = computed(() => authStore.user?.userId || 0);

// 页面状态
const loading = ref(false);
const activeTab = ref('all');
const moduleFilter = ref('');
const scoreFilter = ref<string | null>(null);
const currentPage = ref(1);
const pageSize = ref(10);
const reviews = ref<Review[]>([]);
const totalItems = ref(0);

// 信用分统计信息
interface CreditStats {
  creditScore: number;
  creditLevel: string;
  totalReviews: number;
  averageScore: number;
  recentReviews: number;
  recentAverageScore: number;
  trend: string;
}

const creditStats = ref<CreditStats>({
  creditScore: 60,
  creditLevel: '及格',
  totalReviews: 0,
  averageScore: 0,
  recentReviews: 0,
  recentAverageScore: 0,
  trend: '稳定'
});

// 统计数据 - 现在使用从服务端获取的统计信息
const totalReviews = computed(() => creditStats.value.totalReviews);
const averageScore = computed(() => creditStats.value.averageScore || 0);

// 筛选后的评价列表
const displayedReviews = computed(() => {
    let filtered = [...reviews.value];

    if (activeTab.value === 'received') {
        filtered = filtered.filter(review => review.reviewedUserId === currentUserId.value);
    } else if (activeTab.value === 'given') {
        filtered = filtered.filter(review => review.reviewerUserId === currentUserId.value);
    }

    if (moduleFilter.value) {
        filtered = filtered.filter(review => review.moduleType === moduleFilter.value);
    }

    if (scoreFilter.value && parseInt(scoreFilter.value)) {
        const scoreValue = parseInt(scoreFilter.value);
        filtered = filtered.filter(review => review.score === scoreValue);
    }

    return filtered;
});

// 计算是否有激活的筛选条件
const hasActiveFilters = computed(() => {
    return Boolean(moduleFilter.value || scoreFilter.value);
});

// 获取平均分的Rate显示值
const averageScoreRate = computed(() => averageScore.value || 0);

// 获取模块类型的显示名称
const getModuleLabel = (moduleType: string | undefined): string => {
    if (!moduleType) return '互助服务';
    const labelMap: Record<string, string> = {
        'HELP': '互助服务',
        'COURSE_TUTORING': '课程辅导',
        'SKILL_LEARNING': '技能学习',
        'ITEM_LEND': '物品借用',
        'GROUP': '学习小组',
        'RESOURCE': '资源共享'
    };
    return labelMap[moduleType] || '互助服务';
};

// 信用分百分比计算（假设最高分为100）
const getCreditPercentage = () => {
    return Math.min(Math.max(creditStats.value.creditScore, 0), 100);
};

// 刷新数据
const refreshData = () => {
    fetchReviews();
    fetchCreditScore();
};

// 重置筛选条件
const resetFilters = () => {
    moduleFilter.value = '';
    scoreFilter.value = null;
    applyFilters();
};

// 获取信用分统计信息
const fetchCreditScore = async () => {
    if (!currentUserId.value) return;
    try {
        const result = await getUserCreditStats(currentUserId.value);
        if (result && result.data) {
            creditStats.value = {
                creditScore: result.data.creditScore || 60,
                creditLevel: result.data.creditLevel || '及格',
                totalReviews: result.data.totalReviews || 0,
                averageScore: result.data.averageScore || 0,
                recentReviews: result.data.recentReviews || 0,
                recentAverageScore: result.data.recentAverageScore || 0,
                trend: result.data.trend || '稳定'
            };
            console.log('获取到的信用分统计信息:', creditStats.value);
        }
    } catch (error) {
        console.error('获取信用分统计信息失败:', error);
        ElMessage.warning('获取信用分统计信息失败');
    }
};

// 加载评价数据
const fetchReviews = async () => {
    if (!currentUserId.value) {
        ElMessage.warning('请先登录后再查看评价');
        return;
    }
    loading.value = true;
    try {
        const query: ReviewQuery = {
            userId: currentUserId.value,
            page: currentPage.value,
            size: pageSize.value
        };

        // 根据筛选条件添加参数
        if (activeTab.value === 'received') {
            query.type = 'received';
        } else if (activeTab.value === 'given') {
            query.type = 'given';
        }

        if (moduleFilter.value) {
            query.moduleType = moduleFilter.value;
        }

        if (scoreFilter.value && parseInt(scoreFilter.value)) {
            query.score = parseInt(scoreFilter.value);
        }

        const result = await getUserReviews(query);
        if (result && result.data) {
            if (Array.isArray(result.data)) {
                // 如果结果直接是数组
                reviews.value = result.data;
                totalItems.value = result.data.length;
            } else if (result.data.items) {
                // 如果结果是分页对象
                reviews.value = result.data.items || [];
                totalItems.value = result.data.total || 0;
            } else {
                reviews.value = [];
                totalItems.value = 0;
            }
        } else {
            reviews.value = [];
            totalItems.value = 0;
        }
    } catch (error) {
        console.error('加载评价数据失败:', error);
        ElMessage.error('加载评价数据失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};

// 切换标签页
const handleTabChange = () => {
    currentPage.value = 1;
    fetchReviews();
};

// 应用筛选
const applyFilters = () => {
    currentPage.value = 1;
    fetchReviews();
};

// 分页变化
const handlePageChange = (page: number) => {
    currentPage.value = page;
    fetchReviews();
};

// 初始化加载
onMounted(() => {
    // 检查路由查询参数，设置初始标签页
    const queryType = route.query.type as string;
    if (queryType === 'received') {
        activeTab.value = 'received';
    } else if (queryType === 'given') {
        activeTab.value = 'given';
    } else {
        activeTab.value = 'all';
    }
    
    fetchReviews();
    fetchCreditScore();
});
</script>

<style scoped>
.reviews-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px 60px;
}

.reviews-header {
    text-align: center;
    margin: 30px 0 40px;
}

.reviews-header h1 {
    font-size: 2.2rem;
    color: var(--text-primary);
    font-weight: 700;
    margin: 0;
}

.reviews-header h1 span {
    display: block;
    font-size: 1rem;
    color: var(--text-secondary);
    font-weight: 400;
    margin-top: 8px;
}

.reviews-container {
    max-width: 1200px;
    margin: 0 auto;
}

.stat-row {
    margin-bottom: 24px;
}

.stat-card {
    height: 100%;
    border-radius: 16px;
    overflow: hidden;
    transition: transform 0.3s, box-shadow 0.3s;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.stat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
    display: flex;
    padding: 20px;
    height: 100%;
}

.stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    border-radius: 16px;
    margin-right: 16px;
}

.credit-icon {
    background-color: rgba(111, 122, 211, 0.1);
    color: #6f7ad3;
}

.reviews-icon {
    background-color: rgba(247, 186, 42, 0.1);
    color: #F7BA2A;
}

.score-icon {
    background-color: rgba(103, 194, 58, 0.1);
    color: #67C23A;
}

.stat-icon :deep(svg) {
    font-size: 36px;
}

.stat-info {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.stat-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 4px;
}

.stat-label {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 8px;
}

.credit-level {
    font-size: 12px;
    color: #6f7ad3;
    font-weight: 600;
}

.trend-info {
    margin-top: 8px;
}

.trend-info :deep(.el-tag) {
    display: flex;
    align-items: center;
    gap: 4px;
}

.stat-detail {
    font-size: 13px;
    margin-top: 4px;
}

.stat-progress {
    margin-top: 8px;
}

.reviews-card {
    border-radius: 16px;
    margin-bottom: 24px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    transition: transform 0.3s, box-shadow 0.3s;
}

.reviews-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.title {
    display: flex;
    align-items: center;
    gap: 12px;
}

.title h2 {
    margin: 0;
    font-size: 1.4rem;
    font-weight: 500;
}

.title :deep(svg) {
    font-size: 18px;
    color: var(--primary-color);
}

.reviews-filter {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    margin-bottom: 20px;
    gap: 16px;
}

.filter-tabs {
    margin-right: auto;
}

.filter-actions {
    display: flex;
    gap: 12px;
}

.filter-popover {
    padding: 12px;
}

.filter-item {
    margin-bottom: 16px;
}

.filter-label {
    display: block;
    font-size: 14px;
    margin-bottom: 8px;
    color: var(--text-primary);
}

.active-filters {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
}

.filter-tag {
    display: flex;
    align-items: center;
    padding: 0 10px;
    height: 28px;
    border-radius: 14px;
    font-size: 12px;
}

.reviews-list-container {
    min-height: 200px;
}

.no-reviews {
    padding: 40px 0;
}

.empty-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
}

.pagination {
    margin-top: 32px;
    display: flex;
    justify-content: center;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
    .reviews-filter {
        flex-direction: column;
        align-items: stretch;
    }

    .filter-tabs {
        width: 100%;
        overflow-x: auto;
    }
}

/* 暗色模式适配 */
[data-theme="dark"] .reviews-page {
    background-color: var(--dark-bg);
    color: var(--dark-text-primary);
}

[data-theme="dark"] .reviews-header h1 {
    color: var(--dark-text-primary);
}

[data-theme="dark"] .reviews-header h1 span {
    color: var(--dark-text-secondary);
}

[data-theme="dark"] .stat-card {
    background-color: var(--dark-card-bg);
    border-color: var(--dark-border-color);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .stat-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .stat-value {
    color: var(--dark-text-primary);
}

[data-theme="dark"] .stat-label,
[data-theme="dark"] .stat-detail {
    color: var(--dark-text-secondary);
}

[data-theme="dark"] .reviews-card {
    background-color: var(--dark-card-bg);
    border-color: var(--dark-border-color);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .reviews-card:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .title h2 {
    color: var(--dark-text-primary);
}

[data-theme="dark"] .title :deep(svg) {
    color: var(--primary-color-dark);
}

[data-theme="dark"] .filter-label {
    color: var(--dark-text-secondary);
}

[data-theme="dark"] .empty-tip {
    color: var(--dark-text-secondary);
}

[data-theme="dark"] :deep(.el-radio-button__inner) {
    background-color: var(--dark-bg-secondary);
    border-color: var(--dark-border-color);
    color: var(--dark-text-secondary);
}

[data-theme="dark"] :deep(.el-radio-button__orig-radio:checked+.el-radio-button__inner) {
    background-color: var(--primary-color-dark);
    border-color: var(--primary-color-dark);
    color: var(--dark-text-on-primary);
}

[data-theme="dark"] :deep(.el-button--primary) {
    background-color: var(--primary-color-dark);
    border-color: var(--primary-color-dark);
}

[data-theme="dark"] :deep(.el-button--primary.is-plain) {
    background-color: transparent;
    border-color: var(--primary-color-dark);
    color: var(--primary-color-dark);
}

[data-theme="dark"] :deep(.el-button--primary.is-plain:hover) {
    background-color: var(--primary-color-dark);
    color: var(--dark-text-on-primary);
}

[data-theme="dark"] :deep(.el-input__inner) {
    background-color: var(--dark-input-bg);
    border-color: var(--dark-border-color);
    color: var(--dark-text-primary);
}

[data-theme="dark"] :deep(.el-input__inner:focus) {
    border-color: var(--primary-color-dark);
}

[data-theme="dark"] :deep(.el-select-dropdown) {
    background-color: var(--dark-card-bg);
    border-color: var(--dark-border-color);
}

[data-theme="dark"] :deep(.el-select-dropdown__item) {
    color: var(--dark-text-primary);
}

[data-theme="dark"] :deep(.el-select-dropdown__item.hover, .el-select-dropdown__item:hover) {
    background-color: var(--dark-bg-hover);
}

[data-theme="dark"] :deep(.el-popover) {
    background-color: var(--dark-card-bg);
    border-color: var(--dark-border-color);
    color: var(--dark-text-primary);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
    .reviews-filter {
        flex-direction: column;
        align-items: flex-start;
    }

    .filter-tabs {
        width: 100%;
        margin-bottom: 12px;
    }

    .reviews-filter .el-radio-group {
        width: 100%;
        display: flex;
    }

    .reviews-filter .el-radio-button {
        flex: 1;
    }

    .filter-actions {
        width: 100%;
        justify-content: flex-end;
    }
}
</style>
