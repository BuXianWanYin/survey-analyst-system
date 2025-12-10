<template>
  <div class="template-list-container">
    <div class="create-header-container">
      <!-- 标签切换：我的模板 / 公共模板 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="template-tabs">
        <el-tab-pane label="我的模板" name="my">
          <template #label>
            <span>我的模板</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="公共模板" name="public">
          <template #label>
            <span>公共模板</span>
          </template>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 搜索栏 -->
      <div class="filter-container">
        <el-form :inline="true" class="filter-form">
          <div class="search-row">
            <el-form-item label="" class="search-form-item">
            <el-input
              v-model="queryParams.name"
                class="search-input"
              placeholder="请输入模板名称"
              @keyup.enter="queryTemplatePage"
            />
          </el-form-item>
            <el-form-item class="search-button-item">
            <el-button class="search-template-btn" type="primary" @click="queryTemplatePage" :icon="Search">
              查询
            </el-button>
            </el-form-item>
          </div>
          <el-form-item>
            <!-- 我的模板显示管理分类下拉框 -->
            <el-dropdown
              v-if="activeTab === 'my'"
              trigger="click"
              @command="handleCategoryCommand"
              style="margin-left: 10px"
            >
              <el-button type="success" size="default" :icon="Setting">
                管理分类
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="add">
                    <el-icon><Plus /></el-icon>
              添加分类
                  </el-dropdown-item>
                  <el-divider />
                  <!-- 分类列表：分类名称 编辑图标 删除图标 -->
                  <template v-for="category in templateTypeList" :key="category.id">
                    <el-dropdown-item
                      v-if="category.userId === currentUserId || (isAdmin && !category.userId)"
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
                  </template>
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
        <!-- 我的模板只显示用户自己的分类，公共模板显示所有分类（包括系统分类） -->
        <el-menu-item 
          v-for="(item, index) in filteredCategoryList" 
          :key="index" 
          :index="item.id.toString()"
        >
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
            <el-button :icon="View" type="primary" size="small" @click="toTemplatePreview(template.formKey)">
              查看
            </el-button>
            <!-- 只有我的模板才显示编辑和删除按钮 -->
            <template v-if="activeTab === 'my' && template.isPublic !== 1">
              <el-button :icon="Edit" type="warning" size="small" @click="handleEditInfo(template)">
                编辑信息
              </el-button>
              <el-button :icon="Setting" type="success" size="small" @click="handleEditComponents(template)">
                编辑组件
              </el-button>
              <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(template)">
              删除
            </el-button>
            </template>
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
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" :width="activeTab === 'my' ? 320 : 100" fixed="right">
          <template #default="{ row }">
            <el-button :icon="View" type="primary" size="small" @click="toTemplatePreview(row.formKey)">查看</el-button>
            <!-- 只有我的模板才显示编辑和删除按钮 -->
            <template v-if="activeTab === 'my' && row.isPublic !== 1">
              <el-button :icon="Edit" type="warning" size="small" @click="handleEditInfo(row)">编辑信息</el-button>
              <el-button :icon="Setting" type="success" size="small" @click="handleEditComponents(row)">编辑组件</el-button>
              <el-button :icon="Delete" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && templateList.length === 0" description="暂无模板" />

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="400px"
    >
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input
            v-model="categoryForm.name"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :icon="Close" @click="categoryDialogVisible = false">取消</el-button>
        <el-button :icon="Check" type="primary" @click="handleSaveCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分页 -->
    <div class="text-center pagination-container">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        background
        :layout="paginationLayout"
        :pager-count="width < 768 ? 5 : 7"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 编辑模板信息对话框（仅在我的模板显示） -->
    <el-dialog
      v-if="activeTab === 'my'"
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
              v-for="category in filteredCategoryList"
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
              <el-button type="primary">选择图片</el-button>
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
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTemplate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Plus, Edit, ArrowDown, Grid, List, View, Setting, Close, Check } from '@element-plus/icons-vue'
import { useWindowSize } from '@vueuse/core'
import { templateApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import { getImageUrl } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()
const { width } = useWindowSize()

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo?.role === 'ADMIN'
})

// 获取当前用户ID
const currentUserId = computed(() => {
  return userStore.userInfo?.id
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

// 过滤后的分类列表：我的模板只显示用户自己的分类，公共模板显示所有分类
const filteredCategoryList = computed(() => {
  if (activeTab.value === 'my') {
    // 我的模板：只显示用户自己的分类
    return templateTypeList.value.filter(category => category.userId === currentUserId.value)
  } else {
    // 公共模板：显示所有分类（包括系统分类）
    return templateTypeList.value
  }
})

const loading = ref(false)
const activeTab = ref('my') // 'my' 我的模板, 'public' 公共模板
const viewMode = ref('card') // 'card' 卡片视图, 'table' 表格视图（仅我的模板）
const queryParams = ref({
  current: 1,
  size: 10,
  name: '',
  type: null,
  isPublic: 0 // 0-我的模板, 1-公共模板
})
const total = ref(0)
const templateTypeList = ref([])
const templateList = ref([])

// 编辑模板信息
const editDialogVisible = ref(false)
const coverImageRef = ref(null)
const editForm = ref({
  id: null,
  formKey: null,
  name: '',
  description: '',
  categoryId: null,
  coverImg: '',
  status: 1
})

// 分类管理
const categoryDialogVisible = ref(false)
const editingCategory = ref(null)
const categoryForm = ref({
  id: null,
  name: '',
  sort: 0
})

// 默认封面图（如果没有图片，使用占位图）
const defaultCoverImg = '/images/default-cover.svg'

// 加载模板分类
const queryTemplateType = async () => {
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
const queryTemplatePage = async () => {
  loading.value = true
  try {
    const res = await templateApi.getTemplatePage(queryParams.value)
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

// 标签切换
const handleTabChange = (tabName) => {
  queryParams.value.isPublic = tabName === 'public' ? 1 : 0
  queryParams.value.current = 1
  queryParams.value.type = null
  queryTemplatePage()
}

// 分类选择
const handleCategorySelect = (index) => {
  queryParams.value.type = index === 'null' ? null : Number(index)
  queryParams.value.current = 1
  queryTemplatePage()
}

// 查看模板预览
const toTemplatePreview = (formKey) => {
  router.push({
    path: '/survey/template/preview',
    query: { key: formKey }
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

// 保存模板信息
const handleSaveTemplate = async () => {
  if (!editForm.value.name) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!editForm.value.categoryId) {
    ElMessage.warning('请选择模板分类')
    return
  }

  try {
    const res = await templateApi.updateTemplate(editForm.value)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      queryTemplatePage()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '-'
  const category = templateTypeList.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.value.size = size
  queryParams.value.current = 1
  queryTemplatePage()
}

// 分页页码改变
const handlePageChange = (page) => {
  queryParams.value.current = page
  queryTemplatePage()
}

// 上传封面图
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  return `${baseUrl}/file/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${getToken()}`
  }
})

const handleTemplateCoverUpload = (response) => {
  if (response && response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    editForm.value.coverImg = getImageUrl(imageUrl)
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
  ElMessage.success('封面图已删除')
}

// 删除模板
const handleDelete = async (template) => {
  // 公共模板不允许删除
  if (template.isPublic === 1) {
    ElMessage.warning('公共模板不允许删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(`此操作将永久删除${template.name}, 是否继续?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await templateApi.deleteTemplate(template.formKey)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      queryTemplatePage()
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
      await queryTemplateType()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 处理分类管理下拉框命令
const handleCategoryCommand = (command) => {
  if (command === 'add') {
    handleAddCategory()
  } else if (command.startsWith('edit-')) {
    const categoryId = Number(command.replace('edit-', ''))
    const category = templateTypeList.value.find(c => c.id === categoryId)
    if (category) {
      // 检查权限：系统分类只能由管理员编辑
      if (!category.userId && !isAdmin.value) {
        ElMessage.warning('系统分类只能由管理员编辑')
        return
      }
      handleEditCategory(category)
    }
  } else if (command.startsWith('delete-')) {
    const categoryId = Number(command.replace('delete-', ''))
    const category = templateTypeList.value.find(c => c.id === categoryId)
    if (category) {
      handleDeleteCategory(category)
    }
  }
}

// 删除分类
const handleDeleteCategory = async (category) => {
  try {
    // 检查是否为系统分类（管理员可以删除系统分类）
    const isSystemCategory = !category.userId
    
    if (isSystemCategory && !isAdmin.value) {
      ElMessage.warning('系统分类只能由管理员删除')
    return
  }

    // 提示信息：说明会删除分类下的模板
    const message = isSystemCategory 
      ? `此操作将永久删除系统分类"${category.name}"及其下的所有模板和相关数据，是否继续？`
      : `此操作将永久删除分类"${category.name}"及其下的所有模板和相关数据，是否继续？`
    
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
      await queryTemplateType()
      // 如果当前选中的分类被删除，重置为全部
      if (queryParams.value.type === category.id) {
        queryParams.value.type = null
        queryTemplatePage()
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
  queryTemplateType()
  queryTemplatePage()
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
  min-height: calc(100vh - 100px);

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

/* 手机端：确保搜索框和按钮在同一行 */
@media (max-width: 768px) {
  .search-row {
    flex-wrap: nowrap;
    gap: 8px;
  }

  .search-form-item {
    flex: 1;
    min-width: 0;
    max-width: none;
    width: auto;
  }

  .search-button-item {
    flex-shrink: 0;
  }
}

.search-input {
  width: 100%;
  max-width: 300px;
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
  width: calc(100% - 16px);
  height: 200px;
  margin: 8px auto;
  display: block;
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
}

.template-actions .el-button {
  flex-shrink: 0;
  white-space: nowrap;
  font-size: 12px;
  padding: 5px 10px;
}

/* 手机端：确保按钮不换行 */
@media (max-width: 768px) {
  .template-actions {
    flex-wrap: nowrap;
    gap: 4px;
    padding: 0 4px;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* IE and Edge */
  }

  .template-actions::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera */
  }

  .template-actions .el-button {
    flex: 0 0 auto;
    font-size: 11px;
    padding: 4px 8px;
    min-width: auto;
  }
}

.template-tabs {
  margin-bottom: 20px;
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

.el-menu.el-menu--horizontal {
  border-bottom: none;
}

.text-center {
  text-align: center;
}

.view-toggle-item {
  margin-left: auto;
}

.view-toggle-group {
  display: flex;
  justify-content: center;
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

.template-cover-container {
  width: 100%;
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  position: relative;
}

.template-cover-upload {
  width: 100%;
}

.template-cover-preview-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-cover-preview {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.template-cover-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.template-cover-preview-wrapper:hover .template-cover-actions {
  opacity: 1;
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
</style>
