# 工具函数文档

## 📁 目录结构

```
src/utils/
├── format/           # 格式化工具
│   ├── fileSize.ts   # 文件大小格式化
│   ├── date.ts       # 日期格式化
│   └── index.ts      # 统一导出
├── parse/            # 解析工具
│   ├── tags.ts       # 标签解析
│   └── index.ts      # 统一导出
├── validate/         # 验证工具
│   ├── file.ts       # 文件验证
│   └── index.ts      # 统一导出
└── index.ts          # 总入口
```

## 📝 使用指南

### 格式化工具

#### formatFileSize - 文件大小格式化

```typescript
import { formatFileSize } from '@/utils/format'

formatFileSize(1024) // "1.00 KB"
formatFileSize(1048576) // "1.00 MB"
formatFileSize(0) // "0 B"
```

#### formatDate - 日期格式化

```typescript
import { formatDate } from '@/utils/format'

formatDate('2024-01-15', 'date') // "2024-01-15"
formatDate('2024-01-15', 'datetime') // "2024-01-15 00:00"
formatDate('2024-01-15', 'full') // "2024-01-15 00:00:00"
```

#### formatRelativeTime - 相对时间

```typescript
import { formatRelativeTime } from '@/utils/format'

formatRelativeTime('2024-01-15') // "5分钟前" / "2小时前" / "3天前"
```

### 解析工具

#### parseTags - 标签解析

```typescript
import { parseTags } from '@/utils/parse'

parseTags(['标签1', '标签2']) // ['标签1', '标签2']
parseTags('["标签1", "标签2"]') // ['标签1', '标签2']
parseTags('标签1,标签2') // ['标签1', '标签2']
parseTags(null) // []
```

#### tagsToString - 标签转字符串

```typescript
import { tagsToString } from '@/utils/parse'

tagsToString(['标签1', '标签2']) // "标签1,标签2"
```

### 验证工具

#### validateImageFile - 验证图片文件

```typescript
import { validateImageFile } from '@/utils/validate'

const file = new File(['...'], 'image.jpg', { type: 'image/jpeg' })
const result = validateImageFile(file)

if (result.valid) {
  // 文件有效
} else {
  console.error(result.error) // "文件过大，最大支持 30MB"
}
```

#### validateFileSize - 验证文件大小

```typescript
import { validateFileSize } from '@/utils/validate'

const result = validateFileSize(file)
if (!result.valid) {
  alert(result.error)
}
```

## 🎯 优化前 vs 优化后

### 优化前（重复代码）

每个文件都定义自己的工具函数：

```typescript
// HorizontalPictureDetailView.vue
const parseTags = (tagsData: string | null): string[] => {
  if (!tagsData) return []
  try {
    if (typeof tagsData === 'string' && tagsData.startsWith('[')) {
      return JSON.parse(tagsData)
    }
    // ... 20+ 行重复代码
  } catch (err) {
    return []
  }
}

// VerticalPictureDetailView.vue
const parseTags = (tagsData: string | null): string[] => {
  // ... 完全相同的代码
}

// HorizontalPicturesView.vue
const parseTags = (tagsData: string | null): string[] => {
  // ... 完全相同的代码
}
```

### 优化后（统一工具函数）

```typescript
// 所有组件统一导入
import { parseTags } from '@/utils/parse'
import { formatFileSize, formatDate } from '@/utils/format'
```

## ✅ 已优化的组件

1. ✅ HorizontalPictureDetailView.vue
   - 移除 parseTags 定义（25行）
   - 移除 formatFileSize 定义（8行）
   - 移除 formatDate 定义（10行）
   - 添加统一导入

2. ✅ VerticalPictureDetailView.vue
   - 移除 parseTags 定义（25行）
   - 移除 formatFileSize 定义（8行）
   - 移除 formatDate 定义（10行）
   - 添加统一导入

3. ✅ HorizontalPicturesView.vue
   - 移除 parseTags 定义（17行）
   - 添加统一导入

## 📊 优化统计

- **减少重复代码**：约 100+ 行
- **提取工具函数**：8个
- **提取常量定义**：3组
- **统一导入方式**：从 `@/utils` 导入

## 🚀 后续扩展

可以继续添加更多工具函数：

- 字符串处理（截断、高亮等）
- 数字格式化（千分位、百分比等）
- URL 处理
- 缓存管理
- 防抖节流
- ...
