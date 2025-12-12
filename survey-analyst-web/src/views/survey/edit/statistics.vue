<template>
  <div class="statistics-container">
    <el-card class="statistics-card">
      <template #header>
        <div class="card-header fixed-header" :class="{ 'mobile-header': isMobile }">
          <span class="header-title">统计分析</span>
          <div class="header-actions" :class="{ 'mobile-actions': isMobile }">
            <!-- 移动端：下拉菜单 -->
            <el-dropdown v-if="isMobile" trigger="click" @command="handleActionCommand">
              <el-button size="small" type="primary">
                更多操作 <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="colorScheme" :icon="Tools">配色方案</el-dropdown-item>
                  <el-dropdown-item command="refresh" :icon="Refresh">刷新数据</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <!-- 桌面端：按钮组 -->
            <template v-else>
              <el-button size="small" @click="showColorSchemeDialog = true" :icon="Tools">配色方案</el-button>
              <el-button size="small" @click="handleRefresh" :icon="Refresh" style="margin-left: 10px">刷新数据</el-button>
            </template>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange" class="fixed-tabs">
        <el-tab-pane label="统计视图" name="chart">
          <div v-loading="loading" class="statistics-content">
            <!-- 问卷整体统计 -->
            <el-card v-if="surveyStatistics" class="survey-overview-card" style="margin-bottom: 20px">
              <template #header>
                <span>问卷整体统计</span>
              </template>
              <el-row :gutter="20">
                <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-statistic title="总填写数" :value="surveyStatistics.totalResponses || 0" />
                </el-col>
                <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-statistic title="已完成数" :value="surveyStatistics.completedResponses || 0" />
                </el-col>
                <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="8">
                  <el-statistic title="完成率" :value="getCompletionRate()" suffix="%" :precision="2" />
                </el-col>
              </el-row>
            </el-card>
            
            <div v-if="formItems.length === 0" class="empty-tip">
              <el-empty description="暂无表单项，无法统计" />
            </div>
            
            <el-card
              v-for="(item, index) in formItems"
              :key="item.formItemId"
              class="question-stat-card"
              style="margin-bottom: 20px"
            >
              <template #header>
                <div class="question-header">
                  <div class="question-title">
                    <span class="question-number">第{{ index + 1 }}题：</span>
                    <span>{{ item.label }}</span>
              </div>
                  <!-- 图表类型切换（仅选择题显示） -->
                  <div v-if="isChoiceType(item.type)" class="chart-type-switcher" :class="{ 'mobile-switcher': isMobile }">
                    <el-button-group :size="isMobile ? 'default' : 'small'">
                      <el-button
                        v-for="chartType in chartTypes"
                        :key="chartType.value"
                        :type="getCurrentChartType(item.formItemId) === chartType.value ? 'primary' : 'default'"
                        @click="switchChartType(item.formItemId, chartType.value)"
                      >
                        {{ chartType.label }}
                      </el-button>
                    </el-button-group>
                  </div>
                </div>
              </template>
              
              <!-- 选择题统计 -->
              <div v-if="isChoiceType(item.type)" class="stat-chart">
                <!-- 调试：显示数据状态 -->
                <!-- 调试信息（临时启用以排查问题） -->
                <div v-if="!statisticsData[item.formItemId] || !statisticsData[item.formItemId].optionStats" style="padding: 10px; background: #fff3cd; margin-bottom: 10px; font-size: 12px; border: 1px solid #ffc107;">
                  <div><strong>调试信息 - 题目 {{ index + 1 }}:</strong></div>
                  <div>formItemId: {{ item.formItemId }}</div>
                  <div>hasStatisticsData: {{ !!statisticsData[item.formItemId] }}</div>
                  <div>hasOptionStats: {{ !!statisticsData[item.formItemId]?.optionStats }}</div>
                  <div>optionStatsLength: {{ statisticsData[item.formItemId]?.optionStats?.length || 0 }}</div>
                  <div>tableDataLength: {{ getTableData(item.formItemId).length }}</div>
                  <div>chartType: {{ getCurrentChartType(item.formItemId) }}</div>
                  <div>loading: {{ loading }}</div>
                </div>
                <!-- 统计表格（只在图表类型为 table 时显示） -->
                <div v-if="getCurrentChartType(item.formItemId) === 'table'" class="stat-table-wrapper">
                  <!-- 级联选择使用树形表格 -->
                  <el-table
                    v-if="item.type === 'CASCADER'"
                    :data="getCascaderTableData(item.formItemId)"
                    border
                    style="width: 100%; margin-bottom: 20px"
                    :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                    row-key="id"
                    default-expand-all
                  >
                    <el-table-column prop="optionLabel" label="选项" min-width="200">
                      <template #default="{ row }">
                        <span>{{ row.optionLabel }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="count" label="小计" align="center" width="100" />
                    <el-table-column label="比例" min-width="300">
                      <template #default="{ row }">
                        <div class="percentage-cell">
                          <div class="bar-wrapper">
                            <div class="bar-bg">
                              <div class="bar-fill" :style="{ width: (row.percentage || 0) + '%', backgroundColor: getProgressBarColor }"></div>
                            </div>
                          </div>
                          <span class="percentage-text">{{ (row.percentage || 0).toFixed(2) }}%</span>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                  <!-- 图片选择使用增强版表格 -->
                  <el-table
                    v-else
                    :data="getTableData(item.formItemId)"
                    border
                    style="width: 100%; margin-bottom: 20px"
                  >
                    <el-table-column label="选项" :width="item.type === 'IMAGE_SELECT' ? 250 : 200">
                      <template #default="{ row }">
                        <div v-if="item.type === 'IMAGE_SELECT' && row.image" class="image-option-cell">
                          <el-image
                            :src="row.image"
                            fit="cover"
                            class="option-image"
                            :preview-src-list="getImagePreviewList(item.formItemId)"
                            :initial-index="getImageIndex(item.formItemId, row.image)"
                          />
                          <span class="option-label">{{ row.optionLabel }}</span>
                        </div>
                        <span v-else>{{ row.optionLabel }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="count" label="小计" align="center" width="100" />
                    <el-table-column label="比例" min-width="300">
                      <template #default="{ row }">
                        <div class="percentage-cell">
                          <div class="bar-wrapper">
                            <div class="bar-bg">
                              <div class="bar-fill" :style="{ width: (row.percentage || 0) + '%', backgroundColor: getProgressBarColor }"></div>
                            </div>
                          </div>
                          <span class="percentage-text">{{ (row.percentage || 0).toFixed(2) }}%</span>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div class="table-footer">
                    <strong>本题有效填写人次：{{ getTotalCount(item.formItemId) }}</strong>
                  </div>
                </div>
                
                <!-- 图表（只在图表类型不为 table 时显示） -->
                <div
                  v-else-if="getCurrentChartType(item.formItemId) !== 'table' && getChartOption(item.formItemId) && activeTab === 'chart'"
                  class="chart-wrapper"
                  style="height: 300px; min-height: 300px"
                >
                  <v-chart
                    :option="getChartOption(item.formItemId)"
                    style="width: 100%; height: 100%"
                    autoresize
                  />
                </div>
                <div v-else-if="getCurrentChartType(item.formItemId) !== 'table'" class="no-data">暂无数据</div>
              </div>
              
              <!-- 文本题统计 -->
              <div v-else-if="isTextType(item.type)" class="stat-text">
                <el-row :gutter="20" class="text-stat-info">
                  <el-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12">
                  <el-statistic title="有效答案数" :value="getTextStat(item.formItemId, 'count') || 0" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12">
                  <el-statistic title="总答案数" :value="getTextStat(item.formItemId, 'total') || 0" />
                  </el-col>
                </el-row>
                <!-- 词云图（仅 INPUT 和 TEXTAREA 显示） -->
                <div v-if="(item.type === 'INPUT' || item.type === 'TEXTAREA') && getWordCloudOption(item.formItemId) && activeTab === 'chart'" class="wordcloud-wrapper" style="height: 300px; min-height: 300px">
                  <h4 style="margin-bottom: 15px">高频词分析</h4>
                  <v-chart
                    :option="getWordCloudOption(item.formItemId)"
                    style="width: 100%; height: calc(100% - 30px)"
                    autoresize
                  />
                </div>
              </div>
              
              <!-- 签名组件统计 -->
              <div v-else-if="item.type === 'SIGN_PAD'" class="stat-signature">
                <el-row :gutter="20" class="signature-stat-info">
                  <el-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12">
                    <el-statistic title="有效签名数" :value="getSignatureStat(item.formItemId, 'count') || 0" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="12" :lg="12" :xl="12">
                    <el-statistic title="总填写数" :value="getSignatureStat(item.formItemId, 'total') || 0" />
                  </el-col>
                </el-row>
                <!-- 签名预览列表 -->
                <div v-if="getSignaturePreviewList(item.formItemId).length > 0" class="signature-preview-list">
                  <h4 style="margin-bottom: 15px">签名预览（{{ getSignaturePreviewList(item.formItemId).length }} 个签名）</h4>
                  <div class="signature-grid">
                    <div
                      v-for="(signature, index) in getSignaturePreviewList(item.formItemId)"
                      :key="index"
                      class="signature-item"
                    >
                      <el-image
                        :src="signature"
                        fit="contain"
                        class="signature-image"
                        :preview-src-list="getSignaturePreviewList(item.formItemId)"
                        :initial-index="index"
                      />
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 文件上传统计 -->
              <div v-else-if="item.type === 'UPLOAD'" class="stat-upload">
                <el-row :gutter="20" class="upload-stat-info">
                  <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                    <el-statistic title="有效上传数" :value="getUploadStat(item.formItemId, 'validUploads') || 0" />
                  </el-col>
                  <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                    <el-statistic title="总答案数" :value="getUploadStat(item.formItemId, 'totalAnswers') || 0" />
                  </el-col>
                  <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                    <el-statistic title="上传率" :value="getUploadStat(item.formItemId, 'uploadRate') || 0" suffix="%" :precision="2" />
                  </el-col>
                </el-row>
                
                <div class="upload-detail-info">
                  <div class="detail-row">
                    <span>总文件数：{{ getUploadStat(item.formItemId, 'totalFiles') || 0 }}</span>
                    <span>平均文件数：{{ getUploadStat(item.formItemId, 'averageFiles') || 0 }}</span>
                  </div>
                  
                  <!-- 文件类型分布 -->
                  <div v-if="getFileTypeStats(item.formItemId).length > 0" class="file-type-stats">
                    <h4 style="margin: 20px 0 10px 0">文件类型分布</h4>
                    <el-table :data="getFileTypeStats(item.formItemId)" border style="width: 100%; max-width: 500px">
                      <el-table-column prop="type" label="文件类型" width="150" />
                      <el-table-column prop="count" label="数量" align="center" width="100" />
                      <el-table-column label="比例" min-width="200">
                        <template #default="{ row }">
                          <div class="percentage-cell">
                            <div class="bar-wrapper">
                              <div class="bar-bg">
                                <div class="bar-fill" :style="{ width: row.percentage + '%', backgroundColor: getProgressBarColor }"></div>
                              </div>
                            </div>
                            <span class="percentage-text">{{ row.percentage }}%</span>
                          </div>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                  
                  <!-- 文件大小统计 -->
                  <div class="file-size-stats">
                    <h4 style="margin: 20px 0 10px 0">文件大小</h4>
                    <div class="detail-row">
                      <span>总大小：{{ formatFileSize(getUploadStat(item.formItemId, 'totalSize') || 0) }}</span>
                      <span>平均大小：{{ formatFileSize(getUploadStat(item.formItemId, 'averageSize') || 0) }}</span>
                      <span>最大文件：{{ formatFileSize(getUploadStat(item.formItemId, 'maxSize') || 0) }}</span>
                      <span>最小文件：{{ formatFileSize(getUploadStat(item.formItemId, 'minSize') || 0) }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 图片上传统计 -->
              <div v-else-if="item.type === 'IMAGE_UPLOAD'" class="stat-image-upload">
                <el-row :gutter="20" class="upload-stat-info">
                  <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                    <el-statistic title="有效上传数" :value="getUploadStat(item.formItemId, 'validUploads') || 0" />
                  </el-col>
                  <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                    <el-statistic title="总答案数" :value="getUploadStat(item.formItemId, 'totalAnswers') || 0" />
                  </el-col>
                  <el-col :xs="8" :sm="8" :md="8" :lg="8" :xl="8">
                    <el-statistic title="上传率" :value="getUploadStat(item.formItemId, 'uploadRate') || 0" suffix="%" :precision="2" />
                  </el-col>
                </el-row>
                
                <div class="upload-detail-info">
                  <div class="detail-row">
                    <span>总图片数：{{ getUploadStat(item.formItemId, 'totalImages') || 0 }}</span>
                    <span>平均图片数：{{ getUploadStat(item.formItemId, 'averageImages') || 0 }}</span>
                  </div>
                  
                  <!-- 图片预览网格 -->
                  <div v-if="getImageUploadList(item.formItemId).length > 0" class="image-upload-preview-list">
                    <h4 style="margin: 20px 0 15px 0">图片预览（{{ getImageUploadList(item.formItemId).length }} 张）</h4>
                    <div class="image-upload-grid">
                      <div
                        v-for="(image, index) in getImageUploadList(item.formItemId)"
                        :key="index"
                        class="image-upload-item"
                      >
                        <el-image
                          :src="getImageUploadUrl(image)"
                          fit="cover"
                          class="upload-image"
                          :preview-src-list="getImageUploadUrlList(item.formItemId)"
                          :initial-index="index"
                        />
                      </div>
                    </div>
                  </div>
                  
                  <!-- 图片大小统计 -->
                  <div class="file-size-stats">
                    <h4 style="margin: 20px 0 10px 0">图片大小</h4>
                    <div class="detail-row">
                      <span>总大小：{{ formatFileSize(getUploadStat(item.formItemId, 'totalSize') || 0) }}</span>
                      <span>平均大小：{{ formatFileSize(getUploadStat(item.formItemId, 'averageSize') || 0) }}</span>
                      <span>最大图片：{{ formatFileSize(getUploadStat(item.formItemId, 'maxSize') || 0) }}</span>
                      <span>最小图片：{{ formatFileSize(getUploadStat(item.formItemId, 'minSize') || 0) }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 评分题统计 -->
              <div v-else-if="isRatingType(item.type)" class="stat-rating">
                <el-row :gutter="20" class="rating-stat-info">
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="平均分" :value="getRatingStat(item.formItemId, 'averageRating') || 0" :precision="2" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="最高分" :value="getRatingStat(item.formItemId, 'maxRating') || 0" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="最低分" :value="getRatingStat(item.formItemId, 'minRating') || 0" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="评分人数" :value="getRatingStat(item.formItemId, 'totalRatings') || 0" />
                  </el-col>
                </el-row>
                <!-- 评分分布图 -->
                <div v-if="getRatingChartOption(item.formItemId) && activeTab === 'chart'" class="chart-wrapper" style="height: 300px; min-height: 300px">
                  <v-chart
                    :option="getRatingChartOption(item.formItemId)"
                    style="width: 100%; height: 100%"
                    autoresize
                  />
                </div>
              </div>
              
              <!-- 数字题统计 -->
              <div v-else-if="item.type === 'NUMBER'" class="stat-number">
                <el-row :gutter="20" class="number-stat-info">
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="平均值" :value="getNumberStat(item.formItemId, 'average') || 0" :precision="2" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="最大值" :value="getNumberStat(item.formItemId, 'max') || 0" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="最小值" :value="getNumberStat(item.formItemId, 'min') || 0" />
                  </el-col>
                  <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
                  <el-statistic title="有效数据数" :value="getNumberStat(item.formItemId, 'count') || 0" />
                  </el-col>
                </el-row>
                </div>
            </el-card>
          </div>
        </el-tab-pane>
        
            <!-- 交叉分析 -->
        <el-tab-pane label="交叉分析" name="cross">
          <div v-loading="analysisLoading" class="analysis-content">
            <div class="cross-analysis-wrapper">
              <!-- 标题栏 -->
              <div class="analysis-header">
                <div class="analysis-title">
                  <span class="title-text">我的交叉分析</span>
                  <el-icon class="title-icon"><ArrowDown /></el-icon>
                </div>
              </div>
              
              <!-- 分析配置 -->
            <el-card class="cross-analysis-card">
                <el-row :gutter="20">
                  <!-- 自变量X（最多2题） -->
                  <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                    <div class="variable-section">
                      <div class="variable-title">
                        <span class="title-label">自变量 X</span>
                        <span class="title-hint">(一般为样本属性,例如性别,年龄等。限2题)</span>
                      </div>
                      <div class="variable-inputs">
                        <div 
                          v-for="(item, index) in crossAnalysisForm.independentVars" 
                          :key="index" 
                          class="variable-input-item"
                        >
                  <el-select
                            v-model="crossAnalysisForm.independentVars[index]"
                            placeholder="添加自变量"
                            class="variable-select"
                            @change="handleIndependentVarChange(index)"
                  >
                    <el-option
                              v-for="formItem in getAvailableIndependentVars(index)"
                              :key="formItem.formItemId"
                              :label="`${formItem.label} (${getTypeLabel(formItem.type)})`"
                              :value="formItem.formItemId"
                    />
                  </el-select>
                          <el-button
                            type="danger"
                            :icon="Delete"
                            circle
                            size="small"
                            class="variable-delete-btn"
                            @click="removeIndependentVar(index)"
                          />
                        </div>
                      </div>
                      <el-button
                        v-if="crossAnalysisForm.independentVars.length < 2"
                        class="add-condition-btn"
                        @click="addIndependentVar"
                      >
                        <el-icon><Plus /></el-icon>
                        <span>增加条件</span>
                      </el-button>
                    </div>
                  </el-col>
                  
                  <!-- 因变量Y（最多10题） -->
                  <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                    <div class="variable-section">
                      <div class="variable-title">
                        <span class="title-label">因变量 Y</span>
                        <span class="title-hint">(您要分析的目标题目,限10题)</span>
                      </div>
                      <div class="variable-inputs">
                        <div 
                          v-for="(item, index) in crossAnalysisForm.dependentVars" 
                          :key="index" 
                          class="variable-input-item"
                        >
                  <el-select
                            v-model="crossAnalysisForm.dependentVars[index]"
                            placeholder="添加因变量"
                            class="variable-select"
                            @change="handleDependentVarChange(index)"
                  >
                    <el-option
                              v-for="formItem in getAvailableDependentVars(index)"
                              :key="formItem.formItemId"
                              :label="`${formItem.label} (${getTypeLabel(formItem.type)})`"
                              :value="formItem.formItemId"
                    />
                  </el-select>
                          <el-button
                            type="danger"
                            :icon="Delete"
                            circle
                            size="small"
                            class="variable-delete-btn"
                            @click="removeDependentVar(index)"
                          />
                        </div>
                      </div>
                      <el-button
                        v-if="crossAnalysisForm.dependentVars.length < 10"
                        class="add-condition-btn"
                        @click="addDependentVar"
                      >
                        <el-icon><Plus /></el-icon>
                        <span>增加条件</span>
                  </el-button>
                    </div>
                  </el-col>
                </el-row>
                
                <!-- 操作按钮 -->
                <div class="analysis-actions">
                  <el-button type="primary" @click="handleCrossAnalyze" :loading="crossAnalyzing" size="default">
                    交叉分析
                  </el-button>
                  <el-button 
                    v-if="crossAnalysisResults && crossAnalysisResults.length > 0" 
                    @click="togglePercentageView"
                  >
                    {{ showPercentage ? '显示数量' : '显示百分比' }}
                  </el-button>
                </div>
              </el-card>

              <!-- 交叉分析结果 -->
              <div v-if="crossAnalysisResults && crossAnalysisResults.length > 0" class="cross-result-section">
                <!-- 对每个因变量显示分析结果 -->
                <el-card
                  v-for="(result, resultIndex) in crossAnalysisResults"
                  :key="resultIndex"
                  style="margin-bottom: 20px"
                >
                  <template #header>
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                      <div>
                        <span style="font-weight: 500">第{{ resultIndex + 1 }}题: {{ result.dependentVarTitle }}</span>
                        <span style="font-size: 12px; color: #909399; margin-left: 10px">
                          {{ getIndependentVarsTitle(result.independentVars) }} × {{ result.dependentVarTitle }}
                        </span>
                      </div>
                      <!-- 图表类型切换 -->
                      <div class="cross-chart-type-switcher">
                        <el-button-group size="small">
                          <el-button
                            v-for="chartType in crossChartTypes"
                            :key="chartType.value"
                            :type="getCurrentCrossChartType(resultIndex) === chartType.value ? 'primary' : 'default'"
                            @click="switchCrossChartType(chartType.value, resultIndex)"
                          >
                            {{ chartType.label }}
                          </el-button>
                        </el-button-group>
                      </div>
                    </div>
                  </template>
                  
                  <!-- 交叉表（只在图表类型为 table 时显示） -->
                  <div v-if="getCurrentCrossChartType(resultIndex) === 'table'" class="cross-table-wrapper">
                    <el-table :data="getCrossTableData(result)" border style="width: 100%">
                      <el-table-column prop="rowLabel" :label="getRowLabel(result)" width="150" fixed="left" />
                    <el-table-column
                        v-for="col in getCrossTableColumns(result)"
                      :key="col"
                      :prop="col"
                      :label="col"
                      min-width="200"
                        align="center"
                      >
                        <template #default="{ row }">
                          <div v-if="showPercentage">
                            <div style="font-weight: 500; margin-bottom: 4px;">{{ row[col]?.count || 0 }}</div>
                            <!-- 行百分比（只显示数字，无进度条） -->
                            <div style="color: #909399; font-size: 12px; margin-bottom: 4px;">
                              {{ row[col]?.rowPercentage ? row[col].rowPercentage.toFixed(1) : '0' }}%
                            </div>
                            <!-- 列百分比（进度条） -->
                            <div class="percentage-cell">
                              <div class="bar-wrapper">
                                <div class="bar-bg">
                                  <div class="bar-fill" :style="{ width: (row[col]?.colPercentage || 0) + '%', backgroundColor: getProgressBarColor }"></div>
                                </div>
                              </div>
                              <span class="percentage-text" :style="{ color: getProgressBarColor }">({{ row[col]?.colPercentage ? row[col].colPercentage.toFixed(1) : '0' }}%)</span>
                            </div>
                          </div>
                          <span v-else>{{ row[col]?.count || 0 }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column 
                        v-if="showPercentage"
                        prop="rowTotal" 
                        label="行合计" 
                        width="100" 
                        align="center"
                        fixed="right"
                      >
                        <template #default="{ row }">
                          <div style="font-weight: 500">{{ row.rowTotal || 0 }}</div>
                          <div style="color: #909399; font-size: 12px">100%</div>
                        </template>
                      </el-table-column>
                  </el-table>
                    <!-- 列合计行 -->
                    <div v-if="showPercentage && getCrossTableSummary(result)" class="cross-table-summary">
                      <div class="summary-label">列合计</div>
                      <div 
                        v-for="col in getCrossTableColumns(result)" 
                        :key="col"
                        class="summary-cell"
                      >
                        <div style="font-weight: 500">{{ getCrossTableSummary(result)[col]?.total || 0 }}</div>
                        <div style="color: #909399; font-size: 12px">100%</div>
                      </div>
                      <div class="summary-cell summary-total">
                        <div style="font-weight: 500">{{ getCrossTableTotal(result) }}</div>
                        <div style="color: #909399; font-size: 12px">100%</div>
                      </div>
                    </div>
                </div>

                  <!-- 图表（只在图表类型不为 table 时显示） -->
                  <div v-else class="cross-chart-wrapper" style="height: 400px; min-height: 400px">
                  <v-chart
                      v-if="getCrossChartOptionForResult(result, resultIndex) && activeTab === 'cross'"
                      :option="getCrossChartOptionForResult(result, resultIndex)"
                      class="cross-chart"
                      style="width: 100%; height: 100%"
                    autoresize
                  />
                    <el-empty v-else description="暂无数据" />
                </div>
                </el-card>
              </div>
              <el-empty v-else-if="!crossAnalyzing" description="请选择自变量和因变量并开始分析" />
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 对比分析 -->
        <el-tab-pane label="对比分析" name="compare">
          <div v-loading="analysisLoading" class="analysis-content">
            <div class="compare-analysis-wrapper">
              <!-- 标题栏 -->
              <div class="analysis-header">
                <div class="analysis-title">
                  <span class="title-text">我的对比分析</span>
                  <el-icon class="title-icon"><ArrowDown /></el-icon>
                </div>
              </div>
              
              <!-- 分析配置 -->
              <el-card class="compare-analysis-card">
                <div class="compare-config">
                  <div class="compare-hint">
                    <span>请选择您要对比的变量 (例如:性别、年龄段、部门等)</span>
                  </div>
                  <el-row :gutter="20">
                    <el-col :xs="24" :sm="24" :md="16" :lg="12" :xl="12">
                      <el-form :model="compareForm" label-width="0">
                        <el-form-item>
                          <el-select
                            v-model="compareForm.compareVariable"
                            placeholder="请选择对比变量（如性别、年龄段等）"
                            class="compare-select"
                            @change="handleCompareVariableChange"
                          >
                            <el-option
                              v-for="item in choiceFormItems"
                              :key="item.formItemId"
                              :label="item.label"
                              :value="item.formItemId"
                            />
                          </el-select>
                        </el-form-item>
                      </el-form>
                    </el-col>
                    <el-col :xs="24" :sm="24" :md="8" :lg="12" :xl="12">
                      <div class="compare-actions">
                        <el-button type="primary" @click="handleCompareAnalyze" :loading="compareAnalyzing" size="default">
                          对比分析
                        </el-button>
                      </div>
                    </el-col>
                  </el-row>
              </div>
            </el-card>

              <!-- 对比分析结果 -->
              <div v-if="compareAnalysisResult && compareAnalysisResult.length > 0" class="compare-result-section">
                <el-card
                  v-for="(compareItem, index) in compareAnalysisResult"
                  :key="index"
                  style="margin-bottom: 20px"
                >
                  <template #header>
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                      <span style="font-weight: 500">{{ compareItem.questionTitle }}</span>
                      <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                        <!-- 显示模式切换（仅在表格模式下显示） -->
                        <div v-if="getCurrentCompareChartType(index) === 'table'" class="compare-display-mode-switcher">
                          <el-button-group size="small">
                            <el-button
                              :type="getCurrentCompareDisplayMode(index) === 'count' ? 'primary' : 'default'"
                              @click="switchCompareDisplayMode('count', index)"
                            >
                              显示数量
                            </el-button>
                            <el-button
                              :type="getCurrentCompareDisplayMode(index) === 'percentage' ? 'primary' : 'default'"
                              @click="switchCompareDisplayMode('percentage', index)"
                            >
                              显示百分比
                            </el-button>
                          </el-button-group>
                        </div>
                        <!-- 进度条开关（仅在表格模式且百分比模式下显示） -->
                        <el-switch
                          v-if="getCurrentCompareChartType(index) === 'table' && getCurrentCompareDisplayMode(index) === 'percentage'"
                          :model-value="getCurrentCompareProgressBar(index)"
                          @update:model-value="(val) => { compareShowProgressBarMap[index] = val }"
                          active-text="进度条"
                          inactive-text=""
                          size="small"
                          style="--el-switch-on-color: #409eff;"
                        />
                        <!-- 图表类型切换 -->
                        <div class="compare-chart-type-switcher">
                          <el-button-group size="small">
                            <el-button
                              v-for="chartType in compareChartTypes"
                              :key="chartType.value"
                              :type="getCurrentCompareChartType(index) === chartType.value ? 'primary' : 'default'"
                              @click="switchCompareChartType(chartType.value, index)"
                            >
                              {{ chartType.label }}
                            </el-button>
                          </el-button-group>
                        </div>
                      </div>
                    </div>
                  </template>
                  
                  <!-- 表格（只在图表类型为 table 时显示） -->
                  <div v-if="getCurrentCompareChartType(index) === 'table'" class="compare-table-wrapper">
                    <!-- 说明文字 -->
                    <div v-if="getCurrentCompareDisplayMode(index) === 'percentage'" class="compare-table-hint" style="margin-bottom: 10px; padding: 8px 12px; background: #f5f7fa; border-radius: 4px; font-size: 12px; color: #606266;">
                      <span>说明：百分比 = 占该选项的比例（行百分比）</span>
                    </div>
                    <el-table :data="compareItem.compareData" border style="width: 100%">
                      <el-table-column prop="optionLabel" label="X\Y" width="200" fixed="left" />
                      <el-table-column
                        v-for="group in compareItem.groups"
                        :key="group"
                        :prop="group"
                        :label="group"
                        min-width="200"
                        align="center"
                      >
                        <template #default="{ row }">
                          <!-- 数量模式 -->
                          <div v-if="getCurrentCompareDisplayMode(index) === 'count'" class="compare-cell count-mode">
                            <div class="count-value">{{ row[group]?.count || 0 }}</div>
                          </div>
                          <!-- 百分比模式 -->
                          <div v-else class="compare-cell percentage-mode">
                            <div class="percentage-value">{{ (row[group]?.percentage ?? 0).toFixed(2) }}%</div>
                            <div v-if="getCurrentCompareProgressBar(index)" class="percentage-bar-wrapper">
                              <div class="percentage-bar-bg">
                                <div class="percentage-bar-fill" :style="{ width: Math.min((row[group]?.percentage ?? 0), 100) + '%', background: getProgressBarGradient }"></div>
                              </div>
                            </div>
                            <div class="count-hint">数量: {{ row[group]?.count ?? 0 }}</div>
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column label="小计" align="center" width="100" fixed="right">
                        <template #default="{ row }">
                          <div class="compare-cell count-mode">
                            <div class="count-value">{{ getCompareRowTotal(row, compareItem.groups) }}</div>
                          </div>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                  
                  <!-- 图表（只在图表类型不为 table 时显示） -->
                  <div v-else class="compare-chart-wrapper" style="height: 400px; min-height: 400px">
                    <v-chart
                      v-if="getCompareChartOption(compareItem, index) && activeTab === 'compare'"
                      :option="getCompareChartOption(compareItem, index)"
                      class="compare-chart"
                      style="width: 100%; height: 100%"
                      autoresize
                    />
                    <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </div>
              <el-empty v-else-if="!compareAnalyzing" description="请选择对比变量并开始分析" />
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 数据分析的其他图表（趋势、设备、时段等） -->
        <el-tab-pane label="数据分析" name="analysis">
          <div v-loading="analysisLoading" class="analysis-content">
            <el-row :gutter="20" style="margin-top: 20px">
              <!-- 填写趋势 -->
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span style="white-space: nowrap;">填写趋势</span>
                      <el-select v-model="timeRange" size="small" @change="loadTrendData">
                        <el-option label="最近7天" value="7d" />
                        <el-option label="最近30天" value="30d" />
                        <el-option label="全部" value="all" />
                      </el-select>
                    </div>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                  <v-chart
                      v-if="trendChartOption && activeTab === 'analysis'"
                    :option="trendChartOption"
                      style="width: 100%; height: 100%"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
              
              <!-- 设备类型分布 -->
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <span>设备类型分布</span>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                  <v-chart
                      v-if="deviceChartOption && activeTab === 'analysis'"
                    :option="deviceChartOption"
                      style="width: 100%; height: 100%"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <!-- 填写时段分布、填写时长分布 -->
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <span>填写时段分布</span>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                  <v-chart
                      v-if="hourChartOption && activeTab === 'analysis'"
                      :option="hourChartOption"
                      style="width: 100%; height: 100%"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
              
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <span>填写时长分布</span>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                  <v-chart
                      v-if="durationChartOption && activeTab === 'analysis'"
                      :option="durationChartOption"
                      style="width: 100%; height: 100%"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <!-- 浏览器类型、题目完成率 -->
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <span>浏览器类型分布</span>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                    <v-chart
                      v-if="browserChartOption && activeTab === 'analysis'"
                      :option="browserChartOption"
                      style="width: 100%; height: 100%"
                      autoresize
                    />
                    <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
              
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <span>题目完成率</span>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                    <v-chart
                      v-if="completionRateChartOption && activeTab === 'analysis'"
                      :option="completionRateChartOption"
                      style="width: 100%; height: 100%"
                      autoresize
                    />
                    <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <!-- 填写质量分布、选项热度对比 -->
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <span>填写质量分布</span>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                    <v-chart
                      v-if="qualityChartOption && activeTab === 'analysis'"
                      :option="qualityChartOption"
                      style="width: 100%; height: 100%"
                      autoresize
                    />
                    <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
              
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-card>
                  <template #header>
                    <span>选项热度对比（TOP 10）</span>
                  </template>
                  <div style="height: 300px; min-height: 300px">
                    <v-chart
                      v-if="optionHeatChartOption && activeTab === 'analysis'"
                      :option="optionHeatChartOption"
                      style="width: 100%; height: 100%"
                      autoresize
                    />
                    <el-empty v-else description="暂无数据" />
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>  
    <!-- 配色方案对话框 -->
    <el-dialog
      v-model="showColorSchemeDialog"
      title="配色方案"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="配色方案">
          <el-select v-model="currentColorSchemeId" @change="handleColorSchemeChange" style="width: 100%">
            <el-option
              v-for="scheme in colorSchemes"
              :key="scheme.id"
              :label="scheme.name"
              :value="scheme.id"
            >
              <div class="color-scheme-option">
                <div class="color-preview" :style="{ background: scheme.preview }"></div>
                <span>{{ scheme.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showColorSchemeDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useWindowSize } from '@vueuse/core'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart, HeatmapChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  VisualMapComponent
} from 'echarts/components'
import 'echarts-wordcloud'
import VChart from 'vue-echarts'
import { Tools, ArrowDown, QuestionFilled, Plus, Delete, ArrowUp, Refresh } from '@element-plus/icons-vue'
import { formApi, analysisApi, questionApi, statisticsApi } from '@/api'
import dayjs from 'dayjs'
import { generateChartOption, generateCrossChartOption, generateCompareChartOption } from '@/utils/chartConfig'
import { colorSchemes, loadColorScheme, saveColorScheme } from '@/utils/colorSchemes'
import { getImageUrl } from '@/utils/image'

// 响应式检测（用于移动端特殊处理，如下拉菜单）
const { width } = useWindowSize()
const isMobile = computed(() => width.value < 768)

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  VisualMapComponent
])

const route = useRoute()

const surveyId = ref(null)
const formKey = ref(null)
const loading = ref(false)
const analysisLoading = ref(false)
const activeTab = ref('chart')
const formItems = ref([])
const statisticsData = ref({})
const surveyStatistics = ref(null) // 问卷整体统计
const timeRange = ref('all') // 默认选中"全部"
const trendChartOption = ref(null)
const deviceChartOption = ref(null)
const hourChartOption = ref(null)
const durationChartOption = ref(null) // 填写时长分布
const completionRateChartOption = ref(null) // 题目完成率
const browserChartOption = ref(null) // 浏览器类型分布
const qualityChartOption = ref(null) // 填写质量分布
const optionHeatChartOption = ref(null) // 选项热度对比

// 配色方案对话框
const showColorSchemeDialog = ref(false)
const currentColorScheme = ref(loadColorScheme())
const currentColorSchemeId = ref(currentColorScheme.value.id)

// 显示设置（全部默认显示）
const displaySettings = reactive({
  showTable: true,
  showBar: true,
  showAverage: true,
  hideEmpty: false,
  hideSkip: false
})

// 图表类型配置（表格放在最前面）
const chartTypes = [
  { value: 'table', label: '表格' },
  { value: 'pie', label: '饼状' },
  { value: 'ring', label: '圆环' },
  { value: 'bar', label: '柱状' },
  { value: 'line', label: '折线' },
  { value: 'horizontalBar', label: '条形' }
]

// 每个题目的图表类型（存储在localStorage，用于响应式更新）
const chartTypeMap = ref({})



// 交叉分析相关
const crossAnalysisForm = reactive({
  independentVars: [null], // 自变量数组（最多2个），默认显示一个下拉框
  dependentVars: [null] // 因变量数组（最多10个），默认显示一个下拉框
})
const crossAnalyzing = ref(false)
const crossAnalysisResults = ref([]) // 数组，存储多个因变量的分析结果
const showPercentage = ref(true) // 默认显示百分比和进度条
const crossChartTypeMap = ref({}) // 存储每个结果索引对应的图表类型
const crossChartTypes = [
  { value: 'table', label: '表格' },
  { value: 'horizontalBar', label: '条形' },
  { value: 'bar', label: '柱状' },
  { value: 'line', label: '折线' },
  { value: 'heatmap', label: '热力图' }
]

// 对比分析相关
const compareForm = reactive({
  compareVariable: null
})
const compareAnalyzing = ref(false)
const compareAnalysisResult = ref(null)
const compareChartTypeMap = ref({}) // 存储每个对比结果索引对应的图表类型
const compareDisplayModeMap = ref({}) // 存储每个对比结果索引对应的显示模式：'count' | 'percentage'
const compareShowProgressBarMap = ref({}) // 存储每个对比结果索引对应的进度条显示状态
const compareChartTypes = [
  { value: 'table', label: '表格' },
  { value: 'bar', label: '柱状' },
  { value: 'horizontalBar', label: '条形' },
  { value: 'line', label: '折线' }
]

// 加载表单配置和表单项
const loadFormConfig = async () => {
  const id = route.query.id
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
          // 过滤掉展示类组件（IMAGE、IMAGE_CAROUSEL、DESC_TEXT、DIVIDER）
          const displayTypes = ['IMAGE', 'IMAGE_CAROUSEL', 'DESC_TEXT', 'DIVIDER']
          formItems.value = itemsRes.data
            .filter(item => !displayTypes.includes(item.type))
            .map(item => {
            const scheme = typeof item.scheme === 'string' 
              ? JSON.parse(item.scheme) 
              : item.scheme
            return {
              formItemId: item.formItemId,
              label: item.label,
              type: item.type,
              scheme: scheme || {}
            }
          })
        }
      }
    }
    
    await loadStatistics()
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

// 加载统计数据
const loadStatistics = async () => {
  if (!formKey.value || !surveyId.value) return

  loading.value = true
  try {
    // 使用统一接口一次性获取所有统计数据
    const allStatRes = await statisticsApi.getAllStatistics(surveyId.value, {
      includeTrend: false,
      includeSource: false,
      includeDevice: false
    })
    
    if (allStatRes.code === 200 && allStatRes.data) {
      const allData = allStatRes.data
      
      // 1. 设置问卷整体统计
      if (allData.surveyStatistics) {
        surveyStatistics.value = allData.surveyStatistics
      }
      
      // 2. 设置各题目统计数据
      if (allData.questionStatistics) {
        // 将后端返回的统计数据设置到 statisticsData
        Object.keys(allData.questionStatistics).forEach(formItemId => {
          statisticsData.value[formItemId] = allData.questionStatistics[formItemId]
        })

      } else {
        console.warn('[loadStatistics] No questionStatistics in response:', allData)
      }
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error('加载统计数据错误:', error)
    // 降级方案：使用旧的逐个请求方式
    await loadStatisticsFallback()
  } finally {
    loading.value = false
  }
}

// 降级方案：如果统一接口失败，使用旧的逐个请求方式
const loadStatisticsFallback = async () => {
  try {
    // 1. 加载问卷整体统计
    const surveyStatRes = await statisticsApi.getSurveyStatistics(surveyId.value)
    if (surveyStatRes.code === 200) {
      surveyStatistics.value = surveyStatRes.data
    }
    
    // 2. 使用后端API获取每个题目的统计数据
    for (const item of formItems.value) {
      try {
        const statRes = await statisticsApi.getQuestionStatistics(item.formItemId, surveyId.value)
        if (statRes.code === 200 && statRes.data) {
          statisticsData.value[item.formItemId] = statRes.data
        }
      } catch (error) {
        console.error(`获取题目 ${item.formItemId} 统计失败:`, error)
        // 如果API失败，使用前端计算作为降级方案
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 1000
    })
    if (dataRes.code === 200 && dataRes.data) {
      const dataList = dataRes.data.records || []
        if (isChoiceType(item.type)) {
          statisticsData.value[item.formItemId] = calculateChoiceStat(item, dataList)
        } else if (isTextType(item.type)) {
          statisticsData.value[item.formItemId] = calculateTextStat(item, dataList)
        }
        }
      }
    }
  } catch (error) {
    console.error('降级方案也失败了:', error)
  }
}

// 计算选择题统计
const calculateChoiceStat = (item, dataList) => {
  const options = item.scheme?.config?.options || []
  const optionCount = {}
  
  options.forEach(opt => {
    optionCount[opt.value] = 0
  })
  
  dataList.forEach(data => {
    const value = data.originalData?.[item.formItemId]
    if (value !== undefined && value !== null) {
      if (Array.isArray(value)) {
        value.forEach(v => {
          if (optionCount[v] !== undefined) {
            optionCount[v]++
          }
        })
      } else {
        if (optionCount[value] !== undefined) {
          optionCount[value]++
        }
      }
    }
  })
  
  return {
    optionCount,
    total: dataList.length
  }
}

// 计算文本题统计
const calculateTextStat = (item, dataList) => {
  let count = 0
  dataList.forEach(data => {
    const value = data.originalData?.[item.formItemId]
    if (value && String(value).trim()) {
      count++
    }
  })
  
  return {
    count,
    total: dataList.length
  }
}

// 获取当前题目的图表类型
const getCurrentChartType = (formItemId) => {
  // 优先从响应式 map 中获取（实时更新）
  if (chartTypeMap.value[formItemId]) {
    return chartTypeMap.value[formItemId]
  }
  
  // 从localStorage加载图表类型偏好
  try {
    const saved = localStorage.getItem(`chart_type_${formItemId}`)
    if (saved && chartTypes.find(t => t.value === saved)) {
      // 同步到响应式 map
      chartTypeMap.value[formItemId] = saved
      return saved
    }
  } catch (error) {
    console.error('加载图表类型失败:', error)
  }
  
  // 默认返回表格
  const defaultType = 'table'
  chartTypeMap.value[formItemId] = defaultType
  return defaultType
}

// 切换图表类型
const switchChartType = (formItemId, type) => {
  try {
    // 更新响应式 map（触发视图更新）
    chartTypeMap.value[formItemId] = type
    // 保存到 localStorage
    localStorage.setItem(`chart_type_${formItemId}`, type)
  } catch (error) {
    console.error('保存图表类型失败:', error)
  }
}

// 获取图表配置（选择题）
const getChartOption = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.optionStats) return null
  
  const chartType = getCurrentChartType(formItemId)
  if (chartType === 'table') return null
  
  return generateChartOption(chartType, stat, currentColorScheme.value)
}

// 获取表格数据
const getTableData = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat) {
    return []
  }
  if (!stat.optionStats || !Array.isArray(stat.optionStats) || stat.optionStats.length === 0) {
    return []
  }
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) {
    return []
  }
  
  let options = stat.optionStats
  const total = stat.totalCount || 0
  
  // 过滤选项
  if (displaySettings.hideEmpty) {
    options = options.filter(opt => (opt.count || 0) > 0)
  }
  if (displaySettings.hideSkip) {
    options = options.filter(opt => {
      const label = (opt.optionLabel || opt.optionValue || '').toLowerCase()
      return !label.includes('跳过') && !label.includes('skip')
    })
  }
  
  // 如果是图片选择，需要获取图片URL
  const isImageSelect = item.type === 'IMAGE_SELECT'
  const schemeOptions = item.scheme?.config?.options || []
  
  return options.map(opt => {
    const count = opt.count || 0
    const percentage = total > 0 ? ((count / total) * 100).toFixed(2) : 0
    
    // 查找对应的选项配置（用于获取图片URL）
    let imageUrl = null
    if (isImageSelect) {
      const optionConfig = schemeOptions.find(o => String(o.value) === String(opt.optionValue))
      if (optionConfig && optionConfig.image) {
        imageUrl = getImageUrl(optionConfig.image)
      }
    }
  
  return {
      optionLabel: opt.optionLabel || opt.optionValue,
      optionValue: opt.optionValue,
      count: count,
      percentage: parseFloat(percentage),
      image: imageUrl
    }
  })
}

// 获取级联选择的树形表格数据
const getCascaderTableData = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.optionStats) return []
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) return []
  
  let options = stat.optionStats
  const total = stat.totalCount || 0
  
  // 过滤选项
  if (displaySettings.hideEmpty) {
    options = options.filter(opt => (opt.count || 0) > 0)
  }
  
  // 构建树形结构
  const treeData = []
  const nodeMap = new Map()
  let nodeId = 0
  
  // 先按路径长度排序
  const sortedOptions = [...options].sort((a, b) => {
    const pathA = a.fullPath || a.optionLabel || ''
    const pathB = b.fullPath || b.optionLabel || ''
    return pathA.split('/').length - pathB.split('/').length
  })
  
  sortedOptions.forEach(opt => {
    const fullPath = opt.fullPath || opt.optionLabel || ''
    const pathParts = fullPath.split('/').filter(p => p.trim())
    const count = opt.count || 0
    
    if (pathParts.length === 0) return
    
    // 逐级构建树
    let currentPath = ''
    let parentNode = null
    
    pathParts.forEach((part, index) => {
      currentPath = currentPath ? `${currentPath}/${part}` : part
      const nodeKey = currentPath
      
      if (!nodeMap.has(nodeKey)) {
        const nodeIdStr = String(nodeId++)
        const isLeaf = index === pathParts.length - 1
        const node = {
          id: nodeIdStr,
          optionLabel: part,
          fullPath: currentPath,
          count: isLeaf ? count : 0, // 只有叶子节点显示实际统计数
          percentage: 0, // 稍后计算
          children: [],
          hasChildren: !isLeaf
        }
        
        nodeMap.set(nodeKey, node)
        
        if (parentNode === null) {
          // 根节点
          treeData.push(node)
        } else {
          // 子节点
          parentNode.children.push(node)
        }
        
        parentNode = node
      } else {
        const node = nodeMap.get(nodeKey)
        if (index === pathParts.length - 1) {
          // 更新叶子节点数据（如果路径完全相同，应该合并）
          node.count = (node.count || 0) + count
        } else {
          // 累计父节点数量
          node.count = (node.count || 0) + count
        }
        parentNode = node
      }
    })
  })
  
  // 计算父节点百分比
  const calculatePercentage = (node) => {
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => calculatePercentage(child))
      // 父节点的百分比基于总数计算
      node.percentage = total > 0 ? ((node.count / total) * 100).toFixed(2) : 0
    }
  }
  
  treeData.forEach(node => calculatePercentage(node))
  
  return treeData
}

// 获取图片预览列表（用于图片选择的预览）
const getImagePreviewList = (formItemId) => {
  const tableData = getTableData(formItemId)
  return tableData.filter(row => row.image).map(row => row.image)
}

// 获取图片在预览列表中的索引
const getImageIndex = (formItemId, imageUrl) => {
  const previewList = getImagePreviewList(formItemId)
  return previewList.indexOf(imageUrl)
}

// 获取总填写人数
const getTotalCount = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  return stat?.totalCount || 0
}

// 获取词云图配置
const getWordCloudOption = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.wordCloudData || stat.wordCloudData.length === 0) {
    return null
  }
  return generateChartOption('wordcloud', stat, currentColorScheme.value)
}

// 获取文本统计
const getTextStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  if (!stat) return 0
  // 后端返回的是 validAnswers 和 totalAnswers
  if (key === 'count') return stat.validAnswers || 0
  if (key === 'total') return stat.totalAnswers || 0
  return stat[key] || 0
}


// 获取签名统计
const getSignatureStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  if (!stat) return 0
  if (key === 'count') return stat.validSignatures || stat.validAnswers || 0
  if (key === 'total') return stat.totalAnswers || 0
  return stat[key] || 0
}

// 获取签名预览列表
const getSignaturePreviewList = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.signatureList) return []
  // 返回签名图片URL列表（base64或URL格式）
  return stat.signatureList.filter(url => url && (url.startsWith('data:image') || url.startsWith('http')))
}

// 获取文件上传统计
const getUploadStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  if (!stat) return 0
  return stat[key] || 0
}

// 获取文件类型统计
const getFileTypeStats = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.fileTypeStats) return []
  return stat.fileTypeStats
}

// 获取图片上传列表
const getImageUploadList = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.imageList) return []
  return stat.imageList
}

// 获取图片URL
const getImageUploadUrl = (image) => {
  if (!image) return ''
  if (typeof image === 'string') {
    return getImageUrl(image)
  }
  if (image.url) {
    return getImageUrl(image.url)
  }
  return ''
}

// 获取图片URL列表
const getImageUploadUrlList = (formItemId) => {
  const images = getImageUploadList(formItemId)
  return images.map(img => getImageUploadUrl(img)).filter(url => url)
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 处理移动端操作命令
const handleActionCommand = (command) => {
  switch (command) {
    case 'colorScheme':
      showColorSchemeDialog.value = true
      break
    case 'refresh':
      handleRefresh()
      break
  }
}

// 获取评分统计
const getRatingStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  return stat ? stat[key] : 0
}

// 获取数字统计
const getNumberStat = (formItemId, key) => {
  const stat = statisticsData.value[formItemId]
  if (!stat) return 0
  // 数字题可以计算平均值、最大值、最小值
  if (key === 'average') {
    // 这里需要从原始数据计算，暂时返回0
    return 0
  }
  return stat[key] || 0
}

// 判断是否为评分题
const isRatingType = (type) => {
  return ['RATE', 'SLIDER'].includes(type)
}

// 获取评分题图表配置
const getRatingChartOption = (formItemId) => {
  const stat = statisticsData.value[formItemId]
  if (!stat || !stat.optionStats) return null
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) return null
  
  // 构建评分分布柱状图
  const data = stat.optionStats.map(opt => ({
    value: opt.count || 0,
    name: opt.optionLabel || opt.optionValue
  }))
  
  return {
    title: {
      text: item.label + ' - 评分分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name)
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [{
      name: '评分人数',
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: {
        color: '#409EFF'
      }
    }]
  }
}

// 判断是否为选择题
const isChoiceType = (type) => {
  return ['RADIO', 'CHECKBOX', 'SELECT', 'IMAGE_SELECT', 'CASCADER'].includes(type)
}

// 获取选择题类型的表单项
const choiceFormItems = computed(() => {
  return formItems.value.filter(item => isChoiceType(item.type))
})

// 获取可用的自变量选项（排除已选择的自变量）
const getAvailableIndependentVars = (currentIndex) => {
  const selected = crossAnalysisForm.independentVars.filter((id, idx) => idx !== currentIndex && id)
  return choiceFormItems.value.filter(item => !selected.includes(item.formItemId))
  }

// 获取可用的因变量选项（排除已选择的因变量和自变量）
const getAvailableDependentVars = (currentIndex) => {
  const selectedIndependent = crossAnalysisForm.independentVars.filter(id => id)
  const selectedDependent = crossAnalysisForm.dependentVars.filter((id, idx) => idx !== currentIndex && id)
  const allSelected = [...selectedIndependent, ...selectedDependent]
  return choiceFormItems.value.filter(item => !allSelected.includes(item.formItemId))
}

// 添加自变量
const addIndependentVar = () => {
  if (crossAnalysisForm.independentVars.length < 2) {
    crossAnalysisForm.independentVars.push(null)
  }
}

// 删除自变量
const removeIndependentVar = (index) => {
  crossAnalysisForm.independentVars.splice(index, 1)
  crossAnalysisResults.value = []
}

// 添加因变量
const addDependentVar = () => {
  if (crossAnalysisForm.dependentVars.length < 10) {
    crossAnalysisForm.dependentVars.push(null)
  }
}

// 删除因变量
const removeDependentVar = (index) => {
  crossAnalysisForm.dependentVars.splice(index, 1)
  crossAnalysisResults.value = []
}

// 处理自变量变化
const handleIndependentVarChange = (index) => {
  crossAnalysisResults.value = []
}

// 处理因变量变化
const handleDependentVarChange = (index) => {
  crossAnalysisResults.value = []
}

// 组合多个自变量的值作为行标签
const combineIndependentValues = (data, independentVars) => {
  const values = independentVars.map(varId => {
    if (!varId) return ''
    const item = formItems.value.find(i => i.formItemId === varId)
    if (!item) return ''
    const value = data.originalData?.[varId]
    if (value === null || value === undefined || value === '') return ''
    
    // 获取选项标签
    const scheme = item.scheme || {}
    const config = scheme.config || {}
    const options = config.options || []
    
    if (Array.isArray(value)) {
      // 多选：组合多个选项
      return value.map(v => {
        const opt = options.find(o => o.value === v)
        return opt ? opt.label : v
      }).join('、')
    } else {
      // 单选
      const opt = options.find(o => o.value === value)
      return opt ? opt.label : value
    }
  }).filter(v => v) // 过滤空值
  
  return values.join('/') || '未填写'
}

// 执行交叉分析
const handleCrossAnalyze = async (showWarning = true) => {
  // 验证自变量（至少1个，最多2个）
  const validIndependentVars = crossAnalysisForm.independentVars.filter(id => id)
  if (validIndependentVars.length === 0) {
    if (showWarning) {
      ElMessage.warning('请至少选择1个自变量')
    }
    return
  }

  // 验证因变量（至少1个，最多10个）
  const validDependentVars = crossAnalysisForm.dependentVars.filter(id => id)
  if (validDependentVars.length === 0) {
    if (showWarning) {
      ElMessage.warning('请至少选择1个因变量')
    }
    return
  }

  if (!surveyId.value) {
    if (showWarning) {
    ElMessage.warning('问卷ID不存在')
    }
    return
  }

  crossAnalyzing.value = true
  crossAnalysisResults.value = []
  
  try {
    // 获取表单数据
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 10000
    })
    
    if (dataRes.code !== 200 || !dataRes.data) {
      throw new Error('获取表单数据失败')
    }
    
    const dataList = dataRes.data.records || []
    
    // 对每个因变量分别进行交叉分析
    for (const dependentVarId of validDependentVars) {
      const dependentItem = formItems.value.find(item => item.formItemId === dependentVarId)
      if (!dependentItem) continue
      
      // 构建交叉表：行 = 自变量组合，列 = 因变量选项
      const crossTable = {}
      
      // 获取因变量的选项
      const dependentScheme = dependentItem.scheme || {}
      const dependentConfig = dependentScheme.config || {}
      const dependentOptions = dependentConfig.options || []
      
      // 遍历数据
      dataList.forEach(data => {
        // 组合自变量的值作为行标签
        const rowKey = combineIndependentValues(data, validIndependentVars)
        
        // 获取因变量的值
        const dependentValue = data.originalData?.[dependentVarId]
        if (dependentValue === null || dependentValue === undefined || dependentValue === '') {
          return // 跳过空值
        }
        
        // 处理因变量的值（单选或多选）
        const values = Array.isArray(dependentValue) ? dependentValue : [dependentValue]
        values.forEach(val => {
          // 获取选项标签
          const opt = dependentOptions.find(o => o.value === val)
          const colKey = opt ? opt.label : val
          
          // 累加计数
          if (!crossTable[rowKey]) {
            crossTable[rowKey] = {}
          }
          crossTable[rowKey][colKey] = (crossTable[rowKey][colKey] || 0) + 1
        })
      })
      
      // 构建结果对象
      const result = {
        dependentVarId,
        dependentVarTitle: dependentItem.label,
        independentVars: validIndependentVars.map(id => {
          const item = formItems.value.find(i => i.formItemId === id)
          return {
            formItemId: id,
            title: item ? item.label : ''
          }
        }),
        crossTable
      }
      
      crossAnalysisResults.value.push(result)
    }
    
    ElMessage.success(`成功分析 ${crossAnalysisResults.value.length} 个因变量`)
  } catch (error) {
    ElMessage.error('交叉分析失败')
    console.error('交叉分析错误:', error)
  } finally {
    crossAnalyzing.value = false
  }
}

// 切换百分比视图
const togglePercentageView = () => {
  showPercentage.value = !showPercentage.value
  // 百分比切换会通过computed自动更新表格数据
}

// 获取自变量的标题
const getIndependentVarsTitle = (independentVars) => {
  if (!independentVars || independentVars.length === 0) return ''
  return independentVars.map(v => v.title).join(' × ')
}

// 获取行的标签（X\Y）
const getRowLabel = (result) => {
  if (!result.independentVars || result.independentVars.length === 0) return 'X\\Y'
  return result.independentVars.map(v => v.title).join(' × ') + ' \\ ' + result.dependentVarTitle
}

// 获取交叉表的列
const getCrossTableColumns = (result) => {
  if (!result?.crossTable) return []
  const rows = Object.keys(result.crossTable)
  const cols = new Set()
  rows.forEach(row => {
    Object.keys(result.crossTable[row]).forEach(col => cols.add(col))
  })
  return Array.from(cols)
}

// 获取交叉表的表格数据
const getCrossTableData = (result) => {
  if (!result?.crossTable) return []
  
  const rows = Object.keys(result.crossTable)
  const cols = getCrossTableColumns(result)
  
  // 计算行合计
  const rowTotals = {}
  rows.forEach(row => {
    rowTotals[row] = 0
    cols.forEach(col => {
      rowTotals[row] += result.crossTable[row][col] || 0
    })
  })
  
  // 计算列合计
  const summary = {}
  cols.forEach(col => {
    summary[col] = { total: 0 }
    rows.forEach(row => {
      summary[col].total += result.crossTable[row][col] || 0
    })
  })
  
  return rows.map(row => {
    const rowTotal = rowTotals[row] || 0
    const rowData = { rowLabel: row, rowTotal }
    
    cols.forEach(col => {
      const count = result.crossTable[row][col] || 0
      if (showPercentage.value) {
        // 计算行百分比和列百分比
        const rowPercentage = rowTotal > 0 ? (count / rowTotal * 100) : 0
        const colPercentage = summary[col].total > 0 ? (count / summary[col].total * 100) : 0
        rowData[col] = {
          count,
          rowPercentage,
          colPercentage
        }
      } else {
        rowData[col] = { count }
      }
    })
    return rowData
  })
}

// 获取交叉表的摘要
const getCrossTableSummary = (result) => {
  if (!result?.crossTable) return {}
  const rows = Object.keys(result.crossTable)
  const cols = getCrossTableColumns(result)
  const summary = {}
  cols.forEach(col => {
    summary[col] = { total: 0 }
    rows.forEach(row => {
      summary[col].total += result.crossTable[row][col] || 0
    })
  })
  return summary
}

// 获取交叉表的总计
const getCrossTableTotal = (result) => {
  if (!result?.crossTable) return 0
  const rows = Object.keys(result.crossTable)
  const cols = getCrossTableColumns(result)
  let total = 0
  rows.forEach(row => {
    cols.forEach(col => {
      total += result.crossTable[row][col] || 0
    })
  })
  return total
}

// 已移除：buildHeatmap（已由 buildHeatmapForResult 替代）

// 切换交叉分析图表类型
const switchCrossChartType = (type, resultIndex = 0) => {
  crossChartTypeMap.value[resultIndex] = type
  // 保存到localStorage
  try {
    localStorage.setItem(`crossChartType_${resultIndex}`, type)
  } catch (error) {
    console.error('保存图表类型失败:', error)
  }
}

// 获取当前交叉分析图表类型
const getCurrentCrossChartType = (resultIndex = 0) => {
  // 优先从map读取
  if (crossChartTypeMap.value[resultIndex]) {
    return crossChartTypeMap.value[resultIndex]
  }
  // 从localStorage读取
  try {
    const saved = localStorage.getItem(`crossChartType_${resultIndex}`)
    if (saved && crossChartTypes.find(t => t.value === saved)) {
      crossChartTypeMap.value[resultIndex] = saved
      return saved
    }
  } catch (error) {
    console.error('加载图表类型失败:', error)
  }
  // 默认返回table
  return 'table'
}

// 获取交叉分析图表类型标签
const getCrossChartTypeLabel = (type) => {
  const typeMap = {
    table: '交叉表',
    horizontalBar: '堆叠条形图',
    bar: '堆叠柱状图',
    line: '折线图',
    heatmap: '热力图'
  }
  return typeMap[type] || type
}

// 已移除：getCrossChartOption（已由 getCrossChartOptionForResult 替代）

// 已移除：buildAllCrossCharts（不再使用）
// 已移除：buildStackedHorizontalBarChart、buildStackedBarChart、buildLineChart（已由 generateCrossChartOption 替代）

// 获取交叉分析图表配置（为指定结果）
const getCrossChartOptionForResult = (result, resultIndex) => {
  if (!result?.crossTable) return null
  
  const chartType = getCurrentCrossChartType(resultIndex)
  if (chartType === 'table') return null
  
  const question1Title = getIndependentVarsTitle(result.independentVars)
  const question2Title = result.dependentVarTitle
  
  return generateCrossChartOption(chartType, result.crossTable, {
    question1Title,
    question2Title,
    colorScheme: currentColorScheme.value
  })
}

// 已移除：buildStackedHorizontalBarChart、buildStackedBarChart、buildLineChart（已由 generateCrossChartOption 替代）

// 切换对比分析图表类型
const switchCompareChartType = (type, resultIndex = 0) => {
  compareChartTypeMap.value[resultIndex] = type
  // 保存到localStorage
  try {
    localStorage.setItem(`compareChartType_${resultIndex}`, type)
  } catch (error) {
    console.error('保存图表类型失败:', error)
      }
}

// 获取当前对比分析图表类型
const getCurrentCompareChartType = (resultIndex = 0) => {
  // 优先从map读取
  if (compareChartTypeMap.value[resultIndex]) {
    return compareChartTypeMap.value[resultIndex]
  }
  // 从localStorage读取
  try {
    const saved = localStorage.getItem(`compareChartType_${resultIndex}`)
    if (saved && compareChartTypes.find(t => t.value === saved)) {
      compareChartTypeMap.value[resultIndex] = saved
      return saved
    }
  } catch (error) {
    console.error('加载图表类型失败:', error)
  }
  // 默认返回table
  return 'table'
}

// 切换对比分析显示模式
const switchCompareDisplayMode = (mode, resultIndex = 0) => {
  compareDisplayModeMap.value[resultIndex] = mode
  // 保存到localStorage
  try {
    localStorage.setItem(`compareDisplayMode_${resultIndex}`, mode)
  } catch (error) {
    console.error('保存显示模式失败:', error)
  }
}

// 获取当前对比分析显示模式
const getCurrentCompareDisplayMode = (resultIndex = 0) => {
  // 优先从map读取
  if (compareDisplayModeMap.value[resultIndex]) {
    return compareDisplayModeMap.value[resultIndex]
  }
  // 从localStorage读取
  try {
    const saved = localStorage.getItem(`compareDisplayMode_${resultIndex}`)
    if (saved && (saved === 'count' || saved === 'percentage')) {
      compareDisplayModeMap.value[resultIndex] = saved
      return saved
    }
  } catch (error) {
    console.error('加载显示模式失败:', error)
  }
  // 默认返回数量模式
  return 'count'
}

// 获取进度条显示状态
const getCurrentCompareProgressBar = (resultIndex = 0) => {
  // 如果未设置，默认显示进度条
  if (compareShowProgressBarMap.value[resultIndex] === undefined) {
    compareShowProgressBarMap.value[resultIndex] = true
  }
  return compareShowProgressBarMap.value[resultIndex]
}

// 获取进度条颜色（使用配色方案的第一种颜色）
const getProgressBarColor = computed(() => {
  return currentColorScheme.value?.colors?.[0] || '#409EFF'
})

// 获取进度条渐变（使用配色方案的前两种颜色）
const getProgressBarGradient = computed(() => {
  const colors = currentColorScheme.value?.colors || ['#409EFF', '#66b1ff']
  const color1 = colors[0] || '#409EFF'
  const color2 = colors[1] || colors[0] || '#66b1ff'
  return `linear-gradient(90deg, ${color1} 0%, ${color2} 100%)`
})

// 获取对比分析图表配置
const getCompareChartOption = (compareItem, resultIndex) => {
  if (!compareItem?.compareData || !compareItem.groups || compareItem.groups.length === 0) {
    return null
  }
  
  const chartType = getCurrentCompareChartType(resultIndex)
  if (chartType === 'table') return null
  
  return generateCompareChartOption(chartType, compareItem.compareData, compareItem.groups, {
    questionTitle: compareItem.questionTitle,
    colorScheme: currentColorScheme.value
  })
}

// 已移除：buildCompareBarChart、buildCompareHorizontalBarChart、buildCompareLineChart（已由 generateCompareChartOption 替代）

// 已移除：buildComparePieChart（对比分析不使用饼图）

// 已移除：getColorByIndex（已由 generateCompareChartOption 内部处理）

// 计算对比分析表格行的总计
const getCompareRowTotal = (row, groups) => {
  if (!groups || !row) return 0
  let total = 0
  groups.forEach(group => {
    total += row[group]?.count || 0
  })
  return total
}

// 判断是否为文本题
const isTextType = (type) => {
  return ['INPUT', 'TEXTAREA', 'DATE', 'TIME', 'DATETIME'].includes(type)
}

// 获取类型标签
const getTypeLabel = (type) => {
  const typeMap = {
    INPUT: '单行文本',
    TEXTAREA: '多行文本',
    RADIO: '单选框',
    CHECKBOX: '多选框',
    SELECT: '下拉框',
    IMAGE_SELECT: '图片选择',
    CASCADER: '级联选择',
    NUMBER: '数字',
    DATE: '日期',
    TIME: '时间',
    DATETIME: '日期时间',
    RATE: '评分',
    SLIDER: '滑块'
  }
  return typeMap[type] || type
}

// 加载趋势数据
const loadTrendData = async () => {
  if (!surveyId.value) return
  
  analysisLoading.value = true
  try {
    // 1. 使用后端API获取填写趋势
    const trendRes = await statisticsApi.getResponseTrend(surveyId.value, timeRange.value)
    if (trendRes.code === 200 && trendRes.data) {
      const trendData = trendRes.data.data || []
      trendChartOption.value = {
        title: { text: '填写趋势', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: trendData.map(item => item.date)
        },
        yAxis: { type: 'value', name: '填写数量' },
        series: [{
          name: '填写数量',
          type: 'line',
          data: trendData.map(item => item.count),
          smooth: true,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ]
            }
          },
          itemStyle: { color: '#409EFF' }
        }]
      }
      }
      
    // 2. 使用后端API获取设备统计
    const deviceRes = await statisticsApi.getDeviceStatistics(surveyId.value)
    if (deviceRes.code === 200 && deviceRes.data) {
      const deviceCount = deviceRes.data.deviceCount || {}
      const deviceData = Object.entries(deviceCount).map(([name, value]) => ({
        name: name === 'PC' ? 'PC端' : name === 'MOBILE' ? '移动端' : name,
        value: value
      }))
      deviceChartOption.value = {
        title: { text: '设备类型分布', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '设备类型',
          type: 'pie',
          radius: '60%',
          data: deviceData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      }
      
    // 3. 填写时段分布（需要从Response数据计算，暂时保留前端计算）
    // 这里可以从后端获取Response列表来计算
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 1000
    })
    if (dataRes.code === 200 && dataRes.data) {
      const dataList = dataRes.data.records || []
      
      // 填写时段分布
      const hourData = calculateHourData(dataList)
      hourChartOption.value = {
        title: { text: '填写时段分布', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: hourData.hours
        },
        yAxis: { type: 'value', name: '填写数量' },
        series: [{
          name: '填写数量',
          type: 'bar',
          data: hourData.counts,
          itemStyle: { color: '#409EFF' }
        }]
      }
      
      // 填写时长分布
      const durationData = calculateDurationData(dataList)
      if (durationData) {
        durationChartOption.value = {
          title: { text: '填写时长分布', left: 'center' },
          tooltip: { 
            trigger: 'axis',
            formatter: (params) => {
              const param = params[0]
              return `${param.name}<br/>数量: ${param.value}`
            }
          },
          xAxis: {
            type: 'category',
            data: durationData.labels,
            name: '时长区间'
          },
          yAxis: { type: 'value', name: '填写数量' },
          series: [{
            name: '填写数量',
            type: 'bar',
            data: durationData.counts,
            itemStyle: { color: '#67C23A' }
          }]
        }
      }
      
      // 浏览器类型分布
      const browserData = calculateBrowserData(dataList)
      if (browserData && browserData.length > 0) {
        browserChartOption.value = {
          title: { text: '浏览器类型分布', left: 'center' },
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [{
            name: '浏览器类型',
            type: 'pie',
            radius: '60%',
            data: browserData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }]
        }
      }
      
      // 题目完成率
      const completionData = calculateCompletionRate(dataList)
      if (completionData && completionData.length > 0) {
        completionRateChartOption.value = {
          title: { text: '题目完成率', left: 'center' },
          tooltip: { 
            trigger: 'axis',
            formatter: (params) => {
              const param = params[0]
              return `${param.name}<br/>完成率: ${param.value}%`
            }
          },
          xAxis: {
            type: 'category',
            data: completionData.map(item => item.questionTitle.length > 8 ? item.questionTitle.substring(0, 8) + '...' : item.questionTitle),
            axisLabel: {
              rotate: 45,
              interval: 0
            }
          },
          yAxis: { 
            type: 'value', 
            name: '完成率(%)',
            max: 100
          },
          series: [{
            name: '完成率',
            type: 'bar',
            data: completionData.map(item => item.rate),
            itemStyle: { 
              color: (params) => {
                const rate = params.value
                if (rate >= 90) return '#67C23A'
                if (rate >= 70) return '#E6A23C'
                return '#F56C6C'
              }
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c}%'
            }
          }]
        }
      }
      
      // 填写质量分布
      const qualityData = calculateQualityData(dataList)
      if (qualityData) {
        qualityChartOption.value = {
          title: { text: '填写质量分布', left: 'center' },
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [{
            name: '填写质量',
            type: 'pie',
            radius: '60%',
            data: qualityData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }]
        }
      }
      
      // 选项热度对比
      const optionHeatData = calculateOptionHeat(dataList)
      if (optionHeatData && optionHeatData.length > 0) {
        optionHeatChartOption.value = {
          title: { text: '选项热度对比（TOP 10）', left: 'center' },
          tooltip: { 
            trigger: 'axis',
            formatter: (params) => {
              const param = params[0]
              // 获取对应的选项名称
              const rankIndex = parseInt(param.name) - 1
              const reversedData = [...optionHeatData].reverse()
              const optionLabel = reversedData[rankIndex]?.optionLabel || ''
              return `排名 ${param.name}<br/>${optionLabel}<br/>选择次数: ${param.value}`
            }
          },
          xAxis: {
            type: 'value',
            name: '选择次数'
          },
          yAxis: {
            type: 'category',
            // 从高到低排序，显示排名序号（1-10）
            data: optionHeatData.map((item, index) => `${optionHeatData.length - index}`).reverse(),
            axisLabel: {
              interval: 0,
              formatter: (value, index) => {
                // value 是排名序号，显示序号即可
                return value
              },
              lineHeight: 14
            }
          },
          series: [{
            name: '选择次数',
            type: 'bar',
            // 从高到低排序，需要reverse因为条形图是从下往上显示
            data: optionHeatData.map(item => item.count).reverse(),
            itemStyle: { color: '#F56C6C' }
          }]
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载分析数据失败')
    console.error('加载分析数据错误:', error)
  } finally {
    analysisLoading.value = false
  }
}

// 计算趋势数据
const calculateTrendData = (dataList, range) => {
  const dates = []
  const counts = []
  const now = dayjs()
  let days = 30
  
  if (range === '7d') days = 7
  else if (range === 'all') {
    if (dataList.length === 0) return { dates: [], counts: [] }
    const firstDate = dayjs(dataList[dataList.length - 1].createTime)
    days = now.diff(firstDate, 'day') + 1
  }
  
  for (let i = days - 1; i >= 0; i--) {
    const date = now.subtract(i, 'day')
    const dateStr = date.format('MM-DD')
    dates.push(dateStr)
    
    const count = dataList.filter(item => {
      const itemDate = dayjs(item.createTime)
      return itemDate.isSame(date, 'day')
    }).length
    counts.push(count)
  }
  
  return { dates, counts }
}

// 计算设备数据
const calculateDeviceData = (dataList) => {
  const deviceMap = {}
  dataList.forEach(item => {
    const device = item.submitOs || '未知'
    deviceMap[device] = (deviceMap[device] || 0) + 1
  })
  
  return Object.entries(deviceMap).map(([name, value]) => ({
    name,
    value
  }))
}

// 计算时段数据
const calculateHourData = (dataList) => {
  const hourCounts = new Array(24).fill(0)
  
  dataList.forEach(item => {
    if (item.createTime) {
      const hour = dayjs(item.createTime).hour()
      hourCounts[hour]++
    }
  })
  
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  return { hours, counts: hourCounts }
}

// 计算填写时长分布
const calculateDurationData = (dataList) => {
  const durations = []
  dataList.forEach(item => {
    if (item.completeTime && item.completeTime > 0) {
      durations.push(item.completeTime) // 单位：秒
    }
  })
  
  if (durations.length === 0) return null
  
  // 将时长分为区间：0-30秒, 30-60秒, 1-3分钟, 3-5分钟, 5-10分钟, 10分钟以上
  const ranges = [
    { label: '0-30秒', min: 0, max: 30 },
    { label: '30秒-1分钟', min: 30, max: 60 },
    { label: '1-3分钟', min: 60, max: 180 },
    { label: '3-5分钟', min: 180, max: 300 },
    { label: '5-10分钟', min: 300, max: 600 },
    { label: '10分钟以上', min: 600, max: Infinity }
  ]
  
  const counts = ranges.map(range => {
    return durations.filter(d => d >= range.min && d < range.max).length
  })
  
  const labels = ranges.map(r => r.label)
  return { labels, counts }
}

// 计算浏览器类型分布
const calculateBrowserData = (dataList) => {
  const browserMap = {}
  dataList.forEach(item => {
    const browser = item.submitBrowser || '未知'
    // 简化浏览器名称
    let browserName = browser
    if (browser.includes('Chrome')) browserName = 'Chrome'
    else if (browser.includes('Firefox')) browserName = 'Firefox'
    else if (browser.includes('Safari')) browserName = 'Safari'
    else if (browser.includes('Edge')) browserName = 'Edge'
    else if (browser.includes('Opera')) browserName = 'Opera'
    else if (browser.includes('IE')) browserName = 'IE'
    
    browserMap[browserName] = (browserMap[browserName] || 0) + 1
  })
  
  return Object.entries(browserMap)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
}

// 计算题目完成率
const calculateCompletionRate = (dataList) => {
  if (!formItems.value || formItems.value.length === 0) return []
  
  const totalResponses = dataList.length
  if (totalResponses === 0) return []
  
  const completionRates = formItems.value.map(item => {
    let filledCount = 0
    dataList.forEach(data => {
      const originalData = data.originalData || {}
      const value = originalData[item.formItemId]
      if (value !== null && value !== undefined && value !== '') {
        // 如果是数组，检查是否为空数组
        if (Array.isArray(value)) {
          if (value.length > 0) filledCount++
        } else {
          filledCount++
        }
      }
    })
    
    const rate = totalResponses > 0 ? (filledCount / totalResponses * 100).toFixed(2) : 0
    return {
      questionTitle: item.label,
      filledCount,
      totalCount: totalResponses,
      rate: parseFloat(rate)
    }
  })
  
  return completionRates.sort((a, b) => a.rate - b.rate) // 按完成率从低到高排序
}

// 计算填写质量分布
const calculateQualityData = (dataList) => {
  if (!formItems.value || formItems.value.length === 0) return null
  
  const totalQuestions = formItems.value.length
  const qualityMap = {
    '优秀 (90%以上)': 0,
    '良好 (70%-90%)': 0,
    '一般 (50%-70%)': 0,
    '较差 (50%以下)': 0
  }
  
  dataList.forEach(data => {
    const originalData = data.originalData || {}
    let filledCount = 0
    
    formItems.value.forEach(item => {
      const value = originalData[item.formItemId]
      if (value !== null && value !== undefined && value !== '') {
        if (Array.isArray(value)) {
          if (value.length > 0) filledCount++
        } else {
          filledCount++
        }
      }
    })
    
    const rate = totalQuestions > 0 ? (filledCount / totalQuestions * 100) : 0
    if (rate >= 90) qualityMap['优秀 (90%以上)']++
    else if (rate >= 70) qualityMap['良好 (70%-90%)']++
    else if (rate >= 50) qualityMap['一般 (50%-70%)']++
    else qualityMap['较差 (50%以下)']++
  })
  
  return Object.entries(qualityMap)
    .map(([name, value]) => ({ name, value }))
    .filter(item => item.value > 0)
}

// 计算选项热度对比
const calculateOptionHeat = (dataList) => {
  const optionCountMap = {}
  
  formItems.value.forEach(item => {
    if (!isChoiceType(item.type)) return
    
    const scheme = item.scheme || {}
    const config = scheme.config || {}
    const options = config.options || []
    
    dataList.forEach(data => {
      const originalData = data.originalData || {}
      const value = originalData[item.formItemId]
      
      if (value !== null && value !== undefined && value !== '') {
        const values = Array.isArray(value) ? value : [value]
        values.forEach(val => {
          const option = options.find(opt => opt.value === val)
          if (option) {
            const key = `${item.label} - ${option.label || option.value}`
            optionCountMap[key] = (optionCountMap[key] || 0) + 1
          }
        })
      }
    })
  })
  
  return Object.entries(optionCountMap)
    .map(([optionLabel, count]) => ({ optionLabel, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10) // 取TOP 10
}

// 刷新数据
const handleRefresh = async () => {
  try {
    // 先清除缓存，然后重新加载统计数据
    if (surveyId.value) {
      await statisticsApi.refreshStatistics(surveyId.value)
      ElMessage.success('缓存已清除，正在刷新数据...')
    }
    await loadStatistics()
  if (activeTab.value === 'analysis') {
    loadTrendData()
    }
  } catch (error) {
    console.error('刷新数据失败:', error)
    // 即使刷新缓存失败，也尝试重新加载数据
    await loadStatistics()
    if (activeTab.value === 'analysis') {
      loadTrendData()
    }
  }
}

// 监听标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'analysis' && !trendChartOption.value) {
    loadTrendData()
  }
}

// 处理配色方案变化（立即生效）
const handleColorSchemeChange = (schemeId) => {
  const scheme = colorSchemes.find(s => s.id === schemeId)
  if (scheme) {
    currentColorScheme.value = scheme
    saveColorScheme(schemeId)
    ElMessage.success('配色方案已更新')
  }
}

// 计算完成率
const getCompletionRate = () => {
  if (!surveyStatistics.value) return 0
  const total = surveyStatistics.value.totalResponses || 0
  const completed = surveyStatistics.value.completedResponses || 0
  if (total === 0) return 0
  return Math.round((completed / total) * 10000) / 100
}


// 处理对比变量变化
const handleCompareVariableChange = () => {
  compareAnalysisResult.value = null
}

// 执行对比分析
const handleCompareAnalyze = async () => {
  if (!compareForm.compareVariable) {
    ElMessage.warning('请选择对比变量')
    return
  }

  if (!surveyId.value) {
    ElMessage.warning('问卷ID不存在')
    return
  }

  compareAnalyzing.value = true
  try {
    // 调用对比分析接口
    const res = await analysisApi.compareAnalysis({
      surveyId: surveyId.value,
      compareVariable: compareForm.compareVariable
    })

    if (res.code === 200) {
      compareAnalysisResult.value = res.data?.results || []
      // 确保数据结构正确，计算行百分比
      if (compareAnalysisResult.value && compareAnalysisResult.value.length > 0) {
        compareAnalysisResult.value.forEach(result => {
          if (result.compareData && result.groups) {
            result.compareData.forEach(row => {
              // 计算该选项的总数（所有组的总和）
              let rowTotal = 0
              result.groups.forEach(group => {
                if (row[group] && typeof row[group] === 'object') {
                  rowTotal += row[group].count || 0
                }
              })
              // 计算每个组的行百分比（占该选项的比例）
              result.groups.forEach(group => {
                if (row[group] && typeof row[group] === 'object') {
                  const count = row[group].count || 0
                  row[group].percentage = rowTotal > 0 ? Math.round((count * 100.0 / rowTotal) * 100) / 100.0 : 0
                }
              })
            })
          }
        })
      }
      ElMessage.success('对比分析完成')
    }
  } catch (error) {
    console.error('对比分析失败:', error)
    ElMessage.error('对比分析失败')
  } finally {
    compareAnalyzing.value = false
  }
}

onMounted(() => {
  loadFormConfig()
})
</script>

<style lang="scss" scoped>
.statistics-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  @media (max-width: 768px) {
    padding: 10px;
  }
}

.statistics-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  
  :deep(.el-card__header) {
    position: sticky;
    top: 0;
    z-index: 101;
    background: #fff;
    flex-shrink: 0;
  }
  
  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}

.statistics-container::-webkit-scrollbar {
  width: 6px;
}

.statistics-container::-webkit-scrollbar-track {
  background: transparent;
}

.statistics-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 3px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-actions {
    display: flex;
    align-items: center;
  }
  
  &.mobile-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-title {
    font-size: 16px;
    font-weight: 500;
    
    @media (max-width: 768px) {
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  .mobile-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  &.fixed-header {
    background: #fff;
    
    @media (max-width: 768px) {
      // 移动端样式已在上面定义
    }
  }
}

.fixed-tabs {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  
  :deep(.el-tabs__header) {
    position: sticky;
    top: 0;
    z-index: 99;
    background: #fff;
    margin: 0;
    padding: 0 20px;
    border-bottom: 1px solid #EBEEF5;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    flex-shrink: 0;
  }
  
  :deep(.el-tabs__content) {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    -webkit-overflow-scrolling: touch;
    padding: 20px;
    
    /* 自定义滚动条样式 */
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.3);
      border-radius: 3px;
    }
    
    @media (max-width: 768px) {
      padding: 15px 10px;
    }
  }
  
  :deep(.el-tab-pane) {
    height: auto;
    min-height: 100%;
  }
}

.statistics-content {
  min-height: 400px;
  
  @media (max-width: 768px) {
    padding: 15px 10px;
  }
}

.empty-tip {
  padding: 40px;
  text-align: center;
}

.question-stat-card {
  @media (max-width: 768px) {
    margin-bottom: 15px;
    
    :deep(.el-card__header) {
      padding: 15px;
    }
    
    :deep(.el-card__body) {
      padding: 15px;
    }
  }
  
  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .question-title {
      display: flex;
      align-items: center;
      flex: 1;

      .question-number {
        color: #909399;
        margin-right: 5px;
      }
      
      @media (max-width: 768px) {
        width: 100%;
        font-size: 14px;
        flex-wrap: wrap;
        
        .question-number {
          font-size: 14px;
        }
      }
    }

    .chart-type-switcher {
      margin-left: 20px;
      
      @media (max-width: 768px) {
        margin-left: 0;
        margin-top: 10px;
        width: 100%;
        
        &.mobile-switcher {
          :deep(.el-button-group) {
            display: flex;
            width: 100%;
            
            .el-button {
              flex: 1;
              font-size: 12px;
              padding: 8px 5px;
            }
          }
        }
      }
    }
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      gap: 15px;
    }
  }
}

.stat-item {
  margin-bottom: 40px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
  }
}

.stat-chart {
  .stat-table-wrapper {
    margin-bottom: 20px;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;

    .el-table {
      min-width: 100%;
      
      @media (max-width: 768px) {
        font-size: 12px;
        
        :deep(.el-table__header) {
          th {
            padding: 8px 5px;
            font-size: 12px;
          }
        }
        
        :deep(.el-table__body) {
          td {
            padding: 8px 5px;
            font-size: 12px;
          }
        }
      }
    }

    .percentage-cell {
      display: flex;
      align-items: center;
      gap: 10px;

      .bar-wrapper {
        flex: 1;
        min-width: 0;
        
        @media (max-width: 768px) {
          min-width: 100px;
        }

        .bar-bg {
          width: 100%;
          height: 18px;
          background-color: #f5f7fa;
          border-radius: 9px;
          overflow: hidden;
          position: relative;

          .bar-fill {
            height: 100%;
            border-radius: 9px;
            transition: width 0.3s ease, background-color 0.3s ease;
            min-width: 2px;
          }
        }
      }

      .percentage-text {
        min-width: 70px;
        text-align: right;
        font-weight: 500;
        color: #606266;
        font-size: 14px;
        flex-shrink: 0;
      }
    }

    .table-footer {
      margin-top: 10px;
      padding: 10px;
      text-align: center;
      background-color: #f5f7fa;
      border-radius: 4px;
    }
  }

  .chart-wrapper {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
  }
  
  .no-data {
    text-align: center;
    padding: 40px;
    color: #909399;
  }
}

.stat-text {
  .text-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }

  .wordcloud-wrapper {
    margin-top: 20px;
    background: #fff;
    padding: 20px;
    border-radius: 4px;

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.color-scheme-option {
  display: flex;
  align-items: center;
  gap: 10px;

  .color-preview {
    width: 30px;
    height: 20px;
    border-radius: 4px;
    border: 1px solid #ddd;
  }
}

.stat-upload,
.stat-image-upload {
  .upload-stat-info {
    margin-bottom: 20px;
  }

  .upload-detail-info {
    margin-top: 20px;

    .detail-row {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
      margin-bottom: 15px;
      font-size: 14px;
      color: #606266;

      span {
        white-space: nowrap;
      }
      
      @media (max-width: 768px) {
        gap: 15px;
        font-size: 12px;
        flex-direction: column;
        gap: 10px;
      }
    }

    .file-type-stats,
    .file-size-stats {
      margin-top: 20px;
    }

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.stat-image-upload {
  .image-upload-preview-list {
    margin-top: 20px;
    background: #fff;
    padding: 20px;
    border-radius: 4px;
    
    @media (max-width: 768px) {
      padding: 15px;
    }

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
      
      @media (max-width: 768px) {
        font-size: 14px;
        margin-bottom: 10px;
      }
    }

    .image-upload-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 15px;
      
      @media (max-width: 768px) {
        grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
        gap: 10px;
      }

      .image-upload-item {
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        padding: 5px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s;
        aspect-ratio: 1;

        &:hover {
          border-color: #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
        }

        .upload-image {
          width: 100%;
          height: 100%;
          display: block;
          border-radius: 4px;
        }
      }
    }
  }
}

.stat-rating {
  .rating-stat-info {
    margin-bottom: 20px;
  }
  
  .chart-wrapper {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
  }
}

// .stat-number 使用 Element Plus 栅格系统自动处理布局

.survey-overview-card {
  .el-statistic {
    text-align: center;
  }
}

.analysis-content {
  min-height: 400px;
}

// 交叉分析和对比分析样式
.cross-analysis-wrapper,
.compare-analysis-wrapper {
  .analysis-header {
  margin-bottom: 20px;
    
    .analysis-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
      cursor: pointer;
      user-select: none;
      
      .title-text {
        margin-right: 8px;
}

      .title-icon {
        transition: transform 0.3s;
        color: #909399;
      }
      
      @media (max-width: 768px) {
        font-size: 18px;
      }
    }
  }
}

.cross-analysis-card,
.compare-analysis-card {
  margin-bottom: 20px;
  
  @media (max-width: 768px) {
    margin-bottom: 15px;
    
    :deep(.el-card__body) {
      padding: 15px;
    }
  }
}

// 变量配置区域
.variable-section {
  .variable-title {
    margin-bottom: 12px;
    
    .title-label {
      font-weight: 500;
      font-size: 14px;
      color: #303133;
      margin-right: 8px;
    }
    
    .title-hint {
      font-size: 12px;
      color: #909399;
    }
    
    @media (max-width: 768px) {
      .title-label {
        font-size: 13px;
      }
      
      .title-hint {
        font-size: 11px;
        display: block;
        margin-top: 4px;
      }
    }
  }
  
  .variable-inputs {
    margin-bottom: 12px;
    
    .variable-input-item {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      gap: 8px;
      
      .variable-select {
        flex: 1;
        min-width: 0;
      }
      
      .variable-delete-btn {
        flex-shrink: 0;
      }
      
      @media (max-width: 768px) {
        margin-bottom: 12px;
      }
    }
  }
  
  .add-condition-btn {
    width: 100%;
    border: 1px dashed #dcdfe6;
    color: #606266;
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 0;
    border-radius: 4px;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
      color: #409eff;
      background-color: #ecf5ff;
    }
    
    @media (max-width: 768px) {
      padding: 12px 0;
    }
  }
}

// 操作按钮区域
.analysis-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
  
  @media (max-width: 768px) {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}

// 对比分析配置
.compare-config {
  .compare-hint {
    margin-bottom: 16px;
    font-size: 13px;
    color: #606266;
    
    @media (max-width: 768px) {
      font-size: 12px;
      margin-bottom: 12px;
    }
  }
  
  .compare-select {
    width: 100%;
  }
  
  .compare-actions {
    display: flex;
    align-items: flex-start;
    
    @media (max-width: 768px) {
      margin-top: 12px;
      
      .el-button {
        width: 100%;
      }
    }
  }
}

.compare-result-section {
  margin-top: 20px;
}

.stat-text {
  .text-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    margin-bottom: 20px;
}

  .wordcloud-wrapper {
    margin-top: 20px;
    background: #fff;
    padding: 20px;
    border-radius: 4px;

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.stat-signature {
  .signature-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }

  .signature-preview-list {
    margin-top: 20px;
    background: #fff;
    padding: 20px;
    border-radius: 4px;

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }

    .signature-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 15px;

      .signature-item {
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        padding: 10px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
        }

        .signature-image {
          width: 100%;
          height: 120px;
          display: block;
        }
      }
    }
  }
}

.image-option-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  .option-image {
    width: 50px;
    height: 50px;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
    flex-shrink: 0;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409EFF;
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
    }
    
    @media (max-width: 768px) {
      width: 40px;
      height: 40px;
    }
  }

  .option-label {
    flex: 1;
    word-break: break-word;
    
    @media (max-width: 768px) {
      font-size: 12px;
    }
  }
}

// 全局响应式样式
@media (max-width: 768px) {
  // Tab标签页响应式
  .fixed-tabs {
    :deep(.el-tabs__header) {
      padding: 0 10px;
    }
    
    :deep(.el-tabs__item) {
      padding: 0 15px;
      font-size: 14px;
    }
    
    :deep(.el-tabs__content) {
      padding: 15px 10px;
    }
  }
  
  .card-header.fixed-header {
    padding: 15px;
    margin: -10px -10px 0 -10px;
  }
  
  // 对话框响应式
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto;
    
    .el-dialog__header {
      padding: 15px;
    }
    
    .el-dialog__body {
      padding: 15px;
    }
  }
  
  // 表格响应式
  :deep(.el-table) {
    font-size: 12px;
    
    .el-table__header th,
    .el-table__body td {
      padding: 8px 5px;
    }
  }
  
  // 统计数字响应式
  :deep(.el-statistic) {
    .el-statistic__head {
      font-size: 12px;
    }
    
    .el-statistic__number {
      font-size: 24px;
    }
  }
  
  // 词云图响应式
  .wordcloud-wrapper {
    :deep(.echarts) {
      height: 250px !important;
    }
  }
}

.stat-text {
  .text-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }

  .wordcloud-wrapper {
    margin-top: 20px;
    background: #fff;
    padding: 20px;
    border-radius: 4px;

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
  font-weight: 500;
  color: #303133;
    }
  }
}

.stat-signature {
  .signature-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }

  .signature-preview-list {
    margin-top: 20px;
    background: #fff;
    padding: 20px;
    border-radius: 4px;

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }

    .signature-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 15px;

      .signature-item {
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        padding: 10px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
        }

        .signature-image {
          width: 100%;
          height: 120px;
          display: block;
        }
      }
    }
  }
}

.stat-display {
  padding: 40px;
  text-align: center;
}

.cross-result-section {
  margin-top: 20px;
  
  @media (max-width: 768px) {
    margin-top: 15px;
  }
}

.cross-table-summary {
  display: flex;
  border: 1px solid #EBEEF5;
  border-top: none;
  background-color: #F5F7FA;
  font-size: 14px;
  
  .summary-label {
    width: 150px;
    padding: 12px;
    font-weight: 500;
    text-align: center;
    border-right: 1px solid #EBEEF5;
    background-color: #FAFAFA;
  }
  
  .summary-cell {
    flex: 1;
    width: 120px;
    padding: 12px;
    text-align: center;
    border-right: 1px solid #EBEEF5;
    
    &:last-child {
      border-right: none;
    }
    
    &.summary-total {
      background-color: #F0F9FF;
      font-weight: 500;
    }
  }
  
  @media (max-width: 768px) {
    font-size: 12px;
    
    .summary-label,
    .summary-cell {
      padding: 8px 5px;
    }
  }
}

.percentage-legend {
  padding: 10px;
  background-color: #F5F7FA;
  border-radius: 4px;
  
  @media (max-width: 768px) {
    font-size: 11px;
    padding: 8px;
    
    span {
      display: block;
      margin: 5px 0;
    }
  }
}

.cross-table-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  
  .percentage-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .bar-wrapper {
      flex: 1;
      min-width: 0;
      
      @media (max-width: 768px) {
        min-width: 100px;
      }

      .bar-bg {
        width: 100%;
        height: 18px;
        background-color: #f5f7fa;
        border-radius: 9px;
        overflow: hidden;
        position: relative;

        .bar-fill {
          height: 100%;
          border-radius: 9px;
          transition: width 0.3s ease, background-color 0.3s ease;
          min-width: 2px;
        }
      }
    }

    .percentage-text {
      min-width: 70px;
      text-align: right;
      font-weight: 500;
      color: #606266;
      font-size: 14px;
      flex-shrink: 0;
    }
  }
  
  @media (max-width: 768px) {
    :deep(.el-table) {
      font-size: 12px;
      
      .el-table__header th,
      .el-table__body td {
        padding: 8px 5px;
      }
    }
  }
}

.compare-result-section {
  margin-top: 20px;
}

.compare-table-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  
  .compare-table-hint {
    margin-bottom: 10px;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 12px;
    color: #606266;
  }
  
  .compare-cell {
    padding: 8px 0;
    
    &.count-mode {
      .count-value {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        text-align: center;
      }
    }
    
    &.percentage-mode {
      .percentage-value {
        font-size: 16px;
        font-weight: 600;
        color: #409EFF;
        text-align: center;
        margin-bottom: 6px;
      }
      
      .percentage-bar-wrapper {
        margin: 6px 0;
        
        .percentage-bar-bg {
          width: 100%;
          height: 20px;
          background-color: #f5f7fa;
          border-radius: 10px;
          overflow: hidden;
          position: relative;
          
          .percentage-bar-fill {
            height: 100%;
            border-radius: 10px;
            transition: width 0.3s ease, background 0.3s ease;
            min-width: 2px;
          }
        }
      }
      
      .count-hint {
        font-size: 12px;
        color: #909399;
        text-align: center;
        margin-top: 4px;
      }
    }
  }
  
  .percentage-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .bar-wrapper {
      flex: 1;
      min-width: 0;
      
      @media (max-width: 768px) {
        min-width: 100px;
      }

      .bar-bg {
        width: 100%;
        height: 18px;
        background-color: #f5f7fa;
        border-radius: 9px;
        overflow: hidden;
        position: relative;

        .bar-fill {
          height: 100%;
          border-radius: 9px;
          transition: width 0.3s ease, background-color 0.3s ease;
          min-width: 2px;
        }
      }
    }

    .percentage-text {
      min-width: 70px;
      text-align: right;
      font-weight: 500;
      color: #606266;
      font-size: 14px;
      flex-shrink: 0;
    }
  }
  
  @media (max-width: 768px) {
    :deep(.el-table) {
      font-size: 12px;
      
      .el-table__header th,
      .el-table__body td {
        padding: 8px 5px;
      }
    }
  }
}

.cross-table-summary {
  display: flex;
  border: 1px solid #EBEEF5;
  border-top: none;
  background-color: #F5F7FA;
  font-size: 14px;
  
  .summary-label {
    width: 150px;
    padding: 12px;
    font-weight: 500;
    text-align: center;
    border-right: 1px solid #EBEEF5;
    background-color: #FAFAFA;
  }
  
  .summary-cell {
    flex: 1;
    width: 120px;
    padding: 12px;
    text-align: center;
    border-right: 1px solid #EBEEF5;
    
    &:last-child {
      border-right: none;
    }
    
    &.summary-total {
      background-color: #F0F9FF;
      font-weight: 500;
    }
  }
  
  @media (max-width: 768px) {
    font-size: 12px;
    
    .summary-label,
    .summary-cell {
      padding: 8px 5px;
    }
  }
}

.percentage-legend {
  padding: 10px;
  background-color: #F5F7FA;
  border-radius: 4px;
  
  @media (max-width: 768px) {
    font-size: 11px;
    padding: 8px;
    
    span {
      display: block;
      margin: 5px 0;
    }
  }
}
</style>
