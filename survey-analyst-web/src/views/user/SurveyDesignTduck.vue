<template>
  <div class="survey-design-tduck-container">
    <!-- 顶部：问卷基本信息 -->
    <div class="survey-header">
      <el-card class="survey-info-card">
        <el-form :model="surveyForm" label-width="100px" :inline="true">
          <el-form-item label="问卷标题">
            <el-input v-model="surveyForm.title" placeholder="请输入问卷标题" style="width: 300px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存</el-button>
            <el-button @click="handlePreview">预览</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 主体：表单设计器 -->
    <div class="design-main">
      <!-- 使用 tduck-form-generator 的 FormDesign 组件 -->
      <div class="form-designer-wrapper">
        <FormDesign 
          v-if="formKey" 
          :question-mode="true"
          :form-key="formKey"
          @save="handleFormSave"
        />
        <div v-else class="loading-container">
          <el-empty description="正在初始化表单设计器..." />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { surveyApi } from '@/api'
// 尝试导入 tduck-form-generator
// 注意：tduck-form-generator 是 Vue2 库，可能需要兼容层
let FormDesign = null
try {
  // 尝试动态导入
  const tduckForm = await import('tduck-form-generator')
  FormDesign = tduckForm.FormDesign || tduckForm.default?.FormDesign
  if (FormDesign) {
    // 导入样式
    await import('tduck-form-generator/dist/TduckForm.css')
  }
} catch (error) {
  console.warn('tduck-form-generator 导入失败，将使用自定义实现:', error)
}

const route = useRoute()
const router = useRouter()

const surveyForm = reactive({
  id: null,
  title: '',
  description: '',
  formKey: null
})

const formKey = ref(null)
const loading = ref(false)

// 初始化表单
const initForm = async () => {
  const surveyId = route.query.id
  if (surveyId) {
    try {
      const res = await surveyApi.getSurveyById(surveyId)
      if (res.code === 200) {
        Object.assign(surveyForm, res.data)
        // 如果已有 formKey，使用它；否则生成新的
        formKey.value = res.data.formKey || generateFormKey()
      }
    } catch (error) {
      ElMessage.error('加载问卷失败')
    }
  } else {
    // 新建问卷，生成 formKey
    formKey.value = generateFormKey()
  }
}

// 生成表单唯一标识
const generateFormKey = () => {
  return 'form_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 保存表单
const handleSave = async () => {
  if (!surveyForm.title) {
    ElMessage.warning('请输入问卷标题')
    return
  }

  loading.value = true
  try {
    const formData = {
      ...surveyForm,
      formKey: formKey.value
    }
    
    let res
    if (surveyForm.id) {
      res = await surveyApi.updateSurvey(surveyForm.id, formData)
    } else {
      res = await surveyApi.createSurvey(formData)
      if (res.code === 200) {
        surveyForm.id = res.data.id
        // 更新路由参数
        router.replace({ query: { id: surveyForm.id } })
      }
    }

    if (res.code === 200) {
      ElMessage.success('保存成功')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

// 表单设计器保存回调
const handleFormSave = (formData) => {
  console.log('表单设计器保存:', formData)
  ElMessage.success('表单配置已保存')
}

// 预览
const handlePreview = () => {
  if (!formKey.value) {
    ElMessage.warning('请先保存问卷')
    return
  }
  // TODO: 打开预览窗口
  ElMessage.info('预览功能开发中')
}

onMounted(() => {
  initForm()
})
</script>

<style scoped>
.survey-design-tduck-container {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.survey-header {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.survey-info-card {
  margin: 0;
}

.design-main {
  flex: 1;
  overflow: hidden;
}

.form-designer-wrapper {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>

