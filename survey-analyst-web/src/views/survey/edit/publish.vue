<template>
  <div class="publish-container">
    <div class="cards-row">
      <el-card class="recovery-limits-card">
        <template #header>
          <span>回收设置</span>
        </template>

        <el-form :model="recoveryForm" label-width="180px" class="recovery-form">
          <el-form-item>
            <template #label>
              <span>最大填写数</span>
              <el-tooltip
                content="0表示不限制"
                placement="top"
              >
                <el-icon class="hint-icon">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-input-number
              v-model="recoveryForm.maxResponses"
              :min="0"
              :formatter="formatLimitValue"
              :parser="parseLimitValue"
              placeholder="不限制"
              style="width: 300px"
            />
          </el-form-item>

          <el-form-item>
            <template #label>
              <span>每个IP答题次数限制</span>
              <el-tooltip
                content="0表示不限制"
                placement="top"
              >
                <el-icon class="hint-icon">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-input-number
              v-model="recoveryForm.ipWriteCountLimit"
              :min="0"
              :formatter="formatLimitValue"
              :parser="parseLimitValue"
              placeholder="不限制"
              style="width: 300px"
            />
          </el-form-item>

          <el-form-item>
            <template #label>
              <span>每个设备答题次数限制</span>
              <el-tooltip
                content="0表示不限制"
                placement="top"
              >
                <el-icon class="hint-icon">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-input-number
              v-model="recoveryForm.deviceWriteCountLimit"
              :min="0"
              :formatter="formatLimitValue"
              :parser="parseLimitValue"
              placeholder="不限制"
              style="width: 300px"
            />
          </el-form-item>

          <el-form-item>
            <template #label>
              <span>每个用户答题次数限制</span>
              <el-tooltip
                content="0表示不限制"
                placement="top"
              >
                <el-icon class="hint-icon">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </template>
            <el-input-number
              v-model="recoveryForm.accountWriteCountLimit"
              :min="0"
              :formatter="formatLimitValue"
              :parser="parseLimitValue"
              placeholder="不限制"
              style="width: 300px"
            />
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

        <el-form :model="publishForm" :label-width="publishFormLabelWidth" class="publish-form">
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
import { ref, reactive, onMounted, watch, nextTick, computed, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Promotion, Link, Download, Close, DocumentCopy, QuestionFilled } from '@element-plus/icons-vue'
import { surveyApi, surveyPublishApi, formApi } from '@/api'
import dayjs from 'dayjs'

const route = useRoute()

// 检测是否为手机端
const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 计算表单的label-width，手机端使用0
const publishFormLabelWidth = computed(() => {
  return isMobile.value ? '0px' : '120px'
})

const survey = ref({})
const publishForm = reactive({
  accessType: 'PUBLIC',
  password: '',
  startTime: null,
  endTime: null
})

const recoveryForm = reactive({
  maxResponses: 0,
  ipWriteCountLimit: 0,
  deviceWriteCountLimit: 0,
  accountWriteCountLimit: 0
})

// 格式化函数：0显示为"不限制"
const formatLimitValue = (value) => {
  if (value === null || value === undefined || value === 0 || value === '0') {
    return '不限制'
  }
  const num = Number(value)
  return isNaN(num) ? '不限制' : String(num)
}

// 解析函数："不限制"转换为0
const parseLimitValue = (value) => {
  if (value === '不限制' || value === '' || value === null || value === undefined) {
    return 0
  }
  const num = Number(value)
  return isNaN(num) ? 0 : num
}

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
      recoveryForm.maxResponses = survey.value.maxResponses ?? settings.maxResponses ?? 0
      
      // IP限制
      if (settings.ipWriteCountLimitStatus === false) {
        // 开关为false，表示未启用限制
        recoveryForm.ipWriteCountLimit = 0
      } else if (settings.ipWriteCountLimitStatus === true && settings.ipWriteCountLimit !== undefined && settings.ipWriteCountLimit !== null) {
        // 开关为true，使用实际限制值
        recoveryForm.ipWriteCountLimit = settings.ipWriteCountLimit
      } else if (settings.ipWriteCountLimit !== undefined && settings.ipWriteCountLimit !== null) {
        // 没有开关状态，但有值：如果值为1（旧默认值），转换为0；否则使用实际值
        recoveryForm.ipWriteCountLimit = settings.ipWriteCountLimit === 1 ? 0 : settings.ipWriteCountLimit
      } else {
        recoveryForm.ipWriteCountLimit = 0
      }
      
      // 设备限制
      if (settings.deviceWriteCountLimitStatus === false) {
        recoveryForm.deviceWriteCountLimit = 0
      } else if (settings.deviceWriteCountLimitStatus === true && settings.deviceWriteCountLimit !== undefined && settings.deviceWriteCountLimit !== null) {
        recoveryForm.deviceWriteCountLimit = settings.deviceWriteCountLimit
      } else if (settings.deviceWriteCountLimit !== undefined && settings.deviceWriteCountLimit !== null) {
        recoveryForm.deviceWriteCountLimit = settings.deviceWriteCountLimit === 1 ? 0 : settings.deviceWriteCountLimit
      } else {
        recoveryForm.deviceWriteCountLimit = 0
      }
      
      // 用户限制
      if (settings.accountWriteCountLimitStatus === false) {
        recoveryForm.accountWriteCountLimit = 0
      } else if (settings.accountWriteCountLimitStatus === true && settings.accountWriteCountLimit !== undefined && settings.accountWriteCountLimit !== null) {
        recoveryForm.accountWriteCountLimit = settings.accountWriteCountLimit
      } else if (settings.accountWriteCountLimit !== undefined && settings.accountWriteCountLimit !== null) {
        recoveryForm.accountWriteCountLimit = settings.accountWriteCountLimit === 1 ? 0 : settings.accountWriteCountLimit
      } else {
        recoveryForm.accountWriteCountLimit = 0
      }
    } else {
      // 如果没有设置，尝试从问卷数据中获取最大填写数
      recoveryForm.maxResponses = survey.value.maxResponses ?? 0
    }
  } catch (error) {
    // 如果接口不存在，使用默认值
    recoveryForm.maxResponses = survey.value.maxResponses ?? 0
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
        maxResponses: recoveryForm.maxResponses ?? 0,
        ipWriteCountLimit: recoveryForm.ipWriteCountLimit ?? 0,
        deviceWriteCountLimit: recoveryForm.deviceWriteCountLimit ?? 0,
        accountWriteCountLimit: recoveryForm.accountWriteCountLimit ?? 0
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
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadSurveyData()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.publish-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  /* 自定义滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
}

.publish-container::-webkit-scrollbar {
  width: 6px;
}

.publish-container::-webkit-scrollbar-track {
  background: transparent;
}

.publish-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 3px;
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

.recovery-form .hint-icon {
  margin-left: 5px;
  color: #909399;
  font-size: 14px;
  cursor: help;
  vertical-align: middle;
}

.recovery-form .hint-icon:hover {
  color: #409eff;
}

.recovery-form :deep(.el-form-item__label) {
  white-space: nowrap;
  word-break: keep-all;
  line-height: 32px;
  padding-right: 12px;
}

.card-header span {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .publish-container {
    padding: 15px;
  }

  .recovery-form :deep(.el-form-item) {
    flex-direction: column;
    align-items: flex-start !important;
  }

  .recovery-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left !important;
    margin-bottom: 8px;
    padding-right: 0;
    line-height: 1.5;
    white-space: normal;
    word-break: break-word;
  }

  .recovery-form :deep(.el-form-item__content) {
    width: 100%;
    margin-left: 0 !important;
  }

  .recovery-form :deep(.el-input-number) {
    width: 100% !important;
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

  /* 发布设置表单样式 - 确保手机端文字靠左 */
  .publish-settings-card .publish-form :deep(.el-form-item) {
    flex-direction: column !important;
    align-items: flex-start !important;
    display: flex !important;
    flex-wrap: nowrap !important;
  }

  .publish-settings-card .publish-form :deep(.el-form-item__label) {
    width: 100% !important;
    max-width: 100% !important;
    text-align: left !important;
    justify-content: flex-start !important;
    align-items: flex-start !important;
    margin-bottom: 8px !important;
    padding-right: 0 !important;
    padding-left: 0 !important;
    line-height: 1.5 !important;
    white-space: nowrap !important;
    word-break: keep-all !important;
    word-wrap: normal !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    margin-right: 0 !important;
    margin-left: 0 !important;
    display: block !important;
  }

  /* 确保标签内的所有元素都左对齐且不换行 */
  .publish-settings-card .publish-form :deep(.el-form-item__label *),
  .publish-settings-card .publish-form :deep(.el-form-item__label span),
  .publish-settings-card .publish-form :deep(.el-form-item__label div) {
    text-align: left !important;
    white-space: nowrap !important;
    word-break: keep-all !important;
    word-wrap: normal !important;
    display: inline !important;
  }

  .publish-settings-card .publish-form :deep(.el-form-item__content) {
    width: 100% !important;
    margin-left: 0 !important;
    text-align: left !important;
    justify-content: flex-start !important;
  }

  .publish-settings-card .publish-form :deep(.el-radio-group) {
    display: flex !important;
    flex-direction: column !important;
    align-items: flex-start !important;
    justify-content: flex-start !important;
    gap: 8px;
  }

  .publish-settings-card .publish-form :deep(.el-radio) {
    margin-right: 0 !important;
    text-align: left !important;
  }

  .publish-settings-card .publish-form :deep(.el-radio__label) {
    text-align: left !important;
  }

  /* 排除发布设置表单的全局样式 */
  .publish-settings-card ~ * :deep(.el-form-item__label),
  .recovery-limits-card :deep(.el-form-item__label) {
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

  /* 发布设置表单在更小屏幕上的样式 */
  .publish-settings-card .publish-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left !important;
    font-size: 14px !important;
    white-space: nowrap !important;
    word-break: keep-all !important;
    word-wrap: normal !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
  }

  /* 确保标签内的所有元素都不换行 */
  .publish-settings-card .publish-form :deep(.el-form-item__label *),
  .publish-settings-card .publish-form :deep(.el-form-item__label span),
  .publish-settings-card .publish-form :deep(.el-form-item__label div) {
    white-space: nowrap !important;
    word-break: keep-all !important;
    word-wrap: normal !important;
  }

  /* 其他表单的标签样式 */
  .recovery-limits-card :deep(.el-form-item__label) {
    width: 80px !important;
    font-size: 14px;
  }
}
</style>

