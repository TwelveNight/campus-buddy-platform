<template>
  <div class="message-container">
    <!-- 左侧会话列表 -->
    <div class="session-list">
      <div class="session-header">
        <h3>消息会话</h3>
      </div>
      <el-divider />
      <div class="session-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人"
          prefix-icon="Search"
          clearable
        />
      </div>
      <div class="session-items">
        <el-empty v-if="sessions.length === 0 && !sessionsLoading" description="暂无消息会话" />
        <el-skeleton :rows="3" animated v-if="sessionsLoading" :count="3" />
        <template v-else>
          <div
            v-for="session in filteredSessions"
            :key="session.userId"
            class="session-item"
            :class="{ 'session-active': currentChatUser?.userId === session.userId }"
            @click="selectChat(session)"
          >
            <el-badge :value="session.unreadCount" :hidden="!session.unreadCount" class="session-badge">
              <el-avatar :size="40" :src="session.avatarUrl || defaultAvatar" />
            </el-badge>
            <div class="session-info">
              <div class="session-name">{{ session.nickname }}</div>
              <div class="session-preview">{{ session.lastMessage }}</div>
            </div>
            <div class="session-time">{{ formatTime(session.lastMessageTime) }}</div>
          </div>
        </template>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-area">
      <template v-if="currentChatUser">
        <div class="chat-header">
          <div class="chat-user">
            <el-avatar :size="36" :src="currentChatUser.avatarUrl || defaultAvatar" />
            <span class="chat-username">{{ currentChatUser.nickname }}</span>
          </div>
          <div class="chat-actions">
            <el-button type="text" @click="markAllChatAsRead" :disabled="messagesLoading">
              标为已读
            </el-button>
          </div>
        </div>
        <el-divider />
        <div class="chat-messages" ref="messagesContainer">
          <el-empty v-if="messages.length === 0 && !messagesLoading" description="暂无消息记录" />
          <el-skeleton :rows="3" animated v-if="messagesLoading" :count="3" />
          <template v-else>
            <div
              v-for="message in messages"
              :key="message.messageId"
              class="message-item"
              :class="{
                'message-self': message.senderId === currentUserId,
                'message-other': message.senderId !== currentUserId
              }"
            >
              <div class="message-avatar">
                <el-avatar
                  :size="36"
                  :src="message.senderId === currentUserId
                    ? currentUserAvatar || defaultAvatar
                    : currentChatUser.avatarUrl || defaultAvatar"
                />
              </div>
              <div class="message-content">
                <div class="message-bubble">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.createdAt) }}</div>
              </div>
            </div>
          </template>
        </div>
        <div class="chat-input">
          <el-input
            v-model="messageContent"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keyup.enter.ctrl="sendMessage"
          />
          <div class="input-actions">
            <span class="input-tip">Ctrl + Enter 发送</span>
            <el-button type="primary" @click="sendMessage" :disabled="!messageContent.trim()">
              发送
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else description="选择一个联系人开始聊天" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useAuthStore } from '@/store/auth'
import {
  getChatSessions,
  getChatHistory,
  sendPrivateMessage,
  markAllMessagesAsRead
} from '@/api/message'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const sessions = ref<any[]>([])
const messages = ref<any[]>([])
const currentChatUser = ref<any>(null)
const messageContent = ref('')
const sessionsLoading = ref(false)
const messagesLoading = ref(false)
const searchKeyword = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const defaultAvatar = ref('/avatar-placeholder.png')

// 当前用户信息
const currentUserId = computed(() => authStore.user?.userId)
const currentUserAvatar = computed(() => authStore.user?.avatarUrl)

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 过滤后的会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) return sessions.value
  return sessions.value.filter(session =>
    session.nickname.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 获取会话列表
const fetchSessions = async () => {
  sessionsLoading.value = true
  try {
    const res = await getChatSessions({
      page: 1,
      size: 50 // 较大的值，一般用户不会有太多活跃会话
    })
    if (res.code === 200) {
      sessions.value = res.data.records || []
      
      // 如果路由有指定用户ID，选择该用户聊天
      const userId = route.params.userId
      if (userId) {
        const targetSession = sessions.value.find(session => session.userId == userId)
        if (targetSession) {
          selectChat(targetSession)
        } else {
          // 如果会话列表中没有该用户，需要获取用户信息并创建会话
          fetchUserAndCreateSession(Number(userId))
        }
      } else if (sessions.value.length > 0) {
        // 默认选择第一个会话
        selectChat(sessions.value[0])
      }
    }
  } catch (error) {
    console.error('获取会话列表失败', error)
    ElMessage.error('获取会话列表失败')
  } finally {
    sessionsLoading.value = false
  }
}

// 获取用户信息并创建会话
const fetchUserAndCreateSession = async (userId: number) => {
  try {
    // 这里应该调用获取用户信息的API
    // 由于示例中没有定义，暂时使用一个模拟对象
    const userInfo = {
      userId: userId,
      nickname: `用户${userId}`,
      avatarUrl: '',
      lastMessage: '',
      lastMessageTime: new Date(),
      unreadCount: 0
    }
    
    // 将新会话添加到列表
    sessions.value.unshift(userInfo)
    selectChat(userInfo)
  } catch (error) {
    console.error('获取用户信息失败', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 选择聊天对象
const selectChat = async (session: any) => {
  currentChatUser.value = session
  
  // 更新路由，但不触发重新加载
  router.replace({
    path: `/messages/${session.userId}`,
    query: route.query
  }, { 
    replace: true 
  })
  
  // 获取聊天历史
  await fetchChatHistory()
  
  // 如果有未读消息，标记为已读
  if (session.unreadCount > 0) {
    markAllChatAsRead()
  }
}

// 获取聊天历史
const fetchChatHistory = async () => {
  if (!currentChatUser.value) return
  
  messagesLoading.value = true
  try {
    const res = await getChatHistory(currentChatUser.value.userId, {
      page: currentPage.value,
      size: pageSize.value
    })
    if (res.code === 200) {
      messages.value = res.data.records || []
      
      // 更新会话中的未读数
      const index = sessions.value.findIndex(s => s.userId === currentChatUser.value.userId)
      if (index !== -1) {
        sessions.value[index].unreadCount = 0
      }
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取聊天历史失败', error)
    ElMessage.error('获取聊天历史失败')
  } finally {
    messagesLoading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageContent.value.trim() || !currentChatUser.value) return
  
  try {
    const data = {
      recipientId: currentChatUser.value.userId,
      content: messageContent.value.trim()
    }
    
    const res = await sendPrivateMessage(data)
    if (res.code === 200) {
      // 添加到消息列表
      const newMessage = {
        messageId: res.data,
        senderId: currentUserId.value,
        recipientId: currentChatUser.value.userId,
        content: messageContent.value.trim(),
        createdAt: new Date(),
        isRead: true
      }
      messages.value.push(newMessage)
      
      // 更新会话列表中的最后消息
      const index = sessions.value.findIndex(s => s.userId === currentChatUser.value.userId)
      if (index !== -1) {
        sessions.value[index].lastMessage = messageContent.value.trim()
        sessions.value[index].lastMessageTime = new Date()
        
        // 将当前会话移动到顶部
        const currentSession = sessions.value.splice(index, 1)[0]
        sessions.value.unshift(currentSession)
      }
      
      // 清空输入框
      messageContent.value = ''
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('发送消息失败', error)
    ElMessage.error('发送消息失败')
  }
}

// 标记所有消息为已读
const markAllChatAsRead = async () => {
  if (!currentChatUser.value) return
  
  try {
    const res = await markAllMessagesAsRead(currentChatUser.value.userId)
    if (res.code === 200) {
      // 更新会话列表中的未读数
      const index = sessions.value.findIndex(s => s.userId === currentChatUser.value.userId)
      if (index !== -1) {
        sessions.value[index].unreadCount = 0
      }
    }
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听路由变化
watch(
  () => route.params.userId,
  (newUserId) => {
    if (newUserId && (!currentChatUser.value || currentChatUser.value.userId != newUserId)) {
      const targetSession = sessions.value.find(session => session.userId == newUserId)
      if (targetSession) {
        selectChat(targetSession)
      } else {
        fetchUserAndCreateSession(Number(newUserId))
      }
    }
  }
)

onMounted(() => {
  fetchSessions()
})
</script>

<style scoped>
.message-container {
  height: 100%;
  display: flex;
  background-color: var(--el-bg-color);
}

.session-list {
  width: 300px;
  border-right: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;
}

.session-header {
  padding: 15px;
}

.session-search {
  padding: 0 15px 15px;
}

.session-items {
  flex: 1;
  overflow-y: auto;
  padding: 0 15px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.session-item:hover {
  background-color: var(--el-fill-color-light);
}

.session-active {
  background-color: var(--el-color-primary-light-9);
}

.session-badge {
  margin-right: 12px;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-name {
  font-weight: bold;
  margin-bottom: 4px;
  color: var(--el-text-color-primary);
}

.session-preview {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  align-self: flex-start;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 20px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
}

.chat-user {
  display: flex;
  align-items: center;
}

.chat-username {
  margin-left: 10px;
  font-weight: bold;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.message-self {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 70%;
}

.message-self .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 10px;
  background-color: var(--el-color-primary-light-9);
  word-break: break-word;
}

.message-self .message-bubble {
  background-color: var(--el-color-primary);
  color: white;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
}

.chat-input {
  padding: 15px 0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.input-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
