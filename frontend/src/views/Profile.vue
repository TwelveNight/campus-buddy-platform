<template>
    <div class="profile-page">
        <div class="profile-header">
            <h1>个人中心<span>完善您的个人信息，获得更好的校园互助体验</span></h1>
        </div>

        <el-row :gutter="24" class="profile-row">
            <!-- 左侧个人概览区域 -->
            <el-col :xs="24" :md="8" class="profile-sidebar">
                <el-card class="profile-card summary-card" shadow="hover">
                    <div class="user-summary">
                        <div class="avatar-container">
                            <AvatarUploader v-model="form.avatarUrl" :size="120" />
                        </div>
                        <h2>{{ form.nickname || form.username }}</h2>
                        <div class="user-credit">
                            <el-progress type="dashboard" :percentage="getCreditPercentage()" :color="creditColors"
                                :stroke-width="8">
                                <template #default>
                                    <div class="credit-label">
                                        <span class="credit-value">{{ form.creditScore }}</span>
                                        <span class="credit-title">信用积分</span>
                                    </div>
                                </template>
                            </el-progress>
                            <div class="credit-level">{{ getCreditLevel() }}</div>
                        </div>
                        <div class="user-stats">
                            <div class="stat-item">
                                <el-icon>
                                    <Medal />
                                </el-icon>
                                <span>注册用户</span>
                            </div>
                            <div class="stat-item">
                                <el-icon>
                                    <Calendar />
                                </el-icon>
                                <span>活跃用户</span>
                            </div>
                            <div class="stat-item">
                                <el-icon>
                                    <Star />
                                </el-icon>
                                <router-link to="/reviews" class="reviews-link">查看我的评价</router-link>
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧设置区域 -->
            <el-col :xs="24" :md="16" class="profile-content">
                <!-- 个人资料卡片 -->
                <el-card class="profile-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <el-icon>
                                <User />
                            </el-icon>
                            <h2>个人资料设置</h2>
                        </div>
                    </template>

                    <el-form :model="form" :rules="rules" ref="formRef" label-position="top" v-loading="loading">
                        <el-form-item label="用户名">
                            <el-input v-model="form.username" disabled>
                                <template #prefix>
                                    <el-icon>
                                        <Avatar />
                                    </el-icon>
                                </template>
                            </el-input>
                            <div class="form-tips">用户名不可修改</div>
                        </el-form-item>

                        <el-form-item label="昵称" prop="nickname">
                            <el-input v-model="form.nickname" placeholder="请输入昵称">
                                <template #prefix>
                                    <el-icon>
                                        <UserFilled />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" @click="handleSubmit" :loading="loading" round>
                                <el-icon>
                                    <Check />
                                </el-icon>保存修改
                            </el-button>
                            <el-button @click="$router.push('/')" round>
                                <el-icon>
                                    <Back />
                                </el-icon>返回
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-card>

                <!-- 修改密码卡片 -->
                <el-card class="profile-card password-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <el-icon>
                                <Lock />
                            </el-icon>
                            <h2>修改密码</h2>
                        </div>
                    </template>

                    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="top">
                        <el-form-item label="当前密码" prop="oldPassword">
                            <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password>
                                <template #prefix>
                                    <el-icon>
                                        <Key />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item label="新密码" prop="newPassword">
                            <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password>
                                <template #prefix>
                                    <el-icon>
                                        <Lock />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item label="确认新密码" prop="confirmPassword">
                            <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码"
                                show-password>
                                <template #prefix>
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" @click="handleChangePassword" :loading="pwdLoading" round>
                                <el-icon>
                                    <RefreshRight />
                                </el-icon>修改密码
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-card>

                <!-- 用户评价卡片 -->
                <el-card class="profile-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <el-icon>
                                <Star />
                            </el-icon>
                            <h2>我收到的评价 <router-link to="/reviews" class="more-reviews">更多评价<el-icon>
                                        <Collection />
                                    </el-icon></router-link></h2>
                        </div>
                    </template>

                    <div class="reviews-section">
                        <div v-if="reviewsLoading" class="loading-reviews">
                            <el-skeleton style="width: 100%">
                                <template #template>
                                    <el-skeleton-item variant="image"
                                        style="width: 48px; height: 48px; border-radius: 50%; margin-right: 16px" />
                                    <div style="flex: 1">
                                        <el-skeleton-item variant="p" style="width: 50%" />
                                        <div
                                            style="display: flex; align-items: center; margin-top: 12px; justify-content: space-between">
                                            <el-skeleton-item variant="text" style="margin-right: 16px" />
                                            <el-skeleton-item variant="text" style="width: 30%" />
                                        </div>
                                    </div>
                                </template>
                            </el-skeleton>
                            <el-skeleton style="width: 100%; margin-top: 20px">
                                <template #template>
                                    <el-skeleton-item variant="p" style="width: 100%" />
                                    <el-skeleton-item variant="p" style="width: 70%; margin-top: 8px" />
                                </template>
                            </el-skeleton>
                            <el-skeleton style="width: 100%; margin-top: 20px">
                                <template #template>
                                    <div style="display: flex; justify-content: space-between; align-items: center">
                                        <el-skeleton-item variant="text" style="width: 40%" />
                                        <el-skeleton-item variant="text" style="width: 20%" />
                                    </div>
                                </template>
                            </el-skeleton>
                        </div>
                        <div v-else-if="reviews.length === 0" class="empty-reviews">
                            <el-empty description="暂无收到的评价" :image-size="100">
                                <template #description>
                                    <div class="empty-text">
                                        <p>暂无收到的评价</p>
                                        <p class="empty-tip">完成更多互助任务，获取用户评价</p>
                                    </div>
                                </template>
                                <router-link to="/helpinfo">
                                    <el-button type="primary" size="small" round>
                                        <el-icon>
                                            <Promotion />
                                        </el-icon>
                                        浏览互助信息
                                    </el-button>
                                </router-link>
                            </el-empty>
                        </div>
                        <div v-else class="review-list">
                            <div v-for="review in reviews" :key="review.reviewId" class="review-item">
                                <div class="review-header">
                                    <div class="reviewer-info">
                                        <el-avatar :size="42" :src="review.reviewerAvatar || defaultAvatar"></el-avatar>
                                        <div class="reviewer-details">
                                            <span class="reviewer-name">{{ review.reviewerNickname || ('用户 #' +
                                                review.reviewerUserId) }}</span>
                                            <div class="review-module" :class="getModuleClass(review.moduleType)">
                                                <el-tag size="small" effect="light"
                                                    :type="getModuleTagType(review.moduleType)">
                                                    <el-icon>
                                                        <MessageBox />
                                                    </el-icon>
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
                                                <div class="score-value">{{ review.score }}<span
                                                        class="score-max">/5</span></div>
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

                                <div class="review-info">
                                    <div class="related-task">
                                        <el-tooltip v-if="review.relatedInfoSummary"
                                            :content="review.relatedInfoSummary" placement="top-start" effect="light">
                                            <span class="related-info">
                                                <el-icon>
                                                    <Link />
                                                </el-icon>
                                                <span class="related-title">{{ review.relatedInfoTitle || '互助信息'
                                                    }}</span>
                                            </span>
                                        </el-tooltip>
                                        <span v-else class="related-info">
                                            <el-icon>
                                                <Link />
                                            </el-icon>
                                            <span class="related-title">{{ review.relatedInfoTitle || '互助信息' }}</span>
                                        </span>
                                    </div>
                                    <router-link :to="`/helpinfo/${review.relatedInfoId}`" class="detail-link">
                                        <el-icon>
                                            <View />
                                        </el-icon>
                                        查看详情
                                    </router-link>
                                </div>
                            </div>

                            <router-link to="/reviews" class="view-all-reviews">
                                <el-button type="primary" plain round>
                                    <el-icon>
                                        <Collection />
                                    </el-icon>
                                    查看全部评价
                                </el-button>
                            </router-link>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useAuthStore } from '../store/auth'
import AvatarUploader from '../components/AvatarUploader.vue'
import { updateUserProfile, changePassword } from '../api/user'
import { getUserReviews } from '../api/review'
import {
    User, UserFilled, Avatar, Key, Lock, Check,
    RefreshRight, Back, Medal, Calendar, Star, Clock, Link, View
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const loading = ref(false)
const pwdLoading = ref(false)
const reviewsLoading = ref(false)

// 个人信息表单数据
const form = reactive({
    userId: 0,
    username: '',
    nickname: '',
    avatarUrl: '',
    creditScore: 0
})

// 修改密码表单数据
const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})

// 用户评价数据
interface ReviewItem {
    reviewId: number;
    reviewedUserId: number;
    reviewerUserId: number;
    reviewerNickname?: string;
    reviewerAvatar?: string;
    relatedInfoId: number;
    relatedInfoTitle?: string;
    relatedInfoSummary?: string;
    helpInfo?: any;
    score: number;
    content?: string;
    createdAt: string | number;
}

const reviews = ref<ReviewItem[]>([])
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 积分相关的颜色渐变配置
const creditColors = [
    { color: '#f56c6c', percentage: 20 },
    { color: '#e6a23c', percentage: 40 },
    { color: '#5cb87a', percentage: 60 },
    { color: '#1989fa', percentage: 80 },
    { color: '#6f7ad3', percentage: 100 }
]

// 计算信用积分的百分比（假设最高分为100）
function getCreditPercentage() {
    const score = form.creditScore || 0
    // 假设最高分为100
    return Math.min(100, Math.max(0, score))
}

// 根据积分返回相应的信用等级
function getCreditLevel() {
    const score = form.creditScore || 0
    if (score >= 90) return '信用优秀'
    if (score >= 80) return '信用良好'
    if (score >= 70) return '信用标准'
    if (score >= 60) return '信用一般'
    return '信用待提升'
}

// 格式化日期
function formatDate(dateString: string | number | Date) {
    if (!dateString) return '';
    try {
        const date = typeof dateString === 'number' ? new Date(dateString) :
            (typeof dateString === 'string' ? new Date(dateString) : dateString);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        console.error('日期格式化错误:', error, dateString);
        return String(dateString);
    }
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
        'COURSE_TUTORING': '课程辅导',
        'SKILL_LEARNING': '技能学习',
        'ITEM_LEND': '物品借用',
        'GROUP': '学习小组',
        'RESOURCE': '资源共享'
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

// 获取用户收到的评价（分页接口，兼容后端PageResult结构）
async function fetchUserReviews() {
    if (!form.userId) return;
    reviewsLoading.value = true;
    try {
        // 使用分页接口，type=received，默认查前10条
        const response = await getUserReviews({ userId: form.userId, type: 'received', page: 1, size: 10 });
        // 兼容 axios 响应结构
        const data = response?.data || response;
        if (data && Array.isArray(data.items)) {
            reviews.value = data.items;
        } else if (Array.isArray(data)) {
            reviews.value = data;
        } else {
            reviews.value = [];
        }
    } catch (error) {
        console.error('获取用户评价失败:', error);
        ElMessage.error('获取评价信息失败');
    } finally {
        reviewsLoading.value = false;
    }
}

// 组件挂载时执行
onMounted(async () => {
    // 加载个人资料
    try {
        loading.value = true;

        // 从store获取基本用户信息
        if (authStore.user) {
            form.userId = authStore.user.userId || 0;
            form.username = authStore.user.username || '';
            form.nickname = authStore.user.nickname || '';
            form.avatarUrl = authStore.user.avatarUrl || '';
            form.creditScore = authStore.user.creditScore || 0;
        }

        // 获取用户收到的评价
        await fetchUserReviews();

    } catch (error) {
        console.error('加载个人信息失败:', error);
        ElMessage.error('获取个人信息失败');
    } finally {
        loading.value = false;
    }
});

// 个人信息表单验证规则
const rules = {
    nickname: [
        { required: true, message: '昵称不能为空', trigger: 'blur' },
        { min: 2, max: 20, message: '昵称长度应为2到20个字符', trigger: 'blur' }
    ]
}

// 修改密码表单验证规则
const pwdRules = {
    oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
            validator: (_: any, value: string, callback: any) => {
                if (value !== pwdForm.newPassword) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
}

// 保存个人信息
async function handleSubmit() {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                const res = await updateUserProfile({
                    nickname: form.nickname,
                    avatarUrl: form.avatarUrl
                })

                if (res.data.code === 200) {
                    ElMessage({
                        message: '个人信息更新成功',
                        type: 'success',
                        duration: 2000
                    })

                    // 更新存储中的用户信息
                    if (authStore.user) {
                        authStore.user.nickname = form.nickname
                        authStore.user.avatarUrl = form.avatarUrl
                    }
                } else {
                    ElMessage.error(res.data.message || '更新失败')
                }
            } catch (e: any) {
                ElMessage.error(e.message || '更新失败，请稍后重试')
            } finally {
                loading.value = false
            }
        }
    })
}

// 修改密码
async function handleChangePassword() {
    if (!pwdFormRef.value) return

    await pwdFormRef.value.validate(async (valid) => {
        if (valid) {
            pwdLoading.value = true
            try {
                const res = await changePassword({
                    oldPassword: pwdForm.oldPassword,
                    newPassword: pwdForm.newPassword
                })

                if (res.data.code === 200) {
                    ElMessage({
                        message: '密码修改成功，请重新登录',
                        type: 'success',
                        duration: 2000
                    })

                    // 清空表单
                    pwdForm.oldPassword = ''
                    pwdForm.newPassword = ''
                    pwdForm.confirmPassword = ''

                    // 短暂延迟后退出登录
                    setTimeout(() => {
                        authStore.logout()
                        router.push('/login')
                    }, 1500)
                } else {
                    ElMessage.error(res.data.message || '密码修改失败')
                }
            } catch (e: any) {
                ElMessage.error(e.message || '密码修改失败，请稍后重试')
            } finally {
                pwdLoading.value = false
            }
        }
    })
}
</script>

<style scoped>
.profile-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px 60px;
}

.profile-header {
    text-align: center;
    margin: 30px 0 40px;
}

.profile-header h1 {
    font-size: 2.2rem;
    color: var(--text-primary);
    font-weight: 700;
}

.profile-header h1 span {
    display: block;
    font-size: 1rem;
    color: var(--text-secondary);
    font-weight: 400;
    margin-top: 8px;
}

.profile-row {
    margin-bottom: 40px;
}

.profile-card {
    border-radius: 16px !important;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08) !important;
    transition: transform 0.3s, box-shadow 0.3s;
    overflow: visible;
    margin-bottom: 32px;
}

.profile-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-bottom: 16px;
}

.card-header :deep(svg) {
    font-size: 20px;
    color: var(--primary-color);
}

.card-header h2 {
    margin: 0;
    font-size: 1.4rem;
    color: var(--text-primary);
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.more-reviews {
    font-size: 0.85rem;
    font-weight: 400;
    color: var(--el-color-primary);
    display: flex;
    align-items: center;
    gap: 4px;
    margin-left: 10px;
    padding: 4px 10px;
    border-radius: 15px;
    background-color: rgba(64, 158, 255, 0.1);
    transition: all 0.3s;
}

.more-reviews:hover {
    background-color: rgba(64, 158, 255, 0.2);
    transform: translateX(3px);
}

/* 用户概要信息卡片 */
.summary-card {
    height: 100%;
    min-height: 450px;
}

.user-summary {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
}

.avatar-container {
    margin-bottom: 24px;
    position: relative;
}

.user-summary h2 {
    font-size: 1.5rem;
    margin: 0 0 24px;
    color: var(--text-primary);
}

.user-credit {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 24px;
}

.credit-label {
    display: flex;
    flex-direction: column;
    align-items: center;
    line-height: 1.2;
}

.credit-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--primary-color);
}

.credit-title {
    font-size: 12px;
    color: var(--text-secondary);
}

.credit-level {
    margin-top: 8px;
    font-size: 16px;
    color: var(--text-primary);
    font-weight: 500;
}

.user-stats {
    width: 100%;
    display: flex;
    justify-content: space-around;
    margin-top: 16px;
    padding: 16px 0;
    border-top: 1px dashed #eee;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.stat-item :deep(svg) {
    font-size: 24px;
    color: var(--accent-color);
}

.stat-item span {
    font-size: 14px;
    color: var(--text-secondary);
}

.reviews-link {
    font-size: 14px;
    color: var(--primary-color);
    text-decoration: none;
}

.reviews-link:hover {
    text-decoration: underline;
}

/* 表单样式 */
.form-tips {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 5px;
    opacity: 0.7;
}

/* 密码卡片间距 */
.password-card {
    margin-top: 32px;
}

:deep(.el-input-group__prepend) {
    background-color: transparent;
}

:deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--text-primary);
}

:deep(.el-button) {
    transition: all 0.3s;
}

:deep(.el-input__inner) {
    border-radius: 8px;
}

:deep(.el-form-item__error) {
    margin-top: 4px;
}

/* 用户评价样式 */
.reviews-section {
    padding: 10px 0;
}

.empty-reviews {
    padding: 40px 0 30px;
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

.loading-reviews {
    padding: 10px 0;
}

.review-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.view-all-reviews {
    display: flex;
    justify-content: center;
    margin-top: 24px;
}

.view-all-reviews .el-button {
    padding: 12px 24px;
    font-size: 0.95rem;
    transition: all 0.3s;
}

.view-all-reviews .el-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.view-all-reviews .el-icon {
    margin-right: 6px;
}

.review-item {
    padding: 22px;
    border-radius: 12px;
    background-color: #fff;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
}

.review-item::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    width: 5px;
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
    margin-right: 6px;
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

.review-score {
    margin-top: 2px;
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
    padding: 16px 20px;
    margin-bottom: 20px;
    background-color: #fafafa;
    border-radius: 12px;
    overflow: hidden;
}

.review-content-wrap::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.2));
}

.review-content-quote-mark {
    position: absolute;
    font-size: 18px;
    color: #c0c4cc;
    opacity: 0.5;
}

.review-content-quote-mark.left {
    left: 10px;
    top: 6px;
    transform: rotate(180deg);
}

.review-content-quote-mark.right {
    right: 10px;
    bottom: 6px;
}

.review-content {
    line-height: 1.7;
    color: #606266;
    font-size: 1rem;
    white-space: pre-line;
    text-indent: 1em;
    position: relative;
}

.review-content-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #909399;
    font-size: 0.9rem;
    padding: 10px;
    text-align: center;
    font-style: italic;
}

.review-info {
    margin-top: 18px;
    font-size: 0.9rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 14px;
    border-top: 1px dashed #ebeef5;
}

.related-task {
    flex: 1;
    overflow: hidden;
}

.related-info {
    color: #606266;
    max-width: 100%;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    background-color: #f5f7fa;
    border-radius: 4px;
}

.related-title {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 180px;
    display: inline-block;
    font-weight: 500;
}

.detail-link {
    color: var(--el-color-primary);
    text-decoration: none;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 4px 10px;
    background-color: rgba(64, 158, 255, 0.1);
    border-radius: 4px;
    transition: all 0.2s;
}

.detail-link:hover {
    background-color: rgba(64, 158, 255, 0.2);
    transform: scale(1.05);
}

/* 不同模块类型的颜色样式 */
.module-help .el-tag {
    --el-tag-bg-color: rgba(103, 194, 58, 0.1);
}

.module-course .el-tag {
    --el-tag-bg-color: rgba(64, 158, 255, 0.1);
}

.module-skill .el-tag {
    --el-tag-bg-color: rgba(230, 162, 60, 0.1);
}

.module-item .el-tag {
    --el-tag-bg-color: rgba(144, 147, 153, 0.1);
}

.module-group .el-tag {
    --el-tag-bg-color: rgba(144, 113, 236, 0.1);
}

.module-resource .el-tag {
    --el-tag-bg-color: rgba(245, 108, 108, 0.1);
}

.tags-container .el-tag {
    margin: 0 6px 6px 0;
}

/* 技能标签样式 */
.skill-tags {
    max-width: 350px;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .profile-header h1 {
        font-size: 1.8rem;
    }

    .profile-content {
        margin-top: 24px;
    }

    .user-summary {
        padding: 10px 0;
    }
}
</style>
