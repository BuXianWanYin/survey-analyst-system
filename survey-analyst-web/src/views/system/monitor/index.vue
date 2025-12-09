<template>
  <div class="system-monitor">
    <h2 class="page-title">系统监控</h2>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>系统数据统计</span>
          </template>
          <div class="stat-item">
            <span class="stat-label">总用户数：</span>
            <span class="stat-value">{{ overview.totalUsers || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">活跃用户：</span>
            <span class="stat-value">{{ overview.activeUsers || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总问卷数：</span>
            <span class="stat-value">{{ overview.totalSurveys || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">已发布问卷：</span>
            <span class="stat-value">{{ overview.publishedSurveys || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总填写数：</span>
            <span class="stat-value">{{ overview.totalResponses || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">已完成填写：</span>
            <span class="stat-value">{{ overview.completedResponses || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
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
      <el-col :span="8">
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
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>今日操作数</span>
          </template>
          <div class="stat-item">
            <span class="stat-value-large">{{ todayOperations || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>今日登录数</span>
          </template>
          <div class="stat-item">
            <span class="stat-value-large">{{ todayLogins || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>系统负载</span>
          </template>
          <div class="stat-item">
            <span class="stat-value-large">正常</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
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
    console.error('加载系统概览失败:', error)
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
.system-monitor {
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

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #606266;
}

.stat-value {
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

.log-card {
  margin-top: 20px;
}

.log-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

