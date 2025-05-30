/**
 * 私信WebSocket增强器
 * 提供便捷的私信WebSocket功能集成
 */

import { ref, computed } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import webSocketService from './websocket';
import { useAuthStore } from '@/store/auth';

export interface MessageWebSocketConfig {
  autoConnect?: boolean;
  showNotifications?: boolean;
  enableSound?: boolean;
  debugMode?: boolean;
}

export interface PrivateMessageData {
  type: string;
  messageId: number;
  senderId: number;
  senderName: string;
  senderAvatar?: string;
  content: string;
  messageType?: string;
  imageUrl?: string;
  timestamp: number;
}

class MessageWebSocketEnhancer {
  private authStore = useAuthStore();
  private config: MessageWebSocketConfig;
  private messageHandlers: Array<(data: PrivateMessageData) => void> = [];
  private connectionHandlers: Array<(connected: boolean) => void> = [];
  
  public isConnected = computed(() => webSocketService.isConnected.value);
  public connectionStatus = computed(() => webSocketService.connectionStatus.value);
  public lastError = computed(() => webSocketService.lastError.value);

  constructor(config: MessageWebSocketConfig = {}) {
    this.config = {
      autoConnect: true,
      showNotifications: true,
      enableSound: false,
      debugMode: false,
      ...config
    };

    this.init();
  }

  /**
   * 初始化WebSocket增强器
   */
  private init() {
    // 自动连接
    if (this.config.autoConnect && this.authStore.isAuthenticated) {
      this.connect();
    }

    // 监听认证状态变化
    this.authStore.$subscribe((_, state) => {
      if (state.isAuthenticated && this.config.autoConnect) {
        this.connect();
      } else if (!state.isAuthenticated) {
        this.disconnect();
      }
    });

    // 设置消息监听器
    webSocketService.addMessageListener(this.handleWebSocketMessage.bind(this));
    webSocketService.addConnectionListener(this.handleConnectionChange.bind(this));
  }

  /**
   * 连接WebSocket
   */
  public connect(): boolean {
    if (!this.authStore.isAuthenticated || !this.authStore.user?.userId) {
      if (this.config.debugMode) {
        console.warn('MessageWebSocket: 用户未认证，无法连接');
      }
      return false;
    }

    try {
      webSocketService.connect(this.authStore.user.userId);
      if (this.config.debugMode) {
        console.log('MessageWebSocket: 连接请求已发送');
      }
      return true;
    } catch (error) {
      if (this.config.debugMode) {
        console.error('MessageWebSocket: 连接失败', error);
      }
      return false;
    }
  }

  /**
   * 断开WebSocket连接
   */
  public disconnect(): void {
    webSocketService.disconnect();
    if (this.config.debugMode) {
      console.log('MessageWebSocket: 已断开连接');
    }
  }

  /**
   * 重新连接WebSocket
   */
  public reconnect(): boolean {
    this.disconnect();
    return this.connect();
  }

  /**
   * 发送私信
   */
  public sendPrivateMessage(recipientId: number, content: string, messageType: string = 'TEXT'): void {
    if (!this.isConnected.value) {
      ElMessage.warning('WebSocket未连接，无法发送消息');
      return;
    }

    const message = {
      type: 'PRIVATE_MESSAGE',
      recipientId,
      content,
      messageType,
      timestamp: Date.now()
    };

    try {
      webSocketService.sendMessage(message);
      if (this.config.debugMode) {
        console.log('MessageWebSocket: 私信已发送', message);
      }
    } catch (error) {
      console.error('MessageWebSocket: 发送私信失败', error);
      // 移除 ElMessage.error，避免重复弹窗
    }
  }

  /**
   * 添加私信处理器
   */
  public addMessageHandler(handler: (data: PrivateMessageData) => void): void {
    this.messageHandlers.push(handler);
  }

  /**
   * 移除私信处理器
   */
  public removeMessageHandler(handler: (data: PrivateMessageData) => void): void {
    const index = this.messageHandlers.indexOf(handler);
    if (index !== -1) {
      this.messageHandlers.splice(index, 1);
    }
  }

  /**
   * 添加连接状态处理器
   */
  public addConnectionHandler(handler: (connected: boolean) => void): void {
    this.connectionHandlers.push(handler);
  }

  /**
   * 移除连接状态处理器
   */
  public removeConnectionHandler(handler: (connected: boolean) => void): void {
    const index = this.connectionHandlers.indexOf(handler);
    if (index !== -1) {
      this.connectionHandlers.splice(index, 1);
    }
  }

  /**
   * 处理WebSocket消息
   */
  private handleWebSocketMessage(data: any): void {
    if (!data || data.type !== 'PRIVATE_MESSAGE') {
      return;
    }

    if (this.config.debugMode) {
      console.log('MessageWebSocket: 收到私信', data);
    }

    // 通知所有消息处理器
    this.messageHandlers.forEach(handler => {
      try {
        handler(data as PrivateMessageData);
      } catch (error) {
        console.error('MessageWebSocket: 消息处理器执行失败', error);
      }
    });

    // 显示通知（如果启用）
    if (this.config.showNotifications) {
      this.showMessageNotification(data as PrivateMessageData);
    }
  }

  /**
   * 处理连接状态变化
   */
  private handleConnectionChange(connected: boolean): void {
    if (this.config.debugMode) {
      console.log('MessageWebSocket: 连接状态变化', connected);
    }

    // 通知所有连接状态处理器
    this.connectionHandlers.forEach(handler => {
      try {
        handler(connected);
      } catch (error) {
        console.error('MessageWebSocket: 连接状态处理器执行失败', error);
      }
    });

    // 显示连接状态提示
    if (connected) {
      if (this.config.debugMode) {
        ElMessage.success('私信服务已连接');
      }
    } else {
      if (this.config.debugMode) {
        ElMessage.warning('私信服务已断开');
      }
    }
  }

  /**
   * 显示消息通知
   */
  private showMessageNotification(data: PrivateMessageData): void {
    // 获取当前用户ID
    const authStore = useAuthStore();
    const currentUserId = authStore.user?.userId;
    
    // 如果消息是当前用户自己发送的，不显示通知
    if (data.senderId && currentUserId && Number(data.senderId) === Number(currentUserId)) {
      // 是自己发送的消息，不显示通知
      return;
    }
    
    const title = data.senderName || '新消息';
    let content = data.content;

    // 根据消息类型调整显示内容
    if (data.messageType === 'IMAGE') {
      content = '[图片]';
    } else if (data.messageType === 'EMOJI') {
      content = data.content;
    }

    ElNotification({
      title,
      message: content,
      type: 'info',
      duration: 5000,
      onClick: () => {
        // 点击通知跳转到消息页面
        window.location.href = `#/messages/${data.senderId}`;
      }
    });
  }

  /**
   * 获取连接状态文本
   */
  public getConnectionStatusText(): string {
    if (this.isConnected.value) {
      return '已连接';
    } else if (this.connectionStatus.value.includes('正在连接')) {
      return '连接中';
    } else if (this.connectionStatus.value.includes('重连')) {
      return '重连中';
    } else {
      return '未连接';
    }
  }

  /**
   * 获取连接状态类型（用于UI样式）
   */
  public getConnectionStatusType(): 'success' | 'warning' | 'danger' | 'info' {
    if (this.isConnected.value) {
      return 'success';
    } else if (this.connectionStatus.value.includes('正在连接') || this.connectionStatus.value.includes('重连')) {
      return 'warning';
    } else if (this.lastError.value) {
      return 'danger';
    } else {
      return 'info';
    }
  }

  /**
   * 检查连接健康状况
   */
  public checkConnectionHealth(): Promise<boolean> {
    return new Promise((resolve) => {
      if (!this.isConnected.value) {
        resolve(false);
        return;
      }

      // 发送心跳检测
      const pingMessage = { type: 'PING', timestamp: Date.now() };
      
      try {
        webSocketService.sendMessage(pingMessage);
        
        // 等待一段时间检查是否有响应
        setTimeout(() => {
          resolve(this.isConnected.value);
        }, 3000);
      } catch (error) {
        resolve(false);
      }
    });
  }
}

// 创建全局实例
export const messageWebSocketEnhancer = new MessageWebSocketEnhancer({
  autoConnect: true,
  showNotifications: true,
  enableSound: false,
  debugMode: false
});

export default messageWebSocketEnhancer;
