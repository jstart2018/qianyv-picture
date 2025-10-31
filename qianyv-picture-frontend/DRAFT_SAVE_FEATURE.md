# 博客上传表单 - 草稿自动保存功能

## 📋 功能概述

为博客上传表单新增草稿自动保存功能，防止用户误关闭表单导致数据丢失，提升用户体验。

---

## 🎯 核心特性

### 1. **自动保存**

- ✅ 实时监听表单数据变化
- ✅ 1秒防抖，避免频繁保存
- ✅ 自动保存到 localStorage

### 2. **智能恢复**

- ✅ 打开表单时自动检测草稿
- ✅ 友好提示，显示草稿内容和保存时间
- ✅ 用户确认后恢复数据

### 3. **自动清理**

- ✅ 发布成功后自动清除草稿
- ✅ 用户拒绝恢复时清除草稿
- ✅ 7天过期自动清除

---

## 🔧 技术实现

### 存储方案

```typescript
// localStorage 存储
const DRAFT_KEY = 'blog_upload_draft'
const DRAFT_EXPIRY_DAYS = 7

interface DraftData {
  title: string
  content: string
  uploadedImages: UploadedImage[]
  currentImageIndex: number
  timestamp: number
}
```

### 核心函数

#### 1. **保存草稿**

```typescript
const saveDraft = () => {
  const draftData = {
    title: formData.value.title,
    content: formData.value.content,
    uploadedImages: uploadedImages.value,
    currentImageIndex: currentImageIndex.value,
    timestamp: Date.now(),
  }
  localStorage.setItem(DRAFT_KEY, JSON.stringify(draftData))
}
```

#### 2. **防抖保存**

```typescript
const debounceSaveDraft = () => {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveDraft()
  }, 1000) // 1秒防抖
}
```

#### 3. **加载草稿**

```typescript
const loadDraft = () => {
  const draftStr = localStorage.getItem(DRAFT_KEY)
  if (!draftStr) return null

  const draft = JSON.parse(draftStr)

  // 检查是否过期（7天）
  const daysAgo = (Date.now() - draft.timestamp) / (1000 * 60 * 60 * 24)
  if (daysAgo > DRAFT_EXPIRY_DAYS) {
    clearDraft()
    return null
  }

  return draft
}
```

#### 4. **恢复草稿**

```typescript
const restoreDraft = (draft: any) => {
  formData.value.title = draft.title || ''
  formData.value.content = draft.content || ''
  uploadedImages.value = draft.uploadedImages || []
  currentImageIndex.value = draft.currentImageIndex || 0
}
```

#### 5. **检查并提示**

```typescript
const checkAndRestoreDraft = () => {
  const draft = loadDraft()
  if (!draft) return

  const hasContent =
    draft.title || draft.content || (draft.uploadedImages && draft.uploadedImages.length > 0)
  if (!hasContent) {
    clearDraft()
    return
  }

  const savedTime = new Date(draft.timestamp)
  const timeStr = savedTime.toLocaleString('zh-CN')

  const shouldRestore = confirm(
    `检测到未发布的草稿（保存于 ${timeStr}），是否恢复？\n\n` +
      `标题：${draft.title || '(空)'}\n` +
      `内容：${draft.content ? draft.content.substring(0, 30) + '...' : '(空)'}\n` +
      `图片：${draft.uploadedImages?.length || 0} 张`,
  )

  if (shouldRestore) {
    restoreDraft(draft)
  } else {
    clearDraft()
  }
}
```

### 生命周期集成

#### **弹窗打开时**

```typescript
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      loadCategories()
      checkAndRestoreDraft() // 检查并恢复草稿
    } else {
      // 关闭时保存草稿
      const hasContent =
        formData.value.title || formData.value.content || uploadedImages.value.length > 0
      if (hasContent) {
        saveDraft()
      }
    }
  },
)
```

#### **数据变化监听**

```typescript
watch(
  () => [formData.value.title, formData.value.content, uploadedImages.value.length],
  () => {
    if (props.visible) {
      debounceSaveDraft() // 防抖自动保存
    }
  },
  { deep: true },
)
```

#### **发布成功**

```typescript
if (result && result.code === 0) {
  clearDraft() // 清除草稿
  // ... 重置表单
}
```

---

## 💡 使用场景

### 场景 1：误关闭恢复

```
用户填写：
  标题：我的新博客
  内容：今天天气很好...
  上传：3张图片

误点关闭 → 数据保存到 localStorage

再次打开 → 提示恢复草稿 → 确认 → 数据恢复
```

### 场景 2：多次编辑

```
第一次：
  输入标题 → 自动保存 → 关闭

第二次：
  打开 → 恢复标题 → 继续输入内容 → 关闭

第三次：
  打开 → 恢复全部数据 → 完成发布 → 自动清除草稿
```

### 场景 3：草稿过期

```
保存草稿 → 8天后打开 → 草稿已过期 → 自动清除
```

---

## 🎨 用户体验

### 友好提示

```
检测到未发布的草稿（保存于 2025-10-31 14:30:25），是否恢复？

标题：我的新博客
内容：今天天气很好，我去公园散步...
图片：3 张

[确定]  [取消]
```

### 自动保存反馈

- ⏱️ 输入时实时保存（1秒防抖）
- 💾 无需手动操作
- 🔔 静默保存，不打扰用户

---

## 📊 数据结构

### localStorage 存储格式

```json
{
  "title": "我的新博客",
  "content": "今天天气很好...",
  "uploadedImages": [
    {
      "id": 123,
      "thumbUrl": "https://...",
      "categoryId": 5,
      "tags": ["生活", "摄影"]
    }
  ],
  "currentImageIndex": 0,
  "timestamp": 1730358625000
}
```

### 容量估算

- 标题：25 字符 ≈ 50 bytes
- 内容：300 字符 ≈ 600 bytes
- 图片数据：10 张 × 200 bytes ≈ 2 KB
- **总计：约 3 KB**（localStorage 限制 5-10 MB，绰绰有余）

---

## 🛡️ 边界处理

### 1. 数据校验

```typescript
try {
  const draft = JSON.parse(draftStr)
  // 处理数据
} catch (error) {
  console.error('加载草稿失败:', error)
  return null
}
```

### 2. 过期清理

```typescript
const daysAgo = (Date.now() - draft.timestamp) / (1000 * 60 * 60 * 24)
if (daysAgo > DRAFT_EXPIRY_DAYS) {
  clearDraft()
  return null
}
```

### 3. 空内容处理

```typescript
const hasContent =
  draft.title || draft.content || (draft.uploadedImages && draft.uploadedImages.length > 0)
if (!hasContent) {
  clearDraft()
  return
}
```

### 4. 多标签页隔离

- 使用唯一的 `DRAFT_KEY`
- 不同用户可以通过 key 隔离
- 同一用户多标签页共享草稿

---

## 🚀 未来优化

### 可选功能

- [ ] 多草稿支持（列表管理）
- [ ] 云端同步（跨设备）
- [ ] 草稿版本历史
- [ ] 自动保存状态提示
- [ ] 草稿列表管理界面

### 性能优化

- [x] 防抖保存（已实现）
- [ ] 增量保存（仅保存变更部分）
- [ ] 压缩存储（对大内容压缩）

---

## 📝 测试清单

- [x] 输入内容后关闭，再打开能恢复
- [x] 发布成功后草稿自动清除
- [x] 拒绝恢复后草稿清除
- [x] 草稿过期自动清除
- [x] 无内容时不提示恢复
- [x] 图片数据能正确恢复
- [x] 分类和标签能正确恢复
- [x] 防抖保存正常工作

---

## ✅ 完成状态

所有核心功能已实现并测试通过！🎉

**修改文件：**

- `src/components/BlogUploadModal.vue`

**新增功能：**

1. ✅ 草稿自动保存（防抖 1 秒）
2. ✅ 草稿智能恢复（友好提示）
3. ✅ 草稿自动清理（发布/拒绝/过期）
4. ✅ 完整的边界处理和错误容错

**用户体验：**

- 🎯 无缝体验，自动保存
- 💡 智能提示，清晰明了
- 🛡️ 安全可靠，数据不丢
