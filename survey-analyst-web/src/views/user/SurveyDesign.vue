<template>
  <div class="survey-design-container">
    <el-card class="survey-info-card">
      <template #header>
        <div class="card-header">
          <span>问卷基本信息</span>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </div>
      </template>
      <el-form :model="surveyForm" label-width="100px">
        <el-form-item label="问卷标题">
          <el-input v-model="surveyForm.title" placeholder="请输入问卷标题" />
        </el-form-item>
        <el-form-item label="问卷描述">
          <el-input
            v-model="surveyForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入问卷描述"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="questions-card">
      <template #header>
        <div class="card-header">
          <span>题目列表</span>
          <el-button type="primary" @click="showQuestionTypeDialog = true">
            <el-icon><Plus /></el-icon>
            添加题目
          </el-button>
        </div>
      </template>

      <draggable
        v-model="questions"
        item-key="id"
        handle=".drag-handle"
        @end="handleQuestionOrderChange"
      >
        <template #item="{ element, index }">
          <el-card class="question-card" shadow="hover">
            <div class="question-header">
              <el-icon class="drag-handle"><Rank /></el-icon>
              <span class="question-number">题目 {{ index + 1 }}</span>
              <el-tag size="small">{{ getQuestionTypeText(element.type) }}</el-tag>
              <el-button
                type="danger"
                size="small"
                text
                @click="handleDeleteQuestion(element.id)"
              >
                删除
              </el-button>
            </div>
            <el-form :model="element" label-width="80px">
              <el-form-item label="题目标题">
                <el-input
                  v-model="element.title"
                  placeholder="请输入题目标题"
                />
              </el-form-item>
              <el-form-item label="题目描述">
                <el-input
                  v-model="element.description"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入题目描述（可选）"
                />
              </el-form-item>
              <el-form-item label="是否必填">
                <el-switch v-model="element.required" :active-value="1" :inactive-value="0" />
              </el-form-item>
              <div v-if="isChoiceQuestion(element.type)" class="options-section">
                <div class="options-header">
                  <span>选项列表</span>
                  <el-button type="primary" size="small" @click="handleAddOption(element.id)">
                    添加选项
                  </el-button>
                </div>
                <div
                  v-for="(option, optIndex) in getOptionsByQuestionId(element.id)"
                  :key="option.id || optIndex"
                  class="option-item"
                >
                  <el-input
                    v-model="option.content"
                    :placeholder="`选项 ${optIndex + 1}`"
                    style="flex: 1"
                  />
                  <el-button
                    type="danger"
                    size="small"
                    text
                    @click="handleDeleteOption(option.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </el-form>
          </el-card>
        </template>
      </draggable>

      <el-empty v-if="questions.length === 0" description="暂无题目，请添加题目" />
    </el-card>

    <!-- 题目类型选择对话框 -->
    <el-dialog
      v-model="showQuestionTypeDialog"
      title="选择题目类型"
      width="600px"
    >
      <div class="question-types-grid">
        <el-card
          v-for="type in questionTypes"
          :key="type.value"
          class="question-type-card"
          shadow="hover"
          @click="handleAddQuestion(type.value)"
        >
          <div class="type-icon">{{ type.icon }}</div>
          <div class="type-name">{{ type.label }}</div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Rank } from '@element-plus/icons-vue'
import draggable from 'vue-draggable-plus'
import { surveyApi, questionApi, optionApi } from '@/api'

const route = useRoute()
const router = useRouter()

const surveyForm = reactive({
  id: null,
  title: '',
  description: ''
})

const questions = ref([])
const options = ref([])
const showQuestionTypeDialog = ref(false)
const loading = ref(false)

const questionTypes = [
  { label: '单选题', value: 'SINGLE_CHOICE', icon: '○' },
  { label: '多选题', value: 'MULTIPLE_CHOICE', icon: '☑' },
  { label: '单行文本', value: 'TEXT', icon: '📝' },
  { label: '多行文本', value: 'TEXTAREA', icon: '📄' },
  { label: '评分题', value: 'RATING', icon: '⭐' },
  { label: '排序题', value: 'SORT', icon: '🔢' },
  { label: '矩阵题', value: 'MATRIX', icon: '📊' },
  { label: '文件上传', value: 'FILE', icon: '📎' },
  { label: '日期时间', value: 'DATE', icon: '📅' }
]

const isChoiceQuestion = (type) => {
  return type === 'SINGLE_CHOICE' || type === 'MULTIPLE_CHOICE'
}

const getQuestionTypeText = (type) => {
  const typeMap = questionTypes.find(t => t.value === type)
  return typeMap ? typeMap.label : type
}

const getOptionsByQuestionId = (questionId) => {
  return options.value.filter(opt => opt.questionId === questionId)
}

const handleAddQuestion = async (type) => {
  if (!surveyForm.id) {
    ElMessage.warning('请先保存问卷基本信息')
    return
  }

  const newQuestion = {
    surveyId: surveyForm.id,
    type,
    title: '新题目',
    description: '',
    required: 0,
    orderNum: questions.value.length
  }

  try {
    const res = await questionApi.addQuestion(newQuestion)
    if (res.code === 200) {
      questions.value.push(res.data)
      showQuestionTypeDialog.value = false
      ElMessage.success('添加成功')
    }
  } catch (error) {
    ElMessage.error('添加题目失败')
  }
}

const handleDeleteQuestion = async (questionId) => {
  try {
    const res = await questionApi.deleteQuestion(questionId)
    if (res.code === 200) {
      questions.value = questions.value.filter(q => q.id !== questionId)
      options.value = options.value.filter(opt => opt.questionId !== questionId)
      ElMessage.success('删除成功')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleAddOption = async (questionId) => {
  const newOption = {
    questionId,
    content: '新选项',
    orderNum: getOptionsByQuestionId(questionId).length
  }

  try {
    const res = await optionApi.addOption(newOption)
    if (res.code === 200) {
      options.value.push(res.data)
      ElMessage.success('添加成功')
    }
  } catch (error) {
    ElMessage.error('添加选项失败')
  }
}

const handleDeleteOption = async (optionId) => {
  try {
    const res = await optionApi.deleteOption(optionId)
    if (res.code === 200) {
      options.value = options.value.filter(opt => opt.id !== optionId)
      ElMessage.success('删除成功')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleQuestionOrderChange = async () => {
  const updateList = questions.value.map((q, index) => ({
    ...q,
    orderNum: index
  }))
  
  try {
    await questionApi.updateQuestionOrder(updateList)
  } catch (error) {
    ElMessage.error('更新排序失败')
  }
}

const handleSave = async () => {
  if (!surveyForm.title) {
    ElMessage.warning('请输入问卷标题')
    return
  }

  loading.value = true
  try {
    let res
    if (surveyForm.id) {
      res = await surveyApi.updateSurvey(surveyForm.id, surveyForm)
    } else {
      res = await surveyApi.createSurvey(surveyForm)
      if (res.code === 200) {
        surveyForm.id = res.data.id
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

const loadSurveyData = async () => {
  const surveyId = route.query.id
  if (!surveyId) return

  try {
    const surveyRes = await surveyApi.getSurveyById(surveyId)
    if (surveyRes.code === 200) {
      Object.assign(surveyForm, surveyRes.data)
    }

    const questionsRes = await questionApi.getQuestionsBySurveyId(surveyId)
    if (questionsRes.code === 200) {
      questions.value = questionsRes.data || []
      
      // 加载所有题目的选项
      for (const question of questions.value) {
        if (isChoiceQuestion(question.type)) {
          const optionsRes = await optionApi.getOptionsByQuestionId(question.id)
          if (optionsRes.code === 200) {
            options.value.push(...(optionsRes.data || []))
          }
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadSurveyData()
})
</script>

<style scoped>
.survey-design-container {
  padding: 20px;
}

.survey-info-card,
.questions-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-card {
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.drag-handle {
  cursor: move;
  color: #909399;
}

.question-number {
  font-weight: 500;
  color: #303133;
}

.options-section {
  margin-top: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.options-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.option-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.question-types-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.question-type-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.question-type-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.type-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.type-name {
  font-size: 14px;
  color: #606266;
}
</style>

