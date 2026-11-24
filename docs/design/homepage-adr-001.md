# ADR-001｜首页由内容引导工具使用

> Version: 0.1
> Status: Approved（仅下列已确认决策）
> Implementation: Partial（首轮公共首页实现，规划功能不含在内）
> Project: RentWise v0.4 / Homepage v0.1
> Baseline: 9e55cda / feat/explore-integrated-v0.4
> Owner: TBD
> Updated: 2026-11-22
> Source: 用户2026-11-22明确确认及本地首页/路由核查

## 背景

现有UI 0.1/0.1.1以住宅展示与登录表单为落地页；Explore已增加城市内容与本地地图集成。用户确定以Editorial Lifestyle × Interactive Exploration为新首页方向，先激发兴趣再引导工具。此ADR只记录决策，不重复建立产品愿景。

## 已确认

- 杂志式内容首页，清晰阅读层次与内容到工具的路径。
- Hero左右分栏首屏。
- Explore、Living、Compare、Learn四业务布局。
- 五区顺序：Hero → Explore精选城市摄影 → Living居住灵感专题 → Compare/Learn工具入口 → Footer。
- 摄影必须来源及授权可核验；未实现模块明确规划状态。

## 尚未确认

精确字体、色值、动效与正式摄影；Hero两侧具体内容分配和比例；Living具体选题及Compare产品契约；公共首页路由实施方案。沿用旧页面token仅是兼容建议，不是这些细节已获得批准。

## 后果与取舍

新首页以内容为首屏重点，登录保留独立可达入口；复用已有Explore故事与Learn业务。Living/Compare未具备功能时不发布空路由或伪按钮。不能因四业务布局已确认就宣称四模块全部上线。旧首页规格保留为历史依据，新工作统一引用[唯一现行首页规格](homepage-v0.1.md)。首页设计批准不解除地图版权及公网部署待办，也不授权修改认证行为。

本轮只写文档，未执行路由迁移、UI实现、提交或远程操作。后续变更已确认方向时追加决策记录，不静默覆盖历史。

## 2026-11-22 实施决策追加

用户明确授权 `/` 为无需登录的公共首页，独立Home组件实施；保留/login与Dashboard保护。此追加取代上文公共路由待确认状态，不改变字体/摄影等细节仍待首轮评审的边界。实施与测试见[验收报告](homepage-v0.1-validation.md)。
