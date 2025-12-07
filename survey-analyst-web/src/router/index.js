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
        path: 'user/survey/template',
        name: 'SurveyTemplate',
        component: () => import('@/views/user/SurveyTemplate.vue'),
        meta: {
          title: '问卷模板',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/design',
        component: () => import('@/views/user/SurveyDesignContainer.vue'),
        redirect: (to) => {
          return { name: 'SurveyDesignEditor', query: to.query }
        },
        meta: {
          title: '问卷设计',
          requiresAuth: true
        },
        children: [
          {
            path: 'editor',
            name: 'SurveyDesignEditor',
            component: () => import('@/views/user/SurveyDesignNew.vue'),
            meta: {
              title: '编辑',
              requiresAuth: true
            }
          },
          {
            path: 'logic',
            name: 'SurveyDesignLogic',
            component: () => import('@/views/user/SurveyDesignLogic.vue'),
            meta: {
              title: '逻辑',
              requiresAuth: true
            }
          },
          {
            path: 'theme',
            name: 'SurveyDesignTheme',
            component: () => import('@/views/user/SurveyDesignTheme.vue'),
            meta: {
              title: '外观',
              requiresAuth: true
            }
          },
          {
            path: 'setting',
            name: 'SurveyDesignSetting',
            component: () => import('@/views/user/SurveyDesignSetting.vue'),
            meta: {
              title: '设置',
              requiresAuth: true
            }
          },
          {
            path: 'publish',
            name: 'SurveyDesignPublish',
            component: () => import('@/views/user/SurveyDesignPublish.vue'),
            meta: {
              title: '发布',
              requiresAuth: true
            }
          },
          {
            path: 'data',
            name: 'SurveyDesignData',
            component: () => import('@/views/user/SurveyDesignData.vue'),
            meta: {
              title: '数据',
              requiresAuth: true
            }
          },
          {
            path: 'statistics',
            name: 'SurveyDesignStatistics',
            component: () => import('@/views/user/SurveyDesignStatistics.vue'),
            meta: {
              title: '统计',
              requiresAuth: true
            }
          }
        ]
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
      },
      {
        path: 'user/survey/statistics',
        name: 'StatisticsOverview',
        component: () => import('@/views/user/StatisticsOverview.vue'),
        meta: {
          title: '数据统计',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/statistics/detail',
        name: 'StatisticsDetail',
        component: () => import('@/views/user/StatisticsDetail.vue'),
        meta: {
          title: '统计详情',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/analysis/cross',
        name: 'CrossAnalysis',
        component: () => import('@/views/user/CrossAnalysis.vue'),
        meta: {
          title: '交叉分析',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/analysis/trend',
        name: 'TrendAnalysis',
        component: () => import('@/views/user/TrendAnalysis.vue'),
        meta: {
          title: '趋势分析',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/analysis/profile',
        name: 'ProfileAnalysis',
        component: () => import('@/views/user/ProfileAnalysis.vue'),
        meta: {
          title: '样本画像',
          requiresAuth: true
        }
      },
      {
        path: 'user/survey/analysis/dashboard',
        name: 'VisualizationDashboard',
        component: () => import('@/views/user/VisualizationDashboard.vue'),
        meta: {
          title: '数据仪表盘',
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
