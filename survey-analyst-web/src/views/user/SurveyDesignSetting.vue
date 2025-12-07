<template>
  <div class="setting-container">
    <el-tabs tab-position="left" class="setting-tabs">
      <el-tab-pane label="提交设置">
        <SubmitSetting />
      </el-tab-pane>
      <el-tab-pane label="回收设置">
        <WriteSetting />
      </el-tab-pane>
      <el-tab-pane label="通知设置">
        <NotifySetting />
      </el-tab-pane>
      <el-tab-pane label="分享设置">
        <ShareSetting />
      </el-tab-pane>
      <el-tab-pane label="数据推送">
        <DataPush />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import SubmitSetting from './setting/SubmitSetting.vue'
import WriteSetting from './setting/WriteSetting.vue'
import NotifySetting from './setting/NotifySetting.vue'
import ShareSetting from './setting/ShareSetting.vue'
import DataPush from './setting/DataPush.vue'
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
</style>
