<template>
  <div class="notify-setting-view">
    <p class="setting-title">
      通知设置
    </p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">发邮件提醒我</p>
        <div class="email-select-wrapper">
          <el-select
            v-model="form.selectedEmail"
            placeholder="请选择邮箱"
            class="email-select"
            @change="handleEmailChange"
            filterable
          >
            <!-- 已添加的邮箱列表 -->
            <el-option
              v-for="email in emailList"
              :key="email"
              :label="email"
              :value="email"
            >
              <span>{{ email }}</span>
              <span class="email-status">已添加</span>
            </el-option>
            <!-- 用户邮箱（如果不在列表中） -->
            <el-option
              v-if="currentUserEmail && !isEmailInList(currentUserEmail)"
              :key="'user-' + currentUserEmail"
              :label="currentUserEmail"
              :value="currentUserEmail"
            >
              <span>{{ currentUserEmail }}</span>
              <span
                class="add-email-icon"
                @click.stop="handleAddEmailFromSelect(currentUserEmail)"
              >
                +
              </span>
            </el-option>
            <!-- 添加邮箱选项 -->
            <el-option
              key="add-email"
              label="添加邮箱"
              value="__add_email__"
              class="add-email-option"
            >
              <span class="add-email-text">
                <el-icon><Plus /></el-icon>
                添加邮箱
              </span>
            </el-option>
          </el-select>
        </div>
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
      :width="dialogWidth"
      class="add-email-dialog"
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
import { ref, onMounted, computed } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Check, Close } from '@element-plus/icons-vue'
import { formApi, userApi } from '@/api'

const route = useRoute()

const formRef = ref(null)
const emailFormRef = ref(null)
const surveyId = ref(null)
const showAddEmailDialog = ref(false)
const emailList = ref([])
const currentUserEmail = ref('')
const form = ref({
  selectedEmail: ''
})

const emailForm = ref({
  email: ''
})

// 响应式弹窗宽度
const { width } = useWindowSize()
const dialogWidth = computed(() => width.value < 768 ? '90%' : '500px')

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ]
}

const rules = {}

// 加载当前用户信息
const loadCurrentUser = async () => {
  try {
    const res = await userApi.getCurrentUser()
    if (res.code === 200 && res.data && res.data.email) {
      currentUserEmail.value = res.data.email
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 检查邮箱是否在列表中
const isEmailInList = (email) => {
  return emailList.value.includes(email)
}

// 从下拉框添加邮箱
const handleAddEmailFromSelect = (email) => {
  if (!email || email === '__add_email__') {
    showAddEmailDialog.value = true
    return
  }
  
  if (isEmailInList(email)) {
    ElMessage.warning('该邮箱已在通知列表中')
    return
  }
  
  emailList.value.push(email)
  form.value.selectedEmail = email
  ElMessage.success('添加成功')
}

// 加载设置
const loadSetting = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)

  try {
    const res = await formApi.getFormSetting(surveyId.value)
    console.log('加载通知设置:', res) // 调试日志
    
    if (res.code === 200 && res.data && res.data.settings) {
      const settings = res.data.settings
      console.log('通知设置内容:', settings) // 调试日志
      
      // 初始化邮箱列表
      emailList.value = []
      form.value.selectedEmail = ''
      
      // 加载邮箱列表
      if (settings.newWriteNotifyEmail && typeof settings.newWriteNotifyEmail === 'string') {
        const emailStr = settings.newWriteNotifyEmail.trim()
        if (emailStr) {
          // 将邮箱字符串转换为数组
          emailList.value = emailStr.split(';').filter(e => e.trim())
          if (emailList.value.length > 0) {
            form.value.selectedEmail = emailList.value[0]
          }
        }
      }
    } else {
      // 如果没有设置，初始化为空
      emailList.value = []
      form.value.selectedEmail = ''
    }
  } catch (error) {
    console.error('加载通知设置失败:', error) // 调试日志
    // 如果不存在，使用默认值
    emailList.value = []
    form.value.selectedEmail = ''
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
      // 自动保存设置
      await autoSaveSettings()
    }
  })
}

// 删除邮箱
const removeEmail = async (index) => {
  emailList.value.splice(index, 1)
  if (emailList.value.length > 0) {
    form.value.selectedEmail = emailList.value[0]
  } else {
    form.value.selectedEmail = ''
  }
  ElMessage.success('删除成功')
  // 自动保存设置
  await autoSaveSettings()
}

// 邮箱选择变化
const handleEmailChange = (value) => {
  if (value === '__add_email__') {
    // 选择"添加邮箱"选项时，打开对话框
    showAddEmailDialog.value = true
    form.value.selectedEmail = ''
  } else if (value && !isEmailInList(value)) {
    // 如果选择的是未添加的邮箱（如用户邮箱），自动添加
    handleAddEmailFromSelect(value)
  }
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
    // 确保保存时包含所有必要的字段
    const settings = {
      emailNotify: emailList.value.length > 0,
      newWriteNotifyEmail: emailList.value.length > 0 ? emailList.value.join(';') : ''
    }
    
    console.log('保存通知设置:', settings) // 调试日志
    
    const res = await formApi.saveFormSetting(surveyId.value, settings)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      // 重新加载设置以确保数据同步
      await loadSetting()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存通知设置失败:', error) // 调试日志
    ElMessage.error(error.response?.data?.message || error.message || '保存失败')
  }
}

onMounted(async () => {
  // 先加载用户信息，再加载设置
  await loadCurrentUser()
  await loadSetting()
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
    font-size: 14px;
    line-height: 32px;
    white-space: nowrap;
    flex-shrink: 0;
  }
}

.email-select-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.email-select {
  flex: 1;
  min-width: 200px;
  max-width: 400px;
}

:deep(.el-select-dropdown__item) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px;
  
  .add-email-icon {
    margin-left: auto;
    padding: 2px 8px;
    font-size: 18px;
    font-weight: bold;
    color: #409eff;
    min-width: 24px;
    cursor: pointer;
    user-select: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    
    &:hover {
      color: #66b1ff;
      background-color: #ecf5ff;
      border-radius: 4px;
    }
  }
  
  .email-tag {
    margin-left: 8px;
    padding: 2px 6px;
    background-color: #ecf5ff;
    color: #409eff;
    border-radius: 2px;
    font-size: 12px;
    white-space: nowrap;
  }
  
  .email-status {
    margin-left: auto;
    padding: 2px 6px;
    background-color: #f0f9ff;
    color: #67c23a;
    border-radius: 2px;
    font-size: 12px;
    white-space: nowrap;
  }
  
  &.add-email-option {
    .add-email-text {
      color: #409eff;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 5px;
    }
    
    &:hover {
      background-color: #ecf5ff;
    }
  }
}

:deep(.el-button) {
  font-size: 14px;
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
      font-size: 13px;
    }
  }
}

.submit-btn {
  margin-top: 30px;
}

@media (max-width: 768px) {
  .notify-setting-view {
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

  .email-select-wrapper {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .email-select {
    width: 100% !important;
    max-width: 100%;
    min-width: 0;
  }

  .add-email-btn {
    width: 100%;
    margin-left: 0 !important;
  }
}

@media (max-width: 768px) {
  :deep(.add-email-dialog) {
    .el-dialog {
      margin: 5vh auto !important;
      max-width: 90% !important;
    }

    .el-dialog__body {
      padding: 15px !important;
    }

    .el-form-item {
      margin-bottom: 15px;
    }

    .el-form-item__label {
      font-size: 13px;
      padding-bottom: 5px;
    }

    .el-input__inner {
      font-size: 14px;
    }

    .el-dialog__footer {
      padding: 10px 15px !important;
    }

    .el-button {
      font-size: 13px;
      padding: 8px 15px;
    }
  }
}
</style>

