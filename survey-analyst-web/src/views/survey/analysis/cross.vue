<template>
  <div class="cross-analysis">
    <div class="page-header">
      <h2 class="page-title">交叉分析</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card>
      <el-form :model="analysisForm" label-width="120px">
        <el-form-item label="问卷">
          <el-select v-model="analysisForm.surveyId" placeholder="请选择问卷" style="width: 300px">
            <el-option
              v-for="survey in surveyList"
              :key="survey.id"
              :label="survey.title"
              :value="survey.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目1">
          <el-select
            v-model="analysisForm.questionId1"
            placeholder="请选择题目1"
            style="width: 300px"
            @change="handleQuestion1Change"
          >
            <el-option
              v-for="question in questions1"
              :key="question.id"
              :label="question.title"
              :value="question.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目2">
          <el-select
            v-model="analysisForm.questionId2"
            placeholder="请选择题目2"
            style="width: 300px"
            @change="handleQuestion2Change"
          >
            <el-option
              v-for="question in questions2"
              :key="question.id"
              :label="question.title"
              :value="question.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAnalyze" :loading="analyzing">
            开始分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分析结果 -->
    <el-card v-if="analysisResult" class="result-card">
      <template #header>
        <span>分析结果</span>
      </template>

      <!-- 交叉表 -->
      <div class="cross-table-section">
        <h3>交叉表</h3>
        <el-table :data="crossTableData" border>
          <el-table-column prop="rowLabel" label="" width="150" fixed="left" />
          <el-table-column
            v-for="col in crossTableColumns"
            :key="col"
            :prop="col"
            :label="col"
            width="120"
          />
        </el-table>
      </div>

      <!-- 热力图 -->
      <div class="heatmap-section">
        <h3>热力图</h3>
        <v-chart
          v-if="heatmapChartOption"
          :option="heatmapChartOption"
          style="height: 400px"
          autoresize
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { HeatmapChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  VisualMapComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { surveyApi, questionApi, analysisApi } from '@/api'
import { createHeatmapChart } from '@/utils/echarts'

use([
  CanvasRenderer,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  VisualMapComponent,
  GridComponent
])

const router = useRouter()

const surveyList = ref([])
const questions1 = ref([])
const questions2 = ref([])
const analyzing = ref(false)
const analysisResult = ref(null)
const crossTableData = ref([])
const crossTableColumns = ref([])
const heatmapChartOption = ref(null)

const analysisForm = reactive({
  surveyId: null,
  questionId1: null,
  questionId2: null
})

const loadSurveyList = async () => {
  try {
    const res = await surveyApi.getSurveyList({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      surveyList.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载问卷列表失败')
  }
}

const handleQuestion1Change = async () => {
  if (!analysisForm.surveyId || !analysisForm.questionId1) return

  try {
    const res = await questionApi.getQuestionsBySurveyId(analysisForm.surveyId)
    if (res.code === 200) {
      questions2.value = (res.data || []).filter(q => q.id !== analysisForm.questionId1)
    }
  } catch (error) {
    ElMessage.error('加载题目列表失败')
  }
}

const handleQuestion2Change = async () => {
  if (!analysisForm.surveyId || !analysisForm.questionId2) return

  try {
    const res = await questionApi.getQuestionsBySurveyId(analysisForm.surveyId)
    if (res.code === 200) {
      questions1.value = (res.data || []).filter(q => q.id !== analysisForm.questionId2)
    }
  } catch (error) {
    ElMessage.error('加载题目列表失败')
  }
}

const handleAnalyze = async () => {
  if (!analysisForm.surveyId || !analysisForm.questionId1 || !analysisForm.questionId2) {
    ElMessage.warning('请选择问卷和两个题目')
    return
  }

  analyzing.value = true
  try {
    const res = await analysisApi.crossAnalysis({
      surveyId: analysisForm.surveyId,
      questionId1: analysisForm.questionId1,
      questionId2: analysisForm.questionId2
    })

    if (res.code === 200) {
      analysisResult.value = res.data
      buildCrossTable(res.data.crossTable)
      buildHeatmap(res.data.crossTable)
    }
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

const buildCrossTable = (crossTable) => {
  if (!crossTable) return

  const rows = Object.keys(crossTable)
  const cols = new Set()
  rows.forEach(row => {
    Object.keys(crossTable[row]).forEach(col => cols.add(col))
  })

  crossTableColumns.value = Array.from(cols)

  crossTableData.value = rows.map(row => {
    const rowData = { rowLabel: row }
    crossTableColumns.value.forEach(col => {
      rowData[col] = crossTable[row][col] || 0
    })
    return rowData
  })
}

const buildHeatmap = (crossTable) => {
  if (!crossTable) return

  const rows = Object.keys(crossTable)
  const cols = new Set()
  rows.forEach(row => {
    Object.keys(crossTable[row]).forEach(col => cols.add(col))
  })

  const xAxisData = Array.from(cols)
  const yAxisData = rows
  const data = []

  rows.forEach((row, rowIndex) => {
    xAxisData.forEach((col, colIndex) => {
      const value = crossTable[row][col] || 0
      data.push([colIndex, rowIndex, value])
    })
  })

  heatmapChartOption.value = createHeatmapChart({
    title: '交叉分析热力图',
    xAxisData,
    yAxisData,
    data
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadSurveyList()
})
</script>

<style scoped>
.cross-analysis {
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

.result-card {
  margin-top: 20px;
}

.cross-table-section,
.heatmap-section {
  margin-bottom: 30px;
}

.cross-table-section h3,
.heatmap-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}
</style>

