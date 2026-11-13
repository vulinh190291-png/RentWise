# RentWise 文档导航

> Document version: v1.0
> Status: Draft
> Implementation: Not applicable
> Baseline: v0.4 文档治理
> Owner: TBD
> Updated: 2026-11-13
> Source: 本地仓库与实际原稿

## 当前、目标与历史
先读[项目入口](../README.md)，再读当前架构。新文档统一填写 Status、Implementation、Baseline、Owner、Updated、Source。Status 使用 Draft / Approved / Implemented / Archived；Implementation 使用 Not started / Partial / Verified / Not applicable。未知事实标 Unverified；Approved 不等于已开发。

| 分类 | 入口 | Status / Implementation | Baseline |
| --- | --- | --- | --- |
| 当前 | [架构、代码与测试证据](architecture/current/v0.4.md) | Draft / Partial；运行待复验 | v0.4 |
| 当前 | [v0.4 API](engineering/api-v0.4.md) | Draft / Partial；源码已实现，运行 Unverified | v0.4 |
| 当前 | [运行说明](../README.md) | 既往验收说明，本轮未复验 | v0.4 |
| 产品 | [Explore](product/explore/overview.md) | Draft / Not started | proposal v0.1 |
| 产品 | [Living Picks](product/explore/living-picks.md) | Draft / Not started | proposal v0.1 |
| 历史 | [架构](architecture.md)、[API](api.md) | Archived / Not applicable | v0.3 |
| 历史 | [MVP](product/mvp.md) | Archived / Not applicable | v0.1 |
| 历史 | [设计](versions/v0.1/design.md)、[实现](versions/v0.1/implementation.md) | 历史索引分类 / Not applicable | v0.1 |
| 历史 | [认证设计](versions/v0.2/auth-security-design.md)、[认证实现](versions/v0.2/auth-security-implementation.md) | 历史索引分类 / Not applicable | v0.2 |
| 治理 | [迁移与验收报告](governance-report.md) | Draft / Not applicable | 2026-11-13 |

本轮新文档 Owner 为 TBD、Updated 为 2026-11-13，历史原始版本保持不变。

## 待导入与规划
- v1.0 目标架构、服务清单、API/DB 及分阶段验收原稿未找到，不制造假正文。
- 独立 UI/UX 规范未找到；Explore 内含 Apple-inspired 方向，但不替代完整规范。`docs/design/uiux-guidelines.md` 待原稿提供后导入。
- F07 情境模拟、F08 决策复盘、F09 学习路径是规划，不是已实现声明。
- 课程 Word、交付 ZIP 和外部图片已按用途归档至本地资料库，见报告；不创建空目录或占位 PRD。

## 历史图表
[故事地图](assets/user-story-map.png) · [流程原型](assets/wireframe-flow.png) · [主界面原型](assets/wireframe-main.png)。仅供追溯，不作为新 UI 基准；来源与授权待核对。

完整[外部资料待导入清单](external-imports.md)记录缺失原稿、目标路径与导入条件。

[本地资料库与维护规则](local-library.md)记录目录、移动表、主稿规则和 Git 纳入边界。
