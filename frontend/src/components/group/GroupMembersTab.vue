<template>
    <div class="group-members-tab">
        <div class="members-section">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane label="成员列表" name="members">
                    <div class="members-container" v-loading="loading">
                        <div class="members-filter">
                            <el-input v-model="searchQuery" placeholder="搜索成员" clearable prefix-icon="Search"
                                @input="filterMembers">
                            </el-input>
                            <el-select v-model="roleFilter" placeholder="角色筛选" clearable @change="filterMembers">
                                <el-option label="创建者" value="CREATOR" />
                                <el-option label="管理员" value="ADMIN" />
                                <el-option label="成员" value="MEMBER" />
                            </el-select>
                        </div>

                        <el-empty v-if="filteredMembers.length === 0 && !loading" description="暂无成员" />

                        <el-table v-else :data="filteredMembers" style="width: 100%"
                            :header-cell-style="{ backgroundColor: '#f5f7fa', color: '#606266' }">
                            <el-table-column label="成员" min-width="220">
                                <template #default="scope">
                                    <div class="member-info">
                                        <el-avatar :size="32" :src="scope.row.avatarUrl || defaultAvatar" @click="goToUserProfile(scope.row.userId)" style="cursor:pointer">
                                            {{ (scope.row.nickname || scope.row.username)?.substring(0, 1) }}
                                        </el-avatar>
                                        <div class="member-name">
                                            <span class="nickname" @click="goToUserProfile(scope.row.userId)" style="cursor:pointer">{{ scope.row.nickname || scope.row.username || '未知用户' }}</span>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column label="角色" width="120">
                                <template #default="{ row }">
                                    <el-tag :type="getRoleTagType(row.role)" size="small" effect="plain">
                                        {{ getRoleLabel(row.role) }}
                                    </el-tag>
                                </template>
                            </el-table-column>

                            <el-table-column label="加入时间" width="180">
                                <template #default="{ row }">
                                    {{ formatTime(row.joinedAt) }}
                                </template>
                            </el-table-column>

                            <el-table-column label="操作" width="200" fixed="right">
                                <template #default="{ row }">
                                    <div class="member-actions">
                                        <el-dropdown v-if="canManageMembers && currentUserId !== row.userId"
                                            trigger="click">
                                            <el-button type="primary" size="small" plain>
                                                管理 <el-icon class="el-icon--right"><arrow-down /></el-icon>
                                            </el-button>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <!-- 管理员相关操作 -->
                                                    <template v-if="isCreator && row.role !== 'CREATOR'">
                                                        <!-- 设置/取消管理员 -->
                                                        <el-dropdown-item v-if="row.role !== 'ADMIN'"
                                                            @click="handleSetAdmin(row)">
                                                            <el-icon>
                                                                <Star />
                                                            </el-icon> 设为管理员
                                                        </el-dropdown-item>
                                                        <el-dropdown-item v-else @click="handleCancelAdmin(row)">
                                                            <el-icon>
                                                                <StarFilled />
                                                            </el-icon> 取消管理员
                                                        </el-dropdown-item>
                                                    </template>

                                                    <!-- 移除成员操作 -->
                                                    <el-dropdown-item
                                                        v-if="(isCreator || (isAdmin && row.role === 'MEMBER'))"
                                                        @click="handleRemoveMember(row)" class="danger-item">
                                                        <el-icon>
                                                            <Delete />
                                                        </el-icon> 移除成员
                                                    </el-dropdown-item>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </el-tab-pane>

                <el-tab-pane label="加入申请" name="requests" v-if="canApproveRequests">
                    <div class="requests-container" v-loading="requestsLoading">
                        <el-empty v-if="joinRequests.length === 0 && !requestsLoading" description="暂无申请" />

                        <el-table v-else :data="joinRequests" style="width: 100%"
                            :header-cell-style="{ backgroundColor: '#f5f7fa', color: '#606266' }">
                            <el-table-column label="申请人" min-width="220">
                                <template #default="{ row }">
                                    <div class="member-info">
                                        <el-avatar :size="32" :src="row.avatarUrl || defaultAvatar" @click="goToUserProfile(row.userId)" style="cursor:pointer">
                                            {{ (row.nickname || row.username)?.substring(0, 1) }}
                                        </el-avatar>
                                        <div class="member-name">
                                            <span class="nickname" @click="goToUserProfile(row.userId)" style="cursor:pointer">{{ row.nickname || row.username || '未知用户' }}</span>
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column label="申请时间" width="180">
                                <template #default="{ row }">
                                    {{ formatTime(row.joinedAt) }}
                                </template>
                            </el-table-column>

                            <el-table-column label="状态" width="120">
                                <template #default="{ row }">
                                    <el-tag type="warning" size="small" effect="plain">
                                        待审核
                                    </el-tag>
                                </template>
                            </el-table-column>

                            <el-table-column label="操作" width="220" fixed="right">
                                <template #default="{ row }">
                                    <div class="request-actions">
                                        <el-button type="primary" size="small" @click="handleApproveRequest(row)">
                                            通过
                                        </el-button>
                                        <el-button type="danger" size="small" plain @click="handleRejectRequest(row)">
                                            拒绝
                                        </el-button>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter, useRoute } from 'vue-router';
import {
    getGroupMembers,
    setAdmin,
    cancelAdmin,
    removeMember,
    approveJoinRequest,
    rejectJoinRequest,
    type GroupMember
} from '../../api/group';
import { useAuthStore } from '../../store/auth';
import {
    Search,
    ArrowDown,
    Star,
    StarFilled,
    Delete
} from '@element-plus/icons-vue';

// 属性定义
const props = defineProps({
    groupId: {
        type: [String, Number],
        required: true
    },
    userRole: {
        type: String,
        default: null
    },
    group: {
        type: Object,
        default: () => ({})
    },
    subtab: {
        type: String,
        default: null
    }
});

// 事件
const emits = defineEmits(['member-updated']);

// 数据状态
const authStore = useAuthStore();
const loading = ref(false);
const requestsLoading = ref(false);
const members = ref<GroupMember[]>([]);
const joinRequests = ref<GroupMember[]>([]);
const filteredMembers = ref<GroupMember[]>([]);
const searchQuery = ref('');
const roleFilter = ref('');
const activeTab = ref('members');
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const router = useRouter();
const route = useRoute();
function goToUserProfile(userId: string | number) {
    router.push(`/user/${userId}`);
}

// 计算属性
const currentUserId = computed(() => authStore.user?.userId);

// 是否为创建者
const isCreator = computed(() => props.userRole === 'CREATOR');

// 是否为管理员
const isAdmin = computed(() => props.userRole === 'ADMIN');

// 是否可以管理成员（创建者和管理员）
const canManageMembers = computed(() =>
    ['CREATOR', 'ADMIN'].includes(props.userRole)
);

// 是否可以审批申请（创建者和管理员）
const canApproveRequests = computed(() =>
    ['CREATOR', 'ADMIN'].includes(props.userRole) && props.group.joinType === 'APPROVAL'
);

// 监视属性变化
watch(() => props.groupId, (newVal) => {
    if (newVal) {
        loadMembers();
        if (canApproveRequests.value) {
            loadJoinRequests();
        }
    }
});

// 监听路由查询参数变化
watch(() => route.query.subtab, (newSubtab) => {
    if (newSubtab === 'requests' && canApproveRequests.value) {
        activeTab.value = 'requests';
    }
});

// 监听 props.subtab 变化
watch(() => props.subtab, (newSubtab) => {
    if (newSubtab === 'requests' && canApproveRequests.value) {
        activeTab.value = 'requests';
    }
}, { immediate: true });

// 生命周期钩子
onMounted(() => {
    loadMembers();
    if (canApproveRequests.value) {
        loadJoinRequests();
    }
    
    // 检查查询参数并设置相应的子标签页
    const subtabFromQuery = route.query.subtab || props.subtab;
    if (subtabFromQuery === 'requests' && canApproveRequests.value) {
        activeTab.value = 'requests';
    }
});

// 加载小组成员
const loadMembers = async () => {
    loading.value = true;
    try {
        const response = await getGroupMembers(props.groupId);
        if (response.data && response.data.code === 200) {
            // 只保留ACTIVE成员
            let allMembers = [];
            if (response.data.data && response.data.data.records !== undefined) {
                allMembers = response.data.data.records || [];
            } else {
                allMembers = response.data.data || [];
            }
            members.value = allMembers.filter((m: GroupMember) => m.status === 'ACTIVE');
            filterMembers();
        } else {
            ElMessage.error(response.data?.message || '加载小组成员失败');
            members.value = [];
        }
    } catch (error) {
        console.error('加载小组成员失败:', error);
        ElMessage.error('加载小组成员失败，请稍后重试');
        members.value = [];
    } finally {
        loading.value = false;
    }
};

// 加载加入申请
const loadJoinRequests = async () => {
    if (!canManageMembers.value) return;

    requestsLoading.value = true;
    try {
        const response = await getGroupMembers(props.groupId);
        if (response.data && response.data.code === 200) {
            let allRequests = [];
            if (response.data.data && response.data.data.records !== undefined) {
                allRequests = response.data.data.records || [];
            } else {
                allRequests = response.data.data || [];
            }
            // 只保留PENDING状态的成员
            joinRequests.value = allRequests.filter((m: GroupMember) => m.status === 'PENDING_APPROVAL');
        } else {
            ElMessage.error(response.data?.message || '加载加入申请失败');
            joinRequests.value = [];
        }
    } catch (error) {
        console.error('加载加入申请失败:', error);
        ElMessage.error('加载加入申请失败，请稍后重试');
        joinRequests.value = [];
    } finally {
        requestsLoading.value = false;
    }
};

// 筛选成员
const filterMembers = () => {
    filteredMembers.value = members.value.filter(member => {
        const matchesSearch = !searchQuery.value ||
            (member.username && member.username.toLowerCase().includes(searchQuery.value.toLowerCase())) ||
            (member.nickname && member.nickname.toLowerCase().includes(searchQuery.value.toLowerCase())) ||
            (member.realName && member.realName.toLowerCase().includes(searchQuery.value.toLowerCase()));

        const matchesRole = !roleFilter.value || member.role === roleFilter.value;

        return matchesSearch && matchesRole;
    });
};

// 标签页切换处理
const handleTabChange = (tab: string) => {
    if (tab === 'requests' && canApproveRequests.value) {
        loadJoinRequests();
    }
};

// 设置为管理员
const handleSetAdmin = async (member: GroupMember) => {
    try {
        const response = await setAdmin(props.groupId, member.userId);
        if (response.data && response.data.code === 200) {
            ElMessage.success(`已将 ${member.username} 设为管理员`);
            loadMembers();
            emits('member-updated');
        } else {
            ElMessage.error(response.data?.message || '设置管理员失败');
        }
    } catch (error) {
        console.error('设置管理员失败:', error);
        ElMessage.error('设置管理员失败，请稍后重试');
    }
};

// 取消管理员
const handleCancelAdmin = async (member: GroupMember) => {
    try {
        const response = await cancelAdmin(props.groupId, member.userId);
        if (response.data && response.data.code === 200) {
            ElMessage.success(`已取消 ${member.username} 的管理员权限`);
            loadMembers();
            emits('member-updated');
        } else {
            ElMessage.error(response.data?.message || '取消管理员失败');
        }
    } catch (error) {
        console.error('取消管理员失败:', error);
        ElMessage.error('取消管理员失败，请稍后重试');
    }
};

// 确认移除成员
const handleRemoveMember = (member: GroupMember) => {
    ElMessageBox.confirm(
        `确定要将 ${member.username} 移出小组吗？`,
        '移出成员',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        try {
            const response = await removeMember(props.groupId, member.userId);
            if (response.data && response.data.code === 200) {
                ElMessage.success(`已将 ${member.username} 移出小组`);
                loadMembers();
                emits('member-updated');
            } else {
                ElMessage.error(response.data?.message || '移出成员失败');
            }
        } catch (error) {
            console.error('移出成员失败:', error);
            ElMessage.error('移出成员失败，请稍后重试');
        }
    }).catch(() => {
        // 取消操作
    });
};

// 通过加入申请
const handleApproveRequest = async (request: GroupMember) => {
    try {
        const response = await approveJoinRequest(props.groupId, request.userId);
        if (response.data && response.data.code === 200) {
            ElMessage.success(`已通过 ${request.username} 的加入申请`);
            loadJoinRequests();
            loadMembers();
            emits('member-updated');
        } else {
            ElMessage.error(response.data?.message || '审批申请失败');
        }
    } catch (error) {
        console.error('审批申请失败:', error);
        ElMessage.error('审批申请失败，请稍后重试');
    }
};

// 拒绝加入申请
const handleRejectRequest = async (request: GroupMember) => {
    try {
        const response = await rejectJoinRequest(props.groupId, request.userId);
        if (response.data && response.data.code === 200) {
            ElMessage.success(`已拒绝 ${request.username} 的加入申请`);
            loadJoinRequests();
        } else {
            ElMessage.error(response.data?.message || '拒绝申请失败');
        }
    } catch (error) {
        console.error('拒绝申请失败:', error);
        ElMessage.error('拒绝申请失败，请稍后重试');
    }
};

// 工具函数 - 获取角色标签类型
const getRoleTagType = (role: string) => {
    switch (role) {
        case 'CREATOR': return 'danger';
        case 'ADMIN': return 'warning';
        case 'MEMBER': return 'info';
        default: return 'info';
    }
};

// 工具函数 - 获取角色标签文本
const getRoleLabel = (role: string) => {
    switch (role) {
        case 'CREATOR': return '创建者';
        case 'ADMIN': return '管理员';
        case 'MEMBER': return '成员';
        default: return '未知';
    }
};

// 工具函数 - 格式化时间
const formatTime = (timestamp: string | undefined) => {
    if (!timestamp) return '';

    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN');
};
</script>

<style scoped>
/* 动画效果定义 */
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideInLeft {
    from {
        opacity: 0;
        transform: translateX(-30px);
    }
    to {
        opacity: 1;
        transform: translateX(0);
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
        box-shadow: 0 0 15px rgba(64, 158, 255, 0.6);
    }
}

@keyframes shakeX {
    0%, 100% {
        transform: translateX(0);
    }
    25% {
        transform: translateX(-3px);
    }
    75% {
        transform: translateX(3px);
    }
}

@keyframes floatY {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-5px);
    }
}

/* 组件整体动画 */
.group-members-tab {
    padding: 20px 0;
    animation: fadeInUp 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.members-section {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: slideInLeft 1s cubic-bezier(0.25, 0.8, 0.25, 1) 0.2s both;
}

.members-section:hover {
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
}

/* 筛选区域动画 */
.members-filter {
    display: flex;
    gap: 15px;
    margin-bottom: 20px;
    animation: fadeInUp 1.2s cubic-bezier(0.25, 0.8, 0.25, 1) 0.4s both;
}

.members-filter .el-input,
.members-filter .el-select {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.members-filter .el-input:hover,
.members-filter .el-select:hover {
    transform: translateY(-2px);
}

/* 成员信息动画 */
.member-info {
    display: flex;
    align-items: center;
    gap: 10px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.member-info:hover {
    transform: translateX(5px);
}

.member-info .el-avatar {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
}

.member-info .el-avatar:hover {
    transform: scale(1.1);
    animation: pulseGlow 1.5s ease-in-out;
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.4);
}

.member-name {
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;
}

.nickname {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
}

.nickname:hover {
    color: #409eff;
    transform: translateX(3px);
}

/* 角色标签动画 */
.el-tag {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-tag:hover {
    transform: scale(1.05);
    animation: floatY 1s ease-in-out infinite;
}

/* 操作按钮动画 */
.member-actions {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.member-actions .el-button {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.member-actions .el-button:hover {
    transform: translateY(-2px) scale(1.02);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.member-actions .el-button.el-button--danger:hover {
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
    animation: shakeX 0.5s ease-in-out;
}

/* 表格行动画 */
.el-table :deep(.el-table__row) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-table :deep(.el-table__row):hover {
    background-color: rgba(64, 158, 255, 0.05) !important;
    transform: scale(1.01);
}

/* 下拉菜单动画 */
.el-dropdown-menu :deep(.el-dropdown-menu__item) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-dropdown-menu :deep(.el-dropdown-menu__item):hover {
    background-color: rgba(64, 158, 255, 0.1);
    transform: translateX(5px);
}

.danger-item:hover {
    background-color: rgba(245, 108, 108, 0.1) !important;
    color: #f56c6c !important;
}

/* 空状态动画 */
.el-empty {
    animation: bounceIn 1s cubic-bezier(0.68, -0.55, 0.265, 1.55) 0.6s both;
}

/* 加载状态优化 */
.el-loading-mask {
    background-color: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(5px);
}

/* 标签页动画 */
.el-tabs :deep(.el-tabs__item) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-tabs :deep(.el-tabs__item):hover {
    transform: translateY(-2px);
}

.el-tabs :deep(.el-tabs__active-bar) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 审批按钮特殊动画 */
.approve-btn {
    background: linear-gradient(45deg, #67c23a, #85ce61);
    border: none;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.approve-btn:hover {
    background: linear-gradient(45deg, #85ce61, #67c23a);
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

.reject-btn {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.reject-btn:hover {
    animation: shakeX 0.5s ease-in-out;
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
}

/* 暗色模式适配 */
[data-theme="dark"] .members-section {
    background: var(--dark-card-bg);
    border: 1px solid var(--dark-border-color);
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .members-section:hover {
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .member-info .el-avatar:hover {
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);
}

[data-theme="dark"] .nickname:hover {
    color: var(--primary-color-dark);
}

[data-theme="dark"] .member-actions .el-button:hover {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

[data-theme="dark"] .member-actions .el-button.el-button--danger:hover {
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}

[data-theme="dark"] .el-table :deep(.el-table__row):hover {
    background-color: rgba(64, 158, 255, 0.08) !important;
}

[data-theme="dark"] .approve-btn:hover {
    box-shadow: 0 6px 20px rgba(103, 194, 58, 0.3);
}

[data-theme="dark"] .reject-btn:hover {
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.member-detail {
    font-size: 12px;
    color: #999;
}

.member-actions,
.request-actions {
    display: flex;
    gap: 10px;
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
}

/* 表格内的危险操作项 */
.danger-item {
    color: #f56c6c;
}

/* 暗色模式适配 */
[data-theme="dark"] .group-members-tab {
    background-color: transparent;
    color: #ffffff;
}

[data-theme="dark"] .members-section {
    background-color: #1a1a1a;
    border: 1px solid #333333;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .member-info {
    color: #ffffff;
}

[data-theme="dark"] .member-name .nickname {
    color: #ffffff;
}

[data-theme="dark"] .member-name .nickname:hover {
    color: #409eff;
}

[data-theme="dark"] .member-detail {
    color: #909399;
}

[data-theme="dark"] .danger-item {
    color: #f78989;
}

/* 暗色模式下的表格样式 */
[data-theme="dark"] :deep(.el-table) {
    --el-table-bg-color: #1a1a1a;
    --el-table-tr-bg-color: #1a1a1a;
    --el-table-header-bg-color: #262626;
    --el-table-header-text-color: #ffffff;
    --el-table-text-color: #ffffff;
    --el-table-border-color: #333333;
    --el-table-row-hover-bg-color: #2a2a2e;
}

[data-theme="dark"] :deep(.el-table th) {
    background-color: #262626 !important;
    color: #ffffff !important;
    border-color: #333333 !important;
}

[data-theme="dark"] :deep(.el-table td) {
    background-color: #1a1a1a !important;
    color: #ffffff !important;
    border-color: #333333 !important;
}

[data-theme="dark"] :deep(.el-table__body tr:hover > td) {
    background-color: #2a2a2e !important;
}

[data-theme="dark"] :deep(.el-table__empty-block) {
    background-color: #1a1a1a !important;
}

/* 暗色模式下的 Element Plus 组件 */
[data-theme="dark"] :deep(.el-tabs__nav-wrap::after) {
    background-color: #333333;
}

[data-theme="dark"] :deep(.el-tabs__active-bar) {
    background-color: #409eff;
}

[data-theme="dark"] :deep(.el-tabs__item) {
    color: #909399;
}

[data-theme="dark"] :deep(.el-tabs__item.is-active) {
    color: #409eff;
}

[data-theme="dark"] :deep(.el-tabs__item:hover) {
    color: #66b1ff;
}

[data-theme="dark"] :deep(.el-input__wrapper) {
    background-color: #262626;
    box-shadow: 0 0 0 1px #333333 inset;
}

[data-theme="dark"] :deep(.el-input__inner) {
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-select .el-input) {
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-select-dropdown) {
    background-color: #1a1a1a;
    border-color: #333333;
}

[data-theme="dark"] :deep(.el-select-dropdown__item) {
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-select-dropdown__item.hover, .el-select-dropdown__item:hover) {
    background-color: rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] :deep(.el-select-dropdown__item.selected) {
    background-color: rgba(64, 158, 255, 0.2);
    color: #409eff;
}

[data-theme="dark"] :deep(.el-tag) {
    background-color: #262626;
    border-color: #333333;
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-tag.el-tag--info) {
    --el-tag-info-color: #ffffff;
    background-color: rgba(144, 147, 153, 0.2);
}

[data-theme="dark"] :deep(.el-tag.el-tag--warning) {
    --el-tag-warning-color: #ffffff;
    background-color: rgba(230, 162, 60, 0.2);
}

[data-theme="dark"] :deep(.el-tag.el-tag--danger) {
    --el-tag-danger-color: #ffffff;
    background-color: rgba(245, 108, 108, 0.2);
}

[data-theme="dark"] :deep(.el-dropdown-menu) {
    background-color: #1a1a1a;
    border: 1px solid #333333;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.5);
}

[data-theme="dark"] :deep(.el-dropdown-menu__item) {
    color: #ffffff;
}

[data-theme="dark"] :deep(.el-dropdown-menu__item:hover) {
    background-color: rgba(255, 255, 255, 0.1);
    color: #409eff;
}

[data-theme="dark"] :deep(.el-empty__description) {
    color: #909399;
}
</style>
