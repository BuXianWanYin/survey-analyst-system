<template>
  <div class="sign-pad-container">
    <!-- 如果已有签名图片，直接显示 -->
    <div
      v-if="imageUrl && disabled"
      class="sign-pad-preview-only"
    >
      <el-image
        :src="imageUrl"
        fit="contain"
        class="sign-pad-image"
      />
    </div>
    <!-- 编辑模式：显示画布和操作按钮 -->
    <template v-else>
      <div
        class="sign-pad-wrapper"
        :style="{ height: height + 'px' }"
      >
        <canvas
          ref="canvasRef"
          class="sign-pad-canvas"
        />
      </div>
      <div class="sign-pad-actions">
        <el-button
          :icon="Check"
          type="primary"
          size="small"
          :disabled="disabled || isEmpty"
          @click="handleSave"
        >
          保存
        </el-button>
        <el-button
          :icon="RefreshLeft"
          type="default"
          size="small"
          :disabled="disabled || canUndo === false"
          @click="handleUndo"
        >
          回撤
        </el-button>
        <el-button
          :icon="Delete"
          type="danger"
          size="small"
          :disabled="disabled"
          @click="handleClear"
        >
          清除
        </el-button>
      </div>
      <div
        v-if="imageUrl"
        class="sign-pad-preview"
      >
        <el-image
          :src="imageUrl"
          fit="contain"
          class="sign-pad-image"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
/**
 * 签名板组件
 * 功能：提供手写签名功能，支持保存、清除、回撤等操作，可导出为Base64图片
 */

import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Check, RefreshLeft, Delete } from '@element-plus/icons-vue'
import SignaturePad from 'signature_pad'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  width: {
    type: Number,
    default: 600
  },
  height: {
    type: Number,
    default: 300
  },
  backgroundColor: {
    type: String,
    default: '#ffffff'
  },
  penColor: {
    type: String,
    default: '#000000'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const canvasRef = ref(null)
const signaturePad = ref(null)
const imageUrl = ref(props.modelValue || '')
const isEmpty = ref(true)
const canUndo = ref(false)

/**
 * 更新是否可以回撤的状态
 */
const updateCanUndo = () => {
  if (signaturePad.value) {
    const data = signaturePad.value.toData()
    canUndo.value = data && data.length > 0
  } else {
    canUndo.value = false
  }
}

/**
 * 调整canvas尺寸以匹配显示尺寸
 * 考虑设备像素比以获得更清晰的绘制效果
 * @returns {Object} 返回显示宽度和高度
 */
const resizeCanvas = () => {
  if (!canvasRef.value) return

  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  const dpr = window.devicePixelRatio || 1

  const displayWidth = rect.width > 0 ? Math.floor(rect.width) : props.width
  const displayHeight = rect.height > 0 ? Math.floor(rect.height) : props.height

  if (rect.width === 0 || rect.height === 0) {
    canvas.style.width = displayWidth + 'px'
    canvas.style.height = displayHeight + 'px'
    return { displayWidth, displayHeight }
  }

  canvas.width = displayWidth * dpr
  canvas.height = displayHeight * dpr

  const ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)

  canvas.style.width = displayWidth + 'px'
  canvas.style.height = displayHeight + 'px'

  return { displayWidth, displayHeight }
}

/**
 * 初始化签名板
 * 创建SignaturePad实例，设置画布背景，监听签名变化事件
 */
const initSignaturePad = () => {
  if (!canvasRef.value) return

  const canvas = canvasRef.value
  
  const { displayWidth, displayHeight } = resizeCanvas()
  
  if (displayWidth === 0 || displayHeight === 0) {
    requestAnimationFrame(() => {
      initSignaturePad()
    })
    return
  }
  
  const ctx = canvas.getContext('2d')

  ctx.fillStyle = props.backgroundColor
  ctx.fillRect(0, 0, displayWidth, displayHeight)

  signaturePad.value = new SignaturePad(canvas, {
    backgroundColor: props.backgroundColor,
    penColor: props.penColor,
    minWidth: 1,
    maxWidth: 3,
    throttle: 16,
    minDistance: 5
  })

  signaturePad.value.addEventListener('beginStroke', () => {
    isEmpty.value = false
    updateCanUndo()
  })

  signaturePad.value.addEventListener('endStroke', () => {
    isEmpty.value = signaturePad.value.isEmpty()
    updateCanUndo()
  })

  if (props.modelValue) {
    imageUrl.value = props.modelValue
    isEmpty.value = false
  }
}

/**
 * 回撤签名
 * 移除最后一个笔画并重新绘制剩余内容
 */
const handleUndo = () => {
  if (signaturePad.value) {
    const data = signaturePad.value.toData()
    if (data && data.length > 0) {
      data.pop()
      signaturePad.value.clear()
      if (data.length > 0) {
        signaturePad.value.fromData(data)
      }
      isEmpty.value = signaturePad.value.isEmpty()
      updateCanUndo()
    }
  }
}

/**
 * 清除签名
 * 清除画布并重置状态
 */
const handleClear = () => {
  if (signaturePad.value) {
    signaturePad.value.clear()
    isEmpty.value = true
    canUndo.value = false
    imageUrl.value = ''
    emit('update:modelValue', '')
    emit('change', '')
  }
}

/**
 * 保存签名
 * 将签名转换为Base64图片并触发更新事件
 */
const handleSave = () => {
  if (!signaturePad.value || signaturePad.value.isEmpty()) {
    return
  }

  const dataURL = signaturePad.value.toDataURL('image/png')
  imageUrl.value = dataURL
  emit('update:modelValue', dataURL)
  emit('change', dataURL)
}

watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal !== imageUrl.value) {
    imageUrl.value = newVal
    isEmpty.value = false
    if (signaturePad.value) {
      signaturePad.value.clear()
      if (!props.disabled) {
        const canvas = canvasRef.value
        if (canvas) {
          const ctx = canvas.getContext('2d')
          ctx.fillStyle = props.backgroundColor
          ctx.fillRect(0, 0, canvas.width, canvas.height)
        }
      }
    }
  } else if (!newVal) {
    imageUrl.value = ''
    isEmpty.value = true
    if (signaturePad.value) {
      signaturePad.value.clear()
    }
  }
})

watch(() => props.disabled, (newVal) => {
  if (signaturePad.value) {
    if (newVal) {
      signaturePad.value.off()
    } else {
      signaturePad.value.on()
    }
  }
})

watch(() => props.penColor, (newColor) => {
  if (signaturePad.value) {
    signaturePad.value.penColor = newColor
  }
})

watch(() => props.height, () => {
  nextTick(() => {
    if (signaturePad.value && canvasRef.value) {
      const data = signaturePad.value.toData()
      initSignaturePad()
      if (data && data.length > 0) {
        signaturePad.value.fromData(data)
        updateCanUndo()
      }
    }
  })
})

/**
 * 监听窗口大小变化，重新调整canvas尺寸
 */
const handleResize = () => {
  if (signaturePad.value && canvasRef.value) {
    const data = signaturePad.value.toData()
    resizeCanvas()
    const canvas = canvasRef.value
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = props.backgroundColor
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    if (data && data.length > 0) {
      signaturePad.value.fromData(data)
      updateCanUndo()
    }
  }
}

onMounted(() => {
  nextTick(() => {
    initSignaturePad()
    window.addEventListener('resize', handleResize)
  })
})

onBeforeUnmount(() => {
  if (signaturePad.value) {
    signaturePad.value.off()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.sign-pad-container {
  width: 100%;
}

.sign-pad-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  cursor: crosshair;
  width: 100%;
  max-width: 100%;
}

.sign-pad-canvas {
  display: block;
  width: 100%;
  max-width: 100%;
  height: auto;
  touch-action: none;
  /* 确保 canvas 尺寸由 JavaScript 控制 */
  box-sizing: border-box;
}

.sign-pad-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

.sign-pad-preview {
  margin-top: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background: #f5f7fa;
  text-align: center;
}

.sign-pad-preview-only {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background: #f5f7fa;
  text-align: center;
}

.sign-pad-image {
  max-width: 100%;
  max-height: 200px;
  display: block;
  margin: 0 auto;
}
</style>

