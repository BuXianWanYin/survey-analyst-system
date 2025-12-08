/**
 * API统一导出入口
 * 按业务模块划分，每个模块创建独立的API文件
 */

// 用户相关 API
export { userApi } from './user'

// 问卷相关 API
export { surveyApi } from './survey'

// 题目相关 API
export { questionApi } from './question'

// 选项相关 API
export { optionApi } from './option'

// 填写记录相关 API
export { responseApi } from './response'

// 问卷发布与推广相关 API
export { surveyPublishApi } from './surveyPublish'

// 文件相关 API
export { fileApi } from './file'

// 管理员相关 API
export { adminApi } from './admin'

// 统计相关 API
export { statisticsApi } from './statistics'

// 分析相关 API
export { analysisApi } from './analysis'

// 导出相关 API
export { exportApi } from './export'

// 表单设计相关 API
export { formApi } from './form'

// 模板相关 API
export { templateApi } from './template'