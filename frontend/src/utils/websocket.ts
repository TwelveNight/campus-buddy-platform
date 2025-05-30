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
  
  // 心跳追踪
  private lastPingTime: number = 0;
  private lastPongTime: number = 0;
  private missedHeartbeats: number = 0;
  private maxMissedHeartbeats: number = 3;
  private lastReconnectTime: number = 0;
  
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
    // 如果已经存在连接，先断开
    if (this.socket) {
      console.log('已存在WebSocket连接，先断开旧连接');
      // 保存旧的连接状态为正在重连，避免显示断开的提示
      this.connectionStatus.value = "正在重连";
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
      
      // 添加连接超时检测，确保连接不会永久挂起
      const connectionTimeoutCheck = setTimeout(() => {
        if (this.socket && this.socket.readyState !== WebSocket.OPEN) {
          console.warn('WebSocket连接超时（15秒）');
          this.lastError.value = '连接超时，重新尝试...';
          
          // 如果连接未能在超时时间内完成，手动关闭它
          if (this.socket) {
            try {
              // 手动触发关闭事件，同时也会触发后续的重连逻辑
              this.socket.close();
            } catch (err) {
              console.error('关闭超时WebSocket连接时出错:', err);
            }
          }
        }
      }, 15000); // 15秒超时

      // 当连接成功或关闭时，清除超时检测
      const clearTimeoutCheck = () => {
        clearTimeout(connectionTimeoutCheck);
        this.socket?.removeEventListener('open', clearTimeoutCheck);
        this.socket?.removeEventListener('close', clearTimeoutCheck);
      };
      
      this.socket.addEventListener('open', clearTimeoutCheck);
      this.socket.addEventListener('close', clearTimeoutCheck);
      
    } catch (error) {
      console.error('WebSocket连接错误:', error);
      this.lastError.value = '连接服务器失败，稍后重试...';
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
      console.error('达到最大重连次数');
      this.lastError.value = '达到最大重连次数，请刷新页面重试';
      this.connectionStatus.value = "重连失败";
      return;
    }

    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
    }

    // 使用更保守的退避策略，避免快速消耗重连次数
    // 前3次快速尝试（3秒、5秒、8秒），之后逐渐增加
    let delay;
    if (this.reconnectAttempts < 3) {
      delay = (this.reconnectAttempts + 1) * 2000 + 1000; // 3秒、5秒、8秒
    } else {
      delay = Math.min(30000, 10000 + 2000 * (this.reconnectAttempts - 2)); // 逐步增加但不超过30秒
    }
    
    console.log('将在 ' + delay + 'ms 后重新连接... (尝试 ' + (this.reconnectAttempts + 1) + '/' + this.maxReconnectAttempts + ')');
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
  
  // 测试echo连接 - 用于诊断WebSocket问题
  public testEchoConnection(): Promise<boolean> {
    return new Promise((resolve) => {
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const serverHost = window.location.host;
      
      // 构建echo URL
      const echoUrl = `${wsProtocol}//${serverHost}/ws/echo`;
      
      console.log('测试Echo WebSocket连接: ' + echoUrl);
      
      let echoSocket: WebSocket | null = null;
      let timeoutId: any = null;
      
      try {
        echoSocket = new WebSocket(echoUrl);
        
        // 设置15秒超时
        timeoutId = setTimeout(() => {
          console.error('Echo测试连接超时');
          if (echoSocket && echoSocket.readyState !== WebSocket.CLOSED) {
            echoSocket.close();
          }
          resolve(false);
        }, 15000);
        
        echoSocket.onopen = () => {
          console.log('Echo测试连接成功, 发送测试消息');
          echoSocket?.send(JSON.stringify({ type: 'ECHO_TEST', message: 'Hello Echo' }));
        };
        
        echoSocket.onmessage = (event) => {
          console.log('Echo测试收到响应:', event.data);
          clearTimeout(timeoutId);
          if (echoSocket && echoSocket.readyState !== WebSocket.CLOSED) {
            echoSocket.close();
          }
          resolve(true);
        };
        
        echoSocket.onerror = (error) => {
          console.error('Echo测试连接错误:', error);
          clearTimeout(timeoutId);
          resolve(false);
        };
        
        echoSocket.onclose = () => {
          console.log('Echo测试连接已关闭');
          clearTimeout(timeoutId);
          resolve(false);
        };
      } catch (error) {
        console.error('创建Echo测试连接时出错:', error);
        clearTimeout(timeoutId);
        resolve(false);
      }
    });
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
      try {
        // 只发送JSON格式的心跳，避免服务器收到双重心跳
        const pingMessage = { type: 'PING', timestamp: Date.now() };
        this.socket.send(JSON.stringify(pingMessage));
        
        console.log('已发送心跳检测');
        this.lastPingTime = Date.now();
        this.missedHeartbeats = 0; // 重置失败计数
        
        // 设置心跳超时检测，延长超时时间到30秒，减少误判
        setTimeout(() => {
          const now = Date.now();
          // 检查是否在30秒内收到过PONG响应
          if (this.isConnected.value && (now - this.lastPongTime) > 30000 && !this.reconnectTimeout) {
            this.missedHeartbeats++;
            console.warn(`心跳检测无响应 (${this.missedHeartbeats}/${this.maxMissedHeartbeats})`);
            
            // 只有连续多次失败才重连，避免网络抖动导致的误判
            if (this.missedHeartbeats >= this.maxMissedHeartbeats) {
              console.warn('连续多次心跳无响应，连接可能已断开，尝试重新连接...');
              this.lastError.value = '连接可能已断开，正在尝试重新连接...';
              
              const currentUserId = this.userId;
              this.disconnect();
              if (currentUserId) {
                setTimeout(() => {
                  if (currentUserId) {
                    this.connect(currentUserId);
                  }
                }, 3000);
              }
            }
          }
        }, 30000); // 30秒超时
      } catch (error) {
        console.error('发送心跳时出错:', error);
        this.missedHeartbeats++;
        // 不要立即重连，减少频繁重连
        if (!this.reconnectTimeout && this.missedHeartbeats >= this.maxMissedHeartbeats) {
          setTimeout(() => this.attemptReconnect(), 5000);
        }
      }
    } else {
      console.warn('WebSocket不处于开启状态，无法发送心跳');
      // 如果连接不是开启状态，尝试重连，但避免过于频繁
      if (this.userId && !this.reconnectTimeout && !this.isConnected.value) {
        setTimeout(() => this.attemptReconnect(), 5000);
      }
    }
  }

  // 连接打开处理
  private handleOpen(_event: Event): void {
    console.log('WebSocket连接成功');
    this.isConnected.value = true;
    this.lastError.value = null;
    this.reconnectAttempts = 0;
    this.connectionStatus.value = "已连接";
    
    // 初始化心跳相关时间戳
    const now = Date.now();
    this.lastPingTime = now;
    this.lastPongTime = now;
    this.missedHeartbeats = 0;
    
    // 发送立即心跳以同步时间
    this.sendPing();
    
    // 设置心跳检测，30秒一次，与心跳超时时间保持一致
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
    }
    this.pingInterval = setInterval(() => this.sendPing(), 30000); // 30秒
    
    // 通知连接状态监听器
    this.connectionListeners.forEach(listener => {
      try {
        listener(true);
      } catch (error) {
        console.error('调用连接状态监听器失败:', error);
      }
    });
  }

  // 接收消息处理
  private handleMessage(event: MessageEvent): void {
    try {
      // 首先记录原始消息，便于调试
      console.log('WebSocket原始消息收到:', event.data);
      
      // 处理非JSON格式消息
      if (typeof event.data === 'string' && (
          event.data === 'PONG' || 
          event.data === 'OK' || 
          event.data.startsWith('echo:')
      )) {
        console.log('收到简单文本响应:', event.data);
        
        // 如果是心跳响应
        if (event.data === 'PONG') {
          // 更新连接状态
          this.lastError.value = null;
          this.lastPongTime = Date.now(); // 更新PONG响应时间
          this.missedHeartbeats = 0; // 重置失败计数
          
          // 通知所有连接状态监听器
          this.connectionListeners.forEach(listener => {
            try {
              listener(true);
            } catch (e) {
              console.error('调用连接状态监听器失败:', e);
            }
          });
        }
        
        // 通知所有消息监听器
        this.messageListeners.forEach(listener => {
          try {
            listener(event.data);
          } catch (e) {
            console.error('调用消息监听器失败:', e);
          }
        });
        
        return;
      }
      
      // 尝试解析JSON消息
      let data: any;
      try {
        data = JSON.parse(event.data);
        console.log('WebSocket JSON消息已解析:', data);
      } catch (jsonError) {
        console.warn('非JSON格式消息:', event.data);
        // 对于非JSON消息，创建一个简单的对象
        data = {
          type: 'RAW',
          content: event.data,
          timestamp: Date.now()
        };
      }
      
      // 生成消息标识符用于去重
      const messageId = (data.type || 'UNKNOWN') + '_' + 
                        (data.timestamp || Date.now()) + '_' + 
                        (data.messageId || '') + '_' + 
                        (data.senderId || '');
      
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
      
      // 根据消息类型处理
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
          this.lastPongTime = Date.now(); // 更新PONG响应时间
          this.missedHeartbeats = 0; // 重置失败计数
          
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
          console.log('收到连接状态更新:', data);
          break;
        default:
          // 处理其他类型消息
          this.messageListeners.forEach(listener => listener(data));
      }
    } catch (error) {
      console.error('处理WebSocket消息时出错:', error);
      // 尝试将原始消息作为字符串传递给监听器
      try {
        this.messageListeners.forEach(listener => listener({
          type: 'ERROR',
          content: String(event.data),
          error: String(error),
          timestamp: Date.now()
        }));
      } catch (e) {
        console.error('处理消息失败的回退处理也失败:', e);
      }
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
      console.warn('WebSocket异常关闭（代码1006），可能是网络波动或服务器重启');
      this.lastError.value = '连接意外断开，正在尝试重新连接...';
      // 对于1006错误，适当延迟后尝试重连
      if (this.userId && !this.reconnectTimeout) {
        setTimeout(() => {
          if (this.userId) {
            this.connect(this.userId);
          }
        }, 5000); // 增加延迟到5秒，减少频繁重连
        return;
      }
    } else if (event.code === 1000) {
      // 正常关闭，检查关闭原因
      console.log('WebSocket正常关闭（代码1000），关闭原因:', event.reason);
      
      // 如果是因为"用户在其他地方连接"而关闭，不要重连
      if (event.reason && event.reason.includes('用户在其他地方连接')) {
        console.log('由于用户在其他地方登录，连接已被服务器关闭，不进行重连');
        this.lastError.value = '您的账号在其他地方登录，连接已断开';
        this.connectionStatus.value = "已被替换";
        return;
      }
      
      // 其他正常关闭情况，较长延迟后重连
      if (this.userId && !this.reconnectTimeout) {
        console.log('服务器主动关闭连接，等待后重连...');
        setTimeout(() => {
          if (this.userId) {
            this.connect(this.userId);
          }
        }, 15000); // 15秒后尝试重连，避免与服务器重启冲突
        return;
      }
    }
    
    // 其他关闭代码，使用正常重连逻辑
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
