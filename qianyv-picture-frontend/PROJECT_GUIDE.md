# 千语壁纸 - 项目开发指南

## 📝 项目概述

千语壁纸是一个集博客、图片素材和团队空间功能于一体的Web应用系统。采用Vue3组合式API开发，界面采用青蓝色清新风格设计。

## ✨ 已完成功能

### 1. 首页布局

- **顶部导航栏**
  - 左侧：Logo + "千语壁纸"网站名称
  - 中间：三大路由导航（社区/图片素材/团队空间）
  - 右侧：用户头像或登录按钮
- **底部版权信息**
  - 展示版权声明

### 2. 用户认证系统

- **登录/注册页面** (`/login`)
  - 采用邮箱/手机号验证码方式
  - 验证码发送功能（60秒倒计时）
  - 登录注册共用同一接口
  - 美观的治愈系UI设计

- **用户状态管理**
  - 使用Pinia管理登录状态
  - 自动获取登录用户信息
  - 持久化会话（基于Cookie）

- **用户头像展示**
  - 已登录：显示头像（如无头像则显示首字母+随机渐变色）
  - 未登录：显示"登录/注册"按钮

- **用户下拉菜单**
  - 个人中心入口
  - 编辑信息入口
  - 退出登录功能

### 3. 路由系统

- `/` - 社区首页（待完善内容）
- `/login` - 登录/注册页
- `/pictures` - 图片素材页（占位）
- `/space` - 团队空间页（占位）
- `/user/:id` - 用户详情页（静态展示）

### 4. 技术栈

- **前端框架**: Vue 3 (组合式API)
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **语言**: TypeScript

## 🎨 设计特色

### 样式风格

- **色彩**: 青蓝色主题 (#1aa0c1, #7bd3d8)
- **风格**: 治愈系、清新、灵动
- **设计理念**: 简约现代、注重用户体验

### UI特点

- 圆角设计 (border-radius: 12-20px)
- 柔和阴影效果
- 渐变色按钮和头像
- 流畅的过渡动画
- 响应式布局

## 📁 项目结构

```
src/
├── api/                    # API接口定义
│   ├── userController.ts   # 用户相关接口
│   ├── blogController.ts   # 博客相关接口
│   ├── pictureController.ts # 图片相关接口
│   ├── spaceController.ts  # 空间相关接口
│   └── typings.d.ts        # TypeScript类型定义
├── stores/                 # Pinia状态管理
│   └── user.ts            # 用户状态Store
├── views/                  # 页面组件
│   ├── HomeView.vue       # 首页
│   ├── LoginView.vue      # 登录页
│   ├── PicturesView.vue   # 图片素材页
│   ├── SpaceView.vue      # 团队空间页
│   └── UserDetailView.vue # 用户详情页
├── router/                 # 路由配置
│   └── index.ts
├── App.vue                 # 根组件
├── main.ts                # 入口文件
└── request.ts             # Axios配置
```

## 🚀 开发指令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint

# 格式化代码
npm run format

# 生成API类型定义
npm run openapi
```

## 🔌 API接口说明

### 用户相关

- `POST /user/code` - 发送验证码
  - 参数: `{ email?: string, phone?: string }`
- `POST /user/login/code` - 验证码登录/注册
  - 参数: `{ emailOrPhone: string, code: string }`
- `GET /user/loginUser` - 获取当前登录用户信息
- `GET /user/logout` - 退出登录
- `PUT /user/{id}` - 编辑用户信息
  - 参数: `{ email?, phone?, nickname?, gender?, introduction? }`

## 📋 待开发功能

### 高优先级

1. **社区首页内容开发**
   - 博客列表展示
   - 推荐内容
   - 分类筛选
   - 搜索功能

2. **图片素材页面**
   - 图片瀑布流展示
   - 图片上传功能
   - 分类管理
   - 标签系统

3. **团队空间功能**
   - 空间创建与管理
   - 成员管理
   - 权限控制
   - 协作功能

4. **用户详情页完善**
   - 用户资料编辑
   - 个人作品展示
   - 收藏夹管理

### 中优先级

5. **博客功能**
   - 博客创建与编辑
   - 富文本编辑器
   - 图片插入
   - 评论系统
   - 点赞收藏

6. **搜索与筛选**
   - 全局搜索
   - 高级筛选
   - 标签云

### 低优先级

7. **其他功能**
   - 通知系统
   - 消息中心
   - 主题切换
   - 多语言支持

## 🛠 开发规范

### Vue 3 组合式API规范

- 使用 `<script setup>` 语法
- 使用 `ref` 和 `reactive` 管理状态
- 使用 `computed` 创建计算属性
- 使用 `watch` 监听数据变化
- 合理拆分组件，保持单一职责

### TypeScript规范

- 为所有变量和函数添加类型注解
- 充分利用API类型定义 (`API.xxx`)
- 避免使用 `any` 类型

### 样式规范

- 使用scoped样式避免污染
- 保持色彩一致性（青蓝色系）
- 注意响应式设计
- 适当使用CSS变量

## 🐛 注意事项

1. **API返回格式**: 所有API返回都包装在 `{ code, data, message }` 结构中
2. **认证方式**: 使用Cookie进行会话管理，请求时需设置 `withCredentials: true`
3. **路由守卫**: 部分页面可能需要登录后访问，注意处理未登录跳转
4. **错误处理**: 统一在request.ts中处理，注意40100状态码（未登录）

## 📞 联系方式

如有问题，请联系开发团队。

---

**千语壁纸** - 让创意触手可及 ✨
