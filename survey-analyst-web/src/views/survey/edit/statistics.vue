<template>
  <div class="statistics-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>统计分析</span>
          <div class="header-actions">
            <el-button-group>
              <el-button size="small" @click="handleShare" :icon="Share">分享</el-button>
              <el-button size="small" @click="handleExportPDF" :icon="Document">PDF</el-button>
              <el-button size="small" @click="handleExportExcel" :icon="Download">Excel</el-button>
              <el-button size="small" @click="showSettingsDialog = true" :icon="Tools">设置</el-button>
            </el-button-group>
            <el-button @click="handleRefresh" style="margin-left: 10px">刷新数据</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <el-tab-pane label="统计视图" name="chart">
          <div v-loading="loading" class="statistics-content">
            <!-- 问卷整体统计 -->
            <el-card v-if="surveyStatistics" class="survey-overview-card" style="margin-bottom: 20px">
              <template #header>
                <span>问卷整体统计</span>
              </template>
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-statistic title="总填写数" :value="surveyStatistics.totalResponses || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="已完成数" :value="surveyStatistics.completedResponses || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="草稿数" :value="surveyStatistics.draftResponses || 0" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="有效率" :value="surveyStatistics.validRate || 0" suffix="%" :precision="2" />
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
                    <el-tag size="small" style="margin-left: 10px">{{ getTypeLabel(item.type) }}</el-tag>
                  </div>
                  <!-- 图表类型切换（仅选择题显示） -->
                  <div v-if="isChoiceType(item.type)" class="chart-type-switcher">
                    <el-button-group size="small">
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
                <!-- 统计表格 -->
                <div v-if="displaySettings.showTable" class="stat-table-wrapper">
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
                          <div v-if="displaySettings.showBar" class="bar-wrapper">
                            <div class="bar-bg">
                              <div class="bar-fill" :style="{ width: row.percentage + '%' }"></div>
                            </div>
                          </div>
                          <span class="percentage-text">{{ row.percentage }}%</span>
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
                          <div v-if="displaySettings.showBar" class="bar-wrapper">
                            <div class="bar-bg">
                              <div class="bar-fill" :style="{ width: row.percentage + '%' }"></div>
                            </div>
                          </div>
                          <span class="percentage-text">{{ row.percentage }}%</span>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div class="table-footer">
                    <strong>本题有效填写人次：{{ getTotalCount(item.formItemId) }}</strong>
                  </div>
                </div>
                
                <!-- 图表 -->
                <div
                  v-if="getCurrentChartType(item.formItemId) !== 'table' && getChartOption(item.formItemId)"
                  class="chart-wrapper"
                >
                  <v-chart
                    :option="getChartOption(item.formItemId)"
                    style="height: 300px"
                    autoresize
                  />
                </div>
                <div v-else-if="getCurrentChartType(item.formItemId) !== 'table'" class="no-data">暂无数据</div>
              </div>
              
              <!-- 文本题统计 -->
              <div v-else-if="isTextType(item.type)" class="stat-text">
                <div class="text-stat-info">
                  <el-statistic title="有效答案数" :value="getTextStat(item.formItemId, 'count') || 0" />
                  <el-statistic title="总答案数" :value="getTextStat(item.formItemId, 'total') || 0" />
                </div>
                <!-- 词云图（仅 INPUT 和 TEXTAREA 显示） -->
                <div v-if="(item.type === 'INPUT' || item.type === 'TEXTAREA') && getWordCloudOption(item.formItemId)" class="wordcloud-wrapper">
                  <h4 style="margin-bottom: 15px">高频词分析</h4>
                  <v-chart
                    :option="getWordCloudOption(item.formItemId)"
                    style="height: 300px"
                    autoresize
                  />
                </div>
              </div>
              
              <!-- 签名组件统计 -->
              <div v-else-if="item.type === 'SIGN_PAD'" class="stat-signature">
                <div class="signature-stat-info">
                  <el-statistic title="有效签名数" :value="getSignatureStat(item.formItemId, 'count') || 0" />
                  <el-statistic title="总填写数" :value="getSignatureStat(item.formItemId, 'total') || 0" />
                </div>
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
                <div class="upload-stat-info">
                  <el-statistic title="有效上传数" :value="getUploadStat(item.formItemId, 'validUploads') || 0" />
                  <el-statistic title="总答案数" :value="getUploadStat(item.formItemId, 'totalAnswers') || 0" />
                  <el-statistic title="上传率" :value="getUploadStat(item.formItemId, 'uploadRate') || 0" suffix="%" :precision="2" />
                </div>
                
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
                                <div class="bar-fill" :style="{ width: row.percentage + '%' }"></div>
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
                <div class="upload-stat-info">
                  <el-statistic title="有效上传数" :value="getUploadStat(item.formItemId, 'validUploads') || 0" />
                  <el-statistic title="总答案数" :value="getUploadStat(item.formItemId, 'totalAnswers') || 0" />
                  <el-statistic title="上传率" :value="getUploadStat(item.formItemId, 'uploadRate') || 0" suffix="%" :precision="2" />
                </div>
                
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
                <div class="rating-stat-info">
                  <el-statistic title="平均分" :value="getRatingStat(item.formItemId, 'averageRating') || 0" :precision="2" />
                  <el-statistic title="最高分" :value="getRatingStat(item.formItemId, 'maxRating') || 0" />
                  <el-statistic title="最低分" :value="getRatingStat(item.formItemId, 'minRating') || 0" />
                  <el-statistic title="评分人数" :value="getRatingStat(item.formItemId, 'totalRatings') || 0" />
                </div>
                <!-- 评分分布图 -->
                <div v-if="getRatingChartOption(item.formItemId)" class="chart-wrapper">
                  <v-chart
                    :option="getRatingChartOption(item.formItemId)"
                    style="height: 300px"
                    autoresize
                  />
                </div>
              </div>
              
              <!-- 数字题统计 -->
              <div v-else-if="item.type === 'NUMBER'" class="stat-number">
                <div class="number-stat-info">
                  <el-statistic title="平均值" :value="getNumberStat(item.formItemId, 'average') || 0" :precision="2" />
                  <el-statistic title="最大值" :value="getNumberStat(item.formItemId, 'max') || 0" />
                  <el-statistic title="最小值" :value="getNumberStat(item.formItemId, 'min') || 0" />
                  <el-statistic title="有效数据数" :value="getNumberStat(item.formItemId, 'count') || 0" />
                </div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="分类统计" name="filter">
          <div v-loading="filterLoading" class="filter-statistics-content">
            <el-card>
              <template #header>
                <div class="filter-header">
                  <span>分类筛选</span>
                  <div>
                    <el-button size="small" type="primary" @click="handleFilterApply" :loading="filterLoading">
                      应用筛选
                    </el-button>
                    <el-button size="small" @click="handleFilterReset" style="margin-left: 10px">重置</el-button>
                  </div>
                </div>
              </template>
              
              <el-form :model="filterForm" label-width="120px">
                <el-form-item label="筛选条件1">
                  <el-select
                    v-model="filterForm.condition1.questionId"
                    placeholder="请选择题目"
                    style="width: 300px"
                    @change="handleFilterQuestion1Change"
                  >
                    <el-option
                      v-for="item in filterQuestionOptions"
                      :key="item.formItemId"
                      :label="item.label"
                      :value="item.formItemId"
                    />
                  </el-select>
                  <el-select
                    v-if="filterForm.condition1.questionId"
                    v-model="filterForm.condition1.optionValue"
                    placeholder="请选择选项"
                    style="width: 300px; margin-left: 10px"
                  >
                    <el-option
                      v-for="opt in getFilterOptions(filterForm.condition1.questionId)"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="筛选条件2（可选）">
                  <el-select
                    v-model="filterForm.condition2.questionId"
                    placeholder="请选择题目"
                    style="width: 300px"
                    clearable
                    @change="handleFilterQuestion2Change"
                  >
                    <el-option
                      v-for="item in filterQuestionOptions2"
                      :key="item.formItemId"
                      :label="item.label"
                      :value="item.formItemId"
                    />
                  </el-select>
                  <el-select
                    v-if="filterForm.condition2.questionId"
                    v-model="filterForm.condition2.optionValue"
                    placeholder="请选择选项"
                    style="width: 300px; margin-left: 10px"
                  >
                    <el-option
                      v-for="opt in getFilterOptions(filterForm.condition2.questionId)"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                    />
                  </el-select>
                </el-form-item>
              </el-form>
            </el-card>

            <!-- 筛选结果 -->
            <div v-if="filteredStatistics" class="filtered-statistics">
              <el-card style="margin-top: 20px">
                <template #header>
                  <span>筛选结果统计（共 {{ getFilteredTotalCount() }} 条数据）</span>
                </template>
                
                <!-- 显示筛选后的题目统计 -->
                <el-card
                  v-for="(item, index) in formItems"
                  :key="item.formItemId"
                  style="margin-bottom: 20px"
                >
                  <template #header>
                    <div class="question-header">
                      <div class="question-title">
                        <span class="question-number">第{{ index + 1 }}题：</span>
                        <span>{{ item.label }}</span>
                        <el-tag size="small" style="margin-left: 10px">{{ getTypeLabel(item.type) }}</el-tag>
                      </div>
                    </div>
                  </template>
                  
                  <!-- 选择题统计 -->
                  <div v-if="isChoiceType(item.type)" class="stat-chart">
                    <div v-if="displaySettings.showTable" class="stat-table-wrapper">
                      <el-table
                        :data="getFilteredTableData(item.formItemId)"
                        border
                        style="width: 100%; margin-bottom: 20px"
                      >
                        <el-table-column prop="optionLabel" label="选项" width="200" />
                        <el-table-column prop="count" label="小计" align="center" width="100" />
                        <el-table-column label="比例" min-width="300">
                          <template #default="{ row }">
                            <div class="percentage-cell">
                              <div v-if="displaySettings.showBar" class="bar-wrapper">
                                <div class="bar-bg">
                                  <div class="bar-fill" :style="{ width: row.percentage + '%' }"></div>
                                </div>
                              </div>
                              <span class="percentage-text">{{ row.percentage }}%</span>
                            </div>
                          </template>
                        </el-table-column>
                      </el-table>
                      <div class="table-footer">
                        <strong>本题有效填写人次：{{ getFilteredTotalCount(item.formItemId) }}</strong>
                      </div>
                    </div>
                    
                    <!-- 图表 -->
                    <div
                      v-if="getCurrentChartType(item.formItemId) !== 'table' && getFilteredChartOption(item.formItemId)"
                      class="chart-wrapper"
                    >
                      <v-chart
                        :option="getFilteredChartOption(item.formItemId)"
                        style="height: 300px"
                        autoresize
                      />
                    </div>
                  </div>
                  
                  <!-- 文本题统计 -->
                  <div v-else-if="isTextType(item.type)" class="stat-text">
                    <div class="text-stat-info">
                      <el-statistic title="有效答案数" :value="getFilteredTextStat(item.formItemId, 'count') || 0" />
                      <el-statistic title="总答案数" :value="getFilteredTextStat(item.formItemId, 'total') || 0" />
                    </div>
                  </div>
                  
                  <!-- 评分题统计 -->
                  <div v-else-if="isRatingType(item.type)" class="stat-rating">
                    <div class="rating-stat-info">
                      <el-statistic title="平均分" :value="getFilteredRatingStat(item.formItemId, 'averageRating') || 0" :precision="2" />
                      <el-statistic title="最高分" :value="getFilteredRatingStat(item.formItemId, 'maxRating') || 0" />
                      <el-statistic title="最低分" :value="getFilteredRatingStat(item.formItemId, 'minRating') || 0" />
                      <el-statistic title="评分人数" :value="getFilteredRatingStat(item.formItemId, 'totalRatings') || 0" />
                    </div>
                  </div>
                  
                  <!-- 数字题统计 -->
                  <div v-else-if="item.type === 'NUMBER'" class="stat-number">
                    <div class="number-stat-info">
                      <el-statistic title="平均值" :value="getFilteredNumberStat(item.formItemId, 'average') || 0" :precision="2" />
                      <el-statistic title="最大值" :value="getFilteredNumberStat(item.formItemId, 'max') || 0" />
                      <el-statistic title="最小值" :value="getFilteredNumberStat(item.formItemId, 'min') || 0" />
                      <el-statistic title="有效数据数" :value="getFilteredNumberStat(item.formItemId, 'count') || 0" />
                    </div>
                  </div>
                </el-card>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据分析" name="analysis">
          <div v-loading="analysisLoading" class="analysis-content">
            <!-- 分析类型切换 -->
            <el-tabs v-model="analysisType" type="card" style="margin-bottom: 20px">
              <el-tab-pane label="交叉分析" name="cross"></el-tab-pane>
              <el-tab-pane label="对比分析" name="compare"></el-tab-pane>
            </el-tabs>
            
            <!-- 交叉分析 -->
            <el-card v-if="analysisType === 'cross'" class="cross-analysis-card">
              <template #header>
                <span>交叉分析</span>
                <span style="font-size: 12px; color: #909399; margin-left: 10px">
                  分析不同 X 值下，Y 的分布差异
                </span>
              </template>
              <el-form :model="crossAnalysisForm" label-width="120px">
                <el-form-item label="题目1">
                  <el-select
                    v-model="crossAnalysisForm.formItemId1"
                    placeholder="请选择题目1"
                    style="width: 300px"
                    @change="handleCrossQuestion1Change"
                  >
                    <el-option
                      v-for="item in choiceFormItems"
                      :key="item.formItemId"
                      :label="item.label"
                      :value="item.formItemId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="题目2">
                  <el-select
                    v-model="crossAnalysisForm.formItemId2"
                    placeholder="请选择题目2"
                    style="width: 300px"
                    @change="handleCrossQuestion2Change"
                  >
                    <el-option
                      v-for="item in crossQuestion2Options"
                      :key="item.formItemId"
                      :label="item.label"
                      :value="item.formItemId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleCrossAnalyze" :loading="crossAnalyzing">
                    开始分析
                  </el-button>
                  <el-button type="info" @click="autoStartCrossAnalysis" :loading="crossAnalyzing" style="margin-left: 10px">
                    自动分析（前两个题目）
                  </el-button>
                </el-form-item>
              </el-form>

              <!-- 交叉分析结果 -->
              <div v-if="crossAnalysisResult" class="cross-result-section">
                <!-- 交叉表 -->
                <div class="cross-table-section">
                  <h3>交叉表</h3>
                  <el-table :data="crossTableData" border>
                    <el-table-column prop="rowLabel" label="" width="150" fixed="left" />
                    <el-table-column
                      v-for="col in crossTableColumns"
                      :key="col"
                      :prop="col"
                      :label="col"
                      width="120"
                    />
                  </el-table>
                </div>

                <!-- 热力图 -->
                <div class="heatmap-section">
                  <h3>热力图</h3>
                  <v-chart
                    v-if="heatmapChartOption"
                    :option="heatmapChartOption"
                    style="height: 400px"
                    autoresize
                  />
                </div>
              </div>
            </el-card>
            
            <!-- 对比分析 -->
            <el-card v-if="analysisType === 'compare'" class="compare-analysis-card">
              <template #header>
                <span>对比分析</span>
                <span style="font-size: 12px; color: #909399; margin-left: 10px">
                  对比不同群体在各题目上的分布差异
                </span>
              </template>
              <el-form :model="compareForm" label-width="120px">
                <el-form-item label="对比变量">
                  <el-select
                    v-model="compareForm.compareVariable"
                    placeholder="请选择对比变量（如性别、年龄段等）"
                    style="width: 400px"
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
                <el-form-item>
                  <el-button type="primary" @click="handleCompareAnalyze" :loading="compareAnalyzing">
                    开始对比分析
                  </el-button>
                </el-form-item>
              </el-form>
              
              <!-- 对比分析结果 -->
              <div v-if="compareAnalysisResult && compareAnalysisResult.length > 0" class="compare-result-section">
                <el-card
                  v-for="(compareItem, index) in compareAnalysisResult"
                  :key="index"
                  style="margin-bottom: 20px"
                >
                  <template #header>
                    <span>{{ compareItem.questionTitle }}</span>
                  </template>
                  <el-table :data="compareItem.compareData" border style="width: 100%">
                    <el-table-column prop="optionLabel" label="选项" width="200" />
                    <el-table-column
                      v-for="group in compareItem.groups"
                      :key="group"
                      :prop="group"
                      :label="group"
                      align="center"
                    >
                      <template #default="{ row }">
                        <div>
                          <div>{{ row[group]?.count || 0 }}</div>
                          <div style="color: #909399; font-size: 12px">
                            {{ row[group]?.percentage || 0 }}%
                          </div>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-card>
              </div>
              <el-empty v-else-if="analysisType === 'compare' && !compareAnalyzing" description="请选择对比变量并开始分析" />
            </el-card>

            <!-- 数据分析的其他图表（趋势、来源、设备等） -->
            <el-row v-if="analysisType === 'cross'" :gutter="20" style="margin-top: 20px">
              <!-- 填写趋势 -->
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>填写趋势</span>
                      <el-select v-model="timeRange" size="small" @change="loadTrendData">
                        <el-option label="最近7天" value="7d" />
                        <el-option label="最近30天" value="30d" />
                        <el-option label="全部" value="all" />
                      </el-select>
                    </div>
                  </template>
                  <v-chart
                    v-if="trendChartOption"
                    :option="trendChartOption"
                    style="height: 300px"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
              
              <!-- 设备类型分布 -->
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>设备类型分布</span>
                  </template>
                  <v-chart
                    v-if="deviceChartOption"
                    :option="deviceChartOption"
                    style="height: 300px"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px">
              <!-- 填写来源 -->
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>填写来源</span>
                  </template>
                  <v-chart
                    v-if="sourceChartOption"
                    :option="sourceChartOption"
                    style="height: 300px"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
              
              <!-- 填写时段 -->
              <el-col :span="12">
                <el-card>
                  <template #header>
                    <span>填写时段分布</span>
                  </template>
                  <v-chart
                    v-if="hourChartOption"
                    :option="hourChartOption"
                    style="height: 300px"
                    autoresize
                  />
                  <el-empty v-else description="暂无数据" />
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 显示设置对话框 -->
    <el-dialog
      v-model="showSettingsDialog"
      title="显示设置"
      width="500px"
    >
      <el-form :model="displaySettings" label-width="150px">
        <el-form-item label="数据表格">
          <el-checkbox v-model="displaySettings.showTable">显示表格</el-checkbox>
        </el-form-item>
        <el-form-item label="表格条形图">
          <el-checkbox v-model="displaySettings.showBar">显示条形图</el-checkbox>
        </el-form-item>
        <el-form-item label="平均分数据">
          <el-checkbox v-model="displaySettings.showAverage">显示平均分</el-checkbox>
        </el-form-item>
        <el-form-item label="隐藏选项">
          <el-checkbox v-model="displaySettings.hideEmpty">隐藏空选项</el-checkbox>
          <el-checkbox v-model="displaySettings.hideSkip" style="margin-left: 20px">隐藏跳过项</el-checkbox>
        </el-form-item>
        <el-form-item label="配色方案">
          <el-select v-model="currentColorSchemeId" @change="handleColorSchemeChange">
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
        <el-button @click="showSettingsDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSettings">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
import { Share, Document, Download, Tools } from '@element-plus/icons-vue'
import { formApi, analysisApi, questionApi, statisticsApi, exportApi } from '@/api'
import dayjs from 'dayjs'
import { generateChartOption } from '@/utils/chartConfig'
import { colorSchemes, loadColorScheme, saveColorScheme } from '@/utils/colorSchemes'
import { loadDisplaySettings, saveDisplaySettings } from '@/utils/displaySettings'
import { getImageUrl } from '@/utils/image'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

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
const timeRange = ref('30d')
const trendChartOption = ref(null)
const deviceChartOption = ref(null)
const sourceChartOption = ref(null)
const hourChartOption = ref(null)

// 显示设置
const showSettingsDialog = ref(false)
const displaySettings = reactive(loadDisplaySettings())
const currentColorScheme = ref(loadColorScheme())
const currentColorSchemeId = ref(currentColorScheme.value.id)

// 图表类型配置
const chartTypes = [
  { value: 'pie', label: '饼状' },
  { value: 'ring', label: '圆环' },
  { value: 'bar', label: '柱状' },
  { value: 'line', label: '折线' },
  { value: 'horizontalBar', label: '条形' },
  { value: 'table', label: '表格' }
]

// 每个题目的图表类型（存储在localStorage，用于响应式更新）
const chartTypeMap = ref({})

// 分类统计相关
const filterLoading = ref(false)
const filterForm = reactive({
  condition1: {
    questionId: null,
    optionValue: null
  },
  condition2: {
    questionId: null,
    optionValue: null
  }
})
const filteredStatistics = ref(null)

// 分类统计题目选项（只显示选择题）
const filterQuestionOptions = computed(() => {
  return formItems.value.filter(item => isChoiceType(item.type))
})

// 分类统计题目2选项（排除题目1）
const filterQuestionOptions2 = computed(() => {
  if (!filterForm.condition1.questionId) {
    return filterQuestionOptions.value
  }
  return filterQuestionOptions.value.filter(item => item.formItemId !== filterForm.condition1.questionId)
})

// 数据分析相关
const analysisType = ref('cross') // cross: 交叉分析, compare: 对比分析

// 交叉分析相关
const crossAnalysisForm = reactive({
  formItemId1: null,
  formItemId2: null
})
const crossAnalyzing = ref(false)
const crossAnalysisResult = ref(null)
const crossTableData = ref([])
const crossTableColumns = ref([])
const heatmapChartOption = ref(null)

// 对比分析相关
const compareForm = reactive({
  compareVariable: null
})
const compareAnalyzing = ref(false)
const compareAnalysisResult = ref(null)

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
      }
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error('加载统计数据错误:', error)
    // 降级方案：使用旧的逐个请求方式
    await loadStatisticsFallback()
  } finally {
    loading.value = false
    // 自动执行交叉分析：如果有至少2个选择题，自动选择前两个进行分析
    autoStartCrossAnalysis()
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

// 自动开始交叉分析
const autoStartCrossAnalysis = () => {
  // 检查是否有选择题（至少2个）
  const choiceItems = formItems.value.filter(item => isChoiceType(item.type))
  if (choiceItems.length >= 2) {
    // 自动选择前两个选择题
    crossAnalysisForm.formItemId1 = choiceItems[0].formItemId
    crossAnalysisForm.formItemId2 = choiceItems[1].formItemId
    // 自动执行分析（不显示提示信息）
    handleCrossAnalyze(false)
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
  if (!stat || !stat.optionStats) return []
  
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item) return []
  
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

// 交叉分析题目2的选项（排除题目1）
const crossQuestion2Options = computed(() => {
  if (!crossAnalysisForm.formItemId1) {
    return choiceFormItems.value
  }
  return choiceFormItems.value.filter(item => item.formItemId !== crossAnalysisForm.formItemId1)
})

// 处理交叉分析题目1变化
const handleCrossQuestion1Change = () => {
  if (crossAnalysisForm.formItemId2 === crossAnalysisForm.formItemId1) {
    crossAnalysisForm.formItemId2 = null
  }
  crossAnalysisResult.value = null
}

// 处理交叉分析题目2变化
const handleCrossQuestion2Change = () => {
  crossAnalysisResult.value = null
}

// 执行交叉分析
const handleCrossAnalyze = async (showWarning = true) => {
  if (!crossAnalysisForm.formItemId1 || !crossAnalysisForm.formItemId2) {
    if (showWarning) {
    ElMessage.warning('请选择两个题目')
    }
    return
  }

  if (!surveyId.value) {
    if (showWarning) {
    ElMessage.warning('问卷ID不存在')
    }
    return
  }

  // 验证表单项是否存在
  const item1 = formItems.value.find(item => item.formItemId === crossAnalysisForm.formItemId1)
  const item2 = formItems.value.find(item => item.formItemId === crossAnalysisForm.formItemId2)

  if (!item1) {
    if (showWarning) {
    ElMessage.warning('题目1不存在，无法进行交叉分析')
    }
    return
  }

  if (!item2) {
    if (showWarning) {
    ElMessage.warning('题目2不存在，无法进行交叉分析')
    }
    return
  }

  crossAnalyzing.value = true
  try {
    const res = await analysisApi.crossAnalysis({
      surveyId: surveyId.value,
      formItemId1: crossAnalysisForm.formItemId1,
      formItemId2: crossAnalysisForm.formItemId2
    })

    if (res.code === 200) {
      crossAnalysisResult.value = res.data
      buildCrossTable(res.data.crossTable)
      buildHeatmap(res.data.crossTable)
    }
  } catch (error) {
    ElMessage.error('交叉分析失败')
    console.error('交叉分析错误:', error)
  } finally {
    crossAnalyzing.value = false
  }
}

// 构建交叉表
const buildCrossTable = (crossTable) => {
  if (!crossTable) return

  const rows = Object.keys(crossTable)
  const cols = new Set()
  rows.forEach(row => {
    Object.keys(crossTable[row]).forEach(col => cols.add(col))
  })

  crossTableColumns.value = Array.from(cols)

  crossTableData.value = rows.map(row => {
    const rowData = { rowLabel: row }
    crossTableColumns.value.forEach(col => {
      rowData[col] = crossTable[row][col] || 0
    })
    return rowData
  })
}

// 构建热力图
const buildHeatmap = (crossTable) => {
  if (!crossTable) return

  const rows = Object.keys(crossTable)
  const cols = new Set()
  rows.forEach(row => {
    Object.keys(crossTable[row]).forEach(col => cols.add(col))
  })

  const xAxisData = Array.from(cols)
  const yAxisData = rows
  const data = []

  rows.forEach((row, rowIndex) => {
    xAxisData.forEach((col, colIndex) => {
      const value = crossTable[row][col] || 0
      data.push([colIndex, rowIndex, value])
    })
  })

  heatmapChartOption.value = {
    title: {
      text: '交叉分析热力图',
      left: 'center'
    },
    tooltip: {
      position: 'top',
      formatter: function(params) {
        return `${yAxisData[params.data[1]]} × ${xAxisData[params.data[0]]}<br/>数量: ${params.data[2]}`
      }
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(...data.map(d => d[2])),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%'
    },
    series: [{
      name: '交叉分析',
      type: 'heatmap',
      data: data,
      label: {
        show: true
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
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
      
    // 3. 使用后端API获取填写来源
    const sourceRes = await statisticsApi.getResponseSource(surveyId.value)
    if (sourceRes.code === 200 && sourceRes.data) {
      const sourceCount = sourceRes.data.sourceCount || {}
      const sourceData = Object.entries(sourceCount).map(([name, value]) => ({
        name: name === 'direct' ? '直接访问' : name === 'share' ? '分享链接' : name,
        value: value
      }))
      sourceChartOption.value = {
        title: { text: '填写来源', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '填写来源',
          type: 'pie',
          radius: '60%',
          data: sourceData
        }]
      }
    }
    
    // 4. 填写时段分布（需要从Response数据计算，暂时保留前端计算）
    // 这里可以从后端获取Response列表来计算
    const dataRes = await formApi.getFormDataList(formKey.value, {
      pageNum: 1,
      pageSize: 1000
    })
    if (dataRes.code === 200 && dataRes.data) {
      const dataList = dataRes.data.records || []
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

// 刷新数据
const handleRefresh = () => {
  loadStatistics()
  if (activeTab.value === 'analysis') {
    loadTrendData()
  }
}

// 监听标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'analysis' && !trendChartOption.value) {
    loadTrendData()
  }
}

// 处理配色方案变化
const handleColorSchemeChange = (schemeId) => {
  const scheme = colorSchemes.find(s => s.id === schemeId)
  if (scheme) {
    currentColorScheme.value = scheme
    saveColorScheme(schemeId)
  }
}

// 保存显示设置
const handleSaveSettings = () => {
  saveDisplaySettings(displaySettings)
  saveColorScheme(currentColorSchemeId.value)
  showSettingsDialog.value = false
  ElMessage.success('设置已保存')
}

// 分享报告
const handleShare = async () => {
  try {
    // TODO: 实现分享功能
    ElMessage.info('分享功能开发中...')
  } catch (error) {
    ElMessage.error('分享失败')
  }
}

// 导出PDF
const handleExportPDF = async () => {
  try {
    ElMessage.info('正在生成PDF...')
    const element = document.querySelector('.statistics-container')
    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff'
    })
    
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    
    const imgWidth = 210
    const pageHeight = 297
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight
    let position = 0
    
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight
    
    while (heightLeft > 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }
    
    const surveyTitle = surveyStatistics.value?.title || '统计报告'
    pdf.save(`${surveyTitle}_${dayjs().format('YYYY-MM-DD')}.pdf`)
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('PDF导出失败:', error)
    ElMessage.error('PDF导出失败')
  }
}

// 导出Excel
const handleExportExcel = async () => {
  try {
    if (!surveyId.value) {
      ElMessage.warning('问卷ID不存在')
      return
    }

    ElMessage.info('正在生成Excel...')
    
    // 使用后端接口导出统计数据
    await exportApi.exportStatistics(surveyId.value)
    
    ElMessage.success('Excel导出成功')
  } catch (error) {
    console.error('Excel导出失败:', error)
    ElMessage.error('Excel导出失败')
  }
}

// 获取筛选题目的选项列表
const getFilterOptions = (formItemId) => {
  const item = formItems.value.find(i => i.formItemId === formItemId)
  if (!item || !item.scheme?.config?.options) {
    return []
  }
  return item.scheme.config.options.map(opt => ({
    label: opt.label,
    value: opt.value
  }))
}

// 处理筛选题目1变化
const handleFilterQuestion1Change = () => {
  filterForm.condition1.optionValue = null
  // 如果题目2和题目1相同，清空题目2
  if (filterForm.condition2.questionId === filterForm.condition1.questionId) {
    filterForm.condition2.questionId = null
    filterForm.condition2.optionValue = null
  }
}

// 处理筛选题目2变化
const handleFilterQuestion2Change = () => {
  filterForm.condition2.optionValue = null
}

// 应用筛选
const handleFilterApply = async () => {
  if (!filterForm.condition1.questionId || !filterForm.condition1.optionValue) {
    ElMessage.warning('请至少选择筛选条件1')
    return
  }

  if (!surveyId.value) {
    ElMessage.warning('问卷ID不存在')
    return
  }

  filterLoading.value = true
  try {
    const filters = []
    
    // 添加条件1
    filters.push({
      formItemId: filterForm.condition1.questionId,
      optionValue: filterForm.condition1.optionValue
    })
    
    // 添加条件2（如果存在）
    if (filterForm.condition2.questionId && filterForm.condition2.optionValue) {
      filters.push({
        formItemId: filterForm.condition2.questionId,
        optionValue: filterForm.condition2.optionValue
      })
    }

    // 调用后端筛选接口
    const res = await statisticsApi.getFilteredStatistics(surveyId.value, filters)
    if (res.code === 200) {
      filteredStatistics.value = res.data
      ElMessage.success('筛选成功')
    }
  } catch (error) {
    console.error('筛选失败:', error)
    ElMessage.error('筛选失败')
  } finally {
    filterLoading.value = false
  }
}

// 重置筛选
const handleFilterReset = () => {
  filterForm.condition1.questionId = null
  filterForm.condition1.optionValue = null
  filterForm.condition2.questionId = null
  filterForm.condition2.optionValue = null
  filteredStatistics.value = null
}

// 获取筛选后的表格数据
const getFilteredTableData = (formItemId) => {
  if (!filteredStatistics.value || !filteredStatistics.value.questionStatistics) {
    return []
  }
  
  const stat = filteredStatistics.value.questionStatistics[formItemId]
  if (!stat || !stat.optionStats) return []
  
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
  
  return options.map(opt => {
    const count = opt.count || 0
    const percentage = total > 0 ? ((count / total) * 100).toFixed(2) : 0
    return {
      optionLabel: opt.optionLabel || opt.optionValue,
      count: count,
      percentage: parseFloat(percentage)
    }
  })
}

// 获取筛选后的总人数
const getFilteredTotalCount = (formItemId = null) => {
  if (!filteredStatistics.value) return 0
  if (formItemId) {
    const stat = filteredStatistics.value.questionStatistics?.[formItemId]
    return stat?.totalCount || 0
  }
  return filteredStatistics.value.surveyStatistics?.totalResponses || 0
}

// 获取筛选后的文本统计
const getFilteredTextStat = (formItemId, key) => {
  if (!filteredStatistics.value || !filteredStatistics.value.questionStatistics) {
    return 0
  }
  const stat = filteredStatistics.value.questionStatistics[formItemId]
  if (!stat) return 0
  if (key === 'count') return stat.validAnswers || 0
  if (key === 'total') return stat.totalAnswers || 0
  return stat[key] || 0
}

// 获取筛选后的评分统计
const getFilteredRatingStat = (formItemId, key) => {
  if (!filteredStatistics.value || !filteredStatistics.value.questionStatistics) {
    return 0
  }
  const stat = filteredStatistics.value.questionStatistics[formItemId]
  return stat ? stat[key] : 0
}

// 获取筛选后的数字统计
const getFilteredNumberStat = (formItemId, key) => {
  if (!filteredStatistics.value || !filteredStatistics.value.questionStatistics) {
    return 0
  }
  const stat = filteredStatistics.value.questionStatistics[formItemId]
  if (!stat) return 0
  if (key === 'average') return stat.average || 0
  return stat[key] || 0
}

// 获取筛选后的图表配置
const getFilteredChartOption = (formItemId) => {
  if (!filteredStatistics.value || !filteredStatistics.value.questionStatistics) {
    return null
  }
  const stat = filteredStatistics.value.questionStatistics[formItemId]
  if (!stat || !stat.optionStats) return null
  
  const chartType = getCurrentChartType(formItemId)
  if (chartType === 'table') return null
  
  return generateChartOption(chartType, stat, currentColorScheme.value)
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
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  /* 自定义滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
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
}

.statistics-content {
  min-height: 400px;
}

.empty-tip {
  padding: 40px;
  text-align: center;
}

.question-stat-card {
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
    }

    .chart-type-switcher {
      margin-left: 20px;
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

    .percentage-cell {
      display: flex;
      align-items: center;
      gap: 10px;

      .bar-wrapper {
        flex: 1;

        .bar-bg {
          width: 100%;
          height: 20px;
          background-color: #f0f0f0;
          border-radius: 10px;
          overflow: hidden;

          .bar-fill {
            height: 100%;
            background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%);
            transition: width 0.3s;
          }
        }
      }

      .percentage-text {
        min-width: 60px;
        text-align: right;
        font-weight: 500;
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
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
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

    h4 {
      margin: 0 0 15px 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }

    .image-upload-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 15px;

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
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }
  
  .chart-wrapper {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
  }
}

.stat-number {
  .number-stat-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
  }
}

.survey-overview-card {
  .el-statistic {
    text-align: center;
  }
}

.analysis-content {
  padding: 20px;
  min-height: 400px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  overflow-x: hidden;
}

.filter-statistics-content {
  padding: 20px;
  min-height: 400px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filtered-statistics {
  margin-top: 20px;
}

.cross-analysis-card,
.compare-analysis-card {
  margin-bottom: 20px;
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
  }

  .option-label {
    flex: 1;
    word-break: break-word;
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
}

.cross-table-section,
.heatmap-section {
  margin-bottom: 30px;
}

.cross-table-section h3,
.heatmap-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}
</style>
