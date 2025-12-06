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
  },

  /**
   * 删除文件
   * @param {String} fileUrl 文件URL
   * @returns {Promise} 删除结果
   */
  deleteFile(fileUrl) {
    return request.delete('/file', { params: { fileUrl } })
  },

  /**
   * 获取文件信息
   * @param {String} fileUrl 文件URL
   * @returns {Promise} 文件信息
   */
  getFileInfo(fileUrl) {
    return request.get('/file/info', { params: { fileUrl } })
  }
}
