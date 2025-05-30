<template>
  <div class="websocket-debug">
    <h2>WebSocket 连接测试</h2>
    
    <el-card class="connection-card">
      <template #header>
        <div class="card-header">
          <span>连接状态</span>
          <el-tag :type="webSocketService.isConnected.value ? 'success' : 'danger'">
            {{ webSocketService.isConnected.value ? '已连接' : '未连接' }}
          </el-tag>
        </div>
      </template>
      
      <div class="connection-info">
        <p v-if="webSocketService.lastError.value" class="error-message">
          错误: {{ webSocketService.lastError.value }}
        </p>
        
        <div class="actions">
          <el-button type="primary" @click="connect" :disabled="!userId || webSocketService.isConnected.value">
            连接
          </el-button>
          <el-button type="danger" @click="disconnect" :disabled="!webSocketService.isConnected.value">
            断开
          </el-button>
          <el-button @click="sendPing" :disabled="!webSocketService.isConnected.value">
            发送 Ping
          </el-button>
        </div>
        
        <el-form label-position="top" class="connection-form">
          <el-form-item label="用户 ID">
            <el-input v-model="userId" placeholder="输入数字用户ID" type="number"></el-input>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card class="message-card">
      <template #header>
        <div class="card-header">
          <span>消息记录</span>
          <el-button type="text" @click="clearMessages">清空</el-button>
        </div>
      </template>
      
      <div class="message-list" ref="messageListRef">
        <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.type">
          <div class="message-time">{{ formatTime(msg.time) }}</div>
          <div class="message-type">{{ msg.type }}</div>
          <pre class="message-content">{{ msg.content }}</pre>
        </div>
        
        <div v-if="messages.length === 0" class="empty-message">
          暂无消息记录
        </div>
      </div>
    </el-card>
    
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>API 测试</span>
        </div>
      </template>
      
      <div class="test-actions">
        <el-button @click="checkStatus" type="info">
          检查 WebSocket 状态
        </el-button>
        
        <el-button @click="checkUserOnline" type="info">
          检查用户是否在线
        </el-button>
        
        <el-button @click="sendTestMessage('NOTIFICATION')" type="success">
          发送测试通知
        </el-button>
        
        <el-button @click="sendTestMessage('MESSAGE')" type="warning">
          发送测试消息
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import webSocketService from '../utils/websocket';
import axios from 'axios';
import { useAuthStore } from '../store/auth';

const authStore = useAuthStore();
const userId = ref<number | null>(null);
const messages = ref<Array<{type: string, content: string, time: Date}>>([]);
const messageListRef = ref<HTMLElement | null>(null);

// 确保Vue能正确追踪组件中使用的reactive对象
console.log('WebSocketDebug组件已加载，WebSocket状态:', webSocketService.isConnected.value);

// 初始化用户ID
onMounted(() => {
  console.log('WebSocketDebug组件mounted，isConnected:', webSocketService.isConnected.value);
  console.log('WebSocketDebug组件mounted，lastError:', webSocketService.lastError.value);
  
  if (authStore.isAuthenticated && authStore.user?.userId) {
    userId.value = authStore.user.userId;
    console.log('从authStore获取到用户ID:', userId.value);
  } else {
    console.warn('用户未认证或无法获取用户ID');
  }
  
  // 添加消息监听器
  webSocketService.addMessageListener(handleMessage);
  webSocketService.addNotificationListener(handleNotification);
  webSocketService.addConnectionListener(handleConnectionChange);
  
  // 添加初始消息
  addMessage('SYSTEM', 'WebSocket调试页面已加载');
});

onBeforeUnmount(() => {
  // 断开连接
  disconnect();
});

// 连接WebSocket
const connect = () => {
  if (!userId.value) {
    ElMessage.warning('请输入用户ID');
    return;
  }
  
  try {
    console.log('尝试连接WebSocket，用户ID:', userId.value);
    addMessage('SYSTEM', `正在连接WebSocket，用户ID: ${userId.value}...`);
    webSocketService.connect(userId.value);
  } catch (error) {
    console.error('连接WebSocket时发生错误:', error);
    addMessage('ERROR', `连接失败: ${error}`);
    ElMessage.error(`连接失败: ${error}`);
  }
};

// 断开WebSocket
const disconnect = () => {
  webSocketService.disconnect();
  addMessage('SYSTEM', 'WebSocket已断开');
};

// 发送Ping消息
const sendPing = () => {
  if (!webSocketService.isConnected.value) {
    ElMessage.warning('WebSocket未连接');
    return;
  }
  
  webSocketService.sendMessage({ type: 'PING', timestamp: Date.now() });
  addMessage('SENT', 'PING消息已发送');
};

// 处理接收到的消息
const handleMessage = (data: any) => {
  addMessage('RECEIVED', JSON.stringify(data, null, 2));
};

// 处理接收到的通知
const handleNotification = (data: any) => {
  addMessage('NOTIFICATION', JSON.stringify(data, null, 2));
};

// 处理连接状态变化
const handleConnectionChange = (connected: boolean) => {
  addMessage('CONNECTION', connected ? '已连接' : '已断开');
};

// 添加消息到列表
const addMessage = (type: string, content: string) => {
  messages.value.push({
    type,
    content,
    time: new Date()
  });
  
  // 滚动到最新消息
  setTimeout(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  }, 50);
};

// 清空消息记录
const clearMessages = () => {
  messages.value = [];
};

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString();
};

// API测试功能
const checkStatus = async () => {
  try {
    const response = await axios.get('/api/websocket/status');
    if (response.data.code === 200) {
      addMessage('API', `WebSocket状态: ${JSON.stringify(response.data.data, null, 2)}`);
    } else {
      addMessage('ERROR', `获取状态失败: ${response.data.msg}`);
    }
  } catch (error) {
    addMessage('ERROR', `API请求错误: ${error}`);
  }
};

const checkUserOnline = async () => {
  if (!userId.value) {
    ElMessage.warning('请输入用户ID');
    return;
  }
  
  try {
    const response = await axios.get(`/api/websocket/user/${userId.value}/online`);
    if (response.data.code === 200) {
      addMessage('API', `用户${userId.value}在线状态: ${response.data.data ? '在线' : '离线'}`);
    } else {
      addMessage('ERROR', `检查在线状态失败: ${response.data.msg}`);
    }
  } catch (error) {
    addMessage('ERROR', `API请求错误: ${error}`);
  }
};

const sendTestMessage = async (type: string) => {
  if (!userId.value) {
    ElMessage.warning('请输入用户ID');
    return;
  }
  
  try {
    const content = type === 'NOTIFICATION' ? '这是一条测试通知' : '这是一条测试消息';
    const response = await axios.post(
      `/api/websocket/user/${userId.value}/send?type=${type}&content=${encodeURIComponent(content)}`
    );
    
    if (response.data.code === 200) {
      addMessage('API', `${type}消息已发送`);
    } else {
      addMessage('ERROR', `发送消息失败: ${response.data.msg}`);
    }
  } catch (error) {
    addMessage('ERROR', `API请求错误: ${error}`);
  }
};
</script>

<style scoped>
.websocket-debug {
  max-width: 900px;
  margin: 20px auto;
  padding: 20px;
}

.connection-card,
.message-card,
.test-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.connection-info {
  margin-bottom: 15px;
}

.error-message {
  color: #f56c6c;
  margin-bottom: 15px;
}

.actions {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.message-list {
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background-color: #f5f7fa;
}

.message-item {
  margin-bottom: 10px;
  padding: 8px;
  border-radius: 4px;
  position: relative;
}

.message-item.SYSTEM {
  background-color: #e1f3d8;
  border-left: 3px solid #67c23a;
}

.message-item.ERROR {
  background-color: #fde2e2;
  border-left: 3px solid #f56c6c;
}

.message-item.RECEIVED {
  background-color: #e6f1fc;
  border-left: 3px solid #409eff;
}

.message-item.SENT {
  background-color: #fdf6ec;
  border-left: 3px solid #e6a23c;
}

.message-item.NOTIFICATION {
  background-color: #f0f9eb;
  border-left: 3px solid #67c23a;
}

.message-item.CONNECTION {
  background-color: #f4f4f5;
  border-left: 3px solid #909399;
}

.message-item.API {
  background-color: #f5f2fd;
  border-left: 3px solid #9254de;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.message-type {
  font-weight: bold;
  margin-bottom: 5px;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
  margin: 0;
  font-size: 12px;
}

.empty-message {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.test-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.connection-form {
  margin-top: 20px;
}

/* 暗色模式适配 */
[data-theme="dark"] .message-list {
  background-color: #1a1a1a;
  border-color: #2c2c2c;
}

[data-theme="dark"] .message-item.SYSTEM {
  background-color: rgba(103, 194, 58, 0.1);
  border-left-color: #67c23a;
}

[data-theme="dark"] .message-item.ERROR {
  background-color: rgba(245, 108, 108, 0.1);
  border-left-color: #f56c6c;
}

[data-theme="dark"] .message-item.RECEIVED {
  background-color: rgba(64, 158, 255, 0.1);
  border-left-color: #409eff;
}

[data-theme="dark"] .message-item.SENT {
  background-color: rgba(230, 162, 60, 0.1);
  border-left-color: #e6a23c;
}

[data-theme="dark"] .message-item.NOTIFICATION {
  background-color: rgba(103, 194, 58, 0.1);
  border-left-color: #67c23a;
}

[data-theme="dark"] .message-item.CONNECTION {
  background-color: rgba(144, 147, 153, 0.1);
  border-left-color: #909399;
}

[data-theme="dark"] .message-item.API {
  background-color: rgba(146, 84, 222, 0.1);
  border-left-color: #9254de;
}
</style>
