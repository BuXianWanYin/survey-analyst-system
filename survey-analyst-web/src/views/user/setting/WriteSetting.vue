<template>
  <div class="write-setting-view">
    <p class="setting-title">
      回收设置
      <span class="setting-desc">
        自定义表单收集时间，次数，以及限制填写环境等
      </span>
    </p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">
          密码填写
          <el-tooltip content="开启后需要输入密码才能进入填写页面" placement="top">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </p>
        <el-switch v-model="form.passwordWriteStatus" />
      </div>
      <el-form-item
        v-if="form.passwordWriteStatus"
        prop="writePassword"
        :rules="[{ required: true, message: '请输入填写密码', trigger: 'blur' }]"
      >
        <el-input
          v-model="form.writePassword"
          placeholder="请输入填写密码"
          style="width: 300px"
        />
      </el-form-item>
      
      <div class="setting-item">
        <p class="label">每个IP答题次数限制</p>
        <el-switch v-model="form.ipWriteCountLimitStatus" />
      </div>
      
      <div class="setting-item">
        <p class="label">设定答题次数上限</p>
        <el-switch v-model="form.totalWriteCountLimitStatus" />
      </div>
      <div v-if="form.totalWriteCountLimitStatus">
        <el-input-number
          v-model="form.totalWriteCountLimit"
          :min="1"
          style="width: 200px"
        />
        <el-input
          v-model="form.totalWriteCountLimitText"
          class="mt10"
          placeholder="该表单收集数据已经达到上限，有问题请与表单发布者联系"
        />
      </div>
      
      <div class="setting-item">
        <p class="label">允许填写时间</p>
        <el-switch v-model="form.writeInterviewTimeStatus" />
      </div>
      <div v-if="form.writeInterviewTimeStatus">
        <el-date-picker
          v-model="form.writeInterviewDateTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 400px"
        />
        <el-input
          v-model="form.writeInterviewTimeText"
          class="mt10"
          placeholder="此表单设置了访问时间，当前时间无法访问，有问题请与表单发布者联系"
        />
      </div>
      
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
import { QuestionFilled } from '@element-plus/icons-vue'
import { formApi } from '@/api'

const route = useRoute()

const formRef = ref(null)
const surveyId = ref(null)
const form = ref({
  passwordWriteStatus: false,
  writePassword: '',
  ipWriteCountLimitStatus: false,
  totalWriteCountLimitStatus: false,
  totalWriteCountLimit: 1,
  totalWriteCountLimitText: '',
  writeInterviewTimeStatus: false,
  writeInterviewDateTimeRange: [],
  writeInterviewTimeText: ''
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
      Object.assign(form.value, settings)
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

