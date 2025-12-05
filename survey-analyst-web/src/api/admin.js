import request from '@/utils/request'

/**
 * 管理员相关 API
 */
export const adminApi = {
  /**
   * 分页查询用户列表
   * @param {Object} params 查询参数
   * @returns {Promise} 用户列表
   */
  getUserPage(params) {
    return request.get('/admin/user/page', { params })
  }
}

