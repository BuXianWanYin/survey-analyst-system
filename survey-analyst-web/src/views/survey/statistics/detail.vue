<template>
  <div class="statistics-detail">
    <div class="page-header">
      <h2 class="page-title">{{ survey.title }} - 统计详情</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card v-loading="loading">
      <div v-for="(question, index) in questions" :key="question.id" class="question-stat-item">
        <div class="question-header">
          <span class="question-number">题目 {{ index + 1 }}</span>
          <el-tag size="small">{{ getQuestionTypeText(question.type) }}</el-tag>
        </div>
        <div class="question-title">{{ question.title }}</div>

        <!-- 选择题统计 -->
        <div v-if="isChoiceQuestion(question.type)" class="question-chart">
          <div class="chart-controls">
            <el-button size="small" @click="showChartConfig(question.id)">自定义图表</el-button>
          </div>
          <v-chart
            v-if="getQuestionChartOption(question.id)"
            :option="getQuestionChartOption(question.id)"
            style="height: 300px"
            autoresize
          />
        </div>

        <!-- 填空题统计 -->
        <div v-else-if="isTextQuestion(question.type)" class="question-text-stat">
          <div class="text-stat-info">
            <span>有效答案数：{{ getQuestionStat(question.id, 'validAnswers') || 0 }}</span>
            <span>总答案数：{{ getQuestionStat(question.id, 'totalAnswers') || 0 }}</span>
          </div>
          <!-- 词云图 -->
          <div v-if="getQuestionWordCloudOption(question.id)" class="question-wordcloud">
            <h4>高频词分析</h4>
            <v-chart
              :option="getQuestionWordCloudOption(question.id)"
              style="height: 300px"
              autoresize
            />
          </div>
        </div>

        <!-- 排序题统计 -->
        <div v-else-if="question.type === 'SORT'" class="question-sort-stat">
          <div class="sort-stat-table">
            <el-table :data="getQuestionSortStats(question.id)" border>
              <el-table-column prop="optionContent" label="选项" width="200" />
              <el-table-column prop="averagePosition" label="平均位置" width="120">
                <template #default="{ row }">
                  {{ row.averagePosition.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="totalSorts" label="排序次数" width="120" />
            </el-table>
          </div>
        </div>

        <!-- 评分题统计 -->
        <div v-else-if="question.type === 'RATING'" class="question-rating-stat">
          <div class="rating-stat-info">
            <div class="rating-item">
              <span class="rating-label">平均分：</span>
              <span class="rating-value">{{ getQuestionStat(question.id, 'averageRating') || 0 }}</span>
            </div>
            <div class="rating-item">
              <span class="rating-label">最高分：</span>
              <span class="rating-value">{{ getQuestionStat(question.id, 'maxRating') || 0 }}</span>
            </div>
            <div class="rating-item">
              <span class="rating-label">最低分：</span>
              <span class="rating-value">{{ getQuestionStat(question.id, 'minRating') || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>

  <!-- 图表配置对话框 -->
  <el-dialog v-model="chartConfigDialog" title="自定义图表配置" width="500px">
    <el-form :model="chartConfig" label-width="120px">
      <el-form-item label="标题字体大小">
        <el-input-number v-model="chartConfig.titleStyle.fontSize" :min="12" :max="24" />
      </el-form-item>
      <el-form-item label="图例位置">
        <el-select v-model="chartConfig.legendStyle.right" placeholder="选择位置">
          <el-option label="右侧" value="10%" />
          <el-option label="底部" value="center" />
          <el-option label="左侧" value="left" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="chartConfigDialog = false">取消</el-button>
      <el-button type="primary" @click="applyChartConfig">应用</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElSelect, ElOption } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElColorPicker, ElInputNumber } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import 'echarts-wordcloud'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { surveyApi, questionApi, statisticsApi } from '@/api'
import { createPieChart, createBarChart, createWordCloudChart, customizeChart } from '@/utils/echarts'

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const survey = ref({})
const questions = ref([])
const questionStats = reactive({})
const questionChartOptions = reactive({})
const questionWordCloudOptions = reactive({})
const chartConfigDialog = ref(false)
const currentConfigQuestionId = ref(null)
const chartConfig = reactive({
  colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#409EFF', '#66b1ff'],
  titleStyle: { fontSize: 16, fontWeight: 'bold' },
  legendStyle: { right: '10%' }
})

const loadSurveyData = async () => {
  const surveyId = route.query.id
  if (!surveyId) {
    ElMessage.warning('请先选择问卷')
    return
  }

  loading.value = true
  try {
    const surveyRes = await surveyApi.getSurveyById(surveyId)
    if (surveyRes.code === 200) {
      survey.value = surveyRes.data
    }

    const questionsRes = await questionApi.getQuestionsBySurveyId(surveyId)
    if (questionsRes.code === 200) {
      questions.value = questionsRes.data || []
      
      // 加载每个题目的统计数据
      for (const question of questions.value) {
        await loadQuestionStatistics(question.id, question.type)
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadQuestionStatistics = async (questionId, questionType) => {
  try {
    const res = await statisticsApi.getQuestionStatistics(questionId)
    if (res.code === 200) {
      questionStats[questionId] = res.data

      // 如果是选择题，创建图表
      if (isChoiceQuestion(questionType)) {
        const optionStats = res.data.optionStats || []
        const data = optionStats.map(item => ({
          value: item.count,
          name: item.content
        }))

        questionChartOptions[questionId] = createPieChart({
          title: res.data.questionTitle,
          data,
          radius: ['40%', '70%']
        })
      } else if (isTextQuestion(questionType)) {
        // 如果是填空题，创建词云图
        const wordFrequency = res.data.wordFrequency || []
        if (wordFrequency.length > 0) {
          const wordCloudData = wordFrequency.map(item => ({
            name: item.word,
            value: item.count
          }))

          questionWordCloudOptions[questionId] = createWordCloudChart({
            title: res.data.questionTitle + ' - 词云分析',
            data: wordCloudData
          })
        }
      }
    }
  } catch (error) {
    console.error(`加载题目${questionId}统计数据失败:`, error)
  }
}

const isChoiceQuestion = (type) => {
  return type === 'SINGLE_CHOICE' || type === 'MULTIPLE_CHOICE'
}

const isTextQuestion = (type) => {
  return type === 'TEXT' || type === 'TEXTAREA'
}

const getQuestionTypeText = (type) => {
  const typeMap = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TEXT: '单行文本',
    TEXTAREA: '多行文本',
    RATING: '评分题',
    SORT: '排序题',
    MATRIX: '矩阵题',
    FILE: '文件上传',
    DATE: '日期时间'
  }
  return typeMap[type] || type
}

const getQuestionStat = (questionId, key) => {
  return questionStats[questionId]?.[key]
}

const getQuestionChartOption = (questionId) => {
  return questionChartOptions[questionId]
}

const getQuestionWordCloudOption = (questionId) => {
  return questionWordCloudOptions[questionId]
}

const getQuestionSortStats = (questionId) => {
  return questionStats[questionId]?.sortStats || []
}

const showChartConfig = (questionId) => {
  currentConfigQuestionId.value = questionId
  chartConfigDialog.value = true
}

const applyChartConfig = () => {
  if (currentConfigQuestionId.value && questionChartOptions[currentConfigQuestionId.value]) {
    const baseOption = questionChartOptions[currentConfigQuestionId.value]
    questionChartOptions[currentConfigQuestionId.value] = customizeChart(baseOption, chartConfig)
    chartConfigDialog.value = false
    ElMessage.success('图表配置已应用')
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadSurveyData()
})
</script>

<style scoped>
.statistics-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.question-stat-item {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
}

.question-stat-item:last-child {
  border-bottom: none;
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
  margin-bottom: 20px;
}

.question-chart {
  margin-top: 20px;
}

.question-text-stat,
.question-rating-stat {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.text-stat-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
}

.rating-stat-info {
  display: flex;
  gap: 30px;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-label {
  font-size: 14px;
  color: #606266;
}

.rating-value {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.question-wordcloud {
  margin-top: 20px;
}

.question-wordcloud h4 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.question-sort-stat {
  margin-top: 20px;
}

.sort-stat-table {
  margin-top: 20px;
}

.chart-controls {
  margin-bottom: 10px;
  text-align: right;
}
</style>

