# 图片子路由重构 - 快速参考 🚀

## 📁 文件结构

```
src/views/
├── PicturesView.vue              # 父容器（筛选栏 + 路由出口）
├── HorizontalPicturesView.vue    # 横屏子页面（3D网格）
├── VerticalPicturesView.vue      # 竖屏子页面（瀑布流）
└── PicturesView_OLD_BACKUP.vue   # 原始备份
```

## 🛣️ 路由配置

```typescript
/pictures → 重定向到 /pictures/horizontal
  ├── /pictures/horizontal (横屏风格)
  └── /pictures/vertical   (竖屏风格)
```

## 🔄 数据流

```
PicturesView.vue (父组件)
  provide: { selectedCategoryId, searchText }
     ↓
HorizontalPicturesView.vue / VerticalPicturesView.vue (子组件)
  inject: { selectedCategoryId, searchText }
  watch: 监听变化 → 重新加载数据
```

## 🎨 两种风格对比

| 特性     | 横屏风格       | 竖屏风格       |
| -------- | -------------- | -------------- |
| 布局     | 3列网格        | 4列瀑布流      |
| 交互效果 | 3D鼠标跟随旋转 | 整体放大 1.05x |
| 悬停展示 | 标签白框面板   | 底部按钮淡入   |
| 图片比例 | 16:9 固定      | 自适应高度     |
| API参数  | pictureType: 0 | pictureType: 1 |

## 💻 核心代码片段

### 父组件 - 数据共享

```typescript
// PicturesView.vue
provide('selectedCategoryId', selectedCategoryId)
provide('searchText', searchText)

const handlePictureTypeChange = (type: 'horizontal' | 'vertical') => {
  router.push({ name: type === 'horizontal' ? 'picturesHorizontal' : 'picturesVertical' })
}
```

### 子组件 - 接收数据

```typescript
// HorizontalPicturesView.vue / VerticalPicturesView.vue
const selectedCategoryId = inject<any>('selectedCategoryId', ref(null))
const searchText = inject<any>('searchText', ref(''))

watch([selectedCategoryId, searchText], () => {
  current.value = 1
  fetchPictures()
})
```

### 横屏 - 3D效果

```typescript
const handleMouseMove = (event: MouseEvent) => {
  const card = event.currentTarget as HTMLElement
  const rect = card.getBoundingClientRect()
  const x = (event.clientX - rect.left) / rect.width
  const y = (event.clientY - rect.top) / rect.height
  const rotateY = (0.5 - x) * 12
  const rotateX = (y - 0.5) * 10
  card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`
}
```

## 🔍 关键特性

### ✅ 已实现

- 路由独立控制
- 数据双向绑定
- 筛选条件共享
- 独立分页加载
- 原有交互效果保留
- 响应式布局

### 🎯 优势

- 代码解耦，易维护
- 路由控制更灵活
- 避免切换闪烁
- 页面状态独立
- 可独立添加过渡动画

## 📊 API参数

```typescript
// 横屏
{
  pictureType: 0,
  categoryId: number | undefined,
  searchText: string | undefined,
  current: number,
  pageSize: number
}

// 竖屏
{
  pictureType: 1,  // 唯一区别
  categoryId: number | undefined,
  searchText: string | undefined,
  current: number,
  pageSize: number
}
```

## 🐛 常见问题

### Q: 切换时筛选条件丢失？

**A**: 检查 `provide/inject` 是否正确设置，确保子组件使用 `watch` 监听变化。

### Q: 滑块位置不正确？

**A**: 在路由跳转后调用 `nextTick(() => updateSlider())`。

### Q: 图片不显示？

**A**: 检查 API 参数 `pictureType` 是否正确（0=横屏, 1=竖屏）。

## 🚀 快速启动

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问页面
http://localhost:5173/pictures
```

## 📝 测试清单

- [ ] 访问 `/pictures` 自动跳转到横屏
- [ ] 横屏3D旋转效果正常
- [ ] 竖屏放大效果正常
- [ ] 切换按钮滑块动画正常
- [ ] 分类筛选在两个页面都生效
- [ ] 搜索功能在两个页面都生效
- [ ] 加载更多功能正常
- [ ] 图片详情跳转正常

## 📚 相关文档

- 详细报告: `PICTURE_SUBROUTE_REFACTOR.md`
- 原方案: `PICTURE_TRANSITION_SIMPLE.md`
- 延迟切换: `DELAYED_LAYOUT_SWITCH.md`

---

**最后更新**: 2025年11月1日  
**状态**: ✅ 可用
