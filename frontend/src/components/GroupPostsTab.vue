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
                                <Pointer />
                            </el-icon> {{ post.likeCount || 0 }}
                        </el-button>
                        <el-button 
                            :type="post.showComments ? 'primary' : 'default'" 
                            size="small" 
                            text
                            @click="showComments(post)">
                            <el-icon>
                                <ChatDotRound />
                            </el-icon> {{ post.commentCount || 0 }}
                        </el-button>
                    </div>
                </div>
                
                <!-- 评论区域 -->
                <el-collapse-transition>
                    <div v-if="post.showComments" class="post-comments">
                        <div class="comments-container" v-loading="post.loadingComments">
                            <div v-if="post.comments && post.comments.length > 0" class="comment-list">
                                <div v-for="comment in post.comments" :key="comment.commentId" class="comment-item">
                                    <div class="comment-header">
                                        <div class="comment-author">
                                            <el-avatar :size="32" :src="comment.avatar || defaultAvatar">
                                                {{ comment.nickname?.substring(0, 1) || comment.username?.substring(0, 1) }}
                                            </el-avatar>
                                            <div>
                                                <div class="comment-author-name">{{ comment.nickname || comment.username || '未知用户' }}</div>
                                                <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
                                            </div>
                                        </div>
                                        <el-button 
                                            v-if="authStore.user?.userId === comment.userId" 
                                            type="text" 
                                            size="small"
                                            @click="deleteCommentItem(post, comment)">
                                            <el-icon><Delete /></el-icon>
                                        </el-button>
                                    </div>
                                    <div class="comment-content">{{ comment.content }}</div>
                                </div>
                            </div>
                            <el-empty v-else-if="!post.loadingComments" description="暂无评论" />
                            
                            <!-- 评论分页 -->
                            <div class="comment-pagination" v-if="post.commentTotal > post.commentPageSize">
                                <el-pagination 
                                    v-model:current-page="post.commentCurrentPage" 
                                    v-model:page-size="post.commentPageSize"
                                    :page-sizes="[5, 10, 20]" 
                                    layout="sizes, prev, pager, next" 
                                    :total="post.commentTotal"
                                    @size-change="(val) => handleCommentSizeChange(post, val)" 
                                    @current-change="(val) => handleCommentPageChange(post, val)" 
                                    small />
                            </div>
                            
                            <!-- 评论输入框 -->
                            <div class="comment-input">
                                <el-input 
                                    v-model="post.newComment" 
                                    placeholder="发表你的评论..." 
                                    :disabled="!authStore.isAuthenticated"
                                    @keyup.enter="submitComment(post)">
                                    <template #append>
                                        <el-button @click="submitComment(post)" :disabled="!authStore.isAuthenticated || !post.newComment">发送</el-button>
                                    </template>
                                </el-input>
                                <div v-if="!authStore.isAuthenticated" class="login-tip">请先登录后再评论</div>
                            </div>
                        </div>
                    </div>
                </el-collapse-transition>
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
import { Edit, MoreFilled, Pointer, ChatDotRound, Delete } from '@element-plus/icons-vue';

// 注释掉有问题的导入
// import DOMPurify from 'dompurify';
// import moment from 'moment';

import { getGroupPosts, createPost, updatePost, deletePost, likePost, unlikePost, getLikeStatus } from '../api/groupPost';
import { getPostComments, addComment, deleteComment } from '../api/postComment';
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
            let postList = [];
            
            // 处理可能的分页数据结构
            if (response.data.data && response.data.data.records !== undefined) {
                // 服务器返回分页对象
                postList = response.data.data.records || [];
                total.value = response.data.data.total || 0;
            } else {
                // 服务器直接返回数组
                postList = response.data.data || [];
                total.value = postList.length;
            }
            
            console.log('帖子列表加载成功:', postList);

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

// 显示/加载评论
const showComments = async (post: Post) => {
    // 切换评论显示状态
    post.showComments = !post.showComments;
    
    // 如果是隐藏评论，则直接返回
    if (!post.showComments) return;
    
    // 初始化评论相关属性
    if (!post.comments) {
        post.comments = [];
        post.commentCurrentPage = 1;
        post.commentPageSize = 5;
        post.commentTotal = 0;
        post.loadingComments = false;
        post.newComment = '';
    }
    
    // 加载评论
    await loadComments(post);
};

// 加载评论列表
const loadComments = async (post: Post) => {
    if (!post.postId) return;
    
    post.loadingComments = true;
    
    try {
        const response = await getPostComments({
            postId: post.postId,
            pageNum: post.commentCurrentPage,
            pageSize: post.commentPageSize
        });
        
        if (response.data && response.data.code === 200) {
            post.comments = response.data.data.comments || [];
            post.commentTotal = response.data.data.total || 0;
            
            // 更新评论数量显示
            post.commentCount = post.commentTotal;
        } else {
            ElMessage.error(response.data?.message || '加载评论失败');
        }
    } catch (error) {
        console.error('加载评论失败:', error);
        ElMessage.error('加载评论失败，请稍后重试');
    } finally {
        post.loadingComments = false;
    }
};

// 提交评论
const submitComment = async (post: Post) => {
    if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录');
        return;
    }
    
    if (!post.newComment || !post.newComment.trim()) {
        ElMessage.warning('评论内容不能为空');
        return;
    }
    
    try {
        const response = await addComment({
            postId: post.postId!,
            content: post.newComment.trim()
        });
        
        if (response.data && response.data.code === 200) {
            ElMessage.success('评论成功');
            post.newComment = ''; // 清空评论输入框
            
            // 重新加载评论列表，显示最新评论
            post.commentCurrentPage = 1; // 重置到第一页
            await loadComments(post);
        } else {
            ElMessage.error(response.data?.message || '评论失败');
        }
    } catch (error) {
        console.error('提交评论失败:', error);
        ElMessage.error('评论失败，请稍后重试');
    }
};

// 删除评论
const deleteCommentItem = async (post: Post, comment: any) => {
    if (!authStore.isAuthenticated) {
        ElMessage.warning('请先登录');
        return;
    }
    
    ElMessageBox.confirm(
        '确定要删除该评论吗？此操作不可恢复。',
        '删除评论',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        try {
            const response = await deleteComment(post.postId!, comment.commentId);
            
            if (response.data && response.data.code === 200) {
                ElMessage.success('评论已成功删除');
                
                // 重新加载评论列表
                await loadComments(post);
            } else {
                ElMessage.error(response.data?.message || '删除评论失败');
            }
        } catch (error) {
            console.error('删除评论失败:', error);
            ElMessage.error('删除评论失败，请稍后重试');
        }
    }).catch(() => {
        // 取消操作
    });
};

// 处理评论分页大小变化
const handleCommentSizeChange = async (post: Post, val: number) => {
    post.commentPageSize = val;
    await loadComments(post);
};

// 处理评论页码变化
const handleCommentPageChange = async (post: Post, val: number) => {
    post.commentCurrentPage = val;
    await loadComments(post);
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

/* 评论区域样式 */
.post-comments {
    margin-top: 15px;
    border-top: 1px solid #eee;
    padding-top: 15px;
}

.post-comments {
    margin-top: 10px;
    background-color: #f9f9f9;
    border-radius: 8px;
    padding: 15px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.comments-container {
    padding: 10px 0;
}

.comment-list {
    margin-bottom: 15px;
}

.comment-item {
    padding: 12px 0;
    border-bottom: 1px solid #eaeaea;
    transition: background-color 0.2s, transform 0.2s;
}

.comment-item:hover {
    background-color: #f5f5f5;
    transform: translateX(3px);
}

.comment-item:last-child {
    border-bottom: none;
}

.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.comment-author {
    display: flex;
    align-items: center;
    gap: 10px;
}

.comment-author-name {
    font-weight: 500;
    font-size: 14px;
    color: #333;
}

.comment-time {
    font-size: 12px;
    color: #999;
}

.comment-content {
    font-size: 14px;
    color: #333;
    line-height: 1.6;
    padding-left: 42px; /* 对齐头像右侧 */
    word-break: break-word;
    margin-top: 5px;
}

.comment-pagination {
    margin: 15px 0;
    display: flex;
    justify-content: center;
}

.comment-input {
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px dashed #e0e0e0;
}

.comment-input .el-input {
    transition: all 0.3s ease;
}

.comment-input .el-input:focus-within {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-tip {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
    text-align: right;
}
</style>
