<template>
  <div class="submit-setting-view">
    <p class="setting-title">
      提交设置
      <span class="setting-desc">
        自定义表单提交成功后的提示内容，以及可以跳转指定链接
      </span>
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
      >
        <el-input
          v-model="form.submitJumpUrl"
          placeholder="https://example.com"
        />
      </el-form-item>
      <div class="submit-btn">
        <el-button type="primary" @click="handleSave">保存设置</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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

// 加载设置
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

// 保存设置
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
  }
}

.submit-btn {
  margin-top: 30px;
}

.mt20 {
  margin-top: 20px;
}
</style>

