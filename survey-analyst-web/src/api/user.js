import request from '@/utils/request'

/**
 * 用户相关 API
 */
export const userApi = {
  /**
   * 用户登录
   * @param {Object} data 登录信息
   * @returns {Promise} 登录结果
   */
  login(data) {
    return request.post('/auth/login', data)
  },

  /**
   * 用户注册
   * @param {Object} data 注册信息
   * @returns {Promise} 注册结果
   */
  register(data) {
    return request.post('/auth/register', data)
  },

  /**
   * 获取当前用户信息
   * @returns {Promise} 用户信息
   */
  getCurrentUser() {
    return request.get('/user/current')
  }
}

