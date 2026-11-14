## UI 0.1 联调 · 2026-11-14

- 验证真实注册、登录、JWT恢复、退出与学习入口；真实诊断初始化后五主题画像成功。
- 新账户未初始化画像502作为既有后端边界记录，不修改后端。
- 按路由懒加载，补充真实/模拟回归脚本与持久截图来源说明。

## UI 0.1 · 2026-11-13（本地待验收）

- 新增中性设计 tokens、住宅科普轮播/详情与合体登录首页，统一注册页样式。
- 保留真实认证与学习路由，增加表单提交防护、键盘焦点与图片降级。
- 当前使用标注占位素材；真实后端联调和最终摄影待完成。

## 文档治理 · 2026-11-13（不改变代码版本）

- 建立导航、当前架构证据与维护规则；保留历史正文，导入产品原稿并列出缺失材料。

# Changelog


## v0.3.0

### Added

- Vue 3 + TypeScript 前端
- 用户注册、登录和学习中心页面
- 十题初始诊断流程
- 五主题学习画像可视化
- 自适应训练与即时反馈
- 五题阶段测评流程

### Improved

- 打通前端与后端学习业务闭环
- 展示个性化训练推荐
- 统一处理受保护接口的 401 认证失效
- 完善项目启动和交付说明

### Architecture

- 保留模块化单体架构
- 复用 User / Training / Profile / Plan 模块
- 暂不引入分布式中间件

---

## v0.2.0

### Added

- Username/password registration and login
- BCrypt password hashing
- JWT access-token authentication
- Spring Security stateless authentication
- LEARNER / CONTENT_EDITOR / ADMIN roles
- Authenticated CurrentUser context
- Diagnosis and assessment session ownership validation

### Changed

- Learner-facing APIs no longer trust client-provided userId
- Diagnosis, training, profile and assessment flows use authenticated identity

### Fixed

- Concurrent duplicate username registration returns 409 Conflict
- Unlisted endpoints default to authenticated access

---

## v0.1.0

### Added

- Modular monolith MVP
- Initial diagnosis workflow
- Five-topic Mastery profile
- Weak-topic adaptive training
- Learning feedback and learning cards
- Stage assessment workflow
- User / Training / Profile / Plan module boundaries