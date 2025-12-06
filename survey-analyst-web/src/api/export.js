import axios from 'axios'
import { saveAs } from 'file-saver'
import { getToken } from '@/utils/auth'

/**
 * 导出相关 API
 */
export const exportApi = {
  /**
   * 导出问卷数据（Excel）
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 文件下载
   */
  async exportSurveyData(surveyId) {
    try {
      const response = await axios({
        url: `${import.meta.env.VITE_APP_BASE_API}/export/survey/${surveyId}/data`,
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
   * 导出统计数据（Excel）
   * @param {Number} surveyId 问卷ID
   * @returns {Promise} 文件下载
   */
  async exportStatistics(surveyId) {
    try {
      const response = await axios({
        url: `${import.meta.env.VITE_APP_BASE_API}/export/survey/${surveyId}/statistics`,
        method: 'get',
        responseType: 'blob',
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      })
      
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })
      saveAs(blob, `统计数据_${surveyId}_${new Date().getTime()}.xlsx`)
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
      const response = await axios({
        url: `${import.meta.env.VITE_APP_BASE_API}/export/survey/${surveyId}/report`,
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

