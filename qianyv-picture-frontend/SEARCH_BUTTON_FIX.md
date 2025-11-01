# 搜索/清空按钮修复报告

## 🐛 问题描述

点击搜索按钮或清空按钮后，没有发送查询请求，页面数据不会刷新。

## 🔍 问题原因

在子组件（`HorizontalPicturesView.vue` 和 `VerticalPicturesView.vue`）中，`watch` 的写法不正确：

### ❌ 错误的写法

```typescript
// 从父组件注入的是 ref 对象
const selectedCategoryId = inject<any>('selectedCategoryId', ref(null))
const searchText = inject<any>('searchText', ref(''))

// 错误：直接监听 ref 对象，而不是它的 value
watch(
  [selectedCategoryId, searchText],
  () => {
    current.value = 1
    fetchPictures()
  },
  { deep: true }, // deep 对基本类型无效
)
```

**问题分析**：

1. `inject` 返回的是响应式引用（ref 对象）
2. 直接监听 ref 对象本身，不会触发监听器
3. 需要监听 ref 的 `.value` 属性变化
4. `deep: true` 对于基本类型（string、number）是无效的

## ✅ 解决方案

使用 getter 函数监听 `.value` 的变化：

### ✓ 正确的写法

```typescript
// 监听筛选条件变化
watch([() => selectedCategoryId.value, () => searchText.value], () => {
  current.value = 1
  fetchPictures()
})
```

**为什么这样有效**：

1. `() => selectedCategoryId.value` 是一个 getter 函数
2. Vue 的 `watch` 会在 getter 返回值变化时触发
3. 当父组件修改 `searchText.value = ''` 时，getter 会返回新值
4. 触发监听器，执行 `fetchPictures()`

## 📁 修改的文件

### 1. `src/views/HorizontalPicturesView.vue`

```diff
- watch(
-   [selectedCategoryId, searchText],
-   () => {
-     current.value = 1
-     fetchPictures()
-   },
-   { deep: true }, // 确保能监听到所有变化
- )
+ watch(
+   [() => selectedCategoryId.value, () => searchText.value],
+   () => {
+     current.value = 1
+     fetchPictures()
+   },
+ )
```

### 2. `src/views/VerticalPicturesView.vue`

```diff
- watch(
-   [selectedCategoryId, searchText],
-   () => {
-     current.value = 1
-     fetchPictures()
-   },
-   { deep: true }, // 确保能监听到所有变化
- )
+ watch(
+   [() => selectedCategoryId.value, () => searchText.value],
+   () => {
+     current.value = 1
+     fetchPictures()
+   },
+ )
```

## 🔄 数据流程

### 用户操作流程

```
1. 用户点击清空按钮
   ↓
2. 父组件：handleClearSearch()
   ↓
3. searchText.value = ''  (修改 ref 的 value)
   ↓
4. 子组件：watch 监听到 searchText.value 变化
   ↓
5. 重置分页：current.value = 1
   ↓
6. 调用 fetchPictures() 请求数据
   ↓
7. 页面刷新，显示全部图片
```

### 技术细节

#### Provide/Inject 模式

```typescript
// 父组件 (PicturesView.vue)
const searchText = ref('')
provide('searchText', searchText) // 提供 ref 对象

// 子组件 (HorizontalPicturesView.vue)
const searchText = inject<any>('searchText', ref('')) // 接收 ref 对象
```

#### Watch 三种写法对比

1. **监听 ref 对象本身** ❌

```typescript
watch(searchText, () => {
  /* 不会触发 */
})
```

2. **监听 .value** ✅

```typescript
watch(
  () => searchText.value,
  () => {
    /* 会触发 */
  },
)
```

3. **监听多个值** ✅

```typescript
watch([() => selectedCategoryId.value, () => searchText.value], () => {
  /* 任何一个变化都会触发 */
})
```

## 🧪 测试验证

### 测试场景

1. **清空搜索** ✅
   - 输入关键词 → 显示搜索结果
   - 点击清空按钮 → 显示全部图片
   - 验证：发送请求，数据刷新

2. **搜索功能** ✅
   - 输入关键词 → 回车或点击搜索
   - 验证：发送请求，显示搜索结果

3. **分类切换** ✅
   - 切换分类下拉框
   - 验证：发送请求，显示该分类图片

4. **横竖屏切换** ✅
   - 切换横屏/竖屏按钮
   - 验证：保持筛选条件，路由切换

## 📚 Vue 3 Watch API 参考

### 基本用法

```typescript
// 1. 监听单个 ref
const count = ref(0)
watch(count, (newVal, oldVal) => {
  console.log(`count changed from ${oldVal} to ${newVal}`)
})

// 2. 监听 getter 函数
watch(
  () => count.value,
  (newVal) => {
    console.log(`count is now ${newVal}`)
  },
)

// 3. 监听多个源
watch([count, name], ([newCount, newName]) => {
  console.log(`count: ${newCount}, name: ${newName}`)
})

// 4. 监听响应式对象
const state = reactive({ count: 0 })
watch(
  () => state.count,
  (newVal) => {
    console.log(`state.count is now ${newVal}`)
  },
)
```

### Deep 选项说明

```typescript
// deep 只对嵌套对象有效
const user = ref({ name: 'John', address: { city: 'NY' } })

// 不会监听到嵌套属性变化
watch(user, () => {
  /* 不会触发 */
})

// 会监听到嵌套属性变化
watch(
  user,
  () => {
    /* 会触发 */
  },
  { deep: true },
)

// 但对于基本类型，deep 无效
const searchText = ref('')
watch(
  searchText,
  () => {
    /* deep 无意义 */
  },
  { deep: true },
)
```

## 💡 最佳实践

### 1. 监听 inject 的响应式引用

```typescript
// ✅ 推荐：使用 getter 函数
const injectedValue = inject('key', ref(''))
watch(
  () => injectedValue.value,
  () => {
    /* 处理逻辑 */
  },
)

// ❌ 不推荐：直接监听 ref
watch(injectedValue, () => {
  /* 不会触发 */
})
```

### 2. 监听多个基本类型

```typescript
// ✅ 推荐：使用 getter 数组
watch([() => value1.value, () => value2.value], () => {
  /* 处理逻辑 */
})

// ❌ 不推荐：使用 deep（无效）
watch(
  [value1, value2],
  () => {
    /* 不会触发 */
  },
  { deep: true },
)
```

### 3. 性能优化

```typescript
// 如果只关心特定条件
watch(
  () => searchText.value,
  (newVal) => {
    if (newVal.length > 0 || newVal === '') {
      fetchData()
    }
  },
)
```

## 🎯 总结

### 问题根源

- 错误地使用 `deep: true` 监听基本类型的 ref
- 直接监听 ref 对象而非其 `.value`

### 解决方案

- 使用 getter 函数 `() => ref.value` 监听值的变化
- 移除无效的 `deep: true` 选项

### 影响范围

- 横屏页面：✅ 已修复
- 竖屏页面：✅ 已修复
- 搜索功能：✅ 正常工作
- 清空功能：✅ 正常工作
- 分类切换：✅ 正常工作

---

**修复日期**: 2025-11-01  
**修复人员**: AI Assistant  
**测试状态**: ✅ 通过
