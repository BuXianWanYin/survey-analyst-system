<template>
  <div class="profile-analysis">
    <div class="page-header">
      <h2 class="page-title">样本画像分析</h2>
      <el-button @click="goBack">返回</el-button>
    </div>

    <el-card>
      <el-form :model="analysisForm" label-width="120px" inline>
        <el-form-item label="问卷">
          <el-select v-model="analysisForm.surveyId" placeholder="请选择问卷" style="width: 300px" @change="handleAnalyze">
            <el-option
              v-for="survey in surveyList"
              :key="survey.id"
              :label="survey.title"
              :value="survey.id"
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
    <el-row v-if="analysisResult" :gutter="20" class="result-row">
      <!-- 设备类型分布 -->
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

      <!-- 填写时间分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>填写时间分布（按小时）</span>
          </template>
          <v-chart
            v-if="hourChartOption"
            :option="hourChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row v-if="analysisResult && (hasGenderData || hasAgeData || hasRegionData)" :gutter="20" class="result-row">
      <!-- 性别分布 -->
      <el-col v-if="hasGenderData" :span="8">
        <el-card>
          <template #header>
            <span>性别分布</span>
          </template>
          <v-chart
            v-if="genderChartOption"
            :option="genderChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>

      <!-- 年龄分布 -->
      <el-col v-if="hasAgeData" :span="8">
        <el-card>
          <template #header>
            <span>年龄分布</span>
          </template>
          <v-chart
            v-if="ageChartOption"
            :option="ageChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>

      <!-- 地域分布 -->
      <el-col v-if="hasRegionData" :span="8">
        <el-card>
          <template #header>
            <span>地域分布</span>
          </template>
          <v-chart
            v-if="regionChartOption"
            :option="regionChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row v-if="analysisResult" :gutter="20" class="result-row">
      <!-- 填写时长分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>填写时长分布</span>
          </template>
          <v-chart
            v-if="durationChartOption"
            :option="durationChartOption"
            style="height: 300px"
            autoresize
          />
        </el-card>
      </el-col>

      <!-- 统计信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>统计信息</span>
          </template>
          <div class="stat-info">
            <div class="stat-item">
              <span class="stat-label">总填写数：</span>
              <span class="stat-value">{{ analysisResult.totalCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">PC端填写：</span>
              <span class="stat-value">{{ getDeviceCount('PC') || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">移动端填写：</span>
              <span class="stat-value">{{ getDeviceCount('MOBILE') || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { surveyApi, analysisApi } from '@/api'
import { createPieChart, createBarChart } from '@/utils/echarts'

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

const router = useRouter()

const surveyList = ref([])
const analyzing = ref(false)
const analysisResult = ref(null)
const deviceChartOption = ref(null)
const hourChartOption = ref(null)
const durationChartOption = ref(null)
const genderChartOption = ref(null)
const ageChartOption = ref(null)
const regionChartOption = ref(null)

const analysisForm = reactive({
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

const handleAnalyze = async () => {
  if (!analysisForm.surveyId) {
    ElMessage.warning('请选择问卷')
    return
  }

  analyzing.value = true
  try {
    const res = await analysisApi.profileAnalysis(analysisForm.surveyId)

    if (res.code === 200) {
      analysisResult.value = res.data
      buildCharts(res.data)
    }
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

const buildCharts = (data) => {
  // 设备类型分布
  if (data.deviceDistribution) {
    const deviceData = Object.entries(data.deviceDistribution).map(([name, value]) => ({
      value,
      name: name === 'PC' ? 'PC端' : name === 'MOBILE' ? '移动端' : name
    }))

    deviceChartOption.value = createPieChart({
      title: '设备类型分布',
      data: deviceData,
      radius: ['40%', '70%']
    })
  }

  // 填写时间分布（按小时）
  if (data.hourDistribution) {
    const hours = Array.from({ length: 24 }, (_, i) => i)
    const hourData = hours.map(hour => data.hourDistribution[hour] || 0)

    hourChartOption.value = createBarChart({
      title: '填写时间分布（按小时）',
      xAxisData: hours.map(h => h + '时'),
      seriesData: hourData,
      xAxisName: '小时',
      yAxisName: '填写数'
    })
  }

  // 填写时长分布
  if (data.durationDistribution) {
    const durationData = [
      { name: '短（<1分钟）', value: data.durationDistribution.short || 0 },
      { name: '中（1-5分钟）', value: data.durationDistribution.medium || 0 },
      { name: '长（>5分钟）', value: data.durationDistribution.long || 0 }
    ]

    durationChartOption.value = createPieChart({
      title: '填写时长分布',
      data: durationData,
      radius: ['40%', '70%']
    })
  }

  // 性别分布
  if (data.genderDistribution) {
    const genderData = Object.entries(data.genderDistribution).map(([name, value]) => ({
      value,
      name
    }))

    genderChartOption.value = createPieChart({
      title: '性别分布',
      data: genderData,
      radius: ['40%', '70%']
    })
  }

  // 年龄分布
  if (data.ageDistribution) {
    const ageData = Object.entries(data.ageDistribution).map(([name, value]) => ({
      value,
      name
    }))

    ageChartOption.value = createBarChart({
      title: '年龄分布',
      xAxisData: ageData.map(item => item.name),
      seriesData: ageData.map(item => item.value),
      xAxisName: '年龄组',
      yAxisName: '人数'
    })
  }

  // 地域分布
  if (data.regionDistribution) {
    const regionData = Object.entries(data.regionDistribution)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10) // 取前10个
      .map(([name, value]) => ({
        value,
        name
      }))

    regionChartOption.value = createBarChart({
      title: '地域分布（Top 10）',
      xAxisData: regionData.map(item => item.name),
      seriesData: regionData.map(item => item.value),
      xAxisName: '地域',
      yAxisName: '人数'
    })
  }
}

const hasGenderData = computed(() => {
  return analysisResult.value && analysisResult.value.genderDistribution && 
         Object.keys(analysisResult.value.genderDistribution).length > 0
})

const hasAgeData = computed(() => {
  return analysisResult.value && analysisResult.value.ageDistribution && 
         Object.keys(analysisResult.value.ageDistribution).length > 0
})

const hasRegionData = computed(() => {
  return analysisResult.value && analysisResult.value.regionDistribution && 
         Object.keys(analysisResult.value.regionDistribution).length > 0
})

const getDeviceCount = (deviceType) => {
  if (!analysisResult.value || !analysisResult.value.deviceDistribution) return 0
  return analysisResult.value.deviceDistribution[deviceType] || 0
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadSurveyList()
})
</script>

<style scoped>
.profile-analysis {
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

.result-row {
  margin-top: 20px;
}

.stat-info {
  padding: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}
</style>

