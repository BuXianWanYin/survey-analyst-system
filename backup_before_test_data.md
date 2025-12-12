# 数据库备份记录

## 备份时间
2025-01-XX（添加测试数据前）

**注意**: 此备份记录用于恢复数据库到添加测试数据前的状态。

## 备份信息

### 问卷信息
- **问卷ID**: 17
- **问卷标题**: 大学生暑假工调查问卷
- **问卷状态**: PUBLISHED（已发布）
- **form_key**: form_1765501213196_fefb4f8c
- **测试用户ID**: 3（账号：user2，用户名：测试2）

### 数据统计（备份前）
- **form_data 最大ID**: 11
- **response 最大ID**: 14
- **form_data 记录数**: 0（针对该问卷）
- **response 记录数**: 0（针对该问卷）

### 数据统计（添加测试数据后）
- **form_data 记录数**: 65条
- **form_data ID范围**: 12-76（新增的测试数据）
- **response 记录数**: 65条
- **response ID范围**: 15-79（新增的测试数据）
- **设备类型分布**:
  - PC: 33条
  - MOBILE: 32条
- **浏览器分布**:
  - Chrome: 26条
  - Safari: 13条
  - Firefox: 13条
  - Edge: 13条

## 恢复方法

如果需要恢复数据库到添加测试数据前的状态，请执行以下SQL：

```sql
-- 删除测试数据（form_data）
DELETE FROM form_data WHERE form_key = 'form_1765501213196_fefb4f8c' AND id >= 12;

-- 删除测试数据（response）
DELETE FROM response WHERE survey_id = 17 AND id >= 15;
```

## 测试数据说明

测试数据包含：
- 不同的选项组合（覆盖所有题目和选项）
- 不同的设备类型（PC和MOBILE）
- 不同的浏览器（Chrome、Safari、Firefox、Edge）
- 不同的IP地址（192.168.1.101 - 192.168.1.165）
- 不同的填写时间（2025-01-15 至 2025-01-22）
- 不同的填写时长（190-330秒）
- 不同的性别、是否参加暑假工等选项组合

