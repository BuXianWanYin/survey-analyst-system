/**
 * 管理员相关 API
 */
import request from '@/utils/request'

export const adminApi = {
  /**
   * 分页查询用户列表
   * @param {Object} params 查询参数
   * @returns {Promise} 用户列表
   */
  getUserList(params) {
    return request.get('/admin/user/list', { params })
  },

  /**
   * 根据ID获取用户详情
   * @param {number} id 用户ID
   * @returns {Promise} 用户详情
   */
  getUserById(id) {
    return request.get(`/admin/user/${id}`)
  },

  /**
   * 更新用户信息
   * @param {number} id 用户ID
   * @param {Object} data 用户信息
   * @returns {Promise} 更新结果
   */
  updateUser(id, data) {
    return request.put(`/admin/user/${id}`, data)
  },

  /**
   * 更新用户状态
   * @param {number} id 用户ID
   * @param {string} status 状态值
   * @returns {Promise} 更新结果
   */
  updateUserStatus(id, status) {
    return request.put(`/admin/user/${id}/status`, null, { params: { status } })
  },

  /**
   * 删除用户
   * @param {number} id 用户ID
   * @returns {Promise} 删除结果
   */
  deleteUser(id) {
    return request.delete(`/admin/user/${id}`)
  },

  /**
   * 获取用户统计信息
   * @returns {Promise} 用户统计数据
   */
  getUserStatistics() {
    return request.get('/admin/user/statistics')
  },

  /**
   * 创建用户
   * @param {Object} data 用户信息
   * @returns {Promise} 创建结果
   */
  createUser(data) {
    return request.post('/admin/user', data)
  },

  /**
   * 分页查询系统日志
   * @param {Object} params 查询参数
   * @returns {Promise} 系统日志列表
   */
  getSystemLogs(params) {
    return request.get('/admin/system/logs', { params })
  },

  /**
   * 分页查询问卷列表
   * @param {Object} params 查询参数
   * @returns {Promise} 问卷列表
   */
  getSurveyList(params) {
    return request.get('/admin/survey/list', { params })
  },

  /**
   * 根据ID获取问卷详情
   * @param {number} id 问卷ID
   * @returns {Promise} 问卷详情
   */
  getSurveyById(id) {
    return request.get(`/admin/survey/${id}`)
  },

  /**
   * 更新问卷状态
   * @param {number} id 问卷ID
   * @param {string} status 状态值
   * @returns {Promise} 更新结果
   */
  updateSurveyStatus(id, status) {
    return request.put(`/admin/survey/${id}/status`, null, { params: { status } })
  },

  /**
   * 删除问卷
   * @param {number} id 问卷ID
   * @returns {Promise} 删除结果
   */
  deleteSurvey(id) {
    return request.delete(`/admin/survey/${id}`)
  },

  /**
   * 获取问卷统计信息
   * @returns {Promise} 问卷统计数据
   */
  getSurveyStatistics() {
    return request.get('/admin/survey/statistics')
  },

  /**
   * 分页查询填写记录列表
   * @param {Object} params 查询参数
   * @returns {Promise} 填写记录列表
   */
  getResponseList(params) {
    return request.get('/admin/data/response/list', { params })
  },

  /**
   * 根据ID获取填写记录详情
   * @param {number} id 填写记录ID
   * @returns {Promise} 填写记录详情
   */
  getResponseById(id) {
    return request.get(`/admin/data/response/${id}`)
  },

  /**
   * 获取填写记录的表单数据
   * @param {number} id 填写记录ID
   * @returns {Promise} 表单数据
   */
  getFormDataByResponseId(id) {
    return request.get(`/admin/data/response/${id}/form-data`)
  },

  /**
   * 删除填写记录
   * @param {number} id 填写记录ID
   * @returns {Promise} 删除结果
   */
  deleteResponse(id) {
    return request.delete(`/admin/data/response/${id}`)
  },

  /**
   * 获取数据统计信息
   * @returns {Promise} 数据统计数据
   */
  getDataStatistics() {
    return request.get('/admin/data/statistics')
  },

  /**
   * 获取系统概览数据
   * @returns {Promise} 系统概览数据
   */
  getSystemOverview() {
    return request.get('/admin/system/overview')
  },

  /**
   * 获取今日新增问卷数
   * @returns {Promise} 今日新增问卷数
   */
  getTodaySurveys() {
    return request.get('/admin/system/today-surveys')
  },

  /**
   * 获取问卷创建趋势
   * @param {string} timeRange 时间范围，默认'30d'
   * @returns {Promise} 问卷创建趋势数据
   */
  getSurveyCreateTrend(timeRange = '30d') {
    return request.get('/admin/system/survey-create-trend', { params: { timeRange } })
  },

  /**
   * 获取问卷填写趋势
   * @param {string} timeRange 时间范围，默认'30d'
   * @returns {Promise} 问卷填写趋势数据
   */
  getResponseTrend(timeRange = '30d') {
    return request.get('/admin/system/response-trend', { params: { timeRange } })
  },

  /**
   * 获取平均填写时长趋势
   * @param {string} timeRange 时间范围，默认'30d'
   * @returns {Promise} 平均填写时长趋势数据
   */
  getResponseDurationTrend(timeRange = '30d') {
    return request.get('/admin/system/response-duration-trend', { params: { timeRange } })
  },

  /**
   * 获取登录趋势
   * @param {string} timeRange 时间范围，默认'30d'
   * @returns {Promise} 登录趋势数据
   */
  getLoginTrend(timeRange = '30d') {
    return request.get('/admin/system/login-trend', { params: { timeRange } })
  },

  /**
   * 分页查询模板列表
   * @param {Object} params 查询参数
   * @returns {Promise} 模板列表
   */
  getTemplateList(params) {
    return request.get('/admin/template/page', { params })
  },

  /**
   * 获取模板详情
   * @param {string} key 模板key
   * @returns {Promise} 模板详情
   */
  getTemplateDetails(key) {
    return request.get(`/admin/template/details/${key}`)
  },

  /**
   * 创建模板
   * @param {Object} data 模板数据
   * @returns {Promise} 创建结果
   */
  createTemplate(data) {
    return request.post('/admin/template/create', data)
  },

  /**
   * 更新模板
   * @param {Object} data 模板数据
   * @returns {Promise} 更新结果
   */
  updateTemplate(data) {
    return request.post('/admin/template/update', data)
  },

  /**
   * 删除模板
   * @param {string} formKey 模板formKey
   * @returns {Promise} 删除结果
   */
  deleteTemplate(formKey) {
    return request.post('/admin/template/delete', { formKey })
  }
}
