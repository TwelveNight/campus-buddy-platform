import axios from '../utils/request'

// 提交申请帮助请求
export function submitApplication(helpInfoId: number, data: {
  message: string;
  contactInfo?: string;
}) {
  return axios.post(`/api/help-application`, {
    infoId: helpInfoId,
    message: data.message
    // 移除contactInfo字段，后端实体中不存在该字段
  })
}

// 获取互助信息的申请列表
export function getApplications(helpInfoId: number) {
  return axios.get(`/api/help-application?infoId=${helpInfoId}`)
}

// 接受申请
export function acceptApplication(helpInfoId: number, applicationId: any) {
  // 确保applicationId是一个数值
  const numericApplicationId = Number(applicationId);
  
  // 如果转换结果是NaN，抛出错误
  if (isNaN(numericApplicationId)) {
    throw new Error('applicationId 必须是一个有效的数值');
  }
  
  console.log(`调用接受API，applicationId: ${applicationId}, 转换后: ${numericApplicationId}`);
  
  return axios.put(`/api/help-application/${numericApplicationId}/status`, {
    status: 'ACCEPTED'
  })
}

// 拒绝申请
export function rejectApplication(helpInfoId: number, applicationId: any) {
  // 确保applicationId是一个数值
  const numericApplicationId = Number(applicationId);
  
  // 如果转换结果是NaN，抛出错误
  if (isNaN(numericApplicationId)) {
    throw new Error('applicationId 必须是一个有效的数值');
  }
  
  console.log(`调用拒绝API，applicationId: ${applicationId}, 转换后: ${numericApplicationId}`);
  
  return axios.put(`/api/help-application/${numericApplicationId}/status`, {
    status: 'REJECTED'
  })
}

// 取消申请
export function cancelApplication(applicationId: number) {
  return axios.put(`/api/help-application/${applicationId}/status`, {
    status: 'CANCELED'
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

// 重新打开互助信息
export function reopenHelpInfo(helpInfoId: number) {
  return axios.patch(`/api/helpinfo/${helpInfoId}/status?status=OPEN`)
}

// 获取当前用户发出的申请列表
export function getMyApplications() {
  return axios.get('/api/help-application/my')
}

// 获取当前用户收到的申请列表
export function getReceivedApplications() {
  return axios.get('/api/help-application/received')
}
