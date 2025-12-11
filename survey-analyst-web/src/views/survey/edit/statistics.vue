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
            <!-- 问卷整体统计 -->
            <el-card v-if="surveyStatistics" class="survey-overview-card" style="margin-bottom: 20px">
              <template #header>
                <span>问卷整体统计</span>
              </template>
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-statistic title="总填写数" :value="surveyStatistics.totalResponses || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="已完成数" :value="surveyStatistics.completedResponses || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="草稿数" :value="surveyStatistics.draftResponses || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="有效率" :value="surveyStatistics.validRate || 0" suffix="%" :precision="2" />
                </el-col>
              </el-row>
            </el-card>
            
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
                  <el-statistic title="总答案数" :value="getTextStat(item.formItemId, 'total') || 0" />
                </div>
              </div>
              
              <!-- 评分题统计 -->
              <div v-else-if="isRatingType(item.type)" class="stat-rating">
                <div class="rating-stat-info">
                  <el-statistic title="平均分" :value="getRatingStat(item.formItemId, 'averageRating') || 0" :precision="2" />
                  <el-statistic title="最高分" :value="getRatingStat(item.formItemId, 'maxRating') || 0" />
                  <el-statistic title="最低分" :value="getRatingStat(item.formItemId, 'minRating') || 0" />
                  <el-statistic title="评分人数" :value="getRatingStat(item.formItemId, 'totalRatings') || 0" />
                </div>
                <!-- 评分分布图 -->
                <div v-if="getRatingChartOption(item.formItemId)" class="chart-wrapper">
                  <v-chart
                    :option="getRatingChartOption(item.formItemId)"
                    style="height: 300px"
                    autoresize
                  />
                </div>
              </div>
              
              <!-- 数字题统计 -->
              <div v-else-if="item.type === 'NUMBER'" class="stat-number">
                <div class="number-stat-info">
                  <el-statistic title="平均值" :value="getNumberStat(item.formItemId, 'average') || 0" :precision="2" />
                  <el-statistic title="最大值" :value="getNumberStat(item.formItemId, 'max') || 0" />
                  <el-statistic title="最小值" :value="getNumberStat(item.formItemId, 'min') || 0" />
                  <el-statistic title="有效数据数" :value="getNumberStat(item.formItemId, 'count') || 0" />
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据分析" name="analysis">
          <div v-loading="analysisLoading" class="analysis-content">
            <!-- 交叉分析 -->
            <el-card class="cross-analysis-card">
              <template #header>
                <span>交叉分析（相关分析）</span>
              </template>
              <el-form :model="crossAnalysisForm" label-width="120px">
                <el-form-item label="题目1">
                  <el-select
                    v-model="crossAnalysisForm.formItemId1"
                    placeholder="请选择题目1"
                    style="width: 300px"
                    @change="handleCrossQuestion1Change"
                  >
                    <el-option
                      v-for="item in choiceFormItems"
                      :key="item.formItemId"
                      :label="item.label"
                      :value="item.formItemId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="题目2">
                  <el-select
                    v-model="crossAnalysisForm.formItemId2"
                    placeholder="请选择题目2"
                    style="width: 300px"
                    @change="handleCrossQuestion2Change"
                  >
                    <el-option
                      v-for="item in crossQuestion2Options"
                      :key="item.formItemId"
                      :label="item.label"
                      :value="item.formItemId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleCrossAnalyze" :loading="crossAnalyzing">
                    开始分析
                  </el-button>
                  <el-button type="info" @click="autoStartCrossAnalysis" :loading="crossAnalyzing" style="margin-left: 10px">
                    自动分析（前两个题目）
                  </el-button>
                </el-form-item>
              </el-form>

              <!-- 交叉分析结果 -->
              <div v-if="crossAnalysisResult" class="cross-result-section">
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
              </div>
            </el-card>

            <el-row :gutter="20" style="margin-top: 20px">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart, HeatmapChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  VisualMapComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { formApi, analysisApi, questionApi, statisticsApi } from '@/api'
import dayjs from 'dayjs'

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  VisualMapComponent
])

const route = useRoute()

const surveyId = ref(null)
const formKey = ref(null)
const loading = ref(false)
const analysisLoading = ref(false)
const activeTab = ref('chart')
const formItems = ref([])
const statisticsData = ref({})
const surveyStatistics = ref(null) // 问卷整体统计
const timeRange = ref('30d')
const trendChartOption = ref(null)
const deviceChartOption = ref(null)
const sourceChartOption = ref(null)
const hourChartOption = ref(null)

// 交叉分析相关
const crossAnalysisForm = reactive({
  formItemId1: null,
  formItemId2: null
})
const crossAnalyzing = ref(false)
const crossAnalysisResult = ref(null)
const crossTableData = ref([])
const crossTableColumns = ref([])
const heatmapChartOption = ref(null)

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
  if (!formKey.value || !surveyId.value) return

  loading.value = true
  try {
    // 使用统一接口一次性获取所有统计数据
    const allStatRes = await statisticsApi.getAllStatistics(surveyId.value, {
      includeTrend: false,
      includeSource: false,
      includeDevice: false
    })
    
    if (allStatRes.code === 200 && allStatRes.data) {
      const allData = allStatRes.data
      
      // 1. 设置问卷整体统计
      if (allData.surveyStatistics) {
        surveyStatistics.value = allData.surveyStatistics
      }
      
      // 2. 设置各题目统计数据
      if (allData.questionStatistics) {
        // 将后端返回的统计数据设置到 statisticsData
        Object.keys(allData.questionStatistics).forEach(formItemId => {
          statisticsData.value[formItemId] = allData.questionStatistics[formItemId]
        })
      }
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error('加载统计数据错误:', error)
    // 降级方案：使用旧的逐个请求方式
    await loadStatisticsFallback()
  } finally {
    loading.value = false
    // 自动执行交叉分析：如果有至少2个选择题，自动选择前两个进行分析
    autoStartCrossAnalysis()
  }
}

// 降级方案：如果统一接口失败，使用旧的逐个请求方式
const loadStatisticsFallback = async () => {
  try {
    // 1. 加载问卷整体统计
    const surveyStatRes = await statisticsApi.getSurveyStatistics(surveyId.value)
    if (surveyStatRes.code === 200) {
      surveyStatistics.value = surveyStatRes.data
    }
    
    // 2. 使用后端API获取每个题目的统计数据
    for (const item of formItems.value) {
      try {
        const statRes = await statisticsApi.getQuestionStatistics(item.formItemId, surveyId.value)
        if (statRes.code === 200 && statRes.data) {
          statisticsData.value[item.formItemId] = statRes.data
        }
      } catch (error) {
        console.error(`获取题目 ${item.formItemId} 统计失败:`, error)
        // 如果API失败，使用前端计算作为降级方案
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 1000
    })
    if (dataRes.code === 200 && dataRes.data) {
      const dataList = dataRes.data.records || []
        if (isChoiceType(item.type)) {
          statisticsData.value[item.formItemId] = calculateChoiceStat(item, dataList)
        } else if (isTextType(item.type)) {
          statisticsData.value[item.formItemId] = calculateTextStat(item, dataList)
        }
        }
      }
    }
  } catch (error) {
    console.error('降级方案也失败了:', error)
  }
}

// 自动开始交叉分析
const autoStartCrossAnalysis = () => {
  // 检查是否有选择题（至少2个）
  const choiceItems = formItems.value.filter(item => isChoiceType(item.type))
  if (choiceItems.length >= 2) {
    // 自动选择前两个选择题
    crossAnalysisForm.formItemId1 = choiceItems[0].formItemId
    crossAnalysisForm.formItemId2 = choiceItems[1].formItemId
    // 自动执行分析（不显示提示信息）
    handleCrossAnalyze(false)
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

// 获取图表配置（选择题）
const getChartOption = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.optionStats) return null
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) return null
  
  // 使用后端返回的optionStats数据
  const data = stat.optionStats.map(opt => ({
    value: opt.count || 0,
    name: opt.optionLabel || opt.optionValue
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
  if (!stat) return 0
  // 后端返回的是 validAnswers 和 totalAnswers
  if (key === 'count') return stat.validAnswers || 0
  if (key === 'total') return stat.totalAnswers || 0
  return stat[key] || 0
}

// 获取评分统计
const getRatingStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  return stat ? stat[key] : 0
}

// 获取数字统计
const getNumberStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  if (!stat) return 0
  // 数字题可以计算平均值、最大值、最小值
  if (key === 'average') {
    // 这里需要从原始数据计算，暂时返回0
    return 0
  }
  return stat[key] || 0
}

// 判断是否为评分题
const isRatingType = (type) => {
  return ['RATE', 'SLIDER'].includes(type)
}

// 获取评分题图表配置
const getRatingChartOption = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.optionStats) return null
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) return null
  
  // 构建评分分布柱状图
  const data = stat.optionStats.map(opt => ({
    value: opt.count || 0,
    name: opt.optionLabel || opt.optionValue
  }))
  
  return {
    title: {
      text: item.label + ' - 评分分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name)
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [{
      name: '评分人数',
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: {
        color: '#409EFF'
      }
    }]
  }
}

// 判断是否为选择题
const isChoiceType = (type) => {
  return ['RADIO', 'CHECKBOX', 'SELECT'].includes(type)
}

// 获取选择题类型的表单项
const choiceFormItems = computed(() => {
  return formItems.value.filter(item => isChoiceType(item.type))
})

// 交叉分析题目2的选项（排除题目1）
const crossQuestion2Options = computed(() => {
  if (!crossAnalysisForm.formItemId1) {
    return choiceFormItems.value
  }
  return choiceFormItems.value.filter(item => item.formItemId !== crossAnalysisForm.formItemId1)
})

// 处理交叉分析题目1变化
const handleCrossQuestion1Change = () => {
  if (crossAnalysisForm.formItemId2 === crossAnalysisForm.formItemId1) {
    crossAnalysisForm.formItemId2 = null
  }
  crossAnalysisResult.value = null
}

// 处理交叉分析题目2变化
const handleCrossQuestion2Change = () => {
  crossAnalysisResult.value = null
}

// 执行交叉分析
const handleCrossAnalyze = async (showWarning = true) => {
  if (!crossAnalysisForm.formItemId1 || !crossAnalysisForm.formItemId2) {
    if (showWarning) {
    ElMessage.warning('请选择两个题目')
    }
    return
  }

  if (!surveyId.value) {
    if (showWarning) {
    ElMessage.warning('问卷ID不存在')
    }
    return
  }

  // 验证表单项是否存在
  const item1 = formItems.value.find(item => item.formItemId === crossAnalysisForm.formItemId1)
  const item2 = formItems.value.find(item => item.formItemId === crossAnalysisForm.formItemId2)

  if (!item1) {
    if (showWarning) {
    ElMessage.warning('题目1不存在，无法进行交叉分析')
    }
    return
  }

  if (!item2) {
    if (showWarning) {
    ElMessage.warning('题目2不存在，无法进行交叉分析')
    }
    return
  }

  crossAnalyzing.value = true
  try {
    const res = await analysisApi.crossAnalysis({
      surveyId: surveyId.value,
      formItemId1: crossAnalysisForm.formItemId1,
      formItemId2: crossAnalysisForm.formItemId2
    })

    if (res.code === 200) {
      crossAnalysisResult.value = res.data
      buildCrossTable(res.data.crossTable)
      buildHeatmap(res.data.crossTable)
    }
  } catch (error) {
    ElMessage.error('交叉分析失败')
    console.error('交叉分析错误:', error)
  } finally {
    crossAnalyzing.value = false
  }
}

// 构建交叉表
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

// 构建热力图
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

  heatmapChartOption.value = {
    title: {
      text: '交叉分析热力图',
      left: 'center'
    },
    tooltip: {
      position: 'top',
      formatter: function(params) {
        return `${yAxisData[params.data[1]]} × ${xAxisData[params.data[0]]}<br/>数量: ${params.data[2]}`
      }
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(...data.map(d => d[2])),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%'
    },
    series: [{
      name: '交叉分析',
      type: 'heatmap',
      data: data,
      label: {
        show: true
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
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
  if (!surveyId.value) return
  
  analysisLoading.value = true
  try {
    // 1. 使用后端API获取填写趋势
    const trendRes = await statisticsApi.getResponseTrend(surveyId.value, timeRange.value)
    if (trendRes.code === 200 && trendRes.data) {
      const trendData = trendRes.data.data || []
      trendChartOption.value = {
        title: { text: '填写趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: trendData.map(item => item.date)
        },
        yAxis: { type: 'value', name: '填写数量' },
        series: [{
          name: '填写数量',
          type: 'line',
          data: trendData.map(item => item.count),
          smooth: true,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ]
            }
          },
          itemStyle: { color: '#409EFF' }
        }]
      }
      }
      
    // 2. 使用后端API获取设备统计
    const deviceRes = await statisticsApi.getDeviceStatistics(surveyId.value)
    if (deviceRes.code === 200 && deviceRes.data) {
      const deviceCount = deviceRes.data.deviceCount || {}
      const deviceData = Object.entries(deviceCount).map(([name, value]) => ({
        name: name === 'PC' ? 'PC端' : name === 'MOBILE' ? '移动端' : name,
        value: value
      }))
      deviceChartOption.value = {
        title: { text: '设备类型分布', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '设备类型',
          type: 'pie',
          radius: '60%',
          data: deviceData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      }
      
    // 3. 使用后端API获取填写来源
    const sourceRes = await statisticsApi.getResponseSource(surveyId.value)
    if (sourceRes.code === 200 && sourceRes.data) {
      const sourceCount = sourceRes.data.sourceCount || {}
      const sourceData = Object.entries(sourceCount).map(([name, value]) => ({
        name: name === 'direct' ? '直接访问' : name === 'share' ? '分享链接' : name,
        value: value
      }))
      sourceChartOption.value = {
        title: { text: '填写来源', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '填写来源',
          type: 'pie',
          radius: '60%',
          data: sourceData
        }]
      }
    }
    
    // 4. 填写时段分布（需要从Response数据计算，暂时保留前端计算）
    // 这里可以从后端获取Response列表来计算
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 1000
    })
    if (dataRes.code === 200 && dataRes.data) {
      const dataList = dataRes.data.records || []
      const hourData = calculateHourData(dataList)
      hourChartOption.value = {
        title: { text: '填写时段分布', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: hourData.hours
        },
        yAxis: { type: 'value', name: '填写数量' },
        series: [{
          name: '填写数量',
          type: 'bar',
          data: hourData.counts,
          itemStyle: { color: '#409EFF' }
        }]
      }
    }
  } catch (error) {
    ElMessage.error('加载分析数据失败')
    console.error('加载分析数据错误:', error)
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
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  /* 自定义滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
}

.statistics-container::-webkit-scrollbar {
  width: 6px;
}

.statistics-container::-webkit-scrollbar-track {
  background: transparent;
}

.statistics-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 3px;
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
    flex-wrap: wrap;
  }
}

.stat-rating {
  .rating-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }
  
  .chart-wrapper {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
  }
}

.stat-number {
  .number-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
  }
}

.survey-overview-card {
  .el-statistic {
    text-align: center;
  }
}

.analysis-content {
  padding: 20px;
  min-height: 400px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  overflow-x: hidden;
}

.cross-analysis-card {
  margin-bottom: 20px;
}

.cross-result-section {
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
