<template>
  <div class="notify-setting-view">
    <p class="setting-title">
      通知设置
    </p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">发邮件提醒我</p>
        <el-select
          v-model="form.selectedEmail"
          placeholder="请选择邮箱"
          style="width: 400px"
          @change="handleEmailChange"
        >
          <el-option
            v-for="email in emailList"
            :key="email"
            :label="email"
            :value="email"
          />
        </el-select>
        <el-button
          :icon="Plus"
          type="primary"
          style="margin-left: 10px"
          @click="showAddEmailDialog = true"
        >
          添加邮箱
        </el-button>
      </div>
      
      <div class="email-list" v-if="emailList.length > 0">
        <div
          v-for="(email, index) in emailList"
          :key="index"
          class="email-item"
        >
          <span>{{ email }}</span>
          <el-button
            :icon="Delete"
            type="danger"
            size="small"
            text
            @click="removeEmail(index)"
          >
            删除
          </el-button>
        </div>
      </div>
      
      <div class="submit-btn">
        <el-button :icon="Check" type="primary" @click="handleSave">保存设置</el-button>
      </div>
    </el-form>

    <!-- 添加邮箱对话框 -->
    <el-dialog
      v-model="showAddEmailDialog"
      title="添加邮箱"
      width="500px"
      @close="resetEmailForm"
    >
      <el-form
        ref="emailFormRef"
        :model="emailForm"
        :rules="emailRules"
        label-width="80px"
      >
        <el-form-item label="邮箱地址" prop="email">
          <el-input
            v-model="emailForm.email"
            placeholder="请输入邮箱地址"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :icon="Close" @click="showAddEmailDialog = false">取消</el-button>
        <el-button :icon="Check" type="primary" @click="handleAddEmail">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Check, Close } from '@element-plus/icons-vue'
import { formApi } from '@/api'

const route = useRoute()

const formRef = ref(null)
const emailFormRef = ref(null)
const surveyId = ref(null)
const showAddEmailDialog = ref(false)
const emailList = ref([])
const form = ref({
  selectedEmail: ''
})

const emailForm = ref({
  email: ''
})

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ]
}

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
      if (settings.newWriteNotifyEmail) {
        // 将邮箱字符串转换为数组
        emailList.value = settings.newWriteNotifyEmail.split(';').filter(e => e.trim())
        if (emailList.value.length > 0) {
          form.value.selectedEmail = emailList.value[0]
        }
      }
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

// 添加邮箱
const handleAddEmail = async () => {
  if (!emailFormRef.value) return
  
  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      const email = emailForm.value.email.trim()
      if (emailList.value.includes(email)) {
        ElMessage.warning('该邮箱已存在')
        return
      }
      emailList.value.push(email)
      form.value.selectedEmail = email
      showAddEmailDialog.value = false
      resetEmailForm()
      ElMessage.success('添加成功')
    }
  })
}

// 删除邮箱
const removeEmail = (index) => {
  emailList.value.splice(index, 1)
  if (emailList.value.length > 0) {
    form.value.selectedEmail = emailList.value[0]
  } else {
    form.value.selectedEmail = ''
  }
  ElMessage.success('删除成功')
}

// 邮箱选择变化
const handleEmailChange = (value) => {
  // 可以在这里处理选择变化
}

// 重置邮箱表单
const resetEmailForm = () => {
  emailForm.value.email = ''
  if (emailFormRef.value) {
    emailFormRef.value.clearValidate()
  }
}

// 保存设置
const handleSave = async () => {
  if (!formRef.value) return

  try {
    const settings = {
      emailNotify: emailList.value.length > 0,
      newWriteNotifyEmail: emailList.value.join(';')
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

.email-list {
  margin-top: 20px;
  margin-bottom: 20px;
  
  .email-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px;
    margin-bottom: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    
    span {
      flex: 1;
    }
  }
}

.submit-btn {
  margin-top: 30px;
}
</style>

