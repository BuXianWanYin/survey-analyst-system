/**
 * 认证工具
 * 功能：提供Token和用户信息的存储、获取、移除功能，使用sessionStorage实现标签页独立的用户会话
 */

const TOKEN_KEY = 'survey_analyst_token'
const REFRESH_TOKEN_KEY = 'survey_analyst_refresh_token'
const USER_INFO_KEY = 'survey_analyst_user_info'

/**
 * 获取Token
 * @returns {string|null} Token字符串，不存在则返回null
 */
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 * @param {string} token Token字符串
 */
export function setToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token)
}

/**
 * 获取刷新Token
 * @returns {string|null} 刷新Token字符串，不存在则返回null
 */
export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 设置刷新Token
 * @param {string} token 刷新Token字符串
 */
export function setRefreshToken(token) {
  sessionStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/**
 * 移除Token和用户信息
 * 清除当前标签页的所有认证信息，不影响其他标签页
 */
export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(USER_INFO_KEY)
}

/**
 * 获取用户信息
 * @returns {Object|null} 用户信息对象，不存在则返回null
 */
export function getUserInfo() {
  const userInfo = sessionStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * @param {Object} userInfo 用户信息对象
 */
export function setUserInfo(userInfo) {
  sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

