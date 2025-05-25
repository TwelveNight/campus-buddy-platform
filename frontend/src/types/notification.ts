// 定义通知相关的类型

// 通知项
export interface NotificationItem {
  notificationId: number;
  senderId?: number;
  senderName?: string;
  senderAvatar?: string;
  type: string;
  title: string;
  content: string;
  isRead: boolean;
  relatedId?: number;
  relatedLink?: string;
  createdAt: string;
  updatedAt?: string;
}

// 分页响应
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// API响应基础结构
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 具体通知列表响应类型
export type NotificationListResponse = ApiResponse<PageResult<NotificationItem>>;

// 未读通知数量响应
export interface UnreadCountResponse extends ApiResponse<{count: number}> {}

// 通用操作响应
export type CommonResponse = ApiResponse<null>;
