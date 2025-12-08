<template>
  <div class="theme-container">
    <!-- 左侧：主题选择 -->
    <div class="left-container">
      <el-scrollbar class="left-scrollbar">
        <div class="theme-selection">
          <p class="theme-title">外观主题</p>
          
          <!-- 风格筛选 -->
          <div class="style-filter">
            <el-radio-group v-model="activeStyle" size="small" @change="handleStyleChange">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="商务">商务</el-radio-button>
              <el-radio-button label="简约">简约</el-radio-button>
              <el-radio-button label="可爱">可爱</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 主题预览卡片 -->
          <div class="theme-list">
            <div
              v-for="theme in filteredThemeList"
              :key="theme.id"
              :class="['theme-card', { active: activeThemeId === theme.id }]"
              @click="handleThemeSelect(theme)"
            >
              <div
                class="theme-preview"
                :style="{
                  background: theme.backgroundColor || '#409EFF',
                  color: theme.textColor || '#fff'
                }"
              >
                <div class="theme-preview-text">{{ theme.name || '主题' }}</div>
              </div>
              <div class="theme-name">{{ theme.name }}</div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <!-- 中间：实时预览 -->
    <div class="center-container">
      <div class="preview-wrapper">
        <!-- 预览模式切换 -->
        <div class="preview-tabs">
          <el-button
            :type="previewMode === 'mobile' ? 'primary' : ''"
            :plain="previewMode !== 'mobile'"
            @click="previewMode = 'mobile'"
          >
            <el-icon><Iphone /></el-icon>
            <span>手机</span>
          </el-button>
          <el-button
            :type="previewMode === 'desktop' ? 'primary' : ''"
            :plain="previewMode !== 'desktop'"
            @click="previewMode = 'desktop'"
          >
            <el-icon><Monitor /></el-icon>
            <span>电脑</span>
          </el-button>
        </div>

        <!-- 移动端预览 -->
        <div v-if="previewMode === 'mobile'" class="mobile-preview-wrapper">
          <div class="preview-layer">
            <div class="preview-phone">
              <div
                class="phone-content"
                :style="{
                  backgroundColor: themeForm.backgroundImg 
                    ? 'transparent' 
                    : (themeForm.backgroundColor || '#ffffff'),
                  backgroundImage: themeForm.backgroundImg 
                    ? `url(${getImageUrl(themeForm.backgroundImg)})` 
                    : 'none',
                  backgroundSize: 'cover',
                  backgroundPosition: 'center'
                }"
              >
                <div class="preview-form-container">
                  <!-- Logo -->
                  <div
                    v-if="themeForm.logoImg"
                    class="phone-logo"
                    :style="{ justifyContent: getLogoPosition() }"
                  >
                    <img :src="getImageUrl(themeForm.logoImg)" alt="logo" />
                  </div>
                  <!-- 头图 -->
                  <div
                    v-if="themeForm.headImgUrl"
                    class="phone-head-img"
                  >
                    <img :src="getImageUrl(themeForm.headImgUrl)" alt="head" />
                  </div>
                  <!-- 标题 -->
                  <div
                    v-if="themeForm.showTitle"
                    class="form-title"
                  >
                    {{ surveyTitle || '未命名问卷' }}
                  </div>
                  <!-- 描述 -->
                  <p
                    v-if="themeForm.showDescribe && surveyDescription"
                    class="form-description"
                  >
                    {{ surveyDescription }}
                  </p>
                  <el-scrollbar class="form-scrollbar">
                    <!-- 表单项 -->
                    <SurveyFormRender
                      :form-items="formItems"
                      :form-model="previewFormModel"
                      :preview-mode="true"
                      :theme-config="themeForm"
                      :show-number="themeForm.showNumber"
                    />
                    <!-- 提交按钮 -->
                    <div
                      v-if="themeForm.showSubmitBtn"
                      class="form-submit-btn"
                    >
                      <el-button
                        type="primary"
                        :style="{
                          backgroundColor: themeForm.themeColor || '#409EFF',
                          borderColor: themeForm.themeColor || '#409EFF',
                          padding: '16px 60px',
                          fontSize: '15px',
                          height: 'auto',
                          minWidth: '240px'
                        }"
                        size="large"
                      >
                        {{ themeForm.submitBtnText || '提交' }}
                      </el-button>
                    </div>
                  </el-scrollbar>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- PC端预览 -->
        <div v-else class="desktop-preview-wrapper">
          <el-scrollbar class="desktop-scrollbar">
            <div
              class="desktop-form-container"
              :style="{
                backgroundColor: themeForm.backgroundImg 
                  ? 'transparent' 
                  : (themeForm.backgroundColor || '#ffffff'),
                backgroundImage: themeForm.backgroundImg 
                  ? `url(${getImageUrl(themeForm.backgroundImg)})` 
                  : 'none',
                backgroundSize: 'cover',
                backgroundPosition: 'center'
              }"
            >
              <!-- Logo -->
              <div
                v-if="themeForm.logoImg"
                class="desktop-logo"
                :style="{ justifyContent: getLogoPosition() }"
              >
                <img :src="getImageUrl(themeForm.logoImg)" alt="logo" />
              </div>
              <!-- 头图 -->
              <div
                v-if="themeForm.headImgUrl"
                class="desktop-head-img"
              >
                <img :src="getImageUrl(themeForm.headImgUrl)" alt="head" />
              </div>
              <!-- 标题 -->
              <div
                v-if="themeForm.showTitle"
                class="form-title"
              >
                {{ surveyTitle || '未命名问卷' }}
              </div>
              <!-- 描述 -->
              <p
                v-if="themeForm.showDescribe && surveyDescription"
                class="form-description"
              >
                {{ surveyDescription }}
              </p>
              <!-- 表单项 -->
              <SurveyFormRender
                :form-items="formItems"
                :form-model="previewFormModel"
                :preview-mode="true"
                :theme-config="themeForm"
                :show-number="themeForm.showNumber"
              />
              <!-- 提交按钮 -->
              <div
                v-if="themeForm.showSubmitBtn"
                class="form-submit-btn"
              >
                <el-button
                  type="primary"
                  :style="{
                    backgroundColor: themeForm.themeColor || '#409EFF',
                    borderColor: themeForm.themeColor || '#409EFF',
                    padding: '16px 60px',
                    fontSize: '15px',
                    height: 'auto',
                    minWidth: '240px'
                  }"
                  size="large"
                >
                  {{ themeForm.submitBtnText || '提交' }}
                </el-button>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </div>
    </div>

    <!-- 右侧：设置项 -->
    <div class="right-container">
      <el-scrollbar class="right-scrollbar">
        <div class="settings-panel">
          <p class="settings-title">外观设置</p>

          <!-- Logo设置 -->
          <div class="setting-item">
            <div class="setting-header">
              <span class="setting-label">添加logo</span>
              <el-switch v-model="themeForm.logoSetting" @change="handleSettingChange" />
            </div>
            <div v-if="themeForm.logoSetting" class="setting-content">
              <div class="setting-sub-item">
                <span class="setting-sub-label">logo设置</span>
                <div class="logo-preview">
                  <img v-if="themeForm.logoImg" :src="themeForm.logoImg" alt="logo" />
                </div>
                <el-upload
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :on-success="handleLogoUpload"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <el-button size="small" type="text">上传Logo</el-button>
                </el-upload>
              </div>
              <div class="setting-sub-item">
                <span class="setting-sub-label">logo位置</span>
                <el-radio-group v-model="themeForm.logoPosition" size="small" @change="handleSettingChange">
                  <el-radio-button label="flex-start">左对齐</el-radio-button>
                  <el-radio-button label="center">居中</el-radio-button>
                  <el-radio-button label="flex-end">右对齐</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </div>

          <!-- 头图设置 -->
          <div class="setting-item">
            <div class="setting-header">
              <span class="setting-label">头图设置</span>
              <el-switch v-model="themeForm.headImgSetting" @change="handleSettingChange" />
            </div>
            <div v-if="themeForm.headImgSetting" class="setting-content">
              <div class="setting-sub-item">
                <span class="setting-sub-label">头图设置</span>
                <div class="head-img-preview">
                  <img v-if="themeForm.headImgUrl" :src="themeForm.headImgUrl" alt="head" />
                </div>
                <el-upload
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :on-success="handleHeadImgUpload"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <el-button size="small" type="text">上传头图</el-button>
                </el-upload>
              </div>
            </div>
          </div>

          <!-- 背景设置 -->
          <div class="setting-item">
            <div class="setting-header">
              <span class="setting-label">背景设置</span>
              <el-switch v-model="themeForm.backgroundSetting" @change="handleSettingChange" />
            </div>
            <div v-if="themeForm.backgroundSetting" class="setting-content">
              <div class="setting-sub-item">
                <span class="setting-sub-label">背景类型</span>
                <el-radio-group v-model="themeForm.backgroundType" size="small" @change="handleSettingChange">
                  <el-radio-button label="color">颜色</el-radio-button>
                  <el-radio-button label="img">图片</el-radio-button>
                </el-radio-group>
              </div>
              <div v-if="themeForm.backgroundType === 'color'" class="setting-sub-item">
                <span class="setting-sub-label">背景颜色</span>
                <el-color-picker v-model="themeForm.backgroundColor" @change="handleSettingChange" />
              </div>
              <div v-if="themeForm.backgroundType === 'img'" class="setting-sub-item">
                <span class="setting-sub-label">背景图片</span>
                <el-input
                  v-model="themeForm.backgroundImg"
                  placeholder="请输入图片URL"
                  size="small"
                  @change="handleSettingChange"
                />
                <el-upload
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :on-success="handleBackgroundImgUpload"
                  :show-file-list="false"
                  accept="image/*"
                >
                  <el-button size="small" type="text">上传背景</el-button>
                </el-upload>
              </div>
            </div>
          </div>

          <!-- 按钮设置 -->
          <div class="setting-item">
            <div class="setting-header">
              <span class="setting-label">按钮设置</span>
              <el-switch v-model="themeForm.btnSetting" @change="handleSettingChange" />
            </div>
            <div v-if="themeForm.btnSetting" class="setting-content">
              <div class="setting-sub-item">
                <span class="setting-sub-label">显示提交按钮</span>
                <el-switch v-model="themeForm.showSubmitBtn" @change="handleSettingChange" />
              </div>
              <div v-if="themeForm.showSubmitBtn" class="setting-sub-item">
                <span class="setting-sub-label">按钮提示文字</span>
                <el-input
                  v-model="themeForm.submitBtnText"
                  placeholder="提交"
                  size="small"
                  @change="handleSettingChange"
                />
              </div>
              <div class="setting-sub-item">
                <span class="setting-sub-label">主题颜色</span>
                <el-color-picker v-model="themeForm.themeColor" @change="handleSettingChange" />
              </div>
            </div>
          </div>

          <!-- 显示选项 -->
          <div class="setting-item">
            <div class="setting-header">
              <span class="setting-label">显示标题</span>
              <el-switch v-model="themeForm.showTitle" @change="handleSettingChange" />
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-header">
              <span class="setting-label">显示描述</span>
              <el-switch v-model="themeForm.showDescribe" @change="handleSettingChange" />
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-header">
              <span class="setting-label">显示序号</span>
              <el-switch v-model="themeForm.showNumber" @change="handleSettingChange" />
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Monitor } from '@element-plus/icons-vue'
import { formApi, surveyApi } from '@/api'
import { getToken } from '@/utils/auth'
import SurveyFormRender from '@/components/SurveyFormRender.vue'

const route = useRoute()
const props = defineProps({
  surveyId: {
    type: Number,
    default: null
  },
  formKey: {
    type: String,
    default: ''
  },
  formItems: {
    type: Array,
    default: () => []
  },
  surveyTitle: {
    type: String,
    default: ''
  },
  surveyDescription: {
    type: String,
    default: ''
  }
})

const surveyId = ref(null)
const surveyTitle = ref('')
const surveyDescription = ref('')
const formKey = ref(null)
const formItems = ref([])
const previewMode = ref('mobile')
const activeStyle = ref('')
const activeThemeId = ref(null)

// 默认主题列表
const defaultThemeList = [
  { id: 1, name: '商务蓝', style: '商务', backgroundColor: '#2672FF', textColor: '#fff' },
  { id: 2, name: '简约灰', style: '简约', backgroundColor: '#EAEAEA', textColor: '#333' },
  { id: 3, name: '活力橙', style: '可爱', backgroundColor: '#FF6D56', textColor: '#fff' },
  { id: 4, name: '清新绿', style: '简约', backgroundColor: '#00BF6F', textColor: '#fff' },
  { id: 5, name: '优雅紫', style: '商务', backgroundColor: '#7464FF', textColor: '#fff' },
  { id: 6, name: '经典黑', style: '商务', backgroundColor: '#484848', textColor: '#fff' }
]

const themeList = ref([...defaultThemeList])

// 筛选后的主题列表
const filteredThemeList = computed(() => {
  if (!activeStyle.value) return themeList.value
  return themeList.value.filter(theme => theme.style === activeStyle.value)
})

// 外观设置表单
const themeForm = reactive({
  surveyId: null,
  themeId: null,
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  backgroundImg: '',
  backgroundType: 'color',
  backgroundSetting: false,
  headImgUrl: '',
  headImgSetting: false,
  logoImg: '',
  logoSetting: false,
  logoPosition: 'flex-start',
  submitBtnText: '提交',
  showTitle: true,
  showDescribe: true,
  showNumber: false,
  showSubmitBtn: true,
  btnSetting: true
})

// 预览表单数据
const previewFormModel = reactive({})

// 上传配置
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_APP_BASE_API}/file/upload`
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

// 获取Logo位置样式值
const getLogoPosition = () => {
  return themeForm.logoPosition || 'flex-start'
}

// 获取后端服务器地址（用于构建图片URL）
const getBackendBaseUrl = () => {
  // 从 VITE_APP_BASE_API 提取后端地址
  const baseApi = import.meta.env.VITE_APP_BASE_API
  const proxyTarget = import.meta.env.VITE_SERVER_PROXY_TARGET
  
  // 如果 baseApi 是相对路径，使用 proxyTarget
  if (baseApi.startsWith('/')) {
    return proxyTarget
  }
  // 如果 baseApi 是完整URL，提取协议和主机
  try {
    const url = new URL(baseApi)
    return `${url.protocol}//${url.host}`
  } catch {
    return proxyTarget
  }
}

// 将相对路径转换为完整的后端URL
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果已经是完整URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是相对路径（以 /upload/ 开头），拼接后端地址
  if (imageUrl.startsWith('/upload/')) {
    return `${getBackendBaseUrl()}${imageUrl}`
  }
  // 其他情况，添加 /upload/ 前缀
  return `${getBackendBaseUrl()}/upload/${imageUrl}`
}

// 将完整URL转换回相对路径（用于保存到后端）
const getRelativeImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果已经是相对路径，直接返回
  if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是完整URL，提取相对路径部分
  try {
    const url = new URL(imageUrl)
    const path = url.pathname
    // 如果是 /upload/ 开头的路径，返回完整路径
    if (path.startsWith('/upload/')) {
      return path
    }
    // 如果路径中包含 upload，提取 upload 之后的部分
    const uploadIndex = path.indexOf('/upload/')
    if (uploadIndex !== -1) {
      return path.substring(uploadIndex)
    }
    // 其他情况，返回路径部分
    return path
  } catch {
    // 解析失败，返回原值
    return imageUrl
  }
}

// 加载问卷信息和表单数据
const loadData = async () => {
  // 优先使用 props 传入的数据
  if (props.surveyId) {
    surveyId.value = props.surveyId
    surveyTitle.value = props.surveyTitle || ''
    surveyDescription.value = props.surveyDescription || ''
    formKey.value = props.formKey || ''
    formItems.value = props.formItems || []
    if (formItems.value.length > 0) {
      initPreviewForm()
    }
  } else {
    // 如果没有 props，从路由参数加载
    const id = route.query.id
    if (!id) return

    surveyId.value = Number(id)
    
    try {
      // 加载问卷信息
      const surveyRes = await surveyApi.getSurveyById(surveyId.value)
      if (surveyRes.code === 200 && surveyRes.data) {
        surveyTitle.value = surveyRes.data.title || ''
        surveyDescription.value = surveyRes.data.description || ''
      }

      // 加载表单配置
      const configRes = await formApi.getFormConfig(surveyId.value)
      if (configRes.code === 200 && configRes.data) {
        formKey.value = configRes.data.formKey
        
        // 加载表单项
        if (formKey.value) {
          const itemsRes = await formApi.getFormItems(formKey.value)
          if (itemsRes.code === 200 && itemsRes.data) {
            formItems.value = itemsRes.data
            initPreviewForm()
          }
        }
      }
    } catch (error) {
      ElMessage.error('加载数据失败')
    }
  }

  themeForm.surveyId = surveyId.value
  
  // 加载外观设置
  await loadTheme()
}

// 加载外观设置
const loadTheme = async () => {
  try {
    const res = await formApi.getFormTheme(surveyId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.themeId) {
        activeThemeId.value = data.themeId
      }
      if (data.themeColor) themeForm.themeColor = data.themeColor
      if (data.backgroundColor) {
        themeForm.backgroundColor = data.backgroundColor
        themeForm.backgroundSetting = true
      }
      if (data.backgroundImg) {
        themeForm.backgroundImg = getImageUrl(data.backgroundImg)
        themeForm.backgroundType = 'img'
        themeForm.backgroundSetting = true
      }
      if (data.headImgUrl) {
        themeForm.headImgUrl = getImageUrl(data.headImgUrl)
        themeForm.headImgSetting = true
      }
      if (data.logoImg) {
        themeForm.logoImg = getImageUrl(data.logoImg)
        themeForm.logoSetting = true
      }
      if (data.logoPosition) themeForm.logoPosition = data.logoPosition
      if (data.submitBtnText) {
        themeForm.submitBtnText = data.submitBtnText
        themeForm.btnSetting = true
      }
      if (data.showTitle !== undefined) themeForm.showTitle = data.showTitle
      if (data.showDescribe !== undefined) themeForm.showDescribe = data.showDescribe
      if (data.showNumber !== undefined) themeForm.showNumber = data.showNumber
      if (data.showSubmitBtn !== undefined) themeForm.showSubmitBtn = data.showSubmitBtn
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

// 初始化预览表单
const initPreviewForm = () => {
  Object.keys(previewFormModel).forEach(key => {
    delete previewFormModel[key]
  })

  formItems.value.forEach(item => {
    if (item.type === 'CHECKBOX' || item.type === 'SORT' || item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
      previewFormModel[item.vModel] = []
    } else if (item.type === 'SLIDER' || item.type === 'NUMBER') {
      previewFormModel[item.vModel] = item.config?.min || 0
    } else if (item.type !== 'DIVIDER' && item.type !== 'IMAGE' && item.type !== 'IMAGE_CAROUSEL') {
      previewFormModel[item.vModel] = item.defaultValue || ''
    }
  })
}

// 保存外观设置（防抖）
let saveTimer = null
const saveTheme = () => {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  saveTimer = setTimeout(async () => {
    if (!surveyId.value) return

    try {
      const themeData = {
        surveyId: surveyId.value,
        themeId: activeThemeId.value,
        themeColor: themeForm.themeColor,
        backgroundColor: themeForm.backgroundSetting && themeForm.backgroundType === 'color' 
          ? themeForm.backgroundColor 
          : '',
        backgroundImg: themeForm.backgroundSetting && themeForm.backgroundType === 'img' 
          ? getRelativeImageUrl(themeForm.backgroundImg) 
          : '',
        headImgUrl: themeForm.headImgSetting ? getRelativeImageUrl(themeForm.headImgUrl) : '',
        logoImg: themeForm.logoSetting ? getRelativeImageUrl(themeForm.logoImg) : '',
        logoPosition: themeForm.logoPosition,
        submitBtnText: themeForm.btnSetting ? themeForm.submitBtnText : '提交',
        showTitle: themeForm.showTitle,
        showDescribe: themeForm.showDescribe,
        showNumber: themeForm.showNumber,
        showSubmitBtn: themeForm.showSubmitBtn
      }
      const res = await formApi.saveFormTheme(themeData)
      if (res.code === 200) {
        // 静默保存，不显示提示
      }
    } catch (error) {
      console.error('保存失败:', error)
    }
  }, 500)
}

// 设置变更处理
const handleSettingChange = () => {
  saveTheme()
}

// 风格变更
const handleStyleChange = () => {
  // 风格变更只影响主题列表筛选，不需要保存
}

// 主题选择
const handleThemeSelect = (theme) => {
  activeThemeId.value = theme.id
  themeForm.themeColor = theme.backgroundColor
  themeForm.backgroundColor = theme.backgroundColor
  themeForm.backgroundType = 'color'
  themeForm.backgroundSetting = true
  saveTheme()
}

// Logo上传成功
const handleLogoUpload = (response) => {
  if (response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    themeForm.logoImg = getImageUrl(imageUrl)
    themeForm.logoSetting = true
    saveTheme()
  }
}

// 头图上传成功
const handleHeadImgUpload = (response) => {
  if (response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    themeForm.headImgUrl = getImageUrl(imageUrl)
    themeForm.headImgSetting = true
    saveTheme()
  }
}

// 背景图片上传成功
const handleBackgroundImgUpload = (response) => {
  if (response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    themeForm.backgroundImg = getImageUrl(imageUrl)
    themeForm.backgroundType = 'img'
    themeForm.backgroundSetting = true
    saveTheme()
  }
}

// 监听表单项变化，重新初始化预览
watch(() => formItems.value.length, () => {
  initPreviewForm()
})

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.theme-container {
  width: 100%;
  height: 100%;
  display: flex;
  background: #f5f5f5;
  overflow: hidden;
}

// 左侧：主题选择
.left-container {
  width: 280px;
  background: #fff;
  border-right: 1px solid #ebeef5;
  flex-shrink: 0;
}

.left-scrollbar {
  height: 100%;
}

.theme-selection {
  padding: 20px;
}

.theme-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 15px;
}

.style-filter {
  margin-bottom: 20px;
}

.theme-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.theme-card {
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 8px;
  transition: all 0.3s;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    border-color: #409EFF;
    background: #ecf5ff;
  }
}

.theme-preview {
  width: 100%;
  height: 80px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.theme-preview-text {
  font-size: 14px;
  font-weight: 500;
}

.theme-name {
  font-size: 12px;
  text-align: center;
  color: #606266;
}

// 中间：实时预览
.center-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f7fa;
}

.preview-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-tabs {
  display: flex;
  justify-content: center;
  gap: 0;
  padding: 30px 0 20px 0;
  background: transparent;
  border: none;
  box-shadow: none;
  flex-shrink: 0;

  .el-button {
    padding: 10px 40px;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    border-radius: 0;
    border: 1px solid #e4e7ed;
    background: white;
    
    &:first-child {
      border-top-left-radius: 8px;
      border-bottom-left-radius: 8px;
      border-right: none;
    }
    
    &:last-child {
      border-top-right-radius: 8px;
      border-bottom-right-radius: 8px;
    }
    
    &.el-button--primary {
      background: #409eff;
      color: white;
      border-color: #409eff;
    }
    
    span {
      margin-left: 4px;
    }
  }
}

.mobile-preview-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  overflow: auto;
  min-height: 0;
}

.preview-layer {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  max-width: 500px;
}

.preview-phone {
  width: 372px;
  height: 744px;
  background: #fff;
  border: 8px solid #1a1a1a;
  border-radius: 40px;
  padding: 0;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: 50%;
    transform: translateX(-50%);
    width: 120px;
    height: 25px;
    background: #1a1a1a;
    border-radius: 0 0 15px 15px;
    z-index: 10;
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: 10px;
    left: 50%;
    transform: translateX(-50%);
    width: 150px;
    height: 5px;
    background: #333;
    border-radius: 5px;
    z-index: 10;
  }
}

.phone-content {
  width: 100%;
  height: 100%;
  border-radius: 32px;
  overflow: hidden;
  position: relative;
  padding-top: 35px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.preview-form-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  
  .phone-logo {
    padding: 15px;
    display: flex;
    flex-shrink: 0;

    img {
      max-width: 100px;
      max-height: 40px;
    }
  }

  .phone-head-img {
    width: 100%;
    flex-shrink: 0;

    img {
      width: 100%;
      height: auto;
      display: block;
    }
  }

  .form-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    text-align: center;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
  }
  
  .form-description {
    font-size: 14px;
    color: #606266;
    margin-bottom: 15px;
    line-height: 1.6;
    flex-shrink: 0;
  }
  
  .form-scrollbar {
    flex: 1;
    overflow: hidden;
    min-height: 0;
  }
  
  .form-submit-btn {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

.desktop-preview-wrapper {
  flex: 1;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
  height: 100%;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  min-height: 0;
  
  .desktop-scrollbar {
    height: 100%;
  }
}

.desktop-form-container {
  padding: 40px;
  min-height: 500px;
  
  .desktop-logo {
    padding: 20px 0;
    display: flex;
    margin-bottom: 20px;

    img {
      max-width: 150px;
      max-height: 60px;
    }
  }

  .desktop-head-img {
    width: 100%;
    margin-bottom: 30px;

    img {
      width: 100%;
      height: auto;
      display: block;
      max-height: 300px;
      object-fit: cover;
    }
  }

  .form-title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    text-align: center;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 2px solid #ebeef5;
  }
  
  .form-description {
    font-size: 14px;
    color: #606266;
    margin-bottom: 30px;
    line-height: 1.6;
    text-align: center;
  }
  
  .form-submit-btn {
    margin-top: 40px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

// 右侧：设置项
.right-container {
  width: 320px;
  background: #fff;
  border-left: 1px solid #ebeef5;
  flex-shrink: 0;
}

.right-scrollbar {
  height: 100%;
}

.settings-panel {
  padding: 20px;
}

.settings-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 20px;
}

.setting-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }
}

.setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.setting-label {
  font-size: 14px;
  color: #303133;
}

.setting-content {
  padding-left: 10px;
}

.setting-sub-item {
  margin-bottom: 15px;

  &:last-child {
    margin-bottom: 0;
  }
}

.setting-sub-label {
  display: block;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.logo-preview,
.head-img-preview {
  margin-bottom: 8px;

  img {
    max-width: 100px;
    max-height: 60px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
  }
}
</style>
