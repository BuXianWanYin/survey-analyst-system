# 在线问卷调查与数据分析系统 - 前端项目

## 项目简介

在线问卷调查与数据分析系统前端，是一个功能完善的问卷创建、发布、数据收集与分析平台。系统支持拖拽式问卷设计、多种题型、逻辑跳转、数据统计分析、可视化展示等功能。

### 核心功能

- **问卷设计**：拖拽式问卷编辑器，支持多种题型（单选、多选、文本、日期、评分、滑块、文件上传、签名等）
- **问卷发布**：支持公开、密码、私有等多种访问方式，生成分享链接和二维码
- **数据收集**：实时收集问卷填写数据，支持IP限制、设备限制、答题次数限制等
- **数据分析**：提供丰富的统计分析功能，包括基础统计、交叉分析、趋势分析、画像分析等
- **数据可视化**：支持多种图表类型（柱状图、饼图、折线图、热力图、词云图等）
- **模板管理**：支持问卷模板的创建、使用和管理
- **权限管理**：支持普通用户和管理员两种角色，管理员可进行系统管理

## 技术栈

### 核心框架
- **Vue 3.4.21** - 渐进式 JavaScript 框架
- **Vite 6.0.0** - 下一代前端构建工具
- **Vue Router 4.3.0** - 官方路由管理器
- **Pinia 2.1.7** - 状态管理库

### UI 组件库
- **Element Plus 2.6.3** - 基于 Vue 3 的组件库
- **@element-plus/icons-vue 2.3.1** - Element Plus 图标库

### 数据可视化
- **ECharts 5.5.0** - 强大的数据可视化图表库
- **vue-echarts 6.6.9** - ECharts 的 Vue 3 封装
- **echarts-wordcloud 2.1.0** - 词云图扩展

### 工具库
- **Axios 1.6.7** - HTTP 客户端
- **dayjs 1.11.10** - 日期处理库
- **lodash-es 4.17.21** - JavaScript 工具库
- **file-saver 2.0.5** - 文件保存工具
- **html2canvas 1.4.1** - HTML 转 Canvas
- **jspdf 2.5.1** - PDF 生成库
- **qrcode 1.5.3** - 二维码生成
- **quill 1.3.7** - 富文本编辑器
- **signature_pad 4.0.9** - 签名板组件

### 表单相关
- **vue-draggable-plus 0.6.0** - 拖拽组件
- **sortablejs 1.15.2** - 拖拽排序库
- **tduck-form-generator 1.9.5** - 表单生成器

### 开发工具
- **@vueuse/core 10.9.0** - Vue Composition API 工具集
- **sass 1.94.0** - CSS 预处理器
- **prettier 3.2.5** - 代码格式化工具

## 环境要求

- **Node.js**: >= 16.0.0
- **npm**: >= 7.0.0 或 **yarn**: >= 1.22.0
- **浏览器**: Chrome >= 90, Firefox >= 88, Safari >= 14, Edge >= 90

## 快速开始

### 1. 安装依赖

```bash
npm install
# 或
yarn install
```

### 2. 配置环境变量

在项目根目录创建 `.env` 文件，配置内容如下：

```env
# 应用标题
VITE_APP_TITLE=在线问卷调查与数据分析系统

# API 基础路径
VITE_APP_BASE_API=/api

# 请求超时时间（毫秒）
VITE_APP_TIMEOUT=30000

# 开发服务器端口
VITE_SERVER_PORT=3001

# 开发服务器主机地址
# 0.0.0.0：允许所有IP访问
# 127.0.0.1：仅允许本机访问
VITE_SERVER_HOST=0.0.0.0

# 代理目标地址（后端服务地址）
VITE_SERVER_PROXY_TARGET=http://192.168.31.33:8080
```

### 3. 启动开发服务器

```bash
npm run dev
# 或
yarn dev
```

启动成功后，访问：`http://localhost:3001`
```

## 项目结构

```
survey-analyst-web/
├── public/                           # 静态资源目录
│   ├── icon/                         # 图标文件
│   │   ├── copy-link.svg            # 复制链接图标
│   │   ├── qrcode-blue.svg          # 蓝色二维码图标
│   │   └── qrcode.svg               # 二维码图标
│   ├── images/                       # 图片资源
│   │   └── default-cover.svg        # 默认封面图
│   └── vite.svg                      # Vite 图标
├── src/                              # 源代码目录
│   ├── api/                          # API 接口定义
│   │   ├── admin.js                 # 管理员相关接口
│   │   ├── analysis.js              # 数据分析接口
│   │   ├── auth.js                  # 认证相关接口
│   │   ├── export.js                # 数据导出接口
│   │   ├── file.js                  # 文件上传接口
│   │   ├── form.js                  # 表单相关接口
│   │   ├── index.js                 # API 统一导出
│   │   ├── option.js                # 选项相关接口
│   │   ├── question.js              # 题目相关接口
│   │   ├── response.js              # 响应相关接口
│   │   ├── statistics.js            # 统计相关接口
│   │   ├── survey.js                # 问卷相关接口
│   │   ├── surveyPublish.js         # 问卷发布相关接口
│   │   ├── template.js              # 模板相关接口
│   │   └── user.js                  # 用户相关接口
│   ├── components/                   # 公共组件
│   │   ├── SignPad.vue              # 签名板组件
│   │   ├── SurveyFormRender.vue     # 表单渲染组件
│   │   └── SurveyPreview.vue        # 问卷预览组件
│   ├── layouts/                      # 布局组件
│   │   └── MainLayout.vue           # 主布局
│   ├── router/                       # 路由配置
│   │   ├── guard.js                 # 路由守卫
│   │   └── index.js                 # 路由定义
│   ├── stores/                       # Pinia 状态管理
│   │   └── user.js                  # 用户状态
│   ├── styles/                       # 全局样式
│   │   └── button.css               # 按钮样式
│   ├── utils/                        # 工具函数
│   │   ├── auth.js                  # 认证工具（Token管理）
│   │   ├── chartConfig.js           # 图表配置工具
│   │   ├── colorSchemes.js          # 配色方案工具
│   │   ├── constants.js             # 常量定义
│   │   ├── date.js                  # 日期处理工具
│   │   ├── displaySettings.js       # 显示设置工具
│   │   ├── echarts.js               # ECharts 工具函数
│   │   ├── image.js                 # 图片处理工具
│   │   ├── request.js               # Axios 封装
│   │   └── wordcloud.js             # 词云图工具
│   ├── views/                        # 页面组件
│   │   ├── auth/                     # 认证相关页面
│   │   │   ├── ForgotPassword.vue   # 忘记密码页
│   │   │   └── ResetPassword.vue    # 重置密码页
│   │   ├── survey/                   # 问卷相关页面
│   │   │   ├── analysis/            # 数据分析页面
│   │   │   │   ├── cross.vue        # 交叉分析
│   │   │   │   ├── dashboard.vue    # 分析仪表盘
│   │   │   │   ├── profile.vue      # 画像分析
│   │   │   │   └── trend.vue        # 趋势分析
│   │   │   ├── edit/                # 问卷编辑页面
│   │   │   │   ├── container.vue    # 编辑容器
│   │   │   │   ├── data.vue         # 数据管理
│   │   │   │   ├── editor.vue       # 问卷编辑器
│   │   │   │   ├── logic.vue        # 逻辑配置
│   │   │   │   ├── publish.vue      # 发布设置
│   │   │   │   ├── ReadonlyFormItem.vue  # 只读表单项
│   │   │   │   ├── setting.vue      # 设置页面
│   │   │   │   ├── setting/         # 设置子页面
│   │   │   │   │   ├── DataPush.vue        # 数据推送设置
│   │   │   │   │   ├── NotifySetting.vue   # 通知设置
│   │   │   │   │   ├── SubmitSetting.vue   # 提交设置
│   │   │   │   │   └── WriteSetting.vue    # 填写设置
│   │   │   │   ├── statistics.vue   # 数据统计
│   │   │   │   └── theme.vue        # 外观主题
│   │   │   ├── fill/                # 问卷填写页面
│   │   │   │   └── index.vue        # 填写页面
│   │   │   ├── list/                # 问卷列表页面
│   │   │   │   └── index.vue        # 列表页
│   │   │   ├── preview/             # 问卷预览页面
│   │   │   │   └── index.vue        # 预览页
│   │   │   ├── publish/             # 问卷发布页面
│   │   │   │   └── index.vue        # 发布页
│   │   │   ├── statistics/          # 数据统计页面
│   │   │   │   ├── detail.vue       # 详细统计
│   │   │   │   └── overview.vue     # 统计概览
│   │   │   └── template/            # 问卷模板页面
│   │   │       ├── index.vue        # 模板列表
│   │   │       └── preview.vue      # 模板预览
│   │   ├── system/                   # 系统管理页面（管理员）
│   │   │   ├── dashboard/           # 系统仪表盘
│   │   │   │   └── index.vue        # 数据概览
│   │   │   ├── data/                # 数据管理
│   │   │   │   └── index.vue        # 数据列表
│   │   │   ├── log/                 # 操作日志
│   │   │   │   └── index.vue        # 日志列表
│   │   │   ├── survey/              # 问卷管理
│   │   │   │   └── index.vue        # 问卷列表
│   │   │   ├── template/            # 模板管理
│   │   │   │   └── index.vue        # 模板列表
│   │   │   └── user/                # 用户管理
│   │   │       └── index.vue        # 用户列表
│   │   ├── user/                     # 用户相关页面
│   │   │   └── Profile.vue          # 个人中心
│   │   ├── Login.vue                 # 登录页
│   │   └── Register.vue              # 注册页
│   ├── App.vue                       # 根组件
│   └── main.js                       # 入口文件
├── .env.development                  # 开发环境变量（需创建）
├── index.html                        # HTML 模板
├── node_modules/                     # 依赖包目录（npm install 后生成）
├── package-lock.json                 # 依赖锁定文件
├── package.json                      # 项目配置文件
├── vite.config.js                    # Vite 配置文件
└── README.md                         # 项目说明文档
```

## 主要功能模块

### 1. 用户认证模块
- 用户注册（支持邮箱验证码）
- 用户登录（支持账号/邮箱登录）
- 密码找回
- 密码重置
- JWT Token 认证

### 2. 问卷管理模块
- 问卷列表（支持搜索、筛选、排序）
- 问卷创建与编辑
- 问卷预览
- 问卷发布
- 问卷删除（逻辑删除）

### 3. 问卷设计模块
- 拖拽式题目添加
- 多种题型支持：
  - 单选、多选、下拉选择
  - 单行文本、多行文本
  - 数字、日期、时间、日期时间
  - 评分、滑块
  - 图片选择、级联选择
  - 文件上传、图片上传
  - 签名、图片展示、图片轮播
  - 描述文本、分割线
- 题目属性配置（必填、提示、验证规则等）
- 逻辑跳转配置
- 外观主题配置
- 发布设置配置

### 4. 数据收集模块
- 问卷填写页面
- 表单验证
- 逻辑跳转
- 数据提交
- 填写限制（IP、设备、次数等）

### 5. 数据统计模块
- 问卷整体统计（填写数、完成率等）
- 题目统计（选项分布、文本统计等）
- 多种图表展示（表格、柱状图、饼图、折线图、词云图等）
- 数据导出（Excel、PDF、图片）

### 6. 数据分析模块
- 交叉分析（多变量关联分析）
- 趋势分析（时间序列分析）
- 画像分析（用户特征分析）
- 对比分析（分组对比）

### 7. 模板管理模块
- 模板列表
- 模板创建
- 模板使用
- 模板分类管理

### 8. 系统管理模块（管理员）
- 数据概览
- 用户管理
- 问卷管理
- 模板管理
- 数据管理
- 操作日志

## 配置说明

### 环境变量配置

需要在项目根目录创建环境变量配置文件 `.env`。

#### VITE_APP_TITLE
应用标题，显示在浏览器标签页和页面标题中。

#### VITE_APP_BASE_API
API 请求的基础路径，所有 API 请求都会加上此前缀。

#### VITE_APP_TIMEOUT
HTTP 请求超时时间（毫秒），默认 30000（30秒）。

#### VITE_SERVER_PORT
开发服务器端口，默认 3001。如果端口被占用，Vite 会自动尝试下一个可用端口。

#### VITE_SERVER_HOST
开发服务器主机地址：
- `0.0.0.0`：允许所有 IP 访问，适合局域网访问
- `127.0.0.1`：仅允许本机访问，更安全

#### VITE_SERVER_PROXY_TARGET
后端服务地址，用于开发环境的 API 代理。所有以 `VITE_APP_BASE_API` 开头的请求都会被代理到此地址。

### Vite 配置

主要配置项在 `vite.config.js` 中：

- **路径别名**：`@` 指向 `src` 目录
- **代理配置**：自动转发 API 请求到后端服务
- **构建配置**：输出目录、资源目录、代码分割等

## 开发规范

### 代码风格
- 使用 2 个空格缩进
- 使用单引号
- 不使用分号（可配置）
- 使用 Prettier 进行代码格式化

### 组件规范
- 组件文件使用 PascalCase 命名（如 `UserList.vue`）
- 组件结构：`<template>` → `<script setup>` → `<style scoped>`
- 使用 Composition API (`<script setup>`)
- 重要方法需要添加 JSDoc 注释

### 路由规范
- 路由名称使用 PascalCase
- 路由路径使用 kebab-case
- 路由需要配置 `meta` 信息（title、requiresAuth 等）

### API 规范
- API 方法使用 camelCase 命名
- 所有 API 方法需要添加 JSDoc 注释
- 统一使用 `request.js` 封装的 Axios 实例


## 常见问题

### 1. 端口被占用

如果 3001 端口被占用，Vite 会自动尝试下一个可用端口，或手动修改 `.env.development` 中的 `VITE_SERVER_PORT`。

### 2. API 请求失败

- 检查后端服务是否正常运行
- 检查 `.env.development` 中 `VITE_SERVER_PROXY_TARGET` 配置是否正确
- 检查网络连接和防火墙设置

### 3. 依赖安装失败

- 清除缓存：`npm cache clean --force`
- 删除 `node_modules` 和 `package-lock.json`，重新安装
- 使用国内镜像：`npm config set registry https://registry.npmmirror.com`

### 4. 构建失败

- 检查 Node.js 版本是否符合要求
- 检查是否有语法错误
- 查看构建日志中的具体错误信息

### 5. 路由刷新 404

确保 Web 服务器配置了 SPA 路由支持（所有路由指向 `index.html`）。

项目为前后端分离架构，需要配合后端服务使用。请确保后端服务正常运行后再启动前端项目。
