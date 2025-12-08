<template>
  <div class="survey-design-container">
    <!-- 顶部：问卷基本信息 -->
    <div class="survey-header">
      <el-card class="survey-info-card">
        <el-form
          :model="surveyForm"
          label-width="100px"
          :inline="true"
        >
          <el-form-item label="问卷标题">
            <el-input
              v-model="surveyForm.title"
              placeholder="请输入问卷标题"
              style="width: 300px"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleSave"
            >
              保存
            </el-button>
            <el-button @click="handlePreview">
              预览
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 主体：三栏布局 -->
    <div class="design-main">
      <!-- 左侧：组件库 -->
      <div class="left-board">
        <el-card class="component-library-card">
          <template #header>
            <div class="library-header">
              题型库
            </div>
          </template>
          <div class="components-list">
            <div
              v-for="type in questionTypes"
              :key="type.value"
              class="components-item"
              :draggable="true"
              @dragstart="handleDragStart($event, type)"
              @dragend="handleDragEnd"
            >
              <div class="components-body">
                <div class="type-icon">
                  {{ type.icon }}
                </div>
                <div class="type-name">
                  {{ type.label }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 中间：设计区域 -->
      <div class="center-board">
        <el-card class="design-area-card">
          <template #header>
            <div class="design-header">
              <span>设计区域</span>
              <el-button
                type="primary"
                size="small"
                @click="showQuestionTypeDialog = true"
              >
                <el-icon><Plus /></el-icon>
                添加题目
              </el-button>
            </div>
          </template>
          
          <div
            class="drawing-board"
            @drop="handleDrop"
            @dragover.prevent
            @dragenter.prevent
            @dragleave.prevent
          >
            <VueDraggable
              :key="`draggable-${questions.length}`"
              v-model="questions"
              item-key="id"
              handle=".drag-handle"
              :animation="200"
              ghost-class="sortable-ghost"
              chosen-class="chosen-item"
              drag-class="drag-item"
              @end="handleQuestionOrderChange"
            >
              <template #item="{ element, index }">
                <div
                  class="drawing-item"
                  :class="{ 'active-from-item': selectedQuestionId === element.id }"
                  @click.stop="handleSelectQuestion(element.id)"
                >
                  <div class="component-name">
                    {{ getQuestionTypeText(element.type) }}
                  </div>
                  <el-card
                    class="question-card"
                    shadow="hover"
                  >
                    <div class="question-header">
                      <el-icon class="drag-handle">
                        <Rank />
                      </el-icon>
                      <span class="question-number">题目 {{ index + 1 }}</span>
                      <el-tag size="small">
                        {{ getQuestionTypeText(element.type) }}
                      </el-tag>
                      <div style="flex: 1" />
                      <el-button
                        type="primary"
                        size="small"
                        text
                        @click="handleCopyQuestion(element)"
                      >
                        复制
                      </el-button>
                      <el-button
                        type="danger"
                        size="small"
                        text
                        @click="handleDeleteQuestion(element.id)"
                      >
                        删除
                      </el-button>
                    </div>
                    <el-form
                      :model="element"
                      label-width="80px"
                    >
                      <el-form-item label="题目标题">
                        <el-input
                          v-model="element.title"
                          placeholder="请输入题目标题"
                          @blur="handleUpdateQuestion(element)"
                        />
                      </el-form-item>
                      <el-form-item label="题目描述">
                        <el-input
                          v-model="element.description"
                          type="textarea"
                          :rows="2"
                          placeholder="请输入题目描述（可选）"
                          @blur="handleUpdateQuestion(element)"
                        />
                      </el-form-item>
                      <el-form-item label="是否必填">
                        <el-switch
                          v-model="element.required"
                          :active-value="1"
                          :inactive-value="0"
                          @change="handleUpdateQuestion(element)"
                        />
                      </el-form-item>
                      <div
                        v-if="isChoiceQuestion(element.type)"
                        class="options-section"
                      >
                        <div class="options-header">
                          <span>选项列表</span>
                          <el-button
                            type="primary"
                            size="small"
                            @click="handleAddOption(element.id)"
                          >
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
                            @blur="handleUpdateOption(option)"
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
                </div>
              </template>
            </VueDraggable>
            

            <div
              v-if="questions.length === 0"
              class="empty-info"
            >
              <el-empty description="从左侧拖拽题型到此处，或点击上方按钮添加题目" />
            </div>
            <!-- 调试信息 -->
            <div
              v-if="false"
              style="padding: 10px; background: #f0f0f0; margin-top: 10px; font-size: 12px;"
            >
              <div>题目数量: {{ questions.length }}</div>
              <div>题目列表: {{ JSON.stringify(questions.map(q => ({ id: q.id, title: q.title, type: q.type }))) }}</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：属性配置面板 -->
      <div class="right-board">
        <el-card class="property-panel-card">
          <template #header>
            <div class="property-header">
              组件属性
            </div>
          </template>
          <div
            v-if="selectedQuestion"
            class="property-content"
          >
            <el-form
              :model="selectedQuestion"
              label-width="100px"
              label-position="top"
            >
              <el-form-item label="题目标题">
                <el-input
                  v-model="selectedQuestion.title"
                  @blur="handleUpdateQuestion(selectedQuestion)"
                />
              </el-form-item>
              <el-form-item label="题目描述">
                <el-input
                  v-model="selectedQuestion.description"
                  type="textarea"
                  :rows="3"
                  @blur="handleUpdateQuestion(selectedQuestion)"
                />
              </el-form-item>
              <el-form-item label="是否必填">
                <el-switch
                  v-model="selectedQuestion.required"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleUpdateQuestion(selectedQuestion)"
                />
              </el-form-item>
              <el-form-item label="题型">
                <el-tag>
                  {{ getQuestionTypeText(selectedQuestion.type) }}
                </el-tag>
              </el-form-item>
            </el-form>
          </div>
          <div
            v-else
            class="empty-property"
          >
            <el-empty
              description="请选择一个题目进行配置"
              :image-size="80"
            />
          </div>
        </el-card>
      </div>
    </div>

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
          <div class="type-icon">
            {{ type.icon }}
          </div>
          <div class="type-name">
            {{ type.label }}
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Rank } from '@element-plus/icons-vue'
import { surveyApi, questionApi, optionApi } from '@/api'
// vue-draggable-plus 的正确导入方式
import { VueDraggable } from 'vue-draggable-plus'

const route = useRoute()

const surveyForm = reactive({
  id: null,
  title: '',
  description: ''
})

const questions = ref([])
const options = ref([])
const showQuestionTypeDialog = ref(false)
const loading = ref(false)
const selectedQuestionId = ref(null)
const draggedType = ref(null)

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

const selectedQuestion = computed(() => {
  return questions.value.find(q => q.id === selectedQuestionId.value)
})

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

// 选中题目
const handleSelectQuestion = (questionId) => {
  selectedQuestionId.value = questionId
}

// 拖拽开始
const handleDragStart = (event, type) => {
  draggedType.value = type
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.setData('text/plain', type.value)
}

// 拖拽结束
const handleDragEnd = () => {
  draggedType.value = null
}

// 拖拽放置
const handleDrop = async (event) => {
  event.preventDefault()
  event.stopPropagation()
  const type = draggedType.value
  if (!type) {
    // 尝试从 dataTransfer 获取
    const typeValue = event.dataTransfer.getData('text/plain')
    if (typeValue) {
      const foundType = questionTypes.find(t => t.value === typeValue)
      if (foundType) {
        await handleAddQuestion(foundType.value)
      }
    }
    return
  }

  if (!surveyForm.id) {
    ElMessage.warning('请先保存问卷基本信息')
    return
  }

  await handleAddQuestion(type.value)
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
    if (res.code === 200 && res.data) {
      // 确保使用响应式方式添加
      const newQ = { ...res.data }
      
      // 直接添加到数组
      questions.value.push(newQ)
      
      // 强制触发响应式更新 - 创建新数组引用
      questions.value = [...questions.value]
      
      // 使用 nextTick 确保 DOM 更新
      await nextTick()
      selectedQuestionId.value = newQ.id
      showQuestionTypeDialog.value = false
      ElMessage.success('添加成功')
    } else {
      ElMessage.error('添加失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('添加题目失败：' + (error.message || '网络错误'))
  }
}

const handleCopyQuestion = async (question) => {
  if (!surveyForm.id) {
    ElMessage.warning('请先保存问卷基本信息')
    return
  }

  const newQuestion = {
    surveyId: surveyForm.id,
    type: question.type,
    title: question.title + ' (副本)',
    description: question.description,
    required: question.required,
    orderNum: questions.value.length
  }

  try {
    const res = await questionApi.addQuestion(newQuestion)
    if (res.code === 200) {
      questions.value.push(res.data)
      
      // 如果是选择题，复制选项
      if (isChoiceQuestion(question.type)) {
        const oldOptions = getOptionsByQuestionId(question.id)
        for (const oldOption of oldOptions) {
          const newOption = {
            questionId: res.data.id,
            content: oldOption.content,
            orderNum: oldOption.orderNum
          }
          const optionRes = await optionApi.addOption(newOption)
          if (optionRes.code === 200) {
            options.value.push(optionRes.data)
          }
        }
      }
      
      ElMessage.success('复制成功')
    }
  } catch (error) {
    ElMessage.error('复制题目失败')
  }
}

const handleDeleteQuestion = async (questionId) => {
  try {
    const res = await questionApi.deleteQuestion(questionId)
    if (res.code === 200) {
      questions.value = questions.value.filter(q => q.id !== questionId)
      options.value = options.value.filter(opt => opt.questionId !== questionId)
      if (selectedQuestionId.value === questionId) {
        selectedQuestionId.value = null
      }
      ElMessage.success('删除成功')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleUpdateQuestion = async (question) => {
  if (!question.id) return
  
  try {
    await questionApi.updateQuestion(question.id, {
      title: question.title,
      description: question.description,
      required: question.required
    })
  } catch (error) {
    ElMessage.error('更新失败')
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

const handleUpdateOption = async (option) => {
  if (!option.id) return
  
  try {
    await optionApi.updateOption(option.id, {
      content: option.content
    })
  } catch (error) {
    ElMessage.error('更新失败')
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
    id: q.id,
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

const handlePreview = () => {
  if (!surveyForm.id) {
    ElMessage.warning('请先保存问卷')
    return
  }
  // TODO: 实现预览功能
  ElMessage.info('预览功能开发中')
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
  display: flex;
  overflow: hidden;
}

/* 左侧组件库 */
.left-board {
  width: 260px;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
  background: #fff;
}

.component-library-card {
  border: none;
  border-radius: 0;
  height: 100%;
}

.library-header {
  font-weight: 500;
  font-size: 14px;
}

.components-list {
  padding: 8px;
}

.components-item {
  display: inline-block;
  width: 48%;
  margin: 1%;
  cursor: move;
}

.components-body {
  padding: 12px 8px;
  background: rgba(24, 144, 255, 0.05);
  border: 1px dashed rgba(24, 144, 255, 0.1);
  border-radius: 4px;
  text-align: center;
  transition: all 0.3s;
}

.components-body:hover {
  border-color: #409EFF;
  background: rgba(24, 144, 255, 0.1);
  transform: translateY(-2px);
}

.type-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.type-name {
  font-size: 12px;
  color: #606266;
}

/* 中间设计区域 */
.center-board {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 20px;
}

.design-area-card {
  min-height: 100%;
  border: none;
}

.design-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  font-size: 14px;
}

.drawing-board {
  min-height: 400px;
  padding: 20px;
}

.drawing-item {
  position: relative;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.drawing-item:hover {
  background: rgba(24, 144, 255, 0.05);
  border-radius: 4px;
}

.drawing-item.active-from-item {
  border: 2px solid #409EFF !important;
  border-radius: 4px;
  padding: 5px;
  background: rgba(24, 144, 255, 0.08);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.component-name {
  position: absolute;
  top: -8px;
  left: 10px;
  font-size: 12px;
  color: #bbb;
  background: #fff;
  padding: 0 6px;
  z-index: 1;
}

.drawing-item.active-from-item .component-name {
  color: #409EFF;
}

.question-card {
  margin: 0;
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
  font-size: 18px;
}

.drag-handle:hover {
  color: #409EFF;
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

.empty-info {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

/* 拖拽样式 */
.sortable-ghost {
  opacity: 0.5;
  background: #f0f0f0;
  border: 2px dashed #409EFF;
}

.chosen-item {
  cursor: grabbing;
}

.drag-item {
  cursor: grabbing;
}

/* 右侧属性面板 */
.right-board {
  width: 300px;
  border-left: 1px solid #ebeef5;
  overflow-y: auto;
  background: #fff;
}

.property-panel-card {
  border: none;
  border-radius: 0;
  height: 100%;
}

.property-header {
  font-weight: 500;
  font-size: 14px;
}

.property-content {
  padding: 10px 0;
}

.empty-property {
  padding: 40px 20px;
  text-align: center;
}

/* 题目类型选择对话框 */
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
</style>
