/**
 * 用户状态管理
 * 功能：管理用户登录状态、用户信息、Token等信息
 */
import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: getUserInfo(),
    token: getToken() || ''
  }),

  getters: {
    /**
     * 是否已登录
     * @returns {boolean} 是否已登录
     */
    isLoggedIn: (state) => {
      return !!state.token
    }
  },

  actions: {
    /**
     * 设置用户信息
     * @param {Object} userInfo 用户信息对象
     */
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    },

    /**
     * 设置Token
     * @param {string} token Token字符串
     */
    setToken(token) {
      this.token = token
      setToken(token)
    },

    /**
     * 退出登录
     * 清除用户信息和Token，调用后端接口记录登出日志
     */
    async logout() {
      try {
        const { userApi } = await import('@/api')
        await userApi.logout()
      } catch (error) {
        console.error('记录登出日志失败:', error)
      } finally {
        this.userInfo = null
        this.token = ''
        removeToken()
      }
    }
  }
})

