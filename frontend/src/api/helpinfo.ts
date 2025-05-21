import axios from '../utils/request'

// 获取互助信息列表
export function fetchHelpInfoList(params: any) {
  return axios.get('/api/helpinfo', { params })
}

// 获取互助信息详情
export function fetchHelpInfoDetail(id: number) {
  return axios.get(`/api/helpinfo/${id}`)
}

// 发布互助信息
export function publishHelpInfo(data: any) {
  return axios.post('/api/helpinfo', data)
}

// 更新互助信息
export function updateHelpInfo(id: number, data: any) {
  return axios.put(`/api/helpinfo/${id}`, data)
}

// 删除互助信息
export function deleteHelpInfo(id: number) {
  return axios.delete(`/api/helpinfo/${id}`)
}
