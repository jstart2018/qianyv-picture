# 图片素材页面数据显示修复

## 问题描述

后端 `/picture/list` 接口返回了图片数据，但前端页面没有正确显示图片。

## 问题原因

### 1. **API响应结构问题**

后端返回的数据结构是：

```json
{
  "code": 0,
  "data": {
    "records": [...],
    "total": "20",  // 注意：是字符串而不是数字
    ...
  },
  "message": "ok"
}
```

前端使用的 `request.ts` 拦截器直接返回了 `response` 对象，所以需要通过 `res.data.data` 才能访问到实际的数据，而不是 `res.data`。

### 2. **数据类型不匹配**

- `id`: 后端返回的是字符串 `"1976271616420237313"`，而不是数字
- `collectCount`: 后端返回的是字符串 `"0"`，而不是数字
- `total`: 后端返回的是字符串 `"20"`，而不是数字

## 解决方案

### 修改 `PicturesView.vue`

#### 1. 修复获取分类列表的数据访问

```typescript
// 修改前
if (res.code === 0 && res.data) {
  categories.value = res.data
}

// 修改后
if (res.data.code === 0 && res.data.data) {
  categories.value = res.data.data
}
```

#### 2. 修复获取图片列表的数据访问和类型转换

```typescript
// 修改前
const res = await getPictureList(params)
if (res.code === 0 && res.data) {
  pictures.value = res.data.records || []
  total.value = res.data.total || 0
}

// 修改后
const res = await getPictureList(params)
if (res.data.code === 0 && res.data.data) {
  const pageData = res.data.data
  pictures.value = pageData.records || []
  // 字符串转数字
  total.value = typeof pageData.total === 'string' ? parseInt(pageData.total) : pageData.total || 0
}
```

#### 3. 修复图片ID参数类型

```typescript
// 修改前
const openPictureDetail = (pictureId: number | undefined) => {
  ...
}

// 修改后（支持字符串ID）
const openPictureDetail = (pictureId: number | string | undefined) => {
  ...
}
```

#### 4. 优化收藏数显示（处理字符串类型）

```vue
<!-- 修改前 -->
{{ pic.collectCount || 0 }}

<!-- 修改后 -->
{{ typeof pic.collectCount === 'string' ? parseInt(pic.collectCount) : pic.collectCount || 0 }}
```

#### 5. 优化标签显示（处理null值）

```vue
<!-- 修改前 -->
<span class="picture-tags">{{ pic.tags }}</span>

<!-- 修改后（tags为null时不显示） -->
<span v-if="pic.tags" class="picture-tags">{{ pic.tags }}</span>
```

#### 6. 修复瀑布流布局的安全检查

```typescript
// 添加安全检查，避免可能的undefined错误
if (cols[minHeightIndex]) {
  cols[minHeightIndex].push(pic)
  colHeights[minHeightIndex] += 1
}
```

## 测试验证

刷新浏览器访问"图片素材"页面，应该能看到：

1. ✅ 图片列表正常显示
2. ✅ 瀑布流布局正常工作
3. ✅ 分类筛选功能正常
4. ✅ 搜索功能正常
5. ✅ 分页加载正常
6. ✅ 收藏数正确显示
7. ✅ 点击图片可以跳转到详情页

## 注意事项

1. **后端数据类型不一致**：后端返回的很多字段是字符串类型，前端需要做类型转换
2. **API响应结构**：由于 `request.ts` 拦截器的设计，需要通过 `res.data.data` 访问实际数据
3. **null值处理**：某些图片的 `tags` 字段为 `null`，需要条件渲染避免显示 "null" 文本

## 相关文件

- `src/views/PicturesView.vue` - 图片素材页面主文件
- `src/api/pictureController.ts` - 图片API接口
- `src/api/picCategoryController.ts` - 分类API接口
- `src/request.ts` - Axios请求封装

## 日期

2025年10月31日
