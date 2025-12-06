import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuard } from './guard'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: {
          title: '首页',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/list',
        name: 'SurveyList',
        component: () => import('@/views/user/SurveyList.vue'),
        meta: {
          title: '我的问卷',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/design',
        name: 'SurveyDesign',
        component: () => import('@/views/user/SurveyDesign.vue'),
        meta: {
          title: '问卷设计',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/fill/:id',
        name: 'SurveyFill',
        component: () => import('@/views/user/SurveyFill.vue'),
        meta: {
          title: '填写问卷',
          requiresAuth: false
        }
      },
      {
        path: 'user/survey/publish',
        name: 'SurveyPublish',
        component: () => import('@/views/user/SurveyPublish.vue'),
        meta: {
          title: '问卷发布',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: {
          title: '数据概览',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'survey',
        name: 'SurveyManagement',
        component: () => import('@/views/admin/SurveyManagement.vue'),
        meta: {
          title: '问卷管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'data',
        name: 'DataManagement',
        component: () => import('@/views/admin/DataManagement.vue'),
        meta: {
          title: '数据管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'system',
        name: 'SystemMonitor',
        component: () => import('@/views/admin/SystemMonitor.vue'),
        meta: {
          title: '系统监控',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

setupRouterGuard(router)

export default router
