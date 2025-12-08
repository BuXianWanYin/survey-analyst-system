<template>
  <div v-loading="createSurveyLoading" class="template-preview-container">
    <div class="header">
      <el-page-header content="模板详情" @back="router.back()" />
    </div>
    <div class="template-preview-content">
      <el-scrollbar class="template-scrollbar">
        <SurveyPreview
          v-if="formKey"
          :form-key="formKey"
          :survey-id="null"
          :preview-mode="true"
        />
      </el-scrollbar>
      <div class="use-btn">
        <el-button type="primary" @click="createSurveyByTemplate">使用该模板</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { templateApi } from '@/api'
import SurveyPreview from '@/components/SurveyPreview.vue'

const route = useRoute()
const router = useRouter()

const createSurveyLoading = ref(false)
const formKey = ref('')

onMounted(() => {
  formKey.value = route.query.key || ''
  if (!formKey.value) {
    ElMessage.error('模板不存在')
    router.back()
  }
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
        path: '/user/survey/design/editor',
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

  .use-btn {
    position: absolute;
    top: 50px;
    right: 180px;
  }
}
</style>

