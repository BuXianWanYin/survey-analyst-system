import request from '@/utils/request'

/**
 * 问卷相关 API
 */
export const surveyApi = {
  /**
   * 分页查询问卷列表
   * @param {Object} params 查询参数
   * @returns {Promise} 问卷列表
   */
  getSurveyList(params) {
    return request.get('/survey/list', { params })
  },

  /**
   * 根据ID获取问卷详情
   * @param {Number} id 问卷ID
   * @returns {Promise} 问卷详情
   */
  getSurveyById(id) {
    return request.get(`/survey/${id}`)
  },

  /**
   * 创建问卷
   * @param {Object} data 问卷信息
   * @returns {Promise} 创建结果
   */
  createSurvey(data) {
    return request.post('/survey', data)
  },

  /**
   * 更新问卷
   * @param {Number} id 问卷ID
   * @param {Object} data 问卷信息
   * @returns {Promise} 更新结果
   */
  updateSurvey(id, data) {
    return request.put(`/survey/${id}`, data)
  },

  /**
   * 删除问卷
   * @param {Number} id 问卷ID
   * @returns {Promise} 删除结果
   */
  deleteSurvey(id) {
    return request.delete(`/survey/${id}`)
  },

  /**
   * 发布问卷
   * @param {Number} id 问卷ID
   * @returns {Promise} 发布结果
   */
  publishSurvey(id) {
    return request.post(`/survey/${id}/publish`)
  }
}
