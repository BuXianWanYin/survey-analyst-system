<template>
  <div class="readonly-form-item">
    <!-- 单行文本 -->
    <div v-if="item.type === 'INPUT'" class="readonly-input">
      {{ value || '-' }}
    </div>
    
    <!-- 多行文本 -->
    <div v-else-if="item.type === 'TEXTAREA'" class="readonly-textarea">
      {{ value || '-' }}
    </div>
    
    <!-- 数字 -->
    <div v-else-if="item.type === 'NUMBER'" class="readonly-input">
      {{ value !== null && value !== undefined ? value : '-' }}
    </div>
    
    <!-- 单选框 -->
    <div v-else-if="item.type === 'RADIO'" class="readonly-radio-group">
      <label
        v-for="opt in (item.config?.options || [])"
        :key="opt.value"
        class="readonly-radio"
        :class="{ checked: opt.value === value }"
      >
        <el-radio
          :model-value="opt.value === value"
          :label="opt.value"
          disabled
        >
          {{ opt.label }}
        </el-radio>
      </label>
      <div v-if="!item.config?.options || item.config.options.length === 0" class="readonly-input">
        {{ value || '-' }}
      </div>
    </div>
    
    <!-- 多选框 -->
    <div v-else-if="item.type === 'CHECKBOX'" class="readonly-checkbox-group">
      <label
        v-for="opt in (item.config?.options || [])"
        :key="opt.value"
        class="readonly-checkbox"
        :class="{ checked: isSelected(opt.value) }"
      >
        <el-checkbox
          :model-value="isSelected(opt.value)"
          :label="opt.value"
          disabled
        >
          {{ opt.label }}
        </el-checkbox>
      </label>
      <div v-if="!item.config?.options || item.config.options.length === 0" class="readonly-input">
        {{ formatArrayValue(value) }}
      </div>
    </div>
    
    <!-- 下拉框 -->
    <div v-else-if="item.type === 'SELECT'" class="readonly-select">
      <el-select
        :model-value="value"
        disabled
        style="width: 100%"
      >
        <el-option
          v-for="opt in (item.config?.options || [])"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <div v-if="!item.config?.options || item.config.options.length === 0" class="readonly-input">
        {{ value || '-' }}
      </div>
    </div>
    
    <!-- 日期 -->
    <div v-else-if="item.type === 'DATE'" class="readonly-input">
      {{ value || '-' }}
    </div>
    
    <!-- 评分 -->
    <div v-else-if="item.type === 'RATE'" class="readonly-rate">
      <el-rate
        :model-value="value || 0"
        :max="item.config?.max || 5"
        disabled
      />
      <span style="margin-left: 10px;">({{ value || 0 }}分)</span>
    </div>
    
    <!-- 滑块 -->
    <div v-else-if="item.type === 'SLIDER'" class="readonly-slider">
      <div style="display: flex; align-items: center; gap: 10px;">
        <span>{{ item.config?.min || 0 }}</span>
        <el-slider
          :model-value="value || item.config?.min || 0"
          :min="item.config?.min || 0"
          :max="item.config?.max || 100"
          disabled
          style="flex: 1;"
        />
        <span>{{ item.config?.max || 100 }}</span>
        <span style="font-weight: 500; min-width: 40px;">{{ value || item.config?.min || 0 }}</span>
      </div>
    </div>
    
    <!-- 级联选择 -->
    <div v-else-if="item.type === 'CASCADER'" class="readonly-cascader">
      <el-cascader
        :model-value="Array.isArray(value) ? value : []"
        :options="item.config?.options || []"
        disabled
        style="width: 100%"
      />
      <div v-if="!item.config?.options || item.config.options.length === 0" class="readonly-input">
        {{ formatCascaderValue(value) }}
      </div>
    </div>
    
    <!-- 手写签名 -->
    <div v-else-if="item.type === 'SIGN_PAD'" class="signature-image">
      <el-image
        v-if="value && typeof value === 'string' && value.startsWith('data:image')"
        :src="value"
        fit="contain"
        style="max-width: 300px; max-height: 200px; border: 1px solid #dcdfe6; border-radius: 4px;"
        :preview-src-list="[value]"
      />
      <div v-else class="readonly-input">-</div>
    </div>
    
    <!-- 文件上传 -->
    <div v-else-if="item.type === 'UPLOAD'" class="file-list">
      <div
        v-for="(file, idx) in (Array.isArray(value) ? value : (value ? [value] : []))"
        :key="idx"
        class="file-item"
      >
        <el-link
          v-if="file.url || file"
          :href="file.url || file"
          target="_blank"
          type="primary"
        >
          {{ file.name || `文件${idx + 1}` }}
          <el-icon style="margin-left: 4px;"><Download /></el-icon>
        </el-link>
        <span v-else>{{ file.name || file }}</span>
      </div>
      <div v-if="!value || (Array.isArray(value) && value.length === 0)" class="readonly-input">-</div>
    </div>
    
    <!-- 图片上传 -->
    <div v-else-if="item.type === 'IMAGE_UPLOAD'" class="image-list">
      <el-image
        v-for="(img, idx) in (Array.isArray(value) ? value : (value ? [value] : []))"
        :key="idx"
        :src="getImageUrl(img.url || img)"
        fit="cover"
        style="width: 100px; height: 100px; margin-right: 8px; margin-bottom: 8px; border: 1px solid #dcdfe6; border-radius: 4px;"
        :preview-src-list="Array.isArray(value) ? value.map(i => getImageUrl(i.url || i)) : [getImageUrl(value.url || value)]"
      />
      <div v-if="!value || (Array.isArray(value) && value.length === 0)" class="readonly-input">-</div>
    </div>
    
    <!-- 默认 -->
    <div v-else class="readonly-input">
      {{ value || '-' }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  value: {
    type: [String, Number, Array, Object],
    default: null
  }
})

// 判断多选框是否选中
const isSelected = (optionValue) => {
  if (!props.value) return false
  if (Array.isArray(props.value)) {
    return props.value.includes(optionValue)
  }
  return props.value === optionValue
}

// 格式化数组值
const formatArrayValue = (value) => {
  if (!value) return '-'
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  return String(value)
}

// 格式化级联选择值
const formatCascaderValue = (value) => {
  if (!value || !Array.isArray(value) || value.length === 0) {
    return '-'
  }
  const options = props.item.config?.options || []
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
</script>

<style lang="scss" scoped>
.readonly-form-item {
  width: 100%;
}

.readonly-input,
.readonly-textarea {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #303133;
  word-break: break-all;
  line-height: 1.6;
}

.readonly-textarea {
  white-space: pre-wrap;
  min-height: 60px;
}

.readonly-radio-group,
.readonly-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
  
  .readonly-radio,
  .readonly-checkbox {
    display: flex;
    align-items: center;
    cursor: default;
    width: 100%;
    
    :deep(.el-radio__label),
    :deep(.el-checkbox__label) {
      text-align: left;
    }
    
    &.checked {
      :deep(.el-radio__input.is-checked .el-radio__inner),
      :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
        background-color: #409eff;
        border-color: #409eff;
      }
    }
  }
}

.readonly-select {
  :deep(.el-input__wrapper) {
    background-color: #f5f7fa;
    cursor: not-allowed;
  }
}

.readonly-rate {
  display: flex;
  align-items: center;
}

.readonly-slider {
  padding: 10px 0;
}

.readonly-cascader {
  :deep(.el-input__wrapper) {
    background-color: #f5f7fa;
    cursor: not-allowed;
  }
}

.signature-image {
  margin-top: 8px;
}

.file-list {
  .file-item {
    margin-bottom: 8px;
  }
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>

