<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <h2 class="forgot-password-title">找回密码</h2>
      <el-form
        ref="forgotPasswordFormRef"
        :model="forgotPasswordForm"
        :rules="forgotPasswordRules"
        class="forgot-password-form"
      >
        <el-form-item prop="email">
          <el-input
            v-model="forgotPasswordForm.email"
            placeholder="请输入注册邮箱"
            size="large"
            :prefix-icon="Message"
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
            发送重置邮件
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()

const forgotPasswordFormRef = ref()
const loading = ref(false)

const forgotPasswordForm = reactive({
  email: ''
})

const forgotPasswordRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!forgotPasswordFormRef.value) return

  await forgotPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await authApi.forgotPassword({ email: forgotPasswordForm.email })
        if (res.code === 200) {
          ElMessage.success(res.message || '密码重置邮件已发送，请查收您的邮箱')
          // 3秒后返回登录页
          setTimeout(() => {
            router.push('/login')
          }, 3000)
        }
      } catch (error) {
        ElMessage.error(error.message || '发送失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  padding: 20px;
  box-sizing: border-box;
}

.forgot-password-box {
  width: 400px;
  max-width: 100%;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.forgot-password-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.forgot-password-form {
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
  .forgot-password-container {
    padding: 15px;
  }

  .forgot-password-box {
    padding: 30px 20px;
  }

  .forgot-password-title {
    font-size: 20px;
    margin-bottom: 25px;
  }
}
</style>

