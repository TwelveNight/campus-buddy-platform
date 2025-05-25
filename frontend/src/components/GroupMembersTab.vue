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
                                    {{ formatTime(row.requestTime) }}
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
import { useRouter } from 'vue-router';
import {
    getGroupMembers,
    setAdmin,
    cancelAdmin,
    removeMember,
    approveJoinRequest,
    rejectJoinRequest,
    type GroupMember
} from '@/api/group';
import { useAuthStore } from '@/store/auth';
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
function goToUserProfile(userId) {
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

// 生命周期钩子
onMounted(() => {
    loadMembers();
    if (canApproveRequests.value) {
        loadJoinRequests();
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
            members.value = allMembers.filter(m => m.status === 'ACTIVE');
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
        const response = await getGroupMembers(props.groupId, 'PENDING');
        if (response.data && response.data.code === 200) {
            let allRequests = [];
            if (response.data.data && response.data.data.records !== undefined) {
                allRequests = response.data.data.records || [];
            } else {
                allRequests = response.data.data || [];
            }
            // 只保留PENDING_APPROVAL或PENDING状态的成员
            joinRequests.value = allRequests.filter(m => m.status === 'PENDING_APPROVAL' || m.status === 'PENDING');
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
.group-members-tab {
    padding: 20px 0;
}

.members-section {
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.members-filter {
    display: flex;
    gap: 15px;
    margin-bottom: 20px;
}

.member-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.member-name {
    display: flex;
    flex-direction: column;
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
