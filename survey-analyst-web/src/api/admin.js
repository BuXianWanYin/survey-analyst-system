import request from '@/utils/request'

/**
 * 管理员相关 API
 */
export const adminApi = {
  // 用户管理
  getUserList(params) {
    return request.get('/admin/user/list', { params })
  },
  getUserById(id) {
    return request.get(`/admin/user/${id}`)
  },
  updateUser(id, data) {
    return request.put(`/admin/user/${id}`, data)
  },
  updateUserStatus(id, status) {
    return request.put(`/admin/user/${id}/status`, null, { params: { status } })
  },
  deleteUser(id) {
    return request.delete(`/admin/user/${id}`)
  },
  getUserStatistics() {
    return request.get('/admin/user/statistics')
  },
  createUser(data) {
    return request.post('/admin/user', data)
  },

  // 系统日志
  getSystemLogs(params) {
    return request.get('/admin/system/logs', { params })
  },

  // 问卷管理
  getSurveyList(params) {
    return request.get('/admin/survey/list', { params })
  },
  getSurveyById(id) {
    return request.get(`/admin/survey/${id}`)
  },
  updateSurveyStatus(id, status) {
    return request.put(`/admin/survey/${id}/status`, null, { params: { status } })
  },
  deleteSurvey(id) {
    return request.delete(`/admin/survey/${id}`)
  },
  getSurveyStatistics() {
    return request.get('/admin/survey/statistics')
  },

  // 数据管理
  getResponseList(params) {
    return request.get('/admin/data/response/list', { params })
  },
  getResponseById(id) {
    return request.get(`/admin/data/response/${id}`)
  },
  getFormDataByResponseId(id) {
    return request.get(`/admin/data/response/${id}/form-data`)
  },
  deleteResponse(id) {
    return request.delete(`/admin/data/response/${id}`)
  },
  getDataStatistics() {
    return request.get('/admin/data/statistics')
  },

  // 系统概览（数据概览）
  getSystemOverview() {
    return request.get('/admin/system/overview')
  },

  // 获取今日新增问卷数
  getTodaySurveys() {
    return request.get('/admin/system/today-surveys')
  },

  // 获取问卷创建趋势
  getSurveyCreateTrend(timeRange = '30d') {
    return request.get('/admin/system/survey-create-trend', { params: { timeRange } })
  },

  // 获取问卷填写趋势
  getResponseTrend(timeRange = '30d') {
    return request.get('/admin/system/response-trend', { params: { timeRange } })
  },

  // 获取平均填写时长趋势
  getResponseDurationTrend(timeRange = '30d') {
    return request.get('/admin/system/response-duration-trend', { params: { timeRange } })
  },

  // 获取登录趋势
  getLoginTrend(timeRange = '30d') {
    return request.get('/admin/system/login-trend', { params: { timeRange } })
  },

  // 公共模板管理
  getTemplateList(params) {
    return request.get('/admin/template/page', { params })
  },
  getTemplateDetails(key) {
    return request.get(`/admin/template/details/${key}`)
  },
  createTemplate(data) {
    return request.post('/admin/template/create', data)
  },
  updateTemplate(data) {
    return request.post('/admin/template/update', data)
  },
  deleteTemplate(formKey) {
    return request.post('/admin/template/delete', { formKey })
  }
}
