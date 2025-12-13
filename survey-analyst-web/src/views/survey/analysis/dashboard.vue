<template>
  <div class="visualization-dashboard">
    <div class="page-header">
      <h2 class="page-title">数据可视化仪表盘</h2>
      <div class="header-actions">
        <el-button @click="handleExportPNG">导出PNG</el-button>
        <el-button type="primary" @click="handleExportPDF">导出PDF</el-button>
      </div>
    </div>

    <el-card>
      <el-form :model="dashboardForm" label-width="120px" inline>
        <el-form-item label="问卷">
          <el-select v-model="dashboardForm.surveyId" placeholder="请选择问卷" style="width: 300px" @change="handleLoadData">
            <el-option
              v-for="survey in surveyList"
              :key="survey.id"
              :label="survey.title"
              :value="survey.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 仪表盘内容 -->
    <div v-if="dashboardForm.surveyId" class="dashboard-content" ref="dashboardRef">
      <el-row :gutter="20" class="dashboard-row">
        <!-- 统计概览 -->
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>统计概览</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-box">
                  <div class="stat-value">{{ statistics.totalResponses || 0 }}</div>
                  <div class="stat-label">总填写数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-box">
                  <div class="stat-value">{{ statistics.completedResponses || 0 }}</div>
                  <div class="stat-label">已完成</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-box">
                  <div class="stat-value">{{ statistics.validRate || 0 }}%</div>
                  <div class="stat-label">有效率</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-box">
                  <div class="stat-value">{{ statistics.draftResponses || 0 }}</div>
                  <div class="stat-label">草稿数</div>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="dashboard-row">
        <!-- 填写趋势 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>填写趋势</span>
            </template>
            <v-chart
              v-if="trendChartOption"
              :option="trendChartOption"
              style="height: 300px"
              autoresize
            />
          </el-card>
        </el-col>

        <!-- 填写来源 -->
        <el-col :span="12">
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

      <el-row :gutter="20" class="dashboard-row">
        <!-- 设备类型 -->
        <el-col :span="12">
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
  </div>
</template>

<script setup>
/**
 * 数据可视化仪表盘页面
 * 功能：提供问卷数据的可视化仪表盘，支持导出PNG和PDF格式，包含统计概览、题目统计图表等
 */

import { ref, reactive, onMounted } from 'vue'
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
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { surveyApi, statisticsApi } from '@/api'
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

const dashboardRef = ref(null)
const surveyList = ref([])
const statistics = reactive({
  totalResponses: 0,
  completedResponses: 0,
  draftResponses: 0,
  validRate: 0
})

const trendChartOption = ref(null)
const sourceChartOption = ref(null)
const deviceChartOption = ref(null)

const dashboardForm = reactive({
  surveyId: null
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

/**
 * 处理加载数据
 * 依次加载统计数据、趋势数据、来源数据和设备数据
 */
const handleLoadData = async () => {
  if (!dashboardForm.surveyId) return

  await loadStatistics()
  await loadTrendData()
  await loadSourceData()
  await loadDeviceData()
}

/**
 * 加载统计数据
 * 从后端获取问卷的统计概览数据
 */
const loadStatistics = async () => {
  try {
    const res = await statisticsApi.getSurveyStatistics(dashboardForm.surveyId)
    if (res.code === 200) {
      Object.assign(statistics, res.data)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

/**
 * 加载填写趋势数据
 * 加载30天内的填写趋势数据并生成折线图配置
 */
const loadTrendData = async () => {
  try {
    const res = await statisticsApi.getResponseTrend(dashboardForm.surveyId, '30d')
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

/**
 * 加载填写来源数据
 * 加载填写来源统计数据并生成饼图配置
 */
const loadSourceData = async () => {
  try {
    const res = await statisticsApi.getResponseSource(dashboardForm.surveyId)
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

/**
 * 加载设备类型数据
 * 加载设备类型统计数据并生成柱状图配置
 */
const loadDeviceData = async () => {
  try {
    const res = await statisticsApi.getDeviceStatistics(dashboardForm.surveyId)
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

/**
 * 处理导出PNG图片
 * 将仪表盘内容导出为PNG图片文件
 */
const handleExportPNG = async () => {
  if (!dashboardRef.value) {
    ElMessage.warning('请先加载数据')
    return
  }

  try {
    ElMessage.info('正在生成图片，请稍候...')
    const canvas = await html2canvas(dashboardRef.value)
    const imgData = canvas.toDataURL('image/png')
    
    const link = document.createElement('a')
    link.download = `仪表盘_${new Date().getTime()}.png`
    link.href = imgData
    link.click()
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

/**
 * 处理导出PDF
 * 将仪表盘内容导出为PDF文件
 */
const handleExportPDF = async () => {
  if (!dashboardRef.value) {
    ElMessage.warning('请先加载数据')
    return
  }

  try {
    ElMessage.info('正在生成PDF，请稍候...')
    const canvas = await html2canvas(dashboardRef.value)
    const imgData = canvas.toDataURL('image/png')
    
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 210
    const pageHeight = 297
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight

    let position = 0

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    pdf.save(`仪表盘_${new Date().getTime()}.pdf`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadSurveyList()
})
</script>

<style scoped>
.visualization-dashboard {
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

.dashboard-content {
  margin-top: 20px;
}

.dashboard-row {
  margin-bottom: 20px;
}

.stat-box {
  text-align: center;
  padding: 20px;
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
</style>

