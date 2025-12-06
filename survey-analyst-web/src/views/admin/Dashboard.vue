<template>
  <div class="admin-dashboard">
    <h2 class="page-title">数据概览</h2>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
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
      <el-col :span="6">
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
      <el-col :span="6">
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
      <el-col :span="6">
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

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表功能开发中" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
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
import { ref, computed, onMounted } from 'vue'
import { User, Document, DataAnalysis } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const overview = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalSurveys: 0,
  publishedSurveys: 0,
  totalResponses: 0,
  completedResponses: 0
})

const completionRate = computed(() => {
  if (overview.value.totalResponses === 0) return 0
  return Math.round((overview.value.completedResponses / overview.value.totalResponses) * 100)
})

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

onMounted(() => {
  loadOverview()
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
</style>
