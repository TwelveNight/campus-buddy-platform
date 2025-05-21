import { defineStore } from 'pinia'
import { getMyApplications, getReceivedApplications } from '../api/helpApplication'
import { fetchHelpInfoDetail } from '../api/helpinfo'
import { getUserById } from '../api/user'
import { useAuthStore } from './auth'

export const useApplicationStore = defineStore('application', {
  state: () => ({
    myApplications: [] as any[],
    receivedApplications: [] as any[],
    loading: false,
    error: null as string | null,
    userCache: new Map() // 用户信息缓存
  }),
  
  getters: {
    // 按状态分类我的申请
    pendingApplications: (state) => state.myApplications.filter(app => app.status === 'PENDING'),
    acceptedApplications: (state) => state.myApplications.filter(app => app.status === 'ACCEPTED'),
    rejectedApplications: (state) => state.myApplications.filter(app => app.status === 'REJECTED'),
    
    // 按状态分类收到的申请
    pendingReceivedApplications: (state) => state.receivedApplications.filter(app => app.status === 'PENDING'),
    processingReceivedApplications: (state) => state.receivedApplications.filter(app => 
      app.status === 'ACCEPTED' && app.helpInfo && app.helpInfo.status === 'IN_PROGRESS'
    ),
    completedReceivedApplications: (state) => state.receivedApplications.filter(app => 
      app.helpInfo && (app.helpInfo.status === 'RESOLVED' || app.helpInfo.status === 'CLOSED')
    )
  },
  
  actions: {
    // 获取用户信息，先从缓存中找，没有则通过API获取
    async getUserInfo(userId: number) {
      // 如果无效的用户ID，直接返回未知用户
      if (!userId) {
        return { nickname: '未知用户', avatar: '' }
      }
      
      // 如果缓存中有，直接返回
      if (this.userCache.has(userId)) {
        return this.userCache.get(userId)
      }
      
      try {
        const res = await getUserById(userId)
        if (res.data.code === 200 && res.data.data) {
          const userData = {
            nickname: res.data.data.nickname || '未知用户',
            avatar: res.data.data.avatarUrl || ''
          }
          // 添加到缓存
          this.userCache.set(userId, userData)
          return userData
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
      
      return { nickname: '未知用户', avatar: '' }
    },
    
    // 为申请加载互助信息
    async loadHelpInfoForApplications(applications: any[]) {
      const authStore = useAuthStore()
      
      // 先将当前用户添加到缓存
      if (authStore.user && authStore.user.userId && authStore.user.nickname) {
        this.userCache.set(authStore.user.userId, {
          nickname: authStore.user.nickname,
          avatar: authStore.user.avatarUrl || ''
        })
      }
      
      const promises = applications
        .filter(app => app.infoId && (!app.helpInfo || !app.helpInfo.title || app.helpInfo.title === '加载中...'))
        .map(async (app) => {
          try {
            // 获取互助信息详情
            const res = await fetchHelpInfoDetail(app.infoId)
            if (res.data.code === 200 && res.data.data) {
              app.helpInfo = res.data.data
              
              // 添加互助发布者信息
              if (res.data.data.publisher) {
                app.helpInfo.publisherNickname = res.data.data.publisher.nickname || '未知用户'
                app.helpInfo.publisherAvatar = res.data.data.publisher.avatar
              } else if (app.publisherId) {
                // 如果有发布者ID但没有发布者信息，尝试获取用户信息
                const publisher = await this.getUserInfo(app.publisherId)
                app.helpInfo.publisherNickname = publisher.nickname
                app.helpInfo.publisherAvatar = publisher.avatar
              } else {
                app.helpInfo.publisherNickname = '未知用户'
                app.helpInfo.publisherAvatar = ''
              }
            }
          } catch (error) {
            console.error('加载互助信息失败:', error)
            
            // 创建一个最基本的helpInfo对象，避免undefined错误
            app.helpInfo = {
              id: app.infoId, // 确保使用申请中的互助信息ID
              title: '无法获取互助信息',
              status: 'ERROR',
              type: '未知',
              publisherNickname: '未知用户',
              publisherAvatar: ''
            }
          }
          return app
        })

      if (promises.length > 0) {
        await Promise.all(promises)
      }
    },

    // 为申请添加申请人信息
    async loadApplicantInfo(applications: any[]) {
      const applicantPromises = applications
        .filter(app => app.applicantId && (!app.applicantNickname || app.applicantNickname === '加载中...'))
        .map(async (app) => {
          try {
            // 获取申请人信息
            const userInfo = await this.getUserInfo(app.applicantId)
            app.applicantNickname = userInfo.nickname
            app.applicantAvatar = userInfo.avatar
          } catch (error) {
            console.error('获取申请人信息失败:', error)
            app.applicantNickname = '未知用户'
          }
          return app
        })

      if (applicantPromises.length > 0) {
        await Promise.all(applicantPromises)
      }
    },

    // 获取我的申请列表
    async fetchMyApplications() {
      this.loading = true
      this.error = null
      
      try {
        const res = await getMyApplications()
        if (res.data.code === 200) {
          const applications = res.data.data || []
          // 为每个申请对象添加默认互助信息，避免undefined错误
          this.myApplications = applications.map((app: any) => {
            if (!app.helpInfo && app.infoId) {
              app.helpInfo = {
                id: app.infoId,
                title: '加载中...',
                status: '',
                type: '',
                publisherId: app.publisherId
              }
            }
            
            // 如果没有申请人昵称，设置为加载中状态
            if (!app.applicantNickname && app.applicantId) {
              app.applicantNickname = '加载中...'
            }
            
            return app
          })
          
          // 如果后端返回的数据中没有包含完整的helpInfo对象，需要单独获取
          await this.loadHelpInfoForApplications(this.myApplications)
          // 加载申请人信息
          await this.loadApplicantInfo(this.myApplications)
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
          const applications = res.data.data || []
          
          // 为每个申请对象添加申请人信息（如果有）
          this.receivedApplications = applications.map((app: any) => {
            if (!app.helpInfo && app.infoId) {
              app.helpInfo = {
                id: app.infoId,
                title: '加载中...',
                status: '',
                type: '',
                publisherId: app.publisherId
              }
            }
            
            // 如果没有申请人昵称，设置为加载中，稍后获取
            if (!app.applicantNickname && app.applicantId) {
              app.applicantNickname = '加载中...'
            }
            
            return app
          })
          
          // 如果后端返回的数据中没有包含完整的helpInfo对象，需要单独获取
          await this.loadHelpInfoForApplications(this.receivedApplications)
          // 加载申请人信息
          await this.loadApplicantInfo(this.receivedApplications)
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
