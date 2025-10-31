# 博客上传功能 - 快速指南

## 🎯 功能入口

**位置：** 分享社区页面（HomeView）→ 左侧用户卡片 → "分享"按钮

```
点击"分享"按钮 → 弹出上传弹窗
```

---

## 📝 使用步骤

### 1. 选择帖子类型

- 📋 **分享**：分享内容、经验
- 📅 **日常**：记录日常生活
- 🙋 **求助**：寻求帮助

### 2. 填写标题（必填）

- 最多 25 个字符
- 实时显示剩余字数

### 3. 填写内容（选填）

- 最多 300 个字符
- 实时显示剩余字数

### 4. 上传图片（必填）

- 点击上传区域选择文件
- 支持批量选择多张图片
- 至少上传一张图片

### 5. 点击发布

- 标题和图片齐全后按钮可点击
- 发布成功自动关闭弹窗
- 自动刷新博客列表

---

## 📸 图片上传说明

### 支持格式

```
图片：png, jpg, jpeg
视频：mp4, avi, mov, m4v, flv, wmv
```

### 大小限制

```
图片：最大 50MB
视频：最大 500MB
```

### 上传方式

```
✅ 点击上传区域
✅ 批量选择多张
⏳ 拖拽上传（待完善）
```

---

## 🔑 关键API

### 图片上传

```typescript
POST /picture/uploadByFile
参数：{ spaceId: undefined, file: File }
返回：{ id: number, thumbUrl: string }
```

### 博客发布

```typescript
POST /blog/add
参数：{
  title: string,
  content?: string,
  pictureEditDTOList: [{ id: number }]
}
返回：{ code: 0, data: blogId }
```

---

## ⚠️ 注意事项

### 1. 必填项

- ✅ 标题（最多25字）
- ✅ 至少一张图片
- ❌ 内容（可选）

### 2. 发布条件

```javascript
canPublish = 标题非空 && 至少有一张图片
```

### 3. 上传流程

```
选择文件 → 验证大小 → 上传接口 → 获取ID → 保存预览
```

### 4. 发布流程

```
填写信息 → 上传图片 → 构造数据 → 调用接口 → 刷新列表
```

---

## 🎨 界面特点

### 弹窗

- 白色半透明背景
- 毛玻璃效果
- 滑入动画
- 自适应高度

### 图片预览

- 网格布局
- 显示序号
- 悬停删除
- 1:1 比例

### 表单

- 实时字符计数
- 输入框聚焦效果
- 按钮状态切换
- 加载动画

---

## 🐛 常见问题

### Q: 点击分享按钮没反应？

**A:** 检查用户是否已登录，未登录用户不能发布博客。

### Q: 上传图片失败？

**A:** 检查：

- 文件格式是否支持
- 文件大小是否超限
- 网络连接是否正常

### Q: 发布按钮禁用？

**A:** 确保：

- 标题已填写
- 至少上传一张图片

### Q: 发布成功但列表没更新？

**A:** 检查 `handlePublishSuccess` 是否被正确调用。

---

## 🔧 开发者信息

### 文件位置

```
src/components/BlogUploadModal.vue  // 弹窗组件
src/views/HomeView.vue              // 集成页面
src/api/pictureController.ts        // 图片上传API
src/api/blogController.ts           // 博客发布API
```

### 核心函数

```typescript
uploadFile(file: File)              // 上传单个文件
handlePublish()                     // 发布博客
handlePublishSuccess()              // 发布成功回调
```

### 状态管理

```typescript
showUploadModal: boolean            // 控制弹窗显示
formData: { title, content }        // 表单数据
uploadedImages: UploadedImage[]     // 已上传图片
isPublishing: boolean               // 发布中状态
```

---

## 📊 数据流

### 上传图片

```
用户选择文件
  ↓
遍历文件列表
  ↓
验证文件大小
  ↓
调用 upload() 接口
  ↓
保存 { id, thumbUrl }
  ↓
显示预览图
```

### 发布博客

```
用户点击发布
  ↓
构造博客数据
  {
    title: '标题',
    content: '内容',
    pictureEditDTOList: [{ id: 123 }]
  }
  ↓
调用 addBlog() 接口
  ↓
检查返回 code === 0
  ↓
关闭弹窗 + 刷新列表
```

---

## ✅ 功能清单

### 已实现

- [x] 弹窗组件创建
- [x] 帖子类型选择
- [x] 标题输入（字符限制）
- [x] 内容输入（字符限制）
- [x] 图片上传（单张/批量）
- [x] 图片预览
- [x] 图片删除
- [x] 文件大小验证
- [x] 表单验证
- [x] 发布功能
- [x] 加载状态
- [x] 刷新列表
- [x] 弹窗动画

### 待优化

- [ ] Toast 消息提示
- [ ] 拖拽上传
- [ ] 上传进度条
- [ ] 图片编辑
- [ ] 草稿保存

---

## 🚀 快速测试

### 1. 打开弹窗

```javascript
// 点击分享按钮
document.querySelector('.share-btn').click()
```

### 2. 填写数据

```javascript
// 控制台模拟
showUploadModal.value = true
formData.value.title = '测试标题'
formData.value.content = '测试内容'
```

### 3. 上传图片

```javascript
// 手动选择文件
fileInputRef.value?.click()
```

### 4. 发布博客

```javascript
// 点击发布按钮
handlePublish()
```

---

## 💡 最佳实践

### 1. 图片优化

```
建议上传前压缩图片
推荐尺寸：1920x1080 以下
推荐格式：JPG（更小）
```

### 2. 标题撰写

```
简洁明了
突出重点
避免特殊字符
```

### 3. 内容排版

```
分段清晰
使用emoji增加趣味
```

### 4. 图片数量

```
建议 1-9 张
排版更美观
加载更快
```

---

**最后更新：** 2025年10月31日  
**状态：** ✅ 开发完成  
**详细文档：** BLOG_UPLOAD_FEATURE.md
