<template>
  <div class="simple-websocket-debug">
    <h2>WebSocket 简易测试</h2>
    
    <div class="status-card">
      <h3>连接状态</h3>
      <div class="status">
        <div class="status-indicator" :class="{ 'connected': isConnected }"></div>
        <span>{{ isConnected ? '已连接' : '未连接' }}</span>
      </div>
      
      <div class="error" v-if="lastError">
        <p>错误: {{ lastError }}</p>
      </div>
      
      <div class="user-info">
        <p>用户ID: {{ userId || '未获取' }}</p>
      </div>
      
      <div class="actions">
        <button @click="connect" :disabled="isConnected">连接</button>
        <button @click="disconnect" :disabled="!isConnected">断开</button>
        <button @click="sendTestMessage" :disabled="!isConnected">发送测试消息</button>
      </div>
    </div>
    
    <div class="log-card">
      <h3>日志 <button class="clear-btn" @click="clearLogs">清空</button></h3>
      <div class="logs">
        <div v-for="(log, index) in logs" :key="index" class="log-entry" :class="log.type">
          <span class="log-time">{{ formatTime(log.time) }}</span>
          <span class="log-type">[{{ log.type }}]</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
        <div v-if="logs.length === 0" class="empty-log">
          暂无日志
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import webSocketService from '../utils/websocket';
import { useAuthStore } from '../store/auth';

const logs = ref([]);
const authStore = useAuthStore();
const userId = ref(null);

// 从webSocketService读取状态
const isConnected = computed(() => webSocketService.isConnected.value);
const lastError = computed(() => webSocketService.lastError.value);

onMounted(() => {
  addLog('INFO', 'WebSocket调试页面已加载');
  
  // 从Auth Store获取用户ID
  if (authStore.isAuthenticated && authStore.user?.userId) {
    userId.value = authStore.user.userId;
    addLog('INFO', `已从Auth Store获取用户ID: ${userId.value}`);
  } else {
    addLog('WARN', '未获取到用户ID，请确保已登录');
  }
  
  // 添加WebSocket事件监听器
  webSocketService.addConnectionListener(handleConnectionChange);
  webSocketService.addMessageListener(handleMessage);
  
  // 检查连接状态
  if (isConnected.value) {
    addLog('INFO', '已存在活跃的WebSocket连接');
  }
});

onBeforeUnmount(() => {
  // 移除事件监听器
  // 注意：此处应该有移除监听器的方法，但当前实现可能没有提供
});

// 连接WebSocket
const connect = () => {
  if (!userId.value) {
    addLog('ERROR', '无法连接: 缺少用户ID');
    return;
  }
  
  try {
    addLog('INFO', `正在连接WebSocket，用户ID: ${userId.value}...`);
    webSocketService.connect(userId.value);
  } catch (error) {
    addLog('ERROR', `连接失败: ${error}`);
  }
};

// 断开WebSocket连接
const disconnect = () => {
  addLog('INFO', '正在断开WebSocket连接...');
  webSocketService.disconnect();
};

// 发送测试消息
const sendTestMessage = () => {
  if (!isConnected.value) {
    addLog('ERROR', '无法发送消息: WebSocket未连接');
    return;
  }
  
  const testMsg = { 
    type: 'TEST',
    content: '这是一条测试消息',
    timestamp: Date.now()
  };
  
  try {
    webSocketService.sendMessage(testMsg);
    addLog('INFO', `已发送测试消息: ${JSON.stringify(testMsg)}`);
  } catch (error) {
    addLog('ERROR', `发送消息失败: ${error}`);
  }
};

// 处理连接状态变化
const handleConnectionChange = (connected) => {
  addLog(connected ? 'SUCCESS' : 'WARN', 
         connected ? 'WebSocket已连接' : 'WebSocket已断开');
};

// 处理接收到的消息
const handleMessage = (data) => {
  addLog('RECEIVED', `收到消息: ${JSON.stringify(data)}`);
};

// 添加日志
const addLog = (type, message) => {
  logs.value.push({
    type,
    message,
    time: new Date()
  });
  
  // 限制日志数量，防止内存泄漏
  if (logs.value.length > 100) {
    logs.value = logs.value.slice(-100);
  }
};

// 清空日志
const clearLogs = () => {
  logs.value = [];
};

// 格式化时间
const formatTime = (date) => {
  return date.toLocaleTimeString();
};
</script>

<style scoped>
.simple-websocket-debug {
  max-width: 800px;
  margin: 20px auto;
  padding: 15px;
  font-family: Arial, sans-serif;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.status-card, .log-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
  margin-bottom: 20px;
}

h3 {
  margin-top: 0;
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #f56c6c;
  margin-right: 8px;
}

.status-indicator.connected {
  background-color: #67c23a;
}

.error {
  color: #f56c6c;
  margin-bottom: 15px;
}

.user-info {
  margin-bottom: 15px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

button {
  padding: 8px 15px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

button:hover:not(:disabled) {
  background-color: #ecf5ff;
  color: #409eff;
  border-color: #c6e2ff;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.clear-btn {
  padding: 2px 8px;
  font-size: 12px;
}

.logs {
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background-color: #f5f7fa;
}

.log-entry {
  padding: 6px 8px;
  margin-bottom: 6px;
  border-radius: 4px;
  font-size: 14px;
  border-left: 3px solid #dcdfe6;
  line-height: 1.4;
}

.log-entry.INFO {
  background-color: #f0f9eb;
  border-left-color: #67c23a;
}

.log-entry.WARN {
  background-color: #fdf6ec;
  border-left-color: #e6a23c;
}

.log-entry.ERROR {
  background-color: #fef0f0;
  border-left-color: #f56c6c;
}

.log-entry.SUCCESS {
  background-color: #f0f9eb;
  border-left-color: #67c23a;
}

.log-entry.RECEIVED {
  background-color: #ecf5ff;
  border-left-color: #409eff;
}

.log-time {
  color: #909399;
  margin-right: 8px;
  font-size: 12px;
}

.log-type {
  font-weight: bold;
  margin-right: 8px;
}

.empty-log {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

/* 暗色模式适配 */
[data-theme="dark"] .status-card,
[data-theme="dark"] .log-card {
  background-color: #1a1a1a;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .logs {
  background-color: #1a1a1a;
  border-color: #2c2c2c;
}

[data-theme="dark"] button {
  background-color: #1a1a1a;
  border-color: #4c4c4c;
  color: #dcdfe6;
}

[data-theme="dark"] button:hover:not(:disabled) {
  background-color: #18222c;
  color: #409eff;
  border-color: #18222c;
}

[data-theme="dark"] .log-entry.INFO {
  background-color: rgba(103, 194, 58, 0.1);
}

[data-theme="dark"] .log-entry.WARN {
  background-color: rgba(230, 162, 60, 0.1);
}

[data-theme="dark"] .log-entry.ERROR {
  background-color: rgba(245, 108, 108, 0.1);
}

[data-theme="dark"] .log-entry.SUCCESS {
  background-color: rgba(103, 194, 58, 0.1);
}

[data-theme="dark"] .log-entry.RECEIVED {
  background-color: rgba(64, 158, 255, 0.1);
}
</style>
