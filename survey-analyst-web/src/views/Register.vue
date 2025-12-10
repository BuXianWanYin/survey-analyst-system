<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="register-title">用户注册</h2>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
      >
        <el-form-item prop="account">
          <el-input
            v-model="registerForm.account"
            placeholder="请输入账号（3-50个字符）"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（昵称）"
            size="large"
            :prefix-icon="Avatar"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20个字符）"
            size="large"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            :icon="UserFilled"
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="register-footer">
            <span>已有账号？</span>
            <el-link type="primary" @click="goToLogin">立即登录</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Avatar, UserFilled } from '@element-plus/icons-vue'
import { userApi } from '@/api'

const router = useRouter()
const route = useRoute()

const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  account: '',
  email: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度在3到50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度在1到50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userApi.register({
          account: registerForm.account,
          email: registerForm.email,
          username: registerForm.username,
          password: registerForm.password
        })
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          // 如果有重定向参数，传递给登录页
          const redirect = route.query.redirect
          if (redirect) {
            router.push({
              name: 'Login',
              query: { redirect }
            })
          } else {
            router.push('/login')
          }
        }
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  // 如果有重定向参数，传递给登录页
  const redirect = route.query.redirect
  if (redirect) {
    router.push({
      name: 'Login',
      query: { redirect }
    })
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.register-box {
  width: 450px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
}

.register-footer {
  width: 100%;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.register-footer .el-link {
  margin-left: 5px;
}
</style>
