<template>
  <div class="logic-container">
    <el-scrollbar class="scrollbar-container">
      <div class="logic-content">
        <p class="logic-title">逻辑设置</p>
        <p class="text-desc">
          你可以为选择字段（单项选择、多项选择、下拉框、评分）设定一些规则：在填写者选择某选项后，触发显示其他显示字段
        </p>
        
        <div class="show-logic-container">
          <div v-if="!logicList.length" class="not-logic-container">
            <el-button type="text" @click="handleAddLogic">
              <el-icon><Plus /></el-icon>
              <span class="label">添加逻辑</span>
            </el-button>
          </div>
          
          <div v-else class="logic-item-container">
            <div class="logic-header">
              <el-tag v-if="saveMessage" :type="saveStatus === 'saved' ? 'success' : 'info'">
                {{ saveMessage }}
              </el-tag>
              <el-button
                size="small"
                type="primary"
                :loading="isLoading"
                @click="handleAddLogic"
              >
                <el-icon><Plus /></el-icon>
                <span>添加逻辑</span>
              </el-button>
            </div>
            
            <el-divider />
            
            <div
              v-for="(logicItem, index) in logicList"
              :key="logicItem.id"
              class="logic-item"
            >
              <div class="logic-item-delete" @click="handleRemoveLogic(index)">
                <el-icon><Delete /></el-icon>
              </div>
              
              <!-- 条件列表 -->
              <div
                v-for="(condition, cIndex) in logicItem.conditionList"
                :key="cIndex"
                class="condition-row"
              >
                <div class="condition-label">
                  <span v-if="cIndex === 0">如果：</span>
                  <el-select
                    v-else
                    v-model="condition.relation"
                    :disabled="cIndex !== 1"
                    style="width: 100px"
                    @change="() => handleRelationChange(logicItem, condition.relation)"
                  >
                    <el-option label="并且" value="AND" />
                    <el-option label="或者" value="OR" />
                  </el-select>
                </div>
                
                <el-select
                  v-model="condition.formItemId"
                  placeholder="请选择题目"
                  style="width: 200px"
                  @change="() => handleConditionChange(condition)"
                >
                  <el-option
                    v-for="item in getConditionItemList(logicItem)"
                    :key="item.formItemId"
                    :label="item.label"
                    :value="item.formItemId"
                  />
                </el-select>
                
                <el-select
                  v-model="condition.expression"
                  placeholder="请选择条件"
                  style="width: 150px"
                >
                  <el-option
                    v-for="opt in getConditionOptions(condition.formItemId)"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
                
                <el-select
                  v-if="getFormItemType(condition.formItemId) !== 'RATE'"
                  v-model="condition.optionValue"
                  placeholder="请选择选项"
                  style="width: 200px"
                >
                  <el-option
                    v-for="opt in getFormItemOptions(condition.formItemId)"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
                
                <el-input-number
                  v-else
                  v-model="condition.optionValue"
                  :min="0"
                  style="width: 200px"
                />
                
                <div class="condition-actions">
                  <el-button type="text" @click="handleAddCondition(logicItem, condition)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button
                    v-if="cIndex !== 0"
                    type="text"
                    danger
                    @click="handleRemoveCondition(logicItem, cIndex)"
                  >
                    <el-icon><Minus /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <!-- 触发列表 -->
              <div
                v-for="(trigger, tIndex) in logicItem.triggerList"
                :key="tIndex"
                class="trigger-row"
              >
                <div class="trigger-label">
                  <span>则显示</span>
                </div>
                <el-select
                  v-model="trigger.formItemId"
                  placeholder="请选择问题"
                  style="width: 300px"
                >
                  <el-option
                    v-for="item in getTriggerItemList(logicItem)"
                    :key="item.formItemId"
                    :label="item.label"
                    :value="item.formItemId"
                    :disabled="item.disabled"
                  />
                </el-select>
                <span class="trigger-desc">否则不显示</span>
                <div class="trigger-actions">
                  <el-button type="text" @click="handleAddTrigger(logicItem)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button
                    v-if="tIndex !== 0"
                    type="text"
                    danger
                    @click="handleRemoveTrigger(logicItem, tIndex)"
                  >
                    <el-icon><Minus /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <el-divider />
            </div>
          </div>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Minus } from '@element-plus/icons-vue'
import { formApi, templateApi } from '@/api'
import { debounce } from 'lodash-es'

const route = useRoute()

const surveyId = ref(null)
const formKey = ref(null)
const isLoading = ref(false)
const saveStatus = ref('info') // 'info' | 'saved'
const saveMessage = ref('')
const logicList = ref([])
const allFormItemList = ref([])

// 默认逻辑结构
const defaultLogicItem = {
  id: null,
  triggerList: [
    {
      formItemId: null,
      type: 'show'
    }
  ],
  conditionList: [
    {
      formItemId: null,
      expression: null,
      optionValue: null,
      relation: 'AND'
    }
  ]
}

// 条件选项
const conditionOptions = [
  { value: 'eq', label: '选中' },
  { value: 'ne', label: '未选中' },
  { value: 'gt', label: '大于', types: ['RATE'] },
  { value: 'ge', label: '大于等于', types: ['RATE'] },
  { value: 'lt', label: '小于', types: ['RATE'] },
  { value: 'le', label: '小于等于', types: ['RATE'] }
]

// 加载表单配置和表单项
const loadFormData = async () => {
  const id = route.query.id
  const formKeyParam = route.query.formKey
  const isTemplate = route.query.isTemplate === 'true'
  
  // 如果是编辑模板（通过formKey）
  if (isTemplate && formKeyParam) {
    formKey.value = formKeyParam
    surveyId.value = null
    
    try {
      // 直接通过formKey加载表单项
      const itemsRes = await formApi.getFormItems(formKeyParam)
      if (itemsRes.code === 200 && itemsRes.data) {
        allFormItemList.value = itemsRes.data
          .filter(item => item.type !== 'PAGINATION')
          .map(item => {
            const scheme = typeof item.scheme === 'string' 
              ? JSON.parse(item.scheme) 
              : item.scheme
            return {
              formItemId: item.formItemId,
              type: item.type,
              label: item.label,
              scheme: scheme || {}
            }
          })
      }
      
      // 加载逻辑
      await loadLogic()
    } catch (error) {
      ElMessage.error('加载数据失败')
    }
    return
  }
  
  // 如果是编辑问卷（通过surveyId）
  if (!id) return

  surveyId.value = Number(id)

  try {
    // 加载表单配置获取 formKey
    const configRes = await formApi.getFormConfig(surveyId.value)
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      
      // 加载表单项
      if (formKey.value) {
        const itemsRes = await formApi.getFormItems(formKey.value)
        if (itemsRes.code === 200 && itemsRes.data) {
          allFormItemList.value = itemsRes.data
            .filter(item => item.type !== 'PAGINATION')
            .map(item => {
              const scheme = typeof item.scheme === 'string' 
                ? JSON.parse(item.scheme) 
                : item.scheme
              return {
                formItemId: item.formItemId,
                type: item.type,
                label: item.label,
                scheme: scheme || {}
              }
            })
        }
      }
    }
    
    // 加载逻辑
    await loadLogic()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 加载逻辑
const loadLogic = async () => {
  try {
    // 如果是编辑模板，从模板的scheme中获取逻辑
    if (!surveyId.value && formKey.value) {
      const templateRes = await templateApi.getTemplateDetails(formKey.value)
      if (templateRes.code === 200 && templateRes.data && templateRes.data.formLogic && templateRes.data.formLogic.scheme) {
        logicList.value = templateRes.data.formLogic.scheme
        return
      }
    }
    
    // 如果是编辑问卷，通过surveyId获取逻辑
    if (surveyId.value) {
      const res = await formApi.getFormLogic(surveyId.value)
      if (res.code === 200 && res.data && res.data.scheme) {
        logicList.value = res.data.scheme
      }
    } else {
      logicList.value = []
    }
  } catch (error) {
    // 如果不存在，使用空数组
    logicList.value = []
  }
}

// 保存逻辑（防抖）
const saveLogic = debounce(async (list) => {
  if (!surveyId.value) return
  
  isLoading.value = true
  saveStatus.value = 'info'
  saveMessage.value = '正在保存...'
  
  try {
    const res = await formApi.saveFormLogic(surveyId.value, list)
    if (res.code === 200) {
      saveStatus.value = 'saved'
      saveMessage.value = '已保存'
      setTimeout(() => {
        saveStatus.value = 'info'
        saveMessage.value = ''
      }, 2000)
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    isLoading.value = false
  }
}, 500)

// 监听逻辑列表变化
watch(
  () => logicList.value,
  (val) => {
    if (val && surveyId.value) {
      saveLogic(val)
    }
  },
  { deep: true }
)

// 添加逻辑
const handleAddLogic = () => {
  const newLogic = JSON.parse(JSON.stringify(defaultLogicItem))
  newLogic.id = Date.now()
  logicList.value.push(newLogic)
}

// 删除逻辑
const handleRemoveLogic = (index) => {
  logicList.value.splice(index, 1)
}

// 添加条件
const handleAddCondition = (logicItem, condition) => {
  logicItem.conditionList.push({
    formItemId: null,
    expression: null,
    optionValue: null,
    relation: condition.relation
  })
}

// 删除条件
const handleRemoveCondition = (logicItem, index) => {
  logicItem.conditionList.splice(index, 1)
}

// 添加触发
const handleAddTrigger = (logicItem) => {
  logicItem.triggerList.push({
    formItemId: null,
    type: 'show'
  })
}

// 删除触发
const handleRemoveTrigger = (logicItem, index) => {
  logicItem.triggerList.splice(index, 1)
}

// 关系变更
const handleRelationChange = (logicItem, relation) => {
  logicItem.conditionList.forEach(item => {
    item.relation = relation
  })
}

// 条件变更
const handleConditionChange = (condition) => {
  condition.optionValue = null
}

// 获取条件可选择的问题列表
const getConditionItemList = (logicItem) => {
  return allFormItemList.value.filter(item => {
    return ['RADIO', 'CHECKBOX', 'SELECT', 'RATE'].includes(item.type)
  })
}

// 获取触发可选择的问题列表
const getTriggerItemList = (logicItem) => {
  const selectedFormItemIds = logicItem.conditionList.map(c => c.formItemId)
  return allFormItemList.value.map(item => ({
    ...item,
    disabled: selectedFormItemIds.includes(item.formItemId)
  }))
}

// 获取条件选项
const getConditionOptions = (formItemId) => {
  if (!formItemId) return []
  const type = getFormItemType(formItemId)
  const options = [...conditionOptions]
  
  if (type === 'RATE') {
    return options.filter(opt => opt.types?.includes('RATE') || !opt.types)
  } else {
    return options.filter(opt => !opt.types || opt.types.length === 0)
  }
}

// 获取表单项选项
const getFormItemOptions = (formItemId) => {
  const formItem = allFormItemList.value.find(item => item.formItemId === formItemId)
  if (formItem && formItem.scheme?.config?.options) {
    return formItem.scheme.config.options
  }
  return []
}

// 获取表单项类型
const getFormItemType = (formItemId) => {
  if (!formItemId) return ''
  const formItem = allFormItemList.value.find(item => item.formItemId === formItemId)
  return formItem?.type || ''
}

onMounted(() => {
  loadFormData()
})
</script>

<style lang="scss" scoped>
.logic-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  overflow: hidden;
}

.scrollbar-container {
  height: 100%;
}

.logic-content {
  max-width: 1000px;
  margin: 0 auto;
}

.logic-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 10px;
}

.text-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
  line-height: 1.6;
}

.show-logic-container {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12);
}

.not-logic-container {
  text-align: center;
  padding: 40px;
  
  .label {
    margin-left: 5px;
  }
}

.logic-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.logic-item {
  position: relative;
  margin-bottom: 20px;
  
  .logic-item-delete {
    position: absolute;
    right: 0;
    top: -10px;
    color: #f56c6c;
    cursor: pointer;
    font-size: 18px;
  }
}

.condition-row,
.trigger-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.condition-label,
.trigger-label {
  width: 80px;
  text-align: right;
}

.trigger-desc {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.condition-actions,
.trigger-actions {
  display: flex;
  gap: 5px;
}
</style>
