/**
 * 应用入口文件
 * 功能：初始化Vue应用，配置Pinia状态管理、Vue Router路由、Element Plus组件库
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/button.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册Element Plus图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用Pinia状态管理
app.use(createPinia())
// 使用Vue Router路由
app.use(router)
// 使用Element Plus组件库，配置中文语言包
app.use(ElementPlus, {
  locale: zhCn,
})

// 挂载应用到DOM
app.mount('#app')

