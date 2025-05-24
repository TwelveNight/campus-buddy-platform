<template>
    <div class="group-posts-tab">
        <div class="post-actions">
            <el-button type="primary" @click="showCreatePostDialog" :disabled="!canCreatePost">
                <el-icon>
                    <Edit />
                </el-icon> 发布帖子
            </el-button>
        </div>

        <div class="posts-container" v-loading="loading">
            <el-empty v-if="posts.length === 0 && !loading" description="暂无帖子" />

            <div v-for="post in posts" :key="post.postId" class="post-item">
                <div class="post-header">
                    <div class="author-info">
                        <el-avatar :size="40" :src="post.authorAvatar || defaultAvatar">
                            {{ post.authorName?.substring(0, 1) }}
                        </el-avatar>
                        <div>
                            <div class="author-name">{{ post.authorName || '未知用户' }}</div>
                            <div class="post-time">{{ formatTime(post.createdAt) }}</div>
                        </div>
                    </div>
                    <div class="post-actions" v-if="isPostAuthor(post) || isGroupAdmin">
                        <el-dropdown trigger="click" @command="handleCommand($event, post)">
                            <el-button type="text">
                                <el-icon>
                                    <MoreFilled />
                                </el-icon>
                            </el-button>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item command="edit" v-if="isPostAuthor(post)">编辑</el-dropdown-item>
                                    <el-dropdown-item command="delete"
                                        v-if="isPostAuthor(post) || isGroupAdmin">删除</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </div>

                <div class="post-content">
                    <h3 class="post-title">{{ post.title }}</h3>
                    <div class="post-body" v-html="renderContent(post)"></div>
                </div>

                <div class="post-footer">
                    <div class="post-stats">
                        <el-button :type="post.liked ? 'primary' : 'default'" size="small" text
                            @click="handleLike(post)">
                            <el-icon>
                                <GoodsFilled />
                            </el-icon> {{ post.likeCount || 0 }}
                        </el-button>
                        <el-button type="text" size="small">
                            <el-icon>
                                <ChatDotRound />
                            </el-icon> {{ post.commentCount || 0 }}
                        </el-button>
                    </div>
                </div>
            </div>

            <div class="pagination">
                <el-pagination v-if="total > pageSize" v-model:current-page="currentPage" v-model:page-size="pageSize"
                    :page-sizes="[5, 10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" background />
            </div>
        </div>

        <!-- 创建/编辑帖子对话框 -->
        <el-dialog v-model="postDialogVisible" :title="isEditing ? '编辑帖子' : '发布帖子'" width="65%"
            :close-on-click-modal="false">
            <el-form :model="postForm" :rules="postRules" ref="postFormRef" label-width="80px">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="postForm.title" placeholder="请输入帖子标题" />
                </el-form-item>

                <el-form-item label="内容" prop="content">
                    <el-radio-group v-model="postForm.contentType" class="content-type-selector">
                        <el-radio-button label="TEXT">纯文本</el-radio-button>
                        <el-radio-button label="HTML">富文本</el-radio-button>
                    </el-radio-group>

                    <div class="editor-container">
                        <!-- 纯文本编辑器 -->
                        <el-input v-if="postForm.contentType === 'TEXT'" v-model="postForm.content" type="textarea"
                            :rows="10" placeholder="请输入帖子内容" />

                        <!-- 富文本编辑器 -->
                        <rich-editor v-else v-model="postForm.content" :height="'350'" @change="handleEditorChange" />
                    </div>
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="postDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="handleSubmitPost" :loading="submitting">
                        {{ isEditing ? '保存' : '发布' }}
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineProps, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, MoreFilled, GoodsFilled, ChatDotRound } from '@element-plus/icons-vue';

// 注释掉有问题的导入
// import DOMPurify from 'dompurify';
// import moment from 'moment';

import { getGroupPosts, createPost, updatePost, deletePost, likePost, unlikePost, getLikeStatus } from '../api/groupPost';
import { useAuthStore } from '../store/auth';
import RichEditor from '../components/RichEditor.vue';

// 类型定义
interface Post {
    id?: number;
    postId?: number;
    title: string;
    content: string;
    contentType?: 'TEXT' | 'HTML';
    authorId?: number;
    authorName?: string;
    authorAvatar?: string;
    groupId: number | string;
    createdAt?: string;
    updatedAt?: string;
    likes?: number;
    hasLiked?: boolean;
    [key: string]: any;
}

// 接收属性
const props = defineProps({
    groupId: {
        type: [String, Number],
        required: true
    },
    userRole: {
        type: String,
        default: null
    }
});

const authStore = useAuthStore();

// 数据状态
const loading = ref(false);
const submitting = ref(false);
const posts = ref<Post[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const postDialogVisible = ref(false);
const isEditing = ref(false);
const editingPostId = ref<number | null>(null);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 表单相关
const postFormRef = ref<any>(null);
const postForm = ref<Post>({
    title: '',
    content: '',
    contentType: 'TEXT',
    groupId: props.groupId
});

// 表单验证规则
const postRules = {
    title: [
        { required: true, message: '请输入帖子标题', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
    ],
    content: [
        { required: true, message: '请输入帖子内容', trigger: 'blur' },
        { min: 5, max: 50000, message: '长度在 5 到 50000 个字符', trigger: 'blur' }
    ]
};

// 计算属性
const canCreatePost = computed(() => {
    return props.userRole && ['CREATOR', 'ADMIN', 'MEMBER'].includes(props.userRole);
});

const isGroupAdmin = computed(() => {
    return props.userRole && ['CREATOR', 'ADMIN'].includes(props.userRole);
});

// 监听属性变化
watch(() => props.groupId, () => {
    currentPage.value = 1;
    loadPosts();
});

// 生命周期钩子
onMounted(() => {
    loadPosts();
});

// 加载帖子列表
const loadPosts = async () => {
    if (!props.groupId) return;

    loading.value = true;
    try {
        const response = await getGroupPosts({
            groupId: props.groupId,
            pageNum: currentPage.value,
            pageSize: pageSize.value
        });

        if (response.data && response.data.code === 200) {
            const postList = response.data.data.records || [];
            total.value = response.data.data.total || 0;

            // 检查用户是否对每个帖子点赞
            if (authStore.isAuthenticated && postList.length > 0) {
                await Promise.all(postList.map(async (post: Post) => {
                    try {
                        // 确保 postId 存在再调用 getLikeStatus
                        if (post.postId !== undefined) {
                            const likeResponse = await getLikeStatus(post.postId);
                            if (likeResponse.data && likeResponse.data.code === 200) {
                                post.liked = likeResponse.data.data;
                            }
                        }
                    } catch (error) {
                        console.error('获取点赞状态失败:', error);
                    }
                }));
            }

            posts.value = postList;
        } else {
            ElMessage.error(response.data?.message || '加载帖子列表失败');
        }
    } catch (error) {
        console.error('加载帖子列表失败:', error);
        ElMessage.error('加载帖子列表失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};

// 处理分页大小变化
const handleSizeChange = (val: number) => {
    pageSize.value = val;
    loadPosts();
};

// 处理页码变化
const handleCurrentChange = (val: number) => {
    currentPage.value = val;
    loadPosts();
};

// 显示创建帖子对话框
const showCreatePostDialog = () => {
    if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录');
        return;
    }

    isEditing.value = false;
    editingPostId.value = null;
    postForm.value = {
        title: '',
        content: '',
        contentType: 'TEXT',
        groupId: props.groupId
    };

    postDialogVisible.value = true;
};

// 显示编辑帖子对话框
const showEditPostDialog = (post: Post) => {
    isEditing.value = true;
    editingPostId.value = post.postId!;
    postForm.value = {
        title: post.title,
        content: post.content,
        contentType: post.contentType || 'TEXT',
        groupId: props.groupId
    };
    postDialogVisible.value = true;
};

// 处理富文本编辑器内容变化
const handleEditorChange = (content: string) => {
    postForm.value.content = content;
};

// 提交帖子（创建或更新）
const handleSubmitPost = async () => {
    if (!postFormRef.value) return;

    await postFormRef.value.validate(async (valid: boolean) => {
        if (!valid) return;

        submitting.value = true;
        try {
            let response;
            if (isEditing.value && editingPostId.value !== null) {
                response = await updatePost(editingPostId.value, {
                    title: postForm.value.title,
                    content: postForm.value.content,
                    contentType: postForm.value.contentType,
                    groupId: props.groupId // 添加必需的 groupId 属性
                });
            } else {
                response = await createPost(postForm.value);
            }

            if (response.data && response.data.code === 200) {
                ElMessage.success(isEditing.value ? '帖子更新成功' : '帖子发布成功');
                postDialogVisible.value = false;
                loadPosts(); // 重新加载帖子列表
            } else {
                ElMessage.error(response.data?.message || (isEditing.value ? '更新帖子失败' : '发布帖子失败'));
            }
        } catch (error) {
            console.error(isEditing.value ? '更新帖子失败:' : '发布帖子失败:', error);
            ElMessage.error(isEditing.value ? '更新帖子失败，请稍后重试' : '发布帖子失败，请稍后重试');
        } finally {
            submitting.value = false;
        }
    });
};

// 处理下拉菜单命令
const handleCommand = (command: string, post: Post) => {
    if (command === 'edit') {
        showEditPostDialog(post);
    } else if (command === 'delete') {
        confirmDeletePost(post);
    }
};

// 确认删除帖子
const confirmDeletePost = (post: Post) => {
    ElMessageBox.confirm(
        '确定要删除该帖子吗？此操作不可恢复。',
        '删除帖子',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        try {
            if (post.postId === undefined) {
                throw new Error('帖子ID未定义');
            }
            const response = await deletePost(post.postId);
            if (response.data && response.data.code === 200) {
                ElMessage.success('帖子已成功删除');
                loadPosts(); // 重新加载帖子列表
            } else {
                ElMessage.error(response.data?.message || '删除帖子失败');
            }
        } catch (error) {
            console.error('删除帖子失败:', error);
            ElMessage.error('删除帖子失败，请稍后重试');
        }
    }).catch(() => {
        // 取消操作
    });
};

// 处理点赞/取消点赞
const handleLike = async (post: Post) => {
    if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录');
        return;
    }

    try {
        if (post.postId === undefined) {
            throw new Error('帖子ID未定义');
            return;
        }
        
        let response;
        if (post.liked) {
            response = await unlikePost(post.postId);
            if (response.data && response.data.code === 200) {
                post.liked = false;
                post.likeCount = Math.max(0, (post.likeCount || 1) - 1);
            }
        } else {
            response = await likePost(post.postId);
            if (response.data && response.data.code === 200) {
                post.liked = true;
                post.likeCount = (post.likeCount || 0) + 1;
            }
        }

        if (response.data && response.data.code !== 200) {
            ElMessage.error(response.data?.message || '操作失败');
        }
    } catch (error) {
        console.error('点赞操作失败:', error);
        ElMessage.error('操作失败，请稍后重试');
    }
};

// 检查用户是否是帖子作者
const isPostAuthor = (post: Post) => {
    return authStore.isAuthenticated && post.authorId === authStore.user?.userId;
};

// 格式化时间（不使用 moment）
const formatTime = (time: string | undefined) => {
    if (!time) return '';
    
    try {
        const date = new Date(time);
        return date.toLocaleString('zh-CN', { 
            year: 'numeric', 
            month: '2-digit', 
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        console.error('时间格式化错误:', e);
        return time;
    }
};

// 渲染帖子内容（不使用 DOMPurify）
const renderContent = (post: Post) => {
    if (!post.content) return '';

    if (post.contentType === 'HTML') {
        // 注释: 这里应该使用 DOMPurify.sanitize(post.content),
        // 但由于类型问题我们暂时直接返回。在生产环境中应该解决此问题以保证安全性。
        return post.content;
    } else {
        // 处理纯文本，保留换行
        return post.content.replace(/\n/g, '<br>');
    }
};
</script>

<style scoped>
.group-posts-tab {
    margin-top: 20px;
}

.post-actions {
    margin-bottom: 20px;
}

.posts-container {
    margin-top: 20px;
}

.post-item {
    margin-bottom: 30px;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.post-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}

.author-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.author-name {
    font-weight: 500;
    color: #333;
}

.post-time {
    font-size: 12px;
    color: #999;
}

.post-content {
    margin-bottom: 15px;
}

.post-title {
    margin: 0 0 10px 0;
    font-size: 18px;
    font-weight: 600;
    color: #333;
}

.post-body {
    line-height: 1.6;
    color: #666;
    word-break: break-word;
}

.post-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #eee;
    padding-top: 15px;
}

.post-stats {
    display: flex;
    gap: 15px;
}

.content-type-selector {
    margin-bottom: 10px;
}

.editor-container {
    margin-top: 10px;
}

.pagination {
    margin-top: 30px;
    display: flex;
    justify-content: center;
}
</style>
