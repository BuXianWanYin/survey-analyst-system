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

    <!-- 创建问卷对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建问卷"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="问卷名称" required>
          <el-input
            v-model="createForm.title"
            placeholder="请输入问卷名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="问卷描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入问卷描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSurvey">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, List } from '@element-plus/icons-vue'
import { surveyApi } from '@/api'

const router = useRouter()

const stats = ref({
  totalSurveys: 0,
  publishedSurveys: 0,
  totalResponses: 0,
  draftSurveys: 0
})

const showCreateDialog = ref(false)
const createForm = ref({
  title: '',
  description: ''
})

const goToCreateSurvey = () => {
  createForm.value = {
    title: '',
    description: ''
  }
  showCreateDialog.value = true
}

const handleCreateSurvey = async () => {
  if (!createForm.value.title.trim()) {
    ElMessage.warning('请输入问卷名称')
    return
  }
  
  try {
    const res = await surveyApi.createSurvey({
      title: createForm.value.title,
      description: createForm.value.description || ''
    })
    
    if (res.code === 200 && res.data) {
      ElMessage.success('创建成功')
      showCreateDialog.value = false
      // 跳转到编辑页，并传递问卷ID
      router.push(`/user/survey/design?id=${res.data.id}`)
    }
  } catch (error) {
    ElMessage.error('创建问卷失败')
  }
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
