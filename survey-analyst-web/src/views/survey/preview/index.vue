<template>
  <div v-loading="loading" class="survey-preview-container">
    <div class="header">
      <el-page-header :content="surveyInfo?.title || '问卷预览'" @back="router.back()" />
    </div>
    <div class="survey-preview-content">
      <el-scrollbar class="survey-scrollbar">
        <div v-if="formItems.length > 0" class="preview-wrapper">
          <!-- 表单预览卡片 -->
          <div class="form-preview-card">
            <!-- 标题 -->
            <div v-if="surveyInfo?.title" class="form-title">
              {{ surveyInfo.title }}
            </div>
            <!-- 描述 -->
            <p v-if="surveyInfo?.description" class="form-description">
              {{ surveyInfo.description }}
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
          <el-empty description="问卷数据加载中..." />
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
/**
 * 问卷预览页面
 * 功能：预览问卷内容，显示问卷标题、描述和表单项，支持主题配置
 */

import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { surveyApi, formApi } from '@/api'
import SurveyFormRender from '@/components/SurveyFormRender.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const surveyId = ref(null)
const formKey = ref('')
const formItems = ref([])
const surveyInfo = ref(null)
const previewFormModel = reactive({})
const formLogic = ref([])

// 主题配置
const themeConfig = reactive({
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  showTitle: true,
  showDescribe: true,
  showNumber: false,
  showSubmitBtn: false // 预览模式不显示提交按钮
})

/**
 * 加载问卷信息
 * 从后端获取问卷的基本信息（标题、描述等）
 */
const loadSurveyInfo = async () => {
  if (!surveyId.value) return
  
  try {
    const res = await surveyApi.getSurveyById(surveyId.value)
    if (res.code === 200 && res.data) {
      surveyInfo.value = res.data
    }
  } catch (error) {
    console.error('加载问卷信息失败:', error)
    ElMessage.error('加载问卷信息失败')
  }
}

/**
 * 加载问卷数据
 * 加载问卷信息、表单配置、表单项和逻辑规则，并初始化预览表单模型
 */
const loadSurveyData = async () => {
  if (!surveyId.value) return
  
  loading.value = true
  try {
    // 加载问卷信息
    await loadSurveyInfo()
    
    // 获取表单配置
    const configRes = await formApi.getFormConfig(surveyId.value)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      
      // 加载表单逻辑
      try {
        const logicRes = await formApi.getFormLogic(surveyId.value)
        if (logicRes.code === 200 && logicRes.data?.scheme) {
          formLogic.value = logicRes.data.scheme
        }
      } catch (error) {
        console.error('加载表单逻辑失败:', error)
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
            disabled: scheme.disabled || false, // 允许操作，使用配置的disabled状态
            readonly: scheme.readonly || false, // 允许操作，使用配置的readonly状态
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
    } else {
      ElMessage.error('问卷配置不存在')
      formItems.value = []
    }
  } catch (error) {
    console.error('加载问卷数据失败:', error)
    ElMessage.error('加载问卷数据失败')
    formItems.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 初始化预览表单数据
 * 根据表单项的类型和默认值，为预览表单模型初始化对应的值
 */
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
  surveyId.value = route.params.id ? Number(route.params.id) : (route.query.id ? Number(route.query.id) : null)
  if (!surveyId.value) {
    ElMessage.error('问卷不存在')
    router.back()
    return
  }
  
  // 加载问卷数据
  await loadSurveyData()
})
</script>

<style lang="scss" scoped>
.survey-preview-container {
  width: 100%;
  height: 100%;
}

.header {
  padding: 20px;
  background: white;
  border-bottom: 1px solid #ebeef5;
}

.survey-scrollbar {
  height: calc(100% - 64px);
}

.survey-preview-content {
  position: relative;
  background-color: #f5f7fa;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.survey-scrollbar {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px;
  }

  .survey-preview-content {
    height: calc(100vh - 80px);
  }

  .survey-scrollbar {
    padding: 15px;
  }

  .preview-wrapper {
    padding: 15px;
  }

  .form-preview-card {
    padding: 20px;
    
    .form-title {
      font-size: 20px;
      margin-bottom: 12px;
    }
    
    .form-description {
      font-size: 13px;
      margin-bottom: 20px;
    }
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px;
  }

  .survey-scrollbar {
    padding: 10px;
  }

  .preview-wrapper {
    padding: 10px;
  }

  .form-preview-card {
    padding: 15px;
    
    .form-title {
      font-size: 18px;
      margin-bottom: 10px;
    }
    
    .form-description {
      font-size: 12px;
      margin-bottom: 15px;
    }
  }
}
</style>

