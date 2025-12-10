<template>
  <div class="template-list-container">
    <div class="create-header-container">
      <!-- 搜索栏 -->
      <div class="filter-container">
        <el-form :inline="true" class="filter-form">
          <div class="search-row">
            <el-form-item label="" class="search-form-item">
            <el-input
              v-model="queryParams.name"
                class="search-input"
              placeholder="请输入模板名称"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
            <el-form-item class="search-button-item">
            <el-button class="search-template-btn" type="primary" @click="handleSearch" :icon="Search">
              查询
            </el-button>
          </el-form-item>
          </div>
          <el-form-item>
            <el-button type="primary" @click="handleAddTemplate" :icon="Plus">
              添加模板
            </el-button>
          </el-form-item>
          <el-form-item>
            <!-- 管理分类下拉框 -->
            <el-dropdown
              trigger="click"
              @command="handleCategoryCommand"
            >
              <el-button type="primary" size="default" :icon="Setting">
                管理分类
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="add">
                    <el-icon><Plus /></el-icon>
                    添加分类
                  </el-dropdown-item>
                  <el-divider />
                  <!-- 分类列表：分类名称 编辑图标 删除图标 -->
                  <el-dropdown-item
                    v-for="category in templateTypeList"
                    :key="category.id"
                    divided
                  >
                    <div class="category-dropdown-item" @click.stop>
                      <span class="category-name">{{ category.name }}</span>
                      <div class="category-actions">
                        <el-icon 
                          class="action-icon edit-icon" 
                          @click="handleEditCategory(category)"
                          :title="'编辑'"
                        >
                          <Edit />
                        </el-icon>
                        <el-icon 
                          class="action-icon delete-icon" 
                          @click="handleDeleteCategory(category)"
                          :title="'删除'"
                        >
                          <Delete />
                        </el-icon>
                      </div>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-form-item>
        </el-form>
      </div>
      <!-- 分类菜单 -->
      <el-menu
        :default-active="String(queryParams.type || 'null')"
        mode="horizontal"
        style="background-color: transparent"
        @select="handleCategorySelect"
      >
        <el-menu-item index="null">全部</el-menu-item>
        <el-menu-item v-for="(item, index) in templateTypeList" :key="index" :index="String(item.id)">
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
            <el-button :icon="View" type="primary" size="small" @click="handleView(template)">
              查看
            </el-button>
            <el-button :icon="Edit" type="warning" size="small" @click="handleEditInfo(template)">
              编辑信息
            </el-button>
            <el-button :icon="Setting" type="success" size="small" @click="handleEditComponents(template)">
              编辑组件
            </el-button>
            <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(template)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格视图 -->
    <div v-else class="table-container">
      <el-table v-loading="loading" :data="templateList" border style="width: 100%">
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="name" label="模板名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类" min-width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
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
        <el-table-column label="操作" min-width="320" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
            <el-button :icon="View" type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button :icon="Edit" type="warning" size="small" @click="handleEditInfo(row)">编辑信息</el-button>
            <el-button :icon="Setting" type="success" size="small" @click="handleEditComponents(row)">编辑组件</el-button>
            <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </div>
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
        :layout="paginationLayout"
        :pager-count="width < 768 ? 5 : 7"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 添加模板对话框 -->
    <el-dialog 
      v-model="addTemplateDialogVisible" 
      title="添加模板" 
      width="600px"
    >
      <el-form :model="addTemplateForm" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="addTemplateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input 
            v-model="addTemplateForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入模板描述" 
          />
        </el-form-item>
        <el-form-item label="模板分类" required>
          <el-select v-model="addTemplateForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="category in templateTypeList" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :icon="Close" @click="addTemplateDialogVisible = false">取消</el-button>
        <el-button :icon="Check" type="primary" @click="handleConfirmAddTemplate">确定</el-button>
      </template>
    </el-dialog>

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
          <div class="template-cover-container">
            <!-- 如果没有图片，显示上传按钮 -->
            <el-upload
              v-if="!editForm.coverImg"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleTemplateCoverUpload"
              accept="image/*"
              :limit="1"
              class="template-cover-upload"
            >
              <el-button :icon="Upload" type="primary">选择图片</el-button>
            </el-upload>
            <!-- 如果有图片，显示图片预览和操作按钮 -->
            <div v-else class="template-cover-preview-wrapper">
              <el-image
                ref="coverImageRef"
                :src="editForm.coverImg"
                class="template-cover-preview"
                fit="cover"
                :preview-src-list="[editForm.coverImg]"
                :initial-index="0"
                preview-teleported
                :hide-on-click-modal="true"
              />
              <div class="template-cover-actions">
                <el-button
                  type="primary"
                  :icon="View"
                  circle
                  size="small"
                  @click.stop="handlePreviewCoverImage"
                />
                <el-button
                  type="danger"
                  :icon="Delete"
                  circle
                  size="small"
                  @click.stop="handleDeleteCoverImage"
                />
              </div>
            </div>
          </div>
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

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form :model="categoryForm" label-width="100px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :icon="Close" @click="categoryDialogVisible = false">取消</el-button>
        <el-button :icon="Check" type="primary" @click="handleSaveCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useWindowSize } from '@vueuse/core'
import { Search, View, Edit, Delete, Setting, Plus, ArrowDown, Close, Check, Upload } from '@element-plus/icons-vue'
import { adminApi, templateApi, surveyApi } from '@/api'
import { getToken } from '@/utils/auth'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const { width } = useWindowSize()

const loading = ref(false)
const viewMode = ref('card') // 'card' 卡片视图, 'table' 表格视图
const queryParams = ref({
  current: 1,
  size: 10,
  name: '',
  type: null
})
const total = ref(0)
const templateTypeList = ref([])
const templateList = ref([])

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

const editDialogVisible = ref(false)
const addTemplateDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const coverImageRef = ref(null)
const editingCategory = ref(null)
const categoryForm = ref({
  id: null,
  name: '',
  sort: 0
})
const editForm = ref({
  id: null,
  formKey: null,
  name: '',
  description: '',
  categoryId: null,
  coverImg: '',
  status: 1
})
const addTemplateForm = ref({
  name: '',
  description: '',
  categoryId: null
})

// 默认封面图（如果没有图片，使用占位图）
const defaultCoverImg = '/images/default-cover.svg'

// 图片上传相关
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  return `${baseUrl}/file/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${getToken()}`
  }
})

// 处理模板封面图上传
const handleTemplateCoverUpload = (response) => {
  if (response && response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    // 转换为完整的后端URL
    const fullImageUrl = getImageUrl(imageUrl)
    editForm.value.coverImg = fullImageUrl
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response?.message || '图片上传失败')
  }
}

// 预览封面图 - 触发 el-image 的原生预览功能
const handlePreviewCoverImage = () => {
  if (coverImageRef.value) {
    // 触发图片的点击事件来打开预览
    const imgElement = coverImageRef.value.$el?.querySelector('img')
    if (imgElement) {
      imgElement.click()
    }
  }
}

// 删除封面图
const handleDeleteCoverImage = () => {
  editForm.value.coverImg = ''
  ElMessage.success('已删除封面图')
}

// 加载模板分类
const loadCategories = async () => {
  try {
    const res = await templateApi.getTemplateTypeList()
    if (res.code === 200) {
      templateTypeList.value = res.data || []
    }
  } catch (error) {
    // 加载模板分类失败
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
  if (index === 'null' || index === null) {
    queryParams.value.type = null
  } else {
    queryParams.value.type = Number(index)
  }
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

// 编辑信息（基本信息：名称、描述、分类、封面图等）
const handleEditInfo = (template) => {
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

// 编辑组件（直接编辑模板）
const handleEditComponents = (template) => {
  // 直接通过formKey跳转到编辑页面，编辑模板本身
  router.push({
    path: '/survey/edit/editor',
    query: { 
      formKey: template.formKey,
      isTemplate: 'true',
      templateId: template.id,
      templateName: template.name,
      templateDescription: template.description || ''
    }
  })
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

// 添加模板（打开对话框）
const handleAddTemplate = () => {
  addTemplateForm.value = {
    name: '',
    description: '',
    categoryId: null
  }
  addTemplateDialogVisible.value = true
}

// 确认添加模板
const handleConfirmAddTemplate = async () => {
  if (!addTemplateForm.value.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!addTemplateForm.value.categoryId) {
    ElMessage.warning('请选择模板分类')
    return
  }

  try {
    // 创建问卷
    const res = await surveyApi.createSurvey({
      title: addTemplateForm.value.name,
      description: addTemplateForm.value.description || ''
    })

    if (res.code === 200 && res.data) {
      ElMessage.success('创建成功，正在跳转...')
      addTemplateDialogVisible.value = false
      // 跳转到编辑页，并传递问卷ID和模板信息
      router.push({
        path: '/survey/edit/editor',
        query: { 
          id: res.data.id,
          isTemplate: 'true',
          templateName: addTemplateForm.value.name,
          templateDescription: addTemplateForm.value.description,
          categoryId: addTemplateForm.value.categoryId
        }
      })
    } else {
      ElMessage.error('创建问卷失败')
    }
  } catch (error) {
    ElMessage.error('创建问卷失败')
  }
}

// 处理分类管理下拉框命令
const handleCategoryCommand = (command) => {
  if (command === 'add') {
    handleAddCategory()
  }
}

// 添加分类
const handleAddCategory = () => {
  editingCategory.value = null
  categoryForm.value = {
    id: null,
    name: '',
    sort: 0
  }
  categoryDialogVisible.value = true
}

// 编辑分类
const handleEditCategory = (category) => {
  editingCategory.value = category
  categoryForm.value = {
    id: category.id,
    name: category.name,
    sort: category.sort || 0
  }
  categoryDialogVisible.value = true
}

// 保存分类
const handleSaveCategory = async () => {
  if (!categoryForm.value.name || !categoryForm.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    let res
    if (editingCategory.value) {
      res = await templateApi.updateCategory(categoryForm.value)
    } else {
      res = await templateApi.createCategory(categoryForm.value)
    }

    if (res.code === 200) {
      ElMessage.success(editingCategory.value ? '更新成功' : '创建成功')
      categoryDialogVisible.value = false
      // 重新加载分类列表
      await loadCategories()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除分类
const handleDeleteCategory = async (category) => {
  try {
    // 提示信息：说明会删除分类下的模板
    const message = `此操作将永久删除分类"${category.name}"及其下的所有模板和相关数据，是否继续？`
    
    await ElMessageBox.confirm(message, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: false
    })

    const res = await templateApi.deleteCategory({ id: category.id })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载分类列表
      await loadCategories()
      // 如果当前选中的分类被删除，重置为全部
      if (queryParams.value.type === category.id) {
        queryParams.value.type = null
        loadTemplateList()
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadCategories()
  loadTemplateList()
})
</script>

<style lang="scss" scoped>
.template-list-container {
  margin: 20px;
  padding: 24px;
  width: calc(100% - 40px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .el-pagination {
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .template-list-container {
    margin: 10px;
    padding: 15px;
    width: calc(100% - 20px);
    max-height: calc(100vh - 60px);
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
    /* 自定义滚动条样式 */
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
  }

  .template-list-container::-webkit-scrollbar {
    width: 6px;
  }

  .template-list-container::-webkit-scrollbar-track {
    background: transparent;
  }

  .template-list-container::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.3);
    border-radius: 3px;
  }

  .template-list-container::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.5);
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
}

.filter-form {
  width: 100%;
}

.search-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.search-form-item {
  flex: 0 0 auto;
  width: 300px;
  max-width: 300px;
  min-width: 200px;
  margin-right: 0 !important;
}

.search-button-item {
  margin-right: 0 !important;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  max-width: 300px;
}

.view-toggle-item {
  margin-left: auto;
}

.view-toggle-group {
  display: flex;
  justify-content: center;
}

.project-grid-container {
  margin-top: 20px;
}

.project-grid-view {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  width: 100%;
  max-width: 100%;
}

@media (max-width: 1600px) {
  .project-grid-view {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1200px) {
  .project-grid-view {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .project-grid-view {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .project-grid-view {
    grid-template-columns: repeat(1, 1fr);
  }
}

.preview-img {
  width: 95%;
  height: 200px;
  margin-top: 8px;
  border-radius: 10px;
  object-fit: cover;
}

.project-template-view {
  width: 100%;
  min-height: 320px;
  line-height: 20px;
  border-radius: 10px;
  text-align: center;
  margin: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15), 0 0 8px rgba(0, 0, 0, 0.08);
  background: white;
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
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
  align-items: center;
  padding: 0 8px;
  width: 100%;
  box-sizing: border-box;
}

.template-actions .el-button {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  font-size: 12px;
  padding: 5px 10px;
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
  display: flex;
  justify-content: center;
  align-items: center;
  overflow-x: auto;
  width: 100%;
}

.pagination-container :deep(.el-pagination) {
  flex-wrap: nowrap;
  justify-content: center;
}

.el-menu.el-menu--horizontal {
  border-bottom: none;
}

.text-center {
  text-align: center;
}

.category-dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0;
}

.category-name {
  flex: 1;
}

.category-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-left: 10px;
}

.action-icon {
  cursor: pointer;
  font-size: 16px;
  transition: color 0.3s;
}

.edit-icon {
  color: #409eff;
}

.edit-icon:hover {
  color: #66b1ff;
}

.delete-icon {
  color: #f56c6c;
}

.delete-icon:hover {
  color: #f78989;
}

.template-cover-container {
  width: 100%;
}

.template-cover-preview-wrapper {
  position: relative;
  display: inline-block;
  width: 200px;
  height: 120px;
}

.template-cover-preview {
  width: 200px;
  height: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  object-fit: cover;
}

.template-cover-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.template-cover-preview-wrapper:hover .template-cover-actions {
  opacity: 1;
}

.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: nowrap;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .template-list-container {
    padding: 15px;
  }

  .filter-container {
    flex-direction: column;
    gap: 10px;
  }

  .filter-form {
    width: 100%;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }

  .search-row {
    width: 100%;
    display: flex;
    flex-direction: row;
    gap: 8px;
    align-items: center;
    flex-basis: 100%;
  }

  .search-form-item {
    flex: 1;
    min-width: 0;
    margin-right: 0 !important;
    margin-bottom: 0 !important;
  }

  .search-button-item {
    flex-shrink: 0;
    margin-right: 0 !important;
    margin-bottom: 0 !important;
  }

  /* 添加模板和管理分类按钮在同一行 */
  .filter-form > .el-form-item:not(.search-form-item):not(.search-button-item) {
    display: inline-flex;
    margin-right: 10px;
    margin-bottom: 0 !important;
    vertical-align: middle;
    width: auto;
    flex: 0 0 auto;
  }

  .filter-form > .el-form-item:not(.search-form-item):not(.search-button-item):last-of-type {
    margin-right: 0;
  }

  .view-toggle-item {
    width: 100%;
    margin-left: 0;
  }

  .view-toggle-group {
    width: 100%;
    justify-content: center;
  }

  .table-container {
    padding: 15px;
    overflow-x: auto;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table .el-button) {
    padding: 5px 8px;
    font-size: 12px;
    margin: 2px;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }

  /* 卡片按钮在同一行显示 */
  .template-actions {
    flex-direction: row;
    flex-wrap: nowrap;
    gap: 4px;
  }

  .template-actions .el-button {
    flex: 1;
    min-width: 0;
    width: auto;
    padding: 5px 8px;
    font-size: 11px;
  }

  /* 卡片封面图左右间距 */
  .preview-img {
    margin-left: 8px;
    margin-right: 8px;
    width: calc(100% - 16px);
  }
}

@media (max-width: 480px) {
  .template-list-container {
    padding: 10px;
  }

  .table-container {
    padding: 10px;
  }

  :deep(.el-table-column) {
    min-width: 80px !important;
  }
}
</style>
