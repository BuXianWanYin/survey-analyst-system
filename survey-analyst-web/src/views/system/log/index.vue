<template>
  <div class="log-management">
    <h2 class="page-title">系统日志</h2>

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-section">
            <el-input
              v-model="searchUserId"
              placeholder="用户ID"
              clearable
              style="width: 150px"
              @keyup.enter="handleSearch"
            />
            <el-input
              v-model="searchOperationType"
              placeholder="操作类型"
              clearable
              style="width: 200px; margin-left: 10px"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">查询</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="logList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="120" />
        <el-table-column prop="operationDesc" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" width="100" />
        <el-table-column prop="requestUrl" label="请求URL" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
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
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const loading = ref(false)
const logList = ref([])
const searchUserId = ref('')
const searchOperationType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

