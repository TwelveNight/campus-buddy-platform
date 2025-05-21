import { defineStore } from 'pinia'
import { getMyApplications, getReceivedApplications } from '../api/helpApplication'

export const useApplicationStore = defineStore('application', {
  state: () => ({
    myApplications: [] as any[],
    receivedApplications: [] as any[],
    loading: false,
    error: null as string | null
  }),
  
  getters: {
    // 按状态分类我的申请
    pendingApplications: (state) => state.myApplications.filter(app => app.status === 'PENDING'),
    acceptedApplications: (state) => state.myApplications.filter(app => app.status === 'ACCEPTED'),
    rejectedApplications: (state) => state.myApplications.filter(app => app.status === 'REJECTED'),
    
    // 按状态分类收到的申请
    pendingReceivedApplications: (state) => state.receivedApplications.filter(app => app.status === 'PENDING'),
    processingReceivedApplications: (state) => state.receivedApplications.filter(app => 
      app.status === 'ACCEPTED' && app.helpInfo.status === 'IN_PROGRESS'
    ),
    completedReceivedApplications: (state) => state.receivedApplications.filter(app => 
      app.helpInfo.status === 'RESOLVED' || app.helpInfo.status === 'CLOSED'
    )
  },
  
  actions: {
    // 获取我的申请列表
    async fetchMyApplications() {
      this.loading = true
      this.error = null
      
      try {
        const res = await getMyApplications()
        if (res.data.code === 200) {
          this.myApplications = res.data.data || []
        } else {
          throw new Error(res.data.message)
        }
        return this.myApplications
      } catch (error: any) {
        this.error = error.message || '获取我的申请列表失败'
        throw error
      } finally {
        this.loading = false
      }
    },
    
    // 获取收到的申请列表
    async fetchReceivedApplications() {
      this.loading = true
      this.error = null
      
      try {
        const res = await getReceivedApplications()
        if (res.data.code === 200) {
          this.receivedApplications = res.data.data || []
        } else {
          throw new Error(res.data.message)
        }
        return this.receivedApplications
      } catch (error: any) {
        this.error = error.message || '获取收到的申请列表失败'
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})
