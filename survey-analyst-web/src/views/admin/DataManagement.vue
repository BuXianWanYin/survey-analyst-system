<template>
  <div class="data-management">
    <h2 class="page-title">数据管理</h2>

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-section">
            <el-input
              v-model="surveyIdFilter"
              placeholder="问卷ID筛选"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">查询</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="responseList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="surveyId" label="问卷ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'info'">
              {{ row.status === 'COMPLETED' ? '已完成' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceType" label="设备类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.deviceType || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="填写时长" width="100">
          <template #default="{ row }">
            {{ row.duration ? row.duration + '秒' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const loading = ref(false)
const responseList = ref([])
const surveyIdFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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

const handleView = (row) => {
  ElMessage.info('查看详情功能开发中')
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
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

