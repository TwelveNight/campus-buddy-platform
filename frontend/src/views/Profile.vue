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
                            <AvatarUploader v-model="avatarUrlWithTimestamp" :size="120"
                                @upload-success="handleAvatarUploadSuccess" tip="点击更换头像" :isGroupAvatar="false" />
                        </div>
                        <h2>
                            <router-link :to="`/user/${form.userId}`" class="profile-nickname">
                                {{ form.nickname || form.username }}
                            </router-link>
                        </h2>
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

                        <el-form-item label="性别">
                            <el-radio-group v-model="form.gender">
                                <el-radio label="MALE">男</el-radio>
                                <el-radio label="FEMALE">女</el-radio>
                                <el-radio label="UNKNOWN">保密</el-radio>
                            </el-radio-group>
                        </el-form-item>

                        <el-row :gutter="20">
                            <el-col :span="12">
                                <el-form-item label="专业" prop="major">
                                    <el-input v-model="form.major" placeholder="请输入专业">
                                        <template #prefix>
                                            <el-icon>
                                                <Document />
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="年级" prop="grade">
                                    <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
                                        <el-option label="大一" value="大一"></el-option>
                                        <el-option label="大二" value="大二"></el-option>
                                        <el-option label="大三" value="大三"></el-option>
                                        <el-option label="大四" value="大四"></el-option>
                                        <el-option label="研一" value="研一"></el-option>
                                        <el-option label="研二" value="研二"></el-option>
                                        <el-option label="研三" value="研三"></el-option>
                                        <el-option label="博士" value="博士"></el-option>
                                        <el-option label="其他" value="其他"></el-option>
                                    </el-select>
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-form-item label="联系方式" prop="contactInfo">
                            <el-input v-model="form.contactInfo" placeholder="请输入联系方式，如微信、QQ、邮箱等">
                                <template #prefix>
                                    <el-icon>
                                        <Promotion />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>

                        <el-form-item label="技能标签" prop="parsedSkillTags">
                            <div class="skill-tags-section">
                                <el-tag v-for="tag in form.parsedSkillTags" :key="tag" closable
                                    :disable-transitions="false" @close="handleTagClose(tag)" class="skill-tag">
                                    {{ tag }}
                                </el-tag>
                                <el-input v-if="inputTagVisible" ref="tagInputRef" v-model="inputTagValue"
                                    class="tag-input" size="small" @keyup.enter="handleTagConfirm"
                                    @blur="handleTagConfirm" />
                                <el-button v-else class="button-new-tag" size="small" @click="showTagInput">
                                    + 添加技能标签
                                </el-button>
                            </div>
                            <div class="form-tips">添加您擅长的技能标签，最多可添加10个，每个标签不超过20个字符</div>
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
                                            <div class="name-role-row">
                                                <span class="reviewer-name">
                                                    <router-link :to="`/user/${review.reviewerUserId}`" class="user-link">
                                                        {{ review.reviewerNickname || ('用户 #' + review.reviewerUserId)
                                                        }}
                                                    </router-link>
                                                </span>
                                                <div class="role-badge"
                                                    :class="getRoleClass(review.reviewType, review.reviewerUserId)">
                                                    <el-tooltip
                                                        :content="getRoleTooltip(review.reviewType, review.reviewerUserId)"
                                                        placement="top" effect="light">
                                                        <el-tag size="small" effect="dark"
                                                            :type="getUserRoleType(review.reviewType, review.reviewerUserId)"
                                                            class="user-role-tag">
                                                            <span class="role-icon">{{ getRoleIcon(review.reviewType,
                                                                review.reviewerUserId) }}</span>
                                                            {{ getUserRoleLabel(review.reviewType,
                                                            review.reviewerUserId) }}
                                                        </el-tag>
                                                    </el-tooltip>
                                                </div>
                                            </div>
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
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useAuthStore } from '../store/auth'
import AvatarUploader from '../components/AvatarUploader.vue'
import { getUserProfile, updateUserProfile, changePassword } from '../api/user'
import { getUserReviews } from '../api/review'
import {
    User, UserFilled, Avatar, Key, Lock, Check,
    RefreshRight, Back, Medal, Calendar, Star, Clock, Link, View, MessageBox, ChatRound, Document, Collection, Promotion
} from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const loading = ref(false)
const pwdLoading = ref(false)
const reviewsLoading = ref(false)

// 技能标签相关
const inputTagValue = ref('')
const inputTagVisible = ref(false)
const tagInputRef = ref<HTMLInputElement>()

// 显示标签输入框
const showTagInput = () => {
    inputTagVisible.value = true
    nextTick(() => {
        tagInputRef.value?.focus()
    })
}

// 删除标签
const handleTagClose = (tag: string) => {
    form.parsedSkillTags = form.parsedSkillTags.filter(t => t !== tag)
}

// 确认添加标签
const handleTagConfirm = () => {
    const tag = inputTagValue.value.trim()
    if (tag) {
        // 检查是否已存在
        if (form.parsedSkillTags.includes(tag)) {
            ElMessage.warning('该技能标签已存在')
        }
        // 检查标签长度
        else if (tag.length > 20) {
            ElMessage.warning('标签不能超过20个字符')
        }
        // 检查标签数量
        else if (form.parsedSkillTags.length >= 10) {
            ElMessage.warning('最多只能添加10个标签')
        }
        else {
            form.parsedSkillTags.push(tag)
        }
    }
    inputTagVisible.value = false
    inputTagValue.value = ''
}

// 添加一个时间戳控制变量，用于刷新头像
const avatarRefreshTimestamp = ref(Date.now());

// 为头像URL添加时间戳，避免缓存问题
const avatarUrlWithTimestamp = computed(() => {
    const baseUrl = form.avatarUrl;
    if (!baseUrl) return '';

    // 如果URL已经有查询参数，使用&连接，否则使用?开始
    const separator = baseUrl.includes('?') ? '&' : '?';
    return `${baseUrl}${separator}t=${avatarRefreshTimestamp.value}`;
});

// 处理头像上传成功的回调
const handleAvatarUploadSuccess = async (url: string) => {
    if (!url) {
        ElMessage.warning('头像URL为空，无法更新');
        return;
    }

    try {
        loading.value = true;

        // 确保URL没有时间戳参数，避免重复添加
        const cleanUrl = url.split('?')[0];

        // 立即更新表单中的头像URL
        form.avatarUrl = cleanUrl;

        // 更新时间戳，强制刷新头像
        avatarRefreshTimestamp.value = Date.now();

        // 调用API更新用户头像
        try {
            const res = await updateUserProfile({
                avatarUrl: cleanUrl
            });

            if (res.data && res.data.code === 200) {
                ElMessage.success('头像更新成功');
            }
        } catch (error) {
            console.warn('头像更新API返回错误，但头像可能已经上传成功:', error);
            // 即使API返回错误，我们也假设头像已经更新成功
            // 这里不显示错误消息，因为实际上头像已经更新了
        }

        console.log('更新头像URL:', cleanUrl);
        console.log('更新时间戳:', avatarRefreshTimestamp.value);
        console.log('计算后的带时间戳URL:', avatarUrlWithTimestamp.value);

        // 无论API调用成功与否，都更新本地状态
        if (authStore.user) {
            authStore.user = {
                ...authStore.user,
                avatarUrl: cleanUrl
            };

            // 保存到本地存储
            localStorage.setItem('user', JSON.stringify(authStore.user));

            // 强制通知其他组件刷新头像
            authStore.$patch({ avatarUpdateTime: Date.now() });
        }
    } catch (error: any) {
        console.error('更新头像失败:', error);
        ElMessage.error(error.message || '更新头像失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};

// 定义 ProfileForm 接口类型
interface ProfileForm {
    userId: number;
    username: string;
    nickname: string;
    avatarUrl: string;
    gender: string;
    major: string;
    grade: string;
    contactInfo: string;
    skillTags: string;
    parsedSkillTags: string[];
    creditScore: number;
    status: string;
    createdAt: string;
    updatedAt: string;
}

// 个人信息表单数据
const form = reactive<ProfileForm>({
    userId: 0,
    username: '',
    nickname: '',
    avatarUrl: '',
    gender: 'UNKNOWN',
    major: '',
    grade: '',
    contactInfo: '',
    skillTags: '',
    parsedSkillTags: [] as string[],
    creditScore: 0,
    status: 'ACTIVE',
    createdAt: '',
    updatedAt: ''
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
    reviewType?: string;
    moduleType?: string;
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

// 获取用户角色的标签类型
function getUserRoleType(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return 'info';
    const currentUserId = form.userId;
    if (reviewType === 'PUBLISHER_TO_HELPER') {
        return reviewerUserId === currentUserId ? 'primary' : 'success';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        return reviewerUserId === currentUserId ? 'success' : 'primary';
    }
    return 'info';
}

// 获取用户角色的显示名称
function getUserRoleLabel(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return '用户';
    const currentUserId = form.userId;
    if (reviewType === 'PUBLISHER_TO_HELPER') {
        return reviewerUserId === currentUserId ? '求助方' : '帮助方';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        return reviewerUserId === currentUserId ? '帮助方' : '求助方';
    }
    return '用户';
}

// 获取用户角色的CSS类名
function getRoleClass(reviewType: string | undefined, reviewerUserId: number): string {
    if (!reviewType) return 'role-default';
    const currentUserId = form.userId;
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
    const currentUserId = form.userId;
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
    const currentUserId = form.userId;
    if (reviewType === 'PUBLISHER_TO_HELPER') {
        return reviewerUserId === currentUserId ? '您是求助方' : '对方是帮助方';
    } else if (reviewType === 'HELPER_TO_PUBLISHER') {
        return reviewerUserId === currentUserId ? '您是帮助方' : '对方是求助方';
    }
    return '用户角色';
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
            form.gender = authStore.user.gender || 'UNKNOWN';
            form.major = authStore.user.major || '';
            form.grade = authStore.user.grade || '';
            form.contactInfo = authStore.user.contactInfo || '';
            form.status = authStore.user.status || 'ACTIVE';
            form.createdAt = authStore.user.createdAt || '';
            form.updatedAt = authStore.user.updatedAt || '';

            // 解析技能标签
            if (authStore.user.skillTags) {
                try {
                    const tags = JSON.parse(authStore.user.skillTags);
                    form.skillTags = authStore.user.skillTags;
                    form.parsedSkillTags = Array.isArray(tags) ? tags : [];
                } catch (e) {
                    console.error('解析技能标签失败:', e);
                    form.parsedSkillTags = [];
                }
            }
        }

        // 从后端获取最新的用户信息
        try {
            const res = await getUserProfile();
            if (res.data && res.data.data) {
                const userData = res.data.data;

                // 更新表单数据
                form.userId = userData.userId;
                form.username = userData.username || form.username;
                form.nickname = userData.nickname || form.nickname;
                form.avatarUrl = userData.avatarUrl || form.avatarUrl;
                form.gender = userData.gender || form.gender;
                form.major = userData.major || form.major;
                form.grade = userData.grade || form.grade;
                form.contactInfo = userData.contactInfo || form.contactInfo;
                form.creditScore = userData.creditScore || form.creditScore;
                form.status = userData.status || form.status;
                form.createdAt = userData.createdAt || form.createdAt;
                form.updatedAt = userData.updatedAt || form.updatedAt;

                // 解析技能标签
                if (userData.skillTags) {
                    try {
                        const tags = JSON.parse(userData.skillTags);
                        form.skillTags = userData.skillTags;
                        form.parsedSkillTags = Array.isArray(tags) ? tags : [];
                    } catch (e) {
                        console.error('解析技能标签失败:', e);
                    }
                }

                // 更新存储中的用户信息
                authStore.user = {
                    ...authStore.user,
                    ...userData
                };
                localStorage.setItem('user', JSON.stringify(authStore.user));
            }
        } catch (e) {
            console.error('获取用户资料失败:', e);
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
                // 将技能标签数组转换为JSON字符串
                const skillTagsJson = form.parsedSkillTags.length > 0 ? JSON.stringify(form.parsedSkillTags) : ''

                const res = await updateUserProfile({
                    nickname: form.nickname,
                    avatarUrl: form.avatarUrl,
                    gender: form.gender,
                    major: form.major,
                    grade: form.grade,
                    contactInfo: form.contactInfo,
                    skillTags: skillTagsJson
                })

                if (res.data.code === 200) {
                    ElMessage({
                        message: '个人信息更新成功',
                        type: 'success',
                        duration: 2000
                    })

                    // 更新存储中的用户信息
                    if (authStore.user) {
                        // 更新本地存储的用户信息
                        authStore.user = {
                            ...authStore.user,
                            nickname: form.nickname,
                            avatarUrl: form.avatarUrl,
                            gender: form.gender,
                            major: form.major,
                            grade: form.grade,
                            contactInfo: form.contactInfo,
                            skillTags: skillTagsJson
                        };

                        // 保存到本地存储
                        localStorage.setItem('user', JSON.stringify(authStore.user));
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
    gap: 8px;
}

.avatar-container {
    margin-bottom: 24px;
    position: relative;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}

.user-summary h2 {
    font-size: 1.5rem;
    margin: 0 0 24px;
    color: var(--text-primary);
    text-align: center;
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

.name-role-row {
    display: flex;
    align-items: center;
    gap: 8px;
}

.reviewer-name {
    font-weight: 600;
    color: var(--el-text-color-primary);
    font-size: 1.05rem;
    line-height: 1.2;
}

.role-badge {
    display: flex;
    align-items: center;
}

.user-role-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 0.8rem;
    padding: 0 8px;
    height: 22px;
    border-radius: 4px;
    margin-right: 6px;
}

.role-icon {
    font-size: 1rem;
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
.skill-tags-section {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    margin-bottom: 8px;
}

.skill-tag {
    margin-right: 0;
    background-color: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-8);
    color: var(--el-color-primary);
}

.tag-input {
    width: 90px;
    margin-left: 8px;
    vertical-align: bottom;
}

.button-new-tag {
    height: 32px;
    padding: 0 10px;
}

.user-link {
    font-weight: 600;
    color: var(--el-color-primary);
    font-size: 1.05rem;
    line-height: 1.2;
    text-decoration: none;
    transition: all 0.3s ease;
    position: relative;
    cursor: pointer;
}
.user-link:hover {
    color: var(--el-color-primary-dark-2);
}
.user-link::after {
    content: '';
    position: absolute;
    width: 0;
    height: 2px;
    bottom: -2px;
    left: 0;
    background-color: var(--el-color-primary);
    transition: width 0.3s ease;
}
.user-link:hover::after {
    width: 100%;
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
