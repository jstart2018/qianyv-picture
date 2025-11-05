# 样式系统文档

## 📁 目录结构

```
src/styles/
├── base/
│   ├── variables.css   # CSS 变量定义
│   └── animations.css  # 通用动画
├── components/         # 组件样式（待添加）
└── mixins/            # 混合样式（待添加）
```

## 🎨 CSS 变量使用指南

### 颜色系统

#### 主题色

- `--color-primary`: #1aa0c1（主色调）
- `--color-primary-light`: #7bd3d8（浅色主题）
- `--color-secondary`: #667eea（次要色）

#### 状态色

- `--color-success`: #10b981（成功）
- `--color-error`: #ef4444（错误）
- `--color-warning`: #f59e0b（警告）
- `--color-info`: #3b82f6（信息）

#### 文字颜色

- `--text-primary`: #1a202c（主要文字）
- `--text-secondary`: #4a5568（次要文字）
- `--text-tertiary`: #718096（第三级文字）
- `--text-white`: #ffffff（白色文字）

#### 背景色

- `--bg-primary`: #ffffff（主背景）
- `--bg-card`: rgba(255, 255, 255, 0.95)（卡片背景）
- `--bg-overlay`: rgba(0, 0, 0, 0.6)（遮罩层）
- `--bg-glass`: rgba(255, 255, 255, 0.15)（毛玻璃效果）

### 间距系统

- `--spacing-xs`: 4px
- `--spacing-sm`: 8px
- `--spacing-md`: 16px
- `--spacing-lg`: 24px
- `--spacing-xl`: 32px
- `--spacing-2xl`: 48px
- `--spacing-3xl`: 64px

### 圆角系统

- `--radius-xs`: 4px
- `--radius-sm`: 8px
- `--radius-md`: 12px
- `--radius-lg`: 16px
- `--radius-xl`: 24px
- `--radius-full`: 9999px（完全圆角）

### 阴影系统

- `--shadow-xs`: 超小阴影
- `--shadow-sm`: 小阴影
- `--shadow-md`: 中等阴影
- `--shadow-lg`: 大阴影
- `--shadow-xl`: 超大阴影
- `--shadow-primary`: 主题色阴影
- `--shadow-secondary`: 次要色阴影

### 渐变色

- `--gradient-primary`: 主题渐变
- `--gradient-secondary`: 次要渐变
- `--gradient-warm`: 暖色渐变
- `--gradient-cool`: 冷色渐变

### 过渡效果

- `--transition-fast`: 0.15s（快速）
- `--transition-base`: 0.3s（标准）
- `--transition-slow`: 0.5s（慢速）
- `--transition-all`: all 0.3s ease（通用）

### 字体系统

- `--font-size-xs`: 12px
- `--font-size-sm`: 14px
- `--font-size-base`: 16px
- `--font-size-lg`: 18px
- `--font-size-xl`: 20px
- `--font-size-2xl`: 24px
- `--font-size-3xl`: 30px
- `--font-size-4xl`: 36px

### 字重

- `--font-weight-normal`: 400
- `--font-weight-medium`: 500
- `--font-weight-semibold`: 600
- `--font-weight-bold`: 700

## 🎬 动画类使用指南

### 加载动画

```html
<div class="animate-spin">旋转加载</div>
<div class="animate-pulse">脉冲效果</div>
```

### 渐入动画

```html
<div class="animate-fade-in">渐入</div>
<div class="animate-fade-in-up">向上渐入</div>
<div class="animate-slide-in-left">从左滑入</div>
```

### 缩放动画

```html
<div class="animate-zoom-in">放大渐入</div>
```

### 延迟动画

```html
<div class="animate-fade-in animate-delay-200">延迟200ms</div>
<div class="animate-fade-in animate-delay-500">延迟500ms</div>
```

### 速度控制

```html
<div class="animate-fade-in animate-fast">快速动画</div>
<div class="animate-fade-in animate-slow">慢速动画</div>
```

## 📝 使用示例

### 在组件中使用 CSS 变量

```vue
<template>
  <div class="my-component">
    <button class="primary-btn">点击我</button>
  </div>
</template>

<style scoped>
.my-component {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
}

.primary-btn {
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--gradient-primary);
  color: var(--text-white);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  transition: var(--transition-all);
}

.primary-btn:hover {
  box-shadow: var(--shadow-primary);
  transform: translateY(-2px);
}
</style>
```

### 使用动画类

```vue
<template>
  <div class="card animate-fade-in-up animate-delay-200">
    <h3>标题</h3>
    <p>内容</p>
  </div>
</template>

<style scoped>
.card {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}
</style>
```

## ✅ 已完成的优化

1. ✅ 创建 CSS 变量系统（variables.css）
2. ✅ 创建通用动画系统（animations.css）
3. ✅ 在 main.ts 中导入全局样式
4. ✅ 验证样式变量生效（LoginView.vue 作为示例）

## 🚀 下一步计划

1. 将其他页面的硬编码样式替换为 CSS 变量
2. 提取重复的样式为公共组件
3. 创建更多可复用的样式混合

## 📌 注意事项

1. **保持 scoped style**：所有组件样式保持 `<style scoped>`，避免全局污染
2. **仅使用 CSS 变量**：不创建全局 CSS 类，通过 CSS 变量实现样式复用
3. **语义化命名**：使用清晰的变量名，便于理解和维护
4. **渐进式替换**：逐步将现有组件的硬编码样式替换为 CSS 变量

## 🎯 优势

- ✅ **统一管理**：所有样式值集中在 variables.css 中
- ✅ **易于维护**：修改主题只需改变 CSS 变量值
- ✅ **避免冲突**：保留 scoped style，不会造成样式污染
- ✅ **提高复用**：通过变量复用，减少重复代码
- ✅ **未来扩展**：支持暗色模式等主题切换
