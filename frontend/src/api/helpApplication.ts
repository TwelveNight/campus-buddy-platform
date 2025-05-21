import axios from '../utils/request'

// 提交申请帮助请求
export function submitApplication(helpInfoId: number, data: {
  message: string;
  contactInfo?: string;
}) {
  return axios.post(`/api/helpinfo/${helpInfoId}/apply`, data)
}

// 获取互助信息的申请列表
export function getApplications(helpInfoId: number) {
  return axios.get(`/api/helpinfo/${helpInfoId}/applications`)
}

// 接受申请
export function acceptApplication(helpInfoId: number, applicationId: number) {
  return axios.post(`/api/helpinfo/${helpInfoId}/applications/${applicationId}/accept`)
}

// 拒绝申请
export function rejectApplication(helpInfoId: number, applicationId: number) {
  return axios.post(`/api/helpinfo/${helpInfoId}/applications/${applicationId}/reject`)
}

// 完成互助（将互助信息状态改为已解决）
export function completeHelpInfo(helpInfoId: number) {
  return axios.post(`/api/helpinfo/${helpInfoId}/complete`)
}

// 关闭互助信息
export function closeHelpInfo(helpInfoId: number) {
  return axios.post(`/api/helpinfo/${helpInfoId}/close`)
}

// 获取当前用户发出的申请列表
export function getMyApplications() {
  return axios.get('/api/user/applications')
}

// 获取当前用户收到的申请列表
export function getReceivedApplications() {
  return axios.get('/api/user/applications/received')
}
