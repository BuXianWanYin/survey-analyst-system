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
  // 问卷填写页面（公开访问，支持 /user/survey/fill/:id 路径）
  {
    path: '/user/survey/fill/:id',
    name: 'UserSurveyFill',
    component: () => import('@/views/survey/fill/index.vue'),
    meta: {
      title: '填写问卷',
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
        path: 'survey/list',
        name: 'SurveyList',
        component: () => import('@/views/survey/list/index.vue'),
        meta: {
          title: '我的问卷',
          requiresAuth: true
        }
      },
      {
        path: 'survey/template',
        name: 'SurveyTemplate',
        component: () => import('@/views/survey/template/index.vue'),
        meta: {
          title: '问卷模板',
          requiresAuth: true
        }
      },
      {
        path: 'survey/template/preview',
        name: 'SurveyTemplatePreview',
        component: () => import('@/views/survey/template/preview.vue'),
        meta: {
          title: '模板预览',
          requiresAuth: true
        }
      },
      {
        path: 'survey/edit',
        component: () => import('@/views/survey/edit/container.vue'),
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
            component: () => import('@/views/survey/edit/editor.vue'),
            meta: {
              title: '编辑',
              requiresAuth: true
            }
          },
          {
            path: 'logic',
            name: 'SurveyDesignLogic',
            component: () => import('@/views/survey/edit/logic.vue'),
            meta: {
              title: '逻辑',
              requiresAuth: true
            }
          },
          {
            path: 'theme',
            name: 'SurveyDesignTheme',
            component: () => import('@/views/survey/edit/theme.vue'),
            meta: {
              title: '外观',
              requiresAuth: true
            }
          },
          {
            path: 'setting',
            name: 'SurveyDesignSetting',
            component: () => import('@/views/survey/edit/setting.vue'),
            meta: {
              title: '设置',
              requiresAuth: true
            }
          },
          {
            path: 'publish',
            name: 'SurveyDesignPublish',
            component: () => import('@/views/survey/edit/publish.vue'),
            meta: {
              title: '发布',
              requiresAuth: true
            }
          },
          {
            path: 'data',
            name: 'SurveyDesignData',
            component: () => import('@/views/survey/edit/data.vue'),
            meta: {
              title: '数据',
              requiresAuth: true
            }
          },
          {
            path: 'statistics',
            name: 'SurveyDesignStatistics',
            component: () => import('@/views/survey/edit/statistics.vue'),
            meta: {
              title: '统计',
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: 'survey/fill/:id',
        name: 'SurveyFill',
        component: () => import('@/views/survey/fill/index.vue'),
        meta: {
          title: '填写问卷',
          requiresAuth: false
        }
      },
      {
        path: 'survey/publish',
        name: 'SurveyPublish',
        component: () => import('@/views/survey/publish/index.vue'),
        meta: {
          title: '问卷发布',
          requiresAuth: true
        }
      },
      {
        path: 'survey/statistics',
        name: 'StatisticsOverview',
        component: () => import('@/views/survey/statistics/overview.vue'),
        meta: {
          title: '数据统计',
          requiresAuth: true
        }
      },
      {
        path: 'survey/statistics/detail',
        name: 'StatisticsDetail',
        component: () => import('@/views/survey/statistics/detail.vue'),
        meta: {
          title: '统计详情',
          requiresAuth: true
        }
      },
      {
        path: 'survey/analysis/cross',
        name: 'CrossAnalysis',
        component: () => import('@/views/survey/analysis/cross.vue'),
        meta: {
          title: '交叉分析',
          requiresAuth: true
        }
      },
      {
        path: 'survey/analysis/trend',
        name: 'TrendAnalysis',
        component: () => import('@/views/survey/analysis/trend.vue'),
        meta: {
          title: '趋势分析',
          requiresAuth: true
        }
      },
      {
        path: 'survey/analysis/profile',
        name: 'ProfileAnalysis',
        component: () => import('@/views/survey/analysis/profile.vue'),
        meta: {
          title: '样本画像',
          requiresAuth: true
        }
      },
      {
        path: 'survey/analysis/dashboard',
        name: 'VisualizationDashboard',
        component: () => import('@/views/survey/analysis/dashboard.vue'),
        meta: {
          title: '数据仪表盘',
          requiresAuth: true
        }
      },
      {
        path: 'system/dashboard',
        name: 'SystemDashboard',
        component: () => import('@/views/system/dashboard/index.vue'),
        meta: {
          title: '数据概览',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'system/survey',
        name: 'SystemSurvey',
        component: () => import('@/views/system/survey/index.vue'),
        meta: {
          title: '问卷管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'system/template',
        name: 'SystemTemplate',
        component: () => import('@/views/system/template/index.vue'),
        meta: {
          title: '公共模板管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'system/data',
        name: 'SystemData',
        component: () => import('@/views/system/data/index.vue'),
        meta: {
          title: '数据管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'system/monitor',
        name: 'SystemMonitor',
        component: () => import('@/views/system/monitor/index.vue'),
        meta: {
          title: '系统监控',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: {
          title: '系统日志',
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
