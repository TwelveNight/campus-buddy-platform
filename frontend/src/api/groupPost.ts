import request from '@/utils/request';

/**
 * 小组帖子相关API
 */

// 定义接口类型
export interface PostParams {
  groupId?: number | string;
  page?: number;
  size?: number;
  keyword?: string;
  [key: string]: any;
}

export interface PostData {
  id?: number;
  groupId: number | string;
  title: string;
  content: string;
  authorId?: number;
  authorName?: string;
  authorAvatar?: string;
  likes?: number;
  createdAt?: string;
  updatedAt?: string;
  [key: string]: any;
}

// 响应类型
interface ApiResponse<T = any> {
  code: number;
  data: T;
  message?: string;
}

// 获取小组帖子列表
export function getGroupPosts(params: PostParams): Promise<ApiResponse> {
  return request({
    url: '/api/group-posts',
    method: 'get',
    params
  });
}

// 获取帖子详情
export function getPostDetail(postId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}`,
    method: 'get'
  });
}

// 发表帖子
export function createPost(data: PostData): Promise<ApiResponse> {
  return request({
    url: '/api/group-posts',
    method: 'post',
    data
  });
}

// 更新帖子
export function updatePost(postId: number | string, data: PostData): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}`,
    method: 'put',
    data
  });
}

// 删除帖子
export function deletePost(postId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}`,
    method: 'delete'
  });
}

// 点赞帖子
export function likePost(postId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}/like`,
    method: 'post'
  });
}

// 取消点赞
export function unlikePost(postId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}/unlike`,
    method: 'post'
  });
}

// 获取点赞状态
export function getLikeStatus(postId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}/like-status`,
    method: 'get'
  });
}

// =============== 管理员相关API ===============

// 管理员分页查询帖子
export function getAdminPosts(params: {
  page?: number;
  size?: number;
  keyword?: string;
  groupId?: number | string;
  status?: string;
}): Promise<ApiResponse> {
  return request({
    url: '/api/admin/post/page',
    method: 'get',
    params
  });
}

// 管理员更新帖子状态
export function updatePostStatus(postId: number | string, status: string): Promise<ApiResponse> {
  return request({
    url: `/api/admin/post/${postId}/status`,
    method: 'post',
    params: { status }
  });
}

// 管理员删除帖子
export function adminDeletePost(postId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/admin/post/${postId}`,
    method: 'delete'
  });
}
