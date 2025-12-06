import request from '@/utils/request'

/**
 * 统计相关 API
 */
export const statisticsApi = {
  /**
   * 获取问卷统计概览
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 统计概览数据
   */
  getSurveyStatistics(surveyId) {
    return request.get(`/statistics/survey/${surveyId}`)
  },

  /**
   * 获取题目统计
   * @param {Number} questionId 题目ID
   * @returns {Promise} 题目统计数据
   */
  getQuestionStatistics(questionId) {
    return request.get(`/statistics/question/${questionId}`)
  },

  /**
   * 获取选项统计
   * @param {Number} questionId 题目ID
   * @returns {Promise} 选项统计数据
   */
  getOptionStatistics(questionId) {
    return request.get(`/statistics/option/${questionId}`)
  },

  /**
   * 获取填写趋势
   * @param {Number} surveyId 问卷ID
   * @param {String} timeRange 时间范围（7d, 30d, all）
   * @returns {Promise} 填写趋势数据
   */
  getResponseTrend(surveyId, timeRange = '30d') {
    return request.get(`/statistics/trend/${surveyId}`, { params: { timeRange } })
  },

  /**
   * 获取填写来源
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 填写来源数据
   */
  getResponseSource(surveyId) {
    return request.get(`/statistics/source/${surveyId}`)
  },

  /**
   * 获取设备统计
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 设备统计数据
   */
  getDeviceStatistics(surveyId) {
    return request.get(`/statistics/device/${surveyId}`)
  },

  /**
   * 刷新统计数据
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 刷新结果
   */
  refreshStatistics(surveyId) {
    return request.post(`/statistics/refresh/${surveyId}`)
  }
}

