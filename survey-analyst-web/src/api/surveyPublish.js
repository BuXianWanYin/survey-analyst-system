import request from '@/utils/request'

/**
 * 问卷发布与推广相关 API
 */
export const surveyPublishApi = {
  /**
   * 获取问卷链接
   * @param {Number} id 问卷ID
   * @returns {Promise} 问卷链接
   */
  getSurveyLink(id) {
    return request.get(`/survey/${id}/link`)
  },

  /**
   * 获取问卷二维码
   * @param {Number} id 问卷ID
   * @returns {Promise} 二维码Base64字符串
   */
  getQRCode(id) {
    return request.get(`/survey/${id}/qrcode`)
  }
}

