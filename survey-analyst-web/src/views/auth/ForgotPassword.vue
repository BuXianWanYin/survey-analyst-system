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
        <el-form-item prop="code">
          <div class="code-input-wrapper">
            <el-input
              v-model="forgotPasswordForm.code"
              placeholder="请输入验证码"
              size="large"
              :prefix-icon="Lock"
              class="code-input"
            />
            <el-button
              type="primary"
              size="large"
              class="send-code-button"
              :disabled="countdown > 0 || sendingCode"
              :loading="sendingCode"
              @click="handleSendCode"
            >
              <span v-if="countdown > 0">{{ countdown }}秒</span>
              <span v-else>发送验证码</span>
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="forgotPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20个字符）"
            size="large"
            :prefix-icon="Lock"
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()

const forgotPasswordFormRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

const forgotPasswordForm = reactive({
  email: '',
  code: '',
  newPassword: ''
})

const forgotPasswordRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  if (!forgotPasswordFormRef.value) return

  // 先验证邮箱
  try {
    await forgotPasswordFormRef.value.validateField('email')
  } catch (error) {
    return
  }

  sendingCode.value = true
  try {
    const res = await authApi.sendVerificationCode({
      email: forgotPasswordForm.email,
      type: 'RESET_PASSWORD'
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '验证码已发送，请查收您的邮箱')
      // 开始倒计时
      countdown.value = 60
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
          countdownTimer = null
        }
      }, 1000)
    }
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

const handleSubmit = async () => {
  if (!forgotPasswordFormRef.value) return

  await forgotPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await authApi.resetPassword({
          email: forgotPasswordForm.email,
          code: forgotPasswordForm.code,
          newPassword: forgotPasswordForm.newPassword
        })
        if (res.code === 200) {
          ElMessage.success(res.message || '密码重置成功，请使用新密码登录')
          // 2秒后返回登录页
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        }
      } catch (error) {
        ElMessage.error(error.message || '重置密码失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

// 组件卸载时清除定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.forgot-password-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  padding: 20px;
  box-sizing: border-box;
}

.forgot-password-box {
  width: 450px;
  max-width: 100%;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
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

.code-input-wrapper {
  display: flex;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.send-code-button {
  width: 120px;
  flex-shrink: 0;
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
