import axios from 'axios'
import { saveAs } from 'file-saver'
import { getToken } from '@/utils/auth'

/**
 * 构建导出URL，避免路径重复
 * @param {string} path 导出路径
 * @returns {string} 完整的导出URL
 */
function buildExportUrl(path) {
  const baseURL = import.meta.env.VITE_APP_BASE_API || ''
  const normalizedBase = baseURL.replace(/\/+$/, '')
  if (normalizedBase.endsWith('/api')) {
    return `${normalizedBase}/export${path}`
  } else {
    return `${normalizedBase}/api/export${path}`
  }
}

/**
 * 导出相关 API
 * 功能：提供问卷数据导出、分析报告导出等功能
 */
export const exportApi = {
  /**
   * 导出问卷数据（Excel）
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 文件下载
   */
  async exportSurveyData(surveyId) {
    try {
      const exportUrl = buildExportUrl(`/survey/${surveyId}/data`)
      const response = await axios({
        url: exportUrl,
        method: 'get',
        responseType: 'blob',
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      })
      
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      saveAs(blob, `问卷数据_${surveyId}_${new Date().getTime()}.xlsx`)
      return { success: true }
    } catch (error) {
      console.error('导出失败:', error)
      throw error
    }
  },

  /**
   * 导出分析报告（PDF）
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 文件下载
   */
  async exportAnalysisReport(surveyId) {
    try {
      const exportUrl = buildExportUrl(`/survey/${surveyId}/report`)
      const response = await axios({
        url: exportUrl,
        method: 'get',
        responseType: 'blob',
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      })
      
      const blob = new Blob([response.data], {
        type: 'application/pdf'
      })
      saveAs(blob, `分析报告_${surveyId}_${new Date().getTime()}.pdf`)
      return { success: true }
    } catch (error) {
      console.error('导出失败:', error)
      throw error
    }
  }
}

