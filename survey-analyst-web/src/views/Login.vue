<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">在线问卷调查系统</h2>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="loginName">
          <el-input
            v-model="loginForm.loginName"
            placeholder="请输入账号或邮箱"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            :icon="Right"
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="login-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
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
import { User, Lock, Right } from '@element-plus/icons-vue'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  loginName: '',
  password: ''
})

const loginRules = {
  loginName: [
    { required: true, message: '请输入账号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userApi.login(loginForm)
        if (res.code === 200) {
          userStore.setToken(res.data.token)
          userStore.setUserInfo(res.data.user)
          ElMessage.success('登录成功')
          
          // 检查是否有重定向参数
          const redirect = route.query.redirect
          if (redirect && typeof redirect === 'string') {
            // 跳转回原来的页面
            router.push(redirect)
          } else {
            // 根据用户角色跳转到不同页面
            if (res.data.user && res.data.user.role === 'ADMIN') {
              router.push('/system/user')
            } else {
              router.push('/survey/list')
            }
          }
        }
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  // 如果有重定向参数，传递给注册页
  const redirect = route.query.redirect
  if (redirect) {
    router.push({
      name: 'Register',
      query: { redirect }
    })
  } else {
    router.push('/register')
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  padding: 20px;
  box-sizing: border-box;
}

.login-box {
  width: 400px;
  max-width: 100%;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
}

.login-footer {
  width: 100%;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.login-footer .el-link {
  margin-left: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
  }

  .login-box {
    padding: 30px 20px;
  }

  .login-title {
    font-size: 20px;
    margin-bottom: 25px;
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: 25px 15px;
  }

  .login-title {
    font-size: 18px;
    margin-bottom: 20px;
  }
}
</style>
