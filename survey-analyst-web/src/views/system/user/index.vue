<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-section">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索账号/邮箱/用户名"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            />
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable class="status-select">
              <el-option label="全部" value="" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            <el-button :icon="Search" type="primary" @click="handleSearch" class="search-button">查询</el-button>
            <el-button :icon="Plus" type="primary" @click="handleAdd" class="add-button">添加用户</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="userList" border style="width: 100%">
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="account" label="账号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="role" label="角色" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
            <el-button :icon="Edit" type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :icon="row.status === 1 ? Lock : Unlock"
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </div>
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

    <!-- 添加对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加用户" :width="dialogWidth" @close="handleAddDialogClose">
      <el-form :model="addForm" :rules="addFormRules" ref="addFormRef" label-width="100px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="addForm.account" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addForm.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" style="width: 100%">
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="addForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :icon="Close" @click="addDialogVisible = false">取消</el-button>
        <el-button :icon="Check" type="primary" @click="handleAddSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" :width="dialogWidth">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="账号">
          <el-input v-model="editForm.account" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :icon="Close" @click="editDialogVisible = false">取消</el-button>
        <el-button :icon="Check" type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Lock, Unlock, Delete, Close, Check } from '@element-plus/icons-vue'
import { adminApi } from '@/api'
import { useWindowSize } from '@vueuse/core'

const { width } = useWindowSize()

const dialogWidth = computed(() => {
  if (width.value < 768) {
    return '90%'
  } else {
    return '500px'
  }
})

const loading = ref(false)
const userList = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
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

const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addForm = reactive({
  account: '',
  email: '',
  username: '',
  password: '',
  phone: '',
  role: 'USER',
  status: 1
})

const addFormRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const editDialogVisible = ref(false)
const editForm = reactive({
  id: null,
  account: '',
  email: '',
  username: '',
  phone: '',
  role: 'USER',
  status: 1
})

const loadUserList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value !== '' ? statusFilter.value : undefined
    }
    const res = await adminApi.getUserList(params)
    if (res.code === 200) {
      userList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUserList()
}

const handleSizeChange = () => {
  loadUserList()
}

const handleCurrentChange = () => {
  loadUserList()
}

const handleAdd = () => {
  addDialogVisible.value = true
}

const handleAddDialogClose = () => {
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  }
  Object.assign(addForm, {
    account: '',
    email: '',
    username: '',
    password: '',
    phone: '',
    role: 'USER',
    status: 1
  })
}

const handleAddSave = async () => {
  if (!addFormRef.value) {
    return
  }

  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await adminApi.createUser(addForm)
        if (res.code === 200) {
          ElMessage.success('创建成功')
          addDialogVisible.value = false
          handleAddDialogClose()
          loadUserList()
        }
      } catch (error) {
        ElMessage.error(error.message || '创建失败')
      }
    }
  })
}

const handleEdit = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

const handleSave = async () => {
  try {
    const res = await adminApi.updateUser(editForm.id, editForm)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      loadUserList()
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const res = await adminApi.updateUserStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(newStatus === 1 ? '启用成功' : '禁用成功')
      loadUserList()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await adminApi.deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUserList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-management {
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
  flex-wrap: wrap;
  gap: 10px;
}

.search-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.search-input {
  width: 300px;
  min-width: 150px;
  max-width: 300px;
  flex: 0 0 auto;
}

.status-select {
  width: 150px;
  min-width: 120px;
}

.search-button {
  margin-left: 0;
}

.add-button {
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

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: nowrap;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management {
    padding: 15px;
    max-height: calc(100vh - 60px);
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
    /* 自定义滚动条样式 */
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
  }

  .user-management::-webkit-scrollbar {
    width: 6px;
  }

  .user-management::-webkit-scrollbar-track {
    background: transparent;
  }

  .user-management::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.3);
    border-radius: 3px;
  }

  .user-management::-webkit-scrollbar-thumb:hover {
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

  .status-select {
    width: 100%;
    flex: 1;
  }

  .search-button {
    width: 100%;
  }

  .add-button {
    width: 100%;
    margin-top: 10px;
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
  .user-management {
    padding: 10px;
  }

  .page-title {
    font-size: 18px;
  }

  :deep(.el-table-column) {
    min-width: 80px !important;
  }

  :deep(.el-pagination) {
    flex-wrap: nowrap;
    overflow-x: auto;
  }
  
  :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
  
  :deep(.el-table .el-button) {
    margin: 2px;
  }
}
</style>

