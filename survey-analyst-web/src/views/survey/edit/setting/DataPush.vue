<template>
  <div class="data-push-view">
    <p class="setting-title">
      数据推送
      <span class="setting-desc">
        将数据以JSON格式推送给三方系统
      </span>
    </p>
    <el-divider />
    
    <el-alert
      :closable="false"
      type="info"
      show-icon
      style="margin-bottom: 20px"
    >
      <template #default>
        <p>数据提交后，将向填写的webhook地址发送 JSON 格式的填写结果，content-type为json。</p>
        <p>该服务器需要返回200状态作为响应。否则平台认为出现异常，最多重试3次。</p>
      </template>
    </el-alert>
    
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">开启数据推送</p>
        <el-switch v-model="form.enabled" />
      </div>
      
      <template v-if="form.enabled">
        <el-form-item
          label="请求类型"
          prop="requestType"
          :rules="[{ required: true, message: '请选择请求类型', trigger: 'blur' }]"
        >
          <el-select v-model="form.requestType" placeholder="请选择请求类型" style="width: 200px">
            <el-option label="POST" value="POST" />
            <el-option label="GET" value="GET" />
          </el-select>
        </el-form-item>
        
        <el-form-item
          label="推送地址"
          prop="url"
          :rules="[
            { required: true, message: '请输入推送地址', trigger: 'blur' },
            { type: 'url', message: '请输入正确的url地址', trigger: ['blur', 'change'] }
          ]"
        >
          <el-input
            v-model="form.url"
            placeholder="请输入推送的数据的API地址"
            style="width: 500px"
          />
        </el-form-item>
        
        <el-form-item label="事件类型">
          <el-select
            v-model="form.eventTypes"
            multiple
            collapse-tags
            placeholder="请选择事件类型"
            style="width: 500px"
          >
            <el-option label="表单数据新增" value="form_data_add" />
            <el-option label="表单数据修改" value="form_data_update" />
            <el-option label="表单数据删除" value="form_data_delete" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
          <el-button @click="handleSendTest">发送测试</el-button>
        </el-form-item>
      </template>
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
  enabled: false,
  requestType: 'POST',
  url: '',
  eventTypes: []
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
      if (settings.dataPush) {
        Object.assign(form.value, settings.dataPush)
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
        const res = await formApi.getFormSetting(surveyId.value)
        let settings = {}
        if (res.code === 200 && res.data && res.data.settings) {
          settings = res.data.settings
        }
        settings.dataPush = form.value
        
        const saveRes = await formApi.saveFormSetting(surveyId.value, settings)
        if (saveRes.code === 200) {
          ElMessage.success('保存成功')
          await loadSetting()
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

// 发送测试
const handleSendTest = async () => {
  if (!form.enabled) {
    ElMessage.warning('请先开启数据推送')
    return
  }
  
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 实现测试推送功能
        ElMessage.info('测试推送功能开发中...')
      } catch (error) {
        ElMessage.error('发送测试失败')
      }
    }
  })
}

onMounted(() => {
  loadSetting()
})
</script>

<style lang="scss" scoped>
.data-push-view {
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
</style>

