# 分类筛选功能修复 - 快速参考

## 🔴 核心问题

**inject 使用错误的默认值，创建了新的 ref，导致无法追踪父组件的变化！**

## ✅ 解决方案（3步修复）

### 1️⃣ 修复 inject（最关键！）

```typescript
// ❌ 错误 - 创建新 ref
const data = inject<any>('key', ref(defaultValue))

// ✅ 正确 - 使用父组件的 ref
const data = inject<any>('key')
```

### 2️⃣ 修复 watch

```typescript
// ❌ 错误 - 直接监听
watch([data], ...)

// ✅ 正确 - 用 getter 函数
watch(() => [data?.value], ...)
```

### 3️⃣ 添加可选链

```typescript
// ✅ 使用时添加可选链
const value = data?.value
```

## 📁 修改的文件

- `HorizontalPicturesView.vue` - 横屏子页面
- `VerticalPicturesView.vue` - 竖屏子页面
- `PicturesView.vue` - 父组件（添加 `.number` 和调试）

## 🧪 测试方法

1. F12 → Console
2. 选择不同分类
3. 查看日志：`父组件 - 分类ID变化` → `横屏页面 - 筛选条件变化` → `发送请求`

## 📚 详细文档

查看 `CATEGORY_FILTER_FIX.md` 了解完整技术细节
