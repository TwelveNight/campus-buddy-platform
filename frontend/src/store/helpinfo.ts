import { defineStore } from 'pinia'
import { fetchHelpInfoList, fetchHelpInfoDetail, publishHelpInfo, updateHelpInfo } from '../api/helpinfo'

export const useHelpInfoStore = defineStore('helpinfo', {
  state: () => ({
    list: [] as any[],
    detail: null as any,
    total: 0,
    loading: false,
    currentPage: 1,
    pageSize: 10,
    filters: {
      keyword: '',
      type: '',
      status: ''
    }
  }),
  
  actions: {
    async fetchList(params?: any) {
      this.loading = true
      try {
        const queryParams = {
          page: params?.page || this.currentPage,
          size: params?.size || this.pageSize,
          ...this.filters,
          ...params
        }
        
        // 清除undefined和空字符串
        Object.keys(queryParams).forEach(key => 
          (queryParams[key] === undefined || queryParams[key] === '') && delete queryParams[key]
        )
        
        const res = await fetchHelpInfoList(queryParams)
        if (res.data.code === 200) {
          this.list = res.data.data.records || res.data.data || []
          this.total = res.data.data.total || this.list.length
          if (params?.page) this.currentPage = params.page
          if (params?.size) this.pageSize = params.size
        } else {
          throw new Error(res.data.message)
        }
        return this.list
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async fetchDetail(id: number) {
      this.loading = true
      try {
        const res = await fetchHelpInfoDetail(id)
        if (res.data.code === 200) {
          this.detail = res.data.data
        } else {
          throw new Error(res.data.message)
        }
        return this.detail
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async publishInfo(data: any) {
      this.loading = true
      try {
        const res = await publishHelpInfo(data)
        if (res.data.code === 200 || res.data.code === 201) {
          return res.data.data
        } else {
          throw new Error(res.data.message)
        }
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async updateInfo(id: number, data: any) {
      this.loading = true
      try {
        const res = await updateHelpInfo(id, data)
        if (res.data.code === 200) {
          // 如果当前正在查看的是这条记录，更新detail
          if (this.detail && this.detail.id === id) {
            this.detail = {...this.detail, ...data}
          }
          return res.data.data
        } else {
          throw new Error(res.data.message)
        }
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },
    
    updateFilters(filters: any) {
      this.filters = {...this.filters, ...filters}
    },
    
    resetFilters() {
      this.filters = {
        keyword: '',
        type: '',
        status: ''
      }
    }
  }
})
