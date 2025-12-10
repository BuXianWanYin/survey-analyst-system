<template>
  <div class="survey-publish-container">
    <el-card class="publish-settings-card">
      <template #header>
        <div class="card-header">
          <span>发布设置</span>
          <el-button type="primary" @click="handlePublish" :loading="publishing">
            {{ survey.status === 'PUBLISHED' ? '已发布' : '发布问卷' }}
          </el-button>
        </div>
      </template>

      <el-form :model="publishForm" label-width="120px">
        <el-form-item label="访问权限">
          <el-radio-group v-model="publishForm.accessType">
            <el-radio label="PUBLIC">公开访问</el-radio>
            <el-radio label="PASSWORD">密码访问</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="publishForm.accessType === 'PASSWORD'" label="访问密码">
          <el-input
            v-model="publishForm.password"
            type="password"
            placeholder="请输入访问密码"
            style="width: 300px"
          />
        </el-form-item>

        <el-form-item label="最大填写数">
          <el-input-number
            v-model="publishForm.maxResponses"
            :min="0"
            placeholder="不限制"
            style="width: 300px"
          />
          <span style="margin-left: 10px; color: #909399">0表示不限制</span>
        </el-form-item>

        <el-form-item label="开始时间">
          <el-date-picker
            v-model="publishForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 300px"
          />
        </el-form-item>

        <el-form-item label="结束时间">
          <el-date-picker
            v-model="publishForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 300px"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="publish-tools-card">
      <template #header>
        <span>多渠道发布工具</span>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 问卷链接 -->
        <el-tab-pane label="问卷链接" name="link">
          <div class="tool-section">
            <el-input
              v-model="surveyLink"
              readonly
              style="width: 70%"
            />
            <el-button type="primary" @click="handleCopyLink">复制链接</el-button>
          </div>
        </el-tab-pane>

        <!-- 二维码 -->
        <el-tab-pane label="二维码" name="qrcode">
          <div class="tool-section">
            <div v-if="qrCodeBase64" class="qrcode-container">
              <img :src="qrCodeBase64" alt="二维码" class="qrcode-image" />
              <div class="qrcode-actions">
                <el-button type="primary" @click="handleDownloadQRCode">下载二维码</el-button>
              </div>
            </div>
            <el-button v-else type="primary" @click="loadQRCode" :loading="loadingQRCode">
              生成二维码
            </el-button>
          </div>
        </el-tab-pane>

        <!-- 社交媒体分享 -->
        <el-tab-pane label="社交媒体分享" name="share">
          <div class="tool-section">
            <div class="share-buttons">
              <el-button @click="handleShare('wechat')">
                <el-icon><Share /></el-icon>
                微信分享
              </el-button>
              <el-button @click="handleShare('weibo')">
                <el-icon><Share /></el-icon>
                微博分享
              </el-button>
              <el-button @click="handleShare('qq')">
                <el-icon><Share /></el-icon>
                QQ分享
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Share } from '@element-plus/icons-vue'
import { surveyApi, surveyPublishApi } from '@/api'
import dayjs from 'dayjs'

const route = useRoute()

const survey = ref({})
const publishForm = reactive({
  accessType: 'PUBLIC',
  password: '',
  maxResponses: null,
  startTime: null,
  endTime: null
})

const activeTab = ref('link')
const surveyLink = ref('')
const qrCodeBase64 = ref('')
const loadingQRCode = ref(false)
const publishing = ref(false)

const loadSurveyData = async () => {
  const surveyId = route.query.id
  if (!surveyId) {
    ElMessage.warning('请先选择问卷')
    return
  }

  try {
    const res = await surveyApi.getSurveyById(surveyId)
    if (res.code === 200) {
      survey.value = res.data
      publishForm.accessType = res.data.accessType || 'PUBLIC'
      publishForm.password = res.data.password || ''
      publishForm.maxResponses = res.data.maxResponses
      publishForm.startTime = res.data.startTime ? dayjs(res.data.startTime).toDate() : null
      publishForm.endTime = res.data.endTime ? dayjs(res.data.endTime).toDate() : null
      
      // 加载问卷链接
      await loadSurveyLink()
    }
  } catch (error) {
    ElMessage.error('加载问卷数据失败')
  }
}

const loadSurveyLink = async () => {
  try {
    const res = await surveyPublishApi.getSurveyLink(route.query.id)
    if (res.code === 200) {
      surveyLink.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取问卷链接失败')
  }
}

const loadQRCode = async () => {
  loadingQRCode.value = true
  try {
    const res = await surveyPublishApi.getQRCode(route.query.id)
    if (res.code === 200) {
      qrCodeBase64.value = res.data
    }
  } catch (error) {
    ElMessage.error('生成二维码失败')
  } finally {
    loadingQRCode.value = false
  }
}

const handlePublish = async () => {
  publishing.value = true
  try {
    const updateData = {
      ...publishForm,
      startTime: publishForm.startTime ? dayjs(publishForm.startTime).format('YYYY-MM-DD HH:mm:ss') : null,
      endTime: publishForm.endTime ? dayjs(publishForm.endTime).format('YYYY-MM-DD HH:mm:ss') : null
    }
    
    const res = await surveyApi.updateSurvey(route.query.id, updateData)
    if (res.code === 200) {
      const publishRes = await surveyApi.publishSurvey(route.query.id)
      if (publishRes.code === 200) {
        ElMessage.success('发布成功')
        survey.value.status = 'PUBLISHED'
      }
    }
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const handleCopyLink = async () => {
  try {
    await navigator.clipboard.writeText(surveyLink.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleDownloadQRCode = () => {
  if (!qrCodeBase64.value) return
  
  const link = document.createElement('a')
  link.href = qrCodeBase64.value
  link.download = `survey-${route.query.id}-qrcode.png`
  link.click()
}

const handleShare = async (platform) => {
  try {
    const res = await surveyPublishApi.getShareLinks(route.query.id)
    if (res.code === 200) {
      const shareUrl = res.data[platform]
      if (shareUrl) {
        window.open(shareUrl, '_blank')
      } else {
        ElMessage.warning('该平台分享链接暂不可用')
      }
    }
  } catch (error) {
    ElMessage.error('获取分享链接失败')
  }
}

onMounted(() => {
  loadSurveyData()
})
</script>

<style scoped>
.survey-publish-container {
  padding: 20px;
}

.publish-settings-card,
.publish-tools-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tool-section {
  padding: 20px 0;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.qrcode-image {
  width: 300px;
  height: 300px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.qrcode-actions {
  display: flex;
  gap: 10px;
}

.share-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}
</style>

