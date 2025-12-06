<template>
  <div class="form-edit-container">
    <!-- 顶部工具栏 -->
    <div class="header-container">
      <el-card class="header-card">
        <div class="header-content">
          <el-button text @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div class="header-title">
            <el-input
              v-model="formName"
              class="form-name-input"
              placeholder="表单名称"
              @blur="handleFormNameChange"
            />
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="handlePreview">预览</el-button>
            <el-button type="success" @click="handleSave">保存</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主体：三栏布局 -->
    <div class="main-container">
      <!-- 左侧：组件库 -->
      <div class="left-board">
        <div class="left-scrollbar">
          <div class="components-list">
            <div
              v-for="component in componentList"
              :key="component.type"
              class="components-item"
              :draggable="true"
              @dragstart="handleDragStart($event, component)"
            >
              <div class="components-body">
                <el-icon class="component-icon">
                  <component :is="component.icon" />
                </el-icon>
                <span class="component-label">{{ component.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：设计区域 -->
      <div class="center-board">
        <div class="center-scrollbar">
          <div class="center-board-row">
            <el-form
              ref="drawingForm"
              :model="formModel"
              label-position="top"
              class="drawing-board"
            >
              <!-- 表单标题 -->
              <div class="form-name-section">
                <h2
                  v-if="!editingFormName"
                  class="form-name-text"
                  @click="editingFormName = true"
                >
                  {{ formName || '未命名表单' }}
                </h2>
                <el-input
                  v-else
                  v-model="formName"
                  class="form-name-input"
                  @blur="editingFormName = false"
                  @keyup.enter="editingFormName = false"
                />
              </div>

              <!-- 表单项列表 -->
              <div 
                class="draggable-container"
                @drop="handleDrop"
                @dragover.prevent
                @dragenter.prevent
              >
                <VueDraggable
                  v-model="drawingList"
                  handle=".drag-handle"
                  :animation="200"
                  ghost-class="sortable-ghost"
                  chosen-class="chosen-item"
                  drag-class="drag-item"
                  @end="handleDragEnd"
                >
                  <div
                    v-for="element in drawingList"
                    :key="element.formItemId"
                    class="drawing-item"
                    :class="{
                      'active-from-item': activeId === element.formItemId,
                      'unfocus-bordered': activeId !== element.formItemId
                    }"
                    @click.stop="handleItemClick(element)"
                  >
                    <el-icon class="drag-handle"><Rank /></el-icon>
                    <div class="component-name">{{ getComponentLabel(element.type) }}</div>
                    <div class="drawing-item-copy" @click.stop="handleCopyItem(element)">
                      <el-icon><CopyDocument /></el-icon>
                    </div>
                    <div class="drawing-item-delete" @click.stop="handleDeleteItem(element)">
                      <el-icon><Delete /></el-icon>
                    </div>
                    <el-form-item
                      :label="element.label"
                      :prop="element.vModel"
                      :required="element.required"
                    >
                      <!-- 单行文本 -->
                      <el-input
                        v-if="element.type === 'INPUT'"
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        :readonly="element.readonly"
                      />
                      <!-- 多行文本 -->
                      <el-input
                        v-else-if="element.type === 'TEXTAREA'"
                        v-model="formModel[element.vModel]"
                        type="textarea"
                        :rows="4"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        :readonly="element.readonly"
                      />
                      <!-- 数字 -->
                      <el-input-number
                        v-else-if="element.type === 'NUMBER'"
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                      />
                      <!-- 单选框 -->
                      <el-radio-group
                        v-else-if="element.type === 'RADIO'"
                        v-model="formModel[element.vModel]"
                        :disabled="element.disabled"
                      >
                        <el-radio
                          v-for="(option, idx) in element.config?.options || []"
                          :key="idx"
                          :label="option.value"
                        >
                          {{ option.label }}
                        </el-radio>
                      </el-radio-group>
                      <!-- 多选框 -->
                      <el-checkbox-group
                        v-else-if="element.type === 'CHECKBOX'"
                        v-model="formModel[element.vModel]"
                        :disabled="element.disabled"
                      >
                        <el-checkbox
                          v-for="(option, idx) in element.config?.options || []"
                          :key="idx"
                          :label="option.value"
                        >
                          {{ option.label }}
                        </el-checkbox>
                      </el-checkbox-group>
                      <!-- 下拉框 -->
                      <el-select
                        v-else-if="element.type === 'SELECT'"
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                      >
                        <el-option
                          v-for="(option, idx) in element.config?.options || []"
                          :key="idx"
                          :label="option.label"
                          :value="option.value"
                        />
                      </el-select>
                      <!-- 评分 -->
                      <el-rate
                        v-else-if="element.type === 'RATE'"
                        v-model="formModel[element.vModel]"
                        :max="5"
                        :disabled="element.disabled"
                      />
                      <!-- 日期选择 -->
                      <el-date-picker
                        v-else-if="element.type === 'DATE'"
                        v-model="formModel[element.vModel]"
                        type="date"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        style="width: 100%"
                      />
                      <!-- 文件上传 -->
                      <el-upload
                        v-else-if="element.type === 'UPLOAD'"
                        v-model:file-list="formModel[element.vModel]"
                        :disabled="element.disabled"
                        action="#"
                        :auto-upload="false"
                      >
                        <el-button type="primary">选择文件</el-button>
                      </el-upload>
                      <!-- 默认：单行文本 -->
                      <el-input
                        v-else
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        :readonly="element.readonly"
                      />
                    </el-form-item>
                  </div>
                </VueDraggable>
                
                <!-- 空状态 -->
                <div v-if="drawingList.length === 0" class="empty-info">
                  从左侧拖拽组件到此处
                </div>
              </div>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 右侧：属性配置面板 -->
      <div class="right-board">
        <el-card class="property-panel">
          <template #header>
            <div class="property-header">组件属性</div>
          </template>
          <div v-if="activeData" class="property-content">
            <el-form :model="activeData" label-width="100px" label-position="top">
              <el-form-item label="标题">
                <el-input v-model="activeData.label" @input="handlePropertyChange" />
              </el-form-item>
              <el-form-item label="字段名">
                <el-input v-model="activeData.vModel" disabled />
              </el-form-item>
              <el-form-item label="提示文字">
                <el-input
                  v-model="activeData.placeholder"
                  @input="handlePropertyChange"
                />
              </el-form-item>
              <el-form-item label="是否必填">
                <el-switch v-model="activeData.required" @change="handlePropertyChange" />
              </el-form-item>
              <el-form-item label="是否禁用">
                <el-switch v-model="activeData.disabled" @change="handlePropertyChange" />
              </el-form-item>
              <el-form-item label="是否只读">
                <el-switch v-model="activeData.readonly" @change="handlePropertyChange" />
              </el-form-item>
              
              <!-- 选择题选项配置 -->
              <template v-if="isChoiceType(activeData.type)">
                <el-divider />
                <el-form-item label="选项列表">
                  <div
                    v-for="(option, idx) in activeData.config.options"
                    :key="idx"
                    class="option-item"
                  >
                    <el-input
                      v-model="option.label"
                      placeholder="选项文本"
                      @input="handlePropertyChange"
                    />
                    <el-button
                      type="danger"
                      text
                      @click="handleRemoveOption(idx)"
                    >
                      删除
                    </el-button>
                  </div>
                  <el-button type="primary" text @click="handleAddOption">
                    添加选项
                  </el-button>
                </el-form-item>
              </template>
            </el-form>
          </div>
          <div v-else class="empty-property">
            <el-empty description="请选择一个组件进行配置" :image-size="80" />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  CopyDocument,
  Delete,
  Rank
} from '@element-plus/icons-vue'
import { VueDraggable } from 'vue-draggable-plus'
import {
  ElInput,
  ElInputNumber,
  ElSelect,
  ElOption,
  ElRadioGroup,
  ElRadio,
  ElCheckboxGroup,
  ElCheckbox,
  ElRate,
  ElDatePicker,
  ElUpload
} from 'element-plus'
import { formApi } from '@/api'

const route = useRoute()
const router = useRouter()

// 表单基本信息
const formName = ref('未命名表单')
const editingFormName = ref(false)
const formKey = ref(null)
const formModel = reactive({})

// 组件列表
const componentList = [
  { type: 'INPUT', label: '单行文本', icon: 'Document', tag: 'el-input' },
  { type: 'TEXTAREA', label: '多行文本', icon: 'EditPen', tag: 'el-textarea' },
  { type: 'NUMBER', label: '数字', icon: 'List', tag: 'el-input-number' },
  { type: 'RADIO', label: '单选框', icon: 'CircleCheck', tag: 'el-radio-group' },
  { type: 'CHECKBOX', label: '多选框', icon: 'List', tag: 'el-checkbox-group' },
  { type: 'SELECT', label: '下拉框', icon: 'List', tag: 'el-select' },
  { type: 'RATE', label: '评分', icon: 'Star', tag: 'el-rate' },
  { type: 'DATE', label: '日期选择', icon: 'Calendar', tag: 'el-date-picker' },
  { type: 'UPLOAD', label: '文件上传', icon: 'Upload', tag: 'el-upload' }
]

// 设计区域数据
const drawingList = ref([])
const activeId = ref(null)
const activeData = computed(() => {
  return drawingList.value.find(item => item.formItemId === activeId.value)
})

// 获取组件标签文本
const getComponentLabel = (type) => {
  const component = componentList.find(c => c.type === type)
  return component ? component.label : type
}

// 判断是否为选择题
const isChoiceType = (type) => {
  return ['RADIO', 'CHECKBOX', 'SELECT'].includes(type)
}

// 拖拽开始
const handleDragStart = (event, component) => {
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.dropEffect = 'copy'
  event.dataTransfer.setData('componentType', component.type)
  event.dataTransfer.setData('text/plain', component.type) // 兼容性
  
  // 创建一个透明的拖拽图像，隐藏默认的禁用图标
  const dragImage = document.createElement('div')
  dragImage.style.position = 'absolute'
  dragImage.style.top = '-1000px'
  dragImage.style.width = '100px'
  dragImage.style.height = '40px'
  dragImage.style.background = 'rgba(64, 158, 255, 0.1)'
  dragImage.style.border = '1px dashed #409EFF'
  dragImage.style.borderRadius = '4px'
  dragImage.style.display = 'flex'
  dragImage.style.alignItems = 'center'
  dragImage.style.justifyContent = 'center'
  dragImage.style.fontSize = '12px'
  dragImage.style.color = '#409EFF'
  dragImage.textContent = component.label
  document.body.appendChild(dragImage)
  event.dataTransfer.setDragImage(dragImage, 50, 20)
  
  // 延迟移除拖拽图像
  setTimeout(() => {
    document.body.removeChild(dragImage)
  }, 0)
}

// 拖拽结束（VueDraggable 内部排序时触发）
const handleDragEnd = () => {
  // 保存排序后的列表
  saveFormItems()
}

// 处理从组件库拖拽到设计区域
const handleDrop = (event) => {
  event.preventDefault()
  event.stopPropagation()
  
  const componentType = event.dataTransfer.getData('componentType') || 
                        event.dataTransfer.getData('text/plain')
  
  if (!componentType) {
    return
  }
  
  const newItem = createFormItem(componentType)
  
  // 添加到列表末尾 - 使用展开运算符触发响应式更新
  drawingList.value = [...drawingList.value, newItem]
  
  // 初始化表单模型
  formModel[newItem.vModel] = newItem.defaultValue || ''
  
  // 选中新添加的项
  activeId.value = newItem.formItemId
  
  // 等待 DOM 更新后自动保存
  nextTick(() => {
    saveFormItems()
  })
}


// 创建表单项
const createFormItem = (type) => {
  const timestamp = Date.now()
  const formItemId = `${type.toLowerCase()}-${timestamp}`
  
  const baseItem = {
    formItemId,
    type,
    label: getComponentLabel(type),
    vModel: formItemId,
    placeholder: `请输入${getComponentLabel(type)}`,
    required: false,
    disabled: false,
    readonly: false,
    defaultValue: '',
    config: {}
  }
  
  // 选择题添加选项配置
  if (isChoiceType(type)) {
    baseItem.config = {
      options: [
        { label: '选项1', value: 'option1' },
        { label: '选项2', value: 'option2' }
      ]
    }
  }
  
  return baseItem
}

// 点击组件
const handleItemClick = (element) => {
  activeId.value = element.formItemId
}

// 复制组件
const handleCopyItem = (element) => {
  const newItem = {
    ...JSON.parse(JSON.stringify(element)),
    formItemId: `${element.type.toLowerCase()}-${Date.now()}`,
    vModel: `${element.vModel}_copy_${Date.now()}`
  }
  
  const index = drawingList.value.findIndex(item => item.formItemId === element.formItemId)
  drawingList.value.splice(index + 1, 0, newItem)
  
  formModel[newItem.vModel] = newItem.defaultValue || ''
  activeId.value = newItem.formItemId
  
  saveFormItems()
}

// 删除组件
const handleDeleteItem = (element) => {
  const index = drawingList.value.findIndex(item => item.formItemId === element.formItemId)
  if (index > -1) {
    drawingList.value.splice(index, 1)
    delete formModel[element.vModel]
    
    if (activeId.value === element.formItemId) {
      activeId.value = null
    }
    
    saveFormItems()
  }
}

// 属性变更
const handlePropertyChange = () => {
  saveFormItems()
}

// 添加选项
const handleAddOption = () => {
  if (!activeData.value.config.options) {
    activeData.value.config.options = []
  }
  const optionCount = activeData.value.config.options.length + 1
  activeData.value.config.options.push({
    label: `选项${optionCount}`,
    value: `option${optionCount}`
  })
  handlePropertyChange()
}

// 删除选项
const handleRemoveOption = (index) => {
  if (activeData.value.config.options) {
    activeData.value.config.options.splice(index, 1)
    handlePropertyChange()
  }
}

// 表单名称变更
const handleFormNameChange = () => {
  if (formKey.value) {
    saveFormConfig()
  }
}

// 保存表单配置
const saveFormConfig = async () => {
  if (!formKey.value) {
    formKey.value = generateFormKey()
  }
  
  try {
    await formApi.saveFormConfig({
      formKey: formKey.value,
      name: formName.value,
      description: ''
    })
  } catch (error) {
    // 保存表单配置失败
    ElMessage.error('保存表单配置失败')
  }
}

// 保存表单项
const saveFormItems = async () => {
  if (!formKey.value) {
    await saveFormConfig()
  }
  
  try {
    const items = drawingList.value.map((item, index) => ({
      formKey: formKey.value,
      formItemId: item.formItemId,
      type: item.type,
      label: item.label,
      required: item.required ? 1 : 0,
      placeholder: item.placeholder,
      sort: index,
      scheme: JSON.stringify(item)
    }))
    
    await formApi.saveFormItems(formKey.value, items)
  } catch (error) {
    // 保存表单项失败
    ElMessage.error('保存表单项失败')
  }
}

// 生成表单唯一标识
const generateFormKey = () => {
  return 'form_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 加载表单数据
const loadFormData = async () => {
  const surveyId = route.query.id
  if (!surveyId) {
    // 新建表单
    formKey.value = generateFormKey()
    return
  }
  
  try {
    // 加载表单配置
    const configRes = await formApi.getFormConfig(surveyId)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      formName.value = configRes.data.name || '未命名表单'
    } else {
      formKey.value = generateFormKey()
    }
    
    // 加载表单项
    if (formKey.value) {
      const itemsRes = await formApi.getFormItems(formKey.value)
      if (itemsRes.code === 200 && itemsRes.data) {
        drawingList.value = itemsRes.data.map(item => {
          const scheme = typeof item.scheme === 'string' 
            ? JSON.parse(item.scheme) 
            : item.scheme
          return {
            ...scheme,
            formItemId: item.formItemId,
            type: item.type,
            label: item.label,
            required: item.required === 1,
            placeholder: item.placeholder
          }
        })
        
        // 初始化表单模型
        drawingList.value.forEach(item => {
          formModel[item.vModel] = item.defaultValue || ''
        })
      }
    }
  } catch (error) {
    ElMessage.error('加载表单数据失败')
  }
}

// 保存
const handleSave = async () => {
  await saveFormConfig()
  await saveFormItems()
  ElMessage.success('保存成功')
}

// 预览
const handlePreview = () => {
  if (!formKey.value) {
    ElMessage.warning('请先保存表单')
    return
  }
  // TODO: 打开预览窗口
  ElMessage.info('预览功能开发中')
}

onMounted(() => {
  loadFormData()
})
</script>

<style lang="scss" scoped>
.form-edit-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 60px);
  overflow: hidden;
}

.header-container {
  height: 60px;
  border-bottom: 1px solid #ebeef5;
}

.header-card {
  height: 100%;
  border: none;
  border-radius: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  gap: 20px;
}

.header-title {
  flex: 1;
}

.form-name-input {
  font-size: 16px;
  font-weight: 500;
  border: none;
  background: transparent;
  
  :deep(.el-input__wrapper) {
    box-shadow: none;
    padding: 0;
  }
}

.header-actions {
  display: flex;
  gap: 10px;
}

.main-container {
  display: flex;
  height: calc(100vh - 120px);
}

.left-board {
  width: 260px;
  border-right: 1px solid #ebeef5;
  background: #fff;
}

.left-scrollbar {
  height: 100%;
  overflow-y: auto;
  padding: 10px;
}

.components-list {
  .components-item {
    display: inline-block;
    width: 48%;
    margin: 1%;
  }
}

.components-body {
  padding: 12px 8px;
  background: rgba(24, 144, 255, 0.05);
  border: 1px dashed rgba(24, 144, 255, 0.1);
  border-radius: 4px;
  text-align: center;
  cursor: move;
  transition: all 0.3s;
  
  &:hover {
    border-color: #409EFF;
    background: rgba(24, 144, 255, 0.1);
    transform: translateY(-2px);
  }
}

.component-icon {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 5px;
}

.component-label {
  display: block;
  font-size: 12px;
  color: #606266;
}

.center-board {
  flex: 1;
  overflow: hidden;
  background: #f5f7fa;
}

.center-scrollbar {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
}

.center-board-row {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.drawing-board {
  min-height: 400px;
}

.draggable-container {
  min-height: 300px;
  position: relative;
  width: 100%;
}

.draggable-container .empty-info {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
  pointer-events: none;
}

.form-name-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.form-name-text {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  cursor: pointer;
  padding: 10px;
  border: 1px dashed transparent;
  border-radius: 4px;
  
  &:hover {
    border-color: #409EFF;
    background: rgba(24, 144, 255, 0.05);
  }
}

.drawing-item {
  position: relative;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
  
  &.unfocus-bordered {
    :deep(.el-form-item) {
      border: 1px dashed #ccc;
      border-radius: 4px;
      padding: 10px;
    }
  }
  
  &.active-from-item {
    :deep(.el-form-item) {
      background: rgba(24, 144, 255, 0.05);
      border: 2px solid #409EFF;
      border-radius: 6px;
      padding: 10px;
    }
  }
  
  &:hover {
    .drawing-item-copy,
    .drawing-item-delete {
      display: block;
    }
  }
  
  .drag-handle {
    position: absolute;
    left: -30px;
    top: 50%;
    transform: translateY(-50%);
    cursor: move;
    color: #909399;
    font-size: 18px;
    z-index: 10;
    
    &:hover {
      color: #409EFF;
    }
  }
}

.component-name {
  position: absolute;
  top: -8px;
  left: 10px;
  font-size: 12px;
  color: #bbb;
  background: #fff;
  padding: 0 6px;
  z-index: 1;
}

.drawing-item.active-from-item .component-name {
  color: #409EFF;
}

.drawing-item-copy,
.drawing-item-delete {
  display: none;
  position: absolute;
  top: -10px;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  border: 1px solid;
  cursor: pointer;
  z-index: 1;
  background: #fff;
}

.drawing-item-copy {
  right: 56px;
  border-color: #409EFF;
  color: #409EFF;
  
  &:hover {
    background: #409EFF;
    color: #fff;
  }
}

.drawing-item-delete {
  right: 24px;
  border-color: #f56c6c;
  color: #f56c6c;
  
  &:hover {
    background: #f56c6c;
    color: #fff;
  }
}


.sortable-ghost {
  opacity: 0.5;
  background: rgba(24, 144, 255, 0.1);
  border: 2px dashed #409EFF;
}

.chosen-item {
  cursor: grabbing;
}

.drag-item {
  cursor: grabbing;
  opacity: 0.8;
}

.empty-info {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  font-size: 18px;
}

.right-board {
  width: 300px;
  border-left: 1px solid #ebeef5;
  background: #fff;
  overflow-y: auto;
}

.property-panel {
  border: none;
  border-radius: 0;
}

.property-header {
  font-weight: 500;
  font-size: 14px;
}

.property-content {
  padding: 10px 0;
}

.empty-property {
  padding: 40px 20px;
  text-align: center;
}

.option-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
  
  .el-input {
    flex: 1;
  }
}
</style>

