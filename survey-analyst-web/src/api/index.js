/**
 * API统一导出入口
 * 按业务模块划分，每个模块创建独立的API文件
 */

// 用户相关 API
export { userApi } from './user'

// 问卷相关 API
export { surveyApi } from './survey'

// 文件相关 API
export { fileApi } from './file'

// 管理员相关 API
export { adminApi } from './admin'

