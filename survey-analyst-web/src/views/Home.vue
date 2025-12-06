<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <h2>欢迎使用在线问卷调查系统</h2>
      <p class="welcome-desc">快速创建问卷，收集数据，进行多维度分析</p>
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="goToCreateSurvey">
          <el-icon><Plus /></el-icon>
          创建问卷
        </el-button>
        <el-button size="large" @click="goToSurveyList">
          <el-icon><List /></el-icon>
          我的问卷
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalSurveys }}</div>
          <div class="stat-label">总问卷数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.publishedSurveys }}</div>
          <div class="stat-label">已发布</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalResponses }}</div>
          <div class="stat-label">总填写数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.draftSurveys }}</div>
          <div class="stat-label">草稿</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, List } from '@element-plus/icons-vue'
import { surveyApi } from '@/api'

const router = useRouter()

const stats = ref({
  totalSurveys: 0,
  publishedSurveys: 0,
  totalResponses: 0,
  draftSurveys: 0
})

const goToCreateSurvey = () => {
  router.push('/user/survey/design')
}

const goToSurveyList = () => {
  router.push('/user/survey/list')
}

onMounted(async () => {
  try {
    const res = await surveyApi.getSurveyList({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      const surveys = res.data.records || []
      stats.value.totalSurveys = surveys.length
      stats.value.publishedSurveys = surveys.filter(s => s.status === 'PUBLISHED').length
      stats.value.draftSurveys = surveys.filter(s => s.status === 'DRAFT').length
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
  text-align: center;
}

.welcome-card h2 {
  margin-bottom: 10px;
  color: #303133;
}

.welcome-desc {
  color: #606266;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.stats-row {
  margin-top: 20px;
}

.stat-card {
  text-align: center;
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
