/**
 * 请求封装
 * 功能：封装axios请求，提供请求拦截器（自动添加Token）、响应拦截器（统一处理响应和错误）
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: parseInt(import.meta.env.VITE_APP_TIMEOUT),
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动添加Token到请求头
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应和错误
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 401未授权时，清除token并刷新页面跳转到登录页
      if (res.code === 401) {
        removeToken()
        location.reload()
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service

