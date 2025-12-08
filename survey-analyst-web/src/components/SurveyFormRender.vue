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
        :required="element.required"
      >
        <template #label>
          <span class="form-label">
            <span v-if="showNumber" class="question-number">{{ getQuestionIndex(element) }}. </span>
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

        <!-- 图片上传 -->
        <el-upload
          v-else-if="element.type === 'IMAGE_UPLOAD'"
          v-model:file-list="formModel[element.vModel]"
          :disabled="element.disabled || previewMode"
          action="#"
          :auto-upload="false"
          list-type="picture-card"
          :limit="element.config?.limit || 9"
        >
          <el-icon v-if="!previewMode"><Plus /></el-icon>
        </el-upload>

        <!-- 滑块 -->
        <el-slider
          v-else-if="element.type === 'SLIDER'"
          v-model="formModel[element.vModel]"
          :min="element.config?.min || 0"
          :max="element.config?.max || 100"
          :step="element.config?.step || 1"
          :disabled="element.disabled || previewMode"
        />

        <!-- 级联选择 -->
        <el-cascader
          v-else-if="element.type === 'CASCADER'"
          v-model="formModel[element.vModel]"
          :options="element.config?.options || []"
          :placeholder="element.placeholder"
          :disabled="element.disabled || previewMode"
          style="width: 100%"
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
          <div
            v-for="(option, idx) in element.config?.options || []"
            :key="idx"
            class="image-select-item"
            :class="{ active: formModel[element.vModel] === option.value }"
            @click="!previewMode && !element.disabled && (formModel[element.vModel] = option.value)"
          >
            <el-image
              v-if="option.image"
              :src="option.image"
              fit="cover"
              class="image-select-img"
            />
            <span class="image-select-label">{{ option.label }}</span>
          </div>
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
import { ref } from 'vue'
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
  .question-number {
    color: #409EFF;
    font-weight: 500;
    margin-right: 4px;
  }

  .required-mark {
    color: #f56c6c;
    margin-right: 4px;
  }
}

// 图片选择样式
.image-select-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  
  .image-select-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 8px;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    min-width: 100px;
    
    &.active {
      border-color: #409eff;
      background-color: #ecf5ff;
    }
    
    &:hover:not(.disabled) {
      border-color: #409eff;
    }
    
    .image-select-img {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      margin-bottom: 8px;
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
