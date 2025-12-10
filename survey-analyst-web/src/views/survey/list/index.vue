<template>
  <div class="survey-list-container">
    <el-row :gutter="20" class="toolbar">
      <el-col :xs="24" :sm="24" :md="6" :lg="5" :xl="4">
        <el-button :icon="Plus" type="primary" size="large" @click="goToCreateSurvey">
          创建问卷
        </el-button>
      </el-col>
      <el-col :xs="24" :sm="24" :md="18" :lg="19" :xl="20">
        <div class="search-section">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入问卷标题"
            clearable
            class="search-input"
            style="max-width: 400px;"
            @keyup.enter="handleSearch"
          />
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="status-filter">
      <el-col :xs="24" :sm="24" :md="18" :lg="20" :xl="20">
        <div class="filter-left">
          <el-radio-group v-model="statusFilter" @change="handleStatusChange" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="DRAFT">草稿</el-radio-button>
            <el-radio-button label="PUBLISHED">已发布</el-radio-button>
            <el-radio-button label="ENDED">已结束</el-radio-button>
          </el-radio-group>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="6" :lg="4" :xl="4">
        <div class="view-toggle">
          <el-button-group>
            <el-button
              :type="viewMode === 'grid' ? 'primary' : 'default'"
              size="small"
              @click="viewMode = 'grid'"
            >
              <el-icon><Grid /></el-icon>
            </el-button>
            <el-button
              :type="viewMode === 'table' ? 'primary' : 'default'"
              size="small"
              @click="viewMode = 'table'"
            >
              <el-icon><List /></el-icon>
            </el-button>
          </el-button-group>
        </div>
      </el-col>
    </el-row>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'grid'" v-loading="loading" class="survey-grid">
      <el-card
        v-for="survey in surveyList"
        :key="survey.id"
        class="survey-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <el-tag type="primary" size="small">问卷</el-tag>
            <el-dropdown
              v-if="survey.status === 'PUBLISHED'"
              trigger="hover"
              placement="bottom-end"
              @command="handleShareCommand"
            >
              <el-button
                :icon="Share"
                text
                circle
                class="share-button"
                @click.stop
              />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'copyLink', id: survey.id }">
                    <el-icon><Link /></el-icon>
                    <span style="margin-left: 8px">复制链接</span>
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'copyQRCode', id: survey.id }">
                    <img src="/icons/qrcode.svg" class="share-icon" alt="二维码" />
                    <span style="margin-left: 8px">复制二维码</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
          <el-button :icon="Edit" type="primary" size="small" @click.stop="handleEdit(survey.id)">编辑</el-button>
          <el-button 
            v-if="survey.status === 'PUBLISHED'" 
            :icon="Promotion" 
            type="warning" 
            size="small" 
            @click.stop="handleUnpublish(survey.id)"
          >
            停止发布
          </el-button>
          <el-button 
            v-else 
            :icon="Promotion" 
            type="success" 
            size="small" 
            @click.stop="handlePublish(survey.id)"
          >
            发布
          </el-button>
          <el-button :icon="DataAnalysis" type="warning" size="small" @click.stop="handleStatistics(survey.id)">统计</el-button>
          <el-button :icon="Delete" type="danger" size="small" @click.stop="handleDelete(survey.id)">删除</el-button>
        </div>
      </el-card>
    </div>

    <!-- 表格视图 -->
    <div v-if="viewMode === 'table'" v-loading="loading" class="survey-table">
      <el-table
        :data="surveyList"
        border
        empty-text="暂无数据"
        highlight-current-row
        style="width: 100%"
      >
        <el-table-column prop="title" label="问卷标题" min-width="200" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <span class="status-dot" :class="getStatusClass(row.status)"></span>
            <span>{{ getStatusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" align="center" fixed="right">
          <template #default="{ row }">
            <el-button :icon="Edit" type="primary" size="small" @click.stop="handleEdit(row.id)">编辑</el-button>
            <el-button 
              v-if="row.status === 'PUBLISHED'" 
              :icon="Promotion" 
              type="warning" 
              size="small" 
              @click.stop="handleUnpublish(row.id)"
            >
              停止发布
            </el-button>
            <el-button 
              v-else 
              :icon="Promotion" 
              type="success" 
              size="small" 
              @click.stop="handlePublish(row.id)"
            >
              发布
            </el-button>
            <el-button :icon="DataAnalysis" type="warning" size="small" @click.stop="handleStatistics(row.id)">统计</el-button>
            <el-dropdown
              v-if="row.status === 'PUBLISHED'"
              trigger="hover"
              placement="bottom-end"
              @command="handleShareCommand"
            >
              <el-button :icon="Share" type="info" size="small" @click.stop>
                分享
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'copyLink', id: row.id }">
                    <el-icon><Link /></el-icon>
                    <span style="margin-left: 8px">复制链接</span>
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'copyQRCode', id: row.id }">
                    <img src="/icons/qrcode.svg" class="share-icon" alt="二维码" />
                    <span style="margin-left: 8px">复制二维码</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button :icon="Delete" type="danger" size="small" @click.stop="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-if="!loading && surveyList.length === 0" description="暂无数据" />

    <!-- 二维码对话框 -->
    <el-dialog
      v-model="showQRCodeDialog"
      title="问卷二维码"
      :width="qrCodeDialogWidth"
      align-center
    >
      <div v-if="currentQRCode" class="qrcode-dialog-content">
        <img :src="currentQRCode" alt="二维码" class="qrcode-dialog-image">
        <div class="qrcode-dialog-actions">
          <el-button type="primary" @click="handleDownloadQRCode">下载二维码</el-button>
          <el-button @click="handleCopyQRCodeImage">复制二维码</el-button>
        </div>
      </div>
      <div v-else class="qrcode-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span style="margin-left: 10px">正在生成二维码...</span>
      </div>
    </el-dialog>

    <!-- 创建问卷对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建问卷"
      :width="dialogWidth"
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
        <el-button :icon="Close" @click="showCreateDialog = false">取消</el-button>
        <el-button :icon="Check" type="primary" @click="handleCreateSurvey">确定</el-button>
      </template>
    </el-dialog>

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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Grid, List, Search, Edit, Promotion, DataAnalysis, Delete, Close, Check, Share, Link, Loading } from '@element-plus/icons-vue'
import { surveyApi, surveyPublishApi } from '@/api'
import dayjs from 'dayjs'
import { useWindowSize } from '@vueuse/core'

const router = useRouter()
const { width } = useWindowSize()

const dialogWidth = computed(() => {
  if (width.value < 768) {
    return '90%'
  } else if (width.value < 992) {
    return '80%'
  } else {
    return '500px'
  }
})

const qrCodeDialogWidth = computed(() => {
  if (width.value < 768) {
    return '90%'
  } else {
    return '400px'
  }
})

const loading = ref(false)
const surveyList = ref([])
const searchKeyword = ref('')
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const viewMode = ref('grid') // 'grid' 或 'table'

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

const showCreateDialog = ref(false)
const createForm = ref({
  title: '',
  description: ''
})

// 二维码相关
const showQRCodeDialog = ref(false)
const currentQRCode = ref('')
const currentQRCodeSurveyId = ref(null)

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
      router.push(`/survey/edit/editor?id=${res.data.id}`)
    }
  } catch (error) {
    ElMessage.error('创建问卷失败')
  }
}

const handleEdit = (id) => {
  router.push(`/survey/edit/editor?id=${id}`)
}

const handlePublish = (id) => {
  router.push(`/survey/edit/publish?id=${id}`)
}

const handleUnpublish = async (id) => {
  try {
    await ElMessageBox.confirm('确定要停止发布该问卷吗？停止后将变为草稿状态。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await surveyApi.unpublishSurvey(id)
    if (res.code === 200) {
      ElMessage.success('已停止发布')
      await loadSurveyList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleStatistics = (id) => {
  router.push(`/survey/statistics?id=${id}`)
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
    PUBLISHED: '收集中',
    ENDED: '已结束'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status) => {
  const classMap = {
    DRAFT: 'status-draft',
    PUBLISHED: 'status-published',
    ENDED: 'status-ended'
  }
  return classMap[status] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 分享功能处理
const handleShareCommand = async ({ action, id }) => {
  try {
    switch (action) {
      case 'copyLink': {
        const res = await surveyPublishApi.getSurveyLink(id)
        if (res.code === 200) {
          await navigator.clipboard.writeText(res.data)
          ElMessage.success('链接已复制到剪贴板')
        }
        break
      }
      case 'copyQRCode': {
        // 显示二维码对话框
        currentQRCodeSurveyId.value = id
        currentQRCode.value = ''
        showQRCodeDialog.value = true
        // 加载二维码
        const res = await surveyPublishApi.getQRCode(id)
        if (res.code === 200) {
          currentQRCode.value = res.data
        } else {
          ElMessage.error('生成二维码失败')
          showQRCodeDialog.value = false
        }
        break
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 下载二维码
const handleDownloadQRCode = () => {
  if (!currentQRCode.value) return
  
  const link = document.createElement('a')
  link.href = currentQRCode.value
  link.download = `survey-${currentQRCodeSurveyId.value}-qrcode.png`
  link.click()
  ElMessage.success('二维码下载成功')
}

// 复制二维码图片
const handleCopyQRCodeImage = async () => {
  if (!currentQRCode.value) return
  
  try {
    const response = await fetch(currentQRCode.value)
    const blob = await response.blob()
    await navigator.clipboard.write([
      new ClipboardItem({ [blob.type]: blob })
    ])
    ElMessage.success('二维码已复制到剪贴板')
  } catch (error) {
    // 如果浏览器不支持复制图片，提示下载
    ElMessage.warning('当前浏览器不支持复制图片，请使用下载功能')
    handleDownloadQRCode()
  }
}

onMounted(() => {
  loadSurveyList()
})
</script>

<style scoped>
.survey-list-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.toolbar {
  margin-bottom: 20px;
}

.search-section {
  display: flex;
  gap: 10px;
  width: 100%;
}

.search-input {
  flex: 1;
  min-width: 0;
}

@media (max-width: 768px) {
  .search-section {
    flex-direction: column;
  }
  
  .search-section .el-button {
    width: 100%;
  }
}

.status-filter {
  margin-bottom: 20px;
}

.filter-left {
  width: 100%;
  overflow-x: auto;
}

.view-toggle {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

@media (min-width: 768px) {
  .view-toggle {
    margin-top: 0;
  }
}

.survey-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .survey-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
}

.survey-card {
  cursor: pointer;
  transition: all 0.3s;
  min-width: 0;
}

.survey-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.share-button {
  color: #409eff;
  font-size: 18px;
}

.share-button:hover {
  color: #66b1ff;
}

.share-icon {
  width: 16px;
  height: 16px;
  vertical-align: middle;
  display: inline-block;
  margin-right: 0;
  object-fit: contain;
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
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  flex-wrap: nowrap;
  gap: 8px;
}

.card-footer .el-button {
  flex: 1;
  min-width: 0;
}

.survey-table {
  margin-bottom: 20px;
  background: white;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.qrcode-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
}

.qrcode-dialog-image {
  width: 100%;
  max-width: 300px;
  height: auto;
  aspect-ratio: 1;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.qrcode-dialog-actions {
  display: flex;
  gap: 10px;
}

.qrcode-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
</style>


