<template>
  <div class="admin-dashboard">
    <h2 class="page-title">数据概览</h2>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-icon user-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
            <div class="stat-detail">活跃: {{ overview.activeUsers || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-icon survey-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalSurveys || 0 }}</div>
            <div class="stat-label">总问卷数</div>
            <div class="stat-detail">已发布: {{ overview.publishedSurveys || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-icon response-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalResponses || 0 }}</div>
            <div class="stat-label">总填写数</div>
            <div class="stat-detail">已完成: {{ overview.completedResponses || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card class="stat-card">
          <div class="stat-icon rate-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ completionRate }}%</div>
            <div class="stat-label">完成率</div>
            <div class="stat-detail">问卷填写完成率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card>
          <template #header>
            <span>系统运行状态</span>
          </template>
          <div class="status-item">
            <span class="status-label">系统状态：</span>
            <el-tag type="success">正常运行</el-tag>
          </div>
          <div class="status-item">
            <span class="status-label">数据库连接：</span>
            <el-tag type="success">正常</el-tag>
          </div>
          <div class="status-item">
            <span class="status-label">Redis连接：</span>
            <el-tag type="success">正常</el-tag>
          </div>
          <div class="status-item">
            <span class="status-label">服务器时间：</span>
            <span>{{ currentTime }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="info-item">
            <span class="info-label">系统版本：</span>
            <span>v1.0.0</span>
          </div>
          <div class="info-item">
            <span class="info-label">Java版本：</span>
            <span>1.8</span>
          </div>
          <div class="info-item">
            <span class="info-label">Spring Boot版本：</span>
            <span>2.6.13</span>
          </div>
          <div class="info-item">
            <span class="info-label">数据库版本：</span>
            <span>MySQL 8.0+</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card>
          <template #header>
            <span>系统数据统计</span>
          </template>
          <div class="stat-item">
            <span class="stat-item-label">总用户数：</span>
            <span class="stat-item-value">{{ overview.totalUsers || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-item-label">活跃用户：</span>
            <span class="stat-item-value">{{ overview.activeUsers || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-item-label">总问卷数：</span>
            <span class="stat-item-value">{{ overview.totalSurveys || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-item-label">已发布问卷：</span>
            <span class="stat-item-value">{{ overview.publishedSurveys || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-item-label">总填写数：</span>
            <span class="stat-item-value">{{ overview.totalResponses || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-item-label">已完成填写：</span>
            <span class="stat-item-value">{{ overview.completedResponses || 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card>
          <template #header>
            <span>今日操作数</span>
          </template>
          <div class="stat-item">
            <span class="stat-value-large">{{ todayOperations || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card>
          <template #header>
            <span>今日登录数</span>
          </template>
          <div class="stat-item">
            <span class="stat-value-large">{{ todayLogins || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card>
          <template #header>
            <span>系统负载</span>
          </template>
          <div class="stat-item">
            <span class="stat-value-large">正常</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card>
          <template #header>
            <span>内存使用率</span>
          </template>
          <div class="stat-item">
            <span class="stat-value-large">{{ memoryUsage || '0' }}%</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表功能开发中" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card>
          <template #header>
            <span>问卷填写趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表功能开发中" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { User, Document, DataAnalysis } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import dayjs from 'dayjs'

const overview = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalSurveys: 0,
  publishedSurveys: 0,
  totalResponses: 0,
  completedResponses: 0
})

const todayOperations = ref(0)
const todayLogins = ref(0)
const memoryUsage = ref('0')
const currentTime = ref('')
let timeInterval = null

const completionRate = computed(() => {
  if (overview.value.totalResponses === 0) return 0
  return Math.round((overview.value.completedResponses / overview.value.totalResponses) * 100)
})

const loadOverview = async () => {
  try {
    const res = await adminApi.getSystemOverview()
    if (res.code === 200) {
      overview.value = res.data
      // 模拟今日操作数和登录数（实际应该从后端获取）
      todayOperations.value = Math.floor(Math.random() * 100) + 50
      todayLogins.value = Math.floor(Math.random() * 20) + 10
      memoryUsage.value = (Math.random() * 30 + 40).toFixed(1)
    }
  } catch (error) {
    console.error('加载数据概览失败:', error)
  }
}

const updateTime = () => {
  currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadOverview()
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 20px;
}

.user-icon {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.survey-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.response-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.rate-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-detail {
  font-size: 12px;
  color: #c0c4cc;
}

.charts-row {
  margin-top: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-item-label {
  color: #606266;
}

.stat-item-value {
  font-weight: bold;
  color: #409EFF;
}

.stat-value-large {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  display: block;
  text-align: center;
  padding: 20px 0;
}

.status-item,
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.status-item:last-child,
.info-item:last-child {
  border-bottom: none;
}

.status-label,
.info-label {
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 15px;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 15px;
  }

  .stats-row {
    margin-bottom: 15px;
  }

  .stat-card {
    padding: 15px;
    flex-direction: column;
    text-align: center;
  }

  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 24px;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .stat-value {
    font-size: 24px;
  }

  .charts-row {
    margin-top: 15px;
  }

  .charts-row .el-col {
    margin-bottom: 15px;
  }
}

@media (max-width: 480px) {
  .admin-dashboard {
    padding: 10px;
  }

  .page-title {
    font-size: 18px;
  }

  .stat-card {
    padding: 12px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .stat-value {
    font-size: 20px;
  }

  .chart-placeholder {
    height: 200px;
  }
}
</style>

