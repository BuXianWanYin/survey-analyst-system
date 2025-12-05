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
  getSurveyPage(params) {
    return request.get('/survey/page', { params })
  }
}

