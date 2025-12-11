import request from '@/utils/request'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 发送验证码
   * @param {Object} data 包含邮箱和类型（REGISTER或RESET_PASSWORD）
   * @returns {Promise} 发送结果
   */
  sendVerificationCode(data) {
    return request.post('/auth/send-verification-code', data)
  },

  /**
   * 重置密码（使用验证码）
   * @param {Object} data 包含邮箱、验证码和新密码
   * @returns {Promise} 重置结果
   */
  resetPassword(data) {
    return request.post('/auth/reset-password', data)
  }
}

