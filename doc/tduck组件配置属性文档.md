# Tduck 基础组件配置属性文档

本文档详细整理了 Tduck 表单系统中所有基础组件的可配置属性，基于 Tduck 源代码分析整理。

## 一、通用基础属性

所有组件都支持以下基础属性：

| 属性名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| `label` | String | 组件标题/标签 | - |
| `formItemId` | String | 表单项唯一标识 | - |
| `vModel` | String | 表单字段名（通常等于formItemId） | - |
| `span` | Number | 栅格宽度（1-24，24为100%宽度） | 24 |
| `required` | Boolean | 是否必填 | false |
| `showLabel` | Boolean | 是否显示标题 | true |
| `displayType` | Boolean | 是否为展示类型组件 | false |
| `hideType` | Boolean | 是否为隐藏类型组件 | false |
| `specialType` | Boolean | 是否为特殊类型组件 | false |
| `defaultValue` | String/Object | 默认值 | - |
| `sort` | Number | 排序号 | 0 |

---

## 二、输入类组件

### 2.1 单行文本 (INPUT)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `placeholder` | String | 占位提示文字 | "请输入单行文本" | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `readonly` | Boolean | 是否只读 | false | 基础属性 |
| `clearable` | Boolean | 是否可清空 | true | scheme.config |
| `prefix-icon` | String | 前缀图标 | "" | scheme.config |
| `suffix-icon` | String | 后缀图标 | "" | scheme.config |
| `show-word-limit` | Boolean | 是否显示字数统计 | false | scheme.config |
| `maxLength` | Number | 最大输入字符数 | - | scheme.config |
| `minLength` | Number | 最小输入字符数 | - | scheme.config |
| `regList` | Array | 正则表达式验证规则 | [] | 独立字段 |
| `dataType` | Object | 数据类型验证 | {type: "", message: ""} | scheme.config |
| `style.width` | String | 组件宽度样式 | "100%" | scheme.style |
| `notRepeat` | Boolean | 不允许重复值 | false | scheme.config（InputResultStruct） |
| `append` | String | 输入框后置内容 | "" | scheme.config |
| `prepend` | String | 输入框前置内容 | "" | scheme.config |

**正则表达式规则格式 (regList):**
```json
[
  {
    "pattern": "正则表达式",
    "message": "验证失败提示信息"
  }
]
```

**示例配置:**
```json
{
  "placeholder": "请输入姓名",
  "required": true,
  "disabled": false,
  "readonly": false,
  "clearable": true,
  "maxLength": 20,
  "show-word-limit": true,
  "regList": [
    {
      "pattern": "^[\\u4e00-\\u9fa5]+$",
      "message": "只能输入中文"
    }
  ]
}
```

---

### 2.2 多行文本 (TEXTAREA)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `placeholder` | String | 占位提示文字 | "请输入多行文本" | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `readonly` | Boolean | 是否只读 | false | 基础属性 |
| `clearable` | Boolean | 是否可清空 | true | scheme.config |
| `rows` | Number | 文本域行数 | 4 | scheme.config |
| `maxLength` | Number | 最大输入字符数 | - | scheme.config |
| `minLength` | Number | 最小输入字符数 | - | scheme.config |
| `show-word-limit` | Boolean | 是否显示字数统计 | false | scheme.config |
| `regList` | Array | 正则表达式验证规则 | [] | 独立字段 |
| `dataType` | Object | 数据类型验证 | {type: "", message: ""} | scheme.config |

**示例配置:**
```json
{
  "placeholder": "请输入详细描述",
  "required": true,
  "rows": 5,
  "maxLength": 500,
  "show-word-limit": true
}
```

---

### 2.3 数字组件 (NUMBER)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `placeholder` | String | 占位提示文字 | "请输入数字" | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `readonly` | Boolean | 是否只读 | false | 基础属性 |
| `min` | Number | 最小值 | - | scheme.config |
| `max` | Number | 最大值 | - | scheme.config |
| `step` | Number | 步长 | 1 | scheme.config |
| `precision` | Number | 精度（小数位数） | - | scheme.config |
| `controls-position` | String | 控制按钮位置（right/left） | "right" | scheme.config |
| `regList` | Array | 正则表达式验证规则 | [] | 独立字段 |

**示例配置:**
```json
{
  "placeholder": "请输入年龄",
  "required": true,
  "min": 0,
  "max": 150,
  "step": 1,
  "precision": 0
}
```

---

### 2.4 日期时间 (DATE)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `placeholder` | String | 占位提示文字 | "请选择日期" | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `readonly` | Boolean | 是否只读 | false | 基础属性 |
| `format` | String | 显示格式 | "YYYY-MM-DD" | scheme.config |
| `value-format` | String | 绑定值格式 | "YYYY-MM-DD" | scheme.config |
| `type` | String | 日期类型（date/datetime/datetimerange/daterange） | "date" | scheme.config |
| `picker-options` | Object | 日期选择器选项 | {} | scheme.config |
| `clearable` | Boolean | 是否可清空 | true | scheme.config |
| `regList` | Array | 正则表达式验证规则 | [] | 独立字段 |

**日期类型说明:**
- `date`: 日期选择
- `datetime`: 日期时间选择
- `daterange`: 日期范围选择
- `datetimerange`: 日期时间范围选择

**示例配置:**
```json
{
  "placeholder": "请选择日期",
  "required": true,
  "type": "date",
  "format": "YYYY-MM-DD",
  "value-format": "YYYY-MM-DD",
  "clearable": true
}
```

---

## 三、选择类组件

### 3.1 单选框组 (RADIO)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `options` | Array | 选项列表 | [] | scheme.config |
| `option.label` | String | 选项文本 | - | options数组项 |
| `option.value` | String/Number | 选项值 | - | options数组项 |
| `border` | Boolean | 是否显示边框 | false | scheme.config |
| `button` | Boolean | 是否按钮样式 | false | scheme.config |
| `size` | String | 尺寸（medium/small/mini） | "medium" | scheme.config |

**选项格式:**
```json
{
  "options": [
    {"label": "选项1", "value": "option1"},
    {"label": "选项2", "value": "option2"}
  ]
}
```

---

### 3.2 多选框组 (CHECKBOX)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `options` | Array | 选项列表 | [] | scheme.config |
| `option.label` | String | 选项文本 | - | options数组项 |
| `option.value` | String/Number | 选项值 | - | options数组项 |
| `min` | Number | 最少选择数量 | - | scheme.config |
| `max` | Number | 最多选择数量 | - | scheme.config |
| `border` | Boolean | 是否显示边框 | false | scheme.config |
| `button` | Boolean | 是否按钮样式 | false | scheme.config |
| `size` | String | 尺寸（medium/small/mini） | "medium" | scheme.config |

---

### 3.3 下拉选择 (SELECT)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `placeholder` | String | 占位提示文字 | "请选择" | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `options` | Array | 选项列表 | [] | scheme.config |
| `option.label` | String | 选项文本 | - | options数组项 |
| `option.value` | String/Number | 选项值 | - | options数组项 |
| `clearable` | Boolean | 是否可清空 | true | scheme.config |
| `multiple` | Boolean | 是否多选 | false | scheme.config |
| `filterable` | Boolean | 是否可搜索 | false | scheme.config |
| `size` | String | 尺寸（medium/small/mini） | "medium" | scheme.config |

---

### 3.4 级联选择 (CASCADER)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `placeholder` | String | 占位提示文字 | "请选择" | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `options` | Array | 级联选项数据 | [] | scheme.config |
| `props` | Object | 配置选项 | {} | scheme.config |
| `clearable` | Boolean | 是否可清空 | true | scheme.config |
| `show-all-levels` | Boolean | 是否显示完整路径 | true | scheme.config |
| `filterable` | Boolean | 是否可搜索 | false | scheme.config |
| `size` | String | 尺寸（medium/small/mini） | "medium" | scheme.config |

**级联选项格式:**
```json
{
  "options": [
    {
      "value": "zhinan",
      "label": "指南",
      "children": [
        {"value": "shejiyuanze", "label": "设计原则"},
        {"value": "daohang", "label": "导航"}
      ]
    }
  ]
}
```

---

### 3.5 图片选择 (IMAGE_SELECT)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `required` | Boolean | 是否必填 | false | 基础属性 |
| `options` | Array | 选项列表（带图片） | [] | scheme.config |
| `option.label` | String | 选项文本 | - | options数组项 |
| `option.value` | String/Number | 选项值 | - | options数组项 |
| `option.image` | String | 图片URL | "" | options数组项 |
| `multiple` | Boolean | 是否多选 | false | scheme.config |

**选项格式:**
```json
{
  "options": [
    {"label": "选项1", "value": "option1", "image": "https://example.com/image1.jpg"},
    {"label": "选项2", "value": "option2", "image": "https://example.com/image2.jpg"}
  ]
}
```

---

## 四、上传类组件

### 4.1 图片上传 (IMAGE_UPLOAD)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `required` | Boolean | 是否必填 | false | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `limit` | Number | 最大上传数量 | 1 | scheme.config |
| `accept` | String | 接受的文件类型 | "image/*" | scheme.config |
| `list-type` | String | 文件列表类型（text/picture/picture-card） | "picture" | scheme.config |
| `max-size` | Number | 文件大小限制（KB） | - | scheme.config |

**示例配置:**
```json
{
  "required": true,
  "limit": 3,
  "accept": "image/*",
  "list-type": "picture-card",
  "max-size": 2048
}
```

---

### 4.2 文件上传 (UPLOAD)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `required` | Boolean | 是否必填 | false | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `limit` | Number | 最大上传数量 | 1 | scheme.config |
| `accept` | String | 接受的文件类型 | "*" | scheme.config |
| `max-size` | Number | 文件大小限制（KB） | - | scheme.config |
| `auto-upload` | Boolean | 是否自动上传 | true | scheme.config |

---

## 五、展示类组件

### 5.1 图片展示 (IMAGE)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `imageUrl` | String | 图片地址 | "" | scheme.config |
| `fit` | String | 图片适应方式 | "cover" | scheme.config |
| `previewList` | Array | 预览图片列表 | [] | scheme.config |

**适应方式选项:**
- `fill`: 填充
- `contain`: 适应（保持比例，完整显示）
- `cover`: 覆盖（保持比例，填充容器）
- `none`: 无缩放
- `scale-down`: 缩放

**示例配置:**
```json
{
  "imageUrl": "https://example.com/image.jpg",
  "fit": "cover",
  "previewList": ["https://example.com/image.jpg"]
}
```

---

### 5.2 图片轮播 (IMAGE_CAROUSEL)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `height` | Number | 轮播高度（px） | 300 | scheme.config |
| `interval` | Number | 切换间隔（毫秒） | 4000 | scheme.config |
| `fit` | String | 图片适应方式 | "cover" | scheme.config |
| `options` | Array | 轮播项列表 | [] | scheme.config |
| `option.text` | String | 文字说明 | "" | options数组项 |
| `option.url` | String | 图片URL | "" | options数组项 |
| `indicator-position` | String | 指示器位置（outside/none） | "outside" | scheme.config |
| `arrow` | String | 箭头显示时机（always/hover/never） | "hover" | scheme.config |

**轮播项格式:**
```json
{
  "height": 300,
  "interval": 4000,
  "fit": "cover",
  "options": [
    {"text": "图片1", "url": "https://example.com/image1.jpg"},
    {"text": "图片2", "url": "https://example.com/image2.jpg"}
  ]
}
```

---

### 5.3 文字描述 (DESC_TEXT)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `content` | String | 描述文字内容 | "" | scheme.config |
| `textAlign` | String | 文字对齐方式（left/center/right） | "left" | scheme.config |
| `fontSize` | Number | 字体大小（px） | 14 | scheme.config |
| `color` | String | 文字颜色 | "#606266" | scheme.config |

---

### 5.4 分割线 (DIVIDER)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `content` | String | 分割线文字 | "" | scheme.config |
| `contentPosition` | String | 文字位置（left/center/right） | "center" | scheme.config |
| `direction` | String | 分割线方向（horizontal/vertical） | "horizontal" | scheme.config |

**示例配置:**
```json
{
  "content": "分割线文字",
  "contentPosition": "center",
  "direction": "horizontal"
}
```

---

## 六、其他组件

### 6.1 评分组件 (RATE)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `required` | Boolean | 是否必填 | false | 基础属性 |
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `max` | Number | 最大分值 | 5 | scheme.config |
| `allow-half` | Boolean | 是否允许半选 | false | scheme.config |
| `show-text` | Boolean | 是否显示辅助文字 | false | scheme.config |
| `texts` | Array | 辅助文字数组 | [] | scheme.config |
| `colors` | Array | 颜色数组 | [] | scheme.config |

**示例配置:**
```json
{
  "required": true,
  "max": 5,
  "allow-half": false,
  "show-text": true,
  "texts": ["很差", "差", "一般", "好", "很好"]
}
```

---

### 6.2 滑块组件 (SLIDER)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `min` | Number | 最小值 | 0 | scheme.config |
| `max` | Number | 最大值 | 100 | scheme.config |
| `step` | Number | 步长 | 1 | scheme.config |
| `show-input` | Boolean | 是否显示输入框 | false | scheme.config |
| `show-stops` | Boolean | 是否显示间断点 | false | scheme.config |
| `range` | Boolean | 是否为范围选择 | false | scheme.config |
| `vertical` | Boolean | 是否垂直方向 | false | scheme.config |

**示例配置:**
```json
{
  "min": 0,
  "max": 100,
  "step": 10,
  "show-input": true,
  "show-stops": true
}
```

---

### 6.3 排序题型 (SORT)

| 属性名 | 类型 | 说明 | 默认值 | 位置 |
|--------|------|------|--------|------|
| `disabled` | Boolean | 是否禁用 | false | 基础属性 |
| `options` | Array | 选项列表 | [] | scheme.config |
| `option.label` | String | 选项文本 | - | options数组项 |
| `option.value` | String/Number | 选项值 | - | options数组项 |
| `defaultValue` | Array | 默认排序（选项数组） | [] | 基础属性 |

**示例配置:**
```json
{
  "options": [
    {"label": "选项1", "value": "option1"},
    {"label": "选项2", "value": "option2"}
  ],
  "defaultValue": [
    {"label": "选项1", "value": "option1"},
    {"label": "选项2", "value": "option2"}
  ]
}
```

---

## 七、配置属性存储说明

### 7.1 数据库字段结构

Tduck 使用以下字段存储组件配置：

1. **基础字段**（直接存储在表字段中）:
   - `label`: 标题
   - `form_item_id`: 表单项ID
   - `type`: 组件类型
   - `required`: 是否必填
   - `placeholder`: 提示文字
   - `span`: 栅格宽度
   - `default_value`: 默认值
   - `show_label`: 是否显示标签
   - `sort`: 排序号

2. **scheme 字段**（JSON格式，存储扩展配置）:
   ```json
   {
     "style": {
       "width": "100%"
     },
     "config": {
       // 组件特有配置
     },
     "typeId": "INPUT",
     "vModel": "input-xxx",
     "disabled": false,
     "readonly": false,
     // 其他扩展属性
   }
   ```

3. **reg_list 字段**（JSON格式，存储正则表达式验证规则）:
   ```json
   [
     {
       "pattern": "正则表达式",
       "message": "验证失败提示信息"
     }
   ]
   ```

### 7.2 配置优先级

1. 基础字段优先级最高（直接字段）
2. scheme.config 中的配置次之
3. scheme 根级别的配置作为补充

---

## 八、常用配置组合示例

### 8.1 带验证的单行文本
```json
{
  "label": "手机号",
  "placeholder": "请输入手机号",
  "required": true,
  "maxLength": 11,
  "show-word-limit": true,
  "regList": [
    {
      "pattern": "^1[3-9]\\d{9}$",
      "message": "请输入正确的手机号"
    }
  ]
}
```

### 8.2 带默认值的多选框
```json
{
  "label": "兴趣爱好",
  "required": true,
  "options": [
    {"label": "阅读", "value": "reading"},
    {"label": "运动", "value": "sports"},
    {"label": "音乐", "value": "music"}
  ],
  "defaultValue": ["reading", "sports"],
  "min": 1,
  "max": 3
}
```

### 8.3 带范围限制的数字输入
```json
{
  "label": "年龄",
  "placeholder": "请输入年龄",
  "required": true,
  "min": 0,
  "max": 150,
  "step": 1,
  "precision": 0
}
```

---

## 九、注意事项

1. **scheme 字段是 JSON 格式**，需要正确序列化和反序列化
2. **regList 是独立字段**，不是存储在 scheme 中
3. **defaultValue 可以是字符串或对象**，根据组件类型而定
4. **span 值范围是 1-24**，24 表示 100% 宽度
5. **所有布尔类型字段**在数据库中存储为 tinyint(1)
6. **组件类型必须与 FormItemTypeEnum 枚举值匹配**

---

## 十、组件类型枚举

参考 `FormItemTypeEnum.java`，支持的组件类型：

- `INPUT`: 单行文本
- `TEXTAREA`: 多行文本
- `NUMBER`: 数字
- `DATE`: 日期选择
- `RADIO`: 单选框
- `CHECKBOX`: 多选框
- `SELECT`: 下拉框
- `CASCADER`: 级联选择
- `SLIDER`: 滑块
- `RATE`: 评分
- `IMAGE_UPLOAD`: 图片上传
- `UPLOAD`: 文件上传
- `IMAGE`: 图片展示
- `IMAGE_SELECT`: 图片选择
- `IMAGE_CAROUSEL`: 图片轮播
- `DESC_TEXT`: 文字描述
- `DIVIDER`: 分割线
- `SORT`: 排序

---

## 十一、参考资源

- Tduck 后端实体类: `UserFormItemEntity.java`
- Tduck 组件类型枚举: `FormItemTypeEnum.java`
- Tduck 数据库表结构: `fm_user_form_item`
- Tduck 表单生成器: `tduck-form-generator` npm 包

---

**文档版本**: 1.0  
**最后更新**: 2025-01-09  
**基于**: Tduck Platform v3/v4/v5

