# 博客上传功能开发文档

## 开发日期

2025年10月31日

## 功能概述

在分享社区页面（HomeView）中实现博客上传功能。用户点击左侧用户卡片中的"分享"按钮后，弹出白色半透明的表单弹窗，用户可以填写博客信息并上传多张图片，最后发布博客。

---

## 文件变更

### 1. 新建文件

#### `src/components/BlogUploadModal.vue`

博客上传弹窗组件，包含完整的上传表单和图片上传功能。

**主要功能：**

- 博客类型选择（分享/日常/求助）
- 标题输入（最多25个字符）
- 内容输入（最多300个字符，非必填）
- 多图片上传（支持批量上传）
- 图片预览和删除
- 发布功能

---

### 2. 修改文件

#### `src/views/HomeView.vue`

**修改内容：**

1. 导入 `BlogUploadModal` 组件
2. 添加 `showUploadModal` 状态控制弹窗显示
3. 修改 `handleShare` 函数，打开上传弹窗
4. 添加 `handlePublishSuccess` 回调，发布成功后刷新博客列表
5. 在模板中添加 `<BlogUploadModal>` 组件

---

## API 接口说明

### 1. 图片上传接口

**接口：** `POST /picture/uploadByFile`

**导入：**

```typescript
import { upload } from '@/api/pictureController'
```

**参数：**

```typescript
params: {
  spaceId?: number  // 空间ID，不传则为 undefined
}
body: FormData {
  file: File  // 要上传的文件
}
```

**返回：**

```typescript
{
  code: number
  data: {
    id: number // 图片ID（用于后续发布博客）
    thumbUrl: string // 缩略图URL（用于预览展示）
  }
  message: string
}
```

**使用示例：**

```typescript
const formData = new FormData()
formData.append('file', file)

const response = await upload({ spaceId: undefined }, formData as any, {
  headers: {
    'Content-Type': 'multipart/form-data',
  },
})
```

---

### 2. 博客发布接口

**接口：** `POST /blog/add`

**导入：**

```typescript
import { addBlog } from '@/api/blogController'
```

**参数：**

```typescript
{
  title: string                    // 博客标题（必填）
  content?: string                 // 博客内容（选填）
  pictureEditDTOList: Array<{      // 图片列表（必填，至少一张）
    id: number                     // 图片ID（来自上传接口返回）
  }>
}
```

**返回：**

```typescript
{
  code: number // 0 表示成功
  data: number // 返回博客ID
  message: string
}
```

**使用示例：**

```typescript
const blogData = {
  title: '我的博客标题',
  content: '博客内容...',
  pictureEditDTOList: [{ id: 123 }, { id: 456 }],
}

const response = await addBlog(blogData)
```

---

## 组件接口

### BlogUploadModal.vue

#### Props

```typescript
interface Props {
  visible: boolean // 控制弹窗显示/隐藏
}
```

#### Emits

```typescript
{
  'update:visible': (value: boolean) => void  // 更新显示状态
  'success': () => void                       // 发布成功回调
}
```

#### 使用示例

```vue
<BlogUploadModal v-model:visible="showUploadModal" @success="handlePublishSuccess" />
```

---

## 功能流程

### 1. 打开上传弹窗

```
用户点击"分享"按钮
  ↓
触发 handleShare()
  ↓
设置 showUploadModal = true
  ↓
BlogUploadModal 组件显示
```

### 2. 上传图片

```
用户点击上传区域 或 选择文件
  ↓
触发 handleFileChange()
  ↓
遍历所有选中的文件
  ↓
对每个文件调用 uploadFile()
  ↓
验证文件大小（图片≤50MB，视频≤500MB）
  ↓
创建 FormData 并添加文件
  ↓
调用 upload() 接口
  ↓
接收返回的 { id, thumbUrl }
  ↓
添加到 uploadedImages 数组
  ↓
在界面显示预览图
```

### 3. 发布博客

```
用户填写标题和内容
  ↓
上传至少一张图片
  ↓
点击"发布"按钮
  ↓
触发 handlePublish()
  ↓
构造博客数据：
  - title: 标题
  - content: 内容
  - pictureEditDTOList: [{ id: 图片ID }]
  ↓
调用 addBlog() 接口
  ↓
发布成功
  ↓
重置表单
  ↓
关闭弹窗
  ↓
触发 @success 事件
  ↓
父组件刷新博客列表
```

---

## 界面特性

### 1. 弹窗样式

- 白色半透明背景（`rgba(255, 255, 255, 0.95)`）
- 毛玻璃效果（`backdrop-filter: blur(20px)`）
- 圆角边框（`border-radius: 24px`）
- 优雅的阴影效果
- 滑入动画

### 2. 帖子类型

- **分享**：默认选中，用于分享内容
- **日常**：记录日常生活
- **求助**：寻求帮助

### 3. 表单验证

- **标题**：必填，最多25个字符
- **内容**：选填，最多300个字符
- **图片**：必须上传至少一张

### 4. 文件上传

- **支持格式**：
  - 图片：png, jpg, jpeg
  - 视频：mp4, avi, mov, m4v, flv, wmv
- **大小限制**：
  - 图片：最大 50MB
  - 视频：最大 500MB
- **上传方式**：
  - 点击上传
  - 拖拽上传（UI支持，功能待完善）
  - 批量上传

### 5. 图片管理

- 预览已上传的图片
- 显示图片序号（第1张、第2张...）
- 悬停显示删除按钮
- 点击删除按钮移除图片

---

## 交互细节

### 1. 字符计数

- 实时显示剩余字符数
- 标题：剩余字数 = 25 - 当前长度
- 内容：剩余字数 = 300 - 当前长度

### 2. 发布按钮状态

- **可发布**：标题非空 且 至少有一张图片
- **禁用**：标题为空 或 没有图片
- **发布中**：显示加载动画，禁止重复点击

### 3. 页面滚动控制

- 弹窗打开：禁止页面滚动（`body.overflow = 'hidden'`）
- 弹窗关闭：恢复页面滚动（`body.overflow = ''`）

### 4. 表单重置

- 关闭弹窗时自动重置所有表单数据
- 包括：标题、内容、已上传图片、帖子类型

---

## 响应式设计

### 弹窗尺寸

- 最大宽度：900px
- 最大高度：90vh
- 内边距：32px
- 可滚动（当内容超出高度时）

### 图片网格

- 响应式网格布局
- 每列最小宽度：120px
- 自动调整列数
- 保持1:1宽高比

---

## 错误处理

### 1. 文件上传失败

```typescript
try {
  // 上传文件
} catch (error) {
  console.error('上传失败:', error)
  alert(`${file.name} 上传失败`)
}
```

### 2. 博客发布失败

```typescript
try {
  // 发布博客
} catch (error) {
  console.error('发布失败:', error)
  alert('发布失败，请重试')
} finally {
  isPublishing.value = false
}
```

### 3. 文件大小验证

```typescript
const maxSize = file.type.startsWith('image/')
  ? 50 * 1024 * 1024 // 50MB
  : 500 * 1024 * 1024 // 500MB

if (file.size > maxSize) {
  alert('文件过大')
  return
}
```

---

## 样式亮点

### 1. 玻璃态设计

```css
background: rgba(255, 255, 255, 0.95);
backdrop-filter: blur(20px);
border: 1px solid rgba(255, 255, 255, 0.5);
```

### 2. 优雅的动画

```css
@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
```

### 3. 悬停效果

- 按钮悬停：`transform: translateY(-2px)`
- 图片悬停：显示删除按钮
- 上传区域悬停：边框颜色改变

### 4. 自定义滚动条

```css
.modal-container::-webkit-scrollbar {
  width: 6px;
}

.modal-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}
```

---

## 开发注意事项

### 1. FormData 上传

```typescript
const formData = new FormData()
formData.append('file', file)

// 必须设置正确的 headers
headers: {
  'Content-Type': 'multipart/form-data',
}
```

### 2. spaceId 参数

- 社区首页上传时 `spaceId` 不传（undefined）
- 团队空间上传时需要传递对应的 spaceId

### 3. 响应数据结构

```typescript
// API 返回可能是嵌套结构
const result = response.data || response
if (result && result.code === 0) {
  // 成功处理
}
```

### 4. 图片ID保存

```typescript
// 上传成功后保存图片ID和缩略图URL
uploadedImages.value.push({
  id: response.data.id, // 用于发布
  thumbUrl: response.data.thumbUrl, // 用于预览
})
```

---

## 测试清单

### 功能测试

- [ ] 点击"分享"按钮打开弹窗
- [ ] 选择不同的帖子类型
- [ ] 输入标题和内容
- [ ] 字符计数正确显示
- [ ] 单张图片上传
- [ ] 多张图片批量上传
- [ ] 删除已上传的图片
- [ ] 文件大小验证
- [ ] 发布按钮禁用/启用状态
- [ ] 发布成功后刷新列表
- [ ] 关闭弹窗重置表单

### 交互测试

- [ ] 点击遮罩层关闭弹窗
- [ ] 点击关闭按钮关闭弹窗
- [ ] 弹窗打开时页面不可滚动
- [ ] 弹窗关闭后页面恢复滚动
- [ ] 图片悬停显示删除按钮
- [ ] 上传区域悬停效果
- [ ] 发布按钮加载动画

### 异常测试

- [ ] 上传失败的错误提示
- [ ] 发布失败的错误提示
- [ ] 文件过大的提示
- [ ] 网络异常处理

---

## 后续优化建议

### 1. 消息提示组件

- 替换 `alert()` 为更优雅的 Toast 组件
- 添加加载状态提示
- 添加进度条显示

### 2. 拖拽上传

- 实现真正的拖拽上传功能
- 添加拖拽区域高亮效果

### 3. 图片编辑

- 添加图片裁剪功能
- 添加图片滤镜
- 添加图片标签输入

### 4. 草稿保存

- 自动保存草稿
- 从草稿恢复

### 5. 表单验证

- 更详细的验证规则
- 实时验证提示
- 友好的错误提示

---

## 总结

✅ **已完成功能：**

- 博客上传弹窗组件
- 图片上传功能（单张/批量）
- 博客发布功能
- 表单验证
- 状态管理
- 交互动画

✅ **集成完成：**

- HomeView 中集成上传功能
- 分享按钮触发弹窗
- 发布成功刷新列表

---

**开发完成时间：** 2025年10月31日  
**开发者：** GitHub Copilot  
**状态：** ✅ 功能完成，待测试
