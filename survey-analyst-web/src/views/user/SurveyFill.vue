<template>
  <div class="survey-fill-container">
    <div class="fill-content">
      <el-card v-loading="loading" class="survey-card">
        <template #header>
          <h2 class="survey-title">{{ survey.title }}</h2>
          <p v-if="survey.description" class="survey-description">{{ survey.description }}</p>
        </template>

        <el-form ref="fillFormRef" :model="formData" label-width="0">
          <div
            v-for="(question, index) in questions"
            :key="question.id"
            class="question-item"
          >
            <div class="question-header">
              <span class="question-number">题目 {{ index + 1 }}</span>
              <el-tag v-if="question.required" type="danger" size="small">必填</el-tag>
            </div>
            <div class="question-title">{{ question.title }}</div>
            <div v-if="question.description" class="question-description">
              {{ question.description }}
            </div>

            <!-- 单选题 -->
            <div v-if="question.type === 'SINGLE_CHOICE'" class="question-answer">
              <el-radio-group v-model="formData[question.id]">
                <el-radio
                  v-for="option in getOptionsByQuestionId(question.id)"
                  :key="option.id"
                  :label="option.id"
                >
                  {{ option.content }}
                </el-radio>
              </el-radio-group>
            </div>

            <!-- 多选题 -->
            <div v-if="question.type === 'MULTIPLE_CHOICE'" class="question-answer">
              <el-checkbox-group v-model="formData[question.id]">
                <el-checkbox
                  v-for="option in getOptionsByQuestionId(question.id)"
                  :key="option.id"
                  :label="option.id"
                >
                  {{ option.content }}
                </el-checkbox>
              </el-checkbox-group>
            </div>

            <!-- 单行文本 -->
            <div v-if="question.type === 'TEXT'" class="question-answer">
              <el-input
                v-model="formData[question.id]"
                placeholder="请输入答案"
              />
            </div>

            <!-- 多行文本 -->
            <div v-if="question.type === 'TEXTAREA'" class="question-answer">
              <el-input
                v-model="formData[question.id]"
                type="textarea"
                :rows="4"
                placeholder="请输入答案"
              />
            </div>

            <!-- 评分题 -->
            <div v-if="question.type === 'RATING'" class="question-answer">
              <el-rate v-model="formData[question.id]" :max="5" />
            </div>
          </div>

          <div class="submit-buttons">
            <el-button @click="handleSaveDraft">保存草稿</el-button>
            <el-button type="primary" @click="handleSubmit">提交</el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { surveyApi, questionApi, optionApi, responseApi } from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const survey = ref({})
const questions = ref([])
const options = ref([])
const formData = reactive({})
const fillFormRef = ref()

const getOptionsByQuestionId = (questionId) => {
  return options.value.filter(opt => opt.questionId === questionId)
}

const loadSurveyData = async () => {
  loading.value = true
  try {
    const surveyId = route.params.id
    const surveyRes = await surveyApi.getSurveyById(surveyId)
    if (surveyRes.code === 200) {
      survey.value = surveyRes.data
    }

    const questionsRes = await questionApi.getQuestionsBySurveyId(surveyId)
    if (questionsRes.code === 200) {
      questions.value = questionsRes.data || []
      
      // 初始化表单数据
      questions.value.forEach(question => {
        if (question.type === 'MULTIPLE_CHOICE') {
          formData[question.id] = []
        } else {
          formData[question.id] = null
        }
      })

      // 加载所有题目的选项
      for (const question of questions.value) {
        if (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') {
          const optionsRes = await optionApi.getOptionsByQuestionId(question.id)
          if (optionsRes.code === 200) {
            options.value.push(...(optionsRes.data || []))
          }
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载问卷失败')
  } finally {
    loading.value = false
  }
}

const validateForm = () => {
  for (const question of questions.value) {
    if (question.required) {
      const value = formData[question.id]
      if (value === null || value === undefined || value === '' || 
          (Array.isArray(value) && value.length === 0)) {
        ElMessage.warning(`请填写题目：${question.title}`)
        return false
      }
    }
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  try {
    const surveyId = route.params.id
    const answers = {}
    
    // 转换表单数据为答案格式
    for (const question of questions.value) {
      const value = formData[question.id]
      if (value !== null && value !== undefined && value !== '') {
        if (Array.isArray(value)) {
          // 多选题，保存多个选项ID
          answers[question.id] = value
        } else {
          answers[question.id] = value
        }
      }
    }

    const responseData = {
      surveyId: parseInt(surveyId),
      answers,
      ipAddress: '',
      deviceType: /Mobile|Android|iPhone/i.test(navigator.userAgent) ? 'MOBILE' : 'PC'
    }

    const res = await responseApi.submitResponse(responseData)
    if (res.code === 200) {
      ElMessage.success('提交成功，感谢您的参与！')
      router.push('/home')
    }
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    loading.value = false
  }
}

const handleSaveDraft = async () => {
  loading.value = true
  try {
    const surveyId = route.params.id
    const answers = {}
    
    for (const question of questions.value) {
      const value = formData[question.id]
      if (value !== null && value !== undefined && value !== '') {
        if (Array.isArray(value)) {
          // 多选题，保存多个选项ID
          answers[question.id] = value
        } else {
          answers[question.id] = value
        }
      }
    }

    const responseData = {
      surveyId: parseInt(surveyId),
      answers,
      ipAddress: '',
      deviceType: /Mobile|Android|iPhone/i.test(navigator.userAgent) ? 'MOBILE' : 'PC'
    }

    const res = await responseApi.saveDraft(responseData)
    if (res.code === 200) {
      ElMessage.success('草稿保存成功')
    }
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSurveyData()
})
</script>

<style scoped>
.survey-fill-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.fill-content {
  max-width: 800px;
  margin: 0 auto;
}

.survey-card {
  margin-bottom: 20px;
}

.survey-title {
  margin: 0 0 10px 0;
  color: #303133;
}

.survey-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.question-item {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.question-number {
  font-weight: 500;
  color: #409EFF;
}

.question-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.question-description {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
}

.question-answer {
  margin-top: 15px;
}

.submit-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>

