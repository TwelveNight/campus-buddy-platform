// 聊天相关类型定义

export interface ChatSession {
  userId: number;
  nickname: string;
  avatarUrl?: string;
  lastMessage: string;
  lastMessageTime: string | Date;
  unreadCount: number;
}

export interface ChatMessage {
  messageId: number;
  senderId: number;
  recipientId: number;
  content: string;
  createdAt: string | Date;
  isRead: boolean;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export type ChatSessionListResponse = ApiResponse<PageResult<ChatSession>>;
export type ChatMessageListResponse = ApiResponse<PageResult<ChatMessage>>;
export type SendMessageResponse = ApiResponse<number>;
export type CommonResponse = ApiResponse<null>;
