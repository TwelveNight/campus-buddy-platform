import request from '@/utils/request';

/**
 * 小组文件相关API
 */

// 定义接口类型
export interface FileParams {
  groupId?: number | string;
  page?: number;
  size?: number;
  keyword?: string;
  [key: string]: any;
}

export interface FileData {
  id?: number;
  fileName: string;
  fileSize?: number;
  fileType?: string;
  uploaderId?: number;
  uploaderName?: string;
  groupId: number | string;
  description?: string;
  downloadUrl?: string;
  createdAt?: string;
  [key: string]: any;
}

export interface FileUpdateData {
  description?: string;
  [key: string]: any;
}

// 响应类型
interface ApiResponse<T = any> {
  code: number;
  data: T;
  message?: string;
}

// 获取小组文件列表
export function getGroupFiles(params: FileParams): Promise<ApiResponse> {
  return request({
    url: '/api/group-files',
    method: 'get',
    params
  });
}

// 获取文件详情
export function getFileDetail(fileId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-files/${fileId}`,
    method: 'get'
  });
}

// 上传文件
export function uploadFile(groupId: number | string, file: File, description?: string): Promise<ApiResponse> {
  const formData = new FormData();
  formData.append('groupId', groupId.toString());
  formData.append('file', file);
  if (description) {
    formData.append('description', description);
  }
  
  return request({
    url: '/api/group-files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// 更新文件信息
export function updateFileInfo(fileId: number | string, data: FileUpdateData): Promise<ApiResponse> {
  return request({
    url: `/api/group-files/${fileId}`,
    method: 'put',
    data
  });
}

// 删除文件
export function deleteFile(fileId: number | string): Promise<ApiResponse> {
  return request({
    url: `/api/group-files/${fileId}`,
    method: 'delete'
  });
}

// 下载文件（返回文件URL）
export function getFileDownloadUrl(groupId: number | string, fileName: string): string {
  return `${request.defaults.baseURL}/api/group-files/${groupId}/${fileName}`;
}
