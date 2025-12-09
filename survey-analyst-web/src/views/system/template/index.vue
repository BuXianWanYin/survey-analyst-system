<template>
  <div class="template-management">
    <h2 class="page-title">公共模板管理</h2>

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="search-section">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索模板名称"
              clearable
              style="width: 300px"
              @keyup.enter="handleSearch"
            />
            <el-select 
              v-model="categoryFilter" 
              placeholder="分类筛选" 
              clearable 
              style="width: 150px; margin-left: 10px"
            >
              <el-option label="全部" value="" />
              <el-option 
                v-for="category in categoryList" 
                :key="category.id" 
                :label="category.name" 
                :value="category.id" 
              />
            </el-select>
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">查询</el-button>
            <el-button type="success" @click="handleAdd" style="margin-left: 10px">新增模板</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="templateList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 编辑/新增对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      :title="editForm.id ? '编辑模板' : '新增模板'" 
      width="600px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="editForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input 
            v-model="editForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入模板描述" 
          />
        </el-form-item>
        <el-form-item label="模板分类" required>
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="category in categoryList" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="editForm.coverImg" placeholder="请输入封面图URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, templateApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const searchKeyword = ref('')
const categoryFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const templateList = ref([])
const categoryList = ref([])

const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  name: '',
  description: '',
  categoryId: null,
  coverImg: '',
  status: 1
})

// 加载模板分类
const loadCategories = async () => {
  try {
    const res = await templateApi.getTemplateTypeList()
    if (res.code === 200) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categoryList.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

// 加载模板列表
const loadTemplateList = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      name: searchKeyword.value || undefined,
      categoryId: categoryFilter.value || undefined
    }
    const res = await adminApi.getTemplateList(params)
    if (res.code === 200) {
      const { records, total: totalCount } = res.data
      templateList.value = records || []
      total.value = totalCount || 0
    }
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadTemplateList()
}

// 新增
const handleAdd = () => {
  editForm.value = {
    id: null,
    name: '',
    description: '',
    categoryId: null,
    coverImg: '',
    status: 1
  }
  editDialogVisible.value = true
}

// 查看
const handleView = (row) => {
  router.push({
    path: '/survey/template/preview',
    query: { key: row.formKey }
  })
}

// 编辑
const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    formKey: row.formKey,
    name: row.name,
    description: row.description || '',
    categoryId: row.categoryId,
    coverImg: row.coverImg || '',
    status: row.status
  }
  editDialogVisible.value = true
}

// 保存
const handleSave = async () => {
  if (!editForm.value.name) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!editForm.value.categoryId) {
    ElMessage.warning('请选择模板分类')
    return
  }

  try {
    let res
    if (editForm.value.id) {
      // 更新
      res = await adminApi.updateTemplate(editForm.value)
    } else {
      // 新增（需要先创建表单，然后保存为模板）
      ElMessage.warning('新增模板功能需要先创建表单，请使用"保存为模板"功能')
      return
    }
    
    if (res.code === 200) {
      ElMessage.success(editForm.value.id ? '更新成功' : '创建成功')
      editDialogVisible.value = false
      loadTemplateList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`此操作将永久删除模板"${row.name}"，是否继续?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await adminApi.deleteTemplate(row.formKey)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadTemplateList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 分页
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadTemplateList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadTemplateList()
}

onMounted(() => {
  loadCategories()
  loadTemplateList()
})
</script>

<style lang="scss" scoped>
.template-management {
  padding: 20px;

  .page-title {
    margin-bottom: 20px;
    font-size: 20px;
    font-weight: 500;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .search-section {
      display: flex;
      align-items: center;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>

