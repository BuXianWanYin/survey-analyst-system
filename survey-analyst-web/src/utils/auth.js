const TOKEN_KEY = 'survey_analyst_token'
const REFRESH_TOKEN_KEY = 'survey_analyst_refresh_token'
const USER_INFO_KEY = 'survey_analyst_user_info'

/**
 * 获取Token
 * 优先从sessionStorage获取（标签页独立），如果没有则从localStorage获取（跨标签页共享）
 */
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 * 同时存储到sessionStorage（标签页独立）和localStorage（跨标签页共享）
 */
export function setToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 获取刷新Token
 */
export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY) || localStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新Token
 */
export function setRefreshToken(token) {
  sessionStorage.setItem(REFRESH_TOKEN_KEY, token)
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/**
 * 移除Token
 * 只移除当前标签页的token（sessionStorage），不影响其他标签页
 */
export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(USER_INFO_KEY)
  // 可选：是否同时清除localStorage（如果希望所有标签页都退出）
  // localStorage.removeItem(TOKEN_KEY)
  // localStorage.removeItem(REFRESH_TOKEN_KEY)
  // localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取用户信息
 * 优先从sessionStorage获取（标签页独立），如果没有则从localStorage获取
 */
export function getUserInfo() {
  const userInfo = sessionStorage.getItem(USER_INFO_KEY) || localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * 同时存储到sessionStorage（标签页独立）和localStorage（跨标签页共享）
 */
export function setUserInfo(userInfo) {
  sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

