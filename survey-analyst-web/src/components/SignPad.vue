<template>
  <div class="sign-pad-container">
    <!-- 如果已有签名图片，直接显示 -->
    <div
      v-if="imageUrl && (readonly || disabled)"
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
      <div class="sign-pad-wrapper">
        <canvas
          ref="canvasRef"
          class="sign-pad-canvas"
          :width="width"
          :height="height"
        />
      </div>
      <div class="sign-pad-actions">
        <el-button
          type="primary"
          size="small"
          :disabled="disabled || readonly || isEmpty"
          @click="handleSave"
        >
          保存
        </el-button>
        <el-button
          type="default"
          size="small"
          :disabled="disabled || readonly || canUndo === false"
          @click="handleUndo"
        >
          回撤
        </el-button>
        <el-button
          type="danger"
          size="small"
          :disabled="disabled || readonly"
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
  },
  readonly: {
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

// 初始化签名板
const initSignaturePad = () => {
  if (!canvasRef.value) return

  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')

  // 设置画布背景
  ctx.fillStyle = props.backgroundColor
  ctx.fillRect(0, 0, canvas.width, canvas.height)

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
      // 如果是在只读或禁用模式下，不重新初始化画布
      if (!props.readonly && !props.disabled) {
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
    if (newVal || props.readonly) {
      signaturePad.value.off()
    } else {
      signaturePad.value.on()
    }
  }
})

// 监听只读状态
watch(() => props.readonly, (newVal) => {
  if (signaturePad.value) {
    if (newVal || props.disabled) {
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
      // 重新设置画布高度
      canvasRef.value.height = props.height
      // 重新初始化
      initSignaturePad()
      // 恢复数据
      if (data && data.length > 0) {
        signaturePad.value.fromData(data)
        updateCanUndo()
      }
    }
  })
})

onMounted(() => {
  nextTick(() => {
    initSignaturePad()
  })
})

onBeforeUnmount(() => {
  if (signaturePad.value) {
    signaturePad.value.off()
  }
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

