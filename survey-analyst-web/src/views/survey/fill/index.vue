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
        <el-scrollbar class="fill-scrollbar">
          <div
            class="fill-form-container"
            :style="{
              backgroundColor: themeConfig.backgroundColor || '#ffffff'
            }"
          >
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
              ref="surveyFormRef"
              :form-items="formItems"
              :form-model="formModel"
              :preview-mode="false"
              :theme-config="themeConfig"
              :show-number="themeConfig.showNumber"
              :form-logic="formLogic"
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
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { formatDateTime, getCurrentDateTime } from '@/utils/date'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { surveyApi, formApi, responseApi } from '@/api'
import SurveyFormRender from '@/components/SurveyFormRender.vue'
import { getImageUrl, getRelativeImageUrl } from '@/utils/image'
import { getToken } from '@/utils/auth'

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
const formLogic = ref([])
const survey = ref(null)
const surveyFormRef = ref(null)
const errorMessage = ref('')
const canFill = ref(false)
const passwordDialogVisible = ref(false)
const inputPassword = ref('')
const responseCount = ref(0)
const verifyingPassword = ref(false)
const fillStartTime = ref(null) // 记录开始填写时间

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

    // 填写前校验（检查各种限制）
    if (formKey.value) {
      // 生成设备ID（使用localStorage存储，用于设备限制）
      let deviceId = localStorage.getItem('deviceId')
      if (!deviceId) {
        deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
        localStorage.setItem('deviceId', deviceId)
      }
      
      try {
        const validateRes = await formApi.validateBeforeFill(formKey.value, deviceId)
        if (validateRes.code !== 200) {
          errorMessage.value = validateRes.message || '校验失败，无法填写'
          canFill.value = false
          return
        }
        // 校验通过，记录开始填写时间（格式化为 yyyy-MM-dd HH:mm:ss）
        fillStartTime.value = getCurrentDateTime()
      } catch (error) {
        errorMessage.value = error.response?.data?.message || error.message || '校验失败，无法填写'
        canFill.value = false
        return
      }
    }

    // 加载表单项
    if (formKey.value && formItems.value.length === 0) {
      const itemsRes = await formApi.getFormItems(formKey.value)
      if (itemsRes.code === 200 && itemsRes.data) {
        // 解析 scheme 字段，提取 vModel 和 config
        formItems.value = itemsRes.data.map(item => {
          // 解析 scheme（可能是 JSON 字符串或对象）
          let scheme = {}
          try {
            scheme = typeof item.scheme === 'string' 
              ? (item.scheme ? JSON.parse(item.scheme) : {})
              : (item.scheme || {})
          } catch (e) {
            scheme = {}
          }
          
          // 解析 regList（可能是 JSON 字符串或数组）
          let regList = []
          try {
            regList = typeof item.regList === 'string'
              ? (item.regList ? JSON.parse(item.regList) : [])
              : (Array.isArray(item.regList) ? item.regList : [])
          } catch (e) {
            regList = []
          }
          
          return {
            formItemId: item.formItemId,
            type: item.type,
            label: item.label,
            vModel: scheme.vModel || item.formItemId,
            placeholder: scheme.placeholder || item.placeholder || '',
            required: scheme.required !== undefined ? scheme.required : (item.required === 1),
            disabled: scheme.disabled || false,
            readonly: scheme.readonly || false,
            hideType: item.isHideType || false,
            defaultValue: scheme.defaultValue !== undefined ? scheme.defaultValue : (item.defaultValue || null),
            config: scheme.config || {},
            regList: regList,
            sort: item.sort != null ? item.sort : 0
          }
        }).sort((a, b) => {
          // 确保按 sort 排序（后端虽然已经排序，但为了保险起见，前端再排序一次）
          const sortA = a.sort != null ? a.sort : 0
          const sortB = b.sort != null ? b.sort : 0
          return sortA - sortB
        })
        initFormModel()
      }
    }
    
    // 加载逻辑规则（如果有surveyId）
    if (surveyId.value && formLogic.value.length === 0) {
      try {
        const logicRes = await formApi.getFormLogic(surveyId.value)
        if (logicRes.code === 200 && logicRes.data && logicRes.data.scheme) {
          formLogic.value = logicRes.data.scheme || []
        }
      } catch (error) {
        console.error('加载逻辑规则失败:', error)
        // 逻辑规则加载失败不影响表单填写，只记录错误
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
  // 根据登录状态跳转
  const token = getToken()
  if (token) {
    router.push('/survey/list')
  } else {
    router.push('/login')
  }
}

// 外观配置（默认值）
const themeConfig = reactive({
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
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

// 检查登录状态，如果未登录则提示并跳转
const checkLoginAndRedirect = async () => {
  const hasNoToken = !getToken()
  if (hasNoToken) {
    try {
      await ElMessageBox.confirm('您还未登录，请先登录', '提示', {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      })
      // 用户点击确认，跳转到登录页
      const currentPath = route.fullPath
      router.push({
        name: 'Login',
        query: {
          redirect: currentPath
        }
      })
      return true // 返回true表示已跳转
    } catch {
      // 用户点击取消，不跳转
      return false
    }
  }
  return false // 已登录，不需要跳转
}

// 加载问卷数据
const loadSurveyData = async () => {
  // 检查登录状态
  const hasRedirected = await checkLoginAndRedirect()
  if (hasRedirected) {
    return
  }
  
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
        
        // 检查问卷状态，只有已发布的问卷才能填写
        if (surveyRes.data.status !== 'PUBLISHED') {
          errorMessage.value = '问卷尚未发布，无法填写'
          loading.value = false
          return
        }
        
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
      } else {
        errorMessage.value = surveyRes.message || '问卷不存在或加载失败'
        loading.value = false
        return
      }

      // 加载表单配置
      const configRes = await formApi.getFormConfig(surveyId.value)
      if (configRes.code === 200 && configRes.data) {
        formKey.value = configRes.data.formKey
      } else {
        errorMessage.value = '加载表单配置失败'
        loading.value = false
        return
      }
    } else if (key) {
      // 通过key访问
      formKey.value = key
    } else {
      errorMessage.value = '缺少必要参数（问卷ID或表单Key）'
      loading.value = false
      return
    }

    // 加载表单项
    if (formKey.value) {
      const itemsRes = await formApi.getFormItems(formKey.value)
      if (itemsRes.code === 200 && itemsRes.data) {
        // 解析 scheme 字段，提取 vModel 和 config
        formItems.value = itemsRes.data.map(item => {
          // 解析 scheme（可能是 JSON 字符串或对象）
          let scheme = {}
          try {
            scheme = typeof item.scheme === 'string' 
              ? (item.scheme ? JSON.parse(item.scheme) : {})
              : (item.scheme || {})
          } catch (e) {
            scheme = {}
          }
          
          // 解析 regList（可能是 JSON 字符串或数组）
          let regList = []
          try {
            regList = typeof item.regList === 'string'
              ? (item.regList ? JSON.parse(item.regList) : [])
              : (Array.isArray(item.regList) ? item.regList : [])
          } catch (e) {
            regList = []
          }
          
          return {
            formItemId: item.formItemId,
            type: item.type,
            label: item.label,
            vModel: scheme.vModel || item.formItemId,
            placeholder: scheme.placeholder || item.placeholder || '',
            required: scheme.required !== undefined ? scheme.required : (item.required === 1),
            disabled: scheme.disabled || false,
            readonly: scheme.readonly || false,
            hideType: item.isHideType || false,
            defaultValue: scheme.defaultValue !== undefined ? scheme.defaultValue : (item.defaultValue || null),
            config: scheme.config || {},
            regList: regList,
            sort: item.sort != null ? item.sort : 0
          }
        }).sort((a, b) => {
          // 确保按 sort 排序（后端虽然已经排序，但为了保险起见，前端再排序一次）
          const sortA = a.sort != null ? a.sort : 0
          const sortB = b.sort != null ? b.sort : 0
          return sortA - sortB
        })
        initFormModel()
      } else {
        errorMessage.value = '加载表单项失败'
        loading.value = false
        return
      }
    } else {
      errorMessage.value = '表单Key不存在，无法加载表单'
      loading.value = false
      return
    }

    // 加载逻辑规则（如果有surveyId）
    if (surveyId.value) {
      try {
        const logicRes = await formApi.getFormLogic(surveyId.value)
        if (logicRes.code === 200 && logicRes.data && logicRes.data.scheme) {
          formLogic.value = logicRes.data.scheme || []
        }
      } catch (error) {
        console.error('加载逻辑规则失败:', error)
        // 逻辑规则加载失败不影响表单填写，只记录错误
      }
    }

    // 加载外观配置（如果有surveyId）
    if (surveyId.value) {
      await loadTheme()
    }

    // 如果不需要密码验证，进行填写前校验
    if (!survey.value || survey.value.accessType !== 'PASSWORD') {
      // 生成设备ID（使用localStorage存储，用于设备限制）
      let deviceId = localStorage.getItem('deviceId')
      if (!deviceId) {
        deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
        localStorage.setItem('deviceId', deviceId)
      }
      
      // 填写前校验（检查各种限制）
      try {
        const validateRes = await formApi.validateBeforeFill(formKey.value, deviceId)
        if (validateRes.code === 200) {
          canFill.value = true
          // 校验通过，记录开始填写时间（格式化为 yyyy-MM-dd HH:mm:ss）
          fillStartTime.value = getCurrentDateTime()
        } else {
          errorMessage.value = validateRes.message || '校验失败，无法填写'
          canFill.value = false
        }
      } catch (error) {
        errorMessage.value = error.response?.data?.message || error.message || '校验失败，无法填写'
        canFill.value = false
      }
    }
  } catch (error) {
    // 加载问卷失败，显示错误提示
    errorMessage.value = error.response?.data?.message || error.message || '加载问卷失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 初始化表单数据模型
const initFormModel = () => {
  formItems.value.forEach(item => {
    // 确保 vModel 存在
    if (!item.vModel) {
      return
    }
    
    const defaultValue = item.defaultValue
    
    // 数组类型组件（多选）
    if (item.type === 'CHECKBOX' || item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
      // 确保是数组类型，空字符串或其他类型都转换为空数组
      if (Array.isArray(defaultValue)) {
        formModel[item.vModel] = [...defaultValue]
      } else if (defaultValue === null || defaultValue === undefined || defaultValue === '' || defaultValue === '0') {
        formModel[item.vModel] = []
      } else {
        // 尝试解析为数组
        try {
          const parsed = typeof defaultValue === 'string' ? JSON.parse(defaultValue) : defaultValue
          formModel[item.vModel] = Array.isArray(parsed) ? parsed : []
        } catch {
          formModel[item.vModel] = []
        }
      }
    }
    // 图片选择组件（根据配置决定是单选还是多选）
    else if (item.type === 'IMAGE_SELECT') {
      if (item.config?.multiple) {
        // 多选模式：数组类型
        if (Array.isArray(defaultValue)) {
          formModel[item.vModel] = [...defaultValue]
        } else if (defaultValue === null || defaultValue === undefined || defaultValue === '' || defaultValue === '0') {
          formModel[item.vModel] = []
        } else {
          try {
            const parsed = typeof defaultValue === 'string' ? JSON.parse(defaultValue) : defaultValue
            formModel[item.vModel] = Array.isArray(parsed) ? parsed : []
          } catch {
            formModel[item.vModel] = []
          }
        }
      } else {
        // 单选模式：字符串类型（确保是字符串）
        if (defaultValue === null || defaultValue === undefined || defaultValue === '') {
          formModel[item.vModel] = ''
        } else {
          formModel[item.vModel] = String(defaultValue)
        }
      }
    }
    // 数字类型组件 - NUMBER
    else if (item.type === 'NUMBER') {
      // NUMBER 类型：Number 或 null/undefined
      if (defaultValue === null || defaultValue === undefined || defaultValue === '' || defaultValue === 'null') {
        formModel[item.vModel] = undefined
      } else {
        const numValue = Number(defaultValue)
        formModel[item.vModel] = isNaN(numValue) ? undefined : numValue
      }
    }
    // 数字类型组件 - SLIDER
    else if (item.type === 'SLIDER') {
      // SLIDER 类型：必须是 Number，默认值为 min 或 0
      if (defaultValue === null || defaultValue === undefined || defaultValue === '' || defaultValue === 'null') {
        formModel[item.vModel] = item.config?.min || 0
      } else {
        const numValue = Number(defaultValue)
        formModel[item.vModel] = isNaN(numValue) ? (item.config?.min || 0) : numValue
      }
    }
    // 数字类型组件 - RATE
    else if (item.type === 'RATE') {
      // RATE 类型：必须是 Number，默认值为 0
      if (defaultValue === null || defaultValue === undefined || defaultValue === '' || defaultValue === 'null') {
        formModel[item.vModel] = 0
      } else {
        const numValue = Number(defaultValue)
        formModel[item.vModel] = isNaN(numValue) ? 0 : numValue
      }
    }
    // 手写签名组件（字符串类型，默认为空字符串）
    else if (item.type === 'SIGN_PAD') {
      // SIGN_PAD 类型：必须是字符串
      if (defaultValue === null || defaultValue === undefined) {
        formModel[item.vModel] = ''
      } else {
        // 确保转换为字符串（即使是数字 0 也要转为字符串）
        formModel[item.vModel] = String(defaultValue)
      }
    }
    // 展示类组件不需要初始化表单值
    else if (item.type === 'DIVIDER' || item.type === 'IMAGE' || item.type === 'IMAGE_CAROUSEL' || item.type === 'DESC_TEXT') {
      // 这些类型不需要绑定表单模型
    }
    // 其他输入类组件（字符串或默认值）
    else {
      // INPUT, TEXTAREA, DATE, SELECT, RADIO, CASCADER 等
      if (defaultValue === null || defaultValue === undefined || defaultValue === 'null') {
        formModel[item.vModel] = ''
      } else {
        formModel[item.vModel] = defaultValue
      }
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

// 评估逻辑条件（用于验证时判断字段是否可见）
const evaluateCondition = (condition, formModel, formItems) => {
  const { formItemId, expression, optionValue } = condition
  if (!formItemId || !expression) return true
  
  const item = formItems.find(i => i.formItemId === formItemId)
  if (!item) return true
  
  const value = formModel[item.vModel]
  
  switch (expression) {
    case 'eq': // 等于/选中
      if (item.type === 'CHECKBOX') {
        return Array.isArray(value) && value.includes(optionValue)
      }
      return value === optionValue
    case 'ne': // 不等于/未选中
      if (item.type === 'CHECKBOX') {
        return !Array.isArray(value) || !value.includes(optionValue)
      }
      return value !== optionValue
    case 'gt': // 大于
      return Number(value) > Number(optionValue)
    case 'ge': // 大于等于
      return Number(value) >= Number(optionValue)
    case 'lt': // 小于
      return Number(value) < Number(optionValue)
    case 'le': // 小于等于
      return Number(value) <= Number(optionValue)
    default:
      return true
  }
}

// 评估逻辑规则（用于验证时判断字段是否可见）
const evaluateLogicRule = (rule, formModel, formItems) => {
  const conditionList = rule.conditionList || []
  if (conditionList.length === 0) {
    return true
  }
  
  // 评估所有条件
  let result = true
  for (let i = 0; i < conditionList.length; i++) {
    const condition = conditionList[i]
    const conditionResult = evaluateCondition(condition, formModel, formItems)
    
    if (i === 0) {
      result = conditionResult
    } else {
      // 使用第一个条件的relation（所有条件使用相同的relation）
      const relation = conditionList[0]?.relation || condition.relation || 'AND'
      if (relation === 'AND') {
        result = result && conditionResult
      } else if (relation === 'OR') {
        result = result || conditionResult
      }
    }
  }
  
  return result
}

// 判断字段是否可见（用于验证时，逻辑与SurveyFormRender组件保持一致）
const isItemVisible = (item, formModel, formItems, formLogic) => {
  // 如果字段的hideType为true，则不可见
  if (item.hideType) {
    return false
  }
  
  // 如果没有逻辑规则，默认可见
  if (!formLogic || formLogic.length === 0) {
    return true
  }
  
  // 初始化可见性为true（默认显示）
  let isVisible = true
  
  // 收集所有被逻辑规则控制的表单项ID
  const controlledItems = new Set()
  
  // 遍历所有逻辑规则（与SurveyFormRender中的逻辑保持一致）
  formLogic.forEach(rule => {
    if (!rule.conditionList || !rule.triggerList) return
    
    // 将Set转换为数组（如果必要）
    const conditionList = Array.isArray(rule.conditionList) 
      ? rule.conditionList 
      : (rule.conditionList instanceof Set ? Array.from(rule.conditionList) : [])
    const triggerList = Array.isArray(rule.triggerList) 
      ? rule.triggerList 
      : (rule.triggerList instanceof Set ? Array.from(rule.triggerList) : [])
    
    // 记录被控制的项
    triggerList.forEach(trigger => {
      if (trigger.formItemId) {
        controlledItems.add(trigger.formItemId)
      }
    })
    
    const conditionMet = evaluateLogicRule({ ...rule, conditionList }, formModel, formItems)
    
    // 如果当前字段被这个规则控制，更新可见性
    const trigger = triggerList.find(t => t.formItemId === item.formItemId)
    if (trigger) {
      if (conditionMet) {
        // 条件满足时，执行show/hide操作
        if (trigger.type === 'show') {
          isVisible = true
        } else if (trigger.type === 'hide') {
          isVisible = false
        }
      } else {
        // 条件不满足时，执行相反的操作
        if (trigger.type === 'show') {
          isVisible = false
        } else if (trigger.type === 'hide') {
          isVisible = true
        }
      }
    }
  })
  
  // 对于没有被逻辑规则控制的项，保持默认显示状态
  if (!controlledItems.has(item.formItemId)) {
    isVisible = true
  }
  
  return isVisible
}

// 验证表单
const validateForm = async () => {
  // 使用 SurveyFormRender 组件的完整验证规则
  if (surveyFormRef.value) {
    try {
      await surveyFormRef.value.validate()
      return true
    } catch (error) {
      // 验证失败，Element Plus 会自动显示错误信息
      // 这里可以添加额外的处理逻辑
      return false
    }
  }
  
  // 如果组件未加载，使用简单的验证逻辑作为后备
  const excludeTypes = ['DIVIDER', 'IMAGE', 'IMAGE_CAROUSEL', 'DESC_TEXT']
  
  for (const item of formItems.value) {
    // 跳过不需要验证的组件类型
    if (excludeTypes.includes(item.type)) {
      continue
    }
    
    // 跳过隐藏的字段（包括hideType为true和逻辑隐藏的字段）
    if (!isItemVisible(item, formModel, formItems.value, formLogic.value)) {
      continue
    }
    
    // 只验证必填项
    if (item.required) {
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
  // 检查登录状态
  const hasRedirected = await checkLoginAndRedirect()
  if (hasRedirected) {
    return
  }
  
  const isValid = await validateForm()
  if (!isValid) {
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
    // 不需要提交的组件类型（展示类组件）
    const excludeTypes = ['DIVIDER', 'IMAGE', 'IMAGE_CAROUSEL', 'DESC_TEXT']
    
    formItems.value.forEach(item => {
      if (!excludeTypes.includes(item.type)) {
        const value = formModel[item.vModel]
        if (value !== null && value !== undefined && value !== '' &&
            !(Array.isArray(value) && value.length === 0)) {
          // 对于上传组件，需要将文件对象转换为URL字符串或URL数组
          if (item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
            if (Array.isArray(value)) {
              // 提取文件URL（优先使用rawUrl相对路径，否则从url中提取相对路径）
              answers[item.formItemId] = value.map(file => {
                if (typeof file === 'string') {
                  // 如果已经是字符串，转换为相对路径
                  return getRelativeImageUrl(file)
                } else if (file && file.rawUrl) {
                  // 使用保存的相对路径
                  return file.rawUrl
                } else if (file && file.url) {
                  // 从完整URL中提取相对路径
                  return getRelativeImageUrl(file.url)
                } else {
                  // 其他情况，尝试从response中获取
                  return file?.response?.data || file
                }
              })
            } else {
              // 单个文件的情况
              if (typeof value === 'string') {
                answers[item.formItemId] = getRelativeImageUrl(value)
              } else if (value && value.rawUrl) {
                answers[item.formItemId] = value.rawUrl
              } else if (value && value.url) {
                answers[item.formItemId] = getRelativeImageUrl(value.url)
              } else {
                answers[item.formItemId] = value?.response?.data || value
              }
            }
          } else {
            answers[item.formItemId] = value
          }
        }
      }
    })

    // 生成设备ID（使用localStorage存储，用于设备限制）
    let deviceId = localStorage.getItem('deviceId')
    if (!deviceId) {
      deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
      localStorage.setItem('deviceId', deviceId)
    }

    // 使用表单数据提交接口（传递开始填写时间）
    const formDataRes = await formApi.submitFormData(formKey.value, answers, deviceId, fillStartTime.value)
    if (formDataRes && formDataRes.code === 200) {
      const data = formDataRes.data || {}
      const submitSettings = data.submitSettings || {}
      
      // 根据提交设置显示提示信息
      const submitShowType = submitSettings.submitShowType || 1
      let message = '提交成功，感谢您的参与！'
      
      if (submitShowType === 2 && submitSettings.submitShowCustomPageContent) {
        message = submitSettings.submitShowCustomPageContent
      }
      
      ElMessage.success(message)
      
      // 根据提交设置进行跳转
      const submitJump = submitSettings.submitJump
      const submitJumpUrl = submitSettings.submitJumpUrl
      
      if (submitJump && submitJumpUrl) {
        // 跳转到指定URL
      setTimeout(() => {
          window.location.href = submitJumpUrl
      }, 1500)
      } else {
        // 默认跳转：已登录跳转到问卷列表，未登录跳转到登录页
        setTimeout(() => {
          const token = getToken()
          if (token) {
            router.push('/survey/list')
          } else {
            router.push('/login')
          }
        }, 1500)
      }
    } else {
      ElMessage.error('提交失败，请稍后重试')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '提交失败')
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

.fill-content {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.fill-form-wrapper {
  width: 100%;
  height: calc(100vh - 40px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  
  .fill-scrollbar {
    height: 100%;
  }
}

.fill-form-container {
  position: relative;
  padding: 40px;
  min-height: 500px;
}

.fill-logo {
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

.fill-head-img {
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

.fill-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  margin: 0 0 20px 0;
  padding-bottom: 20px;
  border-bottom: 2px solid #ebeef5;
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
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  align-items: center;

  .el-button {
    min-width: 120px;
  }
}

.error-card {
  margin-bottom: 20px;
}

// 移动端适配（参考预览页移动端样式）
@media (max-width: 768px) {
  .survey-fill-container {
    padding: 0;
  }

  .fill-content {
    padding: 0;
    max-width: 100%;
  }

  .fill-form-wrapper {
    height: 100vh;
    border-radius: 0;
    box-shadow: none;
  }

  .fill-form-container {
    padding: 20px;
    min-height: auto;
  }

  .fill-logo {
    margin-bottom: 5px;

    img {
      width: var(--logo-size, 60px);
      height: var(--logo-size, 60px);
      min-width: var(--logo-size, 60px);
      min-height: var(--logo-size, 60px);
    }
  }

  .fill-head-img {
    margin-bottom: 15px;
    border-radius: 0;

    img {
      height: var(--head-img-height, 150px);
    }
  }

  .fill-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid #ebeef5;
  }

  .fill-description {
    font-size: 14px;
    margin-bottom: 15px;
    line-height: 1.6;
  }

  .fill-submit-buttons {
    margin-top: 20px;
    padding-top: 20px;
  }
}

// 小屏幕手机适配
@media (max-width: 480px) {
  .fill-content {
    padding: 0;
  }

  .fill-form-wrapper {
    height: 100vh;
    border-radius: 0;
  }

  .fill-form-container {
    padding: 15px;
  }

  .fill-title {
    font-size: 18px;
    margin-bottom: 10px;
    padding-bottom: 10px;
  }

  .fill-description {
    font-size: 13px;
    margin-bottom: 15px;
  }

  .fill-logo {
    img {
      width: var(--logo-size, 50px);
      height: var(--logo-size, 50px);
      min-width: var(--logo-size, 50px);
      min-height: var(--logo-size, 50px);
    }
  }

  .fill-head-img {
    img {
      height: var(--head-img-height, 120px);
    }
  }
}
</style>
