import request from '@/utils/request'

/**
 * 模板相关 API
 */
export const templateApi = {
  /**
   * 获取模板分类列表
   * @returns {Promise} 模板分类列表
   */
  getTemplateTypeList() {
    return request.get('/form/template/type/list')
  },

  /**
   * 分页查询模板列表
   * @param {Object} params 查询参数 { current, size, name, type }
   * @returns {Promise} 模板列表
   */
  getTemplatePage(params) {
    return request.get('/form/template/page', { params })
  },

  /**
   * 获取模板详情
   * @param {String} key 模板key
   * @returns {Promise} 模板详情
   */
  getTemplateDetails(key) {
    return request.get(`/form/template/details/${key}`)
  },

  /**
   * 保存为模板
   * @param {Object} data 模板数据 { formKey, name, description, categoryId, coverImg }
   * @returns {Promise} 保存结果
   */
  createTemplate(data) {
    return request.post('/form/template/create', data)
  },

  /**
   * 删除模板
   * @param {String} formKey 模板formKey
   * @returns {Promise} 删除结果
   */
  deleteTemplate(formKey) {
    return request.post('/form/template/delete', { formKey })
  },

  /**
   * 使用模板创建问卷
   * @param {String} formKey 模板formKey
   * @returns {Promise} 新创建的问卷formKey
   */
  useTemplateCreateSurvey(formKey) {
    return request.post('/user/form/use-template/create', { formKey })
  }
}

