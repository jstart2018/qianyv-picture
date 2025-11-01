# 🎴 3D 卡片旋转跟随鼠标效果

## ✨ 功能概述

为横屏风格的图片卡片实现了炫酷的 **3D 旋转跟随鼠标** 交互效果。整个卡片（包括图片、边框、标签和"前往"按钮）作为一个整体，会根据鼠标在卡片上的位置进行实时 3D 旋转。

---

## 🎯 核心原理

### 两个关键元素

1. **鼠标活动区域**：`.grid-card` 元素
2. **旋转物体**：整个卡片及其所有子元素

### 旋转方向分解

- **X 轴旋转**：鼠标上下移动时，卡片前后倾斜
- **Y 轴旋转**：鼠标左右移动时，卡片左右旋转

---

## 🔧 实现细节

### 1. Script 逻辑 (TypeScript)

```typescript
// 3D 旋转跟随鼠标效果
const handleMouseMove = (event: MouseEvent) => {
  const card = event.currentTarget as HTMLElement
  const rect = card.getBoundingClientRect()

  // 计算鼠标在卡片内的相对位置（0-1范围）
  const x = (event.clientX - rect.left) / rect.width
  const y = (event.clientY - rect.top) / rect.height

  // 计算旋转角度（中心点为0，边缘最大±15度）
  const rotateY = (x - 0.5) * 30 // 左右旋转 -15度到15度
  const rotateX = (0.5 - y) * 30 // 上下旋转 -15度到15度

  // 应用3D变换
  card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.02, 1.02, 1.02)`
  card.style.transition = 'transform 0.1s ease-out'
}

// 鼠标离开时重置
const handleMouseLeave = (event: MouseEvent) => {
  const card = event.currentTarget as HTMLElement
  card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)'
  card.style.transition = 'transform 0.5s ease-out'
}
```

**关键参数说明**：

- **透视距离**：`1000px` - 控制 3D 效果的强度
- **旋转范围**：`±15度` - 最大旋转角度（乘以 30 后除以 2）
- **悬停缩放**：`1.02` - 卡片轻微放大 2%
- **跟随速度**：`0.1s` - 快速响应鼠标移动
- **复位速度**：`0.5s` - 平滑回到初始状态

---

### 2. Template 修改

```vue
<div
  v-for="pic in pictures"
  :key="pic.id"
  class="grid-card"
  @mousemove="handleMouseMove($event)"
  @mouseleave="handleMouseLeave($event)"
></div>
```

**事件绑定**：

- `@mousemove` - 实时跟踪鼠标位置
- `@mouseleave` - 鼠标离开时重置状态

---

### 3. CSS 样式优化

#### 容器设置

```css
.grid-container {
  perspective: 1200px; /* 为整个网格提供透视 */
}
```

#### 卡片基础样式

```css
.grid-card {
  transform-style: preserve-3d; /* 保持3D空间 */
  transform: perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1);
  transition:
    transform 0.5s ease-out,
    box-shadow 0.3s ease;
  will-change: transform; /* 性能优化 */
}

.grid-card:hover {
  box-shadow:
    8px 8px 20px rgba(0, 0, 0, 0.4),
    15px 15px 45px rgba(0, 0, 0, 0.3);
}
```

#### 图片层次

```css
.grid-image-wrapper {
  transform-style: preserve-3d;
  transform: translateZ(0);
}

.grid-image-wrapper img {
  transform: translateZ(20px); /* 图片浮起 20px */
}
```

#### 标签白框层次

```css
.tags-panel {
  transform-style: preserve-3d;
}

.grid-card:hover .tags-panel {
  transform: translate(-50%, -50%) scale(0.95) translateZ(30px); /* 白框浮起 30px */
}
```

---

## 🎨 视觉效果层次

### Z 轴深度布局

```
        🎯 标签白框 (translateZ: 30px)
              ⬆️
        📷 图片 (translateZ: 20px)
              ⬆️
        🃏 卡片背景 (translateZ: 0px)
```

### 旋转方向示意

```
        鼠标向上 → 卡片后仰 (rotateX 负)
             ⬆️
    左转 ⬅️  🎴  ➡️ 右转
             ⬇️
        鼠标向下 → 卡片前倾 (rotateX 正)
```

---

## 💡 交互体验

### 鼠标移动时

- ✅ 卡片实时跟随鼠标旋转
- ✅ 快速响应（0.1秒过渡）
- ✅ 轻微放大（1.02倍）
- ✅ 阴影增强，增加立体感

### 鼠标离开时

- ✅ 平滑恢复到初始状态（0.5秒过渡）
- ✅ 阴影恢复正常
- ✅ 尺寸恢复原始大小

### 特殊设计

- 🎯 只有"前往"按钮显示手型光标
- 🖼️ 图片区域保持默认光标
- 📱 整个卡片保持可交互性

---

## 🚀 性能优化

1. **`will-change: transform`** - 提前告知浏览器该元素会变换，启用硬件加速
2. **`transform-style: preserve-3d`** - 保持3D渲染上下文
3. **使用 `transform` 而非 `top/left`** - 触发 GPU 加速，避免重排
4. **短过渡时间** - 快速响应，流畅体验

---

## 📊 参数调优指南

### 旋转角度

```typescript
const rotateY = (x - 0.5) * 30 // 调整 30 改变旋转幅度
const rotateX = (0.5 - y) * 30
```

- **增大**：旋转更明显（如 `* 40`）
- **减小**：旋转更微妙（如 `* 20`）

### 透视距离

```typescript
perspective(1000px)  // 调整透视强度
```

- **增大**：3D 效果更平缓（如 `1500px`）
- **减小**：3D 效果更夸张（如 `800px`）

### 悬停缩放

```typescript
scale3d(1.02, 1.02, 1.02) // 调整悬停放大
```

- **增大**：更明显（如 `1.05`）
- **减小**：更微妙（如 `1.01`）

### Z轴深度

```css
translateZ(20px)  /* 图片层 */
translateZ(30px)  /* 标签层 */
```

- **增大**：层次更明显
- **减小**：层次更扁平

---

## 🎯 适用场景

- ✅ **横屏风格图片列表** - 当前实现
- ✅ 产品展示卡片
- ✅ 相册预览
- ✅ 作品集展示
- ✅ 任何需要增强交互感的卡片界面

---

## 🔮 扩展可能

1. **添加光泽效果**：根据鼠标位置添加动态高光
2. **视差滚动**：不同层元素移动速度不同
3. **倾斜阴影**：阴影随旋转角度动态调整
4. **弹性动画**：使用 spring 动画库增强回弹效果
5. **触摸支持**：在移动端使用陀螺仪替代鼠标

---

## 📝 注意事项

1. ⚠️ **overflow 设置**：卡片容器使用 `overflow: hidden`，避免旋转时内容溢出
2. ⚠️ **性能监控**：大量卡片时注意性能，可考虑使用虚拟滚动
3. ⚠️ **移动端适配**：触摸设备上可以禁用此效果或使用陀螺仪
4. ⚠️ **浏览器兼容性**：现代浏览器均支持，IE11 需要降级处理

---

## 🎉 效果演示

### 鼠标在中心

```
┌─────────────┐
│             │
│      🖱️     │
│   [卡片]    │
│             │
└─────────────┘
状态：无旋转，正面朝前
```

### 鼠标在左上角

```
  🖱️
┌─────────────┐
│    ↗️       │
│   [卡片]    │
│             │
└─────────────┘
状态：向左上方倾斜
rotateX: -15deg, rotateY: -15deg
```

### 鼠标在右下角

```
┌─────────────┐
│             │
│   [卡片]    │
│       ↘️    │
└─────────────┘
                🖱️
状态：向右下方倾斜
rotateX: +15deg, rotateY: +15deg
```

---

## 🏆 技术亮点

- ✨ **纯 CSS + JS 实现**：无需额外库
- 🎯 **精确计算**：基于鼠标相对位置
- 🚀 **性能优异**：使用 GPU 加速
- 💎 **视觉震撼**：3D 效果引人注目
- 🎨 **层次分明**：多层元素协同旋转

---

**创建时间**：2025年11月1日  
**功能状态**：✅ 已完成并测试  
**影响范围**：横屏风格图片卡片 (PicturesView.vue)
