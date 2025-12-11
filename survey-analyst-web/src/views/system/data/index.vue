<template>
  <div class="data-management">

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-section">
            <el-input
              v-model="surveyIdFilter"
              placeholder="问卷ID筛选"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <el-button :icon="Search" type="primary" @click="handleSearch" class="search-button">查询</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="responseList" border style="width: 100%">
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="surveyId" label="问卷ID" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'info'">
              {{ row.status === 'COMPLETED' ? '已完成' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceType" label="设备类型" min-width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.deviceType || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="填写时长" min-width="100">
          <template #default="{ row }">
            {{ row.duration ? row.duration + '秒' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="180" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-button :icon="View" type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        :layout="paginationLayout"
        :pager-count="width < 768 ? 5 : 7"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
    
    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="填写详情"
      width="900px"
    >
      <div v-if="currentFormData" class="data-detail" v-loading="detailLoading">
        <div class="detail-form-container">
          <SurveyFormRender
            :form-items="formItems"
            :form-model="getFormModel(currentFormData)"
            :preview-mode="true"
            :theme-config="themeConfig"
            :show-number="themeConfig.showNumber || false"
          />
        </div>
        <div class="detail-info">
          <div class="detail-item">
            <div class="detail-label">提交时间</div>
            <div class="detail-value">{{ formatDate(currentFormData.createTime) }}</div>
          </div>
          <div v-if="currentFormData.submitRequestIp" class="detail-item">
            <div class="detail-label">IP地址</div>
            <div class="detail-value">{{ currentFormData.submitRequestIp }}</div>
          </div>
          <div v-if="currentFormData.submitBrowser" class="detail-item">
            <div class="detail-label">浏览器</div>
            <div class="detail-value">{{ currentFormData.submitBrowser }}</div>
          </div>
          <div v-if="currentFormData.startTime" class="detail-item">
            <div class="detail-label">开始填写时间</div>
            <div class="detail-value">{{ formatDate(currentFormData.startTime) }}</div>
          </div>
          <div v-if="currentFormData.completeTime" class="detail-item">
            <div class="detail-label">填写时长</div>
            <div class="detail-value">{{ currentFormData.completeTime }}秒</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useWindowSize } from '@vueuse/core'
import { Search, View, Delete } from '@element-plus/icons-vue'
import { adminApi, formApi } from '@/api'
import SurveyFormRender from '@/components/SurveyFormRender.vue'
import dayjs from 'dayjs'

const { width } = useWindowSize()

const loading = ref(false)
const responseList = ref([])
const surveyIdFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const viewDialogVisible = ref(false)
const detailLoading = ref(false)
const currentFormData = ref(null)
const formItems = ref([])
const themeConfig = reactive({
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  showNumber: false
})

// 响应式分页布局
const paginationLayout = computed(() => {
  if (width.value < 768) {
    // 手机端：简洁布局
    return 'prev, pager, next'
  } else {
    // 电脑端：完整布局
    return 'total, sizes, prev, pager, next, jumper'
  }
})

const loadResponseList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      surveyId: surveyIdFilter.value || undefined
    }
    const res = await adminApi.getResponseList(params)
    if (res.code === 200) {
      responseList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载填写记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadResponseList()
}

const handleSizeChange = () => {
  loadResponseList()
}

const handleCurrentChange = () => {
  loadResponseList()
}

const handleView = async (row) => {
  viewDialogVisible.value = true
  detailLoading.value = true
  currentFormData.value = null
  formItems.value = []
  
  try {
    // 1. 获取表单数据
    const formDataRes = await adminApi.getFormDataByResponseId(row.id)
    if (formDataRes.code !== 200 || !formDataRes.data) {
      ElMessage.error('获取表单数据失败')
      viewDialogVisible.value = false
      return
    }
    
    currentFormData.value = formDataRes.data
    
    // 2. 获取表单项配置
    if (currentFormData.value.formKey) {
      const itemsRes = await formApi.getFormItems(currentFormData.value.formKey)
      if (itemsRes.code === 200 && itemsRes.data) {
        formItems.value = itemsRes.data.map(item => {
          const scheme = typeof item.scheme === 'string' 
            ? JSON.parse(item.scheme) 
            : item.scheme
          return {
            formItemId: item.formItemId,
            label: item.label,
            type: item.type,
            required: item.required === 1,
            placeholder: scheme.placeholder || item.placeholder || '',
            disabled: scheme.disabled || false,
            readonly: scheme.readonly || false,
            hideType: item.isHideType || false,
            defaultValue: scheme.defaultValue !== undefined ? scheme.defaultValue : (item.defaultValue || null),
            config: scheme.config || {},
            regList: item.regList ? (typeof item.regList === 'string' ? JSON.parse(item.regList) : item.regList) : [],
            vModel: scheme.vModel || item.formItemId,
            scheme: scheme || {},
            sort: item.sort != null ? item.sort : 0
          }
        }).sort((a, b) => {
          const sortA = a.sort != null ? a.sort : 0
          const sortB = b.sort != null ? b.sort : 0
          return sortA - sortB
        })
      }
      
      // 3. 获取外观配置（如果有 surveyId）
      if (row.surveyId) {
        try {
          const themeRes = await formApi.getFormTheme(row.surveyId)
          if (themeRes.code === 200 && themeRes.data) {
            const data = themeRes.data
            if (data.themeColor) themeConfig.themeColor = data.themeColor
            if (data.backgroundColor) themeConfig.backgroundColor = data.backgroundColor
            if (data.showNumber !== undefined) themeConfig.showNumber = data.showNumber
          }
        } catch (error) {
          // 使用默认值
        }
      }
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
    viewDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

// 获取表单模型
const getFormModel = (formData) => {
  const formModel = {}
  const rowData = formData.originalData || {}
  
  formItems.value.forEach(item => {
    if (!item.vModel) return
    
    const value = rowData[item.formItemId]
    
    // 根据组件类型处理值
    if (item.type === 'CHECKBOX' || item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
      formModel[item.vModel] = Array.isArray(value) ? value : (value ? [value] : [])
    } else if (item.type === 'IMAGE_SELECT' && item.config?.multiple) {
      formModel[item.vModel] = Array.isArray(value) ? value : (value ? [value] : [])
    } else if (item.type === 'NUMBER' || item.type === 'SLIDER' || item.type === 'RATE') {
      formModel[item.vModel] = value !== null && value !== undefined ? Number(value) : (item.type === 'SLIDER' ? (item.config?.min || 0) : (item.type === 'RATE' ? 0 : undefined))
    } else if (item.type === 'SIGN_PAD') {
      formModel[item.vModel] = value || ''
    } else {
      formModel[item.vModel] = value !== null && value !== undefined ? value : ''
    }
  })
  
  return formModel
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该填写记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await adminApi.deleteResponse(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadResponseList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadResponseList()
})
</script>

<style scoped>
.data-management {
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
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
}

.search-input {
  width: 300px;
  min-width: 150px;
  max-width: 300px;
  flex: 0 0 auto;
}

.search-button {
  margin-left: 0;
  white-space: nowrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  overflow-x: auto;
  width: 100%;
}

.pagination :deep(.el-pagination) {
  flex-wrap: nowrap;
}

/* 响应式设计 - 手机端滚动条 */
@media (max-width: 768px) {
  .data-management {
    padding: 15px;
    max-height: calc(100vh - 60px);
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
    /* 自定义滚动条样式 */
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
  }

  .data-management::-webkit-scrollbar {
    width: 6px;
  }

  .data-management::-webkit-scrollbar-track {
    background: transparent;
  }

  .data-management::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.3);
    border-radius: 3px;
  }

  .data-management::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.5);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .data-management {
    padding: 15px;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 15px;
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-section {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .search-section {
    flex-direction: row;
    gap: 8px;
  }

  .search-input {
    flex: 1;
    min-width: 0;
  }

  .search-button {
    flex-shrink: 0;
    white-space: nowrap;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table .el-button) {
    padding: 5px 8px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .data-management {
    padding: 10px;
  }

  .page-title {
    font-size: 18px;
  }

  :deep(.el-table-column) {
    min-width: 80px !important;
  }

  .pagination {
    overflow-x: auto;
  }
  
  :deep(.el-pagination) {
    flex-wrap: nowrap;
  }
}

.data-detail {
  .detail-form-container {
    margin-bottom: 20px;
    :deep(.survey-form-render) {
      width: 100%;
    }
    
    :deep(.form-item-wrapper) {
      margin-bottom: 20px;
    }
  }
  
  .detail-info {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }
  
  .detail-item {
    display: flex;
    margin-bottom: 12px;
    
    .detail-label {
      width: 120px;
      color: #909399;
      font-size: 14px;
    }
    
    .detail-value {
      flex: 1;
      color: #303133;
      font-size: 14px;
    }
  }
}
</style>

