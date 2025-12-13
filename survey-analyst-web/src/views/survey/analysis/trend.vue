<template>
  <div class="trend-analysis">
    <div class="page-header">
      <h2 class="page-title">趋势分析</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card>
      <el-form :model="analysisForm" label-width="120px">
        <el-form-item label="问卷">
          <el-select v-model="analysisForm.surveyId" placeholder="请选择问卷" style="width: 300px" @change="handleSurveyChange">
            <el-option
              v-for="survey in surveyList"
              :key="survey.id"
              :label="survey.title"
              :value="survey.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目">
          <el-select
            v-model="analysisForm.formItemId"
            placeholder="请选择题目"
            style="width: 300px"
            @change="handleAnalyze"
          >
            <el-option
              v-for="question in questions"
              :key="question.id"
              :label="question.title"
              :value="question.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-select v-model="analysisForm.timeRange" style="width: 300px" @change="handleAnalyze">
            <el-option label="最近7天" value="7d" />
            <el-option label="最近30天" value="30d" />
            <el-option label="全部" value="all" />
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
        <span>趋势分析结果</span>
      </template>

      <div class="trend-chart-section">
        <v-chart
          v-if="trendChartOption"
          :option="trendChartOption"
          style="height: 400px"
          autoresize
        />
      </div>

      <div class="trend-table-section">
        <h3>趋势数据表</h3>
        <el-table :data="trendTableData" border>
          <el-table-column prop="date" label="日期" width="150" />
          <el-table-column
            v-for="option in trendOptions"
            :key="option"
            :prop="option"
            :label="option"
            width="120"
          />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 趋势分析页面
 * 功能：分析题目选项在不同时间段的填写趋势，支持选择时间范围（7天、30天、全部）进行趋势分析
 */

import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { surveyApi, questionApi, analysisApi } from '@/api'
import { createLineChart } from '@/utils/echarts'

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const router = useRouter()

const surveyList = ref([])
const questions = ref([])
const analyzing = ref(false)
const analysisResult = ref(null)
const trendChartOption = ref(null)
const trendTableData = ref([])
const trendOptions = ref([])

const analysisForm = reactive({
  surveyId: null,
  formItemId: null,
  timeRange: '30d'
})

/**
 * 加载问卷列表
 * 加载所有问卷列表，用于筛选下拉框
 */
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

/**
 * 处理问卷变化
 * 选择问卷后加载该问卷的题目列表
 */
const handleSurveyChange = async () => {
  if (!analysisForm.surveyId) return

  try {
    const res = await questionApi.getQuestionsBySurveyId(analysisForm.surveyId)
    if (res.code === 200) {
      questions.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载题目列表失败')
  }
}

/**
 * 处理趋势分析
 * 调用趋势分析接口，分析指定题目在不同时间段的选项变化趋势
 */
const handleAnalyze = async () => {
  if (!analysisForm.surveyId || !analysisForm.questionId) {
    ElMessage.warning('请选择问卷和题目')
    return
  }

  analyzing.value = true
  try {
    const res = await analysisApi.trendAnalysis({
      surveyId: analysisForm.surveyId,
      questionId: analysisForm.questionId,
      timeRange: analysisForm.timeRange
    })

    if (res.code === 200) {
      analysisResult.value = res.data
      buildTrendChart(res.data.trendData)
      buildTrendTable(res.data.trendData)
    }
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

/**
 * 构建趋势图表
 * 根据趋势数据生成折线图配置，显示各选项随时间的变化趋势
 * @param {Object} trendData - 趋势数据对象，键为日期，值为选项统计
 */
const buildTrendChart = (trendData) => {
  if (!trendData) return

  const dates = Object.keys(trendData).sort()
  const options = new Set()
  dates.forEach(date => {
    Object.keys(trendData[date]).forEach(opt => options.add(opt))
  })

  trendOptions.value = Array.from(options)

  const series = trendOptions.value.map(option => ({
    name: option,
    type: 'line',
    smooth: true,
    data: dates.map(date => trendData[date][option] || 0)
  }))

  trendChartOption.value = {
    title: {
      text: '选项趋势分析',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: trendOptions.value,
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: series
  }
}

/**
 * 构建趋势表格数据
 * 将趋势数据转换为表格格式，每行代表一个日期，每列代表一个选项
 * @param {Object} trendData - 趋势数据对象
 */
const buildTrendTable = (trendData) => {
  if (!trendData) return

  const dates = Object.keys(trendData).sort()
  trendTableData.value = dates.map(date => {
    const row = { date }
    trendOptions.value.forEach(option => {
      row[option] = trendData[date][option] || 0
    })
    return row
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadSurveyList()
})
</script>

<style scoped>
.trend-analysis {
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

.trend-chart-section {
  margin-bottom: 30px;
}

.trend-table-section {
  margin-top: 30px;
}

.trend-table-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}
</style>

