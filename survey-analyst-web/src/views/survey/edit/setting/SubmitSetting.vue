<template>
  <div class="submit-setting-view">
    <p class="setting-title">
      提交设置
    </p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">提交后显示方式</p>
        <el-radio-group v-model="form.submitShowType">
          <el-radio :label="1">系统默认提示</el-radio>
          <el-radio :label="2">自定义文案</el-radio>
        </el-radio-group>
      </div>
      <div v-if="form.submitShowType === 2" class="mt20">
        <el-input
          v-model="form.submitShowCustomPageContent"
          placeholder="请输入自定义提示内容"
        />
      </div>
      <div class="setting-item">
        <p class="label">提交后跳转网页地址</p>
        <el-switch v-model="form.submitJump" />
      </div>
      <el-form-item
        v-if="form.submitJump"
        prop="submitJumpUrl"
        :rules="[
          { required: true, message: '请输入跳转地址', trigger: 'blur' },
          { type: 'url', message: '请输入正确的url地址', trigger: ['blur', 'change'] }
        ]"
        class="jump-url-item"
      >
        <el-input
          v-model="form.submitJumpUrl"
          placeholder="https://example.com"
        />
      </el-form-item>
      <div class="submit-btn">
        <el-button :icon="Check" type="primary" @click="handleSave">保存设置</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
/**
 * 提交设置组件
 * 功能：配置问卷提交后的显示方式和跳转设置，支持系统默认提示、自定义文案、提交后跳转网页等功能
 */

import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { formApi } from '@/api'

const route = useRoute()

const formRef = ref(null)
const surveyId = ref(null)
const form = ref({
  submitShowType: 1,
  submitShowCustomPageContent: '提交成功',
  submitJump: false,
  submitJumpUrl: ''
})

const rules = {}

/**
 * 加载提交设置
 * 从后端加载问卷的提交相关设置（提交后显示内容、跳转等）
 */
const loadSetting = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)

  try {
    const res = await formApi.getFormSetting(surveyId.value)
    if (res.code === 200 && res.data && res.data.settings) {
      const settings = res.data.settings
      if (settings.submitShowType !== undefined) {
        form.value.submitShowType = settings.submitShowType
      }
      if (settings.submitShowCustomPageContent) {
        form.value.submitShowCustomPageContent = settings.submitShowCustomPageContent
      }
      if (settings.submitJump !== undefined) {
        form.value.submitJump = settings.submitJump
      }
      if (settings.submitJumpUrl) {
        form.value.submitJumpUrl = settings.submitJumpUrl
      }
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

/**
 * 保存提交设置
 * 验证表单后保存提交相关设置到后端
 */
const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const settings = {
          submitShowType: form.value.submitShowType,
          submitShowCustomPageContent: form.value.submitShowCustomPageContent,
          submitJump: form.value.submitJump,
          submitJumpUrl: form.value.submitJumpUrl
        }
        const res = await formApi.saveFormSetting(surveyId.value, settings)
        if (res.code === 200) {
          ElMessage.success('保存成功')
          await loadSetting()
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

onMounted(() => {
  loadSetting()
})
</script>

<style lang="scss" scoped>
.submit-setting-view {
  padding: 20px;
}

.setting-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  
  .setting-desc {
    font-size: 12px;
    color: #909399;
    margin-left: 10px;
  }
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  .label {
    width: 200px;
    margin: 0;
    font-size: 14px;
    line-height: 32px;
    white-space: nowrap;
    flex-shrink: 0;
  }
}

:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
}

:deep(.el-radio) {
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
}

:deep(.el-radio__label) {
  font-size: 14px;
  padding-left: 6px;
}

.submit-btn {
  margin-top: 30px;
}

.mt20 {
  margin-top: 20px;
}

.jump-url-item {
  margin-top: 10px;
  margin-bottom: 20px;
}

.jump-url-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
}

@media (max-width: 768px) {
  .submit-setting-view {
    padding: 15px 10px;
  }

  .setting-title {
    font-size: 13px;
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    
    .label {
      width: 100%;
      line-height: 1.5;
      font-size: 13px;
    }
  }

  :deep(.el-radio-group) {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  :deep(.el-radio) {
    margin-right: 0;
  }
}
</style>

