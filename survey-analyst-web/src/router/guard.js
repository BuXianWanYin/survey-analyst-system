/**
 * 路由守卫
 * 功能：设置路由前置守卫和后置守卫，处理页面标题设置、登录验证、管理员权限验证等
 */

import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 设置路由守卫
 * @param {Object} router Vue Router实例
 */
export function setupRouterGuard(router) {
  router.beforeEach((to, from, next) => {
    if (to.meta.title) {
      document.title = `${to.meta.title} - 在线问卷调查与数据分析系统`
    }
    
    if (to.meta.requiresAuth) {
      const token = getToken()
      if (!token) {
        next({
          name: 'Login',
          query: {
            redirect: to.fullPath
          }
        })
        return
      }
    }
    
    if (to.meta.requiresAdmin) {
      const userStore = useUserStore()
      if (!userStore.userInfo || userStore.userInfo.role !== 'ADMIN') {
        ElMessage.warning('您没有权限访问此页面')
        next('/survey/list')
        return
      }
    }
    
    next()
  })
  
  router.afterEach(() => {
    // 可以在这里处理一些后置逻辑，如埋点等
  })
}

