const TOKEN_KEY = 'survey_analyst_token'
const REFRESH_TOKEN_KEY = 'survey_analyst_refresh_token'
const USER_INFO_KEY = 'survey_analyst_user_info'

/**
 * 获取Token
 * 从sessionStorage获取（标签页独立，每个标签页单独一个用户会话）
 */
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 * 存储到sessionStorage（标签页独立，每个标签页单独一个用户会话）
 */
export function setToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token)
}

/**
 * 获取刷新Token
 * 从sessionStorage获取（标签页独立）
 */
export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新Token
 * 存储到sessionStorage（标签页独立）
 */
export function setRefreshToken(token) {
  sessionStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/**
 * 移除Token
 * 移除当前标签页的token（sessionStorage），不影响其他标签页
 */
export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取用户信息
 * 从sessionStorage获取（标签页独立）
 */
export function getUserInfo() {
  const userInfo = sessionStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * 存储到sessionStorage（标签页独立，每个标签页单独一个用户会话）
 */
export function setUserInfo(userInfo) {
  sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

