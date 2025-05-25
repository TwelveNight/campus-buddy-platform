import request from '../utils/request'

/**
 * 文件上传相关API
 */
export const uploadApi = {
  /**
   * 上传用户头像
   * @param file 头像文件
   * @returns 头像URL
   */
  uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<string>('/api/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传小组头像
   * @param file 头像文件
   * @returns 头像URL
   */
  uploadGroupAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<string>('/api/upload/group-avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传图片
   * @param file 图片文件
   * @returns 图片URL
   */
  uploadImage(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<string>('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
