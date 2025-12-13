<template>
  <div class="write-setting-view">
    <p class="setting-title">
      回收设置
    </p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">每个IP答题次数限制</p>
        <el-switch v-model="form.ipWriteCountLimitStatus" />
      </div>
      
      <div class="submit-btn">
        <el-button :icon="Check" type="primary" @click="handleSave">保存设置</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
/**
 * 填写设置组件
 * 功能：配置问卷的填写限制设置，包括每个IP答题次数限制等功能
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
  ipWriteCountLimitStatus: false
})

const rules = {}

/**
 * 加载填写设置
 * 从后端加载问卷的填写相关设置（IP限制、设备限制等）
 */
const loadSetting = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)

  try {
    const res = await formApi.getFormSetting(surveyId.value)
    if (res.code === 200 && res.data && res.data.settings) {
      const settings = res.data.settings
      Object.assign(form.value, settings)
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

/**
 * 保存填写设置
 * 验证表单后保存填写相关设置到后端
 */
const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await formApi.saveFormSetting(surveyId.value, form.value)
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
.write-setting-view {
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
    display: flex;
    align-items: center;
    gap: 5px;
  }
}

.submit-btn {
  margin-top: 30px;
}

.mt10 {
  margin-top: 10px;
}
</style>

