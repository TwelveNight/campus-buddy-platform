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
                    <el-button v-if="joinStatus === 'not_joined'" type="primary" @click="handleJoinGroup">加入小组</el-button>
                    <el-button v-else-if="joinStatus === 'pending'" type="warning" disabled>等待审批</el-button>
                    <el-button v-else-if="joinStatus === 'joined'" type="success" disabled>已加入</el-button>
                    <el-button @click="goBack">返回列表</el-button>
                </div>
            </div>

            <!-- 小组介绍信息 -->
            <div class="group-intro">
                <el-alert
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
                    // 如果用户已是小组成员且是通过预览页访问的，重定向到详情页
                    router.push(`/groups/${groupId.value}/detail`);
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
                router.push(`/groups/${groupId.value}/detail`);
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
.group-preview-page {
    padding: 20px;
}

.group-header {
    display: flex;
    align-items: flex-start;
    margin-bottom: 30px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.group-avatar {
    margin-right: 20px;
    flex-shrink: 0;
}

.group-header-info {
    flex: 1;
}

.group-name {
    margin: 0 0 15px 0;
    font-size: 24px;
    font-weight: 600;
    color: #333;
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
    font-size: 24px;
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
</style>
