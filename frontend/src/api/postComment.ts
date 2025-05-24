import request from '@/utils/request';

/**
 * 帖子评论相关API
 */

// 定义接口类型
export interface CommentParams {
  postId: number | string;
  pageNum?: number;
  pageSize?: number;
}

export interface CommentData {
  postId: number | string;
  content: string;
  parentId?: number | null;
  [key: string]: any;
}

// 响应类型
interface ApiResponse<T = any> {
  code: number;
  data: T;
  message?: string;
}

// 获取帖子评论列表
export function getPostComments(params: CommentParams): Promise<ApiResponse> {
  const { postId, pageNum = 1, pageSize = 10 } = params;
  return request({
    url: `/api/group-posts/${postId}/comments`,
    method: 'get',
    params: { pageNum, pageSize }
  });
}

// 添加评论
export function addComment(data: CommentData): Promise<ApiResponse> {
  const { postId, content } = data;
  return request({
    url: `/api/group-posts/${postId}/comments`,
    method: 'post',
    data: { content }
  });
}

// 删除评论
export function deleteComment(postId: number | string, commentId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}/comments/${commentId}`,
    method: 'delete'
  });
}

// 获取评论详情
export function getCommentDetail(postId: number | string, commentId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-posts/${postId}/comments/${commentId}`,
    method: 'get'
  });
}
