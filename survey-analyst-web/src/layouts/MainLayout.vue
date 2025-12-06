<template>
  <div class="main-layout">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h2 class="logo">在线问卷调查系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo?.username || '用户' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
          >
            <el-menu-item index="/home">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/user/survey/list">
              <el-icon><Document /></el-icon>
              <span>我的问卷</span>
            </el-menu-item>
            <el-menu-item index="/user/survey/design">
              <el-icon><Edit /></el-icon>
              <span>创建问卷</span>
            </el-menu-item>
            <el-sub-menu index="/user/survey/analysis">
              <template #title>
                <el-icon><DataAnalysis /></el-icon>
                <span>数据分析</span>
              </template>
              <el-menu-item index="/user/survey/analysis/cross">交叉分析</el-menu-item>
              <el-menu-item index="/user/survey/analysis/trend">趋势分析</el-menu-item>
              <el-menu-item index="/user/survey/analysis/profile">样本画像</el-menu-item>
              <el-menu-item index="/user/survey/analysis/dashboard">数据仪表盘</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled, Document, Edit, User, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    // TODO: 跳转到个人中心
    ElMessage.info('个人中心功能开发中')
  }
}
</script>

<style scoped>
.main-layout {
  width: 100%;
  height: 100vh;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  margin: 0;
  font-size: 20px;
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  border: none;
  height: 100%;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
}
</style>
