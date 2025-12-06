<template>
  <el-dialog
    v-model="visible"
    title="问卷预览"
    width="90%"
    :fullscreen="false"
    :close-on-click-modal="false"
    destroy-on-close
    class="survey-preview-dialog"
    @close="handleClose"
  >
    <div class="preview-container">
      <!-- 视图切换按钮 -->
      <div class="preview-tabs">
        <el-button
          :type="viewMode === 'mobile' ? 'primary' : ''"
          :plain="viewMode !== 'mobile'"
          @click="viewMode = 'mobile'"
        >
          <el-icon><Iphone /></el-icon>
          <span>手机</span>
        </el-button>
        <el-button
          :type="viewMode === 'desktop' ? 'primary' : ''"
          :plain="viewMode !== 'desktop'"
          @click="viewMode = 'desktop'"
        >
          <el-icon><Monitor /></el-icon>
          <span>电脑</span>
        </el-button>
      </div>

      <!-- 移动端预览 -->
      <div v-if="viewMode === 'mobile'" class="mobile-preview-wrapper">
        <div class="preview-layer">
          <div class="preview-phone">
            <div class="phone-content">
              <div class="preview-form-container">
                <div class="form-title">{{ formName || '未命名问卷' }}</div>
                <el-scrollbar height="calc(100vh - 200px)">
                  <SurveyFormRender
                    :form-items="formItems"
                    :form-model="previewFormModel"
                    :preview-mode="true"
                  />
                </el-scrollbar>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 二维码预览区域 -->
        <div v-if="showQrcode && props.formKey" class="qrcode-view">
          <p class="qrcode-title">手机扫码预览</p>
          <p class="tips-text">* 预览仅查看效果，无法提交数据</p>
          <div class="qrcode-container">
            <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="二维码" class="qrcode-image" />
            <div v-else class="qrcode-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- PC端预览 -->
      <div v-if="viewMode === 'desktop'" class="desktop-preview-wrapper">
        <el-scrollbar height="calc(100vh - 200px)">
          <div class="desktop-form-container">
            <div class="form-title">{{ formName || '未命名问卷' }}</div>
            <SurveyFormRender
              :form-items="formItems"
              :form-model="previewFormModel"
              :preview-mode="true"
            />
          </div>
        </el-scrollbar>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { Iphone, Monitor, Loading } from '@element-plus/icons-vue'
import SurveyFormRender from './SurveyFormRender.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  formName: {
    type: String,
    default: '未命名问卷'
  },
  formItems: {
    type: Array,
    default: () => []
  },
  formKey: {
    type: String,
    default: ''
  },
  showQrcode: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const viewMode = ref('mobile') // 'mobile' 或 'desktop'
const qrCodeUrl = ref('')
const previewFormModel = reactive({})

// 监听外部传入的显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    initPreviewForm()
    if (props.showQrcode && props.formKey) {
      loadQRCode()
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 初始化预览表单数据
const initPreviewForm = () => {
  // 清空之前的表单数据
  Object.keys(previewFormModel).forEach(key => {
    delete previewFormModel[key]
  })
  
  // 初始化表单模型
  props.formItems.forEach(item => {
    if (item.type === 'CHECKBOX') {
      previewFormModel[item.vModel] = []
    } else {
      previewFormModel[item.vModel] = item.defaultValue || ''
    }
  })
}

// 加载二维码
const loadQRCode = async () => {
  if (!props.formKey) {
    return
  }
  
  try {
    const QRCode = (await import('qrcode')).default
    // 生成预览链接，实际使用时应该指向真实的问卷填写页面
    const url = `${window.location.protocol}//${window.location.host}/survey/fill?key=${props.formKey}`
    qrCodeUrl.value = await QRCode.toDataURL(url, {
      width: 200,
      margin: 1,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
  } catch (error) {
    console.error('生成二维码失败:', error)
    qrCodeUrl.value = ''
  }
}

const handleClose = () => {
  visible.value = false
  viewMode.value = 'mobile'
  qrCodeUrl.value = ''
}
</script>

<style lang="scss" scoped>
.survey-preview-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    background-color: #f5f7fa;
    min-height: 600px;
  }
}

.preview-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.preview-tabs {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .el-button {
    padding: 10px 30px;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    span {
      margin-left: 4px;
    }
  }
}

.mobile-preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 40px;
  position: relative;
}

.preview-layer {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  max-width: 500px;
}

.preview-phone {
  width: 372px;
  height: 744px;
  background: #fff;
  border: 8px solid #1a1a1a;
  border-radius: 40px;
  padding: 0;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: 50%;
    transform: translateX(-50%);
    width: 120px;
    height: 25px;
    background: #1a1a1a;
    border-radius: 0 0 15px 15px;
    z-index: 10;
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: 10px;
    left: 50%;
    transform: translateX(-50%);
    width: 150px;
    height: 5px;
    background: #333;
    border-radius: 5px;
    z-index: 10;
  }
}

.phone-content {
  width: 100%;
  height: 100%;
  background: white;
  border-radius: 32px;
  overflow: hidden;
  position: relative;
  padding-top: 35px;
  box-sizing: border-box;
}

.preview-form-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  
  .form-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    text-align: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #ebeef5;
  }
}

.qrcode-view {
  position: absolute;
  top: 20px;
  right: 40px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
  min-width: 200px;
  
  .qrcode-title {
    font-weight: bold;
    font-size: 16px;
    margin: 0 0 10px 0;
    color: #303133;
  }
  
  .tips-text {
    font-size: 12px;
    color: #909399;
    margin: 10px 0;
  }
  
  .qrcode-container {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 10px;
    
    .qrcode-image {
      width: 180px;
      height: 180px;
      border-radius: 8px;
      border: 1px solid #ebeef5;
    }
    
    .qrcode-loading {
      width: 180px;
      height: 180px;
      display: flex;
      justify-content: center;
      align-items: center;
      
      .el-icon {
        font-size: 32px;
        color: #409eff;
      }
    }
  }
}

.desktop-preview-wrapper {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.desktop-form-container {
  padding: 40px;
  min-height: 500px;
  
  .form-title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    text-align: center;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 2px solid #ebeef5;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .qrcode-view {
    position: static;
    margin-top: 20px;
    right: auto;
  }
  
  .mobile-preview-wrapper {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .survey-preview-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 0 auto;
    }
    
    :deep(.el-dialog__body) {
      padding: 10px;
    }
  }
  
  .preview-tabs {
    flex-wrap: wrap;
    gap: 8px;
    
    .el-button {
      padding: 8px 20px;
      font-size: 12px;
    }
  }
  
  .preview-phone {
    width: 280px;
    height: 568px;
    border-radius: 30px;
    border-width: 6px;
    
    &::before {
      width: 100px;
      height: 20px;
    }
    
    &::after {
      width: 120px;
      height: 4px;
    }
  }
  
  .phone-content {
    padding-top: 25px;
    border-radius: 24px;
  }
  
  .preview-form-container {
    padding: 12px;
    
    .form-title {
      font-size: 16px;
      margin-bottom: 15px;
      padding-bottom: 10px;
    }
  }
  
  .qrcode-view {
    width: 100%;
    max-width: 180px;
    padding: 12px;
    
    .qrcode-title {
      font-size: 14px;
    }
    
    .tips-text {
      font-size: 11px;
    }
    
    .qrcode-container .qrcode-image {
      width: 120px;
      height: 120px;
    }
  }
  
  .desktop-form-container {
    padding: 15px;
    
    .form-title {
      font-size: 20px;
      margin-bottom: 20px;
      padding-bottom: 15px;
    }
  }
}
</style>
