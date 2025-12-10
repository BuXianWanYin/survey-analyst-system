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
   * @param {Object} params 查询参数 { current, size, name, type, isPublic }
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
   * 更新模板
   * @param {Object} data 模板数据 { formKey, name, description, categoryId, coverImg, status }
   * @returns {Promise} 更新结果
   */
  updateTemplate(data) {
    return request.post('/form/template/update', data)
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
   * @returns {Promise} 新创建的问卷ID
   */
  useTemplateCreateSurvey(formKey) {
    return request.post('/survey/use-template/create', { formKey })
  },

  /**
   * 创建用户自定义分类
   * @param {Object} data 分类数据 { name, sort }
   * @returns {Promise} 创建结果
   */
  createCategory(data) {
    return request.post('/form/template/category/create', data)
  },

  /**
   * 更新用户自定义分类
   * @param {Object} data 分类数据 { id, name, sort }
   * @returns {Promise} 更新结果
   */
  updateCategory(data) {
    return request.post('/form/template/category/update', data)
  },

  /**
   * 删除用户自定义分类
   * @param {Object} data 分类数据 { id }
   * @returns {Promise} 删除结果
   */
  deleteCategory(data) {
    return request.post('/form/template/category/delete', data)
  }
}

