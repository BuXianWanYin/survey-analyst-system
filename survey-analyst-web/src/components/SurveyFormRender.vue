<template>
  <el-form
    ref="formRef"
    :model="formModel"
    label-position="top"
    class="survey-form-render"
  >
    <div
      v-for="element in formItems"
      :key="element.formItemId"
      class="form-item-wrapper"
    >
      <el-form-item
        v-if="element.type !== 'DIVIDER'"
        :label="element.label"
        :prop="element.vModel"
      >
        <template #label>
          <span class="form-label">
            <span v-if="showNumber" class="question-number">{{ getQuestionIndex(element) }}. </span>
            <span v-if="!!element.required || element.required === 1" class="required-mark">*</span>
            {{ element.label }}
          </span>
        </template>

        <!-- 单行文本 -->
        <el-input
          v-if="element.type === 'INPUT'"
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :readonly="element.readonly || previewMode"
          :style="getInputStyle()"
          class="theme-input"
        />

        <!-- 多行文本 -->
        <el-input
          v-else-if="element.type === 'TEXTAREA'"
          v-model="formModel[element.vModel]"
          type="textarea"
          :rows="4"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :readonly="element.readonly || previewMode"
          :style="getInputStyle()"
          class="theme-input"
        />

        <!-- 数字 -->
        <el-input-number
          v-else-if="element.type === 'NUMBER'"
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :style="getInputStyle('width: 100%')"
          class="theme-input"
        />

        <!-- 单选框 -->
        <el-radio-group
          v-else-if="element.type === 'RADIO'"
          v-model="formModel[element.vModel]"
          :disabled="element.disabled || previewMode"
          class="theme-radio-group"
        >
          <el-radio
            v-for="(option, idx) in element.config?.options || []"
            :key="idx"
            :label="option.value"
            class="theme-radio"
          >
            {{ option.label }}
          </el-radio>
        </el-radio-group>

        <!-- 多选框 -->
        <el-checkbox-group
          v-else-if="element.type === 'CHECKBOX'"
          v-model="formModel[element.vModel]"
          :disabled="element.disabled || previewMode"
          class="theme-checkbox-group"
        >
          <el-checkbox
            v-for="(option, idx) in element.config?.options || []"
            :key="idx"
            :label="option.value"
            class="theme-checkbox"
          >
            {{ option.label }}
          </el-checkbox>
        </el-checkbox-group>

        <!-- 下拉框 -->
        <el-select
          v-else-if="element.type === 'SELECT'"
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :style="getInputStyle('width: 100%')"
          class="theme-select"
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
          :max="element.config?.max || 5"
          :disabled="element.disabled || previewMode"
          :color="getThemeColor()"
          class="theme-rate"
        />

        <!-- 日期选择 -->
        <el-date-picker
          v-else-if="element.type === 'DATE'"
          v-model="formModel[element.vModel]"
          type="date"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :style="getInputStyle('width: 100%')"
          class="theme-date-picker"
        />

        <!-- 文件上传 -->
        <el-upload
          v-else-if="element.type === 'UPLOAD'"
          :file-list="getUploadFileList(element.vModel)"
          @change="handleUploadChange(element.vModel, $event)"
          :disabled="element.disabled || previewMode"
          action="#"
          :auto-upload="false"
          :limit="element.config?.limit || 1"
        >
          <el-button 
            type="primary" 
            :disabled="previewMode"
            :style="{
              backgroundColor: getThemeColor(),
              borderColor: getThemeColor()
            }"
          >
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
        </el-upload>

        <!-- 图片上传 -->
        <el-upload
          v-else-if="element.type === 'IMAGE_UPLOAD'"
          :file-list="getUploadFileList(element.vModel)"
          @change="handleUploadChange(element.vModel, $event)"
          :disabled="element.disabled || previewMode"
          action="#"
          :auto-upload="false"
          list-type="picture-card"
          :limit="element.config?.limit || 9"
        >
          <el-icon v-if="!previewMode"><Plus /></el-icon>
        </el-upload>

        <!-- 滑块 -->
        <div
          v-else-if="element.type === 'SLIDER'"
          class="slider-wrapper"
        >
          <el-slider
            :model-value="getSliderValue(element.vModel, element.config)"
            @update:model-value="setSliderValue(element.vModel, $event)"
            :min="element.config?.min || 0"
            :max="element.config?.max || 100"
            :step="element.config?.step || 1"
            :disabled="element.disabled || previewMode"
            :color="getThemeColor()"
            class="theme-slider"
          />
        </div>

        <!-- 级联选择 -->
        <el-cascader
          v-else-if="element.type === 'CASCADER'"
          v-model="formModel[element.vModel]"
          :options="element.config?.options || []"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :style="getInputStyle('width: 100%')"
          class="theme-cascader"
        />


        <!-- 图片轮播 -->
        <div
          v-else-if="element.type === 'IMAGE_CAROUSEL'"
          class="image-carousel-wrapper"
        >
          <el-carousel
            v-if="element.config?.options && element.config.options.filter(opt => opt.url).length > 0"
            :key="`carousel-${element.formItemId}`"
            :height="`${element.config?.height || 300}px`"
            :interval="element.config?.interval || 4000"
          >
            <el-carousel-item
              v-for="(option, idx) in element.config.options.filter(opt => opt.url)"
              :key="option.url || idx"
            >
              <el-image
                :src="option.url"
                :fit="element.config?.fit || 'cover'"
                style="width: 100%; height: 100%"
              />
            </el-carousel-item>
          </el-carousel>
          <div
            v-else
            class="carousel-placeholder"
          >
            <el-icon><Picture /></el-icon>
            <span>请添加图片</span>
          </div>
        </div>

        <!-- 图片选择 -->
        <div
          v-else-if="element.type === 'IMAGE_SELECT'"
          class="image-select-container"
        >
          <el-radio-group
            v-model="formModel[element.vModel]"
            :disabled="element.disabled || previewMode"
            class="image-select-radio-group"
          >
            <div
              v-for="(option, idx) in element.config?.options || []"
              :key="idx"
              class="image-select-item"
              :class="{ active: formModel[element.vModel] === option.value }"
            >
              <el-radio
                :label="option.value"
                class="image-select-radio"
              >
                <div class="image-select-content">
                  <el-image
                    v-if="option.image"
                    :src="option.image"
                    fit="cover"
                    class="image-select-img"
                  />
                  <div v-else class="image-select-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                  <span class="image-select-label">{{ option.label }}</span>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
        </div>

        <!-- 图片展示 -->
        <el-image
          v-else-if="element.type === 'IMAGE'"
          :src="element.config?.imageUrl || ''"
          :fit="element.config?.fit || 'cover'"
          style="width: 100%"
          :preview-src-list="element.config?.previewList || []"
        />

        <!-- 排序题型 -->
        <div
          v-else-if="element.type === 'SORT'"
          class="sort-container"
        >
          <VueDraggable
            v-model="formModel[element.vModel]"
            handle=".sort-handle"
            :animation="200"
            :disabled="previewMode || element.disabled"
          >
            <div
              v-for="(item, idx) in formModel[element.vModel] || []"
              :key="idx"
              class="sort-item"
            >
              <el-icon class="sort-handle">
                <Rank />
              </el-icon>
              <span>{{ item.label || item }}</span>
            </div>
          </VueDraggable>
        </div>

        <!-- 默认：单行文本 -->
        <el-input
          v-else
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :readonly="element.readonly || previewMode"
          :style="getInputStyle()"
          class="theme-input"
        />
      </el-form-item>

      <!-- 分割线独立渲染 -->
      <el-divider
        v-else
        :content-position="element.config?.contentPosition || 'center'"
      >
        {{ element.config?.content || '' }}
      </el-divider>
    </div>
  </el-form>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Upload, Plus, Picture, Rank } from '@element-plus/icons-vue'
import { VueDraggable } from 'vue-draggable-plus'

const props = defineProps({
  formItems: {
    type: Array,
    default: () => []
  },
  formModel: {
    type: Object,
    default: () => ({})
  },
  previewMode: {
    type: Boolean,
    default: false
  },
  themeConfig: {
    type: Object,
    default: () => ({})
  },
  showNumber: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:formModel'])

// 获取题目序号
const getQuestionIndex = (element) => {
  let index = 0
  for (const item of props.formItems) {
    if (item.type !== 'DIVIDER' && item.type !== 'IMAGE' && item.type !== 'IMAGE_CAROUSEL') {
      index++
      if (item.formItemId === element.formItemId) {
        return index
      }
    }
  }
  return index
}

// 内部状态存储，用于类型转换（只用于类型转换，不直接修改 formModel）
const uploadFileLists = ref({})
const sliderValues = ref({})

// 获取上传组件的文件列表（确保是数组）
const getUploadFileList = (vModel) => {
  // 如果已经有缓存的值，使用缓存
  if (uploadFileLists.value[vModel] !== undefined) {
    return uploadFileLists.value[vModel]
  }
  
  const value = props.formModel[vModel]
  let fileList = []
  
  if (Array.isArray(value)) {
    fileList = value
  } else if (value === null || value === undefined || value === '') {
    fileList = []
  } else {
    // 如果值是字符串或其他类型，尝试转换为数组
    try {
      const parsed = typeof value === 'string' ? JSON.parse(value) : value
      if (Array.isArray(parsed)) {
        fileList = parsed
      } else {
        fileList = []
      }
    } catch {
      fileList = []
    }
  }
  
  // 只缓存，不修改 formModel（让父组件负责初始化）
  uploadFileLists.value[vModel] = fileList
  
  return fileList
}

// 处理上传组件的变化
const handleUploadChange = (vModel, file, fileList) => {
  // Element Plus 的 upload 组件的 change 事件参数是 (file, fileList)
  const files = Array.isArray(fileList) ? fileList : []
  uploadFileLists.value[vModel] = files
  // 只有在 formModel 中已存在该字段时才更新
  if (vModel in props.formModel) {
    props.formModel[vModel] = files
  }
}

// 获取滑块的值（确保是数字）
const getSliderValue = (vModel, config) => {
  // 如果已经有缓存的值，使用缓存
  if (sliderValues.value[vModel] !== undefined) {
    return sliderValues.value[vModel]
  }
  
  const value = props.formModel[vModel]
  let numValue
  
  // 只对滑块类型的值进行转换，如果值不存在或无效，使用默认值但不修改 formModel
  if (value === null || value === undefined || value === '') {
    numValue = config?.min || 0
  } else {
    numValue = Number(value)
    if (isNaN(numValue)) {
      numValue = config?.min || 0
    }
  }
  
  // 只缓存，不修改 formModel（让父组件负责初始化）
  sliderValues.value[vModel] = numValue
  
  return numValue
}

// 设置滑块的值
const setSliderValue = (vModel, value) => {
  const numValue = Number(value)
  sliderValues.value[vModel] = numValue
  // 只有在 formModel 中已存在该字段时才更新
  if (vModel in props.formModel) {
    props.formModel[vModel] = numValue
  }
}

const formRef = ref(null)

// 获取主题颜色
const getThemeColor = () => {
  return props.themeConfig?.themeColor || '#409EFF'
}

// 获取输入框样式
const getInputStyle = (additionalStyles = '') => {
  const themeColor = getThemeColor()
  return `
    ${additionalStyles};
    --el-input-focus-border-color: ${themeColor};
    --el-border-color-hover: ${themeColor};
  `
}

// 获取激活状态的背景色（主题色的浅色版本）
const getActiveBgColor = () => {
  const themeColor = getThemeColor()
  // 将主题色转换为rgba，降低透明度
  if (themeColor.startsWith('#')) {
    const r = parseInt(themeColor.slice(1, 3), 16)
    const g = parseInt(themeColor.slice(3, 5), 16)
    const b = parseInt(themeColor.slice(5, 7), 16)
    return `rgba(${r}, ${g}, ${b}, 0.1)`
  }
  return '#ecf5ff'
}
</script>

<style lang="scss" scoped>
.survey-form-render {
  width: 100%;
}

.form-item-wrapper {
  margin-bottom: 20px;
  
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
  
  :deep(.el-form-item__label) {
    padding-bottom: 8px;
    font-weight: 500;
  }
}

.form-label {
  .question-number {
    font-weight: 500;
    margin-right: 4px;
  }

  .required-mark {
    color: #f56c6c;
    margin-right: 4px;
  }
}

// 主题颜色应用到表单控件
:deep(.theme-input) {
  .el-input__wrapper {
    &.is-focus {
      box-shadow: 0 0 0 1px v-bind('getThemeColor()') inset;
    }
  }
  
  &.el-input--large .el-input__wrapper {
    &.is-focus {
      box-shadow: 0 0 0 1px v-bind('getThemeColor()') inset;
    }
  }
}

:deep(.theme-radio-group) {
  .el-radio {
    .el-radio__input.is-checked .el-radio__inner {
      background-color: v-bind('getThemeColor()');
      border-color: v-bind('getThemeColor()');
    }
    
    .el-radio__input.is-checked + .el-radio__label {
      color: v-bind('getThemeColor()');
    }
  }
}

:deep(.theme-checkbox-group) {
  .el-checkbox {
    .el-checkbox__input.is-checked .el-checkbox__inner {
      background-color: v-bind('getThemeColor()');
      border-color: v-bind('getThemeColor()');
    }
    
    .el-checkbox__input.is-checked + .el-checkbox__label {
      color: v-bind('getThemeColor()');
    }
  }
}

:deep(.theme-select) {
  .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 1px v-bind('getThemeColor()') inset;
  }
  
  .el-select__wrapper.is-focused {
    box-shadow: 0 0 0 1px v-bind('getThemeColor()') inset;
  }
}

:deep(.theme-cascader) {
  .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 1px v-bind('getThemeColor()') inset;
  }
}

:deep(.theme-date-picker) {
  .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 1px v-bind('getThemeColor()') inset;
  }
}

:deep(.theme-slider) {
  .el-slider__button {
    border-color: v-bind('getThemeColor()');
  }
  
  .el-slider__bar {
    background-color: v-bind('getThemeColor()');
  }
  
  .el-slider__button-wrapper {
    .el-slider__button {
      background-color: v-bind('getThemeColor()');
    }
  }
}

// 题目序号使用主题颜色
.form-label {
  .question-number {
    color: v-bind('getThemeColor()');
  }
}

// 图片选择样式
.image-select-container {
  width: 100%;
  
  .image-select-radio-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    width: 100%;
  }
  
  .image-select-item {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 8px;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    transition: all 0.3s;
    min-width: 100px;
    
    &.active {
      border-color: v-bind('getThemeColor()');
      background-color: v-bind('getActiveBgColor()');
    }
    
    .image-select-radio {
      width: 100%;
      height: 100%;
      margin: 0;
      position: relative;
      
      :deep(.el-radio__input) {
        position: absolute;
        top: 4px;
        right: 4px;
        z-index: 10;
      }
      
      :deep(.el-radio__label) {
        padding-left: 0;
        width: 100%;
      }
    }
    
    .image-select-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 100%;
    }
    
    .image-select-img {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      margin-bottom: 8px;
    }
    
    .image-select-placeholder {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #909399;
      
      .el-icon {
        font-size: 32px;
      }
    }
    
    .image-select-label {
      font-size: 14px;
      text-align: center;
    }
  }
}

// 排序题型样式
.sort-container {
  .sort-item {
    display: flex;
    align-items: center;
    padding: 12px;
    margin-bottom: 8px;
    background: #f5f7fa;
    border-radius: 4px;
    cursor: move;
    
    .sort-handle {
      margin-right: 8px;
      color: #909399;
      cursor: grab;
      
      &:active {
        cursor: grabbing;
      }
    }
  }
}

// 图片轮播样式
.image-carousel-wrapper {
  width: 100%;
  
  .carousel-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300px;
    background: #f5f7fa;
    border-radius: 4px;
    color: #909399;
    
    .el-icon {
      font-size: 48px;
      margin-bottom: 12px;
    }
  }
}

// 滑块组件样式
.slider-wrapper {
  padding: 0 12px;
  width: 100%;
  
  :deep(.el-slider) {
    width: 100%;
  }
}

// 分割线特殊处理
.form-item-wrapper {
  &:has(.el-divider) {
    margin-bottom: 10px;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
    
    :deep(.el-form-item__label) {
      display: none;
    }
  }
}

// 移动端样式优化
@media (max-width: 768px) {
  .survey-form-render {
    :deep(.el-radio-group) {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    
    :deep(.el-checkbox-group) {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    
    :deep(.el-form-item__label) {
      font-size: 14px;
    }
    
    .image-select-container {
      gap: 8px;
      
      .image-select-item {
        min-width: 80px;
        
        .image-select-img {
          width: 60px;
          height: 60px;
        }
      }
    }
  }
}
</style>
