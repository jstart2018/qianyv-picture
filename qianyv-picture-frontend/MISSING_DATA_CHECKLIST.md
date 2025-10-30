# 千语壁纸系统 - 缺失数据检查清单

> 创建时间：2025年10月30日  
> 说明：本文档记录了社区首页功能中使用静态数据代替后端数据的部分

---

## 一、用户统计信息

### 1.1 左侧用户信息卡片统计数据

**位置**: `HomeView.vue` - 左侧面板 `.user-info-card`

**缺失接口**: 用户统计信息接口（建议路径：`/user/stats`）

**当前静态数据**:

```typescript
const userStats = ref({
  postCount: 128, // 发布数
  likeCount: 3456, // 点赞数
  collectCount: 892, // 收藏数
})
```

**需要的后端返回结构**:

```typescript
interface UserStatsVO {
  postCount: number // 用户发布的博客总数
  likeCount: number // 用户获得的点赞总数
  collectCount: number // 用户获得的收藏总数
}
```

**替换位置**:

- 文件：`src/views/HomeView.vue`
- 变量：`userStats`
- 操作：在 `onMounted` 中调用接口获取真实数据

---

## 二、排行榜数据

### 2.1 近期分享者排行榜

**位置**: `HomeView.vue` - 右侧面板 `.ranking-card`

**缺失接口**: 排行榜接口（建议路径：`/user/ranking` 或 `/blog/top-contributors`）

**当前静态数据**:

```typescript
const rankingList = ref([
  {
    id: 1,
    nickname: '壁纸达人',
    avatar: '',
    postCount: 356,
    fansCount: 1234,
    recentShareCount: 28,
  },
  {
    id: 2,
    nickname: '设计师小王',
    avatar: '',
    postCount: 298,
    fansCount: 987,
    recentShareCount: 24,
  },
  // ... 共8条数据
])
```

**需要的后端返回结构**:

```typescript
interface RankingUserVO {
  id: number // 用户ID
  nickname: string // 用户昵称
  avatar: string // 用户头像URL
  postCount: number // 总帖子数
  fansCount: number // 粉丝数
  recentShareCount: number // 近期分享数（如最近7天或30天）
}

interface RankingListResponse {
  code: number
  data: RankingUserVO[]
  message: string
}
```

**替换位置**:

- 文件：`src/views/HomeView.vue`
- 变量：`rankingList`
- 操作：在 `onMounted` 中调用接口获取真实数据

**建议参数**:

- `limit`: number - 返回数量（默认8）
- `timeRange`: string - 时间范围（如 '7d', '30d', 'all'）
- `sortBy`: string - 排序依据（如 'recentShareCount', 'postCount', 'fansCount'）

---

## 三、用户头像默认显示

### 3.1 用户头像缺失处理

**位置**: 多个位置（用户信息卡片、博客作者头像、排行榜头像）

**当前方案**: 使用用户昵称首字母+渐变背景色显示

**实现函数**:

```typescript
const getUserAvatarText = (nickname?: string) => {
  if (!nickname) return '?'
  return nickname.charAt(0).toUpperCase()
}
```

**应用位置**:

- 左侧用户信息卡片：`.user-avatar-large`
- 博客列表作者头像：`.author-avatar`
- 排行榜用户头像：`.rank-avatar`

**注意事项**:

- 所有头像都使用 `:class="{ 'has-image': user.avatar }"` 来判断是否有图片
- 无图片时显示渐变背景+首字母
- 有图片时显示实际头像

---

## 四、评论功能

### 4.1 评论列表显示

**位置**: `HomeView.vue` - 博客项底部 `.comment-section`

**缺失功能**:

1. 评论列表获取（接口已存在：`comments(BlogCommentDTO)`）
2. 评论发送功能（需要新增接口：`/blog/comment/add`）

**当前状态**:

```vue
<div class="comment-list">
  <p class="comment-empty">暂无评论，快来抢沙发吧~</p>
</div>
```

**需要实现**:

1. 点击"评论"按钮展开评论区时，调用 `blogStore.fetchComments(blogId)` 获取评论列表
2. 实现评论发送功能
3. 显示评论内容（用户头像、昵称、评论内容、时间）
4. 支持回复评论（二级评论）

**建议新增接口**:

```typescript
// 发送评论
interface BlogCommentAddDTO {
  blogId: number
  parentId?: number // 父评论ID，回复评论时使用
  content: string
}

// POST /blog/comment/add
function addComment(body: BlogCommentAddDTO): Promise<ResultBlogCommentVO>
```

---

## 五、点赞和收藏状态

### 5.1 用户是否已点赞/收藏

**位置**: `HomeView.vue` - 博客项 `.blog-actions`

**缺失数据**: 当前用户对每篇博客的点赞和收藏状态

**当前实现**:

- 只显示点赞数和收藏数
- 点击后调用切换接口
- 接口返回包含 `haveBean` 字段（已点赞/收藏状态）

**需要优化**:

1. 在博客列表接口中返回当前用户的点赞和收藏状态
2. 或者新增批量查询接口：`/blog/user-actions`
3. 根据状态改变按钮样式（如：已点赞显示为实心图标）

**建议后端优化**:

```typescript
interface BlogsVO {
  // ...existing fields...
  isLiked?: boolean // 当前用户是否已点赞
  isCollected?: boolean // 当前用户是否已收藏
}
```

---

## 六、聊天功能

### 6.1 在线聊天室

**位置**: `HomeView.vue` - 右侧面板 `.chat-card`

**缺失接口**: WebSocket 聊天接口或 HTTP 轮询接口

**当前状态**:

```vue
<div class="chat-messages">
  <div class="chat-message system">
    <p>欢迎来到千语壁纸社区聊天室！</p>
  </div>
</div>
```

**需要实现**:

1. WebSocket 连接管理
2. 消息发送和接收
3. 在线用户列表
4. 消息历史记录
5. 消息通知

**建议实现方案**:

- WebSocket 端点：`ws://localhost:8080/chat`
- 或使用 HTTP 长轮询：`GET /chat/messages?lastId=xxx`

---

## 七、图片瀑布流

### 7.1 博客图片展示

**位置**: `HomeView.vue` - 博客内容区 `.blog-images`

**当前实现**:

- 使用 CSS Grid 实现基础瀑布流
- `grid-template-columns: repeat(auto-fill, minmax(200px, 1fr))`

**可能的优化**:

1. 使用真实的瀑布流库（如 `vue-waterfall-plugin-next`）
2. 懒加载图片
3. 图片预览功能（点击放大）
4. 图片加载失败处理

**建议**:

- 当前实现已足够基础使用
- 后续可根据需求优化

---

## 八、分页功能

### 8.1 博客列表分页

**位置**: `stores/blog.ts` - `fetchBlogList` 方法

**当前实现**:

```typescript
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
```

**缺失UI**:

- 页面底部没有分页组件
- 需要添加"加载更多"按钮或分页器

**建议实现**:

1. 方案A：无限滚动（滚动到底部自动加载下一页）
2. 方案B："加载更多"按钮
3. 方案C：传统分页器（Element Plus Pagination）

---

## 九、数据mock建议

### 9.1 开发阶段Mock方案

**推荐工具**:

- Mock.js
- json-server
- MSW (Mock Service Worker)

**示例Mock数据**（`src/mock/data.ts`）:

```typescript
export const mockUserStats = {
  postCount: 128,
  likeCount: 3456,
  collectCount: 892,
}

export const mockRankingList = [
  // ...existing data
]

export const mockComments = [
  {
    id: 1,
    userId: 1,
    username: '热心网友',
    userAvatar: '',
    createTime: '2025-10-30 10:30:00',
    content: '这壁纸太棒了！',
    parentId: 0,
  },
]
```

---

## 十、检查清单总结

### ✅ 已实现（使用静态数据）

- [x] 用户基本信息展示
- [x] 用户统计数据（发布/点赞/收藏数）
- [x] 博客列表展示（接口已对接）
- [x] 点赞功能（接口已对接）
- [x] 收藏功能（接口已对接）
- [x] 排行榜展示（静态数据）
- [x] 右侧面板切换（排行榜/聊天框）

### ⏳ 待对接真实接口

- [ ] 用户统计信息接口
- [ ] 排行榜接口
- [ ] 评论列表展示
- [ ] 评论发送功能
- [ ] 聊天功能（WebSocket）
- [ ] 分页UI组件
- [ ] 图片预览功能
- [ ] 用户点赞/收藏状态显示

### 🔧 建议优化项

- [ ] 无限滚动或"加载更多"
- [ ] 图片懒加载
- [ ] 评论二级回复
- [ ] 实时通知（点赞、评论、关注）
- [ ] 搜索和筛选功能
- [ ] 移动端响应式优化

---

## 十一、API接口补充建议

### 11.1 需要后端新增的接口

#### 1. 用户统计信息

```
GET /user/stats?userId={userId}
返回: ResultUserStatsVO
```

#### 2. 排行榜

```
GET /user/ranking?limit=8&timeRange=7d&sortBy=recentShareCount
返回: ResultListRankingUserVO
```

#### 3. 发送评论

```
POST /blog/comment/add
Body: BlogCommentAddDTO
返回: ResultBlogCommentVO
```

#### 4. 用户操作状态批量查询

```
POST /blog/user-actions
Body: { blogIds: number[] }
返回: ResultMapLongUserBlogActionVO
```

#### 5. 分享功能

```
POST /blog/share
Body: { blogId: number, platform?: string }
返回: ResultShareVO
```

---

## 十二、前端Store补充

### 12.1 可能需要的额外Store

#### 1. 统计数据Store

```typescript
// stores/stats.ts
export const useStatsStore = defineStore('stats', () => {
  const userStats = ref<UserStatsVO | null>(null)
  const fetchUserStats = async (userId: number) => {
    /* ... */
  }
  return { userStats, fetchUserStats }
})
```

#### 2. 排行榜Store

```typescript
// stores/ranking.ts
export const useRankingStore = defineStore('ranking', () => {
  const rankingList = ref<RankingUserVO[]>([])
  const fetchRanking = async (params: RankingParams) => {
    /* ... */
  }
  return { rankingList, fetchRanking }
})
```

#### 3. 聊天Store

```typescript
// stores/chat.ts
export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const sendMessage = async (content: string) => {
    /* ... */
  }
  return { messages, sendMessage }
})
```

---

**最后更新**: 2025年10月30日  
**维护者**: GitHub Copilot  
**状态**: 开发中 🚧
