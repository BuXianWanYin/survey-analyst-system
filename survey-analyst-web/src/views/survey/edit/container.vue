<template>
  <div class="survey-design-container">
    <!-- 顶部工具栏 -->
    <div class="header-container">
      <el-card class="header-card">
        <div class="header-content">
          <el-button
            text
            class="back-button"
            @click="router.back()"
          >
            <el-icon><ArrowLeft /></el-icon>
            <span class="back-text">返回</span>
          </el-button>
          <!-- 移动端菜单按钮 -->
          <el-button
            v-if="isMobile"
            text
            class="menu-toggle-button"
            @click="showMenuDrawer = true"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
          <div class="header-title">
            <span>{{ surveyTitle || '未命名问卷' }}</span>
          </div>
          <div
            v-if="showHeaderActions"
            class="header-actions"
          >
            <el-button
              type="primary"
              @click="handlePreview"
            >
              预览
            </el-button>
            <el-button
              v-if="activeMenu === 'editor'"
              type="success"
              @click="handleSave"
            >
              保存
            </el-button>
            <el-button
              v-if="activeMenu === 'editor' && !isAdminTemplateEdit"
              type="warning"
              @click="handleSaveAsTemplate"
            >
              保存为模板
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主体：左侧菜单 + 右侧内容 -->
    <div class="main-container">
      <!-- 左侧：导航菜单（PC端显示） -->
      <div
        v-if="!isMobile"
        class="left-menu-container"
      >
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="menuItem in menuItemList"
            :key="menuItem.route"
            :index="menuItem.route"
          >
            <el-icon><component :is="menuItem.iconComponent" /></el-icon>
            <span>{{ menuItem.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 移动端菜单抽屉 -->
      <el-drawer
        v-model="showMenuDrawer"
        title="菜单"
        :size="drawerWidth"
        direction="ltr"
        :with-header="true"
        class="menu-drawer"
      >
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical drawer-menu"
          @select="handleMenuSelectMobile"
        >
          <el-menu-item
            v-for="menuItem in menuItemList"
            :key="menuItem.route"
            :index="menuItem.route"
          >
            <el-icon><component :is="menuItem.iconComponent" /></el-icon>
            <span>{{ menuItem.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <!-- 右侧：内容区域 -->
      <div class="right-content-container">
        <router-view
          :key="route.fullPath"
          :survey-id="surveyId"
          :form-key="formKey"
          :form-items="formItems"
          :survey-title="surveyTitle"
          :survey-description="surveyDescription"
        />
      </div>
    </div>

    <!-- 预览对话框 -->
    <SurveyPreview
      :form-key="formKey"
      :form-items="formItems"
      :form-name="surveyTitle"
      :form-description="surveyDescription"
      :survey-id="surveyId"
      :theme-config="themeConfig"
      :model-value="previewVisible"
      @update:model-value="previewVisible = $event"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useWindowSize } from '@vueuse/core'
import {
  ArrowLeft,
  Edit,
  Menu,
  View,
  Setting,
  VideoPlay,
  DataAnalysis,
  DataLine
} from '@element-plus/icons-vue'
import { surveyApi, formApi, templateApi } from '@/api'
import SurveyPreview from '@/components/SurveyPreview.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式设计
const { width } = useWindowSize()
const isMobile = computed(() => width.value < 768)
const showMenuDrawer = ref(false)
const drawerWidth = computed(() => width.value < 480 ? '70%' : '280px')

// 判断是否为管理员编辑模板
const isAdminTemplateEdit = computed(() => {
  return route.query.isTemplate === 'true' && userStore.userInfo?.role === 'ADMIN'
})

const surveyId = ref(null)
const surveyTitle = ref('')
const surveyDescription = ref('')
const formKey = ref(null)
const formItems = ref([])
const previewVisible = ref(false)

// 尝试从子组件（外观页面）获取themeForm
const rawThemeForm = inject('themeForm', null)

// 转换themeForm格式为SurveyPreview需要的格式
const themeConfig = computed(() => {
  if (!rawThemeForm) return {}
  
  return {
    themeColor: rawThemeForm.themeColor,
    backgroundColor: rawThemeForm.backgroundColor,
    headImgUrl: rawThemeForm.headImgSetting ? rawThemeForm.headImgUrl : '',
    headImgHeight: rawThemeForm.headImgSetting ? rawThemeForm.headImgHeight : undefined,
    logoImg: rawThemeForm.logoSetting ? rawThemeForm.logoImg : '',
    logoPosition: rawThemeForm.logoPosition,
    logoSize: rawThemeForm.logoSetting ? rawThemeForm.logoSize : undefined,
    submitBtnText: rawThemeForm.submitBtnText,
    showTitle: rawThemeForm.showTitle,
    showDescribe: rawThemeForm.showDescribe,
    showNumber: rawThemeForm.showNumber,
    showSubmitBtn: rawThemeForm.showSubmitBtn
  }
})

// 用于存储子组件注册的方法
const editorMethods = ref({
  handlePreview: null,
  handleSave: null,
  handleSaveAsTemplate: null
})

// 提供方法注册接口给子组件
provide('registerEditorMethods', (methods) => {
  editorMethods.value = methods
})

// 菜单列表
const menuItemList = computed(() => {
  const allMenus = [
    {
      title: '编辑',
      iconComponent: Edit,
      route: 'editor'
    },
    {
      title: '逻辑',
      iconComponent: Menu,
      route: 'logic'
    },
    {
      title: '外观',
      iconComponent: View,
      route: 'theme'
    },
    {
      title: '设置',
      iconComponent: Setting,
      route: 'setting'
    },
    {
      title: '发布',
      iconComponent: VideoPlay,
      route: 'publish'
    },
    {
      title: '数据',
      iconComponent: DataAnalysis,
      route: 'data'
    },
    {
      title: '统计',
      iconComponent: DataLine,
      route: 'statistics'
    }
  ]

  // 如果是管理员编辑模板，隐藏设置、发布、数据、统计
  if (isAdminTemplateEdit.value) {
    return allMenus.filter(menu => 
      !['setting', 'publish', 'data', 'statistics'].includes(menu.route)
    )
  }

  return allMenus
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('/logic')) return 'logic'
  if (path.includes('/theme')) return 'theme'
  if (path.includes('/setting')) return 'setting'
  if (path.includes('/publish')) return 'publish'
  if (path.includes('/data')) return 'data'
  if (path.includes('/statistics')) return 'statistics'
  return 'editor'
})

// 是否显示头部操作按钮（编辑相关页面都显示预览按钮）
const showHeaderActions = computed(() => {
  const menu = activeMenu.value
  return ['editor', 'logic', 'theme', 'setting'].includes(menu)
})

// 菜单选择处理（PC端）
const handleMenuSelect = (index) => {
  const id = route.query.id
  const formKey = route.query.formKey
  const isTemplate = route.query.isTemplate === 'true'
  const templateName = route.query.templateName
  const templateDescription = route.query.templateDescription
  
  if (isTemplate && formKey) {
    // 编辑模板，保留所有模板相关的查询参数
    const query = { formKey, isTemplate: 'true' }
    if (templateName) query.templateName = templateName
    if (templateDescription !== undefined) query.templateDescription = templateDescription
    
    router.push({
      path: `/survey/edit/${index}`,
      query
    })
  } else if (id) {
    // 编辑问卷
    router.push({
      path: `/survey/edit/${index}`,
      query: { id }
    })
  }
}

// 菜单选择处理（移动端，选择后关闭抽屉）
const handleMenuSelectMobile = (index) => {
  showMenuDrawer.value = false
  handleMenuSelect(index)
}

// 加载问卷信息
const loadSurveyInfo = async () => {
  const id = route.query.id
  const formKeyParam = route.query.formKey
  const isTemplate = route.query.isTemplate === 'true'
  
  // 如果是编辑模板（通过formKey）
  if (isTemplate && formKeyParam) {
    formKey.value = formKeyParam
    surveyId.value = null
    
    // 优先使用URL参数中的模板信息
    const templateName = route.query.templateName
    const templateDescription = route.query.templateDescription
    
    if (templateName) {
      surveyTitle.value = templateName
    } else {
      surveyTitle.value = '未命名模板'
    }
    
    if (templateDescription !== undefined) {
      surveyDescription.value = templateDescription
    } else {
      surveyDescription.value = ''
    }
    
    try {
      // getTemplateDetails 返回的是 scheme，不包含 name 和 description
      // 所以主要依赖URL参数传递的信息
      
      // 加载表单项
      if (formKey.value) {
        try {
          const itemsRes = await formApi.getFormItems(formKey.value)
          if (itemsRes.code === 200 && itemsRes.data) {
            // 解析 scheme 字段，提取 vModel 和 config
            formItems.value = itemsRes.data.map(item => {
              const scheme = typeof item.scheme === 'string' 
                ? JSON.parse(item.scheme) 
                : item.scheme || {}
              
              return {
                formItemId: item.formItemId,
                type: item.type,
                label: item.label,
                vModel: scheme.vModel || item.formItemId,
                placeholder: scheme.placeholder || item.placeholder || '',
                required: scheme.required !== undefined ? scheme.required : (item.required === 1),
                disabled: scheme.disabled || false,
                readonly: scheme.readonly || false,
                defaultValue: scheme.defaultValue !== undefined ? scheme.defaultValue : (item.defaultValue || ''),
                config: scheme.config || {}
              }
            })
          }
        } catch (error) {
          // 如果加载失败，使用空数组
          formItems.value = []
        }
      }
    } catch (error) {
      ElMessage.error('加载模板信息失败')
    }
    return
  }
  
  // 如果是编辑问卷（通过surveyId）
  if (!id) {
    ElMessage.error('问卷ID不存在')
    router.back()
    return
  }

  surveyId.value = Number(id)
  
  try {
    const res = await surveyApi.getSurveyById(surveyId.value)
    if (res.code === 200 && res.data) {
      surveyTitle.value = res.data.title || '未命名问卷'
      surveyDescription.value = res.data.description || ''
    }
    
    // 加载表单配置，获取 formKey
    const configRes = await formApi.getFormConfig(surveyId.value)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      
      // 加载表单项
      if (formKey.value) {
        try {
          const itemsRes = await formApi.getFormItems(formKey.value)
          if (itemsRes.code === 200 && itemsRes.data) {
            // 解析 scheme 字段，提取 vModel 和 config
            formItems.value = itemsRes.data.map(item => {
              const scheme = typeof item.scheme === 'string' 
                ? JSON.parse(item.scheme) 
                : item.scheme || {}
              
              return {
                formItemId: item.formItemId,
                type: item.type,
                label: item.label,
                vModel: scheme.vModel || item.formItemId,
                placeholder: scheme.placeholder || item.placeholder || '',
                required: scheme.required !== undefined ? scheme.required : (item.required === 1),
                disabled: scheme.disabled || false,
                readonly: scheme.readonly || false,
                defaultValue: scheme.defaultValue !== undefined ? scheme.defaultValue : (item.defaultValue || ''),
                config: scheme.config || {}
              }
            })
          }
        } catch (error) {
          // 如果加载失败，使用空数组
          formItems.value = []
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载问卷信息失败')
  }
}

// 调用子组件方法：预览
const handlePreview = async () => {
  // 如果当前是编辑页面，调用编辑器的预览方法
  if (activeMenu.value === 'editor' && editorMethods.value.handlePreview && typeof editorMethods.value.handlePreview === 'function') {
    await editorMethods.value.handlePreview()
  } else {
    // 其他页面直接打开预览对话框
    previewVisible.value = true
  }
}

// 调用子组件方法：保存
const handleSave = async () => {
  if (editorMethods.value.handleSave && typeof editorMethods.value.handleSave === 'function') {
    await editorMethods.value.handleSave()
  }
}

// 调用子组件方法：保存为模板
const handleSaveAsTemplate = async () => {
  if (editorMethods.value.handleSaveAsTemplate && typeof editorMethods.value.handleSaveAsTemplate === 'function') {
    await editorMethods.value.handleSaveAsTemplate()
  }
}

onMounted(() => {
  loadSurveyInfo()
})
</script>

<style lang="scss" scoped>
.survey-design-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header-container {
  height: 60px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.header-card {
  height: 100%;
  border: none;
  border-radius: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  gap: 20px;
}

.header-title {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.menu-toggle-button {
  margin-left: 5px;
  font-size: 20px;
  padding: 0 10px;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
    gap: 10px;
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
  }

  .header-content::-webkit-scrollbar {
    display: none;
  }

  .header-content {
    -ms-overflow-style: none;
    scrollbar-width: none;
  }

  .back-button {
    padding: 0 5px !important;
    margin-left: -5px;
  }

  .back-button .back-text {
    display: none;
  }

  .menu-toggle-button {
    margin-left: 0;
    padding: 0 8px;
    font-size: 18px;
  }

  .header-title {
    display: none;
  }

  .header-actions {
    flex-shrink: 0;
  }

  .header-actions .el-button {
    padding: 8px 12px;
    font-size: 12px;
  }

  .header-actions .el-button span {
    display: none;
  }

  .header-actions .el-button::after {
    content: attr(data-text);
  }
}

.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.left-menu-container {
  width: 100px;
  border-right: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}

.el-menu-vertical {
  border: none;
  width: 100%;
}

/* 抽屉菜单样式 */
.drawer-menu {
  border: none;
}

.drawer-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  padding: 0 20px !important;
  display: flex;
  align-items: center;
  gap: 10px;
}

.drawer-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 8px;
}

.drawer-menu :deep(.el-menu-item span) {
  font-size: 14px;
}

.drawer-menu :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
}

@media (max-width: 768px) {
  .right-content-container {
    margin-left: 0 !important;
  }
}

.right-content-container {
  flex: 1;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>

