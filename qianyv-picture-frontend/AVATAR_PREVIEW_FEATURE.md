# 头像预览功能说明

## 功能概述

为千语壁纸系统添加了头像点击放大预览功能，用户可以点击头像查看大图和详细信息。

## 新增文件

- `src/components/AvatarPreview.vue` - 头像预览弹窗组件

## 修改文件

- `src/views/HomeView.vue` - 添加头像预览功能

## 功能特点

### 1. **头像预览组件 (AvatarPreview.vue)**

- 点击任意头像可打开全屏预览弹窗
- 支持有头像和无头像两种展示方式
- 显示用户昵称和个人简介
- 毛玻璃背景效果，提升视觉体验
- 点击遮罩层或关闭按钮可关闭预览

### 2. **HomeView.vue 改动**

#### 新增状态管理

```typescript
// 头像预览相关
const avatarPreviewVisible = ref(false)
const previewAvatarData = ref({
  avatarUrl: '',
  nickname: '',
  introduction: '',
})
```

#### 新增方法

```typescript
// 打开头像预览
const openAvatarPreview = (
  avatarUrl: string | undefined,
  nickname: string | undefined,
  introduction: string | undefined,
) => {
  previewAvatarData.value = {
    avatarUrl: avatarUrl || '',
    nickname: nickname || '',
    introduction: introduction || '',
  }
  avatarPreviewVisible.value = true
}
```

### 3. **支持的头像位置**

- **左侧用户卡片头像**：点击可预览当前登录用户的头像
- **博客作者头像**：点击可预览博客作者的头像

### 4. **样式特点**

- **头像悬停效果**：鼠标悬停时头像放大 1.05 倍，显示指针
- **平滑过渡动画**：所有交互都有 0.3s 的过渡动画
- **响应式设计**：支持移动端显示

### 5. **组件属性**

```typescript
interface Props {
  visible: boolean // 是否显示预览
  avatarUrl?: string // 头像URL（可选）
  nickname?: string // 用户昵称（可选）
  introduction?: string // 个人简介（可选）
}
```

### 6. **组件事件**

```typescript
emit('update:visible', false) // 更新显示状态
```

## 使用方法

### 在 HomeView.vue 中使用

```vue
<!-- 1. 引入组件 -->
<script setup lang="ts">
import AvatarPreview from '@/components/AvatarPreview.vue'
</script>

<!-- 2. 为头像添加点击事件 -->
<div
  class="user-avatar-large"
  @click="
    openAvatarPreview(
      userStore.user?.avatar,
      userStore.user?.nickname,
      userStore.user?.introduction,
    )
  "
>
  <img :src="userStore.user.avatar" alt="avatar" />
</div>

<!-- 3. 添加预览组件 -->
<AvatarPreview
  v-model:visible="avatarPreviewVisible"
  :avatarUrl="previewAvatarData.avatarUrl"
  :nickname="previewAvatarData.nickname"
  :introduction="previewAvatarData.introduction"
/>
```

## 视觉效果

### 头像悬停状态

- 鼠标指针变为手型（cursor: pointer）
- 头像放大 5%（transform: scale(1.05)）
- 阴影增强

### 预览弹窗

- **背景**：黑色半透明遮罩 + 毛玻璃效果
- **容器**：白色半透明背景 + 圆角卡片
- **头像**：300px × 300px 圆形显示
- **关闭按钮**：右上角圆形按钮，悬停时旋转 90 度

### 动画效果

- **入场**：从 0.9 倍缩放到 1 倍，透明度从 0 到 1
- **出场**：从 1 倍缩放到 0.9 倍，透明度从 1 到 0
- **时长**：0.3 秒

## 技术实现

### 1. Teleport 传送门

使用 Vue 3 的 Teleport 功能将弹窗挂载到 body 下，避免 z-index 层级问题。

### 2. 双向绑定

使用 `v-model:visible` 实现父子组件之间的状态同步。

### 3. 阻止滚动

预览打开时禁用 body 滚动，关闭时恢复。

### 4. 事件冒泡控制

- 点击遮罩层关闭预览
- 点击内容区域阻止事件冒泡（@click.stop）

## 样式细节

### CSS 特性

- **backdrop-filter**：毛玻璃模糊效果
- **box-shadow**：多层阴影增强立体感
- **transition**：平滑过渡动画
- **text-shadow**：文字阴影提升可读性

### 响应式断点

- **768px 以下**：
  - 容器宽度 90%
  - 头像尺寸 200px × 200px
  - 字体大小相应缩小

## 浏览器兼容性

- 支持现代浏览器（Chrome、Firefox、Safari、Edge）
- 使用 `-webkit-` 前缀支持 Safari 浏览器

## 后续优化建议

1. 可以添加图片加载失败的占位图
2. 可以添加头像切换功能
3. 可以添加更多用户信息展示
4. 可以添加图片缩放功能

## 测试要点

- ✅ 左侧用户卡片头像点击
- ✅ 博客作者头像点击
- ✅ 有头像时的显示效果
- ✅ 无头像时的占位符显示
- ✅ 关闭按钮功能
- ✅ 点击遮罩层关闭
- ✅ 移动端响应式显示
- ✅ 动画效果流畅性
