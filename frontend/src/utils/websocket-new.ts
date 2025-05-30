import { ref } from 'vue';
import axios from 'axios';

class WebSocketService {
  private socket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10;
  private reconnectTimeout: any = null;
  private pingInterval: any = null;
  private userId: number | null = null;
  private messageListeners: ((data: any) => void)[] = [];
  private notificationListeners: ((data: any) => void)[] = [];
  private connectionListeners: ((status: boolean) => void)[] = [];
  private processedMessages: Set<string> = new Set();
  
  public isConnected = ref(false);
  public lastError = ref<string | null>(null);
  public connectionStatus = ref<string>("未连接");

  // 初始化WebSocket连接
  public connect(userId: number): void {
    if (this.socket) {
      this.disconnect();
    }

    this.userId = userId;
    
    // 从axios baseURL获取后端服务器地址
    let serverHost = window.location.host;
    
    // 构建WebSocket URL
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = wsProtocol + '//' + serverHost + '/ws/' + userId;
    
    console.log('尝试连接WebSocket: ' + wsUrl);
    this.connectionStatus.value = "正在连接";
    
    try {
      this.socket = new WebSocket(wsUrl);
      this.socket.onopen = this.handleOpen.bind(this);
      this.socket.onmessage = this.handleMessage.bind(this);
      this.socket.onclose = this.handleClose.bind(this);
      this.socket.onerror = this.handleError.bind(this);
      
      setTimeout(() => {
        if (this.socket && this.socket.readyState !== WebSocket.OPEN) {
          console.warn('WebSocket连接超时');
          this.lastError.value = '连接超时';
          this.attemptReconnect();
        }
      }, 10000);
    } catch (error) {
      console.error('WebSocket connection error:', error);
      this.lastError.value = '连接服务器失败';
      this.attemptReconnect();
    }
  }

  // 断开WebSocket连接
  public disconnect(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
      this.pingInterval = null;
    }
    
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    
    this.isConnected.value = false;
    this.userId = null;
    this.reconnectAttempts = 0;
    this.connectionStatus.value = "未连接";
  }

  // 尝试重新连接
  private attemptReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached');
      this.lastError.value = '达到最大重连次数，请刷新页面重试';
      this.connectionStatus.value = "重连失败";
      return;
    }

    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
    }

    const delay = Math.min(30000, 1000 * Math.pow(1.5, this.reconnectAttempts));
    console.log('Attempting to reconnect in ' + delay + 'ms... (Attempt ' + (this.reconnectAttempts + 1) + '/' + this.maxReconnectAttempts + ')');
    this.connectionStatus.value = '正在重连 (' + (this.reconnectAttempts + 1) + '/' + this.maxReconnectAttempts + ')';
    
    this.reconnectTimeout = setTimeout(() => {
      if (this.userId) {
        this.reconnectAttempts++;
        
        if (navigator.onLine) {
          console.log('执行第 ' + this.reconnectAttempts + ' 次重连尝试...');
          this.connect(this.userId);
        } else {
          console.log('网络离线，等待网络恢复后重连...');
          this.lastError.value = '网络已断开，等待网络恢复...';
          this.connectionStatus.value = "网络离线";
          
          const onlineHandler = () => {
            console.log('网络已恢复，尝试重新连接...');
            if (this.userId) {
              this.connect(this.userId);
            }
            window.removeEventListener('online', onlineHandler);
          };
          
          window.addEventListener('online', onlineHandler);
        }
      }
    }, delay);
  }

  // 添加消息监听器
  public addMessageListener(callback: (data: any) => void): void {
    this.messageListeners.push(callback);
  }

  // 添加通知监听器
  public addNotificationListener(callback: (data: any) => void): void {
    this.notificationListeners.push(callback);
  }

  // 添加连接状态监听器
  public addConnectionListener(callback: (status: boolean) => void): void {
    this.connectionListeners.push(callback);
  }

  // 发送消息
  public sendMessage(message: any): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      console.error('WebSocket is not connected');
      return;
    }

    this.socket.send(JSON.stringify(message));
  }

  // 发送心跳检测
  private sendPing(): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.sendMessage({ type: 'PING', timestamp: Date.now() });
      
      setTimeout(() => {
        if (this.isConnected.value && !this.reconnectTimeout) {
          console.warn('心跳超时，尝试重新连接...');
          this.disconnect();
          if (this.userId) {
            setTimeout(() => {
              if (this.userId) {
                this.connect(this.userId);
              }
            }, 5000);
          }
        }
      }, 10000);
    } else {
      console.warn('WebSocket不处于开启状态，无法发送心跳');
      if (this.userId && !this.reconnectTimeout && !this.isConnected.value) {
        setTimeout(() => {
          this.attemptReconnect();
        }, 3000);
      }
    }
  }

  // 连接打开处理
  private handleOpen(_event: Event): void {
    console.log('WebSocket connected');
    this.isConnected.value = true;
    this.lastError.value = null;
    this.reconnectAttempts = 0;
    this.connectionStatus.value = "已连接";
    
    this.sendPing();
    
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
    }
    this.pingInterval = setInterval(() => this.sendPing(), 60000);
    
    this.connectionListeners.forEach(listener => listener(true));
  }

  // 接收消息处理
  private handleMessage(event: MessageEvent): void {
    try {
      const data = JSON.parse(event.data);
      console.log('WebSocket message received:', data);
      
      const messageId = data.type + '_' + data.timestamp + '_' + (data.messageId || '') + '_' + (data.senderId || '');
      
      if (this.processedMessages.has(messageId)) {
        console.log('忽略重复消息:', messageId);
        return;
      }
      
      this.processedMessages.add(messageId);
      
      if (this.processedMessages.size > 100) {
        this.processedMessages = new Set(Array.from(this.processedMessages).slice(-50));
      }
      
      switch (data.type) {
        case 'NOTIFICATION':
          this.notificationListeners.forEach(listener => listener(data));
          break;
        case 'PRIVATE_MESSAGE':
          if (!data.senderId || !data.senderName || !data.content) {
            console.warn('接收到的私信数据格式不完整:', data);
          }
          this.messageListeners.forEach(listener => listener(data));
          break;
        case 'PONG':
          console.log('收到PONG响应，服务器时间:', data.timestamp);
          
          this.connectionListeners.forEach(listener => {
            try {
              listener(true);
            } catch (e) {
              console.error('调用连接状态监听器失败:', e);
            }
          });
          break;
        case 'CONNECTION':
          console.log('Received connection state update:', data);
          break;
        default:
          this.messageListeners.forEach(listener => listener(data));
      }
    } catch (error) {
      console.error('Error parsing WebSocket message:', error);
    }
  }

  // 连接关闭处理
  private handleClose(event: CloseEvent): void {
    console.log('WebSocket closed:', event);
    this.isConnected.value = false;
    this.connectionStatus.value = "已断开";
    
    this.connectionListeners.forEach(listener => listener(false));
    
    if (event.code === 1006) {
      console.warn('WebSocket异常关闭（代码1006），可能是网络波动或服务器重启，尝试重新连接...');
      this.lastError.value = '连接意外断开，正在尝试重新连接...';
      if (this.userId) {
        setTimeout(() => {
          if (this.userId) {
            this.connect(this.userId);
          }
        }, 3000);
        return;
      }
    } else if (event.code === 1000) {
      console.log('WebSocket正常关闭（代码1000），等待一段时间后尝试重新连接...');
      if (this.userId && !this.reconnectTimeout) {
        setTimeout(() => {
          if (this.userId) {
            this.connect(this.userId);
          }
        }, 10000);
        return;
      }
    }
    
    this.attemptReconnect();
  }

  // 连接错误处理
  private handleError(event: Event): void {
    console.error('WebSocket错误:', event);
    this.lastError.value = '连接出错';
    this.isConnected.value = false;
    this.connectionStatus.value = "连接出错";
    
    if (event instanceof ErrorEvent && event.error) {
      console.error('WebSocket错误详情:', event.error);
    }
    
    if (!this.reconnectTimeout && this.userId) {
      this.attemptReconnect();
    }
    
    this.connectionListeners.forEach(listener => listener(false));
  }
}

// 创建单例
const webSocketService = new WebSocketService();
export default webSocketService;
