<template>
  <div class="survey-design-container">
    <!-- 顶部工具栏 -->
    <div class="header-container">
      <el-card class="header-card">
        <div class="header-content">
          <el-button text @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div class="header-title">
            <span>{{ surveyTitle || '未命名问卷' }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主体：左侧菜单 + 右侧内容 -->
    <div class="main-container">
      <!-- 左侧：导航菜单 -->
      <div class="left-menu-container">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="menuItem in menuItemList"
            :key="menuItem.route"
            :index="menuItem.route"
          >
            <el-icon><component :is="menuItem.icon" /></el-icon>
            <span>{{ menuItem.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧：内容区域 -->
      <div class="right-content-container">
        <router-view :key="route.fullPath" :survey-id="surveyId" :form-key="formKey" />
      </div>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="预览"
      width="90%"
      fullscreen
      destroy-on-close
    >
      <SurveyPreview
        v-if="previewVisible"
        :form-key="formKey"
        :survey-id="surveyId"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Menu,
  View,
  Setting,
  VideoPlay,
  DataAnalysis,
  DataLine
} from '@element-plus/icons-vue'
import { surveyApi, formApi } from '@/api'
import SurveyPreview from '@/components/SurveyPreview.vue'

const route = useRoute()
const router = useRouter()

const surveyId = ref(null)
const surveyTitle = ref('')
const formKey = ref(null)
const previewVisible = ref(false)

// 菜单列表
const menuItemList = [
  {
    title: '编辑',
    icon: 'Edit',
    route: 'editor'
  },
  {
    title: '逻辑',
    icon: 'Menu',
    route: 'logic'
  },
  {
    title: '外观',
    icon: 'View',
    route: 'theme'
  },
  {
    title: '设置',
    icon: 'Setting',
    route: 'setting'
  },
  {
    title: '发布',
    icon: 'VideoPlay',
    route: 'publish'
  },
  {
    title: '数据',
    icon: 'DataAnalysis',
    route: 'data'
  },
  {
    title: '统计',
    icon: 'DataLine',
    route: 'statistics'
  }
]

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('/logic')) return 'logic'
  if (path.includes('/theme')) return 'theme'
  if (path.includes('/setting')) return 'setting'
  if (path.includes('/publish')) return 'publish'
  if (path.includes('/data')) return 'data'
  if (path.includes('/statistics')) return 'statistics'
  return 'editor'
})

// 菜单选择处理
const handleMenuSelect = (index) => {
  const id = route.query.id
  if (id) {
    router.push({
      path: `/user/survey/design/${index}`,
      query: { id }
    })
  }
}

// 加载问卷信息
const loadSurveyInfo = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('问卷ID不存在')
    router.back()
    return
  }

  surveyId.value = Number(id)

  try {
    const res = await surveyApi.getSurveyById(surveyId.value)
    if (res.code === 200 && res.data) {
      surveyTitle.value = res.data.title || '未命名问卷'
    }
    
    // 加载表单配置，获取 formKey
    const configRes = await formApi.getFormConfig(surveyId.value)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
    }
  } catch (error) {
    ElMessage.error('加载问卷信息失败')
  }
}

// 预览
const handlePreview = () => {
  if (!formKey.value) {
    ElMessage.warning('请先保存表单')
    return
  }
  previewVisible.value = true
}

onMounted(() => {
  loadSurveyInfo()
})
</script>

<style lang="scss" scoped>
.survey-design-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header-container {
  height: 60px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.header-card {
  height: 100%;
  border: none;
  border-radius: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  gap: 20px;
}

.header-title {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.left-menu-container {
  width: 100px;
  border-right: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}

.el-menu-vertical {
  border: none;
  width: 100%;
}

.right-content-container {
  flex: 1;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>

