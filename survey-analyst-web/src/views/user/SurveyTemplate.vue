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
              @keyup.enter="queryTemplatePage"
            />
          </el-form-item>
          <el-form-item>
            <el-button class="search-template-btn" type="primary" @click="queryTemplatePage" :icon="Search">
              查询
            </el-button>
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
            <el-button icon="Delete" type="text" @click="handleDelete(template)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && templateList.length === 0" description="暂无模板" />

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, Delete } from '@element-plus/icons-vue'
import { templateApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const queryParams = ref({
  current: 1,
  size: 12,
  name: '',
  type: null
})
const total = ref(0)
const templateTypeList = ref([])
const templateList = ref([])

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
    console.error('加载模板分类失败', error)
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

// 分类选择
const handleCategorySelect = (index) => {
  queryParams.value.type = index === 'null' ? null : Number(index)
  queryParams.value.current = 1
  queryTemplatePage()
}

// 查看模板预览
const toTemplatePreview = (formKey) => {
  router.push({
    path: '/user/survey/template/preview',
    query: { key: formKey }
  })
}

// 删除模板
const handleDelete = async (template) => {
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

.el-menu.el-menu--horizontal {
  border-bottom: none;
}

.text-center {
  text-align: center;
}
</style>
