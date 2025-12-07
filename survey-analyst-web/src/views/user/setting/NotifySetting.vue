<template>
  <div class="notify-setting-view">
    <p class="setting-title">
      通知设置
      <span class="setting-desc">
        自定义表单提交后的通知，支持邮件等
      </span>
    </p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">发邮件提醒我</p>
        <el-switch v-model="form.emailNotify" />
      </div>
      <el-form-item
        v-if="form.emailNotify"
        prop="newWriteNotifyEmail"
        :rules="[
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
        ]"
      >
        <el-input
          v-model="form.newWriteNotifyEmail"
          placeholder="多个邮箱用 ; 隔开"
          style="width: 400px"
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
  emailNotify: false,
  newWriteNotifyEmail: ''
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
      if (settings.emailNotify !== undefined) {
        form.value.emailNotify = settings.emailNotify
      }
      if (settings.newWriteNotifyEmail) {
        form.value.newWriteNotifyEmail = settings.newWriteNotifyEmail
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
          emailNotify: form.value.emailNotify,
          newWriteNotifyEmail: form.value.newWriteNotifyEmail
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
.notify-setting-view {
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
</style>

