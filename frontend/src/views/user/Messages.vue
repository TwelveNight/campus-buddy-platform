<template>
    <div class="message-container">
        <!-- 左侧会话列表 -->
        <div class="session-list">
            <div class="session-header">
                <h3>消息会话</h3>
                <el-tooltip content="刷新会话列表" placement="top">
                    <el-button size="small" type="text" circle @click="fetchSessions" :loading="sessionsLoading">
                        <el-icon><Refresh /></el-icon>
                    </el-button>
                </el-tooltip>
            </div>
            <div class="session-search">
                <el-input v-model="searchKeyword" placeholder="搜索联系人" clearable>
                    <template #prefix>
                        <el-icon><Search /></el-icon>
                    </template>
                </el-input>
            </div>
            <div class="session-items">
                <el-empty v-if="sessions.length === 0 && !sessionsLoading" description="暂无消息会话">
                    <template #image>
                        <el-icon class="empty-icon"><ChatDotRound /></el-icon>
                    </template>
                </el-empty>
                <el-skeleton :rows="3" animated v-if="sessionsLoading" :count="3" />
                <template v-else>
                    <div v-for="session in filteredSessions" :key="session.userId" class="session-item"
                        :class="{ 'session-active': currentChatUser?.userId === session.userId }"
                        @click="selectChat(session)">
                        <el-badge :value="session.unreadCount" :max="99" :hidden="!session.unreadCount" class="session-badge" type="danger">
                            <el-avatar :size="48" :src="session.avatarUrl || defaultAvatar">
                                {{ session.nickname ? session.nickname.substring(0, 1) : '用' }}
                            </el-avatar>
                        </el-badge>
                        <div class="session-info">
                            <div class="session-name">{{ session.nickname }}</div>
                            <div class="session-preview" :class="{'has-unread': session.unreadCount > 0}">
                                {{ session.lastMessage || '暂无消息' }}
                            </div>
                        </div>
                        <div class="session-meta">
                            <div class="session-time">{{ formatTime(session.lastMessageTime) }}</div>
                            <div v-if="session.unreadCount > 0" class="unread-indicator"></div>
                        </div>
                    </div>
                </template>
            </div>
        </div>

        <!-- 右侧聊天区域 -->
        <div class="chat-area">
            <template v-if="currentChatUser">
                <div class="chat-header">
                    <div class="chat-user">
                        <el-avatar :size="40" :src="currentChatUser.avatarUrl || defaultAvatar">
                            {{ currentChatUser.nickname ? currentChatUser.nickname.substring(0, 1) : '用' }}
                        </el-avatar>
                        <div class="chat-user-info">
                            <div class="chat-username">{{ currentChatUser.nickname }}</div>
                            <div class="user-status">
                                <el-tag size="small" type="success" effect="plain">在线</el-tag>
                            </div>
                        </div>
                    </div>
                    <div class="chat-actions">
                        <el-tooltip content="标记为已读" placement="top">
                            <el-button 
                                type="primary" 
                                size="small" 
                                plain 
                                circle 
                                @click="markAllChatAsRead" 
                                :disabled="messagesLoading || currentChatUser.unreadCount === 0">
                                <el-icon><Check /></el-icon>
                            </el-button>
                        </el-tooltip>
                        <el-tooltip content="查看资料" placement="top">
                            <el-button type="info" size="small" plain circle @click="viewUserProfile">
                                <el-icon><User /></el-icon>
                            </el-button>
                        </el-tooltip>
                    </div>
                </div>
                
                <div class="chat-messages" ref="messagesContainer">
                    <el-empty v-if="messages.length === 0 && !messagesLoading" description="暂无消息记录">
                        <template #image>
                            <el-icon class="empty-icon"><ChatDotRound /></el-icon>
                        </template>
                    </el-empty>
                    <el-skeleton :rows="3" animated v-if="messagesLoading" :count="3" />
                    <template v-else>
                        <div v-if="hasMoreMessages" class="load-more-messages">
                            <el-button type="primary" plain size="small" @click="loadMoreMessages" :loading="loadingMoreMessages">
                                <el-icon><ArrowUp /></el-icon> 加载更多消息
                            </el-button>
                        </div>
                        <div class="messages-date-divider" v-if="messages.length > 0">
                            <span>{{ formatDateDivider(messages[0].createdAt) }}</span>
                        </div>
                        <div v-for="(message, index) in messages" :key="message.messageId" class="message-item" :class="{
                            'message-self': message.senderId === currentUserId,
                            'message-other': message.senderId !== currentUserId,
                            'message-consecutive': index > 0 && message.senderId === messages[index-1].senderId
                        }">
                            <div class="message-avatar" v-if="index === 0 || message.senderId !== messages[index-1].senderId">
                                <el-avatar :size="40" :src="message.senderId === currentUserId
                                    ? currentUserAvatar || defaultAvatar
                                    : currentChatUser.avatarUrl || defaultAvatar">
                                    {{ message.senderId === currentUserId ? '我' : currentChatUser.nickname.substring(0, 1) }}
                                </el-avatar>
                            </div>
                            <div class="message-avatar-placeholder" v-else></div>
                            <div class="message-content">
                                <div class="message-sender" v-if="index === 0 || message.senderId !== messages[index-1].senderId">
                                    {{ message.senderId === currentUserId ? '我' : currentChatUser.nickname }}
                                </div>
                                <div class="message-bubble">
                                    {{ message.content }}
                                </div>
                                <div class="message-time">
                                    {{ formatMessageTime(message.createdAt) }}
                                    <el-icon v-if="message.senderId === currentUserId" class="message-status-icon">
                                        <el-icon v-if="message.isRead"><CircleCheck /></el-icon>
                                        <el-icon v-else><Check /></el-icon>
                                    </el-icon>
                                </div>
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
                        resize="none"
                        maxlength="500"
                        show-word-limit
                        @keyup.enter.ctrl="sendMessage" 
                    />
                    <div class="input-actions">
                        <div class="input-tools">
                            <el-tooltip content="表情" placement="top">
                                <el-button type="text" class="tool-button">
                                    <el-icon><Position /></el-icon>
                                </el-button>
                            </el-tooltip>
                            <el-tooltip content="图片" placement="top">
                                <el-button type="text" class="tool-button">
                                    <el-icon><Picture /></el-icon>
                                </el-button>
                            </el-tooltip>
                            <span class="input-tip">Ctrl + Enter 发送</span>
                        </div>
                        <el-button type="primary" @click="sendMessage" :disabled="!messageContent.trim()" :loading="sending">
                            <el-icon class="send-icon"><Position /></el-icon> 发送
                        </el-button>
                    </div>
                </div>
            </template>
            <div v-else class="empty-chat">
                <el-empty description="选择一个联系人开始聊天">
                    <template #image>
                        <el-icon class="empty-icon-large"><ChatDotRound /></el-icon>
                    </template>
                    <template #description>
                        <p>选择一个联系人开始聊天</p>
                        <p class="empty-chat-tip">或者在"好友"页面添加新的联系人</p>
                    </template>
                </el-empty>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
    Search, Refresh, Check, User, ArrowUp, 
    CircleCheck, ChatDotRound, Picture, Position
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useAuthStore } from '@/store/auth'
import {
    getChatSessions,
    getChatHistory,
    sendPrivateMessage,
    markAllMessagesAsRead,
    getUnreadMessageCount
} from '@/api/message'
import { getUserById } from '@/api/user'
import type { ChatSession, ChatMessage } from '@/types/message'
import webSocketService from '@/utils/websocket'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const sessions = ref<ChatSession[]>([])
const messages = ref<ChatMessage[]>([])
const currentChatUser = ref<ChatSession | null>(null)
const messageContent = ref('')
const sessionsLoading = ref(false)
const messagesLoading = ref(false)
const loadingMoreMessages = ref(false)
const searchKeyword = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const totalMessages = ref(0)
const hasMoreMessages = ref(false)
const defaultAvatar = ref('/avatar-placeholder.png')
const sending = ref(false)

// 当前用户信息
const currentUserId = computed(() => authStore.user?.userId)
const currentUserAvatar = computed(() => authStore.user?.avatarUrl)

// 格式化时间
const formatTime = (time: string | Date) => {
    if (!time) return ''
    return dayjs(typeof time === 'string' ? time : time.toISOString()).fromNow()
}

// 格式化消息时间 - 使用更精确的时间格式
const formatMessageTime = (time: string | Date) => {
    if (!time) return ''
    return dayjs(typeof time === 'string' ? time : time.toISOString()).format('HH:mm')
}

// 格式化日期分隔线 - 按天分组消息
const formatDateDivider = (time: string | Date) => {
    if (!time) return ''
    const messageDate = dayjs(typeof time === 'string' ? time : time.toISOString())
    const today = dayjs()
    
    if (messageDate.isSame(today, 'day')) {
        return '今天'
    } else if (messageDate.isSame(today.subtract(1, 'day'), 'day')) {
        return '昨天'
    } else if (messageDate.isSame(today, 'year')) {
        return messageDate.format('MM月DD日')
    } else {
        return messageDate.format('YYYY年MM月DD日')
    }
}

// 查看用户资料
const viewUserProfile = () => {
    if (!currentChatUser.value) return
    router.push(`/user/${currentChatUser.value.userId}`)
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
        if (res.data.code === 200) {
            sessions.value = res.data.data.records || []

            // 如果路由有指定用户ID，选择该用户聊天
            const userId = route.params.userId
            if (userId) {
                const uid = Number(userId)
                const targetSession = sessions.value.find(session => session.userId === uid)
                if (targetSession) {
                    selectChat(targetSession)
                } else {
                    // 如果会话列表中没有该用户，需要获取用户信息并创建会话
                    fetchUserAndCreateSession(uid)
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
        // 调用真实API获取用户信息
        const res = await getUserById(userId)
        if (res.data.code === 200) {
            const user = res.data.data
            const userInfo = {
                userId: user.userId,
                nickname: user.nickname,
                avatarUrl: user.avatarUrl,
                lastMessage: '',
                lastMessageTime: new Date().toISOString(),
                unreadCount: 0
            }
            sessions.value.unshift(userInfo)
            selectChat(userInfo)
        } else {
            ElMessage.error('获取用户信息失败')
        }
    } catch (error) {
        console.error('获取用户信息失败', error)
        ElMessage.error('获取用户信息失败')
    }
}

// 选择聊天对象
const selectChat = async (session: ChatSession) => {
    // 如果头像或昵称缺失，主动补全
    if (!session.avatarUrl || !session.nickname) {
        await fetchUserAndCreateSession(session.userId)
        return
    }
    currentChatUser.value = session

    // 更新路由，但不触发重新加载
    router.replace({
        path: `/messages/${session.userId}`,
        query: route.query
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
        // 先获取总消息数，以便获取最新的消息
        const totalResponse = await getChatHistory(currentChatUser.value.userId, {
            page: 1,
            size: 1 // 只查询一条来获取总数
        })
        
        if (totalResponse.data.code === 200) {
            totalMessages.value = totalResponse.data.data.total
            // 计算包含最新消息的页码
            const latestPage = Math.max(1, Math.ceil(totalMessages.value / pageSize.value))
            
            // 获取最新的消息
            const res = await getChatHistory(currentChatUser.value.userId, {
                page: latestPage, // 使用计算出的最新页码
                size: pageSize.value
            })
            
            if (res.data.code === 200) {
                messages.value = res.data.data.records || []
                // 记录当前页码，便于后续加载更多旧消息
                currentPage.value = latestPage
                
                // 检查是否还有更多历史消息可以加载
                hasMoreMessages.value = currentPage.value > 1

                // 更新会话中的未读数
                const index = sessions.value.findIndex(s => currentChatUser.value && s.userId === currentChatUser.value.userId)
                if (index !== -1) {
                    sessions.value[index].unreadCount = 0
                }

                // 滚动到底部
                await nextTick()
                scrollToBottom()
            }
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

    sending.value = true
    try {
        const data = {
            recipientId: currentChatUser.value.userId,
            content: messageContent.value.trim()
        }

        const res = await sendPrivateMessage(data)
        if (res.data.code === 200) {
            // 添加到消息列表
            const newMessage = {
                messageId: res.data.data,
                senderId: currentUserId.value || 0,
                recipientId: currentChatUser.value.userId,
                content: messageContent.value.trim(),
                createdAt: new Date().toISOString(),
                isRead: true
            }
            messages.value.push(newMessage)
            
            // 增加总消息数
            totalMessages.value++

            // 更新会话列表中的最后消息
            const index = sessions.value.findIndex(s => currentChatUser.value && s.userId === currentChatUser.value.userId)
            if (index !== -1) {
                sessions.value[index].lastMessage = messageContent.value.trim()
                sessions.value[index].lastMessageTime = new Date().toISOString()

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
    } finally {
        sending.value = false
    }
}

// 标记所有消息为已读
const markAllChatAsRead = async () => {
    if (!currentChatUser.value) return

    try {
        const res = await markAllMessagesAsRead(currentChatUser.value.userId)
        if (res.data.code === 200) {
            // 更新会话列表中的未读数
            const index = sessions.value.findIndex(s => currentChatUser.value && s.userId === currentChatUser.value.userId)
            if (index !== -1) {
                sessions.value[index].unreadCount = 0
            }
            
            // 刷新全局未读消息计数
            refreshUnreadMessageCount();
        }
    } catch (error) {
        console.error('标记已读失败', error)
    }
}

// 加载更多历史消息
const loadMoreMessages = async () => {
    if (!currentChatUser.value || loadingMoreMessages.value || currentPage.value <= 1) return

    loadingMoreMessages.value = true
    try {
        // 获取前一页的消息
        const previousPage = currentPage.value - 1
        const res = await getChatHistory(currentChatUser.value.userId, {
            page: previousPage,
            size: pageSize.value
        })
        
        if (res.data.code === 200) {
            const oldMessages = res.data.data.records || []
            if (oldMessages.length > 0) {
                // 保存当前滚动位置
                const messagesEl = messagesContainer.value
                const scrollHeight = messagesEl ? messagesEl.scrollHeight : 0
                const scrollTop = messagesEl ? messagesEl.scrollTop : 0
                
                // 在消息列表前面添加较旧的消息
                messages.value = [...oldMessages, ...messages.value]
                
                // 更新当前页码
                currentPage.value = previousPage
                
                // 检查是否还有更多历史消息可以加载
                hasMoreMessages.value = previousPage > 1
                
                // 恢复滚动位置，确保用户继续查看之前的位置
                await nextTick()
                if (messagesEl) {
                    const newScrollHeight = messagesEl.scrollHeight
                    messagesEl.scrollTop = scrollTop + (newScrollHeight - scrollHeight)
                }
            } else {
                hasMoreMessages.value = false
            }
        }
    } catch (error) {
        console.error('加载更多消息失败', error)
        ElMessage.error('加载更多消息失败')
    } finally {
        loadingMoreMessages.value = false
    }
}

// 滚动到底部
const scrollToBottom = () => {
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
}

// 更新全局未读消息计数
const refreshUnreadMessageCount = async () => {
    if (!authStore.isAuthenticated) return;
    
    try {
        // 获取最新的未读消息数量
        const res = await getUnreadMessageCount();
        if (res.data.code === 200) {
            console.log('消息页面刷新未读消息数量:', res.data.data.count);
            
            // 更新导航栏中显示的未读消息数 - 触发自定义事件
            const event = new CustomEvent('update-unread-message-count', { 
                detail: { count: res.data.data.count } 
            });
            window.dispatchEvent(event);
        }
    } catch (error) {
        console.error('获取未读消息数量失败', error);
    }
};

// 处理WebSocket接收到的私信
const handleNewMessage = (data: any) => {
    if (!data || data.type !== 'PRIVATE_MESSAGE') return

    // 调试输出，便于排查
    console.log('Messages.vue 收到新消息', data, currentChatUser.value)

    // 判断 senderId 类型一致
    const senderId = Number(data.senderId)
    const chatUserId = currentChatUser.value ? Number(currentChatUser.value.userId) : null

    if (chatUserId && senderId === chatUserId) {
        // 添加新消息到聊天记录
        messages.value.push({
            messageId: data.messageId,
            senderId: senderId,
            recipientId: currentUserId.value || 0,
            content: data.content,
            createdAt: new Date(data.timestamp),
            isRead: false
        })
        
        // 增加总消息数
        totalMessages.value++
        
        nextTick(scrollToBottom)
        
        // 只有当前界面打开且处于活动状态时才自动标记为已读
        if (document.visibilityState === 'visible' && route.path.includes('/messages/')) {
            markAllChatAsRead()
        } 
        // 不再在这里更新未读消息计数，避免重复计数
    } 
    // 不再在这里更新未读消息计数，由NavBarWithWebSocket统一处理

    // 更新会话列表
    const sessionIndex = sessions.value.findIndex(s => Number(s.userId) === senderId)
    if (sessionIndex !== -1) {
        // 更新现有会话
        sessions.value[sessionIndex].lastMessage = data.content
        sessions.value[sessionIndex].lastMessageTime = new Date(data.timestamp)

        // 如果不是当前聊天对象，增加未读数
        if (!currentChatUser.value || senderId !== chatUserId) {
            sessions.value[sessionIndex].unreadCount++
        }

        // 移动到顶部
        const session = sessions.value.splice(sessionIndex, 1)[0]
        sessions.value.unshift(session)
    } else {
        // 创建新会话
        sessions.value.unshift({
            userId: senderId,
            nickname: data.senderName || `用户 #${senderId}`,
            avatarUrl: data.senderAvatar,
            lastMessage: data.content,
            lastMessageTime: new Date(data.timestamp),
            unreadCount: 1
        })
    }
}

// 监听路由变化
watch(
    () => route.params.userId,
    (newUserId) => {
        if (newUserId && (!currentChatUser.value || currentChatUser.value.userId !== Number(newUserId))) {
            const uid = Number(newUserId)
            const targetSession = sessions.value.find(session => session.userId === uid)
            if (targetSession) {
                selectChat(targetSession)
            } else {
                fetchUserAndCreateSession(uid)
            }
        }
    }
)

onMounted(() => {
    fetchSessions()

    // 监听WebSocket私信
    webSocketService.addMessageListener(handleNewMessage)

    // 如果已认证但WebSocket未连接，重新连接
    if (authStore.isAuthenticated && authStore.user?.userId && !webSocketService.isConnected.value) {
        webSocketService.connect(authStore.user.userId);
    }

    // 进入消息页面时，刷新未读消息数量
    refreshUnreadMessageCount();

    // 处理路由中的chat_with参数
    if (route.query.chat_with && typeof route.query.chat_with === 'string') {
        const userId = parseInt(route.query.chat_with as string)
        if (!isNaN(userId)) {
            fetchUserAndCreateSession(userId)
        }
    }
    
    // 添加页面可见性变化事件监听
    document.addEventListener('visibilitychange', handleVisibilityChange);
})

// 处理页面可见性变化
const handleVisibilityChange = () => {
    // 当页面变为可见且在消息页面且有当前选中的聊天对象时，标记为已读
    if (document.visibilityState === 'visible' && 
        route.path.includes('/messages/') && 
        currentChatUser.value && 
        currentChatUser.value.unreadCount > 0) {
        markAllChatAsRead();
    }
};

// 组件卸载前的清理工作
onBeforeUnmount(() => {
    // 离开消息页面时刷新一次未读消息数量
    refreshUnreadMessageCount();
    
    // 移除页面可见性变化事件监听
    document.removeEventListener('visibilitychange', handleVisibilityChange);
});
</script>

<style scoped>
.message-container {
    height: 100%;
    display: flex;
    background-color: var(--el-bg-color);
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.session-list {
    width: 320px;
    border-right: 1px solid var(--el-border-color-light);
    display: flex;
    flex-direction: column;
    background-color: var(--card-bg, #ffffff);
}

.session-header {
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid var(--el-border-color-lighter);
}

.session-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary, #303133);
}

.session-search {
    padding: 12px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
}

.session-items {
    flex: 1;
    overflow-y: auto;
    padding: 10px;
}

.session-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 10px;
    margin-bottom: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
}

.session-item:hover {
    background-color: var(--el-fill-color-light);
    transform: translateY(-2px);
}

.session-active {
    background-color: var(--primary-color-light, rgba(64, 158, 255, 0.1));
    border-left: 3px solid var(--primary-color, #409eff);
}

.session-badge {
    margin-right: 12px;
}

.session-info {
    flex: 1;
    overflow: hidden;
    margin-right: 10px;
}

.session-name {
    font-weight: 600;
    margin-bottom: 6px;
    color: var(--text-primary, #303133);
    font-size: 15px;
}

.session-preview {
    font-size: 13px;
    color: var(--text-secondary, #909399);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 180px;
}

.session-preview.has-unread {
    color: var(--text-primary, #303133);
    font-weight: 500;
}

.session-meta {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
}

.session-time {
    font-size: 12px;
    color: var(--text-placeholder, #c0c4cc);
    margin-bottom: 5px;
}

.unread-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: var(--el-color-danger);
}

.chat-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: var(--bg-color, #f5f7fa);
}

.chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background-color: var(--card-bg, #ffffff);
    border-bottom: 1px solid var(--el-border-color-lighter);
}

.chat-user {
    display: flex;
    align-items: center;
}

.chat-user-info {
    margin-left: 12px;
}

.chat-username {
    font-weight: 600;
    color: var(--text-primary, #303133);
    font-size: 16px;
    margin-bottom: 4px;
}

.user-status {
    font-size: 12px;
}

.chat-actions {
    display: flex;
    gap: 8px;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background-color: var(--bg-color, #f5f7fa);
    display: flex;
    flex-direction: column;
}

.messages-date-divider {
    text-align: center;
    margin: 20px 0;
    position: relative;
}

.messages-date-divider::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    width: 100%;
    height: 1px;
    background-color: var(--el-border-color-lighter);
    z-index: 1;
}

.messages-date-divider span {
    position: relative;
    background-color: var(--bg-color, #f5f7fa);
    padding: 0 10px;
    font-size: 13px;
    color: var(--text-secondary, #909399);
    z-index: 2;
}

.message-item {
    display: flex;
    margin-bottom: 20px;
    align-items: flex-start;
    max-width: 80%;
}

.message-consecutive {
    margin-top: -10px;
}

.message-self {
    flex-direction: row-reverse;
    align-self: flex-end;
}

.message-avatar {
    margin: 0 10px;
    flex-shrink: 0;
}

.message-avatar-placeholder {
    width: 40px;
    margin: 0 10px;
    flex-shrink: 0;
}

.message-content {
    display: flex;
    flex-direction: column;
    max-width: 100%;
}

.message-sender {
    font-size: 13px;
    color: var(--text-secondary, #909399);
    margin-bottom: 4px;
}

.message-self .message-content {
    align-items: flex-end;
}

.message-bubble {
    padding: 12px 16px;
    border-radius: 18px;
    background-color: var(--card-bg, #ffffff);
    word-break: break-word;
    font-size: 14px;
    color: var(--text-primary, #303133);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    margin-bottom: 4px;
    max-width: 100%;
    line-height: 1.5;
}

.message-self .message-bubble {
    background-color: var(--primary-color, #409eff);
    color: white;
    border-bottom-right-radius: 4px;
}

.message-other .message-bubble {
    border-bottom-left-radius: 4px;
}

.message-time {
    font-size: 11px;
    color: var(--text-placeholder, #c0c4cc);
    display: flex;
    align-items: center;
    gap: 4px;
}

.message-status-icon {
    font-size: 12px;
    margin-left: 2px;
}

.chat-input {
    padding: 20px;
    background-color: var(--card-bg, #ffffff);
    border-top: 1px solid var(--el-border-color-lighter);
}

.input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
}

.input-tools {
    display: flex;
    align-items: center;
}

.tool-button {
    font-size: 18px;
    margin-right: 10px;
    color: var(--text-secondary, #909399);
}

.input-tip {
    font-size: 12px;
    color: var(--text-secondary, #909399);
    margin-left: 10px;
}

.send-icon {
    margin-right: 4px;
}

.load-more-messages {
    text-align: center;
    padding: 10px 0;
    margin-bottom: 15px;
}

.empty-chat {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--bg-color, #f5f7fa);
}

.empty-icon {
    font-size: 60px;
    color: var(--text-secondary, #909399);
    opacity: 0.3;
}

.empty-icon-large {
    font-size: 80px;
    color: var(--text-secondary, #909399);
    opacity: 0.3;
}

.empty-chat-tip {
    color: var(--text-secondary, #909399);
    font-size: 14px;
    margin-top: 8px;
}

/* 暗色主题适配 */
[data-theme="dark"] .session-list {
    background-color: var(--dark-card-bg, #252529);
    border-color: var(--dark-border-color, #4c4d4f);
}

[data-theme="dark"] .session-header {
    border-color: var(--dark-border-lighter, #2e2e2f);
}

[data-theme="dark"] .session-header h3 {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .session-search {
    border-color: var(--dark-border-lighter, #2e2e2f);
}

[data-theme="dark"] .session-item:hover {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.05));
}

[data-theme="dark"] .session-active {
    background-color: rgba(64, 158, 255, 0.15);
    border-left-color: var(--primary-color-dark, #60a9ff);
}

[data-theme="dark"] .session-name {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .session-preview {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .session-preview.has-unread {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .session-time {
    color: var(--dark-text-placeholder, #8d9095);
}

[data-theme="dark"] .chat-area {
    background-color: var(--dark-bg, #1e1e20);
}

[data-theme="dark"] .chat-header {
    background-color: var(--dark-card-bg, #252529);
    border-color: var(--dark-border-lighter, #2e2e2f);
}

[data-theme="dark"] .chat-username {
    color: var(--dark-text-primary, #e5eaf3);
}

[data-theme="dark"] .chat-messages {
    background-color: var(--dark-bg, #1e1e20);
}

[data-theme="dark"] .messages-date-divider::before {
    background-color: var(--dark-border-color, #4c4d4f);
}

[data-theme="dark"] .messages-date-divider span {
    background-color: var(--dark-bg, #1e1e20);
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .message-sender {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .message-bubble {
    background-color: var(--dark-card-bg, #252529);
    color: var(--dark-text-primary, #e5eaf3);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .message-self .message-bubble {
    background-color: var(--primary-color-dark, #60a9ff);
    color: white;
}

[data-theme="dark"] .message-time {
    color: var(--dark-text-placeholder, #8d9095);
}

[data-theme="dark"] .chat-input {
    background-color: var(--dark-card-bg, #252529);
    border-color: var(--dark-border-lighter, #2e2e2f);
}

[data-theme="dark"] .tool-button {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .input-tip {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .empty-chat {
    background-color: var(--dark-bg, #1e1e20);
}

[data-theme="dark"] .empty-icon,
[data-theme="dark"] .empty-icon-large {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .empty-chat-tip {
    color: var(--dark-text-secondary, #a3a6ad);
}

/* 响应式设计 */
@media (max-width: 992px) {
    .session-list {
        width: 280px;
    }
    
    .session-preview {
        max-width: 140px;
    }
}

@media (max-width: 768px) {
    .message-container {
        flex-direction: column;
        height: calc(100vh - 110px);
    }
    
    .session-list {
        width: 100%;
        height: 200px;
        overflow-y: auto;
    }
    
    .message-item {
        max-width: 90%;
    }
}
</style>
