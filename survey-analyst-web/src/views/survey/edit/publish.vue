<template>
  <div class="publish-container">
    <div class="cards-row">
      <el-card class="recovery-limits-card">
        <template #header>
          <span>回收设置</span>
        </template>

        <el-form :model="recoveryForm" label-width="180px" class="recovery-form">
          <el-form-item label="最大填写数">
            <el-input-number
              v-model="recoveryForm.maxResponses"
              :min="0"
              placeholder="不限制"
              style="width: 300px"
            />
            <span style="margin-left: 10px; color: #909399">0表示不限制</span>
          </el-form-item>

          <el-form-item label="每个IP答题次数限制">
            <div style="display: flex; align-items: center; gap: 10px; flex-wrap: nowrap">
              <el-switch v-model="recoveryForm.ipWriteCountLimitStatus" />
              <template v-if="recoveryForm.ipWriteCountLimitStatus">
                <el-input-number
                  v-model="recoveryForm.ipWriteCountLimit"
                  :min="1"
                  style="width: 200px"
                />
                <span style="color: #909399">次</span>
              </template>
            </div>
          </el-form-item>

          <el-form-item label="每个设备答题次数限制">
            <div style="display: flex; align-items: center; gap: 10px; flex-wrap: nowrap">
              <el-switch v-model="recoveryForm.deviceWriteCountLimitStatus" />
              <template v-if="recoveryForm.deviceWriteCountLimitStatus">
                <el-input-number
                  v-model="recoveryForm.deviceWriteCountLimit"
                  :min="1"
                  style="width: 200px"
                />
                <span style="color: #909399">次</span>
              </template>
            </div>
          </el-form-item>

          <el-form-item label="每个用户答题次数限制">
            <div style="display: flex; align-items: center; gap: 10px; flex-wrap: nowrap">
              <el-switch v-model="recoveryForm.accountWriteCountLimitStatus" />
              <template v-if="recoveryForm.accountWriteCountLimitStatus">
                <el-input-number
                  v-model="recoveryForm.accountWriteCountLimit"
                  :min="1"
                  style="width: 200px"
                />
                <span style="color: #909399">次</span>
              </template>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="publish-settings-card">
        <template #header>
          <div class="card-header">
            <span>发布设置</span>
            <el-button 
              v-if="survey.status !== 'PUBLISHED'"
              :icon="Promotion" 
              type="primary" 
              @click="handlePublish" 
              :loading="publishing"
            >
              发布问卷
            </el-button>
            <el-button 
              v-else
              :icon="Close" 
              type="danger" 
              @click="handleUnpublish" 
              :loading="unpublishing"
            >
              停止发布
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
              show-password
            />
          </el-form-item>

          <el-form-item label="开始时间">
            <el-date-picker
              v-model="publishForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 300px"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>

          <el-form-item label="结束时间">
            <el-date-picker
              v-model="publishForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 300px"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-form>
      </el-card>


    </div>

    <!-- 第二行：二维码和链接卡片 -->
    <el-card v-if="survey.status === 'PUBLISHED'" class="publish-success-card">
      <template #header>
        <span>分享问卷</span>
      </template>

      <div class="publish-success-content">
        <!-- 二维码 -->
        <div class="qrcode-section">
          <div v-if="qrCodeBase64" class="qrcode-container">
            <img :src="qrCodeBase64" alt="二维码" class="qrcode-image" />
            <div class="qrcode-buttons">
              <el-button :icon="Download" type="primary" @click="handleDownloadQRCode">
                下载二维码
              </el-button>
              <el-button :icon="DocumentCopy" type="primary" @click="handleCopyQRCode">
                复制二维码
              </el-button>
            </div>
          </div>
          <el-button v-else type="primary" @click="loadQRCode" :loading="loadingQRCode">
            生成二维码
          </el-button>
        </div>

        <!-- 链接 -->
        <div class="link-section">
          <div class="link-label">问卷填写链接</div>
          <div class="link-input-group">
            <!-- 隐藏的测量元素 -->
            <span ref="measureSpan" class="link-measure">{{ surveyLink }}</span>
            <el-input
              v-model="surveyLink"
              readonly
              class="link-input"
              :style="{ width: linkInputWidth }"
            />
          </div>
          <div class="link-button-wrapper">
            <el-button :icon="Link" type="primary" @click="handleCopyLink">复制链接</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Promotion, Link, Download, Close, DocumentCopy } from '@element-plus/icons-vue'
import { surveyApi, surveyPublishApi, formApi } from '@/api'
import dayjs from 'dayjs'

const route = useRoute()

const survey = ref({})
const publishForm = reactive({
  accessType: 'PUBLIC',
  password: '',
  startTime: null,
  endTime: null
})

const recoveryForm = reactive({
  maxResponses: null,
  ipWriteCountLimitStatus: false,
  ipWriteCountLimit: 1,
  deviceWriteCountLimitStatus: false,
  deviceWriteCountLimit: 1,
  accountWriteCountLimitStatus: false,
  accountWriteCountLimit: 1
})

const surveyLink = ref('')
const qrCodeBase64 = ref('')
const loadingQRCode = ref(false)
const publishing = ref(false)
const unpublishing = ref(false)
const linkInputWidth = ref('auto')
const measureSpan = ref(null)

// 计算链接输入框宽度
const calculateLinkWidth = () => {
  nextTick(() => {
    if (measureSpan.value && surveyLink.value) {
      const width = measureSpan.value.offsetWidth
      // 加上输入框的内边距和边框，大约5px
      linkInputWidth.value = `${Math.max(width + 5, 200)}px`
    } else {
      linkInputWidth.value = 'auto'
    }
  })
}

// 监听链接变化，重新计算宽度
watch(surveyLink, () => {
  calculateLinkWidth()
})

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
      publishForm.startTime = res.data.startTime ? dayjs(res.data.startTime).format('YYYY-MM-DD HH:mm:ss') : null
      publishForm.endTime = res.data.endTime ? dayjs(res.data.endTime).format('YYYY-MM-DD HH:mm:ss') : null
      
      // 加载回收限制设置
      await loadRecoverySettings(surveyId)
      
      // 加载问卷链接
      await loadSurveyLink()
      
      // 如果问卷已发布，自动加载二维码
      if (res.data.status === 'PUBLISHED') {
        await loadQRCode()
      }
      
      // 计算链接输入框宽度
      calculateLinkWidth()
    }
  } catch (error) {
    ElMessage.error('加载问卷数据失败')
  }
}

const loadRecoverySettings = async (surveyId) => {
  try {
    const res = await formApi.getFormSetting(surveyId)
    if (res.code === 200 && res.data && res.data.settings) {
      const settings = res.data.settings
      
      // 从问卷数据或设置中获取最大填写数
      recoveryForm.maxResponses = survey.value.maxResponses || settings.maxResponses || null
      
      // IP限制
      if (settings.ipWriteCountLimitStatus !== undefined) {
        recoveryForm.ipWriteCountLimitStatus = settings.ipWriteCountLimitStatus
      }
      if (settings.ipWriteCountLimit !== undefined) {
        recoveryForm.ipWriteCountLimit = settings.ipWriteCountLimit || 1
      }
      
      // 设备限制
      if (settings.deviceWriteCountLimitStatus !== undefined) {
        recoveryForm.deviceWriteCountLimitStatus = settings.deviceWriteCountLimitStatus
      }
      if (settings.deviceWriteCountLimit !== undefined) {
        recoveryForm.deviceWriteCountLimit = settings.deviceWriteCountLimit || 1
      }
      
      // 用户限制
      if (settings.accountWriteCountLimitStatus !== undefined) {
        recoveryForm.accountWriteCountLimitStatus = settings.accountWriteCountLimitStatus
      }
      if (settings.accountWriteCountLimit !== undefined) {
        recoveryForm.accountWriteCountLimit = settings.accountWriteCountLimit || 1
      }
    } else {
      // 如果没有设置，尝试从问卷数据中获取最大填写数
      recoveryForm.maxResponses = survey.value.maxResponses || null
    }
  } catch (error) {
    // 如果接口不存在，使用默认值
    recoveryForm.maxResponses = survey.value.maxResponses || null
  }
}

const loadSurveyLink = async () => {
  try {
    const res = await surveyPublishApi.getSurveyLink(route.query.id)
    if (res.code === 200) {
      surveyLink.value = res.data
    } else {
      // 如果接口不存在，使用默认链接
      const baseUrl = window.location.origin
      surveyLink.value = `${baseUrl}/survey/fill/${route.query.id}`
    }
  } catch (error) {
    // 如果接口不存在，使用默认链接
    const baseUrl = window.location.origin
    surveyLink.value = `${baseUrl}/survey/fill/${route.query.id}`
  }
  // 加载链接后计算宽度
  calculateLinkWidth()
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
    const surveyId = route.query.id
    
    // 更新问卷基本信息（访问权限、密码、时间）
    const updateData = {
      ...publishForm,
      maxResponses: recoveryForm.maxResponses
    }
    
    const res = await surveyApi.updateSurvey(surveyId, updateData)
    if (res.code === 200) {
      // 保存回收限制设置
      const recoverySettings = {
        maxResponses: recoveryForm.maxResponses,
        ipWriteCountLimitStatus: recoveryForm.ipWriteCountLimitStatus,
        ipWriteCountLimit: recoveryForm.ipWriteCountLimitStatus ? recoveryForm.ipWriteCountLimit : 1,
        deviceWriteCountLimitStatus: recoveryForm.deviceWriteCountLimitStatus,
        deviceWriteCountLimit: recoveryForm.deviceWriteCountLimitStatus ? recoveryForm.deviceWriteCountLimit : 1,
        accountWriteCountLimitStatus: recoveryForm.accountWriteCountLimitStatus,
        accountWriteCountLimit: recoveryForm.accountWriteCountLimitStatus ? recoveryForm.accountWriteCountLimit : 1
      }
      
      try {
        await formApi.saveFormSetting(surveyId, recoverySettings)
      } catch (error) {
        // 保存回收限制设置失败，但不阻止发布流程
        // 如果接口不存在或出错，继续执行发布
      }
      
      // 发布问卷
      const publishRes = await surveyApi.publishSurvey(surveyId)
      if (publishRes.code === 200) {
        ElMessage.success('发布成功')
        survey.value.status = 'PUBLISHED'
        await loadSurveyData()
        // 发布成功后自动加载二维码
        await loadQRCode()
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

const handleCopyQRCode = async () => {
  if (!qrCodeBase64.value) {
    ElMessage.warning('请先生成二维码')
    return
  }
  
  try {
    // 将base64转换为blob
    const response = await fetch(qrCodeBase64.value)
    const blob = await response.blob()
    
    // 复制到剪贴板
    await navigator.clipboard.write([
      new ClipboardItem({ [blob.type]: blob })
    ])
    ElMessage.success('二维码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制二维码失败')
  }
}

const handleUnpublish = async () => {
  unpublishing.value = true
  try {
    const surveyId = route.query.id
    const res = await surveyApi.unpublishSurvey(surveyId)
    if (res.code === 200) {
      ElMessage.success('已停止发布')
      survey.value.status = 'ENDED'
      await loadSurveyData()
    }
  } catch (error) {
    ElMessage.error('停止发布失败')
  } finally {
    unpublishing.value = false
  }
}


onMounted(() => {
  loadSurveyData()
})
</script>

<style scoped>
.publish-container {
  padding: 20px;
}

/* 第一行：发布设置和回收限制 */
.cards-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.publish-settings-card,
.recovery-limits-card {
  flex: 1;
  width: 50%;
}

.publish-success-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 二维码和链接卡片内容 */
.publish-success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.qrcode-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.qrcode-image {
  width: 200px;
  height: 200px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 15px;
}

.qrcode-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.link-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.link-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.link-input-group {
  display: inline-flex;
  justify-content: center;
  margin-bottom: 15px;
  padding: 0 20px;
  position: relative;
}

.link-measure {
  position: absolute;
  visibility: hidden;
  white-space: nowrap;
  font-size: 14px;
  font-family: inherit;
  padding: 0;
  margin: 0;
  height: 0;
  overflow: hidden;
}

.link-input {
  min-width: 200px;
}

.link-button-wrapper {
  display: flex;
  justify-content: center;
}

.share-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.recovery-form :deep(.el-form-item__label) {
  white-space: nowrap;
  word-break: keep-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .publish-container {
    padding: 15px;
  }

  .cards-row {
    flex-direction: column;
    gap: 15px;
  }

  .publish-settings-card,
  .recovery-limits-card {
    width: 100%;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .card-header .el-button {
    width: 100%;
  }

  .publish-success-card {
    margin-bottom: 15px;
  }

  .publish-success-content {
    gap: 20px;
  }

  .qrcode-buttons {
    flex-direction: column;
    width: 100%;
  }

  .qrcode-buttons .el-button {
    width: 100%;
  }

  .link-section {
    max-width: 100%;
  }

  .link-input-group {
    width: 100%;
  }

  .link-button-wrapper {
    width: 100%;
  }

  .link-button-wrapper .el-button {
    width: 100%;
  }

  :deep(.el-form-item__label) {
    width: 100px !important;
  }

  :deep(.el-input),
  :deep(.el-date-picker),
  :deep(.el-input-number) {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .publish-container {
    padding: 10px;
  }

  .qrcode-image {
    width: 150px;
    height: 150px;
  }

  :deep(.el-form-item__label) {
    width: 80px !important;
    font-size: 14px;
  }
}
</style>

