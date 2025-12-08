<template>
  <el-dialog
    v-model="visible"
    :title="null"
    width="100%"
    :fullscreen="true"
    :close-on-click-modal="false"
    destroy-on-close
    class="survey-preview-dialog"
    @close="handleClose"
  >
    <template #header>
      <div class="dialog-header">
        <span class="dialog-title">问卷预览</span>
      </div>
    </template>
    <div class="preview-container">
      <!-- 视图切换按钮 -->
      <div class="preview-tabs">
        <el-button
          :type="viewMode === 'mobile' ? 'primary' : ''"
          :plain="viewMode !== 'mobile'"
          @click="viewMode = 'mobile'"
        >
          <el-icon><Iphone /></el-icon>
          <span>手机</span>
        </el-button>
        <el-button
          :type="viewMode === 'desktop' ? 'primary' : ''"
          :plain="viewMode !== 'desktop'"
          @click="viewMode = 'desktop'"
        >
          <el-icon><Monitor /></el-icon>
          <span>电脑</span>
        </el-button>
      </div>

      <!-- 移动端预览 -->
      <div v-if="viewMode === 'mobile'" class="mobile-preview-wrapper">
        <div class="preview-layer">
          <div class="preview-phone">
            <div
              class="phone-content"
              :style="{
                backgroundColor: currentThemeConfig.backgroundImg 
                  ? 'transparent' 
                  : (currentThemeConfig.backgroundColor || '#ffffff'),
                backgroundImage: currentThemeConfig.backgroundImg 
                  ? `url(${getImageUrl(currentThemeConfig.backgroundImg)})` 
                  : 'none',
                backgroundSize: 'cover',
                backgroundPosition: 'center'
              }"
            >
              <div class="preview-form-container">
                <!-- Logo -->
                <div
                  v-if="currentThemeConfig.logoImg"
                  class="phone-logo"
                  :style="{ justifyContent: getLogoPosition() }"
                >
                  <img :src="getImageUrl(currentThemeConfig.logoImg)" alt="logo" />
                </div>
                <!-- 头图 -->
                <div
                  v-if="currentThemeConfig.headImgUrl"
                  class="phone-head-img"
                >
                  <img :src="getImageUrl(currentThemeConfig.headImgUrl)" alt="head" />
                </div>
                <!-- 标题 -->
                <div
                  v-if="currentThemeConfig.showTitle"
                  class="form-title"
                >
                  {{ formName || '未命名问卷' }}
                </div>
                <!-- 描述 -->
                <p
                  v-if="currentThemeConfig.showDescribe && props.formDescription"
                  class="form-description"
                >
                  {{ props.formDescription }}
                </p>
                <el-scrollbar class="form-scrollbar">
                  <SurveyFormRender
                    :form-items="formItems"
                    :form-model="previewFormModel"
                    :preview-mode="true"
                    :theme-config="currentThemeConfig"
                    :show-number="currentThemeConfig.showNumber"
                  />
                  <!-- 提交按钮 -->
                  <div
                    v-if="currentThemeConfig.showSubmitBtn"
                    class="form-submit-btn"
                  >
                    <el-button
                      type="primary"
                      :style="{
                        backgroundColor: currentThemeConfig.themeColor || '#409EFF',
                        borderColor: currentThemeConfig.themeColor || '#409EFF',
                        padding: '16px 60px',
                        fontSize: '15px',
                        height: 'auto',
                        minWidth: '240px'
                      }"
                      size="large"
                    >
                      {{ currentThemeConfig.submitBtnText || '提交' }}
                    </el-button>
                  </div>
                </el-scrollbar>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 二维码预览区域 -->
        <div v-if="showQrcode && props.formKey" class="qrcode-view">
          <p class="qrcode-title">手机扫码预览</p>
          <p class="tips-text">* 预览仅查看效果，无法提交数据</p>
          <div class="qrcode-container">
            <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="二维码" class="qrcode-image" />
            <div v-else class="qrcode-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- PC端预览 -->
      <div v-if="viewMode === 'desktop'" class="desktop-preview-wrapper">
        <el-scrollbar class="desktop-scrollbar">
          <div
            class="desktop-form-container"
            :style="{
              backgroundColor: currentThemeConfig.backgroundImg 
                ? 'transparent' 
                : (currentThemeConfig.backgroundColor || '#ffffff'),
              backgroundImage: currentThemeConfig.backgroundImg 
                ? `url(${getImageUrl(currentThemeConfig.backgroundImg)})` 
                : 'none',
              backgroundSize: 'cover',
              backgroundPosition: 'center'
            }"
          >
            <!-- Logo -->
            <div
              v-if="currentThemeConfig.logoImg"
              class="desktop-logo"
              :style="{ justifyContent: getLogoPosition() }"
            >
              <img :src="getImageUrl(currentThemeConfig.logoImg)" alt="logo" />
            </div>
            <!-- 头图 -->
            <div
              v-if="currentThemeConfig.headImgUrl"
              class="desktop-head-img"
            >
              <img :src="getImageUrl(currentThemeConfig.headImgUrl)" alt="head" />
            </div>
            <!-- 标题 -->
            <div
              v-if="currentThemeConfig.showTitle"
              class="form-title"
            >
              {{ formName || '未命名问卷' }}
            </div>
            <!-- 描述 -->
            <p
              v-if="currentThemeConfig.showDescribe && props.formDescription"
              class="form-description"
            >
              {{ props.formDescription }}
            </p>
            <!-- 表单项 -->
            <SurveyFormRender
              :form-items="formItems"
              :form-model="previewFormModel"
              :preview-mode="true"
              :theme-config="currentThemeConfig"
              :show-number="currentThemeConfig.showNumber"
            />
            <!-- 提交按钮 -->
            <div
              v-if="currentThemeConfig.showSubmitBtn"
              class="form-submit-btn"
            >
              <el-button
                type="primary"
                :style="{
                  backgroundColor: currentThemeConfig.themeColor || '#409EFF',
                  borderColor: currentThemeConfig.themeColor || '#409EFF',
                  padding: '16px 60px',
                  fontSize: '15px',
                  height: 'auto',
                  minWidth: '240px'
                }"
                size="large"
              >
                {{ currentThemeConfig.submitBtnText || '提交' }}
              </el-button>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { Iphone, Monitor, Loading } from '@element-plus/icons-vue'
import SurveyFormRender from './SurveyFormRender.vue'
import { formApi } from '@/api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  formName: {
    type: String,
    default: '未命名问卷'
  },
  formItems: {
    type: Array,
    default: () => []
  },
  formKey: {
    type: String,
    default: ''
  },
  showQrcode: {
    type: Boolean,
    default: true
  },
  surveyId: {
    type: Number,
    default: null
  },
  themeConfig: {
    type: Object,
    default: () => ({})
  },
  formDescription: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const viewMode = ref('mobile') // 'mobile' 或 'desktop'
const qrCodeUrl = ref('')
const previewFormModel = reactive({})
const themeConfigData = reactive({
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  backgroundImg: '',
  headImgUrl: '',
  logoImg: '',
  logoPosition: 'flex-start',
  submitBtnText: '提交',
  showTitle: true,
  showDescribe: true,
  showNumber: false,
  showSubmitBtn: true
})

// 合并外部传入的主题配置
const currentThemeConfig = computed(() => {
  return Object.keys(props.themeConfig).length > 0 ? props.themeConfig : themeConfigData
})

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

// 获取Logo位置样式值
const getLogoPosition = () => {
  const positionMap = {
    'flex-start': 'flex-start',
    'center': 'center',
    'flex-end': 'flex-end',
    'left': 'flex-start',
    'right': 'flex-end'
  }
  return positionMap[currentThemeConfig.value.logoPosition] || 'flex-start'
}

// 加载外观配置
const loadTheme = async () => {
  if (!props.surveyId) return
  
  try {
    const res = await formApi.getFormTheme(props.surveyId)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.themeColor) themeConfigData.themeColor = data.themeColor
      if (data.backgroundColor) themeConfigData.backgroundColor = data.backgroundColor
      if (data.backgroundImg) themeConfigData.backgroundImg = getImageUrl(data.backgroundImg)
      if (data.headImgUrl) themeConfigData.headImgUrl = getImageUrl(data.headImgUrl)
      if (data.logoImg) themeConfigData.logoImg = getImageUrl(data.logoImg)
      if (data.logoPosition) themeConfigData.logoPosition = data.logoPosition
      if (data.submitBtnText) themeConfigData.submitBtnText = data.submitBtnText
      if (data.showTitle !== undefined) themeConfigData.showTitle = data.showTitle
      if (data.showDescribe !== undefined) themeConfigData.showDescribe = data.showDescribe
      if (data.showNumber !== undefined) themeConfigData.showNumber = data.showNumber
      if (data.showSubmitBtn !== undefined) themeConfigData.showSubmitBtn = data.showSubmitBtn
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

// 监听外部传入的显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    initPreviewForm()
    if (props.showQrcode && props.formKey) {
      loadQRCode()
    }
    if (props.surveyId) {
      loadTheme()
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 初始化预览表单数据
const initPreviewForm = () => {
  // 清空之前的表单数据
  Object.keys(previewFormModel).forEach(key => {
    delete previewFormModel[key]
  })
  
  // 初始化表单模型
  props.formItems.forEach(item => {
    if (item.type === 'CHECKBOX' || item.type === 'SORT') {
      previewFormModel[item.vModel] = item.defaultValue && Array.isArray(item.defaultValue) 
        ? item.defaultValue 
        : []
    } else if (item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
      previewFormModel[item.vModel] = item.defaultValue && Array.isArray(item.defaultValue)
        ? item.defaultValue
        : []
    } else if (item.type === 'SLIDER') {
      const numValue = item.defaultValue !== null && item.defaultValue !== undefined && item.defaultValue !== ''
        ? Number(item.defaultValue)
        : (item.config?.min || 0)
      previewFormModel[item.vModel] = isNaN(numValue) ? (item.config?.min || 0) : numValue
    } else if (item.type === 'NUMBER') {
      const numValue = item.defaultValue !== null && item.defaultValue !== undefined && item.defaultValue !== ''
        ? Number(item.defaultValue)
        : undefined
      previewFormModel[item.vModel] = isNaN(numValue) ? undefined : numValue
    } else if (item.type === 'DIVIDER' || item.type === 'IMAGE' || item.type === 'IMAGE_CAROUSEL') {
      // 这些类型不需要绑定表单模型
      previewFormModel[item.vModel] = null
    } else {
      previewFormModel[item.vModel] = item.defaultValue !== null && item.defaultValue !== undefined ? item.defaultValue : ''
    }
  })
}

// 加载二维码
const loadQRCode = async () => {
  if (!props.formKey) {
    return
  }
  
  try {
    const QRCode = (await import('qrcode')).default
    // 生成预览链接，实际使用时应该指向真实的问卷填写页面
    const url = `${window.location.protocol}//${window.location.host}/survey/fill?key=${props.formKey}`
    qrCodeUrl.value = await QRCode.toDataURL(url, {
      width: 200,
      margin: 1,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
  } catch (error) {
    console.error('生成二维码失败:', error)
    qrCodeUrl.value = ''
  }
}

const handleClose = () => {
  visible.value = false
  viewMode.value = 'mobile'
  qrCodeUrl.value = ''
}
</script>

<style lang="scss" scoped>
.survey-preview-dialog {
  :deep(.el-dialog) {
    margin: 0;
    max-width: 100%;
    height: 100vh;
    border-radius: 0;
  }
  
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
    border-bottom: 1px solid #ebeef5;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
    margin: 0;
    height: calc(100vh - 60px);
    overflow: hidden;
    background-color: #f5f7fa;
  }
  
}

.dialog-header {
  padding: 15px 20px;
  background: white;
  height: 60px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  
  .dialog-title {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }
}

.preview-container {
  width: 100%;
  height: 100%;
  position: relative;
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
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 40px;
  position: relative;
  height: calc(100vh - 120px);
  padding: 20px;
  overflow: auto;
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
  background: white;
  border-radius: 32px;
  overflow: hidden;
  position: relative;
  padding-top: 35px;
  box-sizing: border-box;
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

.qrcode-view {
  position: absolute;
  top: 80px;
  right: 60px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
  min-width: 200px;
  
  .qrcode-title {
    font-weight: bold;
    font-size: 16px;
    margin: 0 0 10px 0;
    color: #303133;
  }
  
  .tips-text {
    font-size: 12px;
    color: #909399;
    margin: 10px 0;
  }
  
  .qrcode-container {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 10px;
    
    .qrcode-image {
      width: 180px;
      height: 180px;
      border-radius: 8px;
      border: 1px solid #ebeef5;
    }
    
    .qrcode-loading {
      width: 180px;
      height: 180px;
      display: flex;
      justify-content: center;
      align-items: center;
      
      .el-icon {
        font-size: 32px;
        color: #409eff;
      }
    }
  }
}

.desktop-preview-wrapper {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  height: calc(100vh - 120px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  
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

// 响应式设计
@media (max-width: 1400px) {
  .qrcode-view {
    right: 20px;
    top: 60px;
  }
}

@media (max-width: 1200px) {
  .qrcode-view {
    position: static;
    margin-top: 20px;
    right: auto;
    top: auto;
  }
  
  .mobile-preview-wrapper {
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 10px;
  }
}

@media (max-width: 768px) {
  .dialog-header {
    padding: 10px 15px;
    height: 50px;
    
    .dialog-title {
      font-size: 14px;
    }
  }
  
  .preview-tabs {
    padding: 20px 0 15px 0;
    
    .el-button {
      padding: 8px 25px;
      font-size: 13px;
    }
  }
  
  .mobile-preview-wrapper {
    height: calc(100vh - 100px);
    padding: 10px;
  }
  
  .preview-phone {
    width: 300px;
    height: 600px;
    border-radius: 30px;
    border-width: 6px;
    
    &::before {
      width: 100px;
      height: 20px;
    }
    
    &::after {
      width: 120px;
      height: 4px;
    }
  }
  
  .phone-content {
    padding-top: 25px;
    border-radius: 24px;
  }
  
  .preview-form-container {
    padding: 15px;
    
    .form-title {
      font-size: 18px;
      margin-bottom: 15px;
      padding-bottom: 10px;
    }
  }
  
  .qrcode-view {
    width: 100%;
    max-width: 200px;
    padding: 15px;
    
    .qrcode-title {
      font-size: 14px;
    }
    
    .tips-text {
      font-size: 11px;
    }
    
    .qrcode-container .qrcode-image {
      width: 150px;
      height: 150px;
    }
  }
  
  .desktop-preview-wrapper {
    height: calc(100vh - 100px);
  }
  
  .desktop-form-container {
    padding: 20px;
    
    .form-title {
      font-size: 22px;
      margin-bottom: 20px;
      padding-bottom: 15px;
    }
  }
}
</style>
