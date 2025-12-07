<template>
  <div class="theme-container">
    <el-card>
      <template #header>
        <span>外观设置</span>
      </template>
      <el-form :model="themeForm" label-width="120px">
        <el-form-item label="主题颜色">
          <el-color-picker v-model="themeForm.themeColor" />
        </el-form-item>
        <el-form-item label="背景颜色">
          <el-color-picker v-model="themeForm.backgroundColor" />
        </el-form-item>
        <el-form-item label="背景图片">
          <el-input v-model="themeForm.backgroundImg" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="提交按钮文字">
          <el-input v-model="themeForm.submitBtnText" placeholder="提交" />
        </el-form-item>
        <el-form-item label="显示选项">
          <el-checkbox v-model="themeForm.showTitle">显示标题</el-checkbox>
          <el-checkbox v-model="themeForm.showDescribe">显示描述</el-checkbox>
          <el-checkbox v-model="themeForm.showNumber">显示序号</el-checkbox>
          <el-checkbox v-model="themeForm.showSubmitBtn">显示提交按钮</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formApi } from '@/api'

const route = useRoute()
const surveyId = ref(null)

const themeForm = ref({
  surveyId: null,
  themeColor: '#409EFF',
  backgroundColor: '#ffffff',
  backgroundImg: '',
  submitBtnText: '提交',
  showTitle: true,
  showDescribe: true,
  showNumber: false,
  showSubmitBtn: true
})

// 加载主题
const loadTheme = async () => {
  const id = route.query.id
  if (!id) return

  surveyId.value = Number(id)
  themeForm.value.surveyId = surveyId.value

  try {
    const res = await formApi.getFormTheme(surveyId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.themeColor) themeForm.value.themeColor = data.themeColor
      if (data.backgroundColor) themeForm.value.backgroundColor = data.backgroundColor
      if (data.backgroundImg) themeForm.value.backgroundImg = data.backgroundImg
      if (data.submitBtnText) themeForm.value.submitBtnText = data.submitBtnText
      if (data.showTitle !== undefined) themeForm.value.showTitle = data.showTitle
      if (data.showDescribe !== undefined) themeForm.value.showDescribe = data.showDescribe
      if (data.showNumber !== undefined) themeForm.value.showNumber = data.showNumber
      if (data.showSubmitBtn !== undefined) themeForm.value.showSubmitBtn = data.showSubmitBtn
    }
  } catch (error) {
    // 如果不存在，使用默认值
  }
}

// 保存主题
const handleSave = async () => {
  try {
    const themeData = {
      surveyId: surveyId.value,
      themeColor: themeForm.value.themeColor,
      backgroundColor: themeForm.value.backgroundColor,
      backgroundImg: themeForm.value.backgroundImg,
      submitBtnText: themeForm.value.submitBtnText,
      showTitle: themeForm.value.showTitle,
      showDescribe: themeForm.value.showDescribe,
      showNumber: themeForm.value.showNumber,
      showSubmitBtn: themeForm.value.showSubmitBtn
    }
    const res = await formApi.saveFormTheme(themeData)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      await loadTheme()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadTheme()
})
</script>

<style lang="scss" scoped>
.theme-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style>

