<template>
  <div class="post-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="post-detail-header">
          <el-button @click="goBack" type="primary" plain>
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>帖子详情</h2>
        </div>
      </template>

      <div v-if="post" class="post-content">
        <!-- 帖子基本信息 -->
        <div class="post-info">
          <h1 class="post-title">{{ post.title }}</h1>
          <div class="post-meta">
            <div class="author-info">
              <el-avatar :size="32" :src="post.authorAvatar" @click="goToUserProfile(post.authorId)">
                {{ post.authorName?.substring(0, 1) }}
              </el-avatar>
              <span class="author-name" @click="goToUserProfile(post.authorId)">{{ post.authorName }}</span>
            </div>
            <div class="post-stats">
              <span>发布时间: {{ formatTime(post.createdAt) }}</span>
              <span>点赞数: {{ post.likeCount || 0 }}</span>
              <span>评论数: {{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 帖子内容 -->
        <div class="post-body">
          <div v-if="post.contentType === 'MARKDOWN'" class="markdown-content" v-html="renderMarkdown(post.content)"></div>
          <div v-else class="text-content" v-html="renderText(post.content)"></div>
        </div>

        <!-- 操作按钮 -->
        <div class="post-actions">
          <el-button :type="post.liked ? 'primary' : 'default'" @click="handleLike" :disabled="!authStore.isAuthenticated">
            <el-icon><Pointer /></el-icon>
            {{ post.liked ? '已点赞' : '点赞' }} ({{ post.likeCount || 0 }})
          </el-button>
          <el-button @click="showComments = !showComments">
            <el-icon><ChatDotRound /></el-icon>
            {{ showComments ? '收起评论' : '查看评论' }} ({{ post.commentCount || 0 }})
          </el-button>
        </div>

        <!-- 评论区域 -->
        <el-collapse-transition>
          <div v-if="showComments" class="comments-section">
            <div class="comment-input">
              <h3>发表评论</h3>
              <RichEditor v-model="newComment" placeholder="请输入评论内容" />
              <el-button type="primary" @click="submitComment" :disabled="!authStore.isAuthenticated || !newComment?.trim()">发表评论</el-button>
            </div>

            <div class="comments-list" v-loading="commentsLoading">
              <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
                <div class="comment-author">
                  <el-avatar
                    :size="24"
                    :src="comment.avatar || comment.authorAvatar || defaultAvatar"
                    style="cursor:pointer"
                    @click="goToUserProfile(comment.userId || comment.authorId)"
                  >
                    {{ (comment.nickname || comment.authorName || comment.username || '').substring(0, 1) }}
                  </el-avatar>
                  <el-link
                    class="comment-author-name"
                    type="primary"
                    :underline="true"
                    style="font-weight:500"
                    @click="goToUserProfile(comment.userId || comment.authorId)"
                  >
                    {{ comment.nickname || comment.authorName || comment.username || '匿名' }}
                  </el-link>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <div class="comment-content markdown-content" v-html="renderCommentContent(comment.content)"></div>
              </div>
              <el-empty v-if="comments.length === 0 && !commentsLoading" description="暂无评论" />
            </div>

            <!-- 评论分页 -->
            <div class="comment-pagination" v-if="commentTotal > commentPageSize">
              <el-pagination 
                v-model:current-page="commentCurrentPage" 
                v-model:page-size="commentPageSize"
                :page-sizes="[5, 10, 20]" 
                layout="sizes, prev, pager, next" 
                :total="commentTotal"
                @size-change="handleCommentSizeChange" 
                @current-change="handleCommentPageChange" 
                small />
            </div>
          </div>
        </el-collapse-transition>
      </div>

      <el-empty v-else-if="!loading" description="帖子不存在或已被删除">
        <el-button type="primary" @click="goBack">返回</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Pointer, ChatDotRound } from '@element-plus/icons-vue'
import { getPostDetail, likePost, unlikePost, getLikeStatus } from '../../api/groupPost'
import { getPostComments, addComment } from '../../api/postComment'
import { useAuthStore } from '../../store/auth'
import RichEditor from '../../components/form/RichEditor.vue'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 路由参数
const groupId = computed(() => route.params.groupId)
const postId = computed(() => {
  const id = route.params.postId
  return Array.isArray(id) ? id[0] : id
})

// 数据状态
const loading = ref(false)
const commentsLoading = ref(false)
const post = ref<any>(null)
const comments = ref<any[]>([])
const showComments = ref(false)
const newComment = ref('')

// 分页数据
const commentCurrentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 加载帖子详情
const loadPostDetail = async () => {
  if (!postId.value) return
  
  loading.value = true
  try {
    const response = await getPostDetail(postId.value)
    if (response.data && response.data.code === 200) {
      post.value = response.data.data
      
      // 检查点赞状态
      if (authStore.isAuthenticated) {
        const likeResponse = await getLikeStatus(postId.value)
        if (likeResponse.data && likeResponse.data.code === 200) {
          post.value.liked = likeResponse.data.data
        }
      }
    } else {
      ElMessage.error(response.data?.message || '加载帖子失败')
    }
  } catch (error) {
    console.error('加载帖子详情失败:', error)
    ElMessage.error('加载帖子失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  if (!postId.value) return
  
  commentsLoading.value = true
  try {
    const response = await getPostComments({
      postId: postId.value,
      pageNum: commentCurrentPage.value,
      pageSize: commentPageSize.value
    })
    
    if (response.data && response.data.code === 200) {
      const data = response.data.data
      if (data.records !== undefined) {
        comments.value = data.records || []
        commentTotal.value = data.total || 0
      } else if (data.comments !== undefined) {
        comments.value = data.comments || []
        commentTotal.value = data.total || 0
      } else {
        comments.value = data || []
        commentTotal.value = comments.value.length
      }
    } else {
      ElMessage.error(response.data?.message || '加载评论失败')
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败，请稍后重试')
  } finally {
    commentsLoading.value = false
  }
}

// 处理点赞
const handleLike = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    let response
    if (post.value.liked) {
      response = await unlikePost(postId.value)
      if (response.data && response.data.code === 200) {
        post.value.liked = false
        post.value.likeCount = Math.max(0, (post.value.likeCount || 1) - 1)
      }
    } else {
      response = await likePost(postId.value)
      if (response.data && response.data.code === 200) {
        post.value.liked = true
        post.value.likeCount = (post.value.likeCount || 0) + 1
      }
    }

    if (response.data && response.data.code !== 200) {
      ElMessage.error(response.data?.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 提交评论
const submitComment = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  if (!newComment.value?.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const response = await addComment({
      postId: postId.value,
      content: newComment.value.trim()
    })

    if (response.data && response.data.code === 200) {
      ElMessage.success('评论发表成功')
      newComment.value = ''
      
      // 重新加载评论列表
      commentCurrentPage.value = 1
      await loadComments()
      
      // 更新帖子评论数
      if (post.value) {
        post.value.commentCount = (post.value.commentCount || 0) + 1
      }
    } else {
      ElMessage.error(response.data?.message || '评论失败')
    }
  } catch (error) {
    console.error('提交评论失败:', error)
    ElMessage.error('评论失败，请稍后重试')
  }
}

// 分页处理
const handleCommentSizeChange = (val: number) => {
  commentPageSize.value = val
  loadComments()
}

const handleCommentPageChange = (val: number) => {
  commentCurrentPage.value = val
  loadComments()
}

// 工具函数
const goBack = () => {
  if (groupId.value) {
    router.push(`/groups/${groupId.value}/detail?tab=posts`)
  } else {
    router.back()
  }
}

const goToUserProfile = (userId: number | string) => {
  router.push(`/user/${userId}`)
}

const formatTime = (time: string | undefined) => {
  if (!time) return ''
  
  try {
    const date = new Date(time)
    return date.toLocaleString('zh-CN', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    console.error('时间格式化错误:', e)
    return time
  }
}

const renderMarkdown = (content: string) => {
  if (!content) return ''
  try {
    let html = marked(content)
    
    // 为图片添加样式类
    html = html.replace(/<img\s+([^>]*?)>/gi, '<img class="markdown-image" $1>')
    
    // 为表格添加样式类
    html = html.replace(/<table>/gi, '<table class="markdown-table">')
    
    // 为代码块添加样式类
    html = html.replace(/<pre>/gi, '<pre class="markdown-code">')
    
    return html
  } catch (error) {
    console.error('Markdown渲染错误:', error)
    return content
  }
}

const renderText = (content: string) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

const renderCommentContent = (content: string) => {
  if (!content) return ''
  try {
    let html = marked(content)
    
    // 为图片添加样式类
    html = html.replace(/<img\s+([^>]*?)>/gi, '<img class="markdown-image" $1>')
    
    // 为表格添加样式类
    html = html.replace(/<table>/gi, '<table class="markdown-table">')
    
    // 为代码块添加样式类
    html = html.replace(/<pre>/gi, '<pre class="markdown-code">')
    
    return html
  } catch (error) {
    console.error('评论Markdown渲染错误:', error)
    return content.replace(/\n/g, '<br>')
  }
}

// 生命周期
onMounted(() => {
  loadPostDetail();
  loadComments();
  showComments.value = true; // 页面加载时自动展开评论区
})

// 监听评论显示状态变化
watch(() => showComments.value, (newValue) => {
  if (newValue && comments.value.length === 0) {
    loadComments()
  }
})
</script>

<style scoped>
.post-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.post-detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.post-content {
  padding: 20px 0;
}

.post-info {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.post-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
  text-decoration: underline dotted #409eff 1.5px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  transition: color 0.2s, text-decoration 0.2s;
}
.post-title:hover {
  color: #409eff;
  text-decoration: underline solid #409eff 2px;
}
.post-title .el-icon {
  margin-left: 6px;
  font-size: 20px;
  color: #409eff;
  opacity: 0.7;
}

[data-theme="dark"] .post-title {
  color: #fff;
  text-decoration-color: #409eff;
}
[data-theme="dark"] .post-title:hover {
  color: #409eff;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-weight: 500;
  cursor: pointer;
  color: var(--el-color-primary);
}

.author-name:hover {
  text-decoration: underline;
}

.post-stats {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.post-body {
  margin: 24px 0;
  line-height: 1.6;
  font-size: 16px;
}

.post-actions {
  margin: 24px 0;
  padding: 16px 0;
  border-top: 1px solid var(--el-border-color-light);
  border-bottom: 1px solid var(--el-border-color-light);
}

.comments-section {
  margin-top: 24px;
}

.comment-input {
  margin-bottom: 24px;
  padding: 16px;
  background-color: var(--el-bg-color-page);
  border-radius: 8px;
}

.comment-input h3 {
  margin-bottom: 12px;
  font-size: 16px;
}

.comments-list {
  margin: 16px 0;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-author-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.comment-content {
  margin-left: 32px;
  line-height: 1.5;
}

.comment-content.markdown-content {
  background: #f7faff;
  border-radius: 4px;
  padding: 8px 12px 8px 42px;
  margin-top: 5px;
}
[data-theme="dark"] .comment-content.markdown-content {
  background: #232326;
  color: #fff;
}

.comment-pagination {
  margin-top: 16px;
  text-align: center;
}

/* Markdown内容样式 */
:deep(.markdown-content) {
  line-height: 1.6;
  word-break: break-word;
  text-align: left;
}

:deep(.markdown-content h1) {
  font-size: 2em;
  margin-top: 0.67em;
  margin-bottom: 0.67em;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
  color: var(--el-text-color-primary);
}

:deep(.markdown-content h2) {
  font-size: 1.5em;
  margin-top: 0.83em;
  margin-bottom: 0.83em;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
  color: var(--el-text-color-primary);
}

:deep(.markdown-content h3) {
  font-size: 1.17em;
  margin-top: 1em;
  margin-bottom: 1em;
  color: var(--el-text-color-primary);
}

:deep(.markdown-content h4) {
  font-size: 1em;
  margin-top: 1.33em;
  margin-bottom: 1.33em;
  color: var(--el-text-color-primary);
}

:deep(.markdown-content h5) {
  font-size: 0.83em;
  margin-top: 1.67em;
  margin-bottom: 1.67em;
  color: var(--el-text-color-primary);
}

:deep(.markdown-content h6) {
  font-size: 0.67em;
  margin-top: 2.33em;
  margin-bottom: 2.33em;
  color: var(--el-text-color-primary);
}

:deep(.markdown-content p) {
  margin-top: 1em;
  margin-bottom: 1em;
  color: var(--el-text-color-regular);
}

:deep(.markdown-content blockquote) {
  margin: 1em 0;
  padding: 0.5em 1em;
  border-left: 4px solid #409EFF;
  background-color: #f6f8fa;
  color: #666;
}

:deep(.markdown-content pre) {
  background-color: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 0.9em;
  line-height: 1.45;
}

:deep(.markdown-content pre code) {
  padding: 0;
  background-color: transparent;
}

:deep(.markdown-content code:not(pre code)) {
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
  padding: 0.2em 0.4em;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 0.9em;
}

:deep(.markdown-content ul), 
:deep(.markdown-content ol) {
  padding-left: 2em;
  margin-top: 1em;
  margin-bottom: 1em;
}

:deep(.markdown-content li) {
  margin: 0.25em 0;
}

:deep(.markdown-content table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

:deep(.markdown-content table th),
:deep(.markdown-content table td) {
  padding: 6px 13px;
  border: 1px solid #dfe2e5;
}

:deep(.markdown-content table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

:deep(.markdown-content table tr:nth-child(2n)) {
  background-color: #f6f8fa;
}

:deep(.markdown-content img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 0.5em 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s ease;
}

:deep(.markdown-content img:hover) {
  transform: scale(1.02);
}

:deep(.markdown-content hr) {
  border: none;
  border-top: 1px solid #eaecef;
  margin: 2em 0;
}

:deep(.markdown-content a) {
  color: var(--el-color-primary);
  text-decoration: none;
}

:deep(.markdown-content a:hover) {
  text-decoration: underline;
}

.text-content {
  line-height: 1.6;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 暗色模式下的 Markdown 内容样式 */
[data-theme="dark"] :deep(.markdown-content h1),
[data-theme="dark"] :deep(.markdown-content h2) {
  border-bottom-color: var(--el-border-color);
  color: var(--el-text-color-primary);
}

[data-theme="dark"] :deep(.markdown-content h3),
[data-theme="dark"] :deep(.markdown-content h4),
[data-theme="dark"] :deep(.markdown-content h5),
[data-theme="dark"] :deep(.markdown-content h6) {
  color: var(--el-text-color-primary);
}

[data-theme="dark"] :deep(.markdown-content p) {
  color: var(--el-text-color-regular);
}

[data-theme="dark"] :deep(.markdown-content blockquote) {
  color: var(--el-text-color-secondary);
  border-left-color: var(--el-color-primary);
  background-color: var(--el-fill-color-lighter);
}

[data-theme="dark"] :deep(.markdown-content pre) {
  background-color: var(--el-fill-color-lighter);
  border-color: var(--el-border-color);
  color: var(--el-text-color-primary);
}

[data-theme="dark"] :deep(.markdown-content code:not(pre code)) {
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
}

[data-theme="dark"] :deep(.markdown-content table th),
[data-theme="dark"] :deep(.markdown-content table td) {
  border-color: var(--el-border-color);
}

[data-theme="dark"] :deep(.markdown-content table th) {
  background-color: var(--el-fill-color-lighter);
}

[data-theme="dark"] :deep(.markdown-content table tr:nth-child(2n)) {
  background-color: var(--el-fill-color-light);
}

[data-theme="dark"] :deep(.markdown-content hr) {
  border-top-color: var(--el-border-color);
}

[data-theme="dark"] .text-content {
  color: var(--el-text-color-regular);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-detail-page {
    padding: 10px;
  }
  
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .post-stats {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
