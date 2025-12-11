import request from '@/utils/request'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 发送密码重置邮件
   * @param {Object} data 包含邮箱
   * @returns {Promise} 发送结果
   */
  forgotPassword(data) {
    return request.post('/auth/forgot-password', data)
  },

  /**
   * 重置密码
   * @param {Object} data 包含token和新密码
   * @returns {Promise} 重置结果
   */
  resetPassword(data) {
    return request.post('/auth/reset-password', data)
  }
}

