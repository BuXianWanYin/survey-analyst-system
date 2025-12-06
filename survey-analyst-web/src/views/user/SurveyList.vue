<template>
  <div class="survey-list-container">
    <div class="toolbar">
      <el-button type="primary" size="large" @click="goToCreateSurvey">
        <el-icon><Plus /></el-icon>
        创建问卷
      </el-button>
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入问卷标题"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </div>
    </div>

    <div class="status-filter">
      <el-radio-group v-model="statusFilter" @change="handleStatusChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="DRAFT">草稿</el-radio-button>
        <el-radio-button label="PUBLISHED">已发布</el-radio-button>
        <el-radio-button label="PAUSED">已暂停</el-radio-button>
        <el-radio-button label="ENDED">已结束</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="survey-grid">
      <el-card
        v-for="survey in surveyList"
        :key="survey.id"
        class="survey-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <el-tag type="primary" size="small">问卷</el-tag>
          </div>
        </template>
        <div class="card-body">
          <h3 class="survey-title">{{ survey.title }}</h3>
          <div class="survey-status">
            <span class="status-dot" :class="getStatusClass(survey.status)"></span>
            <span>{{ getStatusText(survey.status) }}</span>
          </div>
          <div class="survey-time">
            创建时间: {{ formatDate(survey.createTime) }}
          </div>
        </div>
        <div class="card-footer">
          <el-link type="primary" @click="handleEdit(survey.id)">编辑</el-link>
          <el-link type="primary" @click="handleView(survey.id)">查看</el-link>
          <el-link type="danger" @click="handleDelete(survey.id)">删除</el-link>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && surveyList.length === 0" description="暂无数据" />

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { surveyApi } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const surveyList = ref([])
const searchKeyword = ref('')
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadSurveyList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    const res = await surveyApi.getSurveyList(params)
    if (res.code === 200) {
      let surveys = res.data.records || []
      
      // 状态筛选
      if (statusFilter.value !== 'all') {
        surveys = surveys.filter(s => s.status === statusFilter.value)
      }
      
      // 搜索筛选
      if (searchKeyword.value) {
        surveys = surveys.filter(s => 
          s.title.includes(searchKeyword.value)
        )
      }
      
      surveyList.value = surveys
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载问卷列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadSurveyList()
}

const handleStatusChange = () => {
  currentPage.value = 1
  loadSurveyList()
}

const handleSizeChange = () => {
  loadSurveyList()
}

const handleCurrentChange = () => {
  loadSurveyList()
}

const goToCreateSurvey = () => {
  router.push('/user/survey/design')
}

const handleEdit = (id) => {
  router.push(`/user/survey/design?id=${id}`)
}

const handleView = (id) => {
  router.push(`/user/survey/view/${id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该问卷吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await surveyApi.deleteSurvey(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadSurveyList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getStatusText = (status) => {
  const statusMap = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    PAUSED: '已暂停',
    ENDED: '已结束'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status) => {
  const classMap = {
    DRAFT: 'status-draft',
    PUBLISHED: 'status-published',
    PAUSED: 'status-paused',
    ENDED: 'status-ended'
  }
  return classMap[status] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadSurveyList()
})
</script>

<style scoped>
.survey-list-container {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-section {
  display: flex;
  gap: 10px;
}

.status-filter {
  margin-bottom: 20px;
}

.survey-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.survey-card {
  cursor: pointer;
  transition: all 0.3s;
}

.survey-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: flex-end;
}

.card-body {
  min-height: 150px;
}

.survey-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #303133;
}

.survey-status {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #909399;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
}

.status-dot.status-draft {
  background: #909399;
}

.status-dot.status-published {
  background: #67C23A;
}

.status-dot.status-paused {
  background: #E6A23C;
}

.status-dot.status-ended {
  background: #F56C6C;
}

.survey-time {
  font-size: 12px;
  color: #c0c4cc;
}

.card-footer {
  display: flex;
  justify-content: space-around;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

