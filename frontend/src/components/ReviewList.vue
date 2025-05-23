<template>
    <div class="review-list-component">
        <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="reviews.length === 0" class="empty-reviews">
            <el-empty description="暂无评价" :image-size="100" />
        </div>
        <div v-else>
            <div class="review-filter" v-if="showFilter">
                <el-select v-model="filterType" placeholder="评价类型" clearable @change="handleFilterChange">
                    <el-option label="我收到的评价" value="received" />
                    <el-option label="我发出的评价" value="given" />
                </el-select>
                <el-rate v-model="filterScore" :colors="scoreColors" :texts="scoreTexts" show-text
                    text-color="#909399" />
            </div>
            <div class="review-list">
                <div v-for="review in displayedReviews" :key="review.reviewId" class="review-item">
                    <div class="review-header">
                        <div class="reviewer-info">
                            <el-avatar :size="42" :src="review.reviewerAvatar || defaultAvatar"></el-avatar>
                            <div class="reviewer-details">
                                <div class="name-role-row">
                                    <span class="reviewer-name">{{ review.reviewerNickname || ('用户 #' +
                                        review.reviewerUserId)
                                    }}</span>
                                    <div class="role-badge"
                                        :class="getRoleClass(review.reviewType, review.reviewerUserId)">
                                        <el-tooltip :content="getRoleTooltip(review.reviewType, review.reviewerUserId)"
                                            placement="top" effect="light">
                                            <el-tag size="small" effect="dark"
                                                :type="getUserRoleType(review.reviewType, review.reviewerUserId)"
                                                class="user-role-tag">
                                                <span class="role-icon">{{ getRoleIcon(review.reviewType,
                                                    review.reviewerUserId)
                                                }}</span>
                                                {{ getUserRoleLabel(review.reviewType, review.reviewerUserId) }}
                                            </el-tag>
                                        </el-tooltip>
                                        <span class="pulse-dot" v-if="review.reviewerUserId === currentUserId"></span>
                                    </div>
                                </div>
                                <div class="review-module" :class="getModuleClass(review.moduleType)">
                                    <el-tag size="small" effect="light" :type="getModuleTagType(review.moduleType)">
                                        {{ getModuleLabel(review.moduleType) }}
                                    </el-tag>
                                </div>
                            </div>
                        </div>
                        <div class="review-meta">
                            <div class="review-date">
                                <el-icon>
                                    <Clock />
                                </el-icon>
                                {{ formatDate(review.createdAt) }}
                            </div>
                            <div class="review-score">
                                <div class="score-indicator">
                                    <div class="score-value">{{ review.score }}<span class="score-max">/5</span></div>
                                    <div class="score-stars">
                                        <el-rate v-model="review.score" disabled :max="5"
                                            :colors="['#F7BA2A', '#F7BA2A', '#67C23A']" />
                                    </div>
                                    <div class="score-label">{{ getScoreLabel(review.score) }}</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="review-content-wrap">
                        <div class="review-content-quote-mark left"><el-icon>
                                <ChatRound />
                            </el-icon></div>
                        <div class="review-content-quote-mark right"><el-icon>
                                <ChatRound />
                            </el-icon></div>
                        <div class="review-content" v-if="review.content">
                            {{ review.content }}
                        </div>
                        <div class="review-content-empty" v-else>
                            <el-icon>
                                <Document />
                            </el-icon>
                            <span>评价者未填写具体评价内容</span>
                        </div>
                    </div>

                    <div class="review-footer">
                        <div class="review-info">
                            <div class="related-task">
                                <el-tooltip v-if="review.relatedInfoSummary" :content="review.relatedInfoSummary"
                                    placement="top-start" effect="light">
                                    <span class="related-info">
                                        <el-icon>
                                            <Link />
                                        </el-icon>
                                        <span class="related-title">{{ review.relatedInfoTitle || '互助信息' }}</span>
                                    </span>
                                </el-tooltip>
                                <span v-else class="related-info">
                                    <el-icon>
                                        <Link />
                                    </el-icon>
                                    <span class="related-title">{{ review.relatedInfoTitle || '互助信息' }}</span>
                                </span>
                            </div>
                            <div class="action-buttons">
                                <router-link :to="getRelatedLink(review)" v-if="review.relatedInfoId"
                                    class="detail-link">
                                    <el-icon>
                                        <View />
                                    </el-icon>
                                    查看详情
                                </router-link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="pagination" v-if="reviews.length > pageSize">
                <el-pagination background layout="prev, pager, next" :total="reviews.length" :page-size="pageSize"
                    :current-page="currentPage" @current-change="handlePageChange" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, defineProps, defineEmits } from 'vue';
import { Clock, ChatRound, Document, Link, View } from '@element-plus/icons-vue';
import { useAuthStore } from '../store/auth';

// 定义评价对象类型
interface ReviewItem {
    reviewId: number;
    reviewedUserId: number;
    reviewerUserId: number;
    reviewerNickname?: string;
    reviewerAvatar?: string;
    relatedInfoId: number;
    moduleType?: string; // 例如：'互助', '学习小组', '资源共享'
    helpInfo?: any;
    score: number;
    content?: string;
    createdAt: string | number;
    reviewType?: string; // PUBLISHER_TO_HELPER 或 HELPER_TO_PUBLISHER
    relatedInfoTitle?: string; // 相关互助信息标题
    relatedInfoSummary?: string; // 相关互助信息摘要
}

const props = defineProps({
    reviews: {
        type: Array as () => ReviewItem[],
        required: true
    },
    loading: {
        type: Boolean,
        default: false
    },
    showFilter: {
        type: Boolean,
        default: true
    },
    showActionButtons: {
        type: Boolean,
        default: true
    }
});

const emit = defineEmits(['filter']);

const authStore = useAuthStore();
const currentUserId = computed(() => authStore.user?.userId || 0);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 分页相关
const currentPage = ref(1);
const pageSize = ref(5);
const displayedReviews = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    return filteredReviews.value.slice(start, start + pageSize.value);
});

// 筛选相关
const filterType = ref('');
const filterScore = ref(0);

const scoreColors = ['#99A9BF', '#F7BA2A', '#FF9900'];
const scoreTexts = ['很差', '较差', '一般', '满意', '非常满意'];

const filteredReviews = computed(() => {
    let result = props.reviews;
    if (filterType.value === 'received') {
        result = result.filter(r => r.reviewedUserId === currentUserId.value);
    } else if (filterType.value === 'given') {
        result = result.filter(r => r.reviewerUserId === currentUserId.value);
    }
    if (filterScore.value > 0) {
        result = result.filter(r => r.score === filterScore.value);
    }
    return result;
});

function handleFilterChange() {
    currentPage.value = 1;
    emit('filter', { type: filterType.value, score: filterScore.value });
}

function formatDate(date: string | number) {
    if (!date) return '';
    const d = new Date(date);
    return d.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 获取模块类型对应的标签类型
function getModuleTagType(moduleType: string | undefined): string {
    if (!moduleType) return '';
    const typeMap: Record<string, string> = {
        'HELP': 'success',
        'COURSE_TUTORING': 'success',
        'SKILL_LEARNING': 'warning',
        'ITEM_LEND': 'info',
        'GROUP': 'primary',
        'RESOURCE': 'danger'
    };
    return typeMap[moduleType] || 'info';
}

// 获取模块类型的显示名称
function getModuleLabel(moduleType: string | undefined): string {
    if (!moduleType) return '互助服务';
    const labelMap: Record<string, string> = {
        'HELP': '互助服务',
        'GROUP': '学习小组',
        'RESOURCE': '资源共享',
        // 为了兼容旧数据，仍然保留映射但不在选项中显示
        'COURSE_TUTORING': '课程辅导',
        'SKILL_LEARNING': '技能学习',
        'ITEM_LEND': '物品借用'
    };
    return labelMap[moduleType] || '互助服务';
}

// 获取模块类型的CSS类名
function getModuleClass(moduleType: string | undefined): string {
    if (!moduleType) return 'module-help';
    const classMap: Record<string, string> = {
        'HELP': 'module-help',
        'COURSE_TUTORING': 'module-course',
        'SKILL_LEARNING': 'module-skill',
        'ITEM_LEND': 'module-item',
        'GROUP': 'module-group',
        'RESOURCE': 'module-resource'
    };
    return classMap[moduleType] || 'module-help';
}

// 根据评分获取评分文字描述
function getScoreLabel(score: number): string {
    if (score >= 5) return '非常满意';
    if (score >= 4) return '满意';
    if (score >= 3) return '一般';
    if (score >= 2) return '不满意';
    return '很差';
}

function getRelatedLink(review: ReviewItem) {
    if (review.relatedInfoId) {
        // 无论模块类型如何，只要有相关信息ID，就可以跳转到详情页面
        return `/helpinfo/${review.relatedInfoId}`;
    }
    return '';
}

// 获取用户角色类型（标签样式）
function getUserRoleType(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return 'info';
    // 判断当前用户是发布者还是帮助者
    const currentUserId = useAuthStore().user?.userId;

    if (reviewType === 'PUBLISHER_TO_HELPER') {
        // 如果是发布者评价帮助者
        return reviewerUserId === currentUserId ? 'primary' : 'success';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        // 如果是帮助者评价发布者
        return reviewerUserId === currentUserId ? 'success' : 'primary';
    }

    return 'info';
}

// 获取用户角色标签文本
function getUserRoleLabel(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return '用户';

    const currentUserId = useAuthStore().user?.userId;

    if (reviewType === 'PUBLISHER_TO_HELPER') {
        // 发布者评价帮助者的场景
        return reviewerUserId === currentUserId ? '求助方' : '帮助方';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        // 帮助者评价发布者的场景
        return reviewerUserId === currentUserId ? '帮助方' : '求助方';
    }

    return '用户';
}

// 获取用户角色的CSS类名
function getRoleClass(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return 'role-default';
    const currentUserId = useAuthStore().user?.userId;

    if (reviewType === 'PUBLISHER_TO_HELPER') {
        return reviewerUserId === currentUserId ? 'role-publisher' : 'role-helper';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        return reviewerUserId === currentUserId ? 'role-helper' : 'role-publisher';
    }

    return 'role-default';
}

// 获取用户角色的图标
function getRoleIcon(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return '👤';
    const currentUserId = useAuthStore().user?.userId;

    if (reviewType === 'PUBLISHER_TO_HELPER') {
        return reviewerUserId === currentUserId ? '📢' : '🤝';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        return reviewerUserId === currentUserId ? '🤝' : '📢';
    }

    return '👤';
}

// 获取用户角色的提示信息
function getRoleTooltip(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return '用户角色';
    const currentUserId = useAuthStore().user?.userId;

    if (reviewType === 'PUBLISHER_TO_HELPER') {
        return reviewerUserId === currentUserId ? '您是求助方' : '对方是帮助方';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        return reviewerUserId === currentUserId ? '您是帮助方' : '对方是求助方';
    }

    return '用户角色';
}

function handlePageChange(page: number) {
    currentPage.value = page;
}

</script>

<style scoped>
.review-list-component {
    width: 100%;
    margin: 0 auto;
}

.review-list {
    display: flex;
    flex-direction: column;
    gap: 22px;
    margin-bottom: 20px;
}

.review-item {
    padding: 24px;
    border-radius: 12px;
    background-color: #fff;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    position: relative;
    overflow: visible;
    display: flex;
    flex-direction: column;
    min-height: 200px;
    /* 设置最小高度，防止内容变化导致跳动 */
}

.review-item::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    width: 4px;
    background: var(--el-color-primary);
    opacity: 0.8;
}

.review-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.review-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 18px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f2f5;
}

.reviewer-info {
    display: flex;
    align-items: center;
    gap: 14px;
}

.reviewer-details {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.reviewer-name {
    font-weight: 600;
    color: var(--el-text-color-primary);
    font-size: 1.05rem;
    line-height: 1.2;
}

.name-role-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
}

.review-module {
    display: flex;
    align-items: center;
}

.review-module .el-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 0.8rem;
    padding: 0 8px;
    height: 22px;
    border-radius: 4px;
}

.review-meta {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
}

.review-date {
    font-size: 0.85rem;
    color: #909399;
    background-color: #f5f7fa;
    padding: 3px 10px;
    border-radius: 15px;
    display: flex;
    align-items: center;
    gap: 5px;
}

.review-date :deep(svg) {
    font-size: 14px;
}

.score-indicator {
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: rgba(255, 153, 0, 0.05);
    border-radius: 8px;
    padding: 8px 12px;
    border: 1px solid rgba(255, 153, 0, 0.1);
}

.score-value {
    font-size: 1.4rem;
    font-weight: 700;
    color: #ff9900;
    line-height: 1;
    margin-bottom: 4px;
}

.score-max {
    font-size: 0.85rem;
    font-weight: normal;
    color: #c0c4cc;
    margin-left: 1px;
}

.score-stars {
    margin-bottom: 4px;
}

.score-label {
    font-size: 0.75rem;
    font-weight: 500;
    color: #909399;
    background-color: #f5f7fa;
    padding: 2px 8px;
    border-radius: 10px;
}

.review-content-wrap {
    position: relative;
    padding: 20px;
    background-color: rgba(64, 158, 255, 0.05);
    border-radius: 8px;
    margin: 10px 0 16px;
}

.review-content-quote-mark {
    position: absolute;
    color: rgba(64, 158, 255, 0.2);
    font-size: 18px;
}

.review-content-quote-mark.left {
    top: 5px;
    left: 8px;
}

.review-content-quote-mark.right {
    bottom: 5px;
    right: 8px;
}

.review-content {
    font-size: 1.05rem;
    color: var(--el-text-color-primary);
    line-height: 1.6;
    text-align: justify;
    padding: 0 16px;
    word-break: break-word;
}

.review-content-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-style: italic;
    gap: 8px;
    padding: 10px;
}

.review-footer {
    margin-top: 16px;
}

.review-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
}

.related-task {
    display: flex;
    align-items: center;
}

.related-info {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    color: #606266;
    background-color: #f5f7fa;
    padding: 4px 10px;
    border-radius: 15px;
    font-size: 0.85rem;
    transition: all 0.2s;
}

.related-info:hover {
    background-color: #ebeef5;
}

.related-title {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.action-buttons {
    display: flex;
    align-items: center;
    gap: 12px;
}

.detail-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: var(--el-color-primary);
    padding: 5px 10px;
    border-radius: 15px;
    font-size: 0.9rem;
    background-color: rgba(64, 158, 255, 0.1);
    transition: all 0.3s;
    text-decoration: none;
}

.detail-link:hover {
    background-color: rgba(64, 158, 255, 0.2);
    transform: translateY(-2px);
}

.review-filter {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 8px;
}

/* 模块类型颜色主题 */
.module-help .el-tag {
    color: #67c23a;
    border-color: rgba(103, 194, 58, 0.2);
    background-color: rgba(103, 194, 58, 0.1);
}

.module-course .el-tag {
    color: #409eff;
    border-color: rgba(64, 158, 255, 0.2);
    background-color: rgba(64, 158, 255, 0.1);
}

.module-skill .el-tag {
    color: #e6a23c;
    border-color: rgba(230, 162, 60, 0.2);
    background-color: rgba(230, 162, 60, 0.1);
}

.module-item .el-tag {
    color: #909399;
    border-color: rgba(144, 147, 153, 0.2);
    background-color: rgba(144, 147, 153, 0.1);
}

.module-group .el-tag {
    color: #9c27b0;
    border-color: rgba(156, 39, 176, 0.2);
    background-color: rgba(156, 39, 176, 0.1);
}

.module-resource .el-tag {
    color: #f56c6c;
    border-color: rgba(245, 108, 108, 0.2);
    background-color: rgba(245, 108, 108, 0.1);
}

/* 角色标签样式 */
.role-badge {
    position: relative;
    display: inline-flex;
    align-items: center;
    animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(5px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.role-badge .user-role-tag {
    padding: 3px 8px;
    border-radius: 12px;
    border: none;
    transition: all 0.3s ease;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 0.8rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.role-icon {
    font-size: 1rem;
    line-height: 1;
    margin-right: 2px;
}

.role-publisher .user-role-tag {
    background-color: rgba(64, 158, 255, 0.15);
    color: #409EFF;
}

.role-helper .user-role-tag {
    background-color: rgba(103, 194, 58, 0.15);
    color: #67C23A;
}

.role-publisher .user-role-tag:hover,
.role-helper .user-role-tag:hover {
    transform: translateY(-2px);
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.role-default .user-role-tag {
    background-color: rgba(144, 147, 153, 0.15);
    color: #909399;
}

.pulse-dot {
    position: absolute;
    top: -3px;
    right: -3px;
    width: 8px;
    height: 8px;
    background-color: #67C23A;
    border-radius: 50%;
    border: 2px solid #fff;
    animation: pulse 1.5s infinite;
}

@keyframes pulse {
    0% {
        transform: scale(0.95);
        box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
    }

    70% {
        transform: scale(1);
        box-shadow: 0 0 0 5px rgba(103, 194, 58, 0);
    }

    100% {
        transform: scale(0.95);
        box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
    }
}

.pagination {
    margin-top: 24px;
    display: flex;
    justify-content: center;
}

.empty-reviews {
    padding: 40px 0;
}

.empty-text {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;
}

.empty-tip {
    color: #909399;
    font-size: 12px;
}

.loading-container {
    padding: 40px 0;
    display: flex;
    justify-content: center;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 10px;
}

/* 确保对话框正确显示 */
:deep(.el-overlay) {
    position: fixed;
    /* 固定定位，防止对页面布局产生影响 */
    z-index: 2000;
    /* 确保在其他元素之上 */
}

:deep(.el-dialog) {
    margin: 0 auto !important;
    /* 防止对话框位置偏移 */
    position: relative;
    top: 15vh;
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
    border-radius: 8px;
}

:deep(.el-dialog__header) {
    padding: 20px;
    margin-right: 0;
    border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: var(--el-text-color-primary);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
    .review-header {
        flex-direction: column;
    }

    .review-meta {
        align-items: flex-start;
        margin-top: 12px;
    }

    .review-info {
        flex-direction: column;
        align-items: flex-start;
    }
}

/* 用户角色样式 */
.role-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.role-badge:hover {
    transform: scale(1.1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.role-publisher {
    color: #409eff;
}

.role-helper {
    color: #67c23a;
}

.role-default {
    color: #909399;
}

.role-icon {
    font-size: 1rem;
    margin-right: 4px;
}
</style>
