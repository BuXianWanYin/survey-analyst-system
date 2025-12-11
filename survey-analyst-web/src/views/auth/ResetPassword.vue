<template>
  <div class="reset-password-container">
    <div class="reset-password-box">
      <h2 class="reset-password-title">重置密码</h2>
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        class="reset-password-form"
      >
        <el-form-item prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-button"
            :loading="loading"
            @click="handleSubmit"
          >
            重置密码
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="form-footer">
            <el-link type="primary" @click="goToLogin">返回登录</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()
const route = useRoute()

const resetPasswordFormRef = ref()
const loading = ref(false)

const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== resetPasswordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!resetPasswordFormRef.value) return

  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      const token = route.query.token
      if (!token) {
        ElMessage.error('重置令牌无效，请重新申请')
        router.push('/auth/forgot-password')
        return
      }

      loading.value = true
      try {
        const res = await authApi.resetPassword({
          token: token,
          newPassword: resetPasswordForm.newPassword
        })
        if (res.code === 200) {
          ElMessage.success(res.message || '密码重置成功，请使用新密码登录')
          // 2秒后跳转到登录页
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        }
      } catch (error) {
        ElMessage.error(error.message || '密码重置失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  const token = route.query.token
  if (!token) {
    ElMessage.warning('重置令牌无效，请重新申请')
    router.push('/auth/forgot-password')
  }
})
</script>

<style scoped>
.reset-password-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  padding: 20px;
  box-sizing: border-box;
}

.reset-password-box {
  width: 400px;
  max-width: 100%;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.reset-password-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.reset-password-form {
  margin-top: 20px;
}

.submit-button {
  width: 100%;
}

.form-footer {
  width: 100%;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reset-password-container {
    padding: 15px;
  }

  .reset-password-box {
    padding: 30px 20px;
  }

  .reset-password-title {
    font-size: 20px;
    margin-bottom: 25px;
  }
}
</style>

