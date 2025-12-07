<template>
  <div class="publish-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布</span>
          <span class="desc-text">公开发布后，所有访问的用户都能够填写数据</span>
        </div>
      </template>

      <div class="publish-content">
        <p class="desc-text">开启后，会生成表单填写链接，所有人即可填写表单</p>
        <el-switch
          v-model="publishStatus"
          @change="handlePublishChange"
        />
        
        <div v-if="publishStatus" class="link-section">
          <el-divider />
          <div class="link-info">
            <div class="link-label">表单填写链接：</div>
            <div class="link-value">
              <el-input
                v-model="surveyLink"
                readonly
                class="link-input"
              >
                <template #append>
                  <el-button @click="handleCopyLink">复制</el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { surveyApi } from '@/api'

const route = useRoute()

const surveyId = ref(null)
const publishStatus = ref(false)
const surveyLink = ref('')

// 加载发布状态
const loadPublishStatus = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('问卷ID不存在')
    return
  }

  surveyId.value = Number(id)

  try {
    const res = await surveyApi.getSurveyById(surveyId.value)
    if (res.code === 200 && res.data) {
      publishStatus.value = res.data.status === 'PUBLISHED'
      
      // 生成链接
      const baseUrl = window.location.origin
      surveyLink.value = `${baseUrl}/user/survey/fill/${surveyId.value}`
    }
  } catch (error) {
    ElMessage.error('加载发布状态失败')
  }
}

// 发布状态变更
const handlePublishChange = async (val) => {
  if (!surveyId.value) return

  try {
    if (val) {
      await surveyApi.publishSurvey(surveyId.value)
      ElMessage.success('发布成功')
    } else {
      await surveyApi.stopPublishSurvey(surveyId.value)
      ElMessage.success('停止发布成功')
    }
    await loadPublishStatus()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
    publishStatus.value = !val // 恢复原状态
  }
}

// 复制链接
const handleCopyLink = () => {
  navigator.clipboard.writeText(surveyLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

onMounted(() => {
  loadPublishStatus()
})
</script>

<style lang="scss" scoped>
.publish-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .desc-text {
    font-size: 12px;
    color: #909399;
  }
}

.publish-content {
  .desc-text {
    margin-bottom: 20px;
    color: #606266;
  }
}

.link-section {
  margin-top: 20px;
}

.link-info {
  .link-label {
    margin-bottom: 10px;
    font-weight: 500;
  }
  
  .link-input {
    width: 100%;
  }
}
</style>

