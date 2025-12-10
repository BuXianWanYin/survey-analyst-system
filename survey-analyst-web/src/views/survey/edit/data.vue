<template>
  <div class="data-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据管理</span>
          <div class="header-actions">
            <el-button :icon="Download" type="primary" @click="handleExport">导出Excel</el-button>
            <el-button :icon="Refresh" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="dataList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :expand-row-keys="expandedRows"
        row-key="id"
        @expand-change="handleExpandChange"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expanded-content">
              <div class="expanded-form-container">
                <SurveyFormRender
                  :form-items="formItems"
                  :form-model="getRowFormModel(row)"
                  :preview-mode="true"
                  :theme-config="themeConfig"
                  :show-number="themeConfig.showNumber || false"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="index" label="序号" min-width="60" />
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="createTime" label="提交时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-button :icon="View" type="text" @click="handleView(row)">查看</el-button>
            <el-button :icon="Delete" type="text" danger @click="handleDelete(row)">删除</el-button>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
    
    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="数据详情"
      width="900px"
    >
      <div v-if="currentData" class="data-detail">
        <div class="detail-form-container">
          <SurveyFormRender
            :form-items="formItems"
            :form-model="getRowFormModel(currentData)"
            :preview-mode="true"
            :theme-config="themeConfig"
            :show-number="themeConfig.showNumber || false"
          />
        </div>
        <div class="detail-item">
          <div class="detail-label">提交时间</div>
          <div class="detail-value">{{ formatDate(currentData.createTime) }}</div>
        </div>
        <div v-if="currentData.submitRequestIp" class="detail-item">
          <div class="detail-label">IP地址</div>
          <div class="detail-value">{{ currentData.submitRequestIp }}</div>
        </div>
        <div v-if="currentData.submitOs" class="detail-item">
          <div class="detail-label">操作系统</div>
          <div class="detail-value">{{ currentData.submitOs }}</div>
        </div>
        <div v-if="currentData.submitBrowser" class="detail-item">
          <div class="detail-label">浏览器</div>
          <div class="detail-value">{{ currentData.submitBrowser }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Refresh, View, Delete } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'
import { responseApi, formApi } from '@/api'
import dayjs from 'dayjs'
import SurveyFormRender from '@/components/SurveyFormRender.vue'

const route = useRoute()

const surveyId = ref(null)
const formKey = ref(null)
const loading = ref(false)
const dataList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const viewDialogVisible = ref(false)
const currentData = ref(null)
const formItems = ref([])
const expandedRows = ref([])
const themeConfig = reactive({
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  showNumber: false
})

// 存储每行的表单模型
const rowFormModels = reactive({})

// 加载表单配置和表单项
const loadFormConfig = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)

  try {
    // 加载表单配置获取 formKey
    const configRes = await formApi.getFormConfig(surveyId.value)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      
      // 加载表单项（包括所有类型，不过滤）
      if (formKey.value) {
        const itemsRes = await formApi.getFormItems(formKey.value)
        if (itemsRes.code === 200 && itemsRes.data) {
          formItems.value = itemsRes.data.map(item => {
            const scheme = typeof item.scheme === 'string' 
              ? JSON.parse(item.scheme) 
              : item.scheme
            return {
              formItemId: item.formItemId,
              label: item.label,
              type: item.type,
              required: item.required === 1,
              placeholder: scheme.placeholder || item.placeholder || '',
              disabled: scheme.disabled || false,
              readonly: scheme.readonly || false,
              hideType: item.isHideType || false,
              defaultValue: scheme.defaultValue !== undefined ? scheme.defaultValue : (item.defaultValue || null),
              config: scheme.config || {},
              regList: item.regList ? (typeof item.regList === 'string' ? JSON.parse(item.regList) : item.regList) : [],
              vModel: scheme.vModel || item.formItemId,
              scheme: scheme || {},
              sort: item.sort != null ? item.sort : 0
            }
          }).sort((a, b) => {
            const sortA = a.sort != null ? a.sort : 0
            const sortB = b.sort != null ? b.sort : 0
            return sortA - sortB
          })
        }
      }
      
      // 加载外观配置
      if (surveyId.value) {
        try {
          const themeRes = await formApi.getFormTheme(surveyId.value)
          if (themeRes.code === 200 && themeRes.data) {
            const data = themeRes.data
            if (data.themeColor) themeConfig.themeColor = data.themeColor
            if (data.backgroundColor) themeConfig.backgroundColor = data.backgroundColor
            if (data.showNumber !== undefined) themeConfig.showNumber = data.showNumber
          }
        } catch (error) {
          // 使用默认值
        }
      }
    }
    
    await loadData()
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

// 加载数据
const loadData = async () => {
  if (!formKey.value) return

  loading.value = true
  try {
    const res = await formApi.getFormDataList(formKey.value, {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (res.code === 200 && res.data) {
      // 将 originalData 转换为 data 字段
      dataList.value = (res.data.records || []).map(item => ({
        ...item,
        data: item.originalData || {},
        createTime: item.createTime
      }))
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 获取字段标签
const getFieldLabel = (formItemId) => {
  const item = formItems.value.find(i => i.formItemId === formItemId)
  return item ? item.label : formItemId
}

// 格式化数据值
const formatDataValue = (value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  if (typeof value === 'object') {
    if (Array.isArray(value)) {
      return value.join(', ')
    }
    return JSON.stringify(value)
  }
  return String(value)
}

// 格式化数据值（带选项显示）
const formatDataValueWithOptions = (formItemId, value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) {
    return formatDataValue(value)
  }
  
  // 如果是单选、多选、下拉等有选项的组件
  if (['RADIO', 'CHECKBOX', 'SELECT', 'IMAGE_SELECT'].includes(item.type)) {
    const options = item.config?.options || []
    if (Array.isArray(value)) {
      // 多选情况
      const selectedLabels = value.map(v => {
        const option = options.find(opt => opt.value === v)
        return option ? option.label : v
      })
      return selectedLabels.join('、')
    } else {
      // 单选情况
      const option = options.find(opt => opt.value === value)
      return option ? option.label : value
    }
  }
  
  // 如果是级联选择
  if (item.type === 'CASCADER') {
    const options = item.config?.options || []
    if (Array.isArray(value) && value.length > 0) {
      // 级联选择返回的是数组，需要找到对应的标签
      let labels = []
      let currentOptions = options
      for (const val of value) {
        const option = currentOptions.find(opt => opt.value === val)
        if (option) {
          labels.push(option.label)
          currentOptions = option.children || []
        } else {
          labels.push(val)
        }
      }
      return labels.join(' / ')
    }
  }
  
  // 如果是评分
  if (item.type === 'RATE') {
    return `${value} 分`
  }
  
  // 如果是滑块
  if (item.type === 'SLIDER') {
    return String(value)
  }
  
  // 其他情况使用默认格式化
  return formatDataValue(value)
}

// 判断是否为签名字段
const isSignatureField = (formItemId, value) => {
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (item && item.type === 'SIGN_PAD') {
    // 检查值是否为base64图片
    if (typeof value === 'string' && value.startsWith('data:image')) {
      return true
    }
  }
  return false
}

// 判断是否为文件字段
const isFileField = (formItemId, value) => {
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (item && item.type === 'UPLOAD') {
    return true
  }
  return false
}

// 判断是否为图片字段
const isImageField = (formItemId, value) => {
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (item && item.type === 'IMAGE_UPLOAD') {
    return true
  }
  return false
}

// 获取行的表单模型
const getRowFormModel = (row) => {
  const rowId = row.id
  if (!rowFormModels[rowId]) {
    // 初始化表单模型
    const formModel = {}
    const rowData = row.data || row.originalData || {}
    
    formItems.value.forEach(item => {
      if (!item.vModel) return
      
      const value = rowData[item.formItemId]
      
      // 根据组件类型处理值
      if (item.type === 'CHECKBOX' || item.type === 'UPLOAD' || item.type === 'IMAGE_UPLOAD') {
        formModel[item.vModel] = Array.isArray(value) ? value : (value ? [value] : [])
      } else if (item.type === 'IMAGE_SELECT' && item.config?.multiple) {
        formModel[item.vModel] = Array.isArray(value) ? value : (value ? [value] : [])
      } else if (item.type === 'NUMBER' || item.type === 'SLIDER' || item.type === 'RATE') {
        formModel[item.vModel] = value !== null && value !== undefined ? Number(value) : (item.type === 'SLIDER' ? (item.config?.min || 0) : (item.type === 'RATE' ? 0 : undefined))
      } else if (item.type === 'SIGN_PAD') {
        formModel[item.vModel] = value || ''
      } else {
        formModel[item.vModel] = value !== null && value !== undefined ? value : ''
      }
    })
    
    rowFormModels[rowId] = formModel
  }
  
  return rowFormModels[rowId]
}

// 处理展开/收缩
const handleExpandChange = (row, expandedRows) => {
  // expandedRows 是当前展开的所有行的数组
  // 这里可以用于跟踪展开状态
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 查看详情
const handleView = (row) => {
  currentData.value = row
  viewDialogVisible.value = true
}

// 删除数据
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await formApi.deleteFormData(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 导出数据
const handleExport = async () => {
  if (!surveyId.value) {
    ElMessage.warning('请先选择问卷')
    return
  }
  
  try {
    const { exportApi } = await import('@/api/export')
    await exportApi.exportSurveyData(surveyId.value)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 刷新
const handleRefresh = () => {
  loadData()
}

onMounted(() => {
  loadFormConfig()
})
</script>

<style lang="scss" scoped>
.data-container {
  padding: 20px;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.data-container :deep(.el-card) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.data-container :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  min-height: 0;
  /* 自定义滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
}

.data-container :deep(.el-card__body)::-webkit-scrollbar {
  width: 6px;
}

.data-container :deep(.el-card__body)::-webkit-scrollbar-track {
  background: transparent;
}

.data-container :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 3px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.expanded-content {
  padding: 0;
  background: #f5f7fa;
  
  .expanded-form-container {
    padding: 40px;
    background: #ffffff;
    max-width: 800px;
    margin: 0 auto;
    
    :deep(.survey-form-render) {
      width: 100%;
    }
    
    :deep(.form-item-wrapper) {
      margin-bottom: 20px;
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  
  @media (max-width: 768px) {
    :deep(.el-pagination) {
      justify-content: center;
    }
    
    :deep(.el-pagination__sizes),
    :deep(.el-pagination__total),
    :deep(.el-pagination__jump) {
      margin: 8px 4px;
    }
    
    :deep(.el-pagination__pager) {
      margin: 8px 0;
    }
  }
}

.data-detail {
  .detail-form-container {
    :deep(.survey-form-render) {
      width: 100%;
    }
    
    :deep(.form-item-wrapper) {
    margin-bottom: 20px;
    }
  }
}
</style>
