<template>
  <div class="share-setting-view">
    <p class="setting-title">
      分享设置
    </p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules">
      <div class="setting-item">
        <p class="label">自定义分享图标</p>
        <el-switch v-model="form.shareWxImg" />
      </div>
      <div v-if="form.shareWxImg">
        <p class="setting-label">请上传分享图片 * 建议尺寸500px*500px</p>
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :show-file-list="false"
          accept=".jpg,.jpeg,.png,.gif,.bmp"
        >
          <el-image
            v-if="form.shareWxImgUrl"
            :src="form.shareWxImgUrl"
            class="share-img"
            fit="cover"
          />
          <div v-else class="upload-placeholder">
            <el-icon><Picture /></el-icon>
            <span>点击上传</span>
          </div>
        </el-upload>
      </div>
      
      <div class="setting-item">
        <p class="label">自定义分享标题</p>
        <el-switch v-model="form.shareWxTitle" />
      </div>
      <el-form-item
        v-if="form.shareWxTitle"
        prop="shareWxTitleContent"
        :rules="[{ required: true, message: '请输入标题', trigger: 'blur' }]"
      >
        <el-input
          v-model="form.shareWxTitleContent"
          placeholder="请输入标题"
        />
      </el-form-item>
      
      <div class="setting-item">
        <p class="label">自定义分享描述</p>
        <el-switch v-model="form.shareWxDesc" />
      </div>
      <el-form-item
        v-if="form.shareWxDesc"
        prop="shareWxDescContent"
        :rules="[{ required: true, message: '请输入描述', trigger: 'blur' }]"
      >
        <el-input
          v-model="form.shareWxDescContent"
          type="textarea"
          :rows="3"
          placeholder="请输入描述"
        />
      </el-form-item>
      
      <div class="submit-btn">
        <el-button :icon="Check" type="primary" @click="handleSave">保存设置</el-button>
      </div>
    </el-form>
    
    <!-- 预览 -->
    <div v-if="form.shareWxDesc || form.shareWxTitle || form.shareWxImg" class="share-preview">
      <div class="share-preview-msg">
        <p class="share-preview-msg-title">
          {{ form.shareWxTitleContent || '分享标题' }}
        </p>
        <div class="share-preview-body">
          <p class="share-preview-msg-desc">
            {{ form.shareWxDescContent || '分享描述' }}
          </p>
          <img
            v-if="form.shareWxImgUrl"
            :src="form.shareWxImgUrl"
            class="share-preview-img"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Check } from '@element-plus/icons-vue'
import { formApi } from '@/api'
import { getToken } from '@/utils/auth'

const route = useRoute()

const formRef = ref(null)
const surveyId = ref(null)
const form = ref({
  shareWxImg: false,
  shareWxImgUrl: null,
  shareWxTitle: false,
  shareWxTitleContent: null,
  shareWxDesc: false,
  shareWxDescContent: null
})

const rules = {}

const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || ''}/api/file/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${getToken()}`
  }
})

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200 && response.data) {
    form.value.shareWxImgUrl = response.data.url || response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 加载设置
const loadSetting = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)

  try {
    const res = await formApi.getFormSetting(surveyId.value)
    if (res.code === 200 && res.data && res.data.settings) {
      const settings = res.data.settings
      if (settings.shareWxImg !== undefined) {
        form.value.shareWxImg = settings.shareWxImg
      }
      if (settings.shareWxImgUrl) {
        form.value.shareWxImgUrl = settings.shareWxImgUrl
      }
      if (settings.shareWxTitle !== undefined) {
        form.value.shareWxTitle = settings.shareWxTitle
      }
      if (settings.shareWxTitleContent) {
        form.value.shareWxTitleContent = settings.shareWxTitleContent
      }
      if (settings.shareWxDesc !== undefined) {
        form.value.shareWxDesc = settings.shareWxDesc
      }
      if (settings.shareWxDescContent) {
        form.value.shareWxDescContent = settings.shareWxDescContent
      }
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

// 保存设置
const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.value.shareWxImg && !form.value.shareWxImgUrl) {
        ElMessage.error('请上传分享显示图片')
        return
      }
      
      try {
        const settings = {
          shareWxImg: form.value.shareWxImg,
          shareWxImgUrl: form.value.shareWxImgUrl,
          shareWxTitle: form.value.shareWxTitle,
          shareWxTitleContent: form.value.shareWxTitleContent,
          shareWxDesc: form.value.shareWxDesc,
          shareWxDescContent: form.value.shareWxDescContent
        }
        const res = await formApi.saveFormSetting(surveyId.value, settings)
        if (res.code === 200) {
          ElMessage.success('保存成功')
          await loadSetting()
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

onMounted(() => {
  loadSetting()
})
</script>

<style lang="scss" scoped>
.share-setting-view {
  padding: 20px;
}

.setting-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  
  .setting-desc {
    font-size: 12px;
    color: #909399;
    margin-left: 10px;
  }
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  .label {
    width: 200px;
    margin: 0;
    font-size: 14px;
    line-height: 32px;
    white-space: nowrap;
    flex-shrink: 0;
  }
}

.setting-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}

:deep(.el-input), :deep(.el-textarea) {
  font-size: 14px;
}

:deep(.el-input__inner), :deep(.el-textarea__inner) {
  font-size: 14px;
}

.share-img {
  width: 200px;
  height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.upload-placeholder {
  width: 200px;
  height: 200px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  
  &:hover {
    border-color: #409eff;
  }
}

.submit-btn {
  margin-top: 30px;
}

.share-preview {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.share-preview-msg {
  width: 360px;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 15px;
}

.share-preview-msg-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #303133;
}

.share-preview-body {
  display: flex;
  gap: 10px;
}

.share-preview-msg-desc {
  flex: 1;
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.share-preview-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

@media (max-width: 768px) {
  .share-setting-view {
    padding: 15px 10px;
  }

  .setting-title {
    font-size: 13px;
  }

  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    
    .label {
      width: 100%;
      line-height: 1.5;
      font-size: 13px;
    }
  }

  .setting-label {
    font-size: 12px;
  }

  :deep(.el-input), :deep(.el-textarea) {
    width: 100%;
  }
}
</style>

