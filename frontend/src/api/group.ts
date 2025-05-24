import request from '@/utils/request';

/**
 * 学习小组相关API
 */

// 定义接口类型
export interface GroupParams {
  page?: number;
  size?: number;
  keyword?: string;
  category?: string;
  [key: string]: any;
}

export interface GroupData {
  id?: number;
  name: string;
  category: string;
  tags?: string[];
  description: string;
  joinType: 'PUBLIC' | 'APPROVAL'; // 只允许 PUBLIC 或 APPROVAL
  avatar?: string;
  [key: string]: any;
}

export interface GroupMember {
  userId: number;
  username: string;
  nickname?: string;
  realName?: string;
  avatarUrl?: string;
  role: 'CREATOR' | 'ADMIN' | 'MEMBER';
  status: 'ACTIVE' | 'PENDING' | 'REJECTED';
  joinedAt?: string;
  requestTime?: string;
  [key: string]: any;
}

// 响应类型
export interface ApiResponse<T = any> {
  code: number;
  data: T;
  message?: string;
}

// 获取小组列表
export function getGroups(params: GroupParams): Promise<ApiResponse> {
  return request({
    url: '/api/groups',
    method: 'get',
    params
  });
}

// 获取小组详情
export function getGroupDetail(groupId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}`,
    method: 'get'
  });
}

// 创建学习小组
export function createGroup(data: GroupData): Promise<ApiResponse> {
  return request({
    url: '/api/groups',
    method: 'post',
    data
  });
}

// 更新小组信息
export function updateGroup(groupId: number | string, data: GroupData): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}`,
    method: 'put',
    data
  });
}

// 解散小组
export function disbandGroup(groupId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}`,
    method: 'delete'
  });
}

// 获取当前用户创建的小组
export function getCreatedGroups(): Promise<ApiResponse> {
  return request({
    url: '/api/groups/created',
    method: 'get'
  });
}

// 获取当前用户加入的小组
export function getJoinedGroups(): Promise<ApiResponse> {
  return request({
    url: '/api/groups/joined',
    method: 'get'
  });
}

// 加入小组
export function joinGroup(groupId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/join`,
    method: 'post'
  });
}

// 退出小组
export function quitGroup(groupId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/quit`,
    method: 'post'
  });
}

// 审批加入申请
export function approveJoinRequest(groupId: number | string, userId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/members/${userId}/approve`,
    method: 'post'
  });
}

// 拒绝加入申请
export function rejectJoinRequest(groupId: number | string, userId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/members/${userId}/reject`,
    method: 'post'
  });
}

// 获取小组成员列表
export function getGroupMembers(groupId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/members`,
    method: 'get'
  });
}

// 设置小组管理员
export function setAdmin(groupId: number | string, userId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/members/${userId}/set-admin`,
    method: 'post'
  });
}

// 取消小组管理员
export function cancelAdmin(groupId: number | string, userId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/members/${userId}/cancel-admin`,
    method: 'post'
  });
}

// 移除小组成员
export function removeMember(groupId: number | string, userId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/groups/${groupId}/members/${userId}`,
    method: 'delete'
  });
}

// 获取小组统计信息
export function getGroupStats(): Promise<ApiResponse> {
  return request({
    url: '/api/groups/stats',
    method: 'get'
  });
}
