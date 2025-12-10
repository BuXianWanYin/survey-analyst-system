<template>
  <div class="log-management">

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-section">
            <el-input
              v-model="searchUserId"
              placeholder="用户ID"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="searchOperationType"
              placeholder="操作类型"
              clearable
              class="operation-select"
            >
              <el-option label="全部" value="" />
              <el-option label="查询" value="查询" />
              <el-option label="新增" value="新增" />
              <el-option label="修改" value="修改" />
              <el-option label="删除" value="删除" />
              <el-option label="登录" value="登录" />
              <el-option label="登出" value="登出" />
              <el-option label="导出" value="导出" />
              <el-option label="导入" value="导入" />
            </el-select>
            <el-button :icon="Search" type="primary" @click="handleSearch" class="search-button">查询</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="logList" border style="width: 100%">
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="operationType" label="操作类型" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)">
              {{ row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationDesc" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.requestMethod)" size="small">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" min-width="130" />
        <el-table-column prop="createTime" label="操作时间" min-width="180" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useWindowSize } from '@vueuse/core'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const { width } = useWindowSize()

const loading = ref(false)
const logList = ref([])
const searchUserId = ref('')
const searchOperationType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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

const loadLogList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchUserId.value) {
      params.userId = parseInt(searchUserId.value)
    }
    if (searchOperationType.value) {
      params.operationType = searchOperationType.value
    }
    const res = await adminApi.getSystemLogs(params)
    if (res.code === 200) {
      logList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadLogList()
}

const handleSizeChange = () => {
  loadLogList()
}

const handleCurrentChange = () => {
  loadLogList()
}

// 根据操作类型返回对应的标签类型（颜色）
const getOperationTypeTagType = (operationType) => {
  const typeMap = {
    '查询': 'info',
    '新增': 'success',
    '修改': 'warning',
    '删除': 'danger',
    '登录': 'success',
    '登出': 'info',
    '导出': 'primary',
    '导入': 'primary'
  }
  return typeMap[operationType] || ''
}

// 根据请求方法返回对应的标签类型（颜色）
const getMethodTagType = (method) => {
  const methodMap = {
    'GET': 'info',
    'POST': 'success',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'warning'
  }
  return methodMap[method] || ''
}

onMounted(() => {
  loadLogList()
})
</script>

<style scoped>
.log-management {
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
  width: 150px;
  min-width: 120px;
  flex: 0 0 auto;
}

.operation-select {
  width: 200px;
  min-width: 150px;
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
  .log-management {
    padding: 15px;
    max-height: calc(100vh - 60px);
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
    /* 自定义滚动条样式 */
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
  }

  .log-management::-webkit-scrollbar {
    width: 6px;
  }

  .log-management::-webkit-scrollbar-track {
    background: transparent;
  }

  .log-management::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.3);
    border-radius: 3px;
  }

  .log-management::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.5);
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
    flex: 1;
  }

  .operation-select {
    width: 100%;
    flex: 1;
  }

  .search-button {
    width: 100%;
  }

  :deep(.el-table) {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .log-management {
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
</style>

