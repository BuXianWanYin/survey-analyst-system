<template>
  <div class="form-edit-container">
    <!-- 主体：三栏布局 -->
    <div class="main-container">
      <!-- 左侧：组件库 -->
      <div class="left-board">
        <div class="left-scrollbar">
          <div class="components-list">
            <div
              v-for="component in componentList"
              :key="component.type"
              class="components-item"
              :draggable="true"
              @dragstart="handleDragStart($event, component)"
            >
              <div class="components-body">
                <el-icon class="component-icon">
                  <component :is="component.icon" />
                </el-icon>
                <span class="component-label">{{ component.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：设计区域 -->
      <div class="center-board">
        <div class="center-scrollbar">
          <div class="center-board-row">
            <el-form
              ref="drawingForm"
              :model="formModel"
              label-position="top"
              class="drawing-board"
            >
              <!-- 表单标题和描述 -->
              <div class="form-name-section">
                <div class="form-name-row">
                  <h2
                    v-if="!editingFormName"
                    class="form-name-text"
                    @click="editingFormName = true"
                  >
                    {{ formName || '未命名表单' }}
                  </h2>
                  <el-input
                    v-else
                    v-model="formName"
                    class="form-name-input"
                    placeholder="请输入表单标题"
                    @blur="editingFormName = false"
                    @keyup.enter="editingFormName = false"
                  />
                </div>
                <div class="form-description-row">
                  <span
                    v-if="!editingFormDescription"
                    class="form-description-text"
                    :class="{ 'empty-description': !formDescription }"
                    @click="editingFormDescription = true"
                  >
                    {{ formDescription || '点击添加表单描述（可选）' }}
                  </span>
                  <el-input
                    v-else
                    v-model="formDescription"
                    class="form-description-input"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入表单描述（可选）"
                    @blur="editingFormDescription = false"
                  />
                </div>
              </div>

              <!-- 表单项列表 -->
              <div 
                class="draggable-container"
                @drop="handleDrop"
                @dragover.prevent="handleDragOver"
                @dragenter.prevent="handleDragEnter"
              >
                <!-- 插入提示线 -->
                <div
                  v-if="dragInsertIndex !== null"
                  class="drag-insert-line"
                  :style="{ top: `${dragInsertIndex * 80}px` }"
                />
                <VueDraggable
                  v-model="drawingList"
                  handle=".drag-handle"
                  :animation="200"
                  ghost-class="sortable-ghost"
                  chosen-class="chosen-item"
                  drag-class="drag-item"
                  @end="handleDragEnd"
                >
                  <div
                    v-for="(element, index) in drawingList"
                    :key="element.formItemId"
                    class="drawing-item"
                    :class="{
                      'active-from-item': activeId === element.formItemId,
                      'unfocus-bordered': activeId !== element.formItemId
                    }"
                    :data-index="index"
                    @click.stop="handleItemClick(element)"
                    @dragover.prevent="handleItemDragOver($event, index)"
                    @dragleave="handleItemDragLeave($event, index)"
                  >
                    <div class="component-name">
                      {{ getComponentLabel(element.type) }}
                    </div>
                    <div
                      class="drawing-item-drag"
                      @click.stop
                    >
                      <el-icon class="drag-handle">
                        <Rank />
                      </el-icon>
                    </div>
                    <div
                      class="drawing-item-copy"
                      @click.stop="handleCopyItem(element)"
                    >
                      <el-icon><CopyDocument /></el-icon>
                    </div>
                    <div
                      class="drawing-item-delete"
                      @click.stop="handleDeleteItem(element)"
                    >
                      <el-icon><Delete /></el-icon>
                    </div>
                    <el-form-item
                      :label="element.label"
                      :prop="element.vModel"
                      :required="element.required"
                    >
                      <!-- 单行文本 -->
                      <el-input
                        v-if="element.type === 'INPUT'"
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        :readonly="element.readonly"
                      />
                      <!-- 多行文本 -->
                      <el-input
                        v-else-if="element.type === 'TEXTAREA'"
                        v-model="formModel[element.vModel]"
                        type="textarea"
                        :rows="4"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        :readonly="element.readonly"
                      />
                      <!-- 数字 -->
                      <el-input-number
                        v-else-if="element.type === 'NUMBER'"
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                      />
                      <!-- 单选框 -->
                      <el-radio-group
                        v-else-if="element.type === 'RADIO'"
                        v-model="formModel[element.vModel]"
                        :disabled="element.disabled"
                      >
                        <el-radio
                          v-for="(option, idx) in element.config?.options || []"
                          :key="idx"
                          :label="option.value"
                        >
                          {{ option.label }}
                        </el-radio>
                      </el-radio-group>
                      <!-- 多选框 -->
                      <el-checkbox-group
                        v-else-if="element.type === 'CHECKBOX'"
                        v-model="formModel[element.vModel]"
                        :disabled="element.disabled"
                      >
                        <el-checkbox
                          v-for="(option, idx) in element.config?.options || []"
                          :key="idx"
                          :label="option.value"
                        >
                          {{ option.label }}
                        </el-checkbox>
                      </el-checkbox-group>
                      <!-- 下拉框 -->
                      <el-select
                        v-else-if="element.type === 'SELECT'"
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                      >
                        <el-option
                          v-for="(option, idx) in element.config?.options || []"
                          :key="idx"
                          :label="option.label"
                          :value="option.value"
                        />
                      </el-select>
                      <!-- 评分 -->
                      <el-rate
                        v-else-if="element.type === 'RATE'"
                        v-model="formModel[element.vModel]"
                        :max="5"
                        :disabled="element.disabled"
                      />
                      <!-- 日期选择 -->
                      <el-date-picker
                        v-else-if="element.type === 'DATE'"
                        v-model="formModel[element.vModel]"
                        type="date"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        style="width: 100%"
                      />
                      <!-- 文件上传 -->
                      <el-upload
                        v-else-if="element.type === 'UPLOAD'"
                        v-model:file-list="formModel[element.vModel]"
                        :disabled="element.disabled"
                        action="#"
                        :auto-upload="false"
                      >
                        <el-button type="primary">
                          选择文件
                        </el-button>
                      </el-upload>
                      <!-- 图片上传 -->
                      <el-upload
                        v-else-if="element.type === 'IMAGE_UPLOAD'"
                        v-model:file-list="formModel[element.vModel]"
                        :disabled="element.disabled"
                        action="#"
                        :auto-upload="false"
                        list-type="picture-card"
                        :limit="element.config?.limit || 9"
                      >
                        <el-icon><Plus /></el-icon>
                      </el-upload>
                      <!-- 滑块 -->
                      <el-slider
                        v-else-if="element.type === 'SLIDER'"
                        v-model="formModel[element.vModel]"
                        :min="element.config?.min || 0"
                        :max="element.config?.max || 100"
                        :step="element.config?.step || 1"
                        :disabled="element.disabled"
                      />
                      <!-- 级联选择 -->
                      <el-cascader
                        v-else-if="element.type === 'CASCADER'"
                        v-model="formModel[element.vModel]"
                        :options="element.config?.options || []"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        style="width: 100%"
                      />
                      <!-- 分割线 -->
                      <el-divider
                        v-else-if="element.type === 'DIVIDER'"
                        :content-position="element.config?.contentPosition || 'center'"
                      >
                        {{ element.config?.content || '' }}
                      </el-divider>
                      <!-- 图片轮播 -->
                      <div
                        v-else-if="element.type === 'IMAGE_CAROUSEL'"
                        class="image-carousel-wrapper"
                      >
                        <el-carousel
                          v-if="element.config?.options && element.config.options.filter(opt => opt.url).length > 0"
                          :key="`carousel-${element.formItemId}-${element.config?.options?.length || 0}-${JSON.stringify(element.config?.options?.map(opt => opt.url || ''))}`"
                          :height="`${element.config?.height || 300}px`"
                          :interval="element.config?.interval || 4000"
                        >
                          <el-carousel-item
                            v-for="(option, idx) in element.config.options.filter(opt => opt.url)"
                            :key="option.url || idx"
                          >
                            <el-image
                              :src="option.url"
                              :fit="element.config?.fit || 'cover'"
                              style="width: 100%; height: 100%"
                            />
                          </el-carousel-item>
                        </el-carousel>
                        <div
                          v-else
                          class="carousel-placeholder"
                        >
                          <el-icon><Picture /></el-icon>
                          <span>请添加图片</span>
                        </div>
                      </div>
                      <!-- 图片选择 -->
                      <div
                        v-else-if="element.type === 'IMAGE_SELECT'"
                        class="image-select-container"
                      >
                        <div
                          v-for="(option, idx) in element.config?.options || []"
                          :key="idx"
                          class="image-select-item"
                          :class="{ active: formModel[element.vModel] === option.value }"
                          @click="formModel[element.vModel] = option.value"
                        >
                          <el-image
                            v-if="option.image"
                            :src="option.image"
                            fit="cover"
                            class="image-select-img"
                          />
                          <span class="image-select-label">{{ option.label }}</span>
                        </div>
                      </div>
                      <!-- 图片展示 -->
                      <el-image
                        v-else-if="element.type === 'IMAGE'"
                        :src="element.config?.imageUrl || ''"
                        :fit="element.config?.fit || 'cover'"
                        style="width: 100%"
                        :preview-src-list="element.config?.previewList || []"
                      />
                      <!-- 排序题型 -->
                      <div
                        v-else-if="element.type === 'SORT'"
                        class="sort-container"
                      >
                        <VueDraggable
                          v-model="formModel[element.vModel]"
                          handle=".sort-handle"
                          :animation="200"
                        >
                          <div
                            v-for="(item, idx) in formModel[element.vModel] || []"
                            :key="idx"
                            class="sort-item"
                          >
                            <el-icon class="sort-handle">
                              <Rank />
                            </el-icon>
                            <span>{{ item.label || item }}</span>
                          </div>
                        </VueDraggable>
                      </div>
                      <!-- 默认：单行文本 -->
                      <el-input
                        v-else
                        v-model="formModel[element.vModel]"
                        :placeholder="element.placeholder"
                        :disabled="element.disabled"
                        :readonly="element.readonly"
                      />
                    </el-form-item>
                  </div>
                </VueDraggable>
                
                <!-- 空状态 -->
                <div
                  v-if="drawingList.length === 0"
                  class="empty-info"
                >
                  从左侧拖拽组件到此处
                </div>
              </div>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 右侧：属性配置面板 -->
      <div class="right-board">
        <el-card class="property-panel">
          <template #header>
            <div class="property-header">
              组件属性
            </div>
          </template>
          <div
            v-if="activeData"
            class="property-content"
          >
            <el-scrollbar class="property-scrollbar">
              <el-form
                :model="activeData"
                label-width="100px"
                label-position="top"
              >
                <!-- 基础属性（所有组件通用） -->
                <el-form-item label="标题">
                  <el-input
                    v-model="activeData.label"
                    @input="handlePropertyChange"
                  />
                </el-form-item>
                <el-form-item label="字段名">
                  <el-input
                    v-model="activeData.vModel"
                    disabled
                  />
                </el-form-item>
                
                <!-- 输入类组件特有属性 -->
                <template v-if="['INPUT', 'TEXTAREA', 'NUMBER', 'DATE'].includes(activeData.type)">
                  <el-form-item label="提示文字">
                    <el-input
                      v-model="activeData.placeholder"
                      @input="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否必填">
                    <el-switch
                      v-model="activeData.required"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否禁用">
                    <el-switch
                      v-model="activeData.disabled"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否只读">
                    <el-switch
                      v-model="activeData.readonly"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                </template>
                
                <!-- 选择题选项配置 -->
                <template v-if="isChoiceType(activeData.type)">
                  <el-divider />
                  <el-form-item label="选项列表">
                    <div
                      v-for="(option, idx) in activeData.config.options"
                      :key="idx"
                      class="option-item"
                    >
                      <el-input
                        v-model="option.label"
                        placeholder="选项文本"
                        @input="handlePropertyChange"
                      />
                      <el-button
                        type="danger"
                        text
                        @click="handleRemoveOption(idx)"
                      >
                        删除
                      </el-button>
                    </div>
                    <el-button
                      type="primary"
                      text
                      @click="handleAddOption"
                    >
                      添加选项
                    </el-button>
                  </el-form-item>
                </template>
                
                <!-- 图片选择组件配置 -->
                <template v-if="activeData.type === 'IMAGE_SELECT'">
                  <el-divider />
                  <el-form-item label="选项列表">
                    <div
                      v-for="(option, idx) in activeData.config.options"
                      :key="idx"
                      class="image-option-item"
                    >
                      <el-input
                        v-model="option.label"
                        placeholder="选项文本"
                        style="margin-bottom: 10px"
                        @input="handlePropertyChange"
                      />
                      <el-upload
                        :action="uploadUrl"
                        :headers="uploadHeaders"
                        :show-file-list="false"
                        :on-success="(res) => handleImageOptionUpload(res, idx)"
                        accept="image/*"
                        class="image-option-upload"
                      >
                        <el-image
                          v-if="option.image"
                          :src="option.image"
                          fit="cover"
                          class="option-preview-image"
                        />
                        <el-button
                          v-else
                          type="primary"
                          size="small"
                        >
                          上传图片
                        </el-button>
                      </el-upload>
                      <el-button
                        type="danger"
                        text
                        size="small"
                        style="margin-top: 10px"
                        @click="handleRemoveOption(idx)"
                      >
                        删除
                      </el-button>
                    </div>
                    <el-button
                      type="primary"
                      text
                      @click="handleAddOption"
                    >
                      添加选项
                    </el-button>
                  </el-form-item>
                </template>
                
                <!-- 滑块组件配置 -->
                <template v-if="activeData.type === 'SLIDER'">
                  <el-divider />
                  <el-form-item label="最小值">
                    <el-input-number
                      v-model="activeData.config.min"
                      :min="0"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="最大值">
                    <el-input-number
                      v-model="activeData.config.max"
                      :min="1"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="步长">
                    <el-input-number
                      v-model="activeData.config.step"
                      :min="0.1"
                      :step="0.1"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否禁用">
                    <el-switch
                      v-model="activeData.disabled"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                </template>
                
                <!-- 图片轮播组件配置 -->
                <template v-if="activeData.type === 'IMAGE_CAROUSEL'">
                  <el-divider />
                  <el-form-item label="轮播高度">
                    <el-input-number
                      v-model="activeData.config.height"
                      :min="100"
                      :step="50"
                      placeholder="高度"
                      @change="handlePropertyChange"
                    />
                    <span style="margin-left: 10px; color: #909399">px</span>
                  </el-form-item>
                  <el-form-item label="切换间隔（毫秒）">
                    <el-input-number
                      v-model="activeData.config.interval"
                      :min="1000"
                      :step="500"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="图片适应模式">
                    <el-select
                      v-model="activeData.config.fit"
                      placeholder="请选择适应模式"
                      @change="handlePropertyChange"
                    >
                      <el-option
                        label="填充"
                        value="fill"
                      />
                      <el-option
                        label="包含"
                        value="contain"
                      />
                      <el-option
                        label="覆盖"
                        value="cover"
                      />
                      <el-option
                        label="无"
                        value="none"
                      />
                      <el-option
                        label="缩放"
                        value="scale-down"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="选项">
                    <div class="carousel-options-container">
                      <VueDraggable
                        v-model="activeData.config.options"
                        handle=".carousel-drag-handle"
                        ghost-class="sortable-ghost"
                        drag-class="drag-item"
                        @end="handlePropertyChange"
                      >
                        <div
                          v-for="(option, idx) in (activeData.config?.options || [])"
                          :key="idx"
                          class="carousel-option-item"
                        >
                          <div
                            style="display: flex; gap: 8px; align-items: center; margin-bottom: 10px"
                          >
                            <el-icon
                              class="carousel-drag-handle"
                              style="cursor: move; color: #909399; font-size: 16px"
                            >
                              <Rank />
                            </el-icon>
                            <el-input
                              v-model="option.text"
                              placeholder="文字"
                              style="flex: 1"
                              @input="handlePropertyChange"
                            />
                            <el-icon
                              class="carousel-delete-icon"
                              style="cursor: pointer; color: #f56c6c; font-size: 18px"
                              @click="handleRemoveCarouselOption(idx)"
                            >
                              <Delete />
                            </el-icon>
                          </div>
                          <div
                            style="display: flex; gap: 8px; align-items: center"
                          >
                            <el-input
                              v-model="option.url"
                              placeholder="图片URL"
                              style="flex: 1"
                              @input="handlePropertyChange"
                            />
                            <el-upload
                              :action="uploadUrl"
                              :headers="uploadHeaders"
                              :show-file-list="false"
                              :on-success="(res) => handleCarouselOptionUpload(res, idx)"
                              accept="image/*"
                            >
                              <el-icon
                                style="cursor: pointer; color: #409eff; font-size: 18px"
                                title="上传图片"
                              >
                                <Upload />
                              </el-icon>
                            </el-upload>
                          </div>
                        </div>
                      </VueDraggable>
                      <el-button
                        type="primary"
                        text
                        class="carousel-add-btn"
                        @click="handleAddCarouselOption"
                      >
                        <el-icon><Plus /></el-icon>
                        <span>添加选项</span>
                      </el-button>
                    </div>
                  </el-form-item>
                </template>
                
                <!-- 图片展示组件配置 -->
                <template v-if="activeData.type === 'IMAGE'">
                  <el-divider />
                  <el-form-item label="图片地址">
                    <el-input
                      v-model="activeData.config.imageUrl"
                      placeholder="图片URL"
                      @input="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="上传图片">
                    <el-upload
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :on-success="handleImageUpload"
                      accept="image/*"
                    >
                      <el-button type="primary">
                        选择图片
                      </el-button>
                    </el-upload>
                  </el-form-item>
                  <el-form-item label="图片预览">
                    <el-image
                      v-if="activeData.config.imageUrl"
                      :src="activeData.config.imageUrl"
                      fit="cover"
                      class="image-preview"
                    />
                  </el-form-item>
                  <el-form-item label="适应方式">
                    <el-select
                      v-model="activeData.config.fit"
                      @change="handlePropertyChange"
                    >
                      <el-option
                        label="填充"
                        value="fill"
                      />
                      <el-option
                        label="适应"
                        value="contain"
                      />
                      <el-option
                        label="覆盖"
                        value="cover"
                      />
                      <el-option
                        label="无缩放"
                        value="none"
                      />
                      <el-option
                        label="缩放"
                        value="scale-down"
                      />
                    </el-select>
                  </el-form-item>
                </template>
                
                <!-- 分割线组件配置 -->
                <template v-if="activeData.type === 'DIVIDER'">
                  <el-divider />
                  <el-form-item label="分割线文字">
                    <el-input
                      v-model="activeData.config.content"
                      placeholder="留空则不显示文字"
                      @input="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="文字位置">
                    <el-radio-group
                      v-model="activeData.config.contentPosition"
                      @change="handlePropertyChange"
                    >
                      <el-radio label="left">
                        左
                      </el-radio>
                      <el-radio label="center">
                        中
                      </el-radio>
                      <el-radio label="right">
                        右
                      </el-radio>
                    </el-radio-group>
                  </el-form-item>
                </template>
                
                <!-- 图片上传组件配置 -->
                <template v-if="activeData.type === 'IMAGE_UPLOAD'">
                  <el-divider />
                  <el-form-item label="最大上传数量">
                    <el-input-number
                      v-model="activeData.config.limit"
                      :min="1"
                      :max="20"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否必填">
                    <el-switch
                      v-model="activeData.required"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否禁用">
                    <el-switch
                      v-model="activeData.disabled"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                </template>
                
                <!-- 文件上传组件配置 -->
                <template v-if="activeData.type === 'UPLOAD'">
                  <el-divider />
                  <el-form-item label="是否必填">
                    <el-switch
                      v-model="activeData.required"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否禁用">
                    <el-switch
                      v-model="activeData.disabled"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                </template>
                
                <!-- 评分组件配置 -->
                <template v-if="activeData.type === 'RATE'">
                  <el-divider />
                  <el-form-item label="最大分值">
                    <el-input-number
                      v-model="activeData.config.max"
                      :min="1"
                      :max="10"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否必填">
                    <el-switch
                      v-model="activeData.required"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                  <el-form-item label="是否禁用">
                    <el-switch
                      v-model="activeData.disabled"
                      @change="handlePropertyChange"
                    />
                  </el-form-item>
                </template>
                
                <!-- 排序题型配置 -->
                <template v-if="activeData.type === 'SORT'">
                  <el-divider />
                  <el-form-item label="选项列表">
                    <div
                      v-for="(option, idx) in activeData.config.options"
                      :key="idx"
                      class="option-item"
                    >
                      <el-input
                        v-model="option.label"
                        placeholder="选项文本"
                        @input="handlePropertyChange"
                      />
                      <el-button
                        type="danger"
                        text
                        @click="handleRemoveOption(idx)"
                      >
                        删除
                      </el-button>
                    </div>
                    <el-button
                      type="primary"
                      text
                      @click="handleAddOption"
                    >
                      添加选项
                    </el-button>
                  </el-form-item>
                </template>
              </el-form>
            </el-scrollbar>
          </div>
          <div
            v-else
            class="empty-property"
          >
            <el-empty
              description="请选择一个组件进行配置"
              :image-size="80"
            />
          </div>
        </el-card>
      </div>
    </div>

    <!-- 预览对话框 -->
    <SurveyPreview
      v-model="previewVisible"
      :form-name="formName"
      :form-items="drawingList"
      :form-key="formKey"
      :show-qrcode="true"
    />

    <!-- 保存为模板对话框 -->
    <el-dialog
      v-model="saveTemplateDialogVisible"
      title="保存为模板"
      width="500px"
      append-to-body
    >
      <el-form
        ref="templateFormRef"
        :model="templateForm"
        :rules="templateFormRules"
        label-width="80px"
      >
        <el-form-item
          label="封面图"
          prop="coverImg"
        >
          <el-input
            v-model="templateForm.coverImg"
            placeholder="请输入封面图URL（可选）"
          />
        </el-form-item>
        <el-form-item
          label="模板名称"
          prop="name"
        >
          <el-input
            v-model="templateForm.name"
            placeholder="请输入模板名称"
          />
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述（可选）"
          />
        </el-form-item>
        <el-form-item
          label="模板类型"
          prop="categoryId"
        >
          <el-select
            v-model="templateForm.categoryId"
            placeholder="请选择模板类型"
            style="width: 100%"
          >
            <el-option
              v-for="type in templateTypeList"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="saveTemplateDialogVisible = false">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="handleSubmitTemplate"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, inject } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CopyDocument,
  Delete,
  Rank,
  Plus
} from '@element-plus/icons-vue'
import { VueDraggable } from 'vue-draggable-plus'
import {
  ElInput,
  ElInputNumber,
  ElSelect,
  ElOption,
  ElRadioGroup,
  ElRadio,
  ElCheckboxGroup,
  ElCheckbox,
  ElRate,
  ElDatePicker,
  ElUpload,
  ElSlider,
  ElCascader,
  ElDivider,
  ElCarousel,
  ElCarouselItem,
  ElImage
} from 'element-plus'
import {
  Document,
  EditPen,
  List,
  Calendar,
  Upload,
  Picture,
  Operation,
  Connection,
  Select as SelectIcon,
  Minus,
  PictureRounded,
  ArrowDown,
  CircleCheck,
  View,
  Star,
  Sort
} from '@element-plus/icons-vue'
import { formApi, templateApi } from '@/api'
import SurveyPreview from '@/components/SurveyPreview.vue'
import { getToken } from '@/utils/auth'

const route = useRoute()

// 表单基本信息
const formName = ref('未命名表单')
const editingFormName = ref(false)
const formDescription = ref('')
const editingFormDescription = ref(false)
const formKey = ref(null)
const dragInsertIndex = ref(null) // 拖拽插入位置索引
const isDraggingFromLibrary = ref(false) // 是否从组件库拖拽
const surveyId = ref(null)
const formModel = reactive({})

// 组件列表
const componentList = [
  { type: 'INPUT', label: '单行文本', icon: Document, tag: 'el-input' },
  { type: 'TEXTAREA', label: '多行文本', icon: EditPen, tag: 'el-textarea' },
  { type: 'NUMBER', label: '数字', icon: List, tag: 'el-input-number' },
  { type: 'DATE', label: '日期时间', icon: Calendar, tag: 'el-date-picker' },
  { type: 'UPLOAD', label: '文件上传', icon: Upload, tag: 'el-upload' },
  { type: 'IMAGE_UPLOAD', label: '图片上传', icon: Picture, tag: 'el-upload' },
  { type: 'SLIDER', label: '滑块组件', icon: Operation, tag: 'el-slider' },
  { type: 'CASCADER', label: '级联选择', icon: Connection, tag: 'el-cascader' },
  { type: 'CHECKBOX', label: '多选框组', icon: SelectIcon, tag: 'el-checkbox-group' },
  { type: 'DIVIDER', label: '分割线', icon: Minus, tag: 'el-divider' },
  { type: 'IMAGE_CAROUSEL', label: '图片轮播', icon: PictureRounded, tag: 'el-carousel' },
  { type: 'SELECT', label: '下拉选择', icon: ArrowDown, tag: 'el-select' },
  { type: 'RADIO', label: '单选框组', icon: CircleCheck, tag: 'el-radio-group' },
  { type: 'IMAGE_SELECT', label: '图片选择', icon: Picture, tag: 'el-image' },
  { type: 'IMAGE', label: '图片展示', icon: View, tag: 'el-image' },
  { type: 'RATE', label: '评分组件', icon: Star, tag: 'el-rate' },
  { type: 'SORT', label: '排序题型', icon: Sort, tag: 'el-sort' }
]

// 设计区域数据
const drawingList = ref([])
const activeId = ref(null)
const activeData = computed(() => {
  return drawingList.value.find(item => item.formItemId === activeId.value)
})

// 根据组件类型获取默认值
const getDefaultValue = (type, defaultValue, config) => {
  // 文件上传类型的组件需要数组类型
  if (type === 'UPLOAD' || type === 'IMAGE_UPLOAD') {
    return defaultValue && Array.isArray(defaultValue) ? defaultValue : []
  }
  // 滑块组件需要数字类型
  if (type === 'SLIDER') {
    const numValue = defaultValue !== null && defaultValue !== undefined && defaultValue !== ''
      ? Number(defaultValue)
      : (config?.min || 0)
    return isNaN(numValue) ? (config?.min || 0) : numValue
  }
  // 数字输入框需要数字类型
  if (type === 'NUMBER') {
    const numValue = defaultValue !== null && defaultValue !== undefined && defaultValue !== ''
      ? Number(defaultValue)
      : undefined
    return isNaN(numValue) ? undefined : numValue
  }
  return defaultValue !== null && defaultValue !== undefined ? defaultValue : ''
}

// 获取组件标签文本
const getComponentLabel = (type) => {
  const component = componentList.find(c => c.type === type)
  return component ? component.label : type
}

// 判断是否为选择题
const isChoiceType = (type) => {
  return ['RADIO', 'CHECKBOX', 'SELECT', 'CASCADER', 'IMAGE_SELECT', 'SORT'].includes(type)
}

// 拖拽开始（从组件库）
const handleDragStart = (event, component) => {
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.dropEffect = 'copy'
  event.dataTransfer.setData('componentType', component.type)
  event.dataTransfer.setData('text/plain', component.type) // 兼容性
  isDraggingFromLibrary.value = true
  
  // 创建一个透明的拖拽图像，隐藏默认的禁用图标
  const dragImage = document.createElement('div')
  dragImage.style.position = 'absolute'
  dragImage.style.top = '-1000px'
  dragImage.style.width = '100px'
  dragImage.style.height = '40px'
  dragImage.style.background = 'rgba(64, 158, 255, 0.1)'
  dragImage.style.border = '1px dashed #409EFF'
  dragImage.style.borderRadius = '4px'
  dragImage.style.display = 'flex'
  dragImage.style.alignItems = 'center'
  dragImage.style.justifyContent = 'center'
  dragImage.style.fontSize = '12px'
  dragImage.style.color = '#409EFF'
  dragImage.textContent = component.label
  document.body.appendChild(dragImage)
  event.dataTransfer.setDragImage(dragImage, 50, 20)
  
  // 延迟移除拖拽图像
  setTimeout(() => {
    document.body.removeChild(dragImage)
  }, 0)
}

// 拖拽结束（VueDraggable 内部排序时触发）
const handleDragEnd = () => {
  // 保存排序后的列表
  saveFormItems()
}

// 处理拖拽进入容器
const handleDragEnter = (event) => {
  if (isDraggingFromLibrary.value) {
    event.preventDefault()
    event.stopPropagation()
  }
}

// 处理拖拽在容器上移动
const handleDragOver = (event) => {
  if (isDraggingFromLibrary.value) {
    event.preventDefault()
    event.stopPropagation()
    
    // 计算插入位置
    const container = event.currentTarget
    const containerRect = container.getBoundingClientRect()
    const y = event.clientY - containerRect.top
    
    // 根据 Y 坐标计算应该插入到哪个位置
    const itemHeight = 80 // 每个表单项的大概高度
    let insertIndex = Math.floor(y / itemHeight)
    
    // 限制插入位置范围
    insertIndex = Math.max(0, Math.min(insertIndex, drawingList.value.length))
    dragInsertIndex.value = insertIndex
  }
}

// 处理在表单项上拖拽移动
const handleItemDragOver = (event, index) => {
  if (isDraggingFromLibrary.value) {
    event.preventDefault()
    event.stopPropagation()
    
    const itemRect = event.currentTarget.getBoundingClientRect()
    const y = event.clientY - itemRect.top
    const itemHeight = itemRect.height
    
    // 判断是在上半部分还是下半部分
    if (y < itemHeight / 2) {
      dragInsertIndex.value = index
    } else {
      dragInsertIndex.value = index + 1
    }
  }
}

// 处理离开表单项
const handleItemDragLeave = (event, index) => {
  if (isDraggingFromLibrary.value) {
    // 检查是否真的离开了容器区域
    const container = event.currentTarget.closest('.draggable-container')
    if (container && !container.contains(event.relatedTarget)) {
      dragInsertIndex.value = null
    }
  }
}

// 处理从组件库拖拽到设计区域
const handleDrop = (event) => {
  event.preventDefault()
  event.stopPropagation()
  
  const componentType = event.dataTransfer.getData('componentType') || 
                        event.dataTransfer.getData('text/plain')
  
  if (!componentType || !isDraggingFromLibrary.value) {
    dragInsertIndex.value = null
    isDraggingFromLibrary.value = false
    return
  }
  
  const newItem = createFormItem(componentType)
  
  // 确定插入位置
  let insertIndex = dragInsertIndex.value
  if (insertIndex === null) {
    // 如果没有明确的插入位置，添加到末尾
    insertIndex = drawingList.value.length
  }
  
  // 插入到指定位置
  drawingList.value.splice(insertIndex, 0, newItem)
  
  // 初始化表单模型
  formModel[newItem.vModel] = getDefaultValue(newItem.type, newItem.defaultValue, newItem.config)
  
  // 选中新添加的项
  activeId.value = newItem.formItemId
  
  // 重置拖拽状态
  dragInsertIndex.value = null
  isDraggingFromLibrary.value = false
  
  // 等待 DOM 更新后自动保存
  nextTick(() => {
    saveFormItems()
  })
}


// 创建表单项
const createFormItem = (type) => {
  const timestamp = Date.now()
  const formItemId = `${type.toLowerCase()}-${timestamp}`
  
  const baseItem = {
    formItemId,
    type,
    label: getComponentLabel(type),
    vModel: formItemId,
    placeholder: `请输入${getComponentLabel(type)}`,
    required: false,
    disabled: false,
    readonly: false,
    defaultValue: '',
    config: {}
  }
  
  // 选择题添加选项配置
  if (isChoiceType(type)) {
    baseItem.config = {
      options: [
        { label: '选项1', value: 'option1' },
        { label: '选项2', value: 'option2' }
      ]
    }
  }
  
  // 级联选择配置
  if (type === 'CASCADER') {
    baseItem.config = {
      options: [
        {
          value: 'zhinan',
          label: '指南',
          children: [
            { value: 'shejiyuanze', label: '设计原则' },
            { value: 'daohang', label: '导航' }
          ]
        }
      ]
    }
  }
  
  // 滑块配置
  if (type === 'SLIDER') {
    baseItem.config = {
      min: 0,
      max: 100,
      step: 1
    }
  }
  
  // 图片轮播配置
  if (type === 'IMAGE_CAROUSEL') {
    baseItem.config = {
      height: 300,
      interval: 4000,
      fit: 'cover',
      options: []
    }
  }
  
  // 图片选择配置
  if (type === 'IMAGE_SELECT') {
    baseItem.config = {
      options: [
        { label: '选项1', value: 'option1', image: '' },
        { label: '选项2', value: 'option2', image: '' }
      ]
    }
  }
  
  // 图片展示配置
  if (type === 'IMAGE') {
    baseItem.config = {
      imageUrl: '',
      fit: 'cover',
      previewList: []
    }
  }
  
  // 排序题型配置
  if (type === 'SORT') {
    baseItem.config = {
      options: [
        { label: '选项1', value: 'option1' },
        { label: '选项2', value: 'option2' }
      ]
    }
    baseItem.defaultValue = baseItem.config.options
  }
  
  // 分割线配置
  if (type === 'DIVIDER') {
    baseItem.config = {
      content: '',
      contentPosition: 'center'
    }
  }
  
  // 图片上传配置
  if (type === 'IMAGE_UPLOAD') {
    baseItem.config = {
      limit: 9
    }
  }
  
  return baseItem
}

// 点击组件
const handleItemClick = (element) => {
  activeId.value = element.formItemId
}

// 复制组件
const handleCopyItem = (element) => {
  const newItem = {
    ...JSON.parse(JSON.stringify(element)),
    formItemId: `${element.type.toLowerCase()}-${Date.now()}`,
    vModel: `${element.vModel}_copy_${Date.now()}`
  }
  
  const index = drawingList.value.findIndex(item => item.formItemId === element.formItemId)
  drawingList.value.splice(index + 1, 0, newItem)
  
  formModel[newItem.vModel] = getDefaultValue(newItem.type, newItem.defaultValue, newItem.config)
  activeId.value = newItem.formItemId
  
  saveFormItems()
}

// 删除组件
const handleDeleteItem = (element) => {
  const index = drawingList.value.findIndex(item => item.formItemId === element.formItemId)
  if (index > -1) {
    drawingList.value.splice(index, 1)
    delete formModel[element.vModel]
    
    if (activeId.value === element.formItemId) {
      activeId.value = null
    }
    
    saveFormItems()
  }
}

// 属性变更
const handlePropertyChange = () => {
  saveFormItems()
}

// 添加选项
const handleAddOption = () => {
  if (!activeData.value.config.options) {
    activeData.value.config.options = []
  }
  const optionCount = activeData.value.config.options.length + 1
  activeData.value.config.options.push({
    label: `选项${optionCount}`,
    value: `option${optionCount}`
  })
  handlePropertyChange()
}

// 删除选项
const handleRemoveOption = (index) => {
  if (activeData.value.config.options) {
    activeData.value.config.options.splice(index, 1)
    handlePropertyChange()
  }
}

// 图片上传相关
const uploadUrl = computed(() => {
  // 使用完整的API路径
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  return `${baseUrl}/file/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${getToken()}`
  }
})

// 获取后端服务器地址（用于构建图片URL）
const getBackendBaseUrl = () => {
  // 从 VITE_APP_BASE_API 提取后端地址
  const baseApi = import.meta.env.VITE_APP_BASE_API
  const proxyTarget = import.meta.env.VITE_SERVER_PROXY_TARGET
  
  // 如果 baseApi 是相对路径，使用 proxyTarget
  if (baseApi.startsWith('/')) {
    return proxyTarget
  }
  // 如果 baseApi 是完整URL，提取协议和主机
  try {
    const url = new URL(baseApi)
    return `${url.protocol}//${url.host}`
  } catch {
    return proxyTarget
  }
}

// 将相对路径转换为完整的后端URL
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果已经是完整URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是相对路径（以 /upload/ 开头），拼接后端地址
  if (imageUrl.startsWith('/upload/')) {
    return `${getBackendBaseUrl()}${imageUrl}`
  }
  // 其他情况，添加 /upload/ 前缀
  return `${getBackendBaseUrl()}/upload/${imageUrl}`
}

// 图片展示组件上传
const handleImageUpload = (response) => {
  // response 已经是 Result 格式，data 直接是文件URL字符串
  if (response && response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    // 转换为完整的后端URL
    const fullImageUrl = getImageUrl(imageUrl)
    activeData.value.config.imageUrl = fullImageUrl
    activeData.value.config.previewList = [fullImageUrl]
    handlePropertyChange()
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response?.message || '图片上传失败')
  }
}

// 图片轮播组件选项上传
const handleCarouselOptionUpload = (response, optionIndex) => {
  if (response && response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    // 转换为完整的后端URL
    const fullImageUrl = getImageUrl(imageUrl)
    
    // 找到当前激活的组件在 drawingList 中的索引
    const activeIndex = drawingList.value.findIndex(item => item.formItemId === activeId.value)
    if (activeIndex === -1) {
      ElMessage.error('未找到当前组件')
      return
    }
    
    // 确保 config 和 options 存在
    if (!drawingList.value[activeIndex].config) {
      drawingList.value[activeIndex].config = {}
    }
    if (!drawingList.value[activeIndex].config.options) {
      drawingList.value[activeIndex].config.options = []
    }
    
    // 更新选项的 URL
    if (drawingList.value[activeIndex].config.options[optionIndex]) {
      drawingList.value[activeIndex].config.options[optionIndex].url = fullImageUrl
      // 更新整个 drawingList 以触发响应式
      drawingList.value = [...drawingList.value]
      handlePropertyChange()
      ElMessage.success('图片上传成功')
    }
  } else {
    ElMessage.error(response?.message || '图片上传失败')
  }
}

// 添加轮播选项
const handleAddCarouselOption = () => {
  if (!activeData.value.config.options) {
    activeData.value.config.options = []
  }
  activeData.value.config.options.push({
    text: '',
    url: ''
  })
  handlePropertyChange()
}

// 删除轮播选项
const handleRemoveCarouselOption = (index) => {
  if (activeData.value.config.options) {
    activeData.value.config.options.splice(index, 1)
    handlePropertyChange()
  }
}

// 图片选择组件选项上传
const handleImageOptionUpload = (response, optionIndex) => {
  if (response && response.code === 200 && response.data) {
    const imageUrl = typeof response.data === 'string' ? response.data : (response.data.url || response.data)
    // 转换为完整的后端URL
    const fullImageUrl = getImageUrl(imageUrl)
    activeData.value.config.options[optionIndex].image = fullImageUrl
    handlePropertyChange()
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response?.message || '图片上传失败')
  }
}

// 保存表单配置
const saveFormConfig = async () => {
  if (!formKey.value) {
    formKey.value = generateFormKey()
  }
  
  try {
    const configData = {
      formKey: formKey.value,
      name: formName.value,
      description: formDescription.value || ''
    }
    
    // 如果是编辑模式，传递 surveyId
    if (surveyId.value) {
      configData.surveyId = Number(surveyId.value)
    }
    
    await formApi.saveFormConfig(configData)
  } catch (error) {
    // 保存表单配置失败
    ElMessage.error('保存表单配置失败')
  }
}

// 保存表单项
const saveFormItems = async () => {
  if (!formKey.value) {
    await saveFormConfig()
  }
  
  try {
    const items = drawingList.value.map((item, index) => ({
      formKey: formKey.value,
      formItemId: item.formItemId,
      type: item.type,
      label: item.label,
      required: item.required ? 1 : 0,
      placeholder: item.placeholder,
      sort: index,
      scheme: JSON.stringify(item)
    }))
    
    await formApi.saveFormItems(formKey.value, items)
  } catch (error) {
    // 保存表单项失败
    ElMessage.error('保存表单项失败')
  }
}

// 生成表单唯一标识
const generateFormKey = () => {
  return 'form_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

// 加载表单数据
const loadFormData = async () => {
  const id = route.query.id
  if (!id) {
    // 新建表单
    formKey.value = generateFormKey()
    surveyId.value = null
    return
  }
  
  surveyId.value = Number(id)
  
  try {
    // 加载表单配置
    const configRes = await formApi.getFormConfig(Number(id))
    if (configRes.code === 200 && configRes.data) {
      formKey.value = configRes.data.formKey
      formName.value = configRes.data.name || '未命名表单'
      formDescription.value = configRes.data.description || ''
    } else {
      formKey.value = generateFormKey()
    }
    
    // 加载表单项
    if (formKey.value) {
      const itemsRes = await formApi.getFormItems(formKey.value)
      if (itemsRes.code === 200 && itemsRes.data) {
        drawingList.value = itemsRes.data.map(item => {
          const scheme = typeof item.scheme === 'string' 
            ? JSON.parse(item.scheme) 
            : item.scheme
          
          // 修复图片URL（将相对路径转换为完整的后端URL）
          if (scheme.config) {
            // 图片展示组件
            if (scheme.type === 'IMAGE' && scheme.config.imageUrl) {
              scheme.config.imageUrl = getImageUrl(scheme.config.imageUrl)
              if (scheme.config.previewList) {
                scheme.config.previewList = scheme.config.previewList.map(url => getImageUrl(url))
              }
            }
            // 图片轮播组件
            if (scheme.type === 'IMAGE_CAROUSEL') {
              // 兼容旧格式（images）转换为新格式（options）
              if (scheme.config.images && !scheme.config.options) {
                scheme.config.options = scheme.config.images.map((img) => ({
                  text: img.text || '',
                  url: getImageUrl(img.url)
                }))
                delete scheme.config.images
              } else if (scheme.config.options) {
                // 新格式，更新 URL
                scheme.config.options = scheme.config.options.map(opt => ({
                  text: opt.text || '',
                  url: opt.url ? getImageUrl(opt.url) : ''
                }))
              }
              // 确保有默认值
              if (!scheme.config.fit) {
                scheme.config.fit = 'cover'
              }
              if (!scheme.config.height) {
                scheme.config.height = 300
              }
            }
            // 图片选择组件
            if (scheme.type === 'IMAGE_SELECT' && scheme.config.options) {
              scheme.config.options = scheme.config.options.map(opt => ({
                ...opt,
                image: opt.image ? getImageUrl(opt.image) : opt.image
              }))
            }
          }
          
          return {
            ...scheme,
            formItemId: item.formItemId,
            type: item.type,
            label: item.label,
            required: item.required === 1,
            placeholder: item.placeholder
          }
        })
        
        // 初始化表单模型
        drawingList.value.forEach(item => {
          formModel[item.vModel] = getDefaultValue(item.type, item.defaultValue, item.config)
        })
      }
    }
  } catch (error) {
    ElMessage.error('加载表单数据失败')
  }
}

// 保存
const handleSave = async () => {
  await saveFormConfig()
  await saveFormItems()
  ElMessage.success('保存成功')
}

// 预览
const previewVisible = ref(false)

// 保存为模板相关
const saveTemplateDialogVisible = ref(false)
const templateFormRef = ref(null)
const templateTypeList = ref([])
const templateForm = reactive({
  coverImg: '',
  name: '',
  description: '',
  categoryId: null
})
const templateFormRules = {
  name: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '模板类型不能为空', trigger: 'change' }]
}

// 加载模板分类
const loadTemplateTypes = async () => {
  try {
    const res = await templateApi.getTemplateTypeList()
    if (res.code === 200) {
      templateTypeList.value = res.data || []
    }
  } catch (error) {
    // 加载模板分类失败，静默处理
  }
}

// 打开保存为模板对话框
const handleSaveAsTemplate = async () => {
  if (!formKey.value) {
    ElMessage.warning('请先保存表单')
    return
  }

  if (drawingList.value.length === 0) {
    ElMessage.warning('请先添加表单组件')
    return
  }

  // 加载模板分类
  await loadTemplateTypes()

  // 初始化表单数据
  templateForm.name = formName.value || '未命名模板'
  templateForm.description = ''
  templateForm.coverImg = ''
  templateForm.categoryId = null

  saveTemplateDialogVisible.value = true
}

// 提交保存为模板
const handleSubmitTemplate = async () => {
  if (!templateFormRef.value) return

  await templateFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const templateData = {
          formKey: formKey.value,
          name: templateForm.name,
          description: templateForm.description || '',
          categoryId: templateForm.categoryId,
          coverImg: templateForm.coverImg || ''
        }

        const res = await templateApi.createTemplate(templateData)
        if (res.code === 200) {
          ElMessage.success('保存为模板成功')
          saveTemplateDialogVisible.value = false
          // 可选：跳转到模板列表页面
          // router.push('/survey/template')
        } else {
          ElMessage.error('保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

const handlePreview = async () => {
  // 如果没有表单项，提示用户
  if (drawingList.value.length === 0) {
    ElMessage.warning('请先添加表单组件')
    return
  }
  
  // 如果没有保存，先自动保存
  if (!formKey.value) {
    await saveFormConfig()
  }
  
  // 确保表单项已保存
  await saveFormItems()
  
  // 打开预览窗口
  previewVisible.value = true
}

// 注册方法到父组件（通过 provide/inject）
const registerEditorMethods = inject('registerEditorMethods', null)
if (registerEditorMethods) {
  registerEditorMethods({
    handlePreview,
    handleSave,
    handleSaveAsTemplate
  })
}

// 也暴露方法给父组件调用（兼容性）
defineExpose({
  handlePreview,
  handleSave,
  handleSaveAsTemplate
})

onMounted(() => {
  loadFormData()
  
  // 在组件挂载后重新注册方法（确保方法已定义）
  if (registerEditorMethods) {
    registerEditorMethods({
      handlePreview,
      handleSave,
      handleSaveAsTemplate
    })
  }
})
</script>

<style lang="scss" scoped>
.form-edit-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.left-board {
  width: 260px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.left-scrollbar {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px;
  min-height: 0;
}

.components-list {
  .components-item {
    display: inline-block;
    width: 48%;
    margin: 1%;
  }
}

.components-body {
  padding: 12px 8px;
  background: rgba(24, 144, 255, 0.05);
  border: 1px dashed rgba(24, 144, 255, 0.1);
  border-radius: 4px;
  text-align: center;
  cursor: move;
  transition: all 0.3s;
  
  &:hover {
    border-color: #409EFF;
    background: rgba(24, 144, 255, 0.1);
    transform: translateY(-2px);
  }
}

.component-icon {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 5px;
}

.component-label {
  display: block;
  font-size: 12px;
  color: #606266;
}

.center-board {
  flex: 1;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.center-scrollbar {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px;
}

.center-board-row {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  padding: 20px;
  padding-bottom: 100px; /* 增加底部留白 */
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.drawing-board {
  min-height: 400px;
}

.draggable-container {
  min-height: 300px;
  position: relative;
  width: 100%;
}

.drag-insert-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background: #409EFF;
  z-index: 100;
  pointer-events: none;
  transition: top 0.1s ease;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: -4px;
    width: 8px;
    height: 8px;
    background: #409EFF;
    border-radius: 50%;
  }
}

.draggable-container .empty-info {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
  pointer-events: none;
}

.form-name-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.form-name-row {
  margin-bottom: 15px;
}

.form-name-text {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  cursor: pointer;
  padding: 10px;
  border: 1px dashed transparent;
  border-radius: 4px;
  display: inline-block;
  
  &:hover {
    border-color: #409EFF;
    background: rgba(24, 144, 255, 0.05);
  }
}

.form-name-input {
  width: 100%;
  font-size: 24px;
}

.form-description-row {
  margin-top: 10px;
}

.form-description-text {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  padding: 8px 10px;
  border: 1px dashed transparent;
  border-radius: 4px;
  display: inline-block;
  min-height: 20px;
  white-space: pre-wrap;
  
  &.empty-description {
    color: #c0c4cc;
    font-style: italic;
  }
  
  &:hover {
    border-color: #409EFF;
    background: rgba(24, 144, 255, 0.05);
  }
}

.form-description-input {
  width: 100%;
}

.drawing-item {
  position: relative;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
  
  &.unfocus-bordered {
    :deep(.el-form-item) {
      border: 1px dashed #ccc;
      border-radius: 4px;
      padding: 10px;
    }
  }
  
  &.active-from-item {
    :deep(.el-form-item) {
      background: rgba(24, 144, 255, 0.05);
      border: 2px solid #409EFF;
      border-radius: 6px;
      padding: 10px;
    }
  }
  
  &:hover {
    .drawing-item-drag,
    .drawing-item-copy,
    .drawing-item-delete {
      display: block;
    }
  }
}

.component-name {
  position: absolute;
  top: -8px;
  left: 10px;
  font-size: 12px;
  color: #bbb;
  background: #fff;
  padding: 0 6px;
  z-index: 1;
}

.drawing-item.active-from-item .component-name {
  color: #409EFF;
}

.drawing-item-drag,
.drawing-item-copy,
.drawing-item-delete {
  display: none;
  position: absolute;
  top: -10px;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  border: 1px solid;
  cursor: pointer;
  z-index: 1;
  background: #fff;
}

.drawing-item-drag {
  right: 88px;
  border-color: #909399;
  color: #909399;
  
  .drag-handle {
    cursor: move;
    font-size: 14px;
  }
  
  &:hover {
    border-color: #409EFF;
    background: #409EFF;
    color: #fff;
  }
}

.drawing-item-copy {
  right: 56px;
  border-color: #409EFF;
  color: #409EFF;
  
  &:hover {
    background: #409EFF;
    color: #fff;
  }
}

.drawing-item-delete {
  right: 24px;
  border-color: #f56c6c;
  color: #f56c6c;
  
  &:hover {
    background: #f56c6c;
    color: #fff;
  }
}


.sortable-ghost {
  opacity: 0.5;
  background: rgba(24, 144, 255, 0.1);
  border: 2px dashed #409EFF;
}

.chosen-item {
  cursor: grabbing;
}

.drag-item {
  cursor: grabbing;
  opacity: 0.8;
}

.empty-info {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  font-size: 18px;
}

.right-board {
  width: 300px;
  flex-shrink: 0;
  border-left: 1px solid #ebeef5;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.property-panel {
  border: none;
  border-radius: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  :deep(.el-card__body) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    padding: 0;
    min-height: 0;
  }
}

.property-header {
  font-weight: 500;
  font-size: 14px;
}

.property-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 10px;
  min-height: 0;
  height: 100%;
}

.property-scrollbar {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  height: 100%;
  max-height: 100%;
}

.property-scrollbar :deep(.el-scrollbar__wrap) {
  overflow-x: hidden !important;
  overflow-y: auto !important;
  max-height: 100%;
}

.property-scrollbar :deep(.el-scrollbar__view) {
  padding-bottom: 40px;
}

.property-scrollbar :deep(.el-scrollbar__bar) {
  right: 0;
}

.property-scrollbar :deep(.el-scrollbar__bar.is-vertical) {
  width: 6px;
}

.property-scrollbar :deep(.el-scrollbar__thumb) {
  background-color: rgba(144, 147, 153, 0.3);
}

.property-scrollbar :deep(.el-scrollbar__thumb:hover) {
  background-color: rgba(144, 147, 153, 0.5);
}

.carousel-options-container {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.empty-property {
  padding: 40px 20px;
  text-align: center;
}

.option-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
  
  .el-input {
    flex: 1;
  }
}

.image-select-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.image-select-item {
  position: relative;
  width: 120px;
  height: 120px;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  &:hover {
    border-color: #409eff;
  }
  
  &.active {
    border-color: #409eff;
    background: rgba(64, 158, 255, 0.1);
  }
}

.image-select-img {
  width: 100%;
  height: 80px;
  border-radius: 4px 4px 0 0;
}

.image-select-label {
  padding: 5px;
  font-size: 12px;
  text-align: center;
}

.sort-container {
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  min-height: 100px;
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: move;
  
  .sort-handle {
    cursor: move;
    color: #909399;
    
    &:hover {
      color: #409eff;
    }
  }
}

.image-option-item {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.option-preview-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.image-carousel-wrapper {
  width: 100%;
  min-height: 300px;
}

.carousel-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  background: #f5f7fa;
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  color: #909399;
  font-size: 14px;
  gap: 10px;
}

.carousel-placeholder .el-icon {
  font-size: 48px;
}

.carousel-options-container {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.carousel-option-item {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
  flex-shrink: 0;
}

.carousel-drag-handle {
  cursor: move;
  color: #909399;
  font-size: 16px;
}

.carousel-drag-handle:hover {
  color: #409eff;
}

.carousel-delete-icon {
  cursor: pointer;
  color: #f56c6c;
  font-size: 18px;
}

.carousel-delete-icon:hover {
  color: #f78989;
}

.carousel-add-btn {
  margin-top: 10px;
  width: 100%;
  justify-content: flex-start;
  flex-shrink: 0;
}

.image-preview {
  width: 100%;
  max-height: 200px;
  border-radius: 4px;
}
</style>

