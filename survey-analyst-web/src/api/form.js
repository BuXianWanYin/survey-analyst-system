import request from '@/utils/request'

/**
 * 表单相关 API（参考 tduck 实现）
 */
export const formApi = {
  /**
   * 保存表单配置
   * @param {Object} data 表单配置
   * @returns {Promise} 保存结果
   */
  saveFormConfig(data) {
    return request.post('/form/config', data)
  },

  /**
   * 获取表单配置
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 表单配置
   */
  getFormConfig(surveyId) {
    return request.get(`/form/config/${surveyId}`)
  },

  /**
   * 保存表单项列表
   * @param {String} formKey 表单key
   * @param {Array} items 表单项列表
   * @returns {Promise} 保存结果
   */
  saveFormItems(formKey, items) {
    return request.post(`/form/item/batch`, { formKey, items })
  },

  /**
   * 获取表单项列表
   * @param {String} formKey 表单key
   * @returns {Promise} 表单项列表
   */
  getFormItems(formKey) {
    return request.get(`/form/item/list`, { params: { key: formKey } })
  },

  /**
   * 删除表单项
   * @param {String} formItemId 表单项ID
   * @returns {Promise} 删除结果
   */
  deleteFormItem(formItemId) {
    return request.delete(`/form/item/${formItemId}`)
  }
}

