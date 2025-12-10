<template>
  <div class="setting-container">
    <el-tabs tab-position="left" class="setting-tabs">
      <el-tab-pane label="提交设置">
        <SubmitSetting />
      </el-tab-pane>
      <el-tab-pane label="通知设置">
        <NotifySetting />
      </el-tab-pane>
      <el-tab-pane label="分享设置">
        <ShareSetting />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import SubmitSetting from './setting/SubmitSetting.vue'
import NotifySetting from './setting/NotifySetting.vue'
import ShareSetting from './setting/ShareSetting.vue'
</script>

<style lang="scss" scoped>
.setting-container {
  width: 100%;
  background-color: #f5f5f5;
  display: flex;
  justify-content: center;
  padding: 20px;
  min-height: calc(100vh - 140px);

  .setting-tabs {
    border-radius: 8px;
    width: 900px;
    min-height: 500px;
    background: #fff;
  }
}

:deep(.el-tabs--left .el-tabs__nav-wrap.is-left::after) {
  width: 0;
}

:deep(.el-tabs--left .el-tabs__nav.is-left) {
  background-color: #ffffff !important;
}

:deep(.el-tabs__item) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 20px;
}

:deep(.el-form-item__label) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: keep-all;
}

@media (max-width: 768px) {
  .setting-container {
    padding: 10px;
  }

  .setting-tabs {
    width: 100%;
  }

  :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 13px;
    white-space: nowrap;
  }

  :deep(.el-tabs__nav-wrap) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }

  :deep(.el-tabs__nav-wrap)::-webkit-scrollbar {
    display: none;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
    width: 90px !important;
    padding-right: 8px;
  }

  :deep(.el-tabs__content) {
    padding: 15px 10px;
  }
}
</style>
