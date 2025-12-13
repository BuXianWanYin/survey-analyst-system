<template>
  <div v-loading="loading" class="template-preview-container">
    <div class="header">
      <el-page-header content="模板详情" @back="router.back()" />
    </div>
    <div class="template-preview-content">
      <div v-if="!isAdmin" class="use-btn">
        <el-button type="primary" @click="createSurveyByTemplate" :loading="createSurveyLoading">
          使用该模板
        </el-button>
      </div>
      <el-scrollbar class="template-scrollbar">
        <div v-if="formItems.length > 0" class="preview-wrapper">
          <!-- 表单预览卡片 -->
          <div class="form-preview-card">
            <!-- 标题 -->
            <div v-if="templateInfo?.name" class="form-title">
              {{ templateInfo.name }}
            </div>
            <!-- 描述 -->
            <p v-if="templateInfo?.description" class="form-description">
              {{ templateInfo.description }}
            </p>
                <!-- 表单项 -->
                <SurveyFormRender
                  :form-items="formItems"
                  :form-model="previewFormModel"
                  :preview-mode="false"
                  :theme-config="themeConfig"
                  :show-number="false"
                  :form-logic="formLogic"
                />
          </div>
        </div>
        <div v-else class="empty-tip">
          <el-empty description="模板数据加载中..." />
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
/**
 * 模板预览页面
 * 功能：预览问卷模板的详细内容，支持使用模板创建新问卷
 */

import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { templateApi, formApi } from '@/api'
import SurveyFormRender from '@/components/SurveyFormRender.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo?.role === 'ADMIN'
})

const loading = ref(false)
const createSurveyLoading = ref(false)
const formKey = ref('')
const formItems = ref([])
const templateInfo = ref(null)
const previewFormModel = reactive({})
const formLogic = ref([])

// 主题配置
const themeConfig = reactive({
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  showTitle: true,
  showDescribe: true,
  showNumber: false,
  showSubmitBtn: false
})

// 加载模板信息
const loadTemplateInfo = async () => {
  if (!formKey.value) return
  
  try {
    const res = await templateApi.getTemplateDetails(formKey.value)
    if (res.code === 200 && res.data) {
      templateInfo.value = res.data
    }
  } catch (error) {
    console.error('加载模板信息失败:', error)
  }
}

// 加载模板数据
const loadTemplateData = async () => {
  if (!formKey.value) return
  
  loading.value = true
  try {
    // 加载模板信息
    await loadTemplateInfo()
    
    // 从模板信息中提取逻辑规则
    if (templateInfo.value?.scheme?.formLogic?.scheme) {
      formLogic.value = templateInfo.value.scheme.formLogic.scheme
    }
    
    // 加载表单项
    const itemsRes = await formApi.getFormItems(formKey.value)
    if (itemsRes.code === 200 && itemsRes.data && itemsRes.data.length > 0) {
      // 解析 scheme 字段，提取 vModel 和 config
      formItems.value = itemsRes.data.map(item => {
        let scheme = {}
        try {
          scheme = typeof item.scheme === 'string' 
            ? (item.scheme ? JSON.parse(item.scheme) : {})
            : (item.scheme || {})
        } catch (e) {
          console.error('解析scheme失败:', e, item.scheme)
          scheme = {}
        }
        
        return {
          formItemId: item.formItemId,
          type: item.type,
          label: item.label,
          vModel: scheme.vModel || item.formItemId,
          placeholder: scheme.placeholder !== undefined ? scheme.placeholder : (item.placeholder || ''),
          required: scheme.required !== undefined ? scheme.required : (item.required === 1),
          disabled: scheme.disabled || false,
          readonly: scheme.readonly || false,
          defaultValue: scheme.defaultValue !== undefined ? scheme.defaultValue : (item.defaultValue || ''),
          config: scheme.config || {},
          sort: item.sort || 0
        }
      }).sort((a, b) => {
        // 确保按 sort 排序
        return (a.sort || 0) - (b.sort || 0)
      })
      
      // 初始化表单模型
      initPreviewForm()
    } else {
      formItems.value = []
    }
  } catch (error) {
    console.error('加载模板数据失败:', error)
    ElMessage.error('加载模板数据失败')
    formItems.value = []
  } finally {
    loading.value = false
  }
}

// 初始化预览表单数据
const initPreviewForm = () => {
  // 清空之前的表单数据
  Object.keys(previewFormModel).forEach(key => {
    delete previewFormModel[key]
  })
  
  // 初始化表单模型
  formItems.value.forEach(item => {
    if (item.type === 'CHECKBOX') {
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
    } else if (item.type === 'RATE') {
      const numValue = item.defaultValue !== null && item.defaultValue !== undefined && item.defaultValue !== ''
        ? Number(item.defaultValue)
        : 0
      previewFormModel[item.vModel] = isNaN(numValue) ? 0 : numValue
    } else if (item.type === 'DIVIDER' || item.type === 'IMAGE' || item.type === 'IMAGE_CAROUSEL') {
      previewFormModel[item.vModel] = null
    } else {
      previewFormModel[item.vModel] = item.defaultValue !== null && item.defaultValue !== undefined ? item.defaultValue : ''
    }
  })
}

// 监听formItems变化，重新初始化表单模型
watch(() => formItems.value.length, () => {
  if (formItems.value.length > 0) {
    initPreviewForm()
  }
})

onMounted(async () => {
  formKey.value = route.query.key || ''
  if (!formKey.value) {
    ElMessage.error('模板不存在')
    router.back()
    return
  }
  
  // 加载模板数据
  await loadTemplateData()
})

// 使用模板创建问卷
const createSurveyByTemplate = async () => {
  if (!formKey.value) {
    ElMessage.error('模板不存在')
    return
  }

  createSurveyLoading.value = true
  try {
    const res = await templateApi.useTemplateCreateSurvey(formKey.value)
    if (res.code === 200 && res.data) {
      ElMessage.success('创建成功')
      // 跳转到问卷设计页面
      router.push({
        path: '/survey/edit/editor',
        query: { id: res.data }
      })
    } else {
      ElMessage.error('创建失败')
    }
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    createSurveyLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.template-preview-container {
  width: 100%;
  height: 100%;
}

.header {
  padding: 20px;
}

.template-scrollbar {
  height: calc(100% - 64px);
}

.template-preview-content {
  position: relative;
  background-color: #f5f7fa;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;

  .use-btn {
    position: absolute;
    top: -50px;
    right: 30px;
    z-index: 10;
  }
}

.template-scrollbar {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}

.preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100%;
  padding: 20px;
}

.form-preview-card {
  width: 100%;
  max-width: 800px;
  background: #ffffff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
  
  .form-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
    text-align: left;
    line-height: 1.5;
  }
  
  .form-description {
    font-size: 14px;
    color: #606266;
    margin-bottom: 30px;
    text-align: left;
    line-height: 1.6;
  }
}

.empty-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>

