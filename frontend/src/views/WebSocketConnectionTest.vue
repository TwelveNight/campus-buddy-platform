<template>
  <div class="websocket-test-container">
    <h1>WebSocket 连接测试</h1>
    
    <div class="connection-status">
      <h2>连接状态</h2>
      <div class="status-indicator" :class="connectionStatusClass">
        {{ connectionStatus }}
      </div>
      <p class="error-message" v-if="lastError">错误: {{ lastError }}</p>
    </div>
    
    <div class="test-actions">
      <el-button type="primary" @click="testEchoConnection" :loading="echoTesting">
        测试 Echo 连接
      </el-button>
      <el-button type="success" @click="testUserConnection" :loading="userTesting" :disabled="!userId">
        测试用户连接 (ID: {{ userId || '未登录' }})
      </el-button>
      <el-button type="danger" @click="disconnect">
        断开连接
      </el-button>
    </div>
    
    <div class="connection-details" v-if="isConnected">
      <h3>连接详情</h3>
      <p>连接类型: {{ connectionType }}</p>
      <p>连接时间: {{ connectionTime }}</p>
      <p>最后心跳: {{ lastPingTime }}</p>
    </div>
    
    <div class="test-result">
      <h3>测试结果</h3>
      <div v-if="echoTestResult !== null">
        <p>Echo 测试: <span :class="{ 'success': echoTestResult, 'error': !echoTestResult }">
          {{ echoTestResult ? '成功' : '失败' }}
        </span></p>
      </div>
      <div v-if="userTestResult !== null">
        <p>用户连接测试: <span :class="{ 'success': userTestResult, 'error': !userTestResult }">
          {{ userTestResult ? '成功' : '失败' }}
        </span></p>
      </div>
    </div>
    
    <div class="debug-info">
      <h3>调试信息</h3>
      <el-input
        type="textarea"
        :rows="10"
        placeholder="调试日志会显示在这里..."
        v-model="debugLog"
        readonly
      ></el-input>
      <el-button type="info" @click="clearLog" size="small" style="margin-top: 10px">
        清除日志
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import webSocketService from '@/utils/websocket';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const userId = computed(() => authStore.user?.userId || null);

// 测试状态
const echoTesting = ref(false);
const userTesting = ref(false);
const echoTestResult = ref<boolean | null>(null);
const userTestResult = ref<boolean | null>(null);

// 连接信息
const isConnected = computed(() => webSocketService.isConnected.value);
const connectionStatus = computed(() => webSocketService.connectionStatus.value);
const lastError = computed(() => webSocketService.lastError.value);
const connectionStatusClass = computed(() => {
  if (isConnected.value) return 'connected';
  if (connectionStatus.value.includes('正在连接') || connectionStatus.value.includes('重连')) return 'connecting';
  return 'disconnected';
});

// 连接详情
const connectionType = ref('标准WebSocket');
const connectionTime = ref('');
const lastPingTime = ref('');

// 调试日志
const debugLog = ref('');

// 记录调试信息
const logDebug = (message: string) => {
  const timestamp = new Date().toLocaleTimeString();
  debugLog.value += `[${timestamp}] ${message}\n`;
  // 自动滚动到底部
  const textarea = document.querySelector('.debug-info textarea');
  if (textarea) {
    setTimeout(() => {
      (textarea as HTMLTextAreaElement).scrollTop = (textarea as HTMLTextAreaElement).scrollHeight;
    }, 0);
  }
};

// 清除日志
const clearLog = () => {
  debugLog.value = '';
};

// 测试Echo连接
const testEchoConnection = async () => {
  echoTesting.value = true;
  logDebug('开始测试Echo连接...');
  
  try {
    echoTestResult.value = await webSocketService.testEchoConnection();
    logDebug(`Echo测试结果: ${echoTestResult.value ? '成功' : '失败'}`);
    
    if (echoTestResult.value) {
      ElMessage.success('Echo连接测试成功');
    } else {
      ElMessage.error('Echo连接测试失败');
    }
  } catch (error) {
    logDebug(`Echo测试异常: ${error}`);
    echoTestResult.value = false;
    ElMessage.error(`Echo连接测试异常: ${error}`);
  } finally {
    echoTesting.value = false;
  }
};

// 测试用户连接
const testUserConnection = async () => {
  if (!userId.value) {
    ElMessage.warning('请先登录后再测试用户连接');
    return;
  }
  
  userTesting.value = true;
  logDebug(`开始测试用户ID: ${userId.value} 的连接...`);
  
  // 记录原始连接状态变更处理器
  const originalHandler = (status: boolean) => {
    logDebug(`连接状态变更: ${status ? '已连接' : '已断开'}`);
    if (status) {
      connectionTime.value = new Date().toLocaleString();
      userTestResult.value = true;
      ElMessage.success('用户连接测试成功');
    } else {
      userTestResult.value = false;
    }
    userTesting.value = false;
  };
  
  // 添加临时处理器
  webSocketService.addConnectionListener(originalHandler);
  
  // 连接
  webSocketService.connect(userId.value);
  
  // 20秒后如果仍在测试中，则判定为失败
  setTimeout(() => {
    if (userTesting.value) {
      logDebug('用户连接测试超时');
      userTestResult.value = false;
      userTesting.value = false;
      ElMessage.error('用户连接测试超时');
    }
  }, 20000);
};

// 断开连接
const disconnect = () => {
  logDebug('手动断开WebSocket连接');
  webSocketService.disconnect();
  connectionTime.value = '';
  lastPingTime.value = '';
};

// 监听消息 - 用于记录
const handleMessage = (data: any) => {
  try {
    let message = '';
    if (typeof data === 'string') {
      message = data;
    } else {
      message = JSON.stringify(data);
    }
    logDebug(`收到消息: ${message}`);
  } catch (error) {
    logDebug(`处理消息时出错: ${error}`);
  }
};

// 页面加载时
onMounted(() => {
  // 添加消息监听
  webSocketService.addMessageListener(handleMessage);
  
  // 拦截控制台原始日志进行记录
  const originalConsoleLog = console.log;
  const originalConsoleError = console.error;
  const originalConsoleWarn = console.warn;
  
  console.log = function(...args) {
    // 只拦截WebSocket相关日志
    const message = args.join(' ');
    if (message.includes('WebSocket') || message.includes('ws://') || message.includes('wss://')) {
      logDebug(`[log] ${message}`);
    }
    originalConsoleLog.apply(console, args);
  };
  
  console.error = function(...args) {
    const message = args.join(' ');
    if (message.includes('WebSocket') || message.includes('ws://') || message.includes('wss://')) {
      logDebug(`[error] ${message}`);
    }
    originalConsoleError.apply(console, args);
  };
  
  console.warn = function(...args) {
    const message = args.join(' ');
    if (message.includes('WebSocket') || message.includes('ws://') || message.includes('wss://')) {
      logDebug(`[warn] ${message}`);
    }
    originalConsoleWarn.apply(console, args);
  };
  
  // 记录初始状态
  logDebug('WebSocket测试页面已加载');
  logDebug(`当前连接状态: ${connectionStatus.value}`);
  logDebug(`用户ID: ${userId.value || '未登录'}`);
});

// 页面卸载前
onBeforeUnmount(() => {
  // 断开连接
  if (isConnected.value) {
    webSocketService.disconnect();
  }
});
</script>

<style scoped>
.websocket-test-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.connection-status {
  margin: 20px 0;
  padding: 15px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.status-indicator {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: bold;
  margin: 10px 0;
}

.connected {
  background-color: #67c23a;
  color: white;
}

.connecting {
  background-color: #e6a23c;
  color: white;
}

.disconnected {
  background-color: #f56c6c;
  color: white;
}

.error-message {
  color: #f56c6c;
  margin-top: 10px;
}

.test-actions {
  margin: 20px 0;
  display: flex;
  gap: 10px;
}

.connection-details {
  margin: 20px 0;
  padding: 15px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.test-result {
  margin: 20px 0;
}

.success {
  color: #67c23a;
  font-weight: bold;
}

.error {
  color: #f56c6c;
  font-weight: bold;
}

.debug-info {
  margin: 20px 0;
}
</style>
