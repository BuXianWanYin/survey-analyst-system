import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo } from '@/utils/auth'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    userInfo: getUserInfo(),
    // Token
    token: getToken() || ''
  }),

  getters: {
    /**
     * 是否已登录
     */
    isLoggedIn: (state) => {
      return !!state.token
    }
  },

  actions: {
    /**
     * 设置用户信息
     * @param {Object} userInfo 用户信息
     */
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    },

    /**
     * 设置Token
     * @param {String} token Token
     */
    setToken(token) {
      this.token = token
      setToken(token)
    },

    /**
     * 退出登录
     */
    async logout() {
      try {
        // 调用后端接口记录登出日志
        const { userApi } = await import('@/api')
        await userApi.logout()
      } catch (error) {
        // 登出日志记录失败不影响登出操作
        console.error('记录登出日志失败:', error)
      } finally {
        this.userInfo = null
        this.token = ''
        removeToken()
      }
    }
  }
})

