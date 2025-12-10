<template>
  <div class="statistics-overview">
    <div class="page-header">
      <h2 class="page-title">{{ survey.title }}</h2>
      <div class="header-actions">
        <el-button @click="goToDetail">统计详情</el-button>
        <el-button @click="handleRefresh">刷新数据</el-button>
        <el-button type="primary" @click="handleExport">导出数据</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalResponses || 0 }}</div>
            <div class="stat-label">总填写数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.completedResponses || 0 }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.validRate || 0 }}%</div>
            <div class="stat-label">有效率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.draftResponses || 0 }}</div>
            <div class="stat-label">草稿数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 填写趋势图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>填写趋势</span>
              <el-select v-model="timeRange" size="small" @change="loadTrendData">
                <el-option label="最近7天" value="7d" />
                <el-option label="最近30天" value="30d" />
                <el-option label="全部" value="all" />
              </el-select>
            </div>
          </template>
          <v-chart
            v-if="trendChartOption"
            :option="trendChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>

      <!-- 填写来源图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card>
          <template #header>
            <span>填写来源</span>
          </template>
          <v-chart
            v-if="sourceChartOption"
            :option="sourceChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 设备类型图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card>
          <template #header>
            <span>设备类型分布</span>
          </template>
          <v-chart
            v-if="deviceChartOption"
            :option="deviceChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { surveyApi, statisticsApi, exportApi } from '@/api'
import { createLineChart, createPieChart, createBarChart } from '@/utils/echarts'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const route = useRoute()
const router = useRouter()

const survey = ref({})
const statistics = reactive({
  totalResponses: 0,
  completedResponses: 0,
  draftResponses: 0,
  validRate: 0
})

const timeRange = ref('30d')
const trendChartOption = ref(null)
const sourceChartOption = ref(null)
const deviceChartOption = ref(null)

let refreshTimer = null

const loadSurveyData = async () => {
  const surveyId = route.query.id
  if (!surveyId) {
    ElMessage.warning('请先选择问卷')
    return
  }

  try {
    const res = await surveyApi.getSurveyById(surveyId)
    if (res.code === 200) {
      survey.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载问卷数据失败')
  }
}

const loadStatistics = async () => {
  const surveyId = route.query.id
  if (!surveyId) return

  try {
    const res = await statisticsApi.getSurveyStatistics(surveyId)
    if (res.code === 200) {
      Object.assign(statistics, res.data)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadTrendData = async () => {
  const surveyId = route.query.id
  if (!surveyId) return

  try {
    const res = await statisticsApi.getResponseTrend(surveyId, timeRange.value)
    if (res.code === 200) {
      const trendData = res.data.data || []
      const xAxisData = trendData.map(item => item.date)
      const seriesData = trendData.map(item => item.count)

      trendChartOption.value = createLineChart({
        title: '填写趋势',
        xAxisData,
        seriesData,
        seriesName: '填写数',
        xAxisName: '日期',
        yAxisName: '填写数'
      })
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

const loadSourceData = async () => {
  const surveyId = route.query.id
  if (!surveyId) return

  try {
    const res = await statisticsApi.getResponseSource(surveyId)
    if (res.code === 200) {
      const sourceCount = res.data.sourceCount || {}
      const data = Object.entries(sourceCount).map(([name, value]) => ({
        value,
        name: name === 'direct' ? '直接访问' : name === 'share' ? '分享链接' : '其他'
      }))

      sourceChartOption.value = createPieChart({
        title: '填写来源',
        data,
        radius: ['40%', '70%']
      })
    }
  } catch (error) {
    console.error('加载来源数据失败:', error)
  }
}

const loadDeviceData = async () => {
  const surveyId = route.query.id
  if (!surveyId) return

  try {
    const res = await statisticsApi.getDeviceStatistics(surveyId)
    if (res.code === 200) {
      const deviceCount = res.data.deviceCount || {}
      const xAxisData = Object.keys(deviceCount).map(key => key === 'PC' ? 'PC端' : '移动端')
      const seriesData = Object.values(deviceCount)

      deviceChartOption.value = createBarChart({
        title: '设备类型分布',
        xAxisData,
        seriesData,
        xAxisName: '设备类型',
        yAxisName: '填写数'
      })
    }
  } catch (error) {
    console.error('加载设备数据失败:', error)
  }
}

const handleRefresh = async () => {
  const surveyId = route.query.id
  if (!surveyId) return

  try {
    await statisticsApi.refreshStatistics(surveyId)
    ElMessage.success('刷新成功')
    await loadAllData()
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const handleExport = async () => {
  const surveyId = route.query.id
  if (!surveyId) return

  try {
    ElMessage.info('正在导出数据，请稍候...')
    await exportApi.exportStatistics(surveyId)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const goToDetail = () => {
  router.push(`/survey/statistics/detail?id=${route.query.id}`)
}

const loadAllData = async () => {
  await loadStatistics()
  await loadTrendData()
  await loadSourceData()
  await loadDeviceData()
}

onMounted(() => {
  loadSurveyData()
  loadAllData()

  // 定时刷新（每30秒）
  refreshTimer = setInterval(() => {
    loadStatistics()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.statistics-overview {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-overview {
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .page-title {
    font-size: 20px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .header-actions .el-button {
    flex: 1;
    min-width: 0;
  }

  .stats-cards {
    margin-bottom: 15px;
  }

  .stat-value {
    font-size: 24px;
  }

  .charts-row {
    margin-bottom: 15px;
  }

  .charts-row .el-col {
    margin-bottom: 15px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .statistics-overview {
    padding: 10px;
  }

  .page-title {
    font-size: 18px;
  }

  .header-actions {
    flex-direction: column;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>

