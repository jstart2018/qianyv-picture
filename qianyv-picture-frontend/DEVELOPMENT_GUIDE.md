# 开发与调试指南（Development Guide）

说明：汇总开发环境、调试与架构相关的简短指南，便于新加入的开发者快速上手。

主要内容：

- 项目启动
  - 使用 Vite 进行开发，运行 `npm install` 安装依赖，`npm run dev` 启动（详见 `README.md`）。

- API 与请求规范
  - 前端使用 Axios（带有响应拦截器）进行 API 调用，注意处理身份未登录（code === 40100）以及对返回数据进行规范化（将可安全转换的数字字符串转换为数字，但保留 ID 字段为字符串以避免精度丢失）。

- 代码结构
  - `src/api/`：后端接口调用封装  
  - `src/components/`：复用组件（如 `AvatarPreview.vue`, `BlogUploadModal.vue` 等）  
  - `src/stores/`：Pinia/Vuex 等全局状态管理  
  - `src/views/`：页面视图

- 调试与性能
  - 使用浏览器 DevTools 分析动画与渲染；使用 CSS 的 transform/opacity 做动画以开启 GPU 加速；避免在主线程大量同步计算。

- 文档与变更记录
  - 将功能说明、修复记录与快速参考合并到 `docs_backup/` 的三个文档中（FEATURES_SUMMARY.md, FIXES_AND_QUICK_REF.md, DEVELOPMENT_GUIDE.md）。原始文档保存在 `docs_backup/` 目录，便于手动删除或审阅。

---
如需我把 `PROJECT_GUIDE.md` 的关键信息合并进 `README.md`，我可以继续执行。

# 功能摘要（Features Summary）

说明：汇总项目中关于用户功能的简短说明，便于快速回顾。原始详情已归档为历史记录（若需查看原文，请在项目根搜索对应文件名）。

主要功能要点：

- 博客/图片上传（BLOG_UPLOAD_*）
  - 上传表单、预览、样式与间距调整；支持图片预览与提交流程。

- 头像与预览（AVATAR_PREVIEW_*）
  - 头像上传/预览组件，支持点击预览、移除与更新流程；包含交互边界与常见问题说明。

- 草稿保存（DRAFT_SAVE_*）
  - 自动/手动保存草稿逻辑，恢复机制与冲突处理原则。

- 标签与展示（TAGS_DISPLAY_FEATURE.md）
  - 标签渲染规则、优先级与折叠展示行为。

- 图片布局模式（横/竖屏切换）
  - `DELAYED_LAYOUT_SWITCH.md`：双变量（目标类型 pictureType 与 显示类型 displayPictureType）实现无闪烁的布局切换（旧图淡出 → 切换布局 → 新图淡入）。
  - 过渡时长与 UX 说明（旧图淡出 0.3s、布局切换 0.05s、新图淡入 0.4s）。

- 图片详情与过渡（PICTURE_TRANSITION_* / PICTURE_DETAIL_*）
  - 详情路由重构、页面过渡动画与简化实现方案；保持路由状态与动画连贯性。

- 其他交互（搜索清空、触发器、加载更多）
  - 快速交互说明：搜索清空、触发搜索按钮、加载更多为直接追加不触发过渡。

---
更新策略：保留最小必要描述，详细实现请查看历史文档。

# 修复与快速参考（Fixes & Quick Ref）

说明：汇总项目中各类 bug 修复记录与快速参考提示，重点列出问题与修复思路，便于快速定位历史问题。

主要项：

- 搜索与滚动修复
  - 搜索按钮触发与清空行为修正；滚动定位与性能优化要点。

- 分类过滤（CATEGORY_FILTER_*）
  - 分类切换流程修复，确保请求参数与 UI 状态一致。

- 路由与子路由（PICTURE_SUBROUTE_*、PICTURE_DETAIL_*）
  - 子路由重构导致的状态同步问题修复，保留快速恢复步骤。

- ID 精度与 API 数据处理（ID_PRECISION_*、COLLECT_API_PRECISION_FIX.md）
  - 后端返回的大整数 ID 保持字符串以避免精度丢失；前端接收与展示策略。

- Avatar 与图片移除/更新修复
  - 更新/删除时的状态同步、预览消失与缓存问题处理。

- 布局与样式小修（LEFT_PANEL_*、HORIZONTAL_* 系列）
  - 侧边栏与横向详情修复要点。

快速参考（常见命令/注意事项）：
- 加载更多时避免触发过渡动画；直接追加数据。  
- 当出现 ID 精度问题时，优先保持字符串并在需要计算时使用 BigInt 或后端处理。

---
说明：此文件为快速定位历史问题与修复思路的索引，详细补丁请查看历史文档。
