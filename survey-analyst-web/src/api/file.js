import request from '@/utils/request'

/**
 * 文件相关 API
 */
export const fileApi = {
  /**
   * 上传文件
   * @param {FormData} formData 文件表单数据
   * @returns {Promise} 上传结果
   */
  uploadFile(formData) {
    return request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

