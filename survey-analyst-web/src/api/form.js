import request from '@/utils/request'

/**
 * 表单相关 API
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
  },

  /**
   * 保存表单设置
   * @param {Number} surveyId 问卷ID
   * @param {Object} settings 设置内容
   * @returns {Promise} 保存结果
   */
  saveFormSetting(surveyId, settings) {
    return request.post(`/form/setting/${surveyId}`, settings)
  },

  /**
   * 获取表单设置
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 表单设置
   */
  getFormSetting(surveyId) {
    return request.get(`/form/setting/${surveyId}`)
  },

  /**
   * 保存表单逻辑
   * @param {Number} surveyId 问卷ID
   * @param {Array} scheme 逻辑定义
   * @returns {Promise} 保存结果
   */
  saveFormLogic(surveyId, scheme) {
    return request.post(`/form/logic/${surveyId}`, scheme)
  },

  /**
   * 获取表单逻辑
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 表单逻辑
   */
  getFormLogic(surveyId) {
    return request.get(`/form/logic/${surveyId}`)
  },

  /**
   * 保存表单主题
   * @param {Object} theme 主题数据
   * @returns {Promise} 保存结果
   */
  saveFormTheme(theme) {
    return request.post('/form/theme', theme)
  },

  /**
   * 获取表单主题
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 表单主题
   */
  getFormTheme(surveyId) {
    return request.get(`/form/theme/${surveyId}`)
  },

  /**
   * 分页查询表单数据
   * @param {String} formKey 表单key
   * @param {Object} params 查询参数
   * @returns {Promise} 表单数据列表
   */
  getFormDataList(formKey, params) {
    return request.get('/form/data/list', {
      params: {
        formKey,
        ...params
      }
    })
  },

  /**
   * 获取表单数据详情
   * @param {Number} id 数据ID
   * @returns {Promise} 表单数据详情
   */
  getFormDataById(id) {
    return request.get(`/form/data/${id}`)
  },

  /**
   * 删除表单数据
   * @param {Number} id 数据ID
   * @returns {Promise} 删除结果
   */
  deleteFormData(id) {
    return request.delete(`/form/data/${id}`)
  },

  /**
   * 填写前校验
   * @param {String} formKey 表单key
   * @param {String} deviceId 设备ID（可选）
   * @returns {Promise} 校验结果
   */
  validateBeforeFill(formKey, deviceId) {
    const params = {
      formKey
    }
    if (deviceId) {
      params.deviceId = deviceId
    }
    return request.post('/form/data/validate', params)
  },

  /**
   * 提交表单数据
   * @param {String} formKey 表单key
   * @param {Object} answers 答案数据
   * @param {String} deviceId 设备ID（可选）
   * @returns {Promise} 提交结果
   */
  submitFormData(formKey, answers, deviceId) {
    const params = {
      formKey,
      originalData: answers
    }
    if (deviceId) {
      params.deviceId = deviceId
    }
    return request.post('/form/data', params)
  }
}

