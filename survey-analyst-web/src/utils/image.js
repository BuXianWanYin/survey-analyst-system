/**
 * 图片URL处理工具
 * 功能：处理后端图片路径的转换，包括相对路径转完整URL、完整URL转相对路径等功能
 */

/**
 * 获取后端服务器地址
 * 用于构建图片完整URL
 * @returns {string} 后端服务器地址
 */
export const getBackendBaseUrl = () => {
  const baseApi = import.meta.env.VITE_APP_BASE_API
  const proxyTarget = import.meta.env.VITE_SERVER_PROXY_TARGET
  
  if (baseApi.startsWith('/')) {
    return proxyTarget
  }
  
  try {
    const url = new URL(baseApi)
    return `${url.protocol}//${url.host}`
  } catch {
    return proxyTarget
  }
}

/**
 * 将相对路径转换为完整的后端URL
 * @param {string} imageUrl 图片URL，可以是相对路径或完整URL
 * @returns {string} 完整的图片URL
 */
export const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  
  if (imageUrl.startsWith('/upload/')) {
    return `${getBackendBaseUrl()}${imageUrl}`
  }
  
  return `${getBackendBaseUrl()}/upload/${imageUrl}`
}

/**
 * 将完整URL转换回相对路径
 * 用于保存到后端
 * @param {string} imageUrl 图片URL，可以是完整URL或相对路径
 * @returns {string} 相对路径
 */
export const getRelativeImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  
  if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
    return imageUrl
  }
  
  try {
    const url = new URL(imageUrl)
    const path = url.pathname
    
    if (path.startsWith('/upload/')) {
      return path
    }
    
    const uploadIndex = path.indexOf('/upload/')
    if (uploadIndex !== -1) {
      return path.substring(uploadIndex)
    }
    
    return path
  } catch {
    return imageUrl
  }
}

