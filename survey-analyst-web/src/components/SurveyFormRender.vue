
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
        v-if="element.type !== 'DIVIDER'"
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
            :on-success="handleFileUploadSuccess"
            :on-error="handleFileUploadError"
            :on-exceed="() => ElMessage.warning(`最多只能上传${element.config?.limit || 1}个文件`)"
            @change="handleUploadChange(element.vModel, $event)"
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
          action="#"
          :auto-upload="false"
          :list-type="element.config?.listType || 'picture-card'"
          :limit="element.config?.limit || 9"
          :accept="element.config?.accept || 'image/*'"
          :on-exceed="() => ElMessage.warning(`最多只能上传${element.config?.limit || 9}张图片`)"
          @change="handleUploadChange(element.vModel, $event)"
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

        <!-- 默认：单行文本 -->
        <el-input
          v-else
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :style="getInputStyle()"
          class="theme-input"
        />
        </el-form-item>

        <!-- 图片展示独立渲染 -->
        <el-image
          v-if="element.type === 'IMAGE'"
          :src="element.config?.imageUrl || ''"
          :fit="element.config?.fit || 'cover'"
          style="width: 100%"
          :preview-src-list="element.config?.previewList || []"
        />

        <!-- 手写签名 -->
        <SignPad
          v-else-if="element.type === 'SIGN_PAD'"
          v-model="formModel[element.vModel]"
          :width="600"
          :height="element.config?.height || 300"
          :background-color="'#ffffff'"
          :pen-color="element.config?.penColor || '#000000'"
          :disabled="element.disabled || previewMode"
        />

        <!-- 图片轮播独立渲染 -->
        <div
          v-if="element.type === 'IMAGE_CAROUSEL'"
          class="image-carousel-wrapper"
        >
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

        <!-- 文字描述独立渲染 -->
        <!-- eslint-disable-next-line vue/no-v-html -->
        <div
          v-else-if="element.type === 'DESC_TEXT'"
          class="desc-text-wrapper"
          :style="{
            textAlign: element.config?.textAlign || 'left',
            fontSize: (element.config?.fontSize || 14) + 'px',
            color: element.config?.color || '#606266'
          }"
          v-html="element.config?.content || ''"
        />
        
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
import { ref, computed } from 'vue'
import { Upload, Plus, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import SignPad from '@/components/SignPad.vue'
import { getToken } from '@/utils/auth'

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

// 过滤掉隐藏的组件
const visibleFormItems = computed(() => {
  return props.formItems.filter(item => !item.hideType)
})

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
const handleFileUploadSuccess = (response, file) => {
  if (response && response.code === 200 && response.data) {
    const fileUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    // 更新文件对象的url
    if (file) {
      file.url = fileUrl
      file.response = response
    }
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response?.message || '文件上传失败')
  }
}

// 文件上传失败处理
const handleFileUploadError = () => {
  ElMessage.error('文件上传失败，请重试')
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

// 生成表单验证规则
const formRules = computed(() => {
  const rules = {}
  props.formItems.forEach(item => {
    const itemRules = []
    
    // 必填规则
    if (item.required) {
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

// 验证输入（正则验证和长度验证）
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
