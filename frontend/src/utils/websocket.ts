import { ref } from 'vue';
import axios from 'axios';

class WebSocketService {
  private socket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectTimeout: any = null;
  private pingInterval: any = null;
  private userId: number | null = null;
  private messageListeners: ((data: any) => void)[] = [];
  private notificationListeners: ((data: any) => void)[] = [];
  private connectionListeners: ((status: boolean) => void)[] = [];
  
  public isConnected = ref(false);
  public lastError = ref<string | null>(null);

  // 初始化WebSocket连接
  public connect(userId: number): void {
    if (this.socket) {
      this.disconnect();
    }

    this.userId = userId;
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsHost = window.location.host;
    // 使用与request.ts中相同的baseURL策略，确保与API路径一致
    const baseUrl = axios?.defaults?.baseURL || `${window.location.protocol}//${window.location.host}`;
    // 从baseUrl提取主机名，确保WebSocket连接到正确的后端
    const urlObj = new URL(baseUrl);
    const wsUrl = `${wsProtocol}//${urlObj.host}/ws/${userId}`;

    try {
      this.socket = new WebSocket(wsUrl);
      
      this.socket.onopen = this.handleOpen.bind(this);
      this.socket.onmessage = this.handleMessage.bind(this);
      this.socket.onclose = this.handleClose.bind(this);
      this.socket.onerror = this.handleError.bind(this);
      
      console.log(`WebSocket connecting to ${wsUrl}`);
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
  }

  // 尝试重新连接
  private attemptReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached');
      this.lastError.value = '达到最大重连次数，请刷新页面重试';
      return;
    }

    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
    }

    // 使用指数退避策略，但最长不超过30秒
    const delay = Math.min(30000, 1000 * Math.pow(1.5, this.reconnectAttempts));
    console.log(`Attempting to reconnect in ${delay}ms... (Attempt ${this.reconnectAttempts + 1}/${this.maxReconnectAttempts})`);
    
    this.reconnectTimeout = setTimeout(() => {
      if (this.userId) {
        this.reconnectAttempts++;
        
        // 在重连前检查网络状态
        if (navigator.onLine) {
          console.log(`执行第 ${this.reconnectAttempts} 次重连尝试...`);
          this.connect(this.userId);
        } else {
          console.log('网络离线，等待网络恢复后重连...');
          this.lastError.value = '网络已断开，等待网络恢复...';
          
          // 添加网络恢复事件监听
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
      
      // 设置心跳超时检测
      setTimeout(() => {
        // 如果心跳在10秒内没有响应，认为连接可能已断开，尝试重连
        if (this.isConnected.value && !this.reconnectTimeout) {
          console.warn('心跳超时，尝试重新连接...');
          this.disconnect();
          if (this.userId) {
            setTimeout(() => {
              if (this.userId) {
                this.connect(this.userId);
              }
            }, 5000); // 延迟5秒后重连
          }
        }
      }, 10000); // 增加到10秒超时
    } else {
      console.warn('WebSocket不处于开启状态，无法发送心跳');
      // 如果连接不是开启状态，尝试重连，但增加延迟
      if (this.userId && !this.reconnectTimeout && !this.isConnected.value) {
        setTimeout(() => {
          this.attemptReconnect();
        }, 3000); // 延迟3秒后尝试重连
      }
    }
  }

  // 连接打开处理
  private handleOpen(event: Event): void {
    console.log('WebSocket connected');
    this.isConnected.value = true;
    this.lastError.value = null;
    this.reconnectAttempts = 0;
    
    // 设置心跳检测，增加到60秒一次
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
    }
    this.pingInterval = setInterval(() => this.sendPing(), 60000); // 60秒
    
    // 通知连接状态监听器
    this.connectionListeners.forEach(listener => listener(true));
  }

  // 接收消息处理
  private handleMessage(event: MessageEvent): void {
    try {
      // 解析WebSocket消息
      const data = JSON.parse(event.data);
      console.log('WebSocket message received:', data);
      
      switch (data.type) {
        case 'NOTIFICATION':
          this.notificationListeners.forEach(listener => listener(data));
          break;
        case 'PRIVATE_MESSAGE':
          this.messageListeners.forEach(listener => listener(data));
          break;
        case 'PONG':
          // 心跳响应
          console.log('Received PONG from server');
          break;
        case 'CONNECTION':
          // 连接状态消息
          console.log('Received connection state update:', data);
          break;
        default:
          // 处理其他类型消息
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
    
    // 通知连接状态监听器
    this.connectionListeners.forEach(listener => listener(false));
    
    // 检查关闭原因
    if (event.code === 1006) {
      console.warn('WebSocket异常关闭（代码1006），可能是网络波动或服务器重启，尝试重新连接...');
      this.lastError.value = '连接意外断开，正在尝试重新连接...';
      // 对于1006错误，立即尝试重连，不增加重连尝试次数
      if (this.userId) {
        // 使用较短的延迟立即尝试重连
        setTimeout(() => {
          if (this.userId) {
            this.connect(this.userId);
          }
        }, 3000); // 增加延迟到3秒，减少频繁重连
        return;
      }
    } else if (event.code === 1000) {
      // 这是正常关闭，如果不是由disconnect方法触发的，可能是服务器主动关闭
      // 在这种情况下，可以尝试延迟较长时间后重连
      console.log('WebSocket正常关闭（代码1000），等待一段时间后尝试重新连接...');
      if (this.userId && !this.reconnectTimeout) {
        setTimeout(() => {
          if (this.userId) {
            this.connect(this.userId);
          }
        }, 10000); // 10秒后尝试重连
        return;
      }
    }
    
    // 尝试重新连接
    this.attemptReconnect();
  }

  // 连接错误处理
  private handleError(event: Event): void {
    console.error('WebSocket error:', event);
    this.lastError.value = '连接出错';
    
    // 记录详细错误信息以便调试
    if (event instanceof ErrorEvent && event.error) {
      console.error('WebSocket错误详情:', event.error);
    }
    
    // 尝试重连，但不立即重连，等待下一个重连周期
    if (!this.reconnectTimeout && this.userId) {
      this.attemptReconnect();
    }
  }
}

// 创建单例
const webSocketService = new WebSocketService();
export default webSocketService;
