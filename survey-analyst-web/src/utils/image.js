/**
 * 图片URL处理工具函数
 * 用于处理后端图片路径的转换
 */

/**
 * 获取后端服务器地址（用于构建图片URL）
 * @returns {string} 后端服务器地址
 */
export const getBackendBaseUrl = () => {
  // 从 VITE_APP_BASE_API 提取后端地址
  const baseApi = import.meta.env.VITE_APP_BASE_API
  const proxyTarget = import.meta.env.VITE_SERVER_PROXY_TARGET
  
  // 如果 baseApi 是相对路径，使用 proxyTarget
  if (baseApi.startsWith('/')) {
    return proxyTarget
  }
  // 如果 baseApi 是完整URL，提取协议和主机
  try {
    const url = new URL(baseApi)
    return `${url.protocol}//${url.host}`
  } catch {
    return proxyTarget
  }
}

/**
 * 将相对路径转换为完整的后端URL
 * @param {string} imageUrl - 图片URL（可以是相对路径或完整URL）
 * @returns {string} 完整的图片URL
 */
export const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果已经是完整URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是相对路径（以 /upload/ 开头），拼接后端地址
  if (imageUrl.startsWith('/upload/')) {
    return `${getBackendBaseUrl()}${imageUrl}`
  }
  // 其他情况，添加 /upload/ 前缀
  return `${getBackendBaseUrl()}/upload/${imageUrl}`
}

/**
 * 将完整URL转换回相对路径（用于保存到后端）
 * @param {string} imageUrl - 图片URL（可以是完整URL或相对路径）
 * @returns {string} 相对路径
 */
export const getRelativeImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果已经是相对路径，直接返回
  if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是完整URL，提取相对路径部分
  try {
    const url = new URL(imageUrl)
    const path = url.pathname
    // 如果是 /upload/ 开头的路径，返回完整路径
    if (path.startsWith('/upload/')) {
      return path
    }
    // 如果路径中包含 upload，提取 upload 之后的部分
    const uploadIndex = path.indexOf('/upload/')
    if (uploadIndex !== -1) {
      return path.substring(uploadIndex)
    }
    // 其他情况，返回路径部分
    return path
  } catch {
    // 解析失败，返回原值
    return imageUrl
  }
}

