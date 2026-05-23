<template>
  <div v-if="replies && replies.length > 0" class="replies-list">
    <div v-for="reply in replies" :key="reply.commentId" class="reply-item">
      <div class="comment-author author-container comment-header">
        <el-avatar
          :size="avatarSize"
          :src="reply.avatar || defaultAvatar"
          style="cursor:pointer"
          @click="$emit('goToUserProfile', reply.userId)"
          class="comment-avatar magical-avatar"
        >
          {{ (reply.nickname || reply.username || '').substring(0, 1) }}
        </el-avatar>
        <el-link
          class="comment-author-name gradient-link"
          type="primary"
          :underline="true"
          style="font-weight:500;font-size:13px"
          @click="$emit('goToUserProfile', reply.userId)"
        >
          {{ reply.nickname || reply.username || '匿名' }}
        </el-link>
        <span class="comment-time time-badge">{{ formatTime(reply.createdAt) }}</span>
        <div class="comment-actions">
          <el-button
            v-if="isAuthenticated && groupActive"
            type="text"
            size="small"
            class="action-btn reply-btn"
            @click="$emit('reply', reply)">
            <el-icon><ChatDotRound /></el-icon>
            回复
          </el-button>
          <el-button
            v-if="authUserId === reply.userId"
            type="text"
            size="small"
            @click="$emit('delete', reply)"
            class="action-btn delete-btn">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>

      <div
        class="comment-content markdown-content reply-content"
        style="font-size:13px"
        v-html="renderCommentContent(reply.content)"
      ></div>

      <div v-if="replyTarget?.commentId === reply.commentId" class="reply-input-area" style="margin-left:0">
        <div class="quote-preview" v-if="replyTarget?.quotedContent">
          <span class="quote-author">@{{ replyTarget.replyToNickname }}</span>：<span class="quote-text">{{ getQuotePreview(replyTarget.quotedContent) }}</span>
        </div>
        <div class="editor-wrapper">
          <RichEditor
            :model-value="replyContent"
            placeholder="输入回复内容...（支持Markdown）"
            style="width:100%;margin-bottom:8px;"
            @update:model-value="$emit('update:replyContent', $event)"
          />
        </div>
        <div class="reply-input-actions">
          <el-button size="small" @click="$emit('cancel')">取消</el-button>
          <el-button
            type="primary"
            size="small"
            :loading="replyLoading"
            :disabled="!replyContent.trim()"
            @click="$emit('submit')">
            发送回复
          </el-button>
        </div>
      </div>

      <CommentReplies
        v-if="reply.replies && reply.replies.length > 0"
        :replies="reply.replies"
        :reply-target="replyTarget"
        :reply-content="replyContent"
        :reply-loading="replyLoading"
        :auth-user-id="authUserId"
        :is-authenticated="isAuthenticated"
        :group-active="groupActive"
        :default-avatar="defaultAvatar"
        :format-time="formatTime"
        :render-comment-content="renderCommentContent"
        :get-quote-preview="getQuotePreview"
        :avatar-size="avatarSize"
        @reply="$emit('reply', $event)"
        @delete="$emit('delete', $event)"
        @cancel="$emit('cancel')"
        @submit="$emit('submit')"
        @go-to-user-profile="$emit('goToUserProfile', $event)"
        @update:reply-content="$emit('update:replyContent', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, Delete } from '@element-plus/icons-vue'
import RichEditor from '../form/RichEditor.vue'

defineProps<{
  replies: any[]
  replyTarget: any | null
  replyContent: string
  replyLoading: boolean
  authUserId?: number
  isAuthenticated: boolean
  groupActive: boolean
  defaultAvatar: string
  formatTime: (time: any) => string
  renderCommentContent: (content: any) => string
  getQuotePreview: (content: string) => string
  avatarSize?: number
}>()

defineEmits<{
  reply: [reply: any]
  delete: [reply: any]
  cancel: []
  submit: []
  goToUserProfile: [userId: number]
  'update:replyContent': [value: string]
}>()
</script>
