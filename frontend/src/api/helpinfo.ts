import axios from 'axios'

// 获取互助信息列表
export function fetchHelpInfoList(params: any) {
  return axios.get('/api/helpinfo', { params })
}

// 获取互助信息详情
export function fetchHelpInfoDetail(id: number) {
  return axios.get(`/api/helpinfo/${id}`)
}

// 发布互助信息
export function publishHelpInfo(data: any, token: string) {
  return axios.post('/api/helpinfo', data, {
    headers: { Authorization: `Bearer ${token}` }
  })
}
