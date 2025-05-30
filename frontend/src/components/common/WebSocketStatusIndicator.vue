<template>
  <div class="websocket-status-indicator">
    <el-tooltip :content="tooltipContent" placement="top">
      <div class="status-wrapper" @click="handleClick">
        <div class="status-dot" :class="statusClass" :style="statusStyle"></div>
        <span v-if="showText" class="status-text">{{ statusText }}</span>
        <el-icon v-if="showRefreshButton" class="refresh-icon" @click.stop="handleRefresh">
          <Refresh />
        </el-icon>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import messageWebSocketEnhancer from '@/utils/messageWebSocketEnhancer';

interface Props {
  showText?: boolean;
  showRefreshButton?: boolean;
  size?: 'small' | 'medium' | 'large';
  clickable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showText: true,
  showRefreshButton: true,
  size: 'medium',
  clickable: true
});

const emit = defineEmits<{
  click: [];
  refresh: [];
}>();

// 连接状态相关计算属性
const isConnected = computed(() => messageWebSocketEnhancer.isConnected.value);
const connectionStatus = computed(() => messageWebSocketEnhancer.connectionStatus.value);
const lastError = computed(() => messageWebSocketEnhancer.lastError.value);

const statusText = computed(() => messageWebSocketEnhancer.getConnectionStatusText());
const statusType = computed(() => messageWebSocketEnhancer.getConnectionStatusType());

const statusClass = computed(() => ({
  'status-connected': statusType.value === 'success',
  'status-connecting': statusType.value === 'warning',
  'status-error': statusType.value === 'danger',
  'status-disconnected': statusType.value === 'info',
  [`status-${props.size}`]: true,
  'clickable': props.clickable
}));

const statusStyle = computed(() => {
  const colors = {
    success: '#67c23a',
    warning: '#e6a23c',
    danger: '#f56c6c',
    info: '#909399'
  };
  
  return {
    backgroundColor: colors[statusType.value],
    boxShadow: statusType.value === 'warning' ? `0 0 8px ${colors[statusType.value]}` : 'none'
  };
});

const tooltipContent = computed(() => {
  let content = `连接状态: ${statusText.value}`;
  if (lastError.value) {
    content += `\n错误: ${lastError.value}`;
  }
  if (connectionStatus.value !== statusText.value) {
    content += `\n详情: ${connectionStatus.value}`;
  }
  return content;
});

// 事件处理
const handleClick = () => {
  if (!props.clickable) return;
  
  emit('click');
  
  // 如果未连接，尝试重新连接
  if (!isConnected.value) {
    handleRefresh();
  }
};

const handleRefresh = async () => {
  emit('refresh');
  
  try {
    ElMessage.info('正在重新连接...');
    const success = messageWebSocketEnhancer.reconnect();
    
    if (success) {
      // 检查连接健康状况
      const isHealthy = await messageWebSocketEnhancer.checkConnectionHealth();
      if (isHealthy) {
        ElMessage.success('连接成功');
      } else {
        ElMessage.warning('连接可能不稳定');
      }
    } else {
      ElMessage.error('连接失败');
    }
  } catch (error) {
    ElMessage.error('重连失败');
    console.error('WebSocket重连失败:', error);
  }
};
</script>

<style scoped>
.websocket-status-indicator {
  display: inline-flex;
  align-items: center;
}

.status-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.status-wrapper.clickable {
  cursor: pointer;
}

.status-wrapper:hover.clickable {
  background-color: rgba(0, 0, 0, 0.05);
}

.status-dot {
  border-radius: 50%;
  transition: all 0.3s ease;
  position: relative;
}

.status-dot.status-small {
  width: 8px;
  height: 8px;
}

.status-dot.status-medium {
  width: 10px;
  height: 10px;
}

.status-dot.status-large {
  width: 12px;
  height: 12px;
}

.status-dot.status-connecting {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.status-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.refresh-icon {
  font-size: 14px;
  color: #909399;
  cursor: pointer;
  transition: color 0.3s ease;
}

.refresh-icon:hover {
  color: #409eff;
}

/* 暗色模式适配 */
[data-theme="dark"] .status-wrapper:hover.clickable {
  background-color: rgba(255, 255, 255, 0.05);
}

[data-theme="dark"] .status-text {
  color: #a3a6ad;
}

[data-theme="dark"] .refresh-icon {
  color: #a3a6ad;
}

[data-theme="dark"] .refresh-icon:hover {
  color: #409eff;
}
</style>
