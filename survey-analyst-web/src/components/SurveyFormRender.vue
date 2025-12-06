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
        :label="element.label"
        :prop="element.vModel"
        :required="element.required"
      >
        <template #label>
          <span class="form-label">
            <span v-if="element.required" class="required-mark">*</span>
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
        />

        <!-- 数字 -->
        <el-input-number
          v-else-if="element.type === 'NUMBER'"
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          style="width: 100%"
        />

        <!-- 单选框 -->
        <el-radio-group
          v-else-if="element.type === 'RADIO'"
          v-model="formModel[element.vModel]"
          :disabled="element.disabled || previewMode"
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
          :disabled="element.disabled || previewMode"
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
          :disabled="element.disabled || previewMode"
          style="width: 100%"
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
        />

        <!-- 日期选择 -->
        <el-date-picker
          v-else-if="element.type === 'DATE'"
          v-model="formModel[element.vModel]"
          type="date"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          style="width: 100%"
        />

        <!-- 文件上传 -->
        <el-upload
          v-else-if="element.type === 'UPLOAD'"
          v-model:file-list="formModel[element.vModel]"
          :disabled="element.disabled || previewMode"
          action="#"
          :auto-upload="false"
          :limit="element.config?.limit || 1"
        >
          <el-button type="primary" :disabled="previewMode">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
        </el-upload>

        <!-- 默认：单行文本 -->
        <el-input
          v-else
          v-model="formModel[element.vModel]"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          :readonly="element.readonly || previewMode"
        />
      </el-form-item>
    </div>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'
import { Upload } from '@element-plus/icons-vue'

defineProps({
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
  }
})

const formRef = ref(null)
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
  .required-mark {
    color: #f56c6c;
    margin-right: 4px;
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
  }
}
</style>
