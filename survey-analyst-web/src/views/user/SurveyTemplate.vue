<template>
  <div class="template-list-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入模板名称"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">
        查询
      </el-button>
    </div>

    <!-- 分类标签页 -->
    <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="默认" name="default" />
      <el-tab-pane label="分类1" name="category1" />
    </el-tabs>

    <!-- 模板网格 -->
    <div v-loading="loading" class="template-grid">
      <el-card
        v-for="template in templateList"
        :key="template.id"
        class="template-card"
        shadow="hover"
      >
        <div class="template-icon">
          <el-icon :size="60"><Document /></el-icon>
        </div>
        <div class="template-name">{{ template.name }}</div>
        <div class="template-actions">
          <el-button text :icon="View" @click="handleView(template.id)">
            查看
          </el-button>
          <el-button text :icon="Delete" @click="handleDelete(template.id)">
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && templateList.length === 0" description="暂无模板" />

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, Delete, Document } from '@element-plus/icons-vue'

const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const templateList = ref([])
const currentPage = ref(1)
const pageSize = ref(18)
const total = ref(0)

const loadTemplateList = async () => {
  loading.value = true
  try {
    // TODO: 调用模板列表 API
    // const res = await templateApi.getTemplateList({
    //   pageNum: currentPage.value,
    //   pageSize: pageSize.value,
    //   keyword: searchKeyword.value,
    //   category: activeCategory.value === 'all' ? undefined : activeCategory.value
    // })
    // if (res.code === 200) {
    //   templateList.value = res.data.records || []
    //   total.value = res.data.total || 0
    // }
    
    // 临时数据
    templateList.value = []
    total.value = 0
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadTemplateList()
}

const handleCategoryChange = () => {
  currentPage.value = 1
  loadTemplateList()
}

const handleSizeChange = () => {
  loadTemplateList()
}

const handleCurrentChange = () => {
  loadTemplateList()
}

const handleView = (id) => {
  // TODO: 跳转到模板详情或使用模板创建问卷
  ElMessage.info('查看模板功能开发中')
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用删除 API
    // await templateApi.deleteTemplate(id)
    ElMessage.success('删除成功')
    loadTemplateList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadTemplateList()
})
</script>

<style scoped>
.template-list-container {
  padding: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.template-card {
  text-align: center;
  cursor: pointer;
}

.template-icon {
  padding: 40px 0;
  color: #c0c4cc;
}

.template-name {
  font-size: 14px;
  margin-bottom: 10px;
  color: #303133;
}

.template-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

