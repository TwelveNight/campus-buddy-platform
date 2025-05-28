import request from '../utils/request'

export interface CacheStats {
  userCacheCount: number
  userVOCacheCount: number
  tokenCacheCount: number
  usernameCacheCount: number
  searchCacheCount: number
  creditScoreCacheCount: number
  // 帖子缓存相关计数
  groupPostsCacheCount: number
  postDetailCacheCount: number
  postUserCacheCount: number
  hotPostsCacheCount: number
  // 互助信息缓存相关计数
  helpInfoListCacheCount: number
  helpInfoDetailCacheCount: number
  helpInfoUserCacheCount: number
  helpInfoAdminCacheCount: number
  helpInfoSearchCacheCount: number
  totalCacheCount: number
  redisInfo: {
    version: string
    uptime: number
    connectedClients: number
    usedMemory: number
    totalKeys: number
  }
}

export interface CacheDetails {
  allKeys: string[]
  typeCount: Record<string, number>
  keyDetails: Record<string, {
    type: string
    ttl: number
    size: number
  }>
}

/**
   * 缓存相关API
   */
  export const cacheApi = {
    /**
     * 测试Redis连接
     */
    testConnection: () => {
      return request.get('/api/cache/test').then(res => res.data.data)
    },
  
    /**
     * 获取缓存统计信息
     */
    getStats: () => {
      return request.get('/api/cache/stats').then(res => res.data.data as CacheStats)
    },
  
    /**
     * 获取缓存详细信息
     */
    getDetails: () => {
      return request.get('/api/cache/details').then(res => res.data.data as CacheDetails)
    },
  
    /**
     * 清空所有缓存
     */
    clearAll: () => {
      return request.delete('/api/cache/clear').then(res => res.data.data)
    },
  
    /**
     * 清空指定用户的缓存
     */
    clearUser: (userId: number) => {
      return request.delete(`/api/cache/clear/user/${userId}`).then(res => res.data.data)
    },
  
    /**
     * 清空搜索缓存
     */
    clearSearch: () => {
      return request.delete('/api/cache/clear/search').then(res => res.data.data)
    },

    /**
     * 清空所有信用分缓存
     */
    clearCreditScore: () => {
      return request.delete('/api/cache/clear/credit').then(res => res.data.data)
    },

    /**
     * 清空指定用户的信用分缓存
     */
    clearUserCreditScore: (userId: number) => {
      return request.delete(`/api/cache/clear/credit/${userId}`).then(res => res.data.data)
    },

    /**
     * 清空所有帖子相关缓存
     */
    clearAllPosts: () => {
      return request.delete('/api/cache/clear/posts').then(res => res.data.data)
    },

    /**
     * 清空指定小组的帖子列表缓存
     */
    clearGroupPosts: (groupId: number) => {
      return request.delete(`/api/cache/clear/group-posts/${groupId}`).then(res => res.data.data)
    },

    /**
     * 清空指定帖子的详情缓存
     */
    clearPostDetail: (postId: number) => {
      return request.delete(`/api/cache/clear/post-detail/${postId}`).then(res => res.data.data)
    },

    /**
     * 清空热门帖子缓存
     */
    clearHotPosts: () => {
      return request.delete('/api/cache/clear/hot-posts').then(res => res.data.data)
    },

    /**
     * 清空所有互助信息缓存
     */
    clearAllHelpInfo: () => {
      return request.delete('/api/cache/clear/helpinfo').then(res => res.data.data)
    },

    /**
     * 清空指定互助信息的详情缓存
     */
    clearHelpInfoDetail: (infoId: number) => {
      return request.delete(`/api/cache/clear/helpinfo-detail/${infoId}`).then(res => res.data.data)
    },

    /**
     * 清空互助信息列表缓存
     */
    clearHelpInfoList: () => {
      return request.delete('/api/cache/clear/helpinfo-list').then(res => res.data.data)
    }
  }
