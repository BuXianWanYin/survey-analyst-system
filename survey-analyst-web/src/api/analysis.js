import request from '@/utils/request'

/**
 * 分析相关 API
 */
export const analysisApi = {
  /**
   * 交叉分析
   * @param {Object} data 分析参数 { surveyId, questionId1, questionId2 }
   * @returns {Promise} 交叉分析结果
   */
  crossAnalysis(data) {
    return request.post('/analysis/cross', data)
  },

  /**
   * 趋势分析
   * @param {Object} data 分析参数 { surveyId, questionId, timeRange }
   * @returns {Promise} 趋势分析结果
   */
  trendAnalysis(data) {
    return request.post('/analysis/trend', data)
  },

  /**
   * 样本画像分析
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 样本画像数据
   */
  profileAnalysis(surveyId) {
    return request.get(`/analysis/profile/${surveyId}`)
  },

  /**
   * 条件筛选分析
   * @param {Number} surveyId 问卷ID
   * @param {Object} filter 筛选条件
   * @returns {Promise} 筛选分析结果
   */
  filterAnalysis(surveyId, filter) {
    return request.post(`/analysis/filter/${surveyId}`, filter)
  }
}

