
<template>
  <el-form
    ref="formRef"
    :model="formModel"
    :rules="formRules"
    label-position="top"
    class="survey-form-render"
  >
    <div
      v-for="element in visibleFormItems"
      :key="element.formItemId"
      class="form-item-wrapper"
    >
      <el-form-item
        v-if="element.type !== 'DIVIDER' && element.type !== 'IMAGE' && element.type !== 'SIGN_PAD' && element.type !== 'IMAGE_CAROUSEL' && element.type !== 'DESC_TEXT'"
        :label="element.label"
        :prop="element.vModel"
        :required="!!element.required || element.required === 1"
      >
        <template #label>
          <span class="form-label">
            <span
              v-if="showNumber"
              class="question-number"
            >{{ getQuestionIndex(element) }}. </span>
            {{ element.label }}
          </span>
        </template>

        <!-- 单行文本 -->
        <el-input
          v-if="element.type === 'INPUT'"
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :clearable="element.config?.clearable !== false"
          :maxlength="element.config?.maxLength"
          :minlength="element.config?.minLength"
          :show-word-limit="element.config?.showWordLimit || false"
          :style="getInputStyle()"
          class="theme-input"
          @blur="validateInput(element)"
        />

        <!-- 多行文本 -->
        <el-input
          v-else-if="element.type === 'TEXTAREA'"
          v-model="formModel[element.vModel]"
          type="textarea"
          :autosize="{ minRows: element.config?.minRows || 1, maxRows: element.config?.maxRows || 4 }"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :maxlength="element.config?.maxLength"
          :show-word-limit="element.config?.showWordLimit || false"
          :style="getInputStyle()"
          class="theme-input"
          @blur="validateInput(element)"
        />

        <!-- 数字 -->
        <el-input-number
          v-else-if="element.type === 'NUMBER'"
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :min="element.config?.min"
          :max="element.config?.max"
          :step="element.config?.step || 1"
          :precision="element.config?.precision"
          :controls-position="element.config?.controlsPosition || ''"
          :style="getInputStyle('width: 100%')"
          class="theme-input"
        />

        <!-- 单选框 -->
        <el-radio-group
          v-else-if="element.type === 'RADIO'"
          v-model="formModel[element.vModel]"
          :disabled="element.disabled || previewMode"
          :border="element.config?.border || false"
          :button="element.config?.button || false"
          :size="element.config?.size || 'default'"
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
          :min="element.config?.min"
          :max="element.config?.max"
          :border="element.config?.border || false"
          :button="element.config?.button || false"
          :size="element.config?.size || 'default'"
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
          :clearable="element.config?.clearable !== false"
          :multiple="element.config?.multiple || false"
          :filterable="element.config?.filterable || false"
          :size="element.config?.size || 'default'"
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
          :allow-half="element.config?.allowHalf || false"
          :show-text="element.config?.showText || false"
          :texts="element.config?.texts || []"
          :disabled="element.disabled || previewMode"
          :color="getThemeColor()"
          class="theme-rate"
        />

        <!-- 日期选择 -->
        <el-date-picker
          v-else-if="element.type === 'DATE'"
          v-model="formModel[element.vModel]"
          :type="element.config?.type || 'date'"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :format="element.config?.format || 'YYYY-MM-DD'"
          :value-format="element.config?.valueFormat || 'YYYY-MM-DD'"
          :clearable="element.config?.clearable !== false"
          :style="getInputStyle('width: 100%')"
          class="theme-date-picker"
        />

        <!-- 文件上传 -->
        <div v-else-if="element.type === 'UPLOAD'">
          <el-upload
            :file-list="getUploadFileList(element.vModel)"
            :disabled="element.disabled || previewMode"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :auto-upload="element.config?.autoUpload !== false"
            :limit="element.config?.limit || 1"
            :accept="element.config?.accept || '*/*'"
            :multiple="element.config?.multiple || false"
            :on-success="(response, file, fileList) => handleFileUploadSuccess(response, file, fileList, element.vModel)"
            :on-error="handleFileUploadError"
            :on-exceed="() => ElMessage.warning(`最多只能上传${element.config?.limit || 1}个文件`)"
            @change="(file, fileList) => handleUploadChange(element.vModel, file, fileList)"
          >
            <el-button 
              type="primary" 
              :disabled="previewMode || element.disabled"
              :style="{
                backgroundColor: getThemeColor(),
                borderColor: getThemeColor()
              }"
            >
              <el-icon>
                <Upload />
              </el-icon>
              选择文件
            </el-button>
          </el-upload>
          <div
            v-if="element.config?.showTip"
            style="font-size: 12px; color: #909399; margin-top: 8px"
          >
            {{ getUploadTipText(element) }}
          </div>
        </div>

        <!-- 图片上传 -->
        <el-upload
          v-else-if="element.type === 'IMAGE_UPLOAD'"
          :file-list="getUploadFileList(element.vModel)"
          :disabled="element.disabled || previewMode"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :auto-upload="true"
          :list-type="element.config?.listType || 'picture-card'"
          :limit="element.config?.limit || 9"
          :accept="element.config?.accept || 'image/*'"
          :on-success="(response, file, fileList) => handleImageUploadSuccess(response, file, fileList, element.vModel)"
          :on-error="(error, file, fileList) => handleImageUploadError(error, file, fileList, element.vModel)"
          :on-exceed="() => ElMessage.warning(`最多只能上传${element.config?.limit || 9}张图片`)"
          @change="(file, fileList) => handleUploadChange(element.vModel, file, fileList)"
        >
          <el-icon v-if="!previewMode">
            <Plus />
          </el-icon>
        </el-upload>

        <!-- 滑块 -->
        <div
          v-else-if="element.type === 'SLIDER'"
          class="slider-wrapper"
          :class="{ 'slider-vertical': element.config?.vertical }"
        >
          <el-slider
            :model-value="getSliderValue(element.vModel, element.config)"
            :min="element.config?.min || 0"
            :max="element.config?.max || 100"
            :step="element.config?.step || 1"
            :disabled="element.disabled || previewMode"
            :show-input="element.config?.showInput || false"
            :show-stops="element.config?.showStops || false"
            :range="element.config?.range || false"
            :vertical="element.config?.vertical || false"
            :color="getThemeColor()"
            class="theme-slider"
            @update:model-value="setSliderValue(element.vModel, $event)"
          />
        </div>

        <!-- 级联选择 -->
        <el-cascader
          v-else-if="element.type === 'CASCADER'"
          v-model="formModel[element.vModel]"
          :options="element.config?.options || []"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :clearable="element.config?.clearable !== false"
          :show-all-levels="element.config?.showAllLevels !== false"
          :filterable="element.config?.filterable || false"
          :size="element.config?.size || 'default'"
          :style="getInputStyle('width: 100%')"
          class="theme-cascader"
        />

        <!-- 图片选择 -->
        <div
          v-else-if="element.type === 'IMAGE_SELECT'"
          class="image-select-container"
        >
          <!-- 单选模式 -->
          <el-radio-group
            v-if="!element.config?.multiple"
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
                  <div
                    v-else
                    class="image-select-placeholder"
                  >
                    <el-icon><Picture /></el-icon>
                  </div>
                  <span class="image-select-label">{{ option.label }}</span>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
          <!-- 多选模式 -->
          <el-checkbox-group
            v-else
            v-model="formModel[element.vModel]"
            :disabled="element.disabled || previewMode"
            class="image-select-checkbox-group"
          >
            <div
              v-for="(option, idx) in element.config?.options || []"
              :key="idx"
              class="image-select-item"
              :class="{ active: (formModel[element.vModel] || []).includes(option.value) }"
            >
              <el-checkbox
                :label="option.value"
                class="image-select-checkbox"
              >
                <div class="image-select-content">
                  <el-image
                    v-if="option.image"
                    :src="option.image"
                    fit="cover"
                    class="image-select-img"
                  />
                  <div
                    v-else
                    class="image-select-placeholder"
                  >
                    <el-icon><Picture /></el-icon>
                  </div>
                  <span class="image-select-label">{{ option.label }}</span>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>

        <!-- 默认：单行文本（排除不需要输入框的组件类型） -->
        <el-input
          v-else-if="element.type !== 'IMAGE' && element.type !== 'SIGN_PAD' && element.type !== 'IMAGE_CAROUSEL' && element.type !== 'DESC_TEXT'"
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :style="getInputStyle()"
          class="theme-input"
        />
        </el-form-item>

        <!-- 图片展示独立渲染 -->
        <div v-if="element.type === 'IMAGE'">
          <div class="form-label-wrapper">
            <span class="form-label">
              <span
                v-if="showNumber"
                class="question-number"
              >{{ getQuestionIndex(element) }}. </span>
              {{ element.label }}
            </span>
          </div>
          <el-image
            :src="getImageUrl(element.config?.imageUrl || '')"
            :fit="element.config?.fit || 'cover'"
            style="width: 100%"
            :preview-src-list="(element.config?.previewList || []).map(url => getImageUrl(url))"
          />
        </div>

        <!-- 手写签名 -->
        <div v-else-if="element.type === 'SIGN_PAD'">
          <div class="form-label-wrapper">
            <span class="form-label">
              <span
                v-if="showNumber"
                class="question-number"
              >{{ getQuestionIndex(element) }}. </span>
              <span
                v-if="!!element.required || element.required === 1"
                class="required-mark"
              >* </span>
              {{ element.label }}
            </span>
          </div>
          <SignPad
            v-model="formModel[element.vModel]"
            :width="600"
            :height="element.config?.height || 300"
            :background-color="'#ffffff'"
            :pen-color="element.config?.penColor || '#000000'"
            :disabled="element.disabled || previewMode"
          />
        </div>

        <!-- 图片轮播独立渲染 -->
        <div v-if="element.type === 'IMAGE_CAROUSEL'">
          <div class="form-label-wrapper">
            <span class="form-label">
              <span
                v-if="showNumber"
                class="question-number"
              >{{ getQuestionIndex(element) }}. </span>
              {{ element.label }}
            </span>
          </div>
          <div class="image-carousel-wrapper">
            <el-carousel
              v-if="element.config?.options && element.config.options.filter(opt => opt.url).length > 0"
              :key="`carousel-${element.formItemId}`"
              :height="`${element.config?.height || 300}px`"
              :interval="element.config?.interval || 4000"
              :arrow="element.config?.arrow || 'hover'"
            >
              <el-carousel-item
                v-for="(option, idx) in element.config.options.filter(opt => opt.url)"
                :key="option.url || idx"
              >
                <el-image
                  :src="getImageUrl(option.url)"
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
        </div>

        <!-- 文字描述独立渲染 -->
        <div v-else-if="element.type === 'DESC_TEXT'">
          <div class="form-label-wrapper">
            <span class="form-label">
              <span
                v-if="showNumber"
                class="question-number"
              >{{ getQuestionIndex(element) }}. </span>
              {{ element.label }}
            </span>
          </div>
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div
            class="desc-text-wrapper"
            :style="{
              textAlign: element.config?.textAlign || 'left',
              fontSize: (element.config?.fontSize || 14) + 'px',
              color: element.config?.color || '#606266'
            }"
            v-html="element.config?.content || ''"
          />
        </div>
        
        <!-- 分割线独立渲染 -->
        <el-divider
          v-else-if="element.type === 'DIVIDER'"
          :content-position="element.config?.contentPosition || 'center'"
          :direction="element.config?.direction || 'horizontal'"
        >
          {{ element.config?.content || '' }}
        </el-divider>
    </div>
  </el-form>
</template>

<script setup>
/**
 * 问卷表单渲染组件
 * 功能：渲染问卷表单项，支持多种题型（文本、单选、多选、日期、文件上传等），支持逻辑跳转、表单验证、主题配置等功能
 */

import { ref, computed, watch } from 'vue'
import { Upload, Plus, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import SignPad from '@/components/SignPad.vue'
import { getToken } from '@/utils/auth'
import { getImageUrl } from '@/utils/image'

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
  },
  formLogic: {
    type: Array,
    default: () => []
  }
})

const itemVisibility = ref({})

/**
 * 评估逻辑条件
 * @param {Object} condition 条件对象
 * @param {string} condition.formItemId 表单项ID
 * @param {string} condition.expression 表达式类型（eq、ne、gt、ge、lt、le）
 * @param {*} condition.optionValue 选项值
 * @param {Object} formModel 表单数据模型
 * @param {Array} formItems 表单项数组
 * @returns {boolean} 条件是否满足
 */
const evaluateCondition = (condition, formModel, formItems) => {
  const { formItemId, expression, optionValue, relation } = condition
  if (!formItemId || !expression) return true
  
  const item = formItems.find(i => i.formItemId === formItemId)
  if (!item) return true
  
  const value = formModel[item.vModel]
  
  switch (expression) {
    case 'eq':
      if (item.type === 'CHECKBOX') {
        return Array.isArray(value) && value.includes(optionValue)
      }
      return value === optionValue
    case 'ne':
      if (item.type === 'CHECKBOX') {
        return !Array.isArray(value) || !value.includes(optionValue)
      }
      return value !== optionValue
    case 'gt':
      return Number(value) > Number(optionValue)
    case 'ge':
      return Number(value) >= Number(optionValue)
    case 'lt':
      return Number(value) < Number(optionValue)
    case 'le':
      return Number(value) <= Number(optionValue)
    default:
      return true
  }
}

/**
 * 评估逻辑规则
 * @param {Object} rule 逻辑规则对象
 * @param {Array} rule.conditionList 条件列表
 * @param {Object} formModel 表单数据模型
 * @param {Array} formItems 表单项数组
 * @returns {boolean} 规则是否满足
 */
const evaluateLogicRule = (rule, formModel, formItems) => {
  const conditionList = rule.conditionList || []
  if (conditionList.length === 0) {
    return true
  }
  
  let result = true
  for (let i = 0; i < conditionList.length; i++) {
    const condition = conditionList[i]
    const conditionResult = evaluateCondition(condition, formModel, formItems)
    
    if (i === 0) {
      result = conditionResult
    } else {
      const relation = conditionList[0]?.relation || condition.relation || 'AND'
      if (relation === 'AND') {
        result = result && conditionResult
      } else if (relation === 'OR') {
        result = result || conditionResult
      }
    }
  }
  
  return result
}

/**
 * 更新表单项的显示状态
 * 根据逻辑规则控制表单项的显示和隐藏
 */
const updateItemVisibility = () => {
  props.formItems.forEach(item => {
    if (itemVisibility.value[item.formItemId] === undefined) {
      itemVisibility.value[item.formItemId] = true
    }
  })
  
  if (!props.formLogic || props.formLogic.length === 0) {
    return
  }
  
  const controlledItems = new Set()
  
  props.formLogic.forEach(rule => {
    if (!rule.conditionList || !rule.triggerList) return
    
    const conditionList = Array.isArray(rule.conditionList) 
      ? rule.conditionList 
      : (rule.conditionList instanceof Set ? Array.from(rule.conditionList) : [])
    const triggerList = Array.isArray(rule.triggerList) 
      ? rule.triggerList 
      : (rule.triggerList instanceof Set ? Array.from(rule.triggerList) : [])
    
    triggerList.forEach(trigger => {
      if (trigger.formItemId) {
        controlledItems.add(trigger.formItemId)
      }
    })
    
    const conditionMet = evaluateLogicRule({ ...rule, conditionList }, props.formModel, props.formItems)
    
    triggerList.forEach(trigger => {
      if (trigger.formItemId) {
        if (conditionMet) {
          if (trigger.type === 'show') {
            itemVisibility.value[trigger.formItemId] = true
          } else if (trigger.type === 'hide') {
            itemVisibility.value[trigger.formItemId] = false
          }
        } else {
          if (trigger.type === 'show') {
            itemVisibility.value[trigger.formItemId] = false
          } else if (trigger.type === 'hide') {
            itemVisibility.value[trigger.formItemId] = true
          }
        }
      }
    })
  })
  
  props.formItems.forEach(item => {
    if (!controlledItems.has(item.formItemId) && itemVisibility.value[item.formItemId] === undefined) {
      itemVisibility.value[item.formItemId] = true
    }
  })
}

watch(() => props.formModel, () => {
  updateItemVisibility()
}, { deep: true })

watch(() => props.formLogic, () => {
  updateItemVisibility()
}, { deep: true, immediate: true })

updateItemVisibility()

/**
 * 获取可见的表单项列表
 * 过滤掉隐藏的组件，并按sort排序
 * @returns {Array} 可见的表单项数组
 */
const visibleFormItems = computed(() => {
  return props.formItems
    .filter(item => {
      // 排除hideType为true的项
      if (item.hideType) return false
      // 根据逻辑规则控制显示/隐藏
      return itemVisibility.value[item.formItemId] !== false
    })
    .sort((a, b) => {
      // 确保按 sort 排序
      const sortA = a.sort != null ? a.sort : 0
      const sortB = b.sort != null ? b.sort : 0
      return sortA - sortB
    })
})

const getQuestionIndex = (element) => {
  let index = 0
  for (const item of visibleFormItems.value) {
    if (item.type === 'DIVIDER') {
      continue
    }
    index++
    if (item.formItemId === element.formItemId) {
      return index
    }
  }
  return index
}

const uploadFileLists = ref({})
const sliderValues = ref({})

/**
 * 获取上传组件的文件列表
 * 确保返回数组格式，并处理URL转换
 * @param {string} vModel 表单项的vModel
 * @returns {Array} 文件列表数组
 */
const getUploadFileList = (vModel) => {
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
  
  fileList = fileList.map(file => {
    if (file && file.url) {
      if (!file.url.startsWith('http://') && !file.url.startsWith('https://')) {
        file.url = getImageUrl(file.url)
      }
    } else if (file && typeof file === 'string') {
      const fullUrl = getImageUrl(file)
      return {
        url: fullUrl,
        rawUrl: file,
        name: file.split('/').pop() || 'file'
      }
    }
    return file
  })
  
  uploadFileLists.value[vModel] = fileList
  
  return fileList
}

/**
 * 处理上传组件的变化
 * @param {string} vModel 表单项的vModel
 * @param {Object} file 文件对象
 * @param {Array} fileList 文件列表
 */
const handleUploadChange = (vModel, file, fileList) => {
  const files = Array.isArray(fileList) ? fileList : []
  uploadFileLists.value[vModel] = [...files]
  if (vModel in props.formModel) {
    props.formModel[vModel] = [...files]
  } else {
    props.formModel[vModel] = [...files]
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

// 上传URL和Headers
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  return `${baseUrl}/file/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${getToken()}`
  }
})

// 文件上传成功处理
const handleFileUploadSuccess = (response, file, fileList, vModel) => {
  if (response && response.code === 200 && response.data) {
    const fileUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    // 将相对路径转换为完整URL，用于显示
    const fullFileUrl = getImageUrl(fileUrl)
    // 更新文件对象的url（使用完整URL用于显示）
    if (file) {
      file.url = fullFileUrl
      file.response = response
      // 保存原始相对路径到文件对象，用于提交时使用
      file.rawUrl = fileUrl
    }
    // 更新文件列表，确保每个文件都有正确的url
    if (fileList && Array.isArray(fileList)) {
      const updatedFileList = fileList.map(f => {
        // 如果文件已经有url，确保是完整URL
        if (f.url && !f.url.startsWith('http://') && !f.url.startsWith('https://')) {
          f.url = getImageUrl(f.url)
        }
        // 如果是新上传的文件，使用完整URL
        if (f.uid === file?.uid && !f.url) {
          f.url = fullFileUrl
          f.rawUrl = fileUrl
        }
        return f
      })
      uploadFileLists.value[vModel] = [...updatedFileList]
      if (vModel in props.formModel) {
        props.formModel[vModel] = [...updatedFileList]
      } else {
        props.formModel[vModel] = [...updatedFileList]
      }
    }
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response?.message || '文件上传失败')
  }
}

// 图片上传成功处理
const handleImageUploadSuccess = (response, file, fileList, vModel) => {
  if (response && response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    // 将相对路径转换为完整URL，用于显示
    const fullImageUrl = getImageUrl(imageUrl)
    // 更新文件对象的url（使用完整URL用于显示）
    if (file) {
      file.url = fullImageUrl
      file.response = response
      // 保存原始相对路径到文件对象，用于提交时使用
      file.rawUrl = imageUrl
    }
    // 更新文件列表，确保每个文件都有正确的url
    if (fileList && Array.isArray(fileList)) {
      const updatedFileList = fileList.map(f => {
        // 如果文件已经有url，确保是完整URL
        if (f.url && !f.url.startsWith('http://') && !f.url.startsWith('https://')) {
          f.url = getImageUrl(f.url)
        }
        // 如果是新上传的文件，使用完整URL
        if (f.uid === file?.uid && !f.url) {
          f.url = fullImageUrl
          f.rawUrl = imageUrl
        }
        return f
      })
      uploadFileLists.value[vModel] = [...updatedFileList]
      if (vModel in props.formModel) {
        props.formModel[vModel] = [...updatedFileList]
      } else {
        props.formModel[vModel] = [...updatedFileList]
      }
    }
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response?.message || '图片上传失败')
  }
}

// 文件上传失败处理
const handleFileUploadError = () => {
  ElMessage.error('文件上传失败，请重试')
}

// 图片上传失败处理
const handleImageUploadError = (error, file, fileList, vModel) => {
  ElMessage.error('图片上传失败，请重试')
  // 即使上传失败，也要更新文件列表以显示本地预览
  if (fileList && Array.isArray(fileList)) {
    uploadFileLists.value[vModel] = [...fileList]
    if (vModel in props.formModel) {
      props.formModel[vModel] = [...fileList]
    } else {
      props.formModel[vModel] = [...fileList]
    }
  }
}

// 获取文件上传提示文本
const getUploadTipText = (element) => {
  const limit = element.config?.limit || 1
  const maxSizeValue = element.config?.maxSizeValue
  const maxSizeUnit = element.config?.maxSizeUnit || 'MB'
  
  let tipText = ''
  
  // 如果有文件大小限制
  if (maxSizeValue && maxSizeValue > 0) {
    tipText = `只能上传不超过 ${maxSizeValue}${maxSizeUnit} 的文件`
  }
  
  // 添加文件个数限制
  if (limit > 1) {
    if (tipText) {
      tipText += `，最多不超过${limit}个文件`
    } else {
      tipText = `最多不超过${limit}个文件`
    }
  } else if (!tipText) {
    tipText = '最多不超过1个文件'
  }
  
  return tipText
}

const formRef = ref(null)

// 生成表单验证规则（只对可见的组件生成验证规则）
const formRules = computed(() => {
  const rules = {}
  // 使用 visibleFormItems 确保只对可见的组件生成验证规则
  visibleFormItems.value.forEach(item => {
    const itemRules = []
    
    // 需要特殊处理的组件类型（这些组件有专门的必填验证规则）
    const specialRequiredTypes = ['UPLOAD', 'IMAGE_UPLOAD', 'SIGN_PAD', 'RADIO', 'SELECT', 'CASCADER', 'SLIDER']
    
    // 必填规则（通用规则，不适用于需要特殊处理的组件）
    if (item.required && !specialRequiredTypes.includes(item.type)) {
      itemRules.push({
        required: true,
        message: `${item.label}不能为空`,
        trigger: ['blur', 'change']
      })
    }
    
    // 最小长度验证
    if (item.config?.minLength) {
      itemRules.push({
        min: item.config.minLength,
        message: `${item.label}最少需要${item.config.minLength}个字符`,
        trigger: ['blur', 'change']
      })
    }
    
    // 最大长度验证
    if (item.config?.maxLength) {
      itemRules.push({
        max: item.config.maxLength,
        message: `${item.label}最多只能输入${item.config.maxLength}个字符`,
        trigger: ['blur', 'change']
      })
    }
    
    // 数据类型验证（反馈类型）- 仅对单行文本
    if (item.config?.dataType && item.type === 'INPUT') {
      itemRules.push({
        validator: (rule, value, callback) => {
          if (!value || value === '') {
            // 空值由必填规则处理，这里直接通过
            callback()
            return
          }
          
          const valueStr = String(value).trim()
          const dataType = item.config.dataType
          const errorMessage = item.config.dataTypeMessage || `${item.label}格式不正确`
          let isValid = false
          
          switch (dataType) {
            case 'string':
              // 字符串：只要不是空字符串即可
              isValid = valueStr.length > 0
              break
              
            case 'number':
              // 数字：可以是整数或小数
              isValid = /^-?\d+(\.\d+)?$/.test(valueStr)
              break
              
            case 'integer':
              // 整数：必须是整数
              isValid = /^-?\d+$/.test(valueStr)
              break
              
            case 'float':
              // 小数：必须包含小数点
              isValid = /^-?\d+\.\d+$/.test(valueStr)
              break
              
            case 'url':
              // URL地址
              try {
                new URL(valueStr)
                isValid = true
              } catch {
                isValid = false
              }
              break
              
            case 'email':
              // 邮箱地址
              isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valueStr)
              break
              
            case 'phone':
              // 手机号（中国手机号：11位，1开头）
              isValid = /^1[3-9]\d{9}$/.test(valueStr)
              break
              
            default:
              isValid = true
          }
          
          if (!isValid) {
            callback(new Error(errorMessage))
          } else {
            callback()
          }
        },
        trigger: ['blur', 'change']
      })
    }
    
    // 正则表达式验证
    if (item.regList && Array.isArray(item.regList) && item.regList.length > 0) {
      item.regList.forEach(reg => {
        // 确保 pattern 和 message 都存在且不为空
        if (reg.pattern && reg.pattern.trim() !== '') {
          try {
            const regex = new RegExp(reg.pattern)
            itemRules.push({
              pattern: regex,
              message: reg.message && reg.message.trim() !== '' 
                ? reg.message 
                : `${item.label}格式不正确`,
              trigger: ['blur', 'change']
            })
          } catch (e) {
            // 正则表达式错误，跳过该规则
          }
        }
      })
    }
    
    // 重复值检查（仅对单行文本）
    if (item.type === 'INPUT' && item.config?.notRepeat) {
      itemRules.push({
        validator: (rule, value, callback) => {
          // 检查是否与其他表单项的值重复
          const otherItems = props.formItems.filter(
            other => other.vModel !== item.vModel && 
                     other.type === 'INPUT' && 
                     props.formModel[other.vModel] === value &&
                     value !== '' &&
                     value !== null &&
                     value !== undefined
          )
          if (otherItems.length > 0) {
            callback(new Error(`${item.label}不能与其他字段重复`))
          } else {
            callback()
          }
        },
        trigger: ['blur', 'change']
      })
    }
    
    // NUMBER 组件的最小值/最大值验证
    if (item.type === 'NUMBER') {
      if (item.config?.min !== undefined && item.config?.min !== null) {
        itemRules.push({
          validator: (rule, value, callback) => {
            if (value === null || value === undefined || value === '') {
              // 空值由必填规则处理
              callback()
              return
            }
            const numValue = Number(value)
            if (isNaN(numValue) || numValue < item.config.min) {
              callback(new Error(`${item.label}不能小于${item.config.min}`))
            } else {
              callback()
            }
          },
          trigger: ['blur', 'change']
        })
      }
      
      if (item.config?.max !== undefined && item.config?.max !== null) {
        itemRules.push({
          validator: (rule, value, callback) => {
            if (value === null || value === undefined || value === '') {
              // 空值由必填规则处理
              callback()
              return
            }
            const numValue = Number(value)
            if (isNaN(numValue) || numValue > item.config.max) {
              callback(new Error(`${item.label}不能大于${item.config.max}`))
            } else {
              callback()
            }
          },
          trigger: ['blur', 'change']
        })
      }
      
      // NUMBER 组件的正则表达式验证
      if (item.regList && Array.isArray(item.regList) && item.regList.length > 0) {
        item.regList.forEach(reg => {
          if (reg.pattern && reg.pattern.trim() !== '') {
            try {
              const regex = new RegExp(reg.pattern)
              itemRules.push({
                validator: (rule, value, callback) => {
                  if (value === null || value === undefined || value === '') {
                    callback()
                    return
                  }
                  const valueStr = String(value)
                  if (!regex.test(valueStr)) {
                    callback(new Error(reg.message && reg.message.trim() !== '' 
                      ? reg.message 
                      : `${item.label}格式不正确`))
                  } else {
                    callback()
                  }
                },
                trigger: ['blur', 'change']
              })
            } catch (e) {
              // 正则表达式错误，跳过该规则
            }
          }
        })
      }
    }
    
    // CHECKBOX 组件的最少/最多选择数量验证
    if (item.type === 'CHECKBOX') {
      if (item.config?.min !== undefined && item.config?.min !== null) {
        itemRules.push({
          validator: (rule, value, callback) => {
            const selectedCount = Array.isArray(value) ? value.length : 0
            if (selectedCount < item.config.min) {
              callback(new Error(`${item.label}至少需要选择${item.config.min}项`))
            } else {
              callback()
            }
          },
          trigger: ['change']
        })
      }
      
      if (item.config?.max !== undefined && item.config?.max !== null) {
        itemRules.push({
          validator: (rule, value, callback) => {
            const selectedCount = Array.isArray(value) ? value.length : 0
            if (selectedCount > item.config.max) {
              callback(new Error(`${item.label}最多只能选择${item.config.max}项`))
            } else {
              callback()
            }
          },
          trigger: ['change']
        })
      }
    }
    
    // UPLOAD 组件必填验证
    if (item.type === 'UPLOAD' && item.required) {
      itemRules.push({
        validator: (rule, value, callback) => {
          const fileList = Array.isArray(value) ? value : []
          if (fileList.length === 0) {
            callback(new Error(`${item.label}不能为空`))
          } else {
            callback()
          }
        },
        trigger: ['change']
      })
    }
    
    // IMAGE_UPLOAD 组件必填验证
    if (item.type === 'IMAGE_UPLOAD' && item.required) {
      itemRules.push({
        validator: (rule, value, callback) => {
          const fileList = Array.isArray(value) ? value : []
          if (fileList.length === 0) {
            callback(new Error(`${item.label}不能为空`))
          } else {
            callback()
          }
        },
        trigger: ['change']
      })
    }
    
    // SIGN_PAD 组件必填验证
    if (item.type === 'SIGN_PAD' && item.required) {
      itemRules.push({
        validator: (rule, value, callback) => {
          if (!value || value.trim() === '') {
            callback(new Error(`${item.label}不能为空`))
          } else {
            callback()
          }
        },
        trigger: ['change']
      })
    }
    
    // RADIO 组件必填验证
    if (item.type === 'RADIO' && item.required) {
      itemRules.push({
        validator: (rule, value, callback) => {
          if (value === null || value === undefined || value === '') {
            callback(new Error(`${item.label}不能为空`))
          } else {
            callback()
          }
        },
        trigger: ['change']
      })
    }
    
    // SELECT 组件必填验证
    if (item.type === 'SELECT' && item.required) {
      itemRules.push({
        validator: (rule, value, callback) => {
          if (item.config?.multiple) {
            // 多选模式：检查数组是否为空
            const selectedArray = Array.isArray(value) ? value : []
            if (selectedArray.length === 0) {
              callback(new Error(`${item.label}不能为空`))
            } else {
              callback()
            }
          } else {
            // 单选模式：检查值是否为空
            if (value === null || value === undefined || value === '') {
              callback(new Error(`${item.label}不能为空`))
            } else {
              callback()
            }
          }
        },
        trigger: ['change']
      })
    }
    
    // CASCADER 组件必填验证
    if (item.type === 'CASCADER' && item.required) {
      itemRules.push({
        validator: (rule, value, callback) => {
          const selectedArray = Array.isArray(value) ? value : []
          if (selectedArray.length === 0) {
            callback(new Error(`${item.label}不能为空`))
          } else {
            callback()
          }
        },
        trigger: ['change']
      })
    }
    
    // SLIDER 组件必填验证
    if (item.type === 'SLIDER' && item.required) {
      itemRules.push({
        validator: (rule, value, callback) => {
          if (value === null || value === undefined || value === '') {
            callback(new Error(`${item.label}不能为空`))
          } else {
            callback()
          }
        },
        trigger: ['change']
      })
    }
    
    // DATE 组件的正则表达式验证
    if (item.type === 'DATE' && item.regList && Array.isArray(item.regList) && item.regList.length > 0) {
      item.regList.forEach(reg => {
        if (reg.pattern && reg.pattern.trim() !== '') {
          try {
            const regex = new RegExp(reg.pattern)
            itemRules.push({
              validator: (rule, value, callback) => {
                if (value === null || value === undefined || value === '') {
                  callback()
                  return
                }
                const valueStr = String(value)
                if (!regex.test(valueStr)) {
                  callback(new Error(reg.message && reg.message.trim() !== '' 
                    ? reg.message 
                    : `${item.label}格式不正确`))
                } else {
                  callback()
                }
              },
              trigger: ['blur', 'change']
            })
          } catch (e) {
            // 正则表达式错误，跳过该规则
          }
        }
      })
    }
    
    if (itemRules.length > 0) {
      rules[item.vModel] = itemRules
    }
  })
  return rules
})

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

/**
 * 获取激活状态的背景色
 * 将主题色转换为rgba格式，降低透明度
 * @returns {string} rgba颜色值
 */
const getActiveBgColor = () => {
  const themeColor = getThemeColor()
  if (themeColor.startsWith('#')) {
    const r = parseInt(themeColor.slice(1, 3), 16)
    const g = parseInt(themeColor.slice(3, 5), 16)
    const b = parseInt(themeColor.slice(5, 7), 16)
    return `rgba(${r}, ${g}, ${b}, 0.1)`
  }
  return '#ecf5ff'
}

/**
 * 暴露验证方法给父组件
 * 提供表单验证、字段验证、重置表单、清除验证等功能
 */
defineExpose({
  validate: () => {
    if (!formRef.value) return Promise.resolve(true)
    return formRef.value.validate()
  },
  validateField: (props) => {
    if (!formRef.value) return Promise.resolve(true)
    return formRef.value.validateField(props)
  },
  resetFields: () => {
    if (!formRef.value) return
    formRef.value.resetFields()
  },
  clearValidate: (props) => {
    if (!formRef.value) return
    formRef.value.clearValidate(props)
  }
})

/**
 * 验证输入
 * 执行正则验证和长度验证
 * @param {Object} element 表单项元素
 */
const validateInput = (element) => {
  if (!element || props.previewMode) return
  
  const value = props.formModel[element.vModel]
  const valueStr = String(value || '')
  
  // 验证最小长度（仅对单行文本）
  if (element.type === 'INPUT' && element.config?.minLength && valueStr.length < element.config.minLength) {
    ElMessage.warning(`${element.label} 最少需要 ${element.config.minLength} 个字符`)
    return false
  }
  
  // 验证最大长度
  if (element.config?.maxLength && valueStr.length > element.config.maxLength) {
    ElMessage.warning(`${element.label} 最多只能输入 ${element.config.maxLength} 个字符`)
    return false
  }
  
  // 验证数据类型（反馈类型）- 仅对单行文本
  if (element.type === 'INPUT' && element.config?.dataType && valueStr.length > 0) {
    const dataType = element.config.dataType
    const errorMessage = element.config.dataTypeMessage || `${element.label}格式不正确`
    let isValid = false
    
    switch (dataType) {
      case 'string':
        // 字符串：只要不是空字符串即可
        isValid = valueStr.length > 0
        break
        
      case 'number':
        // 数字：可以是整数或小数
        isValid = /^-?\d+(\.\d+)?$/.test(valueStr)
        break
        
      case 'integer':
        // 整数：必须是整数
        isValid = /^-?\d+$/.test(valueStr)
        break
        
      case 'float':
        // 小数：必须包含小数点
        isValid = /^-?\d+\.\d+$/.test(valueStr)
        break
        
      case 'url':
        // URL地址
        try {
          new URL(valueStr)
          isValid = true
        } catch {
          isValid = false
        }
        break
        
      case 'email':
        // 邮箱地址
        isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valueStr)
        break
        
      case 'phone':
        // 手机号（中国手机号：11位，1开头）
        isValid = /^1[3-9]\d{9}$/.test(valueStr)
        break
        
      default:
        isValid = true
    }
    
    if (!isValid) {
      ElMessage.warning(errorMessage)
      return false
    }
  }
  
  // 验证正则表达式
  if (element.regList && Array.isArray(element.regList) && element.regList.length > 0) {
    for (const reg of element.regList) {
      // 确保 pattern 存在且不为空
      if (reg.pattern && reg.pattern.trim() !== '') {
        try {
          const regex = new RegExp(reg.pattern)
          if (!regex.test(valueStr)) {
            const errorMsg = reg.message && reg.message.trim() !== ''
              ? reg.message
              : `${element.label} 格式不正确`
            ElMessage.warning(errorMsg)
            return false
          }
        } catch (e) {
          // 正则表达式错误，跳过该规则
        }
      }
    }
  }
  
  return true
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

.form-label-wrapper {
  margin-bottom: 8px;
  
  .form-label {
    font-weight: 500;
    font-size: 14px;
    color: #606266;
    display: block;
    
    .question-number {
      color: v-bind('getThemeColor()');
    }
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
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
  
  .el-radio {
    margin-right: 0;
    margin-bottom: 0;
    width: 100%;
    
    .el-radio__label {
      text-align: left;
    }
    
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
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
  
  .el-checkbox {
    margin-right: 0;
    margin-bottom: 0;
    width: 100%;
    
    .el-checkbox__label {
      text-align: left;
    }
    
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
  
  .image-select-radio-group,
  .image-select-checkbox-group {
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
      
      :deep(.el-radio__input.is-checked .el-radio__inner) {
        background-color: #409eff;
        border-color: #409eff;
      }
      
      :deep(.el-radio__input.is-checked.is-disabled .el-radio__inner) {
        background-color: #409eff;
        border-color: #409eff;
        opacity: 1;
      }
      
      :deep(.el-radio__inner) {
        width: 18px;
        height: 18px;
      }
      
      :deep(.el-radio__inner::after) {
        width: 6px;
        height: 6px;
        background-color: #ffffff;
      }
      
      :deep(.el-radio__input.is-disabled.is-checked .el-radio__inner::after) {
        background-color: #ffffff;
      }
      
      :deep(.el-radio__label) {
        padding-left: 0;
        width: 100%;
      }
    }
    
    .image-select-checkbox {
      width: 100%;
      height: 100%;
      margin: 0;
      position: relative;
      
      :deep(.el-checkbox__input) {
        position: absolute;
        top: 4px;
        right: 4px;
        z-index: 10;
      }
      
      :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
        background-color: #409eff;
        border-color: #409eff;
      }
      
      :deep(.el-checkbox__input.is-checked.is-disabled .el-checkbox__inner) {
        background-color: #409eff;
        border-color: #409eff;
        opacity: 1;
      }
      
      :deep(.el-checkbox__inner) {
        width: 18px;
        height: 18px;
      }
      
      :deep(.el-checkbox__inner::after) {
        border-color: #ffffff;
      }
      
      :deep(.el-checkbox__input.is-disabled.is-checked .el-checkbox__inner::after) {
        border-color: #ffffff;
      }
      
      :deep(.el-checkbox__label) {
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
