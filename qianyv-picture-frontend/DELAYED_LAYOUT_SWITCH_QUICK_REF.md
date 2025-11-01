# 延迟布局切换 - 快速参考

## 🎯 核心概念

**问题**：横竖屏切换时，用户看到布局从网格变瀑布流（或反之），视觉上不连贯。

**解决**：使用**双变量设计**，在旧图淡出后再切换布局，用户看不到切换过程。

## 🔑 关键代码

### 1. 状态变量

```typescript
const pictureType = ref<number>(0) // 目标类型（API用）
const displayPictureType = ref<number>(0) // 显示类型（渲染用）
```

### 2. 切换逻辑

```typescript
const handlePictureTypeChange = (type: number) => {
  pictureType.value = type // 立即更新目标
  updateSlider() // 滑块立即响应
  fetchPictures() // 开始过渡
}
```

### 3. 延迟切换

```typescript
const fetchPictures = async () => {
  // 旧图淡出
  isTransitioning.value = true
  await new Promise((resolve) => setTimeout(resolve, 300))

  // 切换布局（用户看不到）
  displayPictureType.value = pictureType.value
  await new Promise((resolve) => setTimeout(resolve, 50))

  // 加载新数据
  // 新图淡入
}
```

### 4. 模板绑定

```vue
<!-- 使用 displayPictureType 而不是 pictureType -->
<div v-else-if="displayPictureType === 1" class="waterfall-container"></div>
```

## ⏱️ 时间序列

```
0ms    → 用户点击
        ├─ pictureType = 新值
        └─ displayPictureType = 旧值（保持）

300ms  → 旧图透明
        └─ displayPictureType = 新值（切换）

350ms  → 加载数据

???ms  → 数据到达
        └─ 新图淡入（新布局）
```

## ✅ 效果对比

### 之前 ❌

```
网格布局 → [突然变成瀑布流] → 淡出 → 淡入
          ↑ 用户看到闪烁
```

### 现在 ✅

```
网格布局 → 淡出 → [切换] → 瀑布流淡入
                   ↑ 用户看不到
```

## 🧪 测试清单

- [ ] 横屏→竖屏：看不到网格变瀑布流
- [ ] 竖屏→横屏：看不到瀑布流变网格
- [ ] 横屏下切换分类：始终网格布局
- [ ] 竖屏下切换分类：始终瀑布流布局
- [ ] 滑块立即响应
- [ ] 首次加载无动画
- [ ] 加载更多无动画

## 📝 记忆要点

1. **两个变量**：`pictureType`（目标）+ `displayPictureType`（显示）
2. **延迟切换**：在淡出后更新 `displayPictureType`
3. **模板使用**：`v-else-if="displayPictureType === 1"`
4. **时机**：300ms淡出 + 50ms DOM更新 + 400ms淡入

---

**更新**：2025-01-XX | **状态**：✅ 完成
