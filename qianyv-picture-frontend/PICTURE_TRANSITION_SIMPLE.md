# 图片列表滑动过渡动画

## 📋 功能说明

为图片列表切换（搜索、分类切换、横竖屏切换）添加**左侧滑动**的过渡效果。

**特别优化**：横竖屏切换时，采用**延迟布局切换**策略，先让旧图完全淡出，然后再改变布局（从网格变瀑布流或反之），最后新图再淡入，完全避免布局切换的闪烁。

## ✨ 实现效果

### 动画流程

```
用户触发操作（搜索/切换分类/切换方向）
  ↓
旧图片快速向左移动并淡出（0.3秒）
  ↓
【关键】布局切换（网格⇄瀑布流）
  ↓
加载新数据
  ↓
新图片从左侧滑入并淡入（0.4秒）
```

### 动画特点

- ✅ **旧图片**：向左移动50px，同时透明度从1降到0（快速淡出）
- ✅ **延迟布局切换**：旧图完全淡出后才改变布局，用户看不到布局切换过程
- ✅ **新图片**：从左侧-40px位置滑入，同时透明度从0升到1
- ✅ **快速流畅**：总耗时约0.7秒，完全避免布局切换闪烁
- ✅ **不影响交互**：横屏3D旋转、竖屏放大效果完全不受影响
- ✅ **智能触发**：只在切换查询条件时触发，加载更多时不触发
- ✅ **完美横竖屏切换**：看不到网格变瀑布流（或反之）的过程

## 🎬 动画演示

### 旧图片退出

```
[图片原位置]  →  [向左移动50px]
  opacity: 1      opacity: 0
     ↓               ↓
   清晰          快速消失（0.3秒）
```

### 新图片进入

```
[从左-40px]  →  [滑到原位置]
 opacity: 0     opacity: 1
     ↓              ↓
   隐藏         快速出现（0.4秒）
```

## 🔧 实现细节

### 1. 状态管理

```typescript
// 目标图片类型（用于API请求和滑块显示）
const pictureType = ref<number>(0)
// 当前显示的布局类型（延迟切换，在旧图淡出后才改变）
const displayPictureType = ref<number>(0)
// 过渡状态标记
const isTransitioning = ref<boolean>(false)
```

**关键设计**：

- `pictureType`：用户选择的目标类型，立即更新（用于 API 请求）
- `displayPictureType`：实际渲染的布局类型，延迟更新（在淡出后切换）
- 这样就能实现"先淡出 → 切换布局 → 再淡入"的效果

### 2. 数据加载逻辑

```typescript
const fetchPictures = async (isLoadMore = false) => {
  if (loading.value) return

  // 如果不是加载更多且有图片，先触发滑出动画
  if (!isLoadMore && pictures.value.length > 0) {
    isTransitioning.value = true
    await new Promise((resolve) => setTimeout(resolve, 300)) // 快速淡出

    // ✨ 关键：淡出完成后，切换显示的布局类型
    displayPictureType.value = pictureType.value
    await new Promise((resolve) => setTimeout(resolve, 50)) // 让DOM更新
  }

  loading.value = true
  // ... 加载数据（使用 pictureType.value 作为 API 参数）

  if (!isLoadMore) {
    pictures.value = pageData.records || []
    // 数据更新后，立即重置过渡状态，触发滑入动画
    nextTick(() => {
      isTransitioning.value = false
    })
  }
}
```

**执行流程**：

1. 旧图淡出（0.3秒）
2. 切换 `displayPictureType`，布局改变（用户看不到，因为已淡出）
3. 加载新数据
4. 新图在新布局下淡入（0.4秒）

### 3. 模板绑定

```vue
<!-- 使用 displayPictureType 控制实际显示的布局 -->
<div v-else-if="displayPictureType === 1" class="waterfall-container">
  <!-- 竖屏瀑布流 -->
</div>
<div v-else class="grid-container">
  <!-- 横屏网格 -->
</div>
```

**关键点**：

- 模板使用 `displayPictureType` 而不是 `pictureType`
- 这样布局切换发生在淡出之后，用户看不到切换过程

### 4. CSS动画

```css
/* 旧图片：快速向左移动并淡出（避免布局切换可见） */
.pictures-container.fade-out .waterfall-container,
.pictures-container.fade-out .grid-container {
  animation: slideOutLeft 0.3s ease-out forwards;
}

@keyframes slideOutLeft {
  0% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateX(-50px) scale(1);
  }
}

/* 新图片：从左侧快速滑入并淡入 */
.waterfall-container,
.grid-container {
  animation: slideInFromLeft 0.4s ease-out forwards;
}

@keyframes slideInFromLeft {
  0% {
    opacity: 0;
    transform: translateX(-40px) scale(1);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}
```

## 📊 完整时间轴

```
0ms ────────────────────────────────────────
     用户点击"横屏/竖屏"切换按钮
     pictureType = 1 (立即更新，用于API)
     displayPictureType = 0 (保持不变，旧布局)
     isTransitioning = true
     ↓
     旧图片开始快速向左移动并淡出
     起始位置: translateX(0), opacity: 1
     ↓
150ms ──────────────────────────────────────
     旧图片移动到一半
     位置: translateX(-25px), opacity: 0.5
     ↓
300ms ──────────────────────────────────────
     旧图片完全消失（用户看不到了）
     位置: translateX(-50px), opacity: 0
     ↓
     🔑 关键时刻：切换布局
     displayPictureType = 1 (切换到新布局)
     ↓
350ms ──────────────────────────────────────
     开始加载新数据（API请求）
     ↓
     （加载时间，取决于网络）
     ↓
     更新 pictures.value（新数据）
     isTransitioning = false
     ↓
     新图片开始从左侧滑入并淡入
     起始位置: translateX(-40px), opacity: 0
     ↓
+200ms ─────────────────────────────────────
     新图片滑入到一半
     位置: translateX(-20px), opacity: 0.5
     ↓
+400ms ─────────────────────────────────────
     新图片完全到位
     位置: translateX(0), opacity: 1
     过渡完成！✨
```

**关键优势**：

- ✅ 布局切换发生在 300ms 时（旧图已完全透明）
- ✅ 用户完全看不到从"网格"变成"瀑布流"的过程
- ✅ 新图以正确的布局淡入，自然流畅

## 🎯 优势

1. **方向一致**：旧图向左退出，新图从左进入，形成连贯的视觉流
2. **性能优良**：使用CSS transform和opacity，GPU加速
3. **完美的延迟布局切换**：
   - ✅ 旧图先淡出（0.3秒）
   - ✅ 布局在淡出后切换（用户看不到）
   - ✅ 新图在新布局下淡入（0.4秒）
   - ✅ **完全避免**网格⇄瀑布流的闪烁
4. **横竖屏切换体验**：
   - ✅ 看不到布局变化过程
   - ✅ 滑块立即响应
   - ✅ 数据无缝过渡
5. **不影响现有功能**：
   - ✅ 横屏3D旋转正常工作
   - ✅ 竖屏放大效果正常工作
   - ✅ 标签显示正常工作
   - ✅ 按钮交互正常工作
6. **用户体验好**：滑动方向清晰，过渡自然流畅

## 🔍 测试建议

1. **切换分类**：验证滑动效果
2. **搜索功能**：验证过渡流畅性
3. **横竖屏切换**：验证滑动动画
4. **加载更多**：验证不触发过渡（直接追加）
5. **首次加载**：验证直接显示（无过渡）

## 📝 注意事项

- 首次加载页面时不会触发过渡动画
- 点击"加载更多"时不会触发过渡动画
- 只有在有旧图片的情况下切换查询条件才会触发过渡
- 过渡期间禁用了重复请求（loading状态保护）
- 新图片的滑入动画会在每次数据更新后自动触发

## 🎨 自定义调整

### 调整移动距离

```css
/* 旧图片向左移动距离（当前：-50px） */
transform: translateX(-50px); /* 改为 -70px 移动更远 */

/* 新图片起始位置（当前：-40px） */
transform: translateX(-40px); /* 改为 -60px 从更远处滑入 */
```

### 调整动画时间

```typescript
// 等待旧图片淡出时间（当前：300ms）
await new Promise(resolve => setTimeout(resolve, 300))

// CSS动画时间
animation: slideOutLeft 0.3s ease-out forwards; // 淡出时间（当前：0.3秒）
animation: slideInFromLeft 0.4s ease-out forwards; // 滑入时间（当前：0.4秒）
```

**⚠️ 重要提示**：

- 旧图片淡出要**快速**（推荐0.2-0.4秒），避免用户看到布局切换
- JavaScript等待时间应与CSS淡出时间一致
- 新图片滑入可以稍慢一些（0.4-0.5秒），让用户看清新布局

### 调整缓动函数

```css
/* 更快的开始 */
animation: slideOutLeft 0.4s ease-in forwards;

/* 更平滑的结束 */
animation: slideInFromLeft 0.5s ease-in-out forwards;
```

## 💡 设计思路

采用"左进左出"的设计，而不是"左出右进"，是因为：

1. 视觉连贯性更好
2. 用户的视线不需要大幅移动
3. 符合"替换"的概念，而不是"翻页"
4. 在小屏幕上体验更好

## 🎊 最终效果

- 🌊 流畅的滑动过渡
- 💫 自然的淡入淡出
- 🎯 清晰的方向指示
- ⚡ 优秀的性能表现
