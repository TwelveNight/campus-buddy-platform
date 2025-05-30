import { ref } from 'vue';
import axios from 'axios';

class WebSocketService {
  private socket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10; // 增加最大重连次数
  private reconnectTimeout: any = null;
  private pingInterval: any = null;
  private userId: number | null = null;
  private messageListeners: ((data: any) => void)[] = [];
  private notificationListeners: ((data: any) => void)[] = [];
  private connectionListeners: ((status: boolean) => void)[] = [];
  // 用于跟踪已处理的消息，防止重复处理
  private processedMessages: Set<string> = new Set();
  
  // 使用Vue的响应式系统跟踪状态
  public isConnected = ref(false);
  public lastError = ref<string | null>(null);
  public connectionStatus = ref<string>("未连接");

  constructor() {
    // 确保在初始化时响应式属性已设置
    console.log('WebSocket服务初始化，isConnected值:', this.isConnected.value);
  }

  // 初始化WebSocket连接
  public connect(userId: number): void {
    if (this.socket) {
      this.disconnect();
    }

    this.userId = userId;
    
    // 确定WebSocket协议（基于当前页面协议）
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    
    // 从axios baseURL获取后端服务器地址
    let serverHost;
    if (axios?.defaults?.baseURL) {
      try {
        // 尝试解析axios配置的baseURL
        const urlObj = new URL(axios.defaults.baseURL);
        serverHost = urlObj.host;
      } catch (e) {
        console.warn('解析baseURL失败，使用当前域名', e);
        serverHost = window.location.host;
      }
    } else {
      // 回退到当前域名
      serverHost = window.location.host;
    }
    
    // 构建WebSocket URL
    // 注意：确保路径是 /ws/ 并避免使用字符串连接
    const wsUrl = `${wsProtocol}//${serverHost}/ws/${userId}`;
    
    console.log('尝试连接WebSocket: ' + wsUrl);
    this.connectionStatus.value = "正在连接";
    
    try {
      // 创建WebSocket连接
      this.socket = new WebSocket(wsUrl);
      
      // 绑定事件处理器
      this.socket.onopen = this.handleOpen.bind(this);
      this.socket.onmessage = this.handleMessage.bind(this);
      this.socket.onclose = this.handleClose.bind(this);
      this.socket.onerror = this.handleError.bind(this);
      
      // 添加超时处理，解决某些浏览器可能不触发error事件的问题
      setTimeout(() => {
        if (this.socket && this.socket.readyState !== WebSocket.OPEN) {
          console.warn('WebSocket连接超时');
          this.lastError.value = '连接超时';
          this.attemptReconnect();
        }
      }, 10000); // 10秒超时
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

    // 使用指数退避策略，但最长不超过30秒
    const delay = Math.min(30000, 1000 * Math.pow(1.5, this.reconnectAttempts));
    console.log('Attempting to reconnect in ' + delay + 'ms... (Attempt ' + (this.reconnectAttempts + 1) + '/' + this.maxReconnectAttempts + ')');
    this.connectionStatus.value = '正在重连 (' + (this.reconnectAttempts + 1) + '/' + this.maxReconnectAttempts + ')';
    
    this.reconnectTimeout = setTimeout(() => {
      if (this.userId) {
        this.reconnectAttempts++;
        
        // 在重连前检查网络状态
        if (navigator.onLine) {
          console.log('执行第 ' + this.reconnectAttempts + ' 次重连尝试...');
          this.connect(this.userId);
        } else {
          console.log('网络离线，等待网络恢复后重连...');
          this.lastError.value = '网络已断开，等待网络恢复...';
          this.connectionStatus.value = "网络离线";
          
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
  private handleOpen(_event: Event): void {
    console.log('WebSocket connected');
    this.isConnected.value = true;
    this.lastError.value = null;
    this.reconnectAttempts = 0;
    this.connectionStatus.value = "已连接";
    
    // 发送立即心跳以同步时间
    this.sendPing();
    
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
      
      // 生成消息标识符用于去重
      const messageId = data.type + '_' + data.timestamp + '_' + (data.messageId || '') + '_' + (data.senderId || '');
      
      // 检查消息是否已经处理过，如果处理过则跳过
      if (this.processedMessages.has(messageId)) {
        console.log('忽略重复消息:', messageId);
        return;
      }
      
      // 记录此消息已处理
      this.processedMessages.add(messageId);
      
      // 保持处理过的消息集合在合理大小
      if (this.processedMessages.size > 100) {
        // 只保留最近的50条消息记录
        this.processedMessages = new Set(Array.from(this.processedMessages).slice(-50));
      }
      
      switch (data.type) {
        case 'NOTIFICATION':
          this.notificationListeners.forEach(listener => listener(data));
          break;
        case 'PRIVATE_MESSAGE':
          // 确保消息包含所需字段
          if (!data.senderId || !data.senderName || !data.content) {
            console.warn('接收到的私信数据格式不完整:', data);
          }
          this.messageListeners.forEach(listener => listener(data));
          break;
        case 'PONG':
          // 心跳响应
          console.log('收到PONG响应，服务器时间:', data.timestamp);
          
          // 通知所有监听器连接状态，并附带服务器时间
          this.connectionListeners.forEach(listener => {
            try {
              // 调用普通连接状态监听器
              listener(true);
            } catch (e) {
              console.error('调用连接状态监听器失败:', e);
            }
          });
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
    this.connectionStatus.value = "已断开";
    
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
    console.error('WebSocket错误:', event);
    this.lastError.value = '连接出错';
    this.isConnected.value = false;
    this.connectionStatus.value = "连接出错";
    
    // 记录详细错误信息以便调试
    if (event instanceof ErrorEvent && event.error) {
      console.error('WebSocket错误详情:', event.error);
    }
    
    // 尝试重连，但不立即重连，等待下一个重连周期
    if (!this.reconnectTimeout && this.userId) {
      this.attemptReconnect();
    }
    
    // 通知连接状态监听器
    this.connectionListeners.forEach(listener => listener(false));
  }
}

// 创建单例
const webSocketService = new WebSocketService();
export default webSocketService;
