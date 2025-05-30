<template>
  <div class="post-detail-page animated-page">
    <el-card v-loading="loading" class="main-card">
      <template #header>
        <div class="post-detail-header animated-header">
          <el-button @click="goBack" type="primary" plain class="back-btn magical-btn">
            <el-icon class="floating-icon"><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2 class="gradient-title">帖子详情</h2>
        </div>
      </template>

      <div v-if="post" class="post-content animated-content">
        <!-- 帖子基本信息 -->
        <div class="post-info floating-section">
          <h1 class="post-title magical-title">{{ post.title }}</h1>
          <div class="post-meta">
            <div class="author-info animated-author">
              <el-avatar :size="32" :src="post.authorAvatar" @click="goToUserProfile(post.authorId)" class="magical-avatar">
                {{ post.authorName?.substring(0, 1) }}
              </el-avatar>
              <span class="author-name" @click="goToUserProfile(post.authorId)">{{ post.authorName }}</span>
            </div>
            <div class="post-stats stats-container">
              <span class="stat-item">发布时间: {{ formatTime(post.createdAt) }}</span>
              <span class="stat-item">点赞数: {{ post.likeCount || 0 }}</span>
              <span class="stat-item">评论数: {{ post.commentCount || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 小组禁用警告 -->
        <DisabledGroupWarning 
          :is-disabled="groupStatus !== 'ACTIVE'" 
          :group-status="groupStatus" 
        />

        <!-- 帖子内容 -->
        <div class="post-body content-section">
          <div v-if="post.contentType === 'MARKDOWN'" class="markdown-content" v-html="renderMarkdown(post.content)"></div>
          <div v-else class="text-content" v-html="renderText(post.content)"></div>
        </div>

        <!-- 操作按钮 -->
        <div class="post-actions actions-container">
          <el-button :type="post.liked ? 'primary' : 'default'" @click="handleLike" :disabled="!authStore.isAuthenticated" class="action-btn like-btn">
            <el-icon class="pulsing-icon"><Pointer /></el-icon>
            {{ post.liked ? '已点赞' : '点赞' }} ({{ post.likeCount || 0 }})
          </el-button>
          <el-button @click="showComments = !showComments" class="action-btn comment-btn">
            <el-icon class="bouncing-icon"><ChatDotRound /></el-icon>
            {{ showComments ? '收起评论' : '查看评论' }} ({{ post.commentCount || 0 }})
          </el-button>
        </div>

        <!-- 评论区域 -->
        <el-collapse-transition>
          <div v-if="showComments" class="comments-section animated-comments">
            <div class="comment-input input-section">
              <h3 class="section-title">发表评论</h3>
              <RichEditor v-model="newComment" placeholder="请输入评论内容" class="editor-container editor-wrapper" />
              <el-button type="primary" @click="submitComment" :disabled="isCommentDisabled" class="submit-btn magical-btn">
                {{ groupStatus === 'INACTIVE' ? '小组已禁用，无法评论' : '发表评论' }}
              </el-button>
            </div>

            <div class="comments-list comments-container" v-loading="commentsLoading">
              <div v-for="(comment, index) in comments" :key="comment.commentId" 
                   class="comment-item floating-comment" 
                   :style="{ '--animation-delay': `${index * 0.1}s` }">
                <div class="comment-author author-container">
                  <el-avatar
                    :size="24"
                    :src="comment.avatar || comment.authorAvatar || defaultAvatar"
                    style="cursor:pointer"
                    @click="goToUserProfile(comment.userId || comment.authorId)"
                    class="comment-avatar magical-avatar"
                  >
                    {{ (comment.nickname || comment.authorName || comment.username || '').substring(0, 1) }}
                  </el-avatar>
                  <el-link
                    class="comment-author-name gradient-link"
                    type="primary"
                    :underline="true"
                    style="font-weight:500"
                    @click="goToUserProfile(comment.userId || comment.authorId)"
                  >
                    {{ comment.nickname || comment.authorName || comment.username || '匿名' }}
                  </el-link>
                  <span class="comment-time time-badge">{{ formatTime(comment.createdAt) }}</span>
                  
                  <!-- 评论操作按钮 -->
                  <div class="comment-actions" v-if="authStore.user?.userId === (comment.userId || comment.authorId)">
                    <el-button 
                      type="text" 
                      size="small"
                      @click="editCommentItem(comment)"
                      class="action-btn edit-btn">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                    <el-button 
                      type="text" 
                      size="small"
                      @click="deleteCommentItem(comment)"
                      class="action-btn delete-btn">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
                
                <!-- 编辑状态的评论表单 -->
                <div v-if="comment.isEditing" class="comment-edit-form">
                  <div class="editor-wrapper">
                    <RichEditor v-model="comment.editContent" placeholder="请输入评论内容" style="width:100%;margin-bottom:8px;" />
                  </div>
                  <div class="comment-edit-actions">
                    <el-button size="small" @click="cancelEditComment(comment)">取消</el-button>
                    <el-button type="primary" size="small" @click="updateCommentItem(comment)" :disabled="!comment.editContent || !comment.editContent.trim()">保存</el-button>
                  </div>
                </div>
                
                <!-- 正常显示的评论内容 -->
                <div v-else class="comment-content markdown-content animated-content" v-html="renderCommentContent(comment.content)"></div>
              </div>
              <el-empty v-if="comments.length === 0 && !commentsLoading" description="暂无评论" class="empty-state" />
            </div>

            <!-- 评论分页 -->
            <div class="comment-pagination pagination-container" v-if="commentTotal > commentPageSize">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Pointer, ChatDotRound, Edit, Delete } from '@element-plus/icons-vue'
import { getPostDetail, likePost, unlikePost, getLikeStatus } from '../../api/groupPost'
import { getPostComments, addComment, deleteComment, updateComment } from '../../api/postComment'
import { useAuthStore } from '../../store/auth'
import RichEditor from '../../components/form/RichEditor.vue'
import DisabledGroupWarning from '../../components/group/DisabledGroupWarning.vue'
import { marked } from 'marked'
import '../../styles/comment-editor.css'

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
const groupStatus = ref('ACTIVE') // 新增: 存储小组状态

// 分页数据
const commentCurrentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 计算属性：根据小组状态判断是否禁用评论
const isCommentDisabled = computed(() => {
  return !authStore.isAuthenticated || !newComment.value?.trim() || groupStatus.value !== 'ACTIVE'
})

// 加载帖子详情
const loadPostDetail = async () => {
  if (!postId.value) return
  
  loading.value = true
  try {
    const response = await getPostDetail(postId.value)
    if (response.data && response.data.code === 200) {
      post.value = response.data.data
      
      // 保存小组状态 (如果API返回了该字段)
      if (post.value.group && post.value.group.status) {
        groupStatus.value = post.value.group.status
      }
      
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

  if (groupStatus.value === 'INACTIVE') {
    ElMessage.warning('小组已被禁用，无法发表评论')
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
      
      // 重新加载帖子详情以获取最新的评论数
      await loadPostDetail()
    } else {
      ElMessage.error(response.data?.message || '评论失败')
    }
  } catch (error) {
    console.error('提交评论失败:', error)
    ElMessage.error('评论失败，请稍后重试')
  }
}

// 编辑评论
const editCommentItem = (comment: any) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 检查小组状态
  if (groupStatus.value !== 'ACTIVE') {
    ElMessage.warning('该小组已被禁用，无法编辑评论')
    return
  }
  
  // 设置编辑状态和内容
  comment.isEditing = true
  comment.editContent = comment.content
}

// 取消编辑评论
const cancelEditComment = (comment: any) => {
  comment.isEditing = false
  comment.editContent = comment.content
}

// 更新评论
const updateCommentItem = async (comment: any) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 检查小组状态
  if (groupStatus.value !== 'ACTIVE') {
    ElMessage.warning('该小组已被禁用，无法更新评论')
    return
  }
  
  if (!comment.editContent || !comment.editContent.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  
  try {
    const response = await updateComment(postId.value, comment.commentId, comment.editContent.trim())
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('评论更新成功')
      
      // 更新本地评论内容
      comment.content = comment.editContent
      comment.isEditing = false
      
      // 重新加载评论列表以确保数据一致性
      await loadComments()
    } else {
      ElMessage.error(response.data?.message || '更新评论失败')
    }
  } catch (error) {
    console.error('更新评论失败:', error)
    ElMessage.error('更新评论失败，请稍后重试')
  }
}

// 删除评论
const deleteCommentItem = async (comment: any) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确定要删除该评论吗？此操作不可恢复。',
      '删除评论',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await deleteComment(postId.value, comment.commentId)
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('评论已成功删除')
      
      // 重新加载评论列表
      await loadComments()
      
      // 重新加载帖子详情以获取最新的评论数
      await loadPostDetail()
    } else {
      ElMessage.error(response.data?.message || '删除评论失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除评论失败，请稍后重试')
    }
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
    // 配置marked选项
    marked.setOptions({
      breaks: true,
      gfm: true,
      silent: true
    })
    
    let html = marked(content)
    
    // 为图片添加样式类
    html = html.replace(/<img\s+([^>]*?)>/gi, '<img class="markdown-image" $1>')
    
    // 为表格添加样式类
    html = html.replace(/<table>/gi, '<table class="markdown-table">')
    
    // 为代码块添加样式类
    html = html.replace(/<pre><code>/gi, '<pre class="markdown-code"><code>')
    html = html.replace(/<pre><code class="language-(\w+)">/gi, '<pre class="markdown-code language-$1"><code>')
    
    // 增强代码语法高亮
    html = html.replace(/<code>([\s\S]*?)<\/code>/g, (_, p1) => {
      // 关键字高亮
      let highlighted = p1.replace(/\b(const|let|var|function|return|if|else|for|while|class|import|export|from|async|await)\b/g, 
                     '<span class="token keyword">$1</span>');
      
      // 字符串高亮
      highlighted = highlighted.replace(/(['"`])(.*?)\1/g, '<span class="token string">$1$2$1</span>');
      
      // 注释高亮 (简单情况)
      highlighted = highlighted.replace(/\/\/(.*?)$/gm, '<span class="token comment">//$1</span>');
      
      // 数字高亮
      highlighted = highlighted.replace(/\b(\d+)\b/g, '<span class="token number">$1</span>');
      
      return '<code>' + highlighted + '</code>';
    });
    
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

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes floatUp {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-8px);
    }
}

@keyframes pulseGlow {
    0%, 100% {
        box-shadow: 0 2px 10px rgba(64, 158, 255, 0.2);
    }
    50% {
        box-shadow: 0 4px 20px rgba(64, 158, 255, 0.4);
    }
}

@keyframes magicalSparkle {
    0%, 100% {
        opacity: 0;
        transform: scale(0.8) rotate(0deg);
    }
    50% {
        opacity: 1;
        transform: scale(1.2) rotate(180deg);
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

@keyframes bounce {
    0%, 20%, 50%, 80%, 100% {
        transform: translateY(0);
    }
    40% {
        transform: translateY(-8px);
    }
    60% {
        transform: translateY(-4px);
    }
}

@keyframes pulse {
    0% {
        transform: scale(1);
    }
    50% {
        transform: scale(1.05);
    }
    100% {
        transform: scale(1);
    }
}

@keyframes gradient {
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

/* 页面整体动画 */
.animated-page {
    animation: slideInFromTop 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.main-card {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.main-card:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
}

/* 头部动画 */
.animated-header {
    animation: fadeInUp 1s cubic-bezier(0.25, 0.8, 0.25, 1) 0.2s both;
}

.back-btn.magical-btn {
    position: relative;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-btn.magical-btn::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: linear-gradient(45deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.3));
    border-radius: 50%;
    transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    transform: translate(-50%, -50%);
    z-index: 0;
}

.back-btn.magical-btn:hover::before {
    width: 200%;
    height: 200%;
}

.back-btn.magical-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.25);
}

.floating-icon {
    animation: floatUp 2s ease-in-out infinite;
}

.gradient-title {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    font-weight: 600;
}

/* 内容区域动画 */
.animated-content {
    animation: fadeInUp 1.2s cubic-bezier(0.25, 0.8, 0.25, 1) 0.4s both;
}

.floating-section {
    animation: fadeInUp 1s cubic-bezier(0.25, 0.8, 0.25, 1) 0.6s both;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.floating-section:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.magical-title {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    background-size: 200% 200%;
    animation: gradient 4s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.magical-title:hover {
    transform: scale(1.02);
    text-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

/* 作者信息动画 */
.animated-author {
    animation: fadeInUp 1.2s cubic-bezier(0.25, 0.8, 0.25, 1) 0.8s both;
}

.magical-avatar {
    position: relative;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.magical-avatar::before {
    content: '';
    position: absolute;
    top: -3px;
    left: -3px;
    right: -3px;
    bottom: -3px;
    background: linear-gradient(45deg, #667eea, #764ba2, #667eea);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
    border-radius: 50%;
    z-index: -1;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.magical-avatar:hover::before {
    opacity: 1;
}

.magical-avatar:hover {
    transform: scale(1.1);
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 统计信息动画 */
.stats-container {
    animation: fadeInUp 1.4s cubic-bezier(0.25, 0.8, 0.25, 1) 1s both;
}

.stat-item {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    padding: 4px 8px;
    border-radius: 4px;
}

.stat-item:hover {
    background: rgba(64, 158, 255, 0.1);
    transform: scale(1.05);
}

/* 内容区域动画 */
.content-section {
    animation: fadeInUp 1.6s cubic-bezier(0.25, 0.8, 0.25, 1) 1.2s both;
}

/* 操作按钮动画 */
.actions-container {
    animation: fadeInUp 1.8s cubic-bezier(0.25, 0.8, 0.25, 1) 1.4s both;
}

.action-btn {
    position: relative;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
}

.action-btn::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
    border-radius: 50%;
    transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    transform: translate(-50%, -50%);
    z-index: 0;
}

.action-btn:hover::before {
    width: 200%;
    height: 200%;
}

.action-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.25);
}

.like-btn:hover {
    animation: pulse 0.6s ease-in-out;
}

.comment-btn:hover {
    animation: bounce 0.8s ease-in-out;
}

.pulsing-icon {
    animation: pulse 2s ease-in-out infinite;
}

.bouncing-icon {
    animation: bounce 2s ease-in-out infinite;
}

/* 评论区域动画 */
.animated-comments {
    animation: fadeInUp 2s cubic-bezier(0.25, 0.8, 0.25, 1) 1.6s both;
}

.input-section {
    animation: fadeInUp 0.8s cubic-bezier(0.25, 0.8, 0.25, 1) 0.2s both;
}

.section-title {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.editor-container {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    width: 100%;
    text-align: left !important;
}

.editor-container:hover {
    transform: scale(1.01);
    box-shadow: 0 4px 15px rgba(64, 158, 255, 0.1);
}

.submit-btn.magical-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    position: relative;
    overflow: hidden;
}

.submit-btn.magical-btn::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    background: linear-gradient(45deg, #667eea, #764ba2, #667eea);
    background-size: 200% 200%;
    animation: shimmer 3s linear infinite;
    border-radius: inherit;
    z-index: -1;
}

.submit-btn.magical-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.disabled-tip {
    margin-top: 10px;
    animation: fadeInUp 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 评论项动画 */
.comments-container {
    animation: fadeInUp 1s cubic-bezier(0.25, 0.8, 0.25, 1) 0.4s both;
}

.floating-comment {
    animation: fadeInUp 0.8s cubic-bezier(0.25, 0.8, 0.25, 1) var(--animation-delay, 0s) both;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.floating-comment:hover {
    transform: translateX(10px);
    background: rgba(64, 158, 255, 0.05);
    border-radius: 8px;
    padding: 8px;
    margin: 4px 0;
}

.author-container {
    animation: fadeInUp 0.6s cubic-bezier(0.25, 0.8, 0.25, 1) 0.2s both;
}

.comment-avatar {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.comment-avatar:hover {
    transform: scale(1.2);
    box-shadow: 0 3px 10px rgba(64, 158, 255, 0.3);
}

.gradient-link {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.gradient-link:hover {
    transform: scale(1.05);
}

.time-badge {
    background: rgba(64, 158, 255, 0.1);
    padding: 2px 6px;
    border-radius: 12px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.time-badge:hover {
    background: rgba(64, 158, 255, 0.2);
    transform: scale(1.1);
}

/* 分页动画 */
.pagination-container {
    animation: fadeInUp 1.2s cubic-bezier(0.25, 0.8, 0.25, 1) 0.6s both;
}

.pagination-container :deep(.el-pagination__button) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.pagination-container :deep(.el-pagination__button):hover {
    transform: scale(1.1);
}

/* 空状态动画 */
.empty-state {
    animation: fadeInUp 1s cubic-bezier(0.68, -0.55, 0.265, 1.55) 0.8s both;
}

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
  position: relative;
}

.comment-author-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-regular);
}

/* 评论操作按钮样式 */
.comment-actions {
  margin-left: auto;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.comment-item:hover .comment-actions {
  opacity: 1;
}

.comment-actions .action-btn {
  padding: 4px 6px;
  border: none;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.comment-actions .edit-btn {
  color: #409eff;
}

.comment-actions .edit-btn:hover {
  background-color: rgba(64, 158, 255, 0.1);
  transform: scale(1.1);
}

.comment-actions .delete-btn {
  color: #f56c6c;
}

.comment-actions .delete-btn:hover {
  background-color: rgba(245, 108, 108, 0.1);
  transform: scale(1.1);
}

/* 评论编辑表单样式 */
.comment-edit-form {
  margin-top: 8px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

[data-theme="dark"] .comment-edit-form {
  background-color: #2a2a2a;
  border-color: #444;
}

.comment-edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
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
  border-bottom-color: var(--dark-border-color, #333);
  color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] :deep(.markdown-content h3),
[data-theme="dark"] :deep(.markdown-content h4),
[data-theme="dark"] :deep(.markdown-content h5),
[data-theme="dark"] :deep(.markdown-content h6) {
  color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] :deep(.markdown-content p) {
  color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] :deep(.markdown-content blockquote) {
  color: var(--dark-text-secondary, #a3a6ad);
  border-left-color: var(--primary-color-dark, #60a9ff);
  background-color: var(--dark-bg-secondary, #2a2a2e);
}

[data-theme="dark"] :deep(.markdown-content pre) {
  background-color: var(--dark-code-bg, #1a1a1c);
  border-color: var(--dark-border-color, #333);
  color: var(--dark-code-text, #e6e6e6);
}

[data-theme="dark"] :deep(.markdown-content code:not(pre code)) {
  background-color: var(--dark-code-bg, #1a1a1c);
  color: var(--dark-code-text, #e6e6e6);
  border: 1px solid var(--dark-border-color, #333);
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

/* 暗色模式适配 */
[data-theme="dark"] .animated-page {
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

[data-theme="dark"] .main-card {
    background: var(--dark-card-bg);
    border: 1px solid var(--dark-border-color);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .main-card:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
}

[data-theme="dark"] .gradient-title {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

[data-theme="dark"] .magical-title {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    background-size: 200% 200%;
    animation: gradient 4s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

[data-theme="dark"] .magical-avatar::before {
    background: linear-gradient(45deg, #4facfe, #00f2fe, #4facfe);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
}

[data-theme="dark"] .stat-item:hover {
    background: rgba(79, 172, 254, 0.2);
}

[data-theme="dark"] .section-title {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

[data-theme="dark"] .gradient-link {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    background-size: 200% 200%;
    animation: gradient 3s ease infinite;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

[data-theme="dark"] .time-badge {
    background: rgba(79, 172, 254, 0.2);
}

[data-theme="dark"] .time-badge:hover {
    background: rgba(79, 172, 254, 0.3);
}

[data-theme="dark"] .floating-comment:hover {
    background: rgba(79, 172, 254, 0.1);
}

[data-theme="dark"] .editor-container:hover {
    box-shadow: 0 4px 15px rgba(79, 172, 254, 0.2);
}

[data-theme="dark"] .submit-btn.magical-btn {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

[data-theme="dark"] .submit-btn.magical-btn::before {
    background: linear-gradient(45deg, #4facfe, #00f2fe, #4facfe);
    background-size: 200% 200%;
    animation: shimmer 3s linear infinite;
}

[data-theme="dark"] .submit-btn.magical-btn:hover {
    box-shadow: 0 8px 25px rgba(79, 172, 254, 0.4);
}

[data-theme="dark"] .action-btn:hover {
    box-shadow: 0 6px 20px rgba(79, 172, 254, 0.3);
}

[data-theme="dark"] .back-btn.magical-btn:hover {
    box-shadow: 0 6px 20px rgba(79, 172, 254, 0.3);
}

[data-theme="dark"] .comment-avatar:hover {
    box-shadow: 0 3px 10px rgba(79, 172, 254, 0.4);
}
</style>
