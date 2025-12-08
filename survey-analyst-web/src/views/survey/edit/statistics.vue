<template>
  <div class="statistics-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>统计分析</span>
          <el-button @click="handleRefresh">刷新数据</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <el-tab-pane label="统计视图" name="chart">
          <div v-loading="loading" class="statistics-content">
            <div v-if="formItems.length === 0" class="empty-tip">
              <el-empty description="暂无表单项，无法统计" />
            </div>
            
            <div
              v-for="item in formItems"
              :key="item.formItemId"
              class="stat-item"
            >
              <div class="stat-header">
                <h3>{{ item.label }}</h3>
                <el-tag size="small">{{ getTypeLabel(item.type) }}</el-tag>
              </div>
              
              <!-- 选择题统计 -->
              <div v-if="isChoiceType(item.type)" class="stat-chart">
                <div
                  v-if="getChartOption(item.formItemId)"
                  class="chart-wrapper"
                >
                  <v-chart
                    :option="getChartOption(item.formItemId)"
                    style="height: 300px"
                    autoresize
                  />
                </div>
                <div v-else class="no-data">暂无数据</div>
              </div>
              
              <!-- 文本题统计 -->
              <div v-else-if="isTextType(item.type)" class="stat-text">
                <div class="text-stat-info">
                  <el-statistic title="有效答案数" :value="getTextStat(item.formItemId, 'count') || 0" />
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据分析" name="analysis">
          <div v-loading="analysisLoading" class="analysis-content">
            <el-row :gutter="20">
              <!-- 填写趋势 -->
              <el-col :span="12">
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
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
              
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
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px">
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
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
              
              <!-- 填写时段 -->
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>填写时段分布</span>
                  </template>
                  <v-chart
                    v-if="hourChartOption"
                    :option="hourChartOption"
                    style="height: 300px"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { formApi } from '@/api'
import dayjs from 'dayjs'

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const route = useRoute()

const surveyId = ref(null)
const formKey = ref(null)
const loading = ref(false)
const analysisLoading = ref(false)
const activeTab = ref('chart')
const formItems = ref([])
const statisticsData = ref({})
const timeRange = ref('30d')
const trendChartOption = ref(null)
const deviceChartOption = ref(null)
const sourceChartOption = ref(null)
const hourChartOption = ref(null)

// 加载表单配置和表单项
const loadFormConfig = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)

  try {
    // 加载表单配置获取 formKey
    const configRes = await formApi.getFormConfig(surveyId.value)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      
      // 加载表单项
      if (formKey.value) {
        const itemsRes = await formApi.getFormItems(formKey.value)
        if (itemsRes.code === 200 && itemsRes.data) {
          formItems.value = itemsRes.data.map(item => {
            const scheme = typeof item.scheme === 'string' 
              ? JSON.parse(item.scheme) 
              : item.scheme
            return {
              formItemId: item.formItemId,
              label: item.label,
              type: item.type,
              scheme: scheme || {}
            }
          })
        }
      }
    }
    
    await loadStatistics()
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

// 加载统计数据
const loadStatistics = async () => {
  if (!formKey.value) return

  loading.value = true
  try {
    // 加载表单数据
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 1000
    })
    
    if (dataRes.code === 200 && dataRes.data) {
      const dataList = dataRes.data.records || []
      
      // 统计每个表单项的数据
      formItems.value.forEach(item => {
        if (isChoiceType(item.type)) {
          statisticsData.value[item.formItemId] = calculateChoiceStat(item, dataList)
        } else if (isTextType(item.type)) {
          statisticsData.value[item.formItemId] = calculateTextStat(item, dataList)
        }
      })
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 计算选择题统计
const calculateChoiceStat = (item, dataList) => {
  const options = item.scheme?.config?.options || []
  const optionCount = {}
  
  options.forEach(opt => {
    optionCount[opt.value] = 0
  })
  
  dataList.forEach(data => {
    const value = data.originalData?.[item.formItemId]
    if (value !== undefined && value !== null) {
      if (Array.isArray(value)) {
        value.forEach(v => {
          if (optionCount[v] !== undefined) {
            optionCount[v]++
          }
        })
      } else {
        if (optionCount[value] !== undefined) {
          optionCount[value]++
        }
      }
    }
  })
  
  return {
    optionCount,
    total: dataList.length
  }
}

// 计算文本题统计
const calculateTextStat = (item, dataList) => {
  let count = 0
  dataList.forEach(data => {
    const value = data.originalData?.[item.formItemId]
    if (value && String(value).trim()) {
      count++
    }
  })
  
  return {
    count,
    total: dataList.length
  }
}

// 获取图表配置
const getChartOption = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.optionCount) return null
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) return null
  
  const options = item.scheme?.config?.options || []
  const data = options.map(opt => ({
    value: stat.optionCount[opt.value] || 0,
    name: opt.label
  }))
  
  return {
    title: {
      text: item.label,
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: item.label,
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c} ({d}%)'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '20',
            fontWeight: 'bold'
          }
        },
        data: data
      }
    ]
  }
}

// 获取文本统计
const getTextStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  return stat ? stat[key] : 0
}

// 判断是否为选择题
const isChoiceType = (type) => {
  return ['RADIO', 'CHECKBOX', 'SELECT'].includes(type)
}

// 判断是否为文本题
const isTextType = (type) => {
  return ['INPUT', 'TEXTAREA'].includes(type)
}

// 获取类型标签
const getTypeLabel = (type) => {
  const typeMap = {
    INPUT: '单行文本',
    TEXTAREA: '多行文本',
    RADIO: '单选框',
    CHECKBOX: '多选框',
    SELECT: '下拉框',
    NUMBER: '数字',
    DATE: '日期',
    RATE: '评分'
  }
  return typeMap[type] || type
}

// 加载趋势数据
const loadTrendData = async () => {
  if (!formKey.value) return
  
  analysisLoading.value = true
  try {
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 1000
    })
    
    if (dataRes.code === 200 && dataRes.data) {
      const dataList = dataRes.data.records || []
      
      // 计算填写趋势
      const trendData = calculateTrendData(dataList, timeRange.value)
      trendChartOption.value = {
        title: { text: '填写趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: trendData.dates
        },
        yAxis: { type: 'value' },
        series: [{
          name: '填写数量',
          type: 'line',
          data: trendData.counts,
          smooth: true,
          areaStyle: {}
        }]
      }
      
      // 计算设备类型分布
      const deviceData = calculateDeviceData(dataList)
      deviceChartOption.value = {
        title: { text: '设备类型分布', left: 'center' },
        tooltip: { trigger: 'item' },
        series: [{
          name: '设备类型',
          type: 'pie',
          radius: '60%',
          data: deviceData
        }]
      }
      
      // 计算填写来源（简化处理，实际应该从IP地址等获取）
      sourceChartOption.value = {
        title: { text: '填写来源', left: 'center' },
        tooltip: { trigger: 'item' },
        series: [{
          name: '填写来源',
          type: 'pie',
          radius: '60%',
          data: [
            { value: dataList.length, name: '直接访问' }
          ]
        }]
      }
      
      // 计算填写时段分布
      const hourData = calculateHourData(dataList)
      hourChartOption.value = {
        title: { text: '填写时段分布', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: hourData.hours
        },
        yAxis: { type: 'value' },
        series: [{
          name: '填写数量',
          type: 'bar',
          data: hourData.counts
        }]
      }
    }
  } catch (error) {
    ElMessage.error('加载分析数据失败')
  } finally {
    analysisLoading.value = false
  }
}

// 计算趋势数据
const calculateTrendData = (dataList, range) => {
  const dates = []
  const counts = []
  const now = dayjs()
  let days = 30
  
  if (range === '7d') days = 7
  else if (range === 'all') {
    if (dataList.length === 0) return { dates: [], counts: [] }
    const firstDate = dayjs(dataList[dataList.length - 1].createTime)
    days = now.diff(firstDate, 'day') + 1
  }
  
  for (let i = days - 1; i >= 0; i--) {
    const date = now.subtract(i, 'day')
    const dateStr = date.format('MM-DD')
    dates.push(dateStr)
    
    const count = dataList.filter(item => {
      const itemDate = dayjs(item.createTime)
      return itemDate.isSame(date, 'day')
    }).length
    counts.push(count)
  }
  
  return { dates, counts }
}

// 计算设备数据
const calculateDeviceData = (dataList) => {
  const deviceMap = {}
  dataList.forEach(item => {
    const device = item.submitOs || '未知'
    deviceMap[device] = (deviceMap[device] || 0) + 1
  })
  
  return Object.entries(deviceMap).map(([name, value]) => ({
    name,
    value
  }))
}

// 计算时段数据
const calculateHourData = (dataList) => {
  const hourCounts = new Array(24).fill(0)
  
  dataList.forEach(item => {
    if (item.createTime) {
      const hour = dayjs(item.createTime).hour()
      hourCounts[hour]++
    }
  })
  
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  return { hours, counts: hourCounts }
}

// 刷新数据
const handleRefresh = () => {
  loadStatistics()
  if (activeTab.value === 'analysis') {
    loadTrendData()
  }
}

// 监听标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'analysis' && !trendChartOption.value) {
    loadTrendData()
  }
}

onMounted(() => {
  loadFormConfig()
})
</script>

<style lang="scss" scoped>
.statistics-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-content {
  min-height: 400px;
}

.empty-tip {
  padding: 40px;
  text-align: center;
}

.stat-item {
  margin-bottom: 40px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
  }
}

.stat-chart {
  .chart-wrapper {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
  }
  
  .no-data {
    text-align: center;
    padding: 40px;
    color: #909399;
  }
}

.stat-text {
  .text-stat-info {
    display: flex;
    gap: 30px;
  }
}

.analysis-content {
  padding: 20px;
  min-height: 400px;
}
</style>
