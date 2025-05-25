import axios from '../utils/request'

// 获取互助任务列表
export function fetchHelpInfoList(params: any) {
  return axios.get('/api/helpinfo', { params })
}

// 获取互助任务详情
export function fetchHelpInfoDetail(id: number) {
  return axios.get(`/api/helpinfo/${id}`)
}

// 发布互助任务
export function publishHelpInfo(data: any) {
  return axios.post('/api/helpinfo', data)
}

// 更新互助任务
export function updateHelpInfo(id: number, data: any) {
  return axios.put(`/api/helpinfo/${id}`, data)
}

// 删除互助任务
export function deleteHelpInfo(id: number) {
  return axios.delete(`/api/helpinfo/${id}`)
}

// 更新互助任务状态
export function updateHelpInfoStatus(id: number, status: string) {
  return axios.patch(`/api/helpinfo/${id}/status?status=${status}`)
}

// 增加浏览量
export function incrementHelpInfoViewCount(id: number) {
  return axios.patch(`/api/helpinfo/${id}/view`)
}
