import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 设置路由守卫
 * @param {Router} router Vue Router实例
 */
export function setupRouterGuard(router) {
  // 前置守卫
  router.beforeEach((to, from, next) => {
    // 设置页面标题
    if (to.meta.title) {
      document.title = `${to.meta.title} - 在线问卷调查系统`
    }
    
    // 检查是否需要登录
    if (to.meta.requiresAuth) {
      const token = getToken()
      if (!token) {
        // 未登录，跳转到登录页
        next({
          name: 'Login',
          query: {
            redirect: to.fullPath
          }
        })
        return
      }
    }
    
    // 检查是否需要管理员权限
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
  
  // 后置守卫
  router.afterEach(() => {
    // 可以在这里处理一些后置逻辑，如埋点等
  })
}

