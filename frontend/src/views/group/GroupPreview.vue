<template>
    <div class="group-preview-page" v-loading="loading">
        <div v-if="!loading && group">
            <!-- 小组基本信息 -->
            <div class="group-header">
                <div class="group-avatar">
                    <el-avatar :size="80" :src="group.avatar || group.avatarUrl || defaultAvatar">
                        {{ group.name?.substring(0, 1) }}
                    </el-avatar>
                </div>
                <div class="group-header-info">
                    <h1 class="group-name">{{ group.name }}</h1>
                    <div class="group-meta">
                        <el-tag>{{ group.category }}</el-tag>
                        <el-tag v-for="tag in normalizeTags(group.tags)" :key="tag" type="info" class="tag">{{ tag }}</el-tag>
                        <span class="member-count">
                            <el-icon>
                                <User />
                            </el-icon> {{ group.memberCount }} 成员
                        </span>
                        <span class="join-type">
                            {{ group.joinType === 'PUBLIC' ? '公开小组' : '私有小组' }}
                        </span>
                    </div>
                    <!-- 创建者信息展示 -->
                    <div class="group-creator" v-if="group.creator">
                        <span style="color:#888;font-size:13px;">创建者：</span>
                        <router-link :to="`/user/${group.creator.userId}`" class="creator-link" style="display:inline-flex;align-items:center;gap:6px;">
                            <el-avatar :size="22" :src="group.creator.avatarUrl || defaultAvatar" />
                            <span>{{ group.creator.nickname }}</span>
                        </router-link>
                    </div>
                    <p class="group-description">{{ group.description }}</p>
                </div>
                <div class="group-actions">
                    <el-button 
                        v-if="joinStatus === 'not_joined' && group.status === 'ACTIVE'" 
                        type="primary" 
                        @click="handleJoinGroup">加入小组</el-button>
                    <el-button 
                        v-else-if="joinStatus === 'not_joined' && (group.status === 'INACTIVE' || group.status === 'DISBANDED')" 
                        type="info" 
                        disabled>小组已{{ group.status === 'INACTIVE' ? '禁用' : '解散' }}</el-button>
                    <el-button v-else-if="joinStatus === 'pending'" type="warning" disabled>等待审批</el-button>
                    <el-button v-else-if="joinStatus === 'joined'" type="success" disabled>已加入</el-button>
                    <el-button @click="goBack">返回列表</el-button>
                </div>
            </div>

            <!-- 小组介绍信息 -->
            <div class="group-intro">
                <!-- 禁用小组警告 -->
                <el-alert
                    v-if="group.status === 'INACTIVE' || group.status === 'DISBANDED'"
                    :title="`该小组已${group.status === 'INACTIVE' ? '被禁用' : '解散'}`"
                    type="warning"
                    :description="`该小组当前${group.status === 'INACTIVE' ? '被管理员禁用' : '已解散'}，仅可查看基本信息。`"
                    show-icon
                    :closable="false"
                    style="margin-bottom: 20px"
                />
                
                <el-alert
                    v-if="joinStatus !== 'joined'"
                    title="您尚未加入该小组"
                    type="info"
                    description="加入小组后，您可以查看小组内的讨论、文件资源以及成员信息。"
                    show-icon
                    :closable="false"
                    style="margin-bottom: 20px"
                />
                
                <el-card class="feature-card">
                    <template #header>
                        <div class="card-header">
                            <h3>小组功能介绍</h3>
                        </div>
                    </template>
                    <div class="feature-list">
                        <div class="feature-item">
                            <el-icon><ChatDotRound /></el-icon>
                            <div class="feature-info">
                                <h4>讨论区</h4>
                                <p>在这里与小组成员进行讨论交流，分享学习心得和资源</p>
                            </div>
                        </div>
                        <div class="feature-item">
                            <el-icon><Document /></el-icon>
                            <div class="feature-info">
                                <h4>文件资源</h4>
                                <p>上传和下载学习资料、笔记、课件等文件资源</p>
                            </div>
                        </div>
                        <div class="feature-item">
                            <el-icon><UserFilled /></el-icon>
                            <div class="feature-info">
                                <h4>成员管理</h4>
                                <p>查看小组成员信息，认识志同道合的学习伙伴</p>
                            </div>
                        </div>
                    </div>
                </el-card>
            </div>
        </div>

        <el-empty v-else-if="!loading && !group" description="小组不存在或已被删除">
            <el-button type="primary" @click="$router.push('/groups')">返回小组列表</el-button>
        </el-empty>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, ChatDotRound, Document, UserFilled } from '@element-plus/icons-vue';
import { getGroupDetail, joinGroup, getGroupMembers } from '@/api/group';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 路由参数
const groupId = computed(() => route.params.id);

// 数据状态
const loading = ref(false);
const group = ref(null);
const isGroupMember = ref(false);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const joinStatus = ref('not_joined'); // 'not_joined' | 'pending' | 'joined'

// 生命周期钩子
onMounted(() => {
    loadGroupDetail();
});

// 加载小组详情
const loadGroupDetail = async () => {
    if (!groupId.value) return;

    loading.value = true;
    try {
        const response = await getGroupDetail(groupId.value);
        if (response.data && response.data.code === 200) {
            group.value = response.data.data;

            // 兼容后端返回的tags为字符串的情况
            group.value.tags = normalizeTags(group.value.tags);
            
            // 检查用户是否已是小组成员
            await checkGroupMembership();
        } else {
            ElMessage.error(response.data?.message || '加载小组详情失败');
        }
    } catch (error) {
        console.error('加载小组详情失败:', error);
        ElMessage.error('加载小组详情失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};

// 检查用户是否是小组成员，并判断状态
const checkGroupMembership = async () => {
    if (!authStore.isAuthenticated) return;
    
    try {
        const response = await getGroupMembers(groupId.value);
        if (response.data && response.data.code === 200) {
            const members = response.data.data || [];
            
            // 确定当前用户在小组中的角色
            const currentUserId = authStore.user?.userId;
            const userMember = members.find(m => m.userId === currentUserId);
            
            if (userMember) {
                if (userMember.status === 'ACTIVE') {
                    joinStatus.value = 'joined';
                    
                    // 检查小组状态，只有当小组处于活跃状态时才重定向到详情页
                    if (group.value && group.value.status === 'ACTIVE') {
                        // 如果用户已是小组成员且是通过预览页访问的，重定向到详情页
                        router.push(`/groups/${groupId.value}/detail`);
                    }
                } else if (userMember.status === 'PENDING' || userMember.status === 'PENDING_APPROVAL') {
                    joinStatus.value = 'pending';
                }
            } else {
                joinStatus.value = 'not_joined';
            }
        }
    } catch (error) {
        console.error('检查小组成员身份失败:', error);
    }
};

// 兼容后端返回的tags为字符串的情况
const normalizeTags = (tags) => {
    if (Array.isArray(tags)) return tags;
    if (typeof tags === 'string') return tags.split(/[，,]/).map(t => t.trim()).filter(Boolean);
    return [];
};

// 返回上一页
const goBack = () => {
    router.push('/groups');
};

// 加入小组
const handleJoinGroup = async () => {
    if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录');
        router.push('/login');
        return;
    }
    
    // 检查小组状态
    if (group.value && (group.value.status === 'INACTIVE' || group.value.status === 'DISBANDED')) {
        ElMessage.warning(`该小组已${group.value.status === 'INACTIVE' ? '被禁用' : '解散'}，无法加入`);
        return;
    }

    try {
        const response = await joinGroup(groupId.value);
        if (response.data && response.data.code === 200) {
            // 根据小组类型显示不同的成功消息
            if (group.value && group.value.joinType === 'APPROVAL') {
                ElMessage.info('小组需要审核，请等待管理员批准');
                joinStatus.value = 'pending';
            } else {
                ElMessage.success('已成功加入小组');
                joinStatus.value = 'joined';
                
                // 检查小组状态，只有当小组处于活跃状态时才重定向到详情页
                if (group.value && group.value.status === 'ACTIVE') {
                    router.push(`/groups/${groupId.value}/detail`);
                }
            }
        } else {
            ElMessage.error(response.data?.message || '加入小组失败');
        }
    } catch (error) {
        console.error('加入小组失败:', error);
        ElMessage.error('加入小组失败，请稍后重试');
    }
};
</script>

<style scoped>
/* 动画效果定义 */
@keyframes slideInFromTop {
    from {
        opacity: 0;
        transform: translateY(-30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes bounceIn {
    0% {
        opacity: 0;
        transform: scale(0.3);
    }
    50% {
        opacity: 1;
        transform: scale(1.05);
    }
    70% {
        transform: scale(0.9);
    }
    100% {
        opacity: 1;
        transform: scale(1);
    }
}

@keyframes pulseGlow {
    0%, 100% {
        box-shadow: 0 0 5px rgba(64, 158, 255, 0.3);
    }
    50% {
        box-shadow: 0 0 20px rgba(64, 158, 255, 0.6), 0 0 30px rgba(64, 158, 255, 0.4);
    }
}

@keyframes floatY {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-8px);
    }
}

@keyframes gradientShift {
    0% {
        background-position: 0% 50%;
    }
    50% {
        background-position: 100% 50%;
    }
    100% {
        background-position: 0% 50%;
    }
}

@keyframes shimmer {
    0% {
        background-position: -200% 0;
    }
    100% {
        background-position: 200% 0;
    }
}

/* 页面整体动画 */
.group-preview-page {
    padding: 20px;
    animation: slideInFromTop 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 小组头部动画 */
.group-header {
    display: flex;
    align-items: flex-start;
    margin-bottom: 30px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: bounceIn 1s cubic-bezier(0.68, -0.55, 0.265, 1.55) 0.2s both;
}

.group-header:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
    transform: translateY(-3px); /* 减小位移 */
    /* animation: floatY 3s ease-in-out infinite; */ /* 移除无限动画 */
}

/* 头像动画 */
.group-avatar {
    margin-right: 20px;
    flex-shrink: 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
}

.group-avatar:hover {
    transform: scale(1.05);
    /* 移除无限动画 */
    /* animation: pulseGlow 2s ease-in-out infinite; */
}

.group-avatar .el-avatar {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    z-index: 1;
}

.group-avatar:before {
    content: '';
    position: absolute;
    top: -3px;
    left: -3px;
    right: -3px;
    bottom: -3px;
    border: 2px solid transparent;
    border-radius: 50%;
    background: linear-gradient(45deg, #409eff, #67c23a);
    background-size: 200% 200%;
    animation: gradientShift 3s ease infinite;
    opacity: 0;
    transition: opacity 0.3s ease;
    z-index: 0;
}

.group-avatar:hover:before {
    opacity: 0.6; /* 降低不透明度，使效果更加柔和 */
}

/* 头部信息动画 */
.group-header-info {
    flex: 1;
    animation: slideInFromTop 1.2s cubic-bezier(0.25, 0.8, 0.25, 1) 0.4s both;
}

.group-name {
    margin: 0 0 15px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    animation: gradientShift 3s ease infinite;
    background-size: 200% 200%;
}

/* 元信息动画 */
.group-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 15px;
    animation: slideInFromTop 1.4s cubic-bezier(0.25, 0.8, 0.25, 1) 0.6s both;
}

.group-meta .el-tag {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: bounceIn 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.group-meta .el-tag:nth-child(1) { animation-delay: 0.1s; }
.group-meta .el-tag:nth-child(2) { animation-delay: 0.2s; }
.group-meta .el-tag:nth-child(3) { animation-delay: 0.3s; }

.group-meta .el-tag:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.member-count,
.join-type {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: #666;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.member-count:hover,
.join-type:hover {
    color: #409eff;
    transform: scale(1.05);
}

.member-count .el-icon {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.member-count:hover .el-icon {
    transform: rotate(360deg);
}

/* 创建者信息动画 */
.group-creator {
    margin-bottom: 15px;
    animation: slideInFromTop 1.6s cubic-bezier(0.25, 0.8, 0.25, 1) 0.8s both;
}

.creator-link {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    text-decoration: none;
}

.creator-link:hover {
    transform: translateX(5px);
    color: #409eff;
}

.creator-link .el-avatar {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.creator-link:hover .el-avatar {
    transform: scale(1.1);
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.4);
}

/* 描述动画 */
.group-description {
    color: #666;
    line-height: 1.6;
    white-space: pre-line;
    transition: all 0.3s ease;
    animation: slideInFromTop 1.8s cubic-bezier(0.25, 0.8, 0.25, 1) 1s both;
}

.group-description:hover {
    color: #333;
}

/* 操作按钮动画 */
.group-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-left: 20px;
    animation: slideInFromTop 2s cubic-bezier(0.25, 0.8, 0.25, 1) 1.2s both;
}

.group-actions .el-button {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
}

.group-actions .el-button:hover {
    transform: translateY(-2px);
}

.group-actions .el-button--primary {
    background: linear-gradient(45deg, #409eff, #67c23a);
    border: none;
}

.group-actions .el-button--primary:hover {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.group-actions .el-button--warning:hover {
    box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.group-actions .el-button--success:hover {
    box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.group-actions .el-button:before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s;
}

.group-actions .el-button:hover:before {
    left: 100%;
}

/* 介绍区域动画 */
.group-intro {
    animation: slideInFromTop 2.2s cubic-bezier(0.25, 0.8, 0.25, 1) 1.4s both;
}

.group-intro .el-alert {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 8px;
}

.group-intro .el-alert:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(64, 158, 255, 0.1);
}

/* 卡片动画 */
.info-card {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 8px;
}

.info-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.info-card h3 {
    background: linear-gradient(135deg, #409eff, #67c23a);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* 暗色模式适配 */
[data-theme="dark"] .group-header {
    background-color: var(--dark-card-bg);
    border: 1px solid var(--dark-border-color);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .group-header:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .group-name {
    background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

[data-theme="dark"] .group-description {
    color: var(--dark-text-secondary);
}

[data-theme="dark"] .group-description:hover {
    color: var(--dark-text-primary);
}

[data-theme="dark"] .member-count,
[data-theme="dark"] .join-type {
    color: var(--dark-text-secondary);
}

[data-theme="dark"] .member-count:hover,
[data-theme="dark"] .join-type:hover {
    color: var(--primary-color-dark);
}

[data-theme="dark"] .creator-link:hover {
    color: var(--primary-color-dark);
}

[data-theme="dark"] .creator-link:hover .el-avatar {
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

[data-theme="dark"] .group-actions .el-button--primary:hover {
    box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
}

[data-theme="dark"] .group-actions .el-button--warning:hover {
    box-shadow: 0 8px 25px rgba(230, 162, 60, 0.3);
}

[data-theme="dark"] .group-actions .el-button--success:hover {
    box-shadow: 0 8px 25px rgba(103, 194, 58, 0.3);
}

[data-theme="dark"] .group-meta .el-tag:hover {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

[data-theme="dark"] .group-intro .el-alert:hover {
    box-shadow: 0 4px 15px rgba(64, 158, 255, 0.15);
}

[data-theme="dark"] .info-card:hover {
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.group-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 15px;
}

.tag {
    margin-right: 5px;
}

.member-count,
.join-type {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: #666;
}

.group-creator {
    margin-bottom: 15px;
}

.group-description {
    color: #666;
    line-height: 1.6;
    white-space: pre-line;
}

.group-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-left: 20px;
}

.group-intro {
    margin-top: 20px;
}

.feature-card {
    margin-top: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.feature-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.feature-item {
    display: flex;
    align-items: flex-start;
    gap: 15px;
}

.feature-item .el-icon {
    font-size: 20px;
    color: #409EFF;
    margin-top: 4px;
}

.feature-info h4 {
    margin: 0 0 8px 0;
    font-size: 16px;
    color: #333;
}

.feature-info p {
    margin: 0;
    color: #666;
    line-height: 1.5;
}

/* 暗色模式适配 */
[data-theme="dark"] .group-preview-page {
    background-color: transparent;
    color: #ffffff;
}

[data-theme="dark"] .group-header {
    background-color: #1a1a1a;
    border: 1px solid #333333;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .group-name {
    color: #ffffff;
}

[data-theme="dark"] .group-meta {
    color: #909399;
}

[data-theme="dark"] .member-count,
[data-theme="dark"] .join-type {
    color: #909399;
}

[data-theme="dark"] .group-creator {
    color: #909399;
}

[data-theme="dark"] .creator-link {
    color: #409eff;
}

[data-theme="dark"] .creator-link:hover {
    color: #66b1ff;
}

[data-theme="dark"] .group-description {
    color: #909399;
}

[data-theme="dark"] .tag {
    background-color: #262626;
    border-color: #333333;
    color: #909399;
}

[data-theme="dark"] .feature-card {
    background-color: #1a1a1a;
    border: 1px solid #333333;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .card-header h3 {
    color: #ffffff;
}

[data-theme="dark"] .feature-item .el-icon {
    color: #409eff;
}

[data-theme="dark"] .feature-info h4 {
    color: #ffffff;
}

[data-theme="dark"] .feature-info p {
    color: #909399;
}

/* 暗色模式下的 Element Plus 组件 */
[data-theme="dark"] :deep(.el-alert) {
    --el-alert-bg-color: rgba(64, 158, 255, 0.1);
    --el-alert-border-color: #333333;
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-alert .el-alert__title) {
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-alert .el-alert__description) {
    color: #a3a6ad;
}

[data-theme="dark"] :deep(.el-card) {
    --el-card-bg-color: #1a1a1a;
    --el-card-border-color: #333333;
    color: #ffffff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] :deep(.el-card__header) {
    background-color: #1a1a1a;
    border-bottom-color: #333333;
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-empty__description) {
    color: #909399;
}
</style>
