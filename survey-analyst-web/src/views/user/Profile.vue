<template>
  <div class="profile-container">
    <h2 class="page-title">个人中心</h2>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>

      <div class="profile-content">
        <el-form :model="userForm" label-width="120px" style="max-width: 600px">
          <el-form-item label="账号">
            <el-input v-model="userForm.account" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="userForm.email" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="userForm.username" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userForm.phone" />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag :type="userForm.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ userForm.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Check" type="primary" @click="handleSaveInfo">保存信息</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>

      <div class="profile-content">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px" style="max-width: 600px">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button :icon="Lock" type="primary" @click="handleChangePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Lock } from '@element-plus/icons-vue'
import { userApi } from '@/api'

const userForm = reactive({
  id: null,
  account: '',
  email: '',
  username: '',
  phone: '',
  role: '',
  status: 1
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref(null)

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadUserInfo = async () => {
  try {
    const res = await userApi.getCurrentUser()
    if (res.code === 200) {
      Object.assign(userForm, res.data)
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

const handleSaveInfo = async () => {
  try {
    const res = await userApi.updateUserInfo({
      email: userForm.email,
      username: userForm.username,
      phone: userForm.phone
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      loadUserInfo()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) {
    return
  }

  await passwordFormRef.value.validate((valid) => {
    if (valid) {
      changePassword()
    }
  })
}

const changePassword = async () => {
  try {
    const res = await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value?.resetFields()
    }
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  padding: 20px 0;
}
</style>

