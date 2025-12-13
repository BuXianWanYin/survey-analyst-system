/**
 * 日期格式化工具
 * 功能：提供日期格式化功能，支持将日期对象、日期字符串或时间戳格式化为指定格式
 */

/**
 * 格式化日期为yyyy-MM-dd HH:mm:ss格式
 * @param {Date|string|number} date 日期对象、日期字符串或时间戳
 * @returns {string|null} 格式化后的日期字符串，格式：yyyy-MM-dd HH:mm:ss，如果日期无效则返回null
 */
export function formatDateTime(date) {
  if (!date) {
    return null
  }
  
  const d = date instanceof Date ? date : new Date(date)
  
  if (isNaN(d.getTime())) {
    return null
  }
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 获取当前时间的格式化字符串
 * @returns {string} 当前时间的格式化字符串，格式：yyyy-MM-dd HH:mm:ss
 */
export function getCurrentDateTime() {
  return formatDateTime(new Date())
}

