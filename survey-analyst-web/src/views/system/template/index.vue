<template>
  <div class="template-list-container">
    <div class="create-header-container">
      <!-- 搜索栏 -->
      <div class="filter-container">
        <el-form :inline="true">
          <el-form-item label="">
            <el-input
              v-model="queryParams.name"
              class="width80"
              placeholder="请输入模板名称"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button class="search-template-btn" type="primary" @click="handleSearch" :icon="Search">
              查询
            </el-button>
          </el-form-item>
          <el-form-item>
            <!-- 视图切换按钮 -->
            <el-radio-group v-model="viewMode" size="default" style="margin-left: 10px">
              <el-radio-button :label="'card'">
                <el-icon><Grid /></el-icon>
                <span style="margin-left: 4px">卡片</span>
              </el-radio-button>
              <el-radio-button :label="'table'">
                <el-icon><List /></el-icon>
                <span style="margin-left: 4px">表格</span>
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <!-- 分类菜单 -->
      <el-menu
        :default-active="queryParams.type"
        mode="horizontal"
        style="background-color: transparent"
        @select="handleCategorySelect"
      >
        <el-menu-item :index="null">全部</el-menu-item>
        <el-menu-item v-for="(item, index) in templateTypeList" :key="index" :index="item.id.toString()">
          {{ item.name }}
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="project-grid-container">
      <div class="project-grid-view">
        <div
          v-for="template in templateList"
          :key="template.id"
          class="project-template-view"
          v-loading="loading"
        >
          <img
            :src="template.coverImg ? template.coverImg : defaultCoverImg"
            class="preview-img"
            @error="handleImageError"
          />
          <p class="project-template-title">
            {{ template.name }}
          </p>
          <div class="template-actions">
            <el-button icon="View" type="text" @click="handleView(template)">
              查看
            </el-button>
            <el-button icon="Edit" type="text" @click="handleEdit(template)">
              编辑
            </el-button>
            <el-button icon="Delete" type="text" @click="handleDelete(template)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格视图 -->
    <div v-else class="table-container">
      <el-table v-loading="loading" :data="templateList" border style="width: 100%">
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
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && templateList.length === 0" description="暂无模板" />

    <!-- 分页 -->
    <div class="text-center pagination-container">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[12, 24, 48, 96]"
        background
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 编辑对话框 -->
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
              v-for="category in templateTypeList" 
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
import { Search, View, Edit, Delete, Grid, List } from '@element-plus/icons-vue'
import { adminApi, templateApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const viewMode = ref('card') // 'card' 卡片视图, 'table' 表格视图
const queryParams = ref({
  current: 1,
  size: 12,
  name: '',
  type: null
})
const total = ref(0)
const templateTypeList = ref([])
const templateList = ref([])

const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  formKey: null,
  name: '',
  description: '',
  categoryId: null,
  coverImg: '',
  status: 1
})

// 默认封面图（如果没有图片，使用占位图）
const defaultCoverImg = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEzMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEzMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjYzBjNGNjIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+6L+Z5Liq5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='

// 加载模板分类
const loadCategories = async () => {
  try {
    const res = await templateApi.getTemplateTypeList()
    if (res.code === 200) {
      templateTypeList.value = res.data || []
    }
  } catch (error) {
    console.error('加载模板分类失败', error)
  }
}

// 加载模板列表
const loadTemplateList = async () => {
  loading.value = true
  try {
    const params = {
      current: queryParams.value.current,
      size: queryParams.value.size,
      name: queryParams.value.name || undefined,
      categoryId: queryParams.value.type || undefined
    }
    const res = await adminApi.getTemplateList(params)
    if (res.code === 200) {
      const { records, total: totalCount, size } = res.data
      templateList.value = records || []
      total.value = totalCount || 0
      queryParams.value.size = size || 12
    }
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.value.current = 1
  loadTemplateList()
}

// 分类选择
const handleCategorySelect = (index) => {
  queryParams.value.type = index === 'null' ? null : Number(index)
  queryParams.value.current = 1
  loadTemplateList()
}

// 查看
const handleView = (template) => {
  router.push({
    path: '/survey/template/preview',
    query: { key: template.formKey }
  })
}

// 编辑
const handleEdit = (template) => {
  editForm.value = {
    id: template.id,
    formKey: template.formKey,
    name: template.name,
    description: template.description || '',
    categoryId: template.categoryId,
    coverImg: template.coverImg || '',
    status: template.status
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
const handleDelete = async (template) => {
  try {
    await ElMessageBox.confirm(`此操作将永久删除模板"${template.name}"，是否继续?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await adminApi.deleteTemplate(template.formKey)
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

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.src = defaultCoverImg
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = templateTypeList.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.value.size = size
  queryParams.value.current = 1
  loadTemplateList()
}

// 页码改变
const handlePageChange = (page) => {
  queryParams.value.current = page
  loadTemplateList()
}

onMounted(() => {
  loadCategories()
  loadTemplateList()
})
</script>

<style lang="scss" scoped>
.template-list-container {
  margin: 0 auto;
  padding: 50px 150px;
  width: 100%;

  .el-pagination {
    margin-top: 20px;
  }
}

.create-header-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.filter-container {
  display: flex;
  justify-content: flex-start;
  margin-top: 20px !important;

  .el-input {
    display: inline-block;
    width: 300px !important;
  }
}

.project-grid-container {
  margin-top: 20px;
}

.project-grid-view {
  display: flex;
  max-width: 1400px;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.preview-img {
  width: 95%;
  height: 180px;
  margin-top: 8px;
  border-radius: 10px;
  object-fit: cover;
}

.project-template-view {
  width: 220px;
  height: 280px;
  line-height: 20px;
  border-radius: 10px;
  text-align: center;
  margin: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15), 0 0 8px rgba(0, 0, 0, 0.08);
  background: white;
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
}

.project-template-view:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), 0 0 12px rgba(0, 0, 0, 0.12);
}

.project-template-title {
  color: rgba(16, 16, 16, 100);
  font-size: 15px;
  font-weight: 500;
  margin: 10px 8px 8px 8px;
  line-height: 22px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-height: 22px;
}

.template-actions {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.table-container {
  margin-top: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 30px;
  margin-bottom: 20px;
}

.el-menu.el-menu--horizontal {
  border-bottom: none;
}

.text-center {
  text-align: center;
}
</style>
