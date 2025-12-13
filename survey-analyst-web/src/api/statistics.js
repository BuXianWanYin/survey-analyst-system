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
   * @param {String} formItemId 表单项ID（字符串）
   * @param {Number} surveyId 问卷ID（可选，用于容错处理）
   * @returns {Promise} 题目统计数据
   */
  getQuestionStatistics(formItemId, surveyId) {
    const params = surveyId ? { surveyId } : {}
    return request.get(`/statistics/question/${encodeURIComponent(formItemId)}`, { params })
  },

  /**
   * 获取选项统计
   * @param {String} formItemId 表单项ID（字符串）
   * @returns {Promise} 选项统计数据
   */
  getOptionStatistics(formItemId) {
    return request.get(`/statistics/option/${encodeURIComponent(formItemId)}`)
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
  },

  /**
   * 获取所有统计数据（统一接口）
   * @param {Number} surveyId 问卷ID
   * @param {Object} options 选项 { includeTrend, includeSource, includeDevice }
   * @returns {Promise} 所有统计数据
   */
  getAllStatistics(surveyId, options = {}) {
    const params = {
      includeTrend: options.includeTrend || false,
      includeSource: options.includeSource || false,
      includeDevice: options.includeDevice || false
    }
    return request.get(`/statistics/all/${surveyId}`, { params })
  },

  /**
   * 获取筛选后的统计数据（分类统计）
   * @param {Number} surveyId 问卷ID
   * @param {Array} filters 筛选条件数组 [{ formItemId, optionValue }, ...]
   * @returns {Promise} 筛选后的统计数据
   */
  getFilteredStatistics(surveyId, filters) {
    return request.post(`/statistics/filter`, {
      surveyId,
      filters
    })
  }
}

