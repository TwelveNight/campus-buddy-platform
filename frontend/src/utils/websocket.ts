import { ref } from 'vue';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: StompSubscription[] = [];
  private connectionId = 0;
  private reconnectTimeout: number | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10;
  private userId: number | null = null;
  private messageListeners: ((data: any) => void)[] = [];
  private notificationListeners: ((data: any) => void)[] = [];
  private connectionListeners: ((status: boolean) => void)[] = [];
  private processedMessages: Set<string> = new Set();

  public isConnected = ref(false);
  public lastError = ref<string | null>(null);
  public connectionStatus = ref<string>('未连接');

  public connect(userId: number): void {
    if (this.client && this.userId === userId && (this.client.connected || this.client.active)) {
      return;
    }

    this.clearReconnectTimeout();

    if (this.client) {
      this.closeClient();
    }

    const currentConnectionId = ++this.connectionId;
    this.userId = userId;
    this.connectionStatus.value = '正在连接';

    const endpoint = this.buildSockJSEndpoint(userId);
    console.log('尝试连接 STOMP/SockJS:', endpoint);

    this.client = new Client({
      webSocketFactory: () => new SockJS(endpoint),
      reconnectDelay: 0,
      heartbeatIncoming: 30000,
      heartbeatOutgoing: 30000,
      onConnect: () => this.handleOpen(currentConnectionId),
      onDisconnect: () => this.handleClose(currentConnectionId, 'STOMP disconnect'),
      onStompError: (frame) => this.handleError(currentConnectionId, frame.headers?.message || frame.body || 'STOMP error'),
      onWebSocketClose: () => this.handleClose(currentConnectionId, 'WebSocket closed'),
      onWebSocketError: () => this.handleError(currentConnectionId, 'WebSocket error')
    });

    this.client.activate();
  }

  public disconnect(): void {
    this.connectionId++;
    this.clearReconnectTimeout();
    this.closeClient();
    this.userId = null;
    this.reconnectAttempts = 0;
    this.isConnected.value = false;
    this.connectionStatus.value = '未连接';
  }

  private closeClient(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.subscriptions = [];

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }

  private buildSockJSEndpoint(userId: number): string {
    let baseUrl = window.location.origin;

    if (axios?.defaults?.baseURL) {
      try {
        baseUrl = new URL(axios.defaults.baseURL).origin;
      } catch (error) {
        console.warn('解析 axios baseURL 失败，使用当前域名', error);
      }
    }

    return `${baseUrl}/ws-stomp?userId=${encodeURIComponent(userId)}`;
  }

  private subscribeQueues(): void {
    if (!this.client || !this.client.connected) {
      return;
    }

    this.subscriptions = [
      this.client.subscribe('/user/queue/messages', message => this.handleMessage(message)),
      this.client.subscribe('/user/queue/notifications', message => this.handleMessage(message)),
      this.client.subscribe('/user/queue/errors', message => this.handleMessage(message))
    ];
  }

  private attemptReconnect(): void {
    if (!this.userId || this.reconnectAttempts >= this.maxReconnectAttempts) {
      this.connectionStatus.value = '重连失败';
      this.lastError.value = '达到最大重连次数，请刷新页面重试';
      return;
    }

    this.clearReconnectTimeout();

    this.reconnectAttempts += 1;
    const delay = this.reconnectAttempts < 3
      ? this.reconnectAttempts * 2000 + 1000
      : Math.min(30000, 10000 + 2000 * (this.reconnectAttempts - 2));

    this.connectionStatus.value = `正在重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`;
    this.reconnectTimeout = window.setTimeout(() => {
      this.reconnectTimeout = null;
      if (this.userId && navigator.onLine) {
        this.connect(this.userId);
      } else if (this.userId) {
        this.connectionStatus.value = '网络离线';
        const onlineHandler = () => {
          window.removeEventListener('online', onlineHandler);
          if (this.userId) {
            this.connect(this.userId);
          }
        };
        window.addEventListener('online', onlineHandler);
      }
    }, delay);
  }

  private clearReconnectTimeout(): void {
    if (this.reconnectTimeout !== null) {
      window.clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
  }

  public addMessageListener(callback: (data: any) => void): void {
    if (!this.messageListeners.includes(callback)) {
      this.messageListeners.push(callback);
    }
  }

  public removeMessageListener(callback: (data: any) => void): void {
    this.messageListeners = this.messageListeners.filter(listener => listener !== callback);
  }

  public addNotificationListener(callback: (data: any) => void): void {
    if (!this.notificationListeners.includes(callback)) {
      this.notificationListeners.push(callback);
    }
  }

  public removeNotificationListener(callback: (data: any) => void): void {
    this.notificationListeners = this.notificationListeners.filter(listener => listener !== callback);
  }

  public addConnectionListener(callback: (status: boolean) => void): void {
    if (!this.connectionListeners.includes(callback)) {
      this.connectionListeners.push(callback);
    }
  }

  public removeConnectionListener(callback: (status: boolean) => void): void {
    this.connectionListeners = this.connectionListeners.filter(listener => listener !== callback);
  }

  public testEchoConnection(): Promise<boolean> {
    return Promise.resolve(this.isConnected.value);
  }

  public sendMessage(message: any): void {
    if (!this.client || !this.client.connected) {
      console.error('STOMP is not connected');
      return;
    }

    this.client.publish({
      destination: '/app/messages',
      body: JSON.stringify(message)
    });
  }

  private handleOpen(connectionId: number): void {
    if (connectionId !== this.connectionId) {
      return;
    }

    console.log('STOMP/SockJS 连接成功');
    this.isConnected.value = true;
    this.lastError.value = null;
    this.reconnectAttempts = 0;
    this.connectionStatus.value = '已连接';
    this.subscribeQueues();
    this.connectionListeners.forEach(listener => listener(true));
  }

  private handleMessage(message: IMessage): void {
    try {
      const data = JSON.parse(message.body);
      const messageId = `${data.type || 'UNKNOWN'}_${data.timestamp || Date.now()}_${data.messageId || ''}_${data.senderId || ''}`;

      if (this.processedMessages.has(messageId)) {
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
          this.messageListeners.forEach(listener => listener(data));
          break;
        case 'PONG':
        case 'CONNECTION':
          this.connectionListeners.forEach(listener => listener(true));
          break;
        default:
          this.messageListeners.forEach(listener => listener(data));
      }
    } catch (error) {
      console.error('处理 STOMP 消息时出错:', error, message.body);
    }
  }

  private handleClose(connectionId: number, reason: string): void {
    if (connectionId !== this.connectionId || !this.userId) {
      return;
    }

    console.log('STOMP/SockJS 连接关闭:', reason);
    this.isConnected.value = false;
    this.connectionStatus.value = '已断开';
    this.connectionListeners.forEach(listener => listener(false));
    this.attemptReconnect();
  }

  private handleError(connectionId: number, error: string): void {
    if (connectionId !== this.connectionId) {
      return;
    }

    console.error('STOMP/SockJS 错误:', error);
    this.lastError.value = error || '连接出错';
    this.isConnected.value = false;
    this.connectionStatus.value = '连接出错';
    this.connectionListeners.forEach(listener => listener(false));
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;
