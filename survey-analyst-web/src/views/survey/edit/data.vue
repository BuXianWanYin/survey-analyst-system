<template>
  <div class="data-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据管理</span>
          <div class="header-actions">
            <el-button :icon="Download" type="primary" @click="handleExport">导出数据</el-button>
            <el-button :icon="Refresh" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="dataList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="serialNumber" label="序号" width="80" />
        <el-table-column label="填写内容" min-width="300">
          <template #default="{ row }">
            <div class="data-content">
              <div
                v-for="(value, key) in (row.data || row.originalData || {})"
                :key="key"
                class="data-item"
              >
                <span class="data-label">{{ getFieldLabel(key) }}：</span>
                <span class="data-value">{{ formatDataValue(value) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button :icon="View" type="text" @click="handleView(row)">查看</el-button>
            <el-button :icon="Delete" type="text" danger @click="handleDelete(row)">删除</el-button>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
    
    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="数据详情"
      width="600px"
    >
      <div v-if="currentData" class="data-detail">
        <div
          v-for="(value, key) in (currentData.data || currentData.originalData || {})"
          :key="key"
          class="detail-item"
        >
          <div class="detail-label">{{ getFieldLabel(key) }}</div>
          <div class="detail-value">{{ formatDataValue(value) }}</div>
        </div>
        <div class="detail-item">
          <div class="detail-label">提交时间</div>
          <div class="detail-value">{{ formatDate(currentData.createTime) }}</div>
        </div>
        <div v-if="currentData.submitRequestIp" class="detail-item">
          <div class="detail-label">IP地址</div>
          <div class="detail-value">{{ currentData.submitRequestIp }}</div>
        </div>
        <div v-if="currentData.submitOs" class="detail-item">
          <div class="detail-label">操作系统</div>
          <div class="detail-value">{{ currentData.submitOs }}</div>
        </div>
        <div v-if="currentData.submitBrowser" class="detail-item">
          <div class="detail-label">浏览器</div>
          <div class="detail-value">{{ currentData.submitBrowser }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Refresh, View, Delete } from '@element-plus/icons-vue'
import { responseApi, formApi } from '@/api'
import dayjs from 'dayjs'

const route = useRoute()

const surveyId = ref(null)
const formKey = ref(null)
const loading = ref(false)
const dataList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const viewDialogVisible = ref(false)
const currentData = ref(null)
const formItems = ref([])

// 加载表单配置和表单项
const loadFormConfig = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)

  try {
    // 加载表单配置获取 formKey
    const configRes = await formApi.getFormConfig(surveyId.value)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      
      // 加载表单项
      if (formKey.value) {
        const itemsRes = await formApi.getFormItems(formKey.value)
        if (itemsRes.code === 200 && itemsRes.data) {
          formItems.value = itemsRes.data.map(item => {
            const scheme = typeof item.scheme === 'string' 
              ? JSON.parse(item.scheme) 
              : item.scheme
            return {
              formItemId: item.formItemId,
              label: item.label,
              type: item.type
            }
          })
        }
      }
    }
    
    await loadData()
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

// 加载数据
const loadData = async () => {
  if (!formKey.value) return

  loading.value = true
  try {
    const res = await formApi.getFormDataList(formKey.value, {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (res.code === 200 && res.data) {
      // 将 originalData 转换为 data 字段
      dataList.value = (res.data.records || []).map(item => ({
        ...item,
        data: item.originalData || {},
        createTime: item.createTime
      }))
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 获取字段标签
const getFieldLabel = (formItemId) => {
  const item = formItems.value.find(i => i.formItemId === formItemId)
  return item ? item.label : formItemId
}

// 格式化数据值
const formatDataValue = (value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  if (typeof value === 'object') {
    if (Array.isArray(value)) {
      return value.join(', ')
    }
    return JSON.stringify(value)
  }
  return String(value)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 查看详情
const handleView = (row) => {
  currentData.value = row
  viewDialogVisible.value = true
}

// 删除数据
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await formApi.deleteFormData(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 导出数据
const handleExport = async () => {
  if (!surveyId.value) {
    ElMessage.warning('请先选择问卷')
    return
  }
  
  try {
    const { exportApi } = await import('@/api/export')
    await exportApi.exportSurveyData(surveyId.value)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 刷新
const handleRefresh = () => {
  loadData()
}

onMounted(() => {
  loadFormConfig()
})
</script>

<style lang="scss" scoped>
.data-container {
  padding: 20px;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.data-container :deep(.el-card) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.data-container :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  min-height: 0;
  /* 自定义滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
}

.data-container :deep(.el-card__body)::-webkit-scrollbar {
  width: 6px;
}

.data-container :deep(.el-card__body)::-webkit-scrollbar-track {
  background: transparent;
}

.data-container :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 3px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.data-content {
  .data-item {
    margin-bottom: 5px;
    
    .data-label {
      font-weight: 500;
      color: #606266;
      margin-right: 5px;
    }
    
    .data-value {
      color: #303133;
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.data-detail {
  .detail-item {
    margin-bottom: 20px;
    
    .detail-label {
      font-weight: 500;
      color: #606266;
      margin-bottom: 5px;
    }
    
    .detail-value {
      color: #303133;
      padding: 10px;
      background: #f5f7fa;
      border-radius: 4px;
      word-break: break-all;
    }
  }
}
</style>
