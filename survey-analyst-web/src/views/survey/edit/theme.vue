<template>
  <div class="theme-container">
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
                  backgroundColor: themeForm.backgroundColor || '#ffffff'
                }"
              >
                <div class="preview-form-container">
                  <!-- Logo -->
                  <div
                    v-if="themeForm.logoSetting && themeForm.logoImg"
                    class="phone-logo"
                    :style="{
                      justifyContent: getLogoPosition(),
                      '--logo-size': `${themeForm.logoSize || 60}px`
                    }"
                  >
                    <img :src="getImageUrl(themeForm.logoImg)" alt="logo" />
                  </div>
                  <!-- 头图 -->
                  <div
                    v-if="themeForm.headImgSetting && themeForm.headImgUrl"
                    class="phone-head-img"
                    :style="{
                      '--head-img-height': `${themeForm.headImgHeight || 150}px`
                    }"
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
                          fontSize: `${themeForm.btnFontSize || 15}px`,
                          width: `${themeForm.btnWidth || 240}px`,
                          height: `${themeForm.btnHeight || 48}px`,
                          minWidth: `${themeForm.btnWidth || 240}px`
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
                backgroundColor: themeForm.backgroundColor || '#ffffff'
              }"
            >
              <!-- Logo -->
              <div
                v-if="themeForm.logoSetting && themeForm.logoImg"
                class="desktop-logo"
                :style="{
                  justifyContent: getLogoPosition(),
                  '--logo-size': `${themeForm.logoSize || 80}px`
                }"
              >
                <img :src="getImageUrl(themeForm.logoImg)" alt="logo" />
              </div>
              <!-- 头图 -->
              <div
                v-if="themeForm.headImgSetting && themeForm.headImgUrl"
                class="desktop-head-img"
                :style="{
                  '--head-img-height': `${themeForm.headImgHeight || 200}px`
                }"
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
                    fontSize: `${themeForm.btnFontSize || 15}px`,
                    width: `${themeForm.btnWidth || 240}px`,
                    height: `${themeForm.btnHeight || 48}px`,
                    minWidth: `${themeForm.btnWidth || 240}px`
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
          <el-form
            :model="themeForm"
            label-width="130px"
            label-position="left"
            class="settings-form"
          >
            <!-- 显示选项 -->
            <el-form-item label="显示标题">
              <el-switch v-model="themeForm.showTitle" @change="handleSettingChange" />
            </el-form-item>

            <el-form-item label="显示描述">
              <el-switch v-model="themeForm.showDescribe" @change="handleSettingChange" />
            </el-form-item>

            <el-form-item label="显示序号">
              <el-switch v-model="themeForm.showNumber" @change="handleSettingChange" />
            </el-form-item>

            <el-divider />

            <!-- Logo设置 -->
            <el-form-item label="添加logo">
              <el-switch v-model="themeForm.logoSetting" @change="handleSettingChange" />
            </el-form-item>
            <template v-if="themeForm.logoSetting">
              <el-form-item label="logo设置">
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
              </el-form-item>
              <el-form-item label="logo位置">
                <el-radio-group v-model="themeForm.logoPosition" size="small" @change="handleSettingChange">
                  <el-radio-button label="flex-start">左对齐</el-radio-button>
                  <el-radio-button label="center">居中</el-radio-button>
                  <el-radio-button label="flex-end">右对齐</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="logo大小">
                <el-input-number
                  v-model="themeForm.logoSize"
                  :min="10"
                  :max="200"
                  :step="1"
                  style="width: 100%"
                  @change="handleSettingChange"
                />
              </el-form-item>
            </template>

            <el-divider />

            <!-- 头图设置 -->
            <el-form-item label="头图设置">
              <el-switch v-model="themeForm.headImgSetting" @change="handleSettingChange" />
            </el-form-item>
            <template v-if="themeForm.headImgSetting">
              <el-form-item label="头图设置">
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
              </el-form-item>
              <el-form-item label="头图高度">
                <el-input-number
                  v-model="themeForm.headImgHeight"
                  :min="50"
                  :max="500"
                  :step="10"
                  style="width: 100%"
                  @change="handleSettingChange"
                />
              </el-form-item>
            </template>

            <el-divider />

            <!-- 按钮设置 -->
            <div class="section-title">按钮设置</div>
            <el-form-item label="按钮文字">
              <el-input
                v-model="themeForm.submitBtnText"
                placeholder="提交"
                @change="handleSettingChange"
              />
            </el-form-item>
            <el-form-item label="主题颜色">
              <el-color-picker v-model="themeForm.themeColor" @change="handleSettingChange" />
            </el-form-item>
            <el-form-item label="字体大小（px）">
              <el-input-number
                v-model="themeForm.btnFontSize"
                :min="10"
                :max="50"
                :step="1"
                style="width: 100%"
                @change="handleSettingChange"
              />
            </el-form-item>
            <el-form-item label="按钮宽度（px）">
              <el-input-number
                v-model="themeForm.btnWidth"
                :min="100"
                :max="1000"
                :step="10"
                style="width: 100%"
                @change="handleSettingChange"
              />
            </el-form-item>
            <el-form-item label="按钮高度（px）">
              <el-input-number
                v-model="themeForm.btnHeight"
                :min="30"
                :max="200"
                :step="2"
                style="width: 100%"
                @change="handleSettingChange"
              />
            </el-form-item>
          </el-form>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, provide } from 'vue'
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

// 外观设置表单
const themeForm = reactive({
  surveyId: null,
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  backgroundSetting: false,
  headImgUrl: '',
  headImgSetting: false,
  headImgHeight: 150,
  logoImg: '',
  logoSetting: false,
  logoPosition: 'flex-start',
  logoSize: 60,
  submitBtnText: '提交',
  showTitle: true,
  showDescribe: true,
  showNumber: false,
  showSubmitBtn: true,
  btnFontSize: 15,
  btnWidth: 240,
  btnHeight: 48
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
    
    // 处理从 props 传入的 formItems，确保数据结构正确
    if (props.formItems && props.formItems.length > 0) {
      formItems.value = props.formItems.map(item => {
        // 如果 item 已经有 vModel，说明已经解析过了，直接返回
        if (item.vModel) {
          return item
        }
        // 否则需要解析 scheme
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
    } else {
      formItems.value = []
    }
    
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
      if (data.themeColor) themeForm.themeColor = data.themeColor
      if (data.backgroundColor) {
        themeForm.backgroundColor = data.backgroundColor
        themeForm.backgroundSetting = true
      }
      if (data.headImgUrl) {
        themeForm.headImgUrl = getImageUrl(data.headImgUrl)
        themeForm.headImgSetting = true
      }
      if (data.headImgHeight !== undefined) themeForm.headImgHeight = data.headImgHeight
      if (data.logoImg) {
        themeForm.logoImg = getImageUrl(data.logoImg)
        themeForm.logoSetting = true
      }
      if (data.logoPosition) themeForm.logoPosition = data.logoPosition
      if (data.logoSize !== undefined) themeForm.logoSize = data.logoSize
      if (data.submitBtnText) {
        themeForm.submitBtnText = data.submitBtnText
      }
      if (data.showTitle !== undefined) themeForm.showTitle = data.showTitle
      if (data.showDescribe !== undefined) themeForm.showDescribe = data.showDescribe
      if (data.showNumber !== undefined) themeForm.showNumber = data.showNumber
      // showSubmitBtn 始终为 true，始终显示提交按钮
      themeForm.showSubmitBtn = true
      if (data.btnFontSize !== undefined && data.btnFontSize !== null) themeForm.btnFontSize = data.btnFontSize
      if (data.btnWidth !== undefined && data.btnWidth !== null) themeForm.btnWidth = data.btnWidth
      if (data.btnHeight !== undefined && data.btnHeight !== null) themeForm.btnHeight = data.btnHeight
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
    const vModel = item.vModel || item.formItemId
    const defaultValue = item.defaultValue
    
    if (item.type === 'CHECKBOX') {
      previewFormModel[vModel] = defaultValue && Array.isArray(defaultValue) 
        ? defaultValue 
        : []
    } else if (item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
      previewFormModel[vModel] = defaultValue && Array.isArray(defaultValue)
        ? defaultValue
        : []
    } else if (item.type === 'SLIDER') {
      const numValue = defaultValue !== null && defaultValue !== undefined && defaultValue !== ''
        ? Number(defaultValue)
        : (item.config?.min || 0)
      previewFormModel[vModel] = isNaN(numValue) ? (item.config?.min || 0) : numValue
    } else if (item.type === 'NUMBER') {
      const numValue = defaultValue !== null && defaultValue !== undefined && defaultValue !== ''
        ? Number(defaultValue)
        : undefined
      previewFormModel[vModel] = isNaN(numValue) ? undefined : numValue
    } else if (item.type === 'DIVIDER' || item.type === 'IMAGE' || item.type === 'IMAGE_CAROUSEL') {
      // 这些类型不需要绑定表单模型
      previewFormModel[vModel] = null
    } else {
      // 其他类型使用 defaultValue，如果没有则使用空字符串（但要注意 defaultValue 为 "0" 字符串时应该保持为字符串）
      if (defaultValue === null || defaultValue === undefined || defaultValue === '') {
        previewFormModel[vModel] = ''
      } else {
        // 保持原始类型，不要转换
        previewFormModel[vModel] = defaultValue
      }
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
        themeColor: themeForm.themeColor,
        backgroundColor: themeForm.backgroundSetting ? themeForm.backgroundColor : '',
        headImgUrl: themeForm.headImgSetting ? getRelativeImageUrl(themeForm.headImgUrl) : '',
        headImgHeight: themeForm.headImgSetting ? themeForm.headImgHeight : null,
        logoImg: themeForm.logoSetting ? getRelativeImageUrl(themeForm.logoImg) : '',
        logoPosition: themeForm.logoPosition,
        logoSize: themeForm.logoSetting ? themeForm.logoSize : null,
        submitBtnText: themeForm.submitBtnText || '提交',
        showTitle: themeForm.showTitle,
        showDescribe: themeForm.showDescribe,
        showNumber: themeForm.showNumber,
        showSubmitBtn: true, // 始终显示提交按钮
        btnFontSize: themeForm.btnFontSize,
        btnWidth: themeForm.btnWidth,
        btnHeight: themeForm.btnHeight
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

// 处理设置变更（包括开关切换、清除逻辑等）
const handleSettingChange = () => {
  // 如果logo设置关闭，清除logo
  if (!themeForm.logoSetting) {
    themeForm.logoImg = ''
  }
  // 如果头图设置关闭，清除头图
  if (!themeForm.headImgSetting) {
    themeForm.headImgUrl = ''
  }
  // 如果背景设置关闭，清除背景
  if (!themeForm.backgroundSetting) {
    themeForm.backgroundColor = ''
  }
  saveTheme()
}

// 监听表单项变化，重新初始化预览
watch(() => formItems.value.length, () => {
  initPreviewForm()
})

// 提供themeForm给父组件使用（用于预览对话框）
provide('themeForm', themeForm)

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
    z-index: 1;
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
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  
  .phone-logo {
    padding: 0;
    display: flex;
    flex-shrink: 0;
    margin-bottom: 5px;

    img {
      width: var(--logo-size, 60px);
      height: var(--logo-size, 60px);
      min-width: var(--logo-size, 60px);
      min-height: var(--logo-size, 60px);
      object-fit: cover;
      border-radius: 4px;
    }
  }

  .phone-head-img {
    width: 100%;
    flex-shrink: 0;
    margin-bottom: 15px;
    border-radius: 8px 8px 0 0;
    overflow: hidden;

    img {
      width: 100%;
      height: var(--head-img-height, 150px);
      display: block;
      object-fit: cover;
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
  position: relative;
  padding: 40px;
  min-height: 500px;
  
  .desktop-logo {
    padding: 0;
    display: flex;
    margin-bottom: 5px;

    img {
      width: var(--logo-size, 80px);
      height: var(--logo-size, 80px);
      min-width: var(--logo-size, 80px);
      min-height: var(--logo-size, 80px);
      object-fit: cover;
      border-radius: 4px;
    }
  }

  .desktop-head-img {
    width: 100%;
    margin-bottom: 20px;
    border-radius: 8px 8px 0 0;
    overflow: hidden;

    img {
      width: 100%;
      height: var(--head-img-height, 200px);
      display: block;
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
  width: 400px;
  background: #fff;
  border-left: 1px solid #ebeef5;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-scrollbar {
  height: 100%;
  flex: 1;
  overflow: hidden;
}

.settings-panel {
  padding: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.settings-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 20px 0 15px 0;
}

.settings-form {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 40px;
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
