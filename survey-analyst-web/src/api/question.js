import request from '@/utils/request'

/**
 * 题目相关 API
 */
export const questionApi = {
  /**
   * 添加题目
   * @param {Object} data 题目信息
   * @returns {Promise} 创建结果
   */
  addQuestion(data) {
    return request.post('/question', data)
  },

  /**
   * 更新题目
   * @param {Number} id 题目ID
   * @param {Object} data 题目信息
   * @returns {Promise} 更新结果
   */
  updateQuestion(id, data) {
    return request.put(`/question/${id}`, data)
  },

  /**
   * 删除题目
   * @param {Number} id 题目ID
   * @returns {Promise} 删除结果
   */
  deleteQuestion(id) {
    return request.delete(`/question/${id}`)
  },

  /**
   * 获取问卷的所有题目
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 题目列表
   */
  getQuestionsBySurveyId(surveyId) {
    return request.get(`/question/survey/${surveyId}`)
  },

  /**
   * 更新题目排序
   * @param {Array} questions 题目列表
   * @returns {Promise} 更新结果
   */
  updateQuestionOrder(questions) {
    return request.put('/question/order', questions)
  }
}

