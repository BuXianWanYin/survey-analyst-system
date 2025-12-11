import request from '@/utils/request'

/**
 * 填写记录相关 API
 */
export const responseApi = {
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
  },

  /**
   * 获取问卷填写数量
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 填写数量
   */
  getResponseCount(surveyId) {
    return request.get(`/response/count/${surveyId}`)
  }
}

