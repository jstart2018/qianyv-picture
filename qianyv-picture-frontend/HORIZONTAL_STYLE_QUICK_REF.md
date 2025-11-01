# 横屏图片样式恢复 - 快速参考

## ✅ 已完成

### 核心样式恢复

#### 1️⃣ 半透明白框 (tags-panel)

```css
background: rgba(255, 255, 255, 0.5); /* 白色半透明 */
border-radius: 45px; /* 大圆角 */
width: 96%;
max-height: 96%; /* 等比缩小 */

/* 初始：从下方隐藏 */
transform: translate(-50%, -20%) scale(0.88) translateY(80px);

/* 悬停：滑到中心 */
transform: translate(-50%, -50%) scale(0.95) translateZ(30px);
```

#### 2️⃣ 椭圆标签 (tag-item)

```css
padding: 10px 22px;                             /* 椭圆尺寸 */
background: rgba(255, 255, 255, 0.85);          /* 白色背景 */
color: #000000;                                  /* 黑色文字 */
border: 1.5px solid rgba(49, 48, 48, 0.5);      /* 灰色边框 */
border-radius: 30px;                             /* 椭圆形 */
font-size: 16px; font-weight: 600;              /* 字体样式 */
animation: tagPopIn 0.4s ease forwards;          /* 弹出动画 */
animation-delay: ${index * 0.05}s;               /* 逐个出现 */
```

#### 3️⃣ 3D旋转效果 (grid-card)

```css
transform-style: preserve-3d;
transform: perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1);
transform-origin: center 50%;

/* 鼠标移动 */
rotateY = (0.5 - x) * 12;      /* X轴旋转 -12 ~ 12度 */
rotateX = (y - 0.5) * 10;      /* Y轴旋转 -10 ~ 10度 */
scale3d(1.005, 1.005, 1.005);  /* 轻微放大 */
```

#### 4️⃣ 前往按钮 (goto-btn)

```css
padding: 14px 48px; /* 加长按钮 */
background: rgba(240, 240, 240, 0.9); /* 灰白背景 */
color: #000000; /* 黑色文字 */
border: 2px solid rgba(255, 255, 255, 0.95); /* 白色边框 */
border-radius: 24px; /* 大圆角 */
font-size: 18px;
font-weight: 600; /* 字体样式 */
box-shadow: 0 4px 16px rgba(100, 100, 100, 0.35); /* 灰色阴影 */
```

## 🎨 关键动画

### 白框滑入

```
初始 → 悬停
translateY(80px) → translateY(0)
scale(0.88) → scale(0.95)
opacity(0) → opacity(1)
时长: 0.45s cubic-bezier(0.25, 0.46, 0.45, 0.94)
```

### 标签弹出

```
@keyframes tagPopIn {
  from: opacity(0) scale(0.8)
  to: opacity(1) scale(1)
}
延迟: index * 0.05s
```

### 纸飞机浮动

```
@keyframes planeFloat {
  0%, 100%: translateY(3px) rotate(-5deg)
  50%: translateY(0px) rotate(-5deg)
}
时长: 2s infinite
```

## 📐 尺寸规格

| 元素       | 尺寸                  |
| ---------- | --------------------- |
| 卡片宽度   | 89.95%                |
| 卡片宽高比 | 69.94% (458.15×320.5) |
| 白框尺寸   | 96% × 96%             |
| 白框圆角   | 45px                  |
| 标签圆角   | 30px                  |
| 按钮圆角   | 24px                  |
| 标签字体   | 16px / 600            |
| 按钮字体   | 18px / 600            |

## 🎯 颜色方案

| 元素     | 颜色                      |
| -------- | ------------------------- |
| 白框背景 | rgba(255, 255, 255, 0.5)  |
| 标签背景 | rgba(255, 255, 255, 0.85) |
| 标签文字 | #000000 (纯黑)            |
| 标签边框 | rgba(49, 48, 48, 0.5)     |
| 按钮背景 | rgba(240, 240, 240, 0.9)  |
| 按钮文字 | #000000 (纯黑)            |
| 按钮边框 | rgba(255, 255, 255, 0.95) |

## 🔧 修改的文件

### HorizontalPicturesView.vue

- ✅ 替换 `.tags-panel` 样式（居中半透明白框）
- ✅ 替换 `.tag-item` 样式（椭圆黑白标签）
- ✅ 替换 `.goto-btn` 样式（灰白按钮）
- ✅ 替换 `.grid-card` 样式（完整3D效果）
- ✅ 添加响应式媒体查询

## 📱 响应式布局

```
桌面 (>1200px):  3列网格
中屏 (968-1200px): 2列网格
小屏 (768-968px):  2列网格
移动 (<768px):     1列网格
```

## 🧪 快速测试清单

- [ ] 悬停显示白框（从下滑入）
- [ ] 标签为椭圆形黑白样式
- [ ] 标签逐个弹出动画
- [ ] 卡片3D鼠标跟随旋转
- [ ] 按钮为灰白样式
- [ ] 纸飞机浮动动画
- [ ] 响应式布局切换

## 💡 核心差异

| 特征     | 修改前   | 修改后        |
| -------- | -------- | ------------- |
| 白框位置 | 底部     | 居中          |
| 白框背景 | 渐变     | 半透明纯白    |
| 标签背景 | 彩色渐变 | 纯白 + 灰边框 |
| 标签文字 | 深色     | 纯黑          |
| 按钮背景 | 彩色渐变 | 灰白纯色      |
| 按钮文字 | 白色     | 黑色          |
| 图标颜色 | 白色     | 黑色          |
| 动画效果 | 简化     | 完整3D        |

---

**状态**: ✅ 完成
**文档**: HORIZONTAL_STYLE_RESTORED.md
