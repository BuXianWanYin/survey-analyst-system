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
        <el-form :inline="true">
          <el-form-item label="">
            <el-input
              v-model="queryParams.name"
              class="width80"
              placeholder="请输入模板名称"
              @keyup.enter="queryTemplatePage"
            />
          </el-form-item>
          <el-form-item>
            <el-button class="search-template-btn" type="primary" @click="queryTemplatePage" :icon="Search">
              查询
            </el-button>
            <!-- 我的模板显示管理分类下拉框 -->
            <el-dropdown
              v-if="activeTab === 'my'"
              trigger="click"
              @command="handleCategoryCommand"
              style="margin-left: 10px"
            >
              <el-button type="success" size="default" icon="Setting">
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
        :default-active="queryParams.type"
        mode="horizontal"
        style="background-color: transparent"
        @select="handleCategorySelect"
      >
        <el-menu-item :index="null">全部</el-menu-item>
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

    <!-- 模板网格 -->
    <div class="project-grid-container">
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
            <el-button icon="View" type="text" @click="toTemplatePreview(template.formKey)">
              查看
            </el-button>
            <!-- 只有我的模板才显示删除按钮，公共模板不显示 -->
            <el-button 
              v-if="activeTab === 'my' && template.isPublic !== 1" 
              icon="Delete" 
              type="text" 
              @click="handleDelete(template)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
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
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分页 -->
    <div class="text-center">
      <el-pagination
        v-if="total > 10"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        background
        layout="total, prev, pager, next"
        @current-change="queryTemplatePage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Plus, Edit, ArrowDown } from '@element-plus/icons-vue'
import { templateApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo?.role === 'ADMIN'
})

// 获取当前用户ID
const currentUserId = computed(() => {
  return userStore.userInfo?.id
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
const queryParams = ref({
  current: 1,
  size: 12,
  name: '',
  type: null,
  isPublic: 0 // 0-我的模板, 1-公共模板
})
const total = ref(0)
const templateTypeList = ref([])
const templateList = ref([])

// 分类管理
const categoryDialogVisible = ref(false)
const editingCategory = ref(null)
const categoryForm = ref({
  id: null,
  name: '',
  sort: 0
})

// 默认封面图（如果没有图片，使用占位图）
const defaultCoverImg = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEzMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEzMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjYzBjNGNjIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+6L+Z5Liq5Zu+54mHPC90ZXh0Pjwvc3ZnPg=='

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
  max-width: 1200px;
  flex-direction: row;
  flex-wrap: wrap;
}

.preview-img {
  width: 90%;
  height: 130px;
  margin-top: 8px;
  border-radius: 10px;
  object-fit: cover;
}

.project-template-view {
  width: 151px;
  height: 196px;
  line-height: 20px;
  border-radius: 10px;
  text-align: center;
  margin: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
  background: white;
  position: relative;
}

.project-template-title {
  color: rgba(16, 16, 16, 100);
  font-size: 14px;
  margin: 0 3px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-actions {
  margin-top: 8px;
  display: flex;
  justify-content: center;
  gap: 10px;
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
</style>
