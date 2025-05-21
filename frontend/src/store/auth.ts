import { defineStore } from 'pinia'
import { login, register } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as null | { username: string; nickname: string },
    isAuthenticated: !!localStorage.getItem('token'),
  }),
  actions: {
    async loginAction(payload: { username: string; password: string }) {
      const res = await login(payload)
      if (res.data.code === 200) {
        this.token = res.data.data.token
        this.user = res.data.data.user
        this.isAuthenticated = true
        localStorage.setItem('token', this.token)
      } else {
        throw new Error(res.data.message)
      }
    },
    async registerAction(payload: { username: string; password: string; nickname: string }) {
      const res = await register(payload)
      if (res.data.code !== 200) {
        throw new Error(res.data.message)
      }
    },
    logout() {
      this.token = ''
      this.user = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    }
  }
})
