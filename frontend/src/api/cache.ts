import request from '../utils/request'

export interface CacheStats {
  userCacheCount: number
  userVOCacheCount: number
  tokenCacheCount: number
  usernameCacheCount: number
  searchCacheCount: number
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
  }
}
