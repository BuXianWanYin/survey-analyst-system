import request from '@/utils/request'

/**
 * 选项相关 API
 */
export const optionApi = {
  /**
   * 添加选项
   * @param {Object} data 选项信息
   * @returns {Promise} 创建结果
   */
  addOption(data) {
    return request.post('/option', data)
  },

  /**
   * 更新选项
   * @param {Number} id 选项ID
   * @param {Object} data 选项信息
   * @returns {Promise} 更新结果
   */
  updateOption(id, data) {
    return request.put(`/option/${id}`, data)
  },

  /**
   * 删除选项
   * @param {Number} id 选项ID
   * @returns {Promise} 删除结果
   */
  deleteOption(id) {
    return request.delete(`/option/${id}`)
  },

  /**
   * 获取题目的所有选项
   * @param {Number} questionId 题目ID
   * @returns {Promise} 选项列表
   */
  getOptionsByQuestionId(questionId) {
    return request.get(`/option/question/${questionId}`)
  }
}

