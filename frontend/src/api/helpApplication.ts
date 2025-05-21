import axios from '../utils/request'

// 提交申请帮助请求
export function submitApplication(helpInfoId: number, data: {
  message: string;
  contactInfo?: string;
}) {
  return axios.post(`/api/help-application`, {
    infoId: helpInfoId,
    message: data.message,
    contactInfo: data.contactInfo
  })
}

// 获取互助信息的申请列表
export function getApplications(helpInfoId: number) {
  return axios.get(`/api/help-application?infoId=${helpInfoId}`)
}

// 接受申请
export function acceptApplication(helpInfoId: number, applicationId: number) {
  return axios.put(`/api/help-application/${applicationId}/status`, {
    status: 'ACCEPTED'
  })
}

// 拒绝申请
export function rejectApplication(helpInfoId: number, applicationId: number) {
  return axios.put(`/api/help-application/${applicationId}/status`, {
    status: 'REJECTED'
  })
}

// 完成互助（将互助信息状态改为已解决）
export function completeHelpInfo(helpInfoId: number) {
  return axios.patch(`/api/helpinfo/${helpInfoId}/status?status=RESOLVED`)
}

// 关闭互助信息
export function closeHelpInfo(helpInfoId: number) {
  return axios.patch(`/api/helpinfo/${helpInfoId}/status?status=CLOSED`)
}

// 获取当前用户发出的申请列表
export function getMyApplications() {
  return axios.get('/api/help-application/my')
}

// 获取当前用户收到的申请列表
export function getReceivedApplications() {
  return axios.get('/api/help-application/received')
}
