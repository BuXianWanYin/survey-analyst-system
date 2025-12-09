<template>
  <div
    v-loading="loading"
    class="survey-fill-container"
    :style="{
      backgroundColor: themeConfig.backgroundColor || '#f5f7fa',
      minHeight: '100vh'
    }"
  >
    <!-- 密码验证对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="请输入访问密码"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <el-form>
        <el-form-item label="访问密码">
          <el-input
            v-model="inputPassword"
            type="password"
            placeholder="请输入访问密码"
            show-password
            @keyup.enter="handleVerifyPassword"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancelPassword">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="verifyingPassword"
          @click="handleVerifyPassword"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 错误提示 -->
    <div
      v-if="errorMessage"
      class="fill-content"
    >
      <el-card class="error-card">
        <el-alert
          :title="errorMessage"
          type="error"
          :closable="false"
          show-icon
        />
      </el-card>
    </div>

    <!-- 表单内容 -->
    <div
      v-if="!errorMessage && canFill"
      class="fill-content"
    >
      <div class="fill-form-wrapper">
        <!-- Logo -->
        <div
          v-if="themeConfig.logoImg"
          class="fill-logo"
          :style="{
            justifyContent: getLogoPosition(),
            '--logo-size': `${themeConfig.logoSize || 60}px`
          }"
        >
          <img
            :src="getImageUrl(themeConfig.logoImg)"
            alt="logo"
          >
        </div>

        <!-- 头图 -->
        <div
          v-if="themeConfig.headImgUrl"
          class="fill-head-img"
          :style="{
            '--head-img-height': `${themeConfig.headImgHeight || 200}px`
          }"
        >
          <img
            :src="getImageUrl(themeConfig.headImgUrl)"
            alt="head"
          >
        </div>

        <!-- 标题 -->
        <h2
          v-if="themeConfig.showTitle"
          class="fill-title"
        >
          {{ surveyTitle || '未命名问卷' }}
        </h2>

        <!-- 描述 -->
        <p
          v-if="themeConfig.showDescribe && surveyDescription"
          class="fill-description"
        >
          {{ surveyDescription }}
        </p>

        <!-- 表单渲染 -->
        <SurveyFormRender
          v-if="formItems.length > 0"
          :form-items="formItems"
          :form-model="formModel"
          :preview-mode="false"
          :theme-config="themeConfig"
          :show-number="themeConfig.showNumber"
        />

        <!-- 提交按钮 -->
        <div
          v-if="themeConfig.showSubmitBtn"
          class="fill-submit-buttons"
        >
          <el-button
            type="primary"
            size="large"
            :style="{
              backgroundColor: themeConfig.themeColor || '#409EFF',
              borderColor: themeConfig.themeColor || '#409EFF',
              fontSize: `${themeConfig.btnFontSize || 15}px`,
              width: `${themeConfig.btnWidth || 240}px`,
              height: `${themeConfig.btnHeight || 48}px`,
              minWidth: `${themeConfig.btnWidth || 240}px`
            }"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ themeConfig.submitBtnText || '提交' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { surveyApi, formApi, responseApi } from '@/api'
import SurveyFormRender from '@/components/SurveyFormRender.vue'
import { getImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const surveyId = ref(null)
const surveyTitle = ref('')
const surveyDescription = ref('')
const formKey = ref(null)
const formItems = ref([])
const formModel = reactive({})
const survey = ref(null)
const errorMessage = ref('')
const canFill = ref(false)
const passwordDialogVisible = ref(false)
const inputPassword = ref('')
const responseCount = ref(0)
const verifyingPassword = ref(false)

// 密码验证相关函数
const handleVerifyPassword = async () => {
  if (!inputPassword.value || !inputPassword.value.trim()) {
    ElMessage.warning('请输入访问密码')
    return
  }

  if (!surveyId.value) {
    ElMessage.error('问卷ID不存在，无法验证密码')
    return
  }

  verifyingPassword.value = true
  try {
    const res = await surveyApi.verifyPassword(surveyId.value, inputPassword.value.trim())
    if (res.code === 200 && res.data) {
      ElMessage.success('密码验证成功')
      passwordDialogVisible.value = false
      canFill.value = true
      inputPassword.value = ''
      // 密码验证通过后，继续加载表单数据
      await continueLoadFormData()
    } else {
      ElMessage.error(res.message || '密码错误，请重新输入')
      inputPassword.value = ''
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '密码验证失败，请稍后重试')
    inputPassword.value = ''
  } finally {
    verifyingPassword.value = false
  }
}

// 继续加载表单数据（密码验证通过后调用）
const continueLoadFormData = async () => {
  try {
    // 加载表单配置
    if (!formKey.value && surveyId.value) {
      const configRes = await formApi.getFormConfig(surveyId.value)
      if (configRes.code === 200 && configRes.data) {
        formKey.value = configRes.data.formKey
      }
    }

    // 加载表单项
    if (formKey.value && formItems.value.length === 0) {
      const itemsRes = await formApi.getFormItems(formKey.value)
      if (itemsRes.code === 200 && itemsRes.data) {
        formItems.value = itemsRes.data
        initFormModel()
      }
    }

    // 加载外观配置
    if (surveyId.value) {
      await loadTheme()
    }
  } catch (error) {
    ElMessage.error('加载表单数据失败')
  }
}

const handleCancelPassword = () => {
  passwordDialogVisible.value = false
  inputPassword.value = ''
  router.push('/home')
}

// 外观配置（默认值）
const themeConfig = reactive({
  themeColor: '#409EFF',
  backgroundColor: '#f5f7fa',
  headImgUrl: '',
  headImgHeight: 200,
  logoImg: '',
  logoPosition: 'flex-start',
  logoSize: 80,
  submitBtnText: '提交',
  showTitle: true,
  showDescribe: true,
  showNumber: false,
  showSubmitBtn: true,
  btnFontSize: 15,
  btnWidth: 240,
  btnHeight: 48
})

// 获取Logo位置样式值
const getLogoPosition = () => {
  const positionMap = {
    'flex-start': 'flex-start',
    'center': 'center',
    'flex-end': 'flex-end',
    'left': 'flex-start',
    'right': 'flex-end'
  }
  return positionMap[themeConfig.logoPosition] || 'flex-start'
}

// 加载问卷数据
const loadSurveyData = async () => {
  loading.value = true
  try {
    // 支持通过 id 或 key 参数访问
    const id = route.params.id
    const key = route.query.key

    if (id) {
      // 通过ID访问
      surveyId.value = Number(id)
      
      // 加载问卷信息
      const surveyRes = await surveyApi.getSurveyById(surveyId.value)
      if (surveyRes.code === 200 && surveyRes.data) {
        survey.value = surveyRes.data
        surveyTitle.value = surveyRes.data.title || '未命名问卷'
        surveyDescription.value = surveyRes.data.description || ''
        
        // 检查是否需要密码验证
        if (surveyRes.data.accessType === 'PASSWORD') {
          // 先加载formKey，用于后续密码验证通过后加载数据
          const configRes = await formApi.getFormConfig(surveyId.value)
          if (configRes.code === 200 && configRes.data) {
            formKey.value = configRes.data.formKey
          }
          passwordDialogVisible.value = true
          canFill.value = false
          loading.value = false
          return // 等待密码验证通过后再继续加载表单项
        }
      }

      // 加载表单配置
      const configRes = await formApi.getFormConfig(surveyId.value)
      if (configRes.code === 200 && configRes.data) {
        formKey.value = configRes.data.formKey
      }
    } else if (key) {
      // 通过key访问
      formKey.value = key
    } else {
      ElMessage.error('缺少必要参数')
      return
    }

    // 加载表单项
    if (formKey.value) {
      const itemsRes = await formApi.getFormItems(formKey.value)
      if (itemsRes.code === 200 && itemsRes.data) {
        formItems.value = itemsRes.data
        initFormModel()
      }
    }

    // 加载外观配置（如果有surveyId）
    if (surveyId.value) {
      await loadTheme()
    }

    // 如果不需要密码验证，允许填写
    if (!survey.value || survey.value.accessType !== 'PASSWORD') {
      canFill.value = true
    }
  } catch (error) {
    // 加载问卷失败，显示错误提示
    ElMessage.error('加载问卷失败')
  } finally {
    loading.value = false
  }
}

// 初始化表单数据模型
const initFormModel = () => {
  formItems.value.forEach(item => {
    if (item.type === 'CHECKBOX' || item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
      formModel[item.vModel] = item.defaultValue && Array.isArray(item.defaultValue)
        ? [...item.defaultValue]
        : []
    } else if (item.type === 'SLIDER') {
      const numValue = item.defaultValue !== null && item.defaultValue !== undefined && item.defaultValue !== ''
        ? Number(item.defaultValue)
        : (item.config?.min || 0)
      formModel[item.vModel] = isNaN(numValue) ? (item.config?.min || 0) : numValue
    } else if (item.type === 'NUMBER') {
      const numValue = item.defaultValue !== null && item.defaultValue !== undefined && item.defaultValue !== ''
        ? Number(item.defaultValue)
        : undefined
      formModel[item.vModel] = isNaN(numValue) ? undefined : numValue
    } else if (item.type !== 'DIVIDER' && item.type !== 'IMAGE' && item.type !== 'IMAGE_CAROUSEL') {
      formModel[item.vModel] = item.defaultValue !== null && item.defaultValue !== undefined 
        ? item.defaultValue 
        : ''
    }
  })
}

// 加载外观配置
const loadTheme = async () => {
  try {
    const res = await formApi.getFormTheme(surveyId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.themeColor) themeConfig.themeColor = data.themeColor
      if (data.backgroundColor) themeConfig.backgroundColor = data.backgroundColor
      if (data.headImgUrl) themeConfig.headImgUrl = getImageUrl(data.headImgUrl)
      if (data.headImgHeight !== undefined) themeConfig.headImgHeight = data.headImgHeight
      if (data.logoImg) themeConfig.logoImg = getImageUrl(data.logoImg)
      if (data.logoPosition) themeConfig.logoPosition = data.logoPosition
      if (data.logoSize !== undefined) themeConfig.logoSize = data.logoSize
      if (data.submitBtnText) themeConfig.submitBtnText = data.submitBtnText
      if (data.showTitle !== undefined) themeConfig.showTitle = data.showTitle
      if (data.showDescribe !== undefined) themeConfig.showDescribe = data.showDescribe
      if (data.showNumber !== undefined) themeConfig.showNumber = data.showNumber
      if (data.showSubmitBtn !== undefined) themeConfig.showSubmitBtn = data.showSubmitBtn
      if (data.btnFontSize !== undefined && data.btnFontSize !== null) themeConfig.btnFontSize = data.btnFontSize
      if (data.btnWidth !== undefined && data.btnWidth !== null) themeConfig.btnWidth = data.btnWidth
      if (data.btnHeight !== undefined && data.btnHeight !== null) themeConfig.btnHeight = data.btnHeight
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

// 验证表单
const validateForm = () => {
  for (const item of formItems.value) {
    if (item.required && item.type !== 'DIVIDER' && item.type !== 'IMAGE' && item.type !== 'IMAGE_CAROUSEL') {
      const value = formModel[item.vModel]
      if (value === null || value === undefined || value === '' || 
          (Array.isArray(value) && value.length === 0)) {
        ElMessage.warning(`请填写：${item.label}`)
        return false
      }
    }
  }
  return true
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  // 再次验证填写数量限制（防止在填写过程中达到上限）
  if (survey.value && survey.value.maxResponses && survey.value.maxResponses > 0) {
    try {
      const res = await responseApi.getResponseList({
        surveyId: surveyId.value,
        pageNum: 1,
        pageSize: 1
      })
      
      if (res.code === 200) {
        const currentCount = res.data?.total || responseCount.value
        if (currentCount >= survey.value.maxResponses) {
          ElMessage.error(`问卷已达到最大填写数（${survey.value.maxResponses}），无法提交`)
          return
        }
      }
    } catch (error) {
      // 验证填写数量失败，继续提交流程
    }
  }

  try {
    await ElMessageBox.confirm('确定要提交吗？提交后无法修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    if (!surveyId.value) {
      ElMessage.error('问卷ID不存在')
      return
    }

    // 转换表单数据为答案格式
    const answers = {}
    formItems.value.forEach(item => {
      if (item.type !== 'DIVIDER' && item.type !== 'IMAGE' && item.type !== 'IMAGE_CAROUSEL') {
        const value = formModel[item.vModel]
        if (value !== null && value !== undefined && value !== '' &&
            !(Array.isArray(value) && value.length === 0)) {
          answers[item.formItemId] = value
        }
      }
    })

    // 使用表单数据提交接口
    const formDataRes = await formApi.submitFormData(formKey.value, answers)
    if (formDataRes && formDataRes.code === 200) {
      ElMessage.success('提交成功，感谢您的参与！')
      setTimeout(() => {
        router.push('/home')
      }, 1500)
    } else {
      ElMessage.error('提交失败，请稍后重试')
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadSurveyData()
})
</script>

<style lang="scss" scoped>
.survey-fill-container {
  width: 100%;
  min-height: 100vh;
  padding: 0;
  position: relative;
}

.fill-logo {
  padding: 0;
  display: flex;
  max-width: 1200px;
  margin: 0 auto 5px;

  img {
    width: var(--logo-size, 80px);
    height: var(--logo-size, 80px);
    min-width: var(--logo-size, 80px);
    min-height: var(--logo-size, 80px);
    object-fit: cover;
    border-radius: 4px;
  }
}

.fill-head-img {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto 20px;
  border-radius: 8px 8px 0 0;
  overflow: hidden;

  img {
    width: 100%;
    height: var(--head-img-height, 200px);
    display: block;
    object-fit: cover;
}
}


.fill-form-wrapper {
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.fill-title {
  font-size: 24px;
  font-weight: 500;
  margin: 0 0 15px 0;
  color: #303133;
  text-align: center;
}

.fill-description {
  font-size: 14px;
  color: #606266;
  margin: 0 0 30px 0;
  line-height: 1.6;
  text-align: center;
}

.fill-submit-buttons {
  margin-top: 40px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;

  .el-button {
    min-width: 120px;
  }
}

// 移动端适配
@media (max-width: 768px) {
  .fill-logo {
    padding: 15px;

    img {
      max-width: 100px;
      max-height: 40px;
    }
  }

  .fill-content {
    padding: 20px 15px;
  }

  .fill-form-wrapper {
    padding: 20px;
  }

  .fill-title {
    font-size: 20px;
  }
}

.error-card {
  margin-bottom: 20px;
}
</style>
