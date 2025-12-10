<template>
  <div class="survey-management">
    <h2 class="page-title">问卷管理</h2>

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-section">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索问卷标题"
              clearable
              style="width: 300px"
              @keyup.enter="handleSearch"
            />
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 150px; margin-left: 10px">
              <el-option label="全部" value="" />
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="已暂停" value="PAUSED" />
              <el-option label="已结束" value="ENDED" />
            </el-select>
            <el-button :icon="Search" type="primary" @click="handleSearch" style="margin-left: 10px">查询</el-button>
          </div>
          <div class="view-toggle-item">
            <el-radio-group v-model="viewMode" size="default" class="view-toggle-group">
              <el-radio-button :label="'card'">
                <el-icon><Grid /></el-icon>
              </el-radio-button>
              <el-radio-button :label="'table'">
                <el-icon><List /></el-icon>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card'" class="card-view-container" v-loading="loading">
        <div class="survey-grid-view">
          <div
            v-for="survey in surveyList"
            :key="survey.id"
            class="survey-card-item"
          >
            <div class="survey-card-header">
              <h3 class="survey-card-title">{{ survey.title }}</h3>
              <el-tag :type="getStatusType(survey.status)" size="small">
                {{ getStatusText(survey.status) }}
              </el-tag>
            </div>
            <div class="survey-card-content">
              <p class="survey-card-desc" v-if="survey.description">
                {{ survey.description }}
              </p>
              <p class="survey-card-desc" v-else style="color: #909399">暂无描述</p>
              <div class="survey-card-info">
                <span class="info-item">
                  <el-icon><User /></el-icon>
                  创建用户：{{ survey.username || '未知' }}
                </span>
                <span class="info-item">
                  <el-icon><Lock /></el-icon>
                  访问类型：{{ getAccessTypeText(survey.accessType) }}
                </span>
                <span class="info-item">
                  <el-icon><Clock /></el-icon>
                  {{ survey.createTime }}
                </span>
              </div>
            </div>
            <div class="survey-card-actions">
              <el-button :icon="View" type="primary" size="small" @click="handleView(survey)">查看</el-button>
              <el-button
                v-if="survey.status === 'PUBLISHED'"
                :icon="VideoPause"
                type="warning"
                size="small"
                @click="handleUpdateStatus(survey, 'PAUSED')"
              >
                暂停
              </el-button>
              <el-button
                v-if="survey.status === 'PAUSED'"
                :icon="VideoPlay"
                type="success"
                size="small"
                @click="handleUpdateStatus(survey, 'PUBLISHED')"
              >
                恢复
              </el-button>
              <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(survey)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && surveyList.length === 0" description="暂无问卷" />
      </div>

      <!-- 表格视图 -->
      <el-table v-else v-loading="loading" :data="surveyList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="问卷标题" min-width="200" />
        <el-table-column prop="username" label="创建用户" width="120">
          <template #default="{ row }">
            {{ row.username || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accessType" label="访问类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getAccessTypeText(row.accessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button :icon="View" type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 'PUBLISHED'"
              :icon="VideoPause"
              type="warning"
              size="small"
              @click="handleUpdateStatus(row, 'PAUSED')"
            >
              暂停
            </el-button>
            <el-button
              v-if="row.status === 'PAUSED'"
              :icon="VideoPlay"
              type="success"
              size="small"
              @click="handleUpdateStatus(row, 'PUBLISHED')"
            >
              恢复
            </el-button>
            <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Grid, List, User, Clock, Lock, Search, View, VideoPause, VideoPlay, Delete } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const viewMode = ref('card') // 'card' 卡片视图, 'table' 表格视图
const surveyList = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadSurveyList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value || undefined
    }
    const res = await adminApi.getSurveyList(params)
    if (res.code === 200) {
      surveyList.value = res.data.records || []
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

const handleSizeChange = () => {
  loadSurveyList()
}

const handleCurrentChange = () => {
  loadSurveyList()
}

const handleView = (row) => {
  // 跳转到问卷预览页面（只读，不能提交）
  router.push(`/survey/preview/${row.id}`)
}

const handleUpdateStatus = async (row, status) => {
  try {
    const res = await adminApi.updateSurveyStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      loadSurveyList()
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该问卷吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await adminApi.deleteSurvey(row.id)
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

const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    PAUSED: 'warning',
    ENDED: 'danger'
  }
  return typeMap[status] || 'info'
}

const getAccessTypeText = (accessType) => {
  const typeMap = {
    PUBLIC: '公开',
    PASSWORD: '密码',
    PRIVATE: '私有'
  }
  return typeMap[accessType] || '未知'
}

onMounted(() => {
  loadSurveyList()
})
</script>

<style scoped>
.survey-management {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-section {
  display: flex;
  align-items: center;
}

.view-toggle-item {
  margin-left: auto;
}

.view-toggle-group {
  display: flex;
  justify-content: center;
}

.card-view-container {
  min-height: 400px;
}

.survey-grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  width: 100%;
}

.survey-card-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.survey-card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.survey-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.survey-card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  flex: 1;
  margin-right: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.survey-card-content {
  margin-bottom: 16px;
}

.survey-card-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.survey-card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.survey-card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

