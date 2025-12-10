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

// 更新是否可以回撤的状态
const updateCanUndo = () => {
  if (signaturePad.value) {
    // signature_pad 通过检查数据点数组来判断是否可以回撤
    // 如果数据点数组长度大于0，说明可以回撤
    const data = signaturePad.value.toData()
    canUndo.value = data && data.length > 0
  } else {
    canUndo.value = false
  }
}

// 调整 canvas 尺寸以匹配显示尺寸
const resizeCanvas = () => {
  if (!canvasRef.value) return

  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  const dpr = window.devicePixelRatio || 1

  // 获取显示尺寸（从父容器获取，如果没有则使用 props）
  const displayWidth = rect.width > 0 ? Math.floor(rect.width) : props.width
  const displayHeight = rect.height > 0 ? Math.floor(rect.height) : props.height

  // 如果 canvas 已经有正确的显示尺寸，直接使用
  if (rect.width === 0 || rect.height === 0) {
    // 如果还没有渲染，先设置 CSS 尺寸
    canvas.style.width = displayWidth + 'px'
    canvas.style.height = displayHeight + 'px'
    // 等待下一帧再获取实际尺寸
    return { displayWidth, displayHeight }
  }

  // 设置 canvas 内部尺寸（考虑设备像素比以获得更清晰的绘制）
  canvas.width = displayWidth * dpr
  canvas.height = displayHeight * dpr

  // 获取上下文并缩放
  const ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)

  // 设置 CSS 尺寸为显示尺寸（确保显示尺寸正确）
  canvas.style.width = displayWidth + 'px'
  canvas.style.height = displayHeight + 'px'

  return { displayWidth, displayHeight }
}

// 初始化签名板
const initSignaturePad = () => {
  if (!canvasRef.value) return

  const canvas = canvasRef.value
  
  // 调整 canvas 尺寸（可能需要等待一帧以确保尺寸正确）
  const { displayWidth, displayHeight } = resizeCanvas()
  
  // 如果尺寸还没有准备好，等待下一帧
  if (displayWidth === 0 || displayHeight === 0) {
    requestAnimationFrame(() => {
      initSignaturePad()
    })
    return
  }
  
  const ctx = canvas.getContext('2d')

  // 设置画布背景（使用显示尺寸，因为上下文已经缩放）
  ctx.fillStyle = props.backgroundColor
  ctx.fillRect(0, 0, displayWidth, displayHeight)

  // 初始化 SignaturePad
  signaturePad.value = new SignaturePad(canvas, {
    backgroundColor: props.backgroundColor,
    penColor: props.penColor,
    minWidth: 1,
    maxWidth: 3,
    throttle: 16,
    minDistance: 5
  })

  // 监听签名变化
  signaturePad.value.addEventListener('beginStroke', () => {
    isEmpty.value = false
    updateCanUndo()
  })

  signaturePad.value.addEventListener('endStroke', () => {
    isEmpty.value = signaturePad.value.isEmpty()
    updateCanUndo()
  })

  // 如果已有图片，加载显示（但不阻止重新编辑）
  if (props.modelValue) {
    imageUrl.value = props.modelValue
    isEmpty.value = false
  }
}

// 回撤签名
const handleUndo = () => {
  if (signaturePad.value) {
    const data = signaturePad.value.toData()
    if (data && data.length > 0) {
      // 移除最后一个笔画
      data.pop()
      // 清除画布并重新绘制
      signaturePad.value.clear()
      // 重新绘制剩余的笔画
      if (data.length > 0) {
        signaturePad.value.fromData(data)
      }
      isEmpty.value = signaturePad.value.isEmpty()
      updateCanUndo()
    }
  }
}

// 清除签名
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

// 保存签名
const handleSave = () => {
  if (!signaturePad.value || signaturePad.value.isEmpty()) {
    return
  }

  // 将签名转换为 Base64 图片
  const dataURL = signaturePad.value.toDataURL('image/png')
  imageUrl.value = dataURL
  emit('update:modelValue', dataURL)
  emit('change', dataURL)
}

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal !== imageUrl.value) {
    imageUrl.value = newVal
    isEmpty.value = false
    // 如果签名板已初始化，清除画布（允许重新编辑）
    if (signaturePad.value) {
      signaturePad.value.clear()
      // 如果是在禁用模式下，不重新初始化画布
      if (!props.disabled) {
        // 重新设置背景
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

// 监听禁用状态
watch(() => props.disabled, (newVal) => {
  if (signaturePad.value) {
    if (newVal) {
      signaturePad.value.off()
    } else {
      signaturePad.value.on()
    }
  }
})

// 监听笔触颜色变化
watch(() => props.penColor, (newColor) => {
  if (signaturePad.value) {
    signaturePad.value.penColor = newColor
  }
})

// 监听高度变化
watch(() => props.height, () => {
  nextTick(() => {
    if (signaturePad.value && canvasRef.value) {
      // 保存当前数据
      const data = signaturePad.value.toData()
      // 重新初始化（会自动调整尺寸）
      initSignaturePad()
      // 恢复数据
      if (data && data.length > 0) {
        signaturePad.value.fromData(data)
        updateCanUndo()
      }
    }
  })
})

// 监听窗口大小变化，重新调整 canvas 尺寸
const handleResize = () => {
  if (signaturePad.value && canvasRef.value) {
    // 保存当前数据
    const data = signaturePad.value.toData()
    // 重新调整尺寸
    resizeCanvas()
    // 重新设置背景
    const canvas = canvasRef.value
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = props.backgroundColor
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    // 恢复数据
    if (data && data.length > 0) {
      signaturePad.value.fromData(data)
      updateCanUndo()
    }
  }
}

onMounted(() => {
  nextTick(() => {
    initSignaturePad()
    // 监听窗口大小变化
    window.addEventListener('resize', handleResize)
  })
})

onBeforeUnmount(() => {
  if (signaturePad.value) {
    signaturePad.value.off()
  }
  // 移除窗口大小变化监听
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

