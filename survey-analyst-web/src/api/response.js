import request from '@/utils/request'

/**
 * 填写记录相关 API
 */
export const responseApi = {
  /**
   * 提交填写记录
   * @param {Object} data 填写记录信息
   * @returns {Promise} 提交结果
   */
  submitResponse(data) {
    return request.post('/response', data)
  },

  /**
   * 保存草稿
   * @param {Object} data 草稿信息
   * @returns {Promise} 保存结果
   */
  saveDraft(data) {
    return request.post('/response/draft', data)
  },

  /**
   * 获取填写记录详情
   * @param {Number} id 填写记录ID
   * @returns {Promise} 填写记录详情
   */
  getResponseById(id) {
    return request.get(`/response/${id}`)
  },

  /**
   * 分页查询填写记录
   * @param {Object} params 查询参数
   * @returns {Promise} 填写记录列表
   */
  getResponseList(params) {
    return request.get('/response/list', { params })
  }
}

