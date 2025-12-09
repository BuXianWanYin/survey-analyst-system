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
  deleteResponse(id) {
    return request.delete(`/admin/data/response/${id}`)
  },
  getDataStatistics() {
    return request.get('/admin/data/statistics')
  },

  // 系统监控
  getSystemOverview() {
    return request.get('/admin/system/overview')
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
