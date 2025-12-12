<template>
  <div class="dashboard-container">
    <h2 class="page-title">数据概览</h2>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="kpi-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-label">总用户数</div>
            <div class="kpi-value">{{ overview.totalUsers || 0 }}</div>
            <div class="kpi-change">
              <span class="change-label">活跃</span>
              <span class="change-value">{{ overview.activeUsers || 0 }}</span>
            </div>
          </div>
          <div class="kpi-icon user-icon">
            <el-icon><User /></el-icon>
          </div>
          </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-label">总问卷数</div>
            <div class="kpi-value">{{ overview.totalSurveys || 0 }}</div>
            <div class="kpi-change">
              <span class="change-label">已发布</span>
              <span class="change-value">{{ overview.publishedSurveys || 0 }}</span>
            </div>
          </div>
          <div class="kpi-icon survey-icon">
            <el-icon><Document /></el-icon>
          </div>
          </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-label">总填写数</div>
            <div class="kpi-value">{{ overview.totalResponses || 0 }}</div>
            <div class="kpi-change">
              <span class="change-label">已完成</span>
              <span class="change-value">{{ overview.completedResponses || 0 }}</span>
            </div>
          </div>
          <div class="kpi-icon response-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <div class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-label">今日新增问卷</div>
            <div class="kpi-value">{{ todaySurveys || 0 }}</div>
            <div class="kpi-change">
              <span class="change-label">今日创建</span>
              <span class="change-value positive">+{{ todaySurveys || 0 }}</span>
          </div>
          </div>
          <div class="kpi-icon today-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          </div>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 问卷创建趋势图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" v-loading="loading.createTrend">
          <template #header>
            <div class="card-header">
              <span>问卷创建趋势</span>
              <el-radio-group v-model="timeRange.create" size="small" @change="loadCreateTrend">
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
                <el-radio-button label="90d">90天</el-radio-button>
              </el-radio-group>
          </div>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="createTrendOption"
              :option="createTrendOption"
              :style="{ height: '300px' }"
              autoresize
            />
            <el-empty v-else description="暂无数据" :image-size="100" />
          </div>
        </el-card>
      </el-col>

      <!-- 问卷填写趋势图 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" v-loading="loading.responseTrend">
          <template #header>
            <div class="card-header">
              <span>问卷填写趋势</span>
              <el-radio-group v-model="timeRange.response" size="small" @change="loadResponseTrend">
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
                <el-radio-button label="90d">90天</el-radio-button>
              </el-radio-group>
          </div>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="responseTrendOption"
              :option="responseTrendOption"
              :style="{ height: '300px' }"
              autoresize
            />
            <el-empty v-else description="暂无数据" :image-size="100" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 平均填写时长趋势图和登录趋势图 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" v-loading="loading.durationTrend">
          <template #header>
            <div class="card-header">
              <span>平均填写时长趋势</span>
              <el-radio-group v-model="timeRange.duration" size="small" @change="loadDurationTrend">
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
                <el-radio-button label="90d">90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="durationTrendOption"
              :option="durationTrendOption"
              :style="{ height: '300px' }"
              autoresize
            />
            <el-empty v-else description="暂无数据" :image-size="100" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card class="chart-card" v-loading="loading.loginTrend">
          <template #header>
            <div class="card-header">
              <span>登录趋势</span>
              <el-radio-group v-model="timeRange.login" size="small" @change="loadLoginTrend">
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
                <el-radio-button label="90d">90天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="loginTrendOption"
              :option="loginTrendOption"
              :style="{ height: '300px' }"
              autoresize
            />
            <el-empty v-else description="暂无数据" :image-size="100" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, Document, DataAnalysis, TrendCharts } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { createLineChart, createBarChart } from '@/utils/echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const overview = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalSurveys: 0,
  publishedSurveys: 0,
  totalResponses: 0,
  completedResponses: 0
})

const todaySurveys = ref(0)
const loading = reactive({
  createTrend: false,
  responseTrend: false,
  durationTrend: false,
  loginTrend: false
})

const timeRange = reactive({
  create: '30d',
  response: '30d',
  duration: '30d',
  login: '30d'
})

const createTrendOption = ref(null)
const responseTrendOption = ref(null)
const durationTrendOption = ref(null)
const loginTrendOption = ref(null)

// 加载系统概览数据
const loadOverview = async () => {
  try {
    const res = await adminApi.getSystemOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('加载数据概览失败:', error)
  }
}

// 加载今日新增问卷数
const loadTodaySurveys = async () => {
  try {
    const res = await adminApi.getTodaySurveys()
    if (res.code === 200) {
      todaySurveys.value = res.data.todaySurveys || 0
    }
  } catch (error) {
    console.error('加载今日新增问卷数失败:', error)
  }
}

// 加载问卷创建趋势
const loadCreateTrend = async () => {
  loading.createTrend = true
  try {
    const res = await adminApi.getSurveyCreateTrend(timeRange.create)
    if (res.code === 200 && res.data) {
      const data = res.data
      const xAxisData = data.map(item => {
        // 格式化日期显示，只显示月-日
        // item.date格式为 "yyyy-MM-dd"
        const parts = item.date.split('-')
        return `${parts[1]}-${parts[2]}`
      })
      const seriesData = data.map(item => item.count)

      // 使用柔和的蓝色调
      createTrendOption.value = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e4e7ed',
          borderWidth: 1,
          textStyle: {
            color: '#606266'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xAxisData,
          name: '日期',
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          axisLabel: {
            color: '#909399'
          },
          splitLine: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          name: '问卷数',
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          axisLabel: {
            color: '#909399'
          },
          splitLine: {
            lineStyle: {
              color: '#f0f2f5',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '创建数',
            type: 'line',
            smooth: true,
            data: seriesData,
            lineStyle: {
              color: '#6BA3FF',
              width: 3
            },
            itemStyle: {
              color: '#6BA3FF'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(107, 163, 255, 0.3)' },
                  { offset: 1, color: 'rgba(107, 163, 255, 0.05)' }
                ]
              }
            },
            symbol: 'circle',
            symbolSize: 6
          }
        ]
      }
    }
  } catch (error) {
    console.error('加载问卷创建趋势失败:', error)
  } finally {
    loading.createTrend = false
  }
}

// 加载问卷填写趋势
const loadResponseTrend = async () => {
  loading.responseTrend = true
  try {
    const res = await adminApi.getResponseTrend(timeRange.response)
    if (res.code === 200 && res.data) {
      const data = res.data
      const xAxisData = data.map(item => {
        // item.date格式为 "yyyy-MM-dd"
        const parts = item.date.split('-')
        return `${parts[1]}-${parts[2]}`
      })
      // 所有提交的都是已完成的，所以只显示总填写数
      const totalData = data.map(item => item.total)

      // 使用柔和的蓝色调柱状图
      responseTrendOption.value = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
            shadowStyle: {
              color: 'rgba(107, 163, 255, 0.1)'
            }
          },
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e4e7ed',
          borderWidth: 1,
          textStyle: {
            color: '#606266'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xAxisData,
          axisLabel: {
            rotate: xAxisData.length > 15 ? 45 : 0,
            color: '#909399'
          },
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          splitLine: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          name: '填写数',
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          axisLabel: {
            color: '#909399'
          },
          splitLine: {
            lineStyle: {
              color: '#f0f2f5',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '填写数',
            type: 'bar',
            data: totalData,
            barWidth: '60%',
            itemStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: '#7BB3FF' },
                  { offset: 1, color: '#6BA3FF' }
                ]
              },
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color: '#8BC3FF' },
                    { offset: 1, color: '#7BB3FF' }
                  ]
                }
              }
            }
          }
        ]
      }
    }
  } catch (error) {
    console.error('加载问卷填写趋势失败:', error)
  } finally {
    loading.responseTrend = false
  }
}

// 加载平均填写时长趋势
const loadDurationTrend = async () => {
  loading.durationTrend = true
  try {
    const res = await adminApi.getResponseDurationTrend(timeRange.duration)
    if (res.code === 200 && res.data) {
      const data = res.data
      const xAxisData = data.map(item => {
        // item.date格式为 "yyyy-MM-dd"
        const parts = item.date.split('-')
        return `${parts[1]}-${parts[2]}`
      })
      const seriesData = data.map(item => item.avgDuration || 0)

      // 使用柔和的蓝色调
      durationTrendOption.value = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e4e7ed',
          borderWidth: 1,
          textStyle: {
            color: '#606266'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xAxisData,
          name: '日期',
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          axisLabel: {
            color: '#909399'
          },
          splitLine: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          name: '时长（秒）',
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          axisLabel: {
            color: '#909399'
          },
          splitLine: {
            lineStyle: {
              color: '#f0f2f5',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '平均时长',
            type: 'line',
            smooth: true,
            data: seriesData,
            lineStyle: {
              color: '#8BC3FF',
              width: 3
            },
            itemStyle: {
              color: '#8BC3FF'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(139, 195, 255, 0.3)' },
                  { offset: 1, color: 'rgba(139, 195, 255, 0.05)' }
                ]
              }
            },
            symbol: 'circle',
            symbolSize: 6
          }
        ]
      }
    }
  } catch (error) {
    console.error('加载平均填写时长趋势失败:', error)
  } finally {
    loading.durationTrend = false
  }
}

// 加载登录趋势
const loadLoginTrend = async () => {
  loading.loginTrend = true
  try {
    const res = await adminApi.getLoginTrend(timeRange.login)
    if (res.code === 200 && res.data) {
      const data = res.data
      const xAxisData = data.map(item => {
        // item.date格式为 "yyyy-MM-dd"
        const parts = item.date.split('-')
        return `${parts[1]}-${parts[2]}`
      })
      const seriesData = data.map(item => item.count)

      // 使用柔和的蓝色调
      loginTrendOption.value = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e4e7ed',
          borderWidth: 1,
          textStyle: {
            color: '#606266'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xAxisData,
          name: '日期',
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          axisLabel: {
            color: '#909399'
          },
          splitLine: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          name: '登录次数',
          axisLine: {
            lineStyle: {
              color: '#e4e7ed'
            }
          },
          axisLabel: {
            color: '#909399'
          },
          splitLine: {
            lineStyle: {
              color: '#f0f2f5',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '登录数',
            type: 'line',
            smooth: true,
            data: seriesData,
            lineStyle: {
              color: '#9CD3FF',
              width: 3
            },
            itemStyle: {
              color: '#9CD3FF'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(156, 211, 255, 0.3)' },
                  { offset: 1, color: 'rgba(156, 211, 255, 0.05)' }
                ]
              }
            },
            symbol: 'circle',
            symbolSize: 6
          }
        ]
      }
    }
  } catch (error) {
    console.error('加载登录趋势失败:', error)
  } finally {
    loading.loginTrend = false
  }
}

onMounted(() => {
  loadOverview()
  loadTodaySurveys()
  loadCreateTrend()
  loadResponseTrend()
  loadDurationTrend()
  loadLoginTrend()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
  overflow-x: hidden;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.kpi-row {
  margin-bottom: 20px;
}

.kpi-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  height: 100%;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.kpi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.2), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.kpi-card:hover {
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: rgba(0, 0, 0, 0.04);
}

.kpi-card:hover::before {
  opacity: 1;
}

.kpi-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kpi-label {
  font-size: 13px;
  color: #8c8c8c;
  font-weight: 400;
  margin: 0;
}

.kpi-value {
  font-size: 26px;
  font-weight: 600;
  color: #1f1f1f;
  line-height: 1.2;
  margin: 4px 0;
  letter-spacing: -0.3px;
}

.kpi-change {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 2px;
}

.change-label {
  font-size: 12px;
  color: #8c8c8c;
  font-weight: 400;
}

.change-value {
  font-size: 12px;
  font-weight: 500;
  color: #52c41a;
}

.change-value.positive {
  color: #52c41a;
}

.change-value.negative {
  color: #ff4d4f;
}

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  position: relative;
  transition: transform 0.3s ease;
}

.kpi-card:hover .kpi-icon {
  transform: scale(1.05);
}

.user-icon {
  background: rgba(64, 158, 255, 0.08);
  color: #409EFF;
}

.survey-icon {
  background: rgba(255, 107, 107, 0.08);
  color: #ff6b6b;
}

.response-icon {
  background: rgba(82, 196, 26, 0.08);
  color: #52c41a;
}

.today-icon {
  background: rgba(250, 173, 20, 0.08);
  color: #faad14;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 15px;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 15px;
  }

  .kpi-card {
    padding: 15px;
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .kpi-content {
    align-items: center;
  }

  .kpi-icon {
    width: 45px;
    height: 45px;
    font-size: 20px;
    margin: 0;
  }

  .kpi-value {
    font-size: 24px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .chart-container {
    min-height: 250px;
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    padding: 10px;
  }

  .kpi-card {
    padding: 12px;
  }

  .kpi-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }

  .kpi-value {
    font-size: 20px;
  }

  .kpi-label {
    font-size: 12px;
  }

  .change-label,
  .change-value {
    font-size: 11px;
  }

  .chart-container {
    min-height: 200px;
  }
}

/* 自定义滚动条样式 */
.dashboard-container::-webkit-scrollbar {
  width: 8px;
}

.dashboard-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.dashboard-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.dashboard-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
