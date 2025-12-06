# vue-draggable-plus 完整文档说明

## 📚 文档来源

本文档基于官方文档整理，来源包括：
- **官方 GitHub**: https://github.com/alfred-skyblue/vue-draggable-plus
- **官方文档网站**: https://vue-draggable-plus.pages.dev/
- **GitCode 镜像**: https://gitcode.com/gh_mirrors/vu/vue-draggable-plus

## 📦 安装

```bash
npm install vue-draggable-plus
```

当前项目版本：`vue-draggable-plus@^0.6.0`

## 🔑 关于 JavaScript 和 TypeScript

### 重要说明

1. **组件本身使用 TypeScript 编写**
   - `vue-draggable-plus` 的源码完全使用 TypeScript 开发
   - 提供了完整的类型定义文件（.d.ts）

2. **JavaScript 项目可以正常使用**
   - ✅ 组件可以在纯 JavaScript 项目中使用
   - ✅ 功能完全正常，没有任何限制
   - ⚠️ 只是无法享受 TypeScript 的类型检查和智能提示

3. **当前项目状态分析**
   ```javascript
   // 项目中使用的是 JavaScript（正确）
   <script setup>
   // 不是 <script setup lang="ts">
   ```

### JavaScript vs TypeScript 使用对比

#### JavaScript 使用方式（当前项目）

```vue
<template>
  <VueDraggable v-model="list" @start="onStart" @end="onEnd">
    <div v-for="item in list" :key="item.id">
      {{ item.name }}
    </div>
  </VueDraggable>
</template>

<script setup>
import { ref } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'

const list = ref([
  { name: 'Joao', id: '1' },
  { name: 'Jean', id: '2' },
  { name: 'Johanna', id: '3' },
  { name: 'Juan', id: '4' }
])

function onStart(event) {
  console.log('开始拖拽')
}

function onEnd(event) {
  console.log('拖拽结束')
}
</script>
```

#### TypeScript 使用方式（可选）

```vue
<template>
  <VueDraggable v-model="list" @start="onStart" @end="onEnd">
    <div v-for="item in list" :key="item.id">
      {{ item.name }}
    </div>
  </VueDraggable>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { VueDraggable, DraggableEvent } from 'vue-draggable-plus'

const list = ref([
  { name: 'Joao', id: '1' },
  { name: 'Jean', id: '2' },
  { name: 'Johanna', id: '3' },
  { name: 'Juan', id: '4' }
])

function onStart(event: DraggableEvent) {
  console.log('开始拖拽')
}

function onEnd(event: DraggableEvent) {
  console.log('拖拽结束')
}
</script>
```

**差异点**：
- TypeScript 版本有类型注解：`event: DraggableEvent`
- TypeScript 版本有更好的 IDE 智能提示
- JavaScript 版本功能完全相同，只是缺少类型检查

## 🎯 三种使用方式

vue-draggable-plus 提供了三种使用方式，可以根据需求选择：

### 1. 组件方式（推荐，当前项目使用）

```vue
<template>
  <VueDraggable
    v-model="list"
    item-key="id"
    handle=".drag-handle"
    :animation="200"
    ghost-class="sortable-ghost"
    @end="handleDragEnd"
  >
    <template #item="{ element, index }">
      <div class="item">
        <el-icon class="drag-handle"><Rank /></el-icon>
        <span>{{ element.name }}</span>
      </div>
    </template>
  </VueDraggable>
</template>

<script setup>
import { ref } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'

const list = ref([...])
</script>
```

**优点**：
- ✅ 最简单易用
- ✅ 支持 v-model 双向绑定
- ✅ 支持插槽，灵活定制
- ✅ 代码清晰易读

### 2. 函数方式（useDraggable Hook）

```vue
<template>
  <div ref="el">
    <div v-for="item in list" :key="item.id">
      {{ item.name }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useDraggable } from 'vue-draggable-plus'

const el = ref(null)
const list = ref([...])

const draggable = useDraggable(el, list, {
  animation: 150,
  onStart() {
    console.log('start')
  },
  onUpdate() {
    console.log('update')
  }
})

// 可以调用方法控制拖拽
// draggable.start()   // 启动拖拽
// draggable.pause()   // 暂停拖拽
// draggable.resume()  // 恢复拖拽
</script>
```

**优点**：
- ✅ 更灵活的控制
- ✅ 可以编程式控制拖拽行为
- ✅ 适合复杂场景

### 3. 指令方式（v-draggable）

```vue
<template>
  <div
    v-draggable="[
      list,
      {
        animation: 150,
        onStart() {
          console.log('start')
        }
      }
    ]"
  >
    <div v-for="item in list" :key="item.id">
      {{ item.name }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { vDraggable } from 'vue-draggable-plus'

const list = ref([...])
</script>
```

**优点**：
- ✅ 声明式，简洁
- ✅ 适合简单场景

## 📋 常用 Props（组件方式）

### 必需属性

| Prop | 类型 | 说明 | 示例 |
|------|------|------|------|
| `v-model` | Array | 绑定的数组数据（必需） | `v-model="list"` |
| `item-key` | String/Function | 每个项的唯一标识（必需） | `item-key="id"` |

### 常用配置属性

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `handle` | String | - | 拖拽手柄选择器 | `.drag-handle` |
| `animation` | Number | 0 | 动画时长（毫秒） | `200` |
| `ghost-class` | String | - | 拖拽时占位符的 CSS 类名 | `sortable-ghost` |
| `chosen-class` | String | - | 选中项的 CSS 类名 | `chosen-item` |
| `drag-class` | String | - | 拖拽时的 CSS 类名 | `drag-item` |
| `disabled` | Boolean | false | 是否禁用拖拽 | `false` |
| `group` | String/Object | - | 多个列表之间的拖拽组 | `"people"` 或 `{ name: "people", pull: "clone" }` |
| `sort` | Boolean | true | 是否允许排序 | `true` |
| `clone` | Function | - | 克隆函数 | `(item) => ({ ...item, id: newId })` |
| `target` | String/Function | - | 指定目标容器 | `".sort-target"` |
| `scroll` | Boolean | - | 是否启用滚动 | `true` |

## 📡 常用事件

| 事件 | 参数 | 说明 |
|------|------|------|
| `@start` | `event` | 拖拽开始 |
| `@end` | `event` | 拖拽结束 |
| `@add` | `event` | 添加元素时触发 |
| `@update` | `event` | 列表更新时触发 |
| `@remove` | `event` | 移除元素时触发 |
| `@choose` | `event` | 选中元素时触发 |
| `@unchoose` | `event` | 取消选中时触发 |
| `@clone` | `event` | 克隆元素时触发 |

## 🎨 项目中的实际使用

### 1. SurveyDesignNew.vue（表单设计器）

```vue
<VueDraggable
  v-model="drawingList"
  :key="`draggable-${drawingList.length}`"
  item-key="formItemId"
  handle=".drag-handle"
  :animation="200"
  ghost-class="sortable-ghost"
  chosen-class="chosen-item"
  drag-class="drag-item"
  @end="handleDragEnd"
>
  <template #item="{ element, index }">
    <!-- 表单项内容 -->
  </template>
</VueDraggable>
```

**配置说明**：
- ✅ `item-key="formItemId"` - 使用表单项的唯一 ID
- ✅ `handle=".drag-handle"` - 指定拖拽手柄
- ✅ `:animation="200"` - 200ms 动画效果
- ✅ 自定义样式类名，提供视觉反馈

### 2. SurveyDesign.vue（问卷设计器）

```vue
<VueDraggable
  v-model="questions"
  :key="`draggable-${questions.length}`"
  item-key="id"
  handle=".drag-handle"
  :animation="200"
  ghost-class="sortable-ghost"
  chosen-class="chosen-item"
  drag-class="drag-item"
  @end="handleQuestionOrderChange"
>
  <template #item="{ element, index }">
    <!-- 题目内容 -->
  </template>
</VueDraggable>
```

**配置说明**：
- ✅ `item-key="id"` - 使用题目的 ID
- ✅ `handle=".drag-handle"` - 指定拖拽手柄
- ✅ `@end` 事件中保存排序结果

## 🎯 高级用法

### 1. 嵌套拖拽

```vue
<template>
  <VueDraggable class="drag-area" tag="ul" v-model="list" group="g1">
    <li v-for="el in list" :key="el.name">
      <p>{{ el.name }}</p>
      <nested-draggable v-model="el.children" />
    </li>
  </VueDraggable>
</template>

<script setup>
import { VueDraggable } from 'vue-draggable-plus'
import NestedDraggable from './NestedDraggable.vue'

const list = ref([
  {
    name: 'item 1',
    children: [
      { name: 'item 2', children: [] }
    ]
  }
])
</script>
```

### 2. 列表间克隆

```vue
<template>
  <div class="flex">
    <!-- 源列表 -->
    <VueDraggable
      v-model="list1"
      :animation="150"
      :group="{ name: 'people', pull: 'clone', put: false }"
      :sort="false"
      :clone="clone"
    >
      <div v-for="item in list1" :key="item.id">
        {{ item.name }}
      </div>
    </VueDraggable>
    
    <!-- 目标列表 -->
    <VueDraggable
      v-model="list2"
      :animation="150"
      group="people"
    >
      <div v-for="item in list2" :key="item.id">
        {{ item.name }}
      </div>
    </VueDraggable>
  </div>
</template>

<script setup>
function clone(element) {
  const len = list2.value.length
  return {
    name: `${element.name}-clone-${len}`,
    id: `${element.id}-clone-${len}`
  }
}
</script>
```

### 3. 指定目标容器

当需要使用非根元素作为拖拽容器时：

```vue
<template>
  <VueDraggable
    v-model="list"
    target=".sort-target"
    :scroll="true"
  >
    <div class="wrapper">
      <ul class="sort-target">
        <li v-for="item in list" :key="item.id">
          {{ item.name }}
        </li>
      </ul>
    </div>
  </VueDraggable>
</template>
```

### 4. 与 TransitionGroup 结合

```vue
<template>
  <VueDraggable
    v-model="list"
    target=".sort-target"
  >
    <TransitionGroup
      type="transition"
      tag="ul"
      name="fade"
      class="sort-target"
    >
      <li
        v-for="item in list"
        :key="item.id"
      >
        {{ item.name }}
      </li>
    </TransitionGroup>
  </VueDraggable>
</template>

<style>
.fade-move,
.fade-enter-active,
.fade-leave-active {
  transition: all 0.5s;
}
</style>
```

## 🎨 CSS 样式配置

项目中使用的拖拽样式：

```css
/* 拖拽占位符样式 */
.sortable-ghost {
  opacity: 0.5;
  background: rgba(24, 144, 255, 0.1);
  border: 2px dashed #409EFF;
}

/* 选中项样式 */
.chosen-item {
  cursor: grabbing;
}

/* 拖拽中样式 */
.drag-item {
  cursor: grabbing;
}

/* 拖拽手柄样式 */
.drag-handle {
  cursor: move;
  color: #909399;
  font-size: 18px;
}

.drag-handle:hover {
  color: #409EFF;
}
```

## ⚠️ 常见问题

### 1. 拖拽不生效

**可能原因**：
- 没有正确设置 `item-key`
- `v-model` 绑定的数据不是响应式的

**解决方案**：
```javascript
// ❌ 错误
const list = []

// ✅ 正确
const list = ref([])
```

### 2. 拖拽后数据没有更新

**解决方案**：
```javascript
// 在 @end 事件中手动保存
const handleDragEnd = async () => {
  await saveData()
}
```

### 3. 使用 handle 时拖拽不工作

**检查点**：
- 手柄元素是否存在
- 手柄选择器是否正确
- 是否有 CSS 阻止拖拽

```vue
<!-- ✅ 正确 -->
<el-icon class="drag-handle"><Rank /></el-icon>

<!-- handle 选择器 -->
handle=".drag-handle"
```

### 4. 列表更新后拖拽失效

**解决方案**：
```vue
<!-- 使用 key 强制重新渲染 -->
<VueDraggable
  :key="`draggable-${list.length}`"
  v-model="list"
>
```

## 💡 最佳实践

1. **始终使用 `item-key`**
   ```vue
   item-key="id"  <!-- 确保每个项都有唯一标识 -->
   ```

2. **使用拖拽手柄提升用户体验**
   ```vue
   handle=".drag-handle"  <!-- 指定特定区域才能拖拽 -->
   ```

3. **添加动画效果**
   ```vue
   :animation="200"  <!-- 让拖拽更流畅 -->
   ```

4. **自定义视觉反馈**
   ```vue
   ghost-class="sortable-ghost"
   chosen-class="chosen-item"
   drag-class="drag-item"
   ```

5. **及时保存数据**
   ```javascript
   @end="handleDragEnd"  // 拖拽结束后保存
   ```

6. **使用 TypeScript 获得更好的开发体验**（可选）
   ```vue
   <script setup lang="ts">
   import { DraggableEvent } from 'vue-draggable-plus'
   
   function onEnd(event: DraggableEvent) {
     // 有类型提示和检查
   }
   </script>
   ```

## 📝 总结

### 关于 JavaScript vs TypeScript

1. ✅ **项目使用 JavaScript 完全没有问题**
   - 组件功能完全正常
   - 所有特性都可以使用
   - 只是缺少类型检查和智能提示

2. ✅ **当前使用方式正确**
   - 导入方式正确：`import { VueDraggable } from 'vue-draggable-plus'`
   - 配置合理，功能完整

3. ⚠️ **可选优化：迁移到 TypeScript**
   - 如果希望有更好的开发体验
   - 获得类型检查和智能提示
   - 但这不是必须的，JavaScript 完全可以

### 项目中的使用评估

- ✅ **SurveyDesignNew.vue**: 配置正确，功能完整
- ✅ **SurveyDesign.vue**: 配置正确，功能完整
- ✅ **版本兼容**: `0.6.0` 版本稳定
- ✅ **使用方式**: 组件方式，简单清晰

## 🔗 参考资源

- [官方文档](https://vue-draggable-plus.pages.dev/)
- [GitHub 仓库](https://github.com/alfred-skyblue/vue-draggable-plus)
- [GitCode 镜像](https://gitcode.com/gh_mirrors/vu/vue-draggable-plus)
- [Sortable.js 文档](https://sortablejs.github.io/Sortable/)

