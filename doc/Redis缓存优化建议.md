# Redis 缓存优化建议

## 一、已实现的 Redis 缓存

### 1. 统计数据缓存 ✅
- **位置**: `StatisticsServiceImpl`
- **缓存内容**: 
  - 问卷统计概览 (`SURVEY_OVERVIEW`)
  - 题目统计 (`QUESTION_STAT`)
  - 填写趋势 (`RESPONSE_TREND`)
  - 填写来源 (`RESPONSE_SOURCE`)
  - 设备统计 (`DEVICE_STAT`)
- **缓存键格式**: `statistics:survey:{surveyId}:{statType}`
- **过期时间**: 1小时 (3600秒)

## 二、建议使用 Redis 缓存的功能

### 2.1 表单配置缓存（高优先级）⭐

**位置**: `FormConfigServiceImpl`

**方法**:
- `getByFormKey(String formKey)` - 通过 formKey 查询表单配置
- `getBySurveyId(Long surveyId)` - 通过 surveyId 查询表单配置

**调用频率**: 非常高，几乎每个表单相关操作都会调用

**缓存策略**:
```java
// 缓存键格式
form:config:formKey:{formKey}
form:config:surveyId:{surveyId}

// 过期时间: 30分钟
// 更新时清除相关缓存
```

**收益**: 减少数据库查询，提升表单加载速度

---

### 2.2 表单项缓存（高优先级）⭐

**位置**: `FormItemServiceImpl`

**方法**:
- `getByFormKey(String formKey)` - 获取表单的所有表单项

**调用频率**: 非常高，每次加载表单都需要查询表单项列表

**缓存策略**:
```java
// 缓存键格式
form:items:{formKey}

// 过期时间: 30分钟
// 更新表单项时清除缓存
```

**收益**: 减少数据库查询，提升表单渲染速度

---

### 2.3 用户信息缓存（中优先级）

**位置**: `UserServiceImpl`

**方法**:
- `getCurrentUser()` - 获取当前登录用户信息

**调用频率**: 高，几乎每个需要用户信息的接口都会调用

**缓存策略**:
```java
// 缓存键格式
user:current:{account}

// 过期时间: 15分钟
// 用户信息更新时清除缓存
```

**收益**: 减少数据库查询，提升接口响应速度

**注意事项**: 
- 用户信息更新时需要清除缓存
- 密码等敏感信息不应缓存

---

### 2.4 表单模板缓存（中优先级）

**位置**: `FormTemplateServiceImpl`

**方法**:
- `getByFormKey(String formKey)` - 通过 formKey 查询模板
- 模板列表查询 - 分页查询模板列表

**调用频率**: 中等，模板查看和选择时调用

**缓存策略**:
```java
// 缓存键格式
template:formKey:{formKey}
template:list:{page}:{size}:{name}:{type}:{isPublic}

// 过期时间: 1小时
// 模板更新时清除相关缓存
```

**收益**: 减少数据库查询，提升模板加载速度

---

### 2.5 表单设置/主题/逻辑缓存（低优先级）

**位置**: 
- `FormSettingServiceImpl.getBySurveyId()`
- `FormThemeServiceImpl.getBySurveyId()`
- `FormLogicServiceImpl.getBySurveyId()`

**调用频率**: 中等，表单配置加载时调用

**缓存策略**:
```java
// 缓存键格式
form:setting:{surveyId}
form:theme:{surveyId}
form:logic:{surveyId}

// 过期时间: 30分钟
// 更新时清除缓存
```

**收益**: 减少数据库查询

---

### 2.6 JWT Token 黑名单（可选）

**位置**: `JwtAuthenticationFilter` 或 `JwtUtils`

**功能**: 存储已注销的 Token，用于实现 Token 黑名单机制

**缓存策略**:
```java
// 缓存键格式
jwt:blacklist:{token}

// 过期时间: 与 Token 过期时间相同
```

**收益**: 支持 Token 注销功能，提升安全性

---

### 2.7 表单访问频率限制（可选）

**位置**: 表单提交接口

**功能**: 防止表单重复提交、限制访问频率

**缓存策略**:
```java
// 缓存键格式
form:submit:limit:{formKey}:{ip}
form:submit:limit:{formKey}:{userId}

// 过期时间: 根据业务需求（如 1分钟）
```

**收益**: 防止恶意提交，保护系统资源

---

## 三、实现建议

### 3.1 缓存更新策略

1. **Cache-Aside 模式**（推荐）
   - 读取：先查缓存，缓存未命中则查数据库并写入缓存
   - 更新：先更新数据库，再删除缓存

2. **缓存失效时机**
   - 数据更新时立即清除相关缓存
   - 使用通配符批量删除（如 `form:config:*`）

### 3.2 缓存键命名规范

```
{模块}:{实体}:{标识}:{值}
例如:
- form:config:formKey:abc123
- form:items:abc123
- user:current:admin
- statistics:survey:1:SURVEY_OVERVIEW
```

### 3.3 过期时间建议

- **高频查询数据**: 15-30分钟（如用户信息、表单配置）
- **中频查询数据**: 30分钟-1小时（如表单项、模板）
- **低频查询数据**: 1-2小时（如统计数据）
- **实时性要求高**: 5-15分钟

### 3.4 注意事项

1. **缓存穿透**: 对于不存在的 key，也要缓存（缓存 null 值，过期时间短）
2. **缓存雪崩**: 过期时间添加随机值，避免同时过期
3. **缓存击穿**: 使用分布式锁，防止热点数据过期时大量请求打到数据库
4. **数据一致性**: 更新数据时及时清除缓存
5. **内存管理**: 合理设置过期时间，避免内存溢出

## 四、实施优先级

1. **第一阶段**（立即实施）:
   - ✅ 统计数据缓存（已完成）
   - ⭐ 表单配置缓存
   - ⭐ 表单项缓存

2. **第二阶段**（近期实施）:
   - 用户信息缓存
   - 表单模板缓存

3. **第三阶段**（可选实施）:
   - 表单设置/主题/逻辑缓存
   - JWT Token 黑名单
   - 访问频率限制

## 五、性能提升预期

- **数据库查询减少**: 预计减少 60-80% 的重复查询
- **接口响应时间**: 预计提升 30-50%
- **系统并发能力**: 预计提升 2-3 倍

