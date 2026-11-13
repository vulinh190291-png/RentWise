# RentWise 维护规则

> Document version: v1.0
> Status: Draft
> Implementation: Not applicable
> Baseline: v0.4 文档治理规则 v1.0
> Owner: TBD
> Updated: 2026-11-13
> Source: 用户授权范围与项目文档治理指南

- 先读根 README.md、docs/README.md，再读对应模块资料。
- Status 与 Implementation 分开：Draft 不是开发验收要求；Implemented 需代码和验证证据及提交基线，不确定项标 Unverified。
- 当前 Maven 模块 learning-service，注册名 training-service，不得擅自改名。实际架构见 docs/architecture/current/v0.4.md。
- Explore 不是房源搜索/交易；Living Picks 不做购物车、支付、订单；不得应用已否定的绿色插画首页方案。
- 代码任务先确认 API 和相关测试；文档任务不修改业务代码、数据库、依赖或服务配置。
- 新文档同步索引；移动已跟踪文件用 git mv；核查链接、图片及大小写，保留历史原文。
- 用户未要求不改课程交付产物及外部文件，不删除用途不明的资料。
- 完成文档变更后运行 git diff --check，核对变更范围及历史完整性；仅文档变更不要求全量业务测试。
