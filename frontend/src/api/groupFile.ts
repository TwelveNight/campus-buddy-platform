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
    url: '/api/upload/group-file',
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

// 下载文件（自动处理token和保存）
export async function downloadFile(groupId: number | string, fileName: string): Promise<void> {
  const response = await request({
    url: `/api/group-files/${groupId}/${fileName}`,
    method: 'get',
    responseType: 'blob'
  });
  // 获取文件名
  const disposition = response.headers?.['content-disposition'];
  let downloadName = fileName;
  if (disposition) {
    const match = disposition.match(/filename="?([^";]+)"?/);
    if (match) downloadName = decodeURIComponent(match[1]);
  }
  // 创建下载链接
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', downloadName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

// 推荐：通过文件ID下载文件（自动处理token和保存）
export async function downloadFileById(fileId: number | string, fileName: string): Promise<void> {
  // 这里直接请求后端新接口，后端会302重定向到七牛云外链
  const response = await request({
    url: `/api/group-files/download/${fileId}`,
    method: 'get',
    responseType: 'blob'
  });
  // 获取文件名
  let downloadName = fileName;
  const disposition = response.headers?.['content-disposition'];
  if (disposition) {
    const match = disposition.match(/filename="?([^";]+)"?/);
    if (match) downloadName = decodeURIComponent(match[1]);
  }
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', downloadName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}
