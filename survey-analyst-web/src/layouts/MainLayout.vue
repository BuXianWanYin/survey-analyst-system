<template>
  <div class="main-layout">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button
            v-if="isMobile"
            :icon="isCollapse ? Expand : Fold"
            text
            class="mobile-menu-toggle"
            @click="toggleMobileMenu"
          />
          <h2 class="logo">在线问卷调查系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span class="username-text">{{ userStore.userInfo?.username || '用户' }}</span>
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
        <el-aside 
          :width="isMobile ? '0px' : '200px'" 
          class="aside"
          :class="{ 'mobile-collapse': isMobile && !isCollapse }"
        >
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
            :collapse="isMobile && !isCollapse"
          >
            <template v-if="userStore.userInfo?.role !== 'ADMIN'">
              <el-menu-item index="/survey/list">
                <el-icon><Document /></el-icon>
                <span>我的问卷</span>
              </el-menu-item>
              <el-menu-item index="/survey/template">
                <el-icon><Document /></el-icon>
                <span>问卷模板</span>
              </el-menu-item>
            </template>
            <template v-if="userStore.userInfo?.role === 'ADMIN'">
              <el-menu-item index="/system/user">
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </el-menu-item>
              <el-menu-item index="/system/survey">
                <el-icon><Document /></el-icon>
                <span>问卷管理</span>
              </el-menu-item>
              <el-menu-item index="/system/template">
                <el-icon><Grid /></el-icon>
                <span>公共模板管理</span>
              </el-menu-item>
              <el-menu-item index="/system/data">
                <el-icon><DataAnalysis /></el-icon>
                <span>数据管理</span>
              </el-menu-item>
              <el-menu-item index="/system/log">
                <el-icon><List /></el-icon>
                <span>系统日志</span>
              </el-menu-item>
            </template>
          </el-menu>
        </el-aside>
        <el-main class="main-content">
          <div v-if="isMobile && !isCollapse" class="mobile-overlay" @click="isCollapse = true"></div>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, User, ArrowDown, Setting, Fold, Expand, Grid, DataAnalysis, List } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useWindowSize } from '@vueuse/core'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { width } = useWindowSize()

const activeMenu = computed(() => route.path)
const isMobile = computed(() => width.value < 768)
const isCollapse = ref(true)

const toggleMobileMenu = () => {
  isCollapse.value = !isCollapse.value
}

// 移动端点击路由后自动关闭菜单
const stopWatcher = router.afterEach(() => {
  if (isMobile.value) {
    isCollapse.value = true
  }
})

onUnmounted(() => {
  stopWatcher()
})

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
    router.push('/user/profile')
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
  position: relative;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mobile-menu-toggle {
  display: none;
}

.logo {
  margin: 0;
  font-size: 20px;
  color: #409EFF;
  white-space: nowrap;
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
  gap: 5px;
}

.username-text {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s;
  overflow: hidden;
}

.aside.mobile-collapse {
  position: fixed;
  left: 0;
  top: 60px;
  height: calc(100vh - 60px);
  z-index: 999;
  width: 200px !important;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.sidebar-menu {
  border: none;
  height: 100%;
}

.main-content {
  background: #f5f7fa;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
}

.mobile-overlay {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }

  .mobile-menu-toggle {
    display: block;
  }

  .logo {
    font-size: 16px;
  }

  .username-text {
    max-width: 60px;
  }

  .main-content {
    padding: 0;
  }
}

@media (max-width: 480px) {
  .logo {
    font-size: 14px;
  }

  .username-text {
    display: none;
  }
}
</style>
