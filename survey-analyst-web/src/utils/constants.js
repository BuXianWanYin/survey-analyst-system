// API 路径
export const API_BASE_URL = import.meta.env.VITE_APP_BASE_API

// 请求超时时间
export const REQUEST_TIMEOUT = parseInt(import.meta.env.VITE_APP_TIMEOUT)

// Token 相关
export const TOKEN_KEY = 'survey_analyst_token'
export const REFRESH_TOKEN_KEY = 'survey_analyst_refresh_token'
export const USER_INFO_KEY = 'survey_analyst_user_info'

// 响应状态码
export const RESPONSE_CODE = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500
}

