<template>
    <div class="group-detail-page" v-loading="loading">
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
                    <p class="group-description">{{ group.description }}</p>
                </div>
                <div class="group-actions">
                    <template v-if="!userRole">
                        <el-button type="primary" @click="handleJoinGroup">加入小组</el-button>
                    </template>
                    <template v-else>
                        <el-button v-if="userRole === 'CREATOR'" type="primary" @click="showEditGroupDialog">
                            编辑小组
                        </el-button>
                        <el-button v-if="userRole !== 'CREATOR'" type="danger" @click="handleQuitGroup">
                            退出小组
                        </el-button>
                        <el-button v-if="userRole === 'CREATOR'" type="danger" @click="confirmDisbandGroup">
                            解散小组
                        </el-button>
                    </template>
                </div>
            </div>

            <!-- 小组功能区 -->
            <el-tabs v-model="activeTab" class="group-tabs">
                <el-tab-pane label="讨论区" name="posts">
                    <group-posts-tab :group-id="groupId" :user-role="userRole" />
                </el-tab-pane>

                <el-tab-pane label="文件资源" name="files">
                    <group-files-tab :group-id="groupId" :user-role="userRole" />
                </el-tab-pane>

                <el-tab-pane label="成员管理" name="members">
                    <group-members-tab :group-id="groupId" :user-role="userRole" :group="group"
                        @member-updated="refreshGroupDetail" />
                </el-tab-pane>
            </el-tabs>

            <!-- 编辑小组对话框 -->
            <el-dialog v-model="editGroupDialogVisible" title="编辑学习小组" width="50%">
                <el-form :model="groupForm" :rules="groupRules" ref="groupFormRef" label-width="80px">
                    <el-form-item label="名称" prop="name">
                        <el-input v-model="groupForm.name" placeholder="请输入小组名称" />
                    </el-form-item>

                    <el-form-item label="分类" prop="category">
                        <el-select v-model="groupForm.category" placeholder="选择小组分类">
                            <el-option v-for="category in categories" :key="category" :label="category"
                                :value="category" />
                        </el-select>
                    </el-form-item>

                    <el-form-item label="标签" prop="tags">
                        <el-select v-model="groupForm.tags" multiple filterable allow-create default-first-option
                            placeholder="请输入标签（可多选，最多5个）" :max-collapse-tags="2">
                            <el-option v-for="tag in commonTags" :key="tag" :label="tag" :value="tag" />
                        </el-select>
                    </el-form-item>

                    <el-form-item label="加入方式" prop="joinType">
                        <el-radio-group v-model="groupForm.joinType">
                            <el-radio label="PUBLIC">公开（任何人可加入）</el-radio>
                            <el-radio label="PRIVATE">私有（需要审批）</el-radio>
                        </el-radio-group>
                    </el-form-item>

                    <el-form-item label="描述" prop="description">
                        <el-input v-model="groupForm.description" type="textarea" rows="4" placeholder="请输入小组描述" />
                    </el-form-item>
                </el-form>

                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="editGroupDialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="handleUpdateGroup" :loading="submitting">
                            保存
                        </el-button>
                    </span>
                </template>
            </el-dialog>
        </div>

        <el-empty v-else-if="!loading && !group" description="小组不存在或已被删除">
            <el-button type="primary" @click="$router.push('/groups')">返回小组列表</el-button>
        </el-empty>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { User } from '@element-plus/icons-vue';
import { getGroupDetail, updateGroup, joinGroup, quitGroup, disbandGroup, getGroupMembers } from '@/api/group';
import { useAuthStore } from '@/store/auth';
import GroupPostsTab from '@/components/GroupPostsTab.vue';
import GroupFilesTab from '@/components/GroupFilesTab.vue';
import GroupMembersTab from '@/components/GroupMembersTab.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 路由参数
const groupId = computed(() => route.params.id);

// 数据状态
const loading = ref(false);
const submitting = ref(false);
const group = ref(null);
const members = ref([]);
const activeTab = ref('posts');
const editGroupDialogVisible = ref(false);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 表单相关
const groupFormRef = ref(null);
const groupForm = ref({
    name: '',
    category: '',
    tags: [],
    joinType: 'PUBLIC',
    description: ''
});

// 表单验证规则
const groupRules = {
    name: [
        { required: true, message: '请输入小组名称', trigger: 'blur' },
        { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
    ],
    category: [
        { required: true, message: '请选择小组分类', trigger: 'change' }
    ],
    description: [
        { required: true, message: '请输入小组描述', trigger: 'blur' },
        { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
    ]
};

// 选项数据
const categories = [
    '学科学习',
    '考研考证',
    '技能培训',
    '课程讨论',
    '竞赛组队',
    '职业发展',
    '兴趣爱好',
    '其他'
];

const commonTags = [
    '计算机',
    '数学',
    '英语',
    '物理',
    '化学',
    '生物',
    '经济',
    '管理',
    '文学',
    '历史',
    '哲学',
    '艺术',
    '体育',
    '编程',
    '设计',
    '演讲',
    '写作',
    '考研',
    '考证',
    '实习',
    '就业'
];

// 计算属性 - 用户在小组中的角色
const userRole = ref(null);

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

            // 加载小组成员
            await loadGroupMembers();
            
            // 检查当前路径，确保它是 /groups/:id/detail
            const isDetailPath = route.path.includes(`/groups/${groupId.value}/detail`);
            
            // 只有在详情页路径且用户不是小组成员时才重定向
            if (isDetailPath && authStore.isAuthenticated && !userRole.value) {
                console.log('用户不是小组成员，重定向到预览页面');
                router.replace(`/groups/${groupId.value}`);
                return;
            }
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

// 加载小组成员
const loadGroupMembers = async () => {
    try {
        const response = await getGroupMembers(groupId.value);
        if (response.data && response.data.code === 200) {
            members.value = response.data.data || [];

            // 确定当前用户在小组中的角色
            if (authStore.isAuthenticated) {
                const currentUserId = authStore.user?.userId;
                const userMember = members.value.find(m =>
                    m.userId === currentUserId && m.status === 'ACTIVE'
                );

                userRole.value = userMember ? userMember.role : null;
            }
        }
    } catch (error) {
        console.error('加载小组成员失败:', error);
    }
};

// 兼容后端返回的tags为字符串的情况
const normalizeTags = (tags) => {
    if (Array.isArray(tags)) return tags;
    if (typeof tags === 'string') return tags.split(/[，,]/).map(t => t.trim()).filter(Boolean);
    return [];
};

// 刷新小组详情
const refreshGroupDetail = () => {
    loadGroupDetail();
};

// 显示编辑小组对话框
const showEditGroupDialog = () => {
    if (!group.value) return;

    // 设置表单初始值
    groupForm.value = {
        name: group.value.name,
        category: group.value.category,
        tags: group.value.tags || [],
        joinType: group.value.joinType,
        description: group.value.description
    };

    editGroupDialogVisible.value = true;
};

// 更新小组信息
const handleUpdateGroup = async () => {
    if (!groupFormRef.value) return;

    await groupFormRef.value.validate(async (valid) => {
        if (!valid) return;

        submitting.value = true;
        try {
            // 格式化数据以匹配后端期望的格式
            const updateData = {
                name: groupForm.value.name,
                description: groupForm.value.description,
                category: groupForm.value.category,
                joinType: groupForm.value.joinType,
                // 将标签数组转换为逗号分隔的字符串
                tags: Array.isArray(groupForm.value.tags) 
                    ? groupForm.value.tags.join(',') 
                    : groupForm.value.tags,
                avatarUrl: group.value.avatarUrl || '',
                status: group.value.status || 'ACTIVE'
            };

            console.log('Updating group with data:', updateData);
            
            const response = await updateGroup(groupId.value, updateData);
            if (response.data && response.data.code === 200) {
                ElMessage.success('更新小组信息成功');
                editGroupDialogVisible.value = false;
                loadGroupDetail(); // 重新加载小组信息
            } else {
                ElMessage.error(response.data?.message || '更新小组信息失败');
            }
        } catch (error) {
            console.error('更新小组信息失败:', error);
            ElMessage.error('更新小组信息失败，请稍后重试');
        } finally {
            submitting.value = false;
        }
    });
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
            if (group.value && group.value.joinType === 'PRIVATE') {
                ElMessage.info('小组需要审核，请等待管理员批准');
            } else {
                ElMessage.success('已成功加入小组');
                loadGroupDetail(); // 重新加载小组信息
            }
        } else {
            ElMessage.error(response.data?.message || '加入小组失败');
        }
    } catch (error) {
        console.error('加入小组失败:', error);
        ElMessage.error('加入小组失败，请稍后重试');
    }
};

// 退出小组
const handleQuitGroup = async () => {
    ElMessageBox.confirm(
        '确定要退出该小组吗？',
        '退出小组',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        try {
            const response = await quitGroup(groupId.value);
            if (response.data && response.data.code === 200) {
                ElMessage.success('已退出小组');
                loadGroupDetail(); // 重新加载小组信息
            } else {
                ElMessage.error(response.data?.message || '退出小组失败');
            }
        } catch (error) {
            console.error('退出小组失败:', error);
            ElMessage.error('退出小组失败，请稍后重试');
        }
    }).catch(() => {
        // 取消操作
    });
};

// 确认解散小组
const confirmDisbandGroup = () => {
    ElMessageBox.confirm(
        '解散小组后，小组内的所有数据将被永久删除，无法恢复。确定要解散该小组吗？',
        '解散小组',
        {
            confirmButtonText: '确定解散',
            cancelButtonText: '取消',
            type: 'danger'
        }
    ).then(async () => {
        try {
            const response = await disbandGroup(groupId.value);
            if (response.data && response.data.code === 200) {
                ElMessage.success('小组已成功解散');
                router.push('/groups');
            } else {
                ElMessage.error(response.data?.message || '解散小组失败');
            }
        } catch (error) {
            console.error('解散小组失败:', error);
            ElMessage.error('解散小组失败，请稍后重试');
        }
    }).catch(() => {
        // 取消操作
    });
};
</script>

<style scoped>
.group-detail-page {
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

.group-tabs {
    margin-top: 20px;
}
</style>
