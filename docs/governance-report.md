# 文档治理与迁移验收报告

> Document version: v1.0
> Status: Draft
> Implementation: Not applicable
> Baseline: v0.4 / 25e67be3b43dc2dae35af57bd0da94acd86ecec9
> Owner: TBD
> Updated: 2026-11-13
> Source: 本地仓库、配置、源码与工作目录原稿

## Phase 0 审计
真实仓库位于工作目录的 `项目实施/RentWise`。原分支 feat/v0.4-microservices，工作区干净；创建 chore/docs-governance。本地 main 仍包含 src 单体结构，未切换或改动。未联网刷新远程。
读取 README、CHANGELOG、架构/API/MVP，枚举文档与授权工作目录资料（排除依赖与构建目录），核对配置、Controller 和 RemoteProfileFacade。未运行服务或业务测试。

## Phase 1–3 变更表
| 原位置 | 处理/目标 |
| --- | --- |
| README.md、CHANGELOG.md | 保留正文，增加导航/治理事件 |
| docs/architecture.md、docs/api.md、docs/product/mvp.md | 原位保留正文，添加历史提示 |
| 工作目录 RentWise_Explore_v0.1.md | 原件保留，正文导入 docs/product/explore/overview.md |
| 工作目录 RentWise_Living_Picks_模块初步构想_v0.1.docx | 原件保留，正文和表格转入 docs/product/explore/living-picks.md；不保留 Word 版式，无嵌入媒体 |
| 新增 | docs/README.md、docs/architecture/current/v0.4.md、AGENTS.md、本报告 |

无已跟踪文件移动，无删除，因此未使用 git mv。产品稿明确为 Draft / Not started 并互链。

## 保留与未处理清单
- docs/versions 四份历史文档、docs/assets 三张图片原位保留，不作为当前 UI 基线，图片授权待核对。
- 独立 UI/UX 规范和 v1.0 目标架构、服务清单、API/DB、阶段验收原稿未找到，待导入；未制造占位原稿。Explore 中的视觉方向不能代替完整规范。
- 工作目录 `03-教育场景-需求文档.docx`、`RentWise_调研笔记_v1.docx`、`RentWise_选题报告_v1.docx`、`RentWise_MVP最小需求清单_v1.docx` 保留，课程用途和版本待确认。
- `RentWise_Explore_v0.1说明.docx` 保留作导出参考，未认定与 Markdown 完全相同。
- `用户故事地图.png`、`用户原型1.png`、`用户原型2.png` 保留，用途/重复关系/授权待确认。
- `项目实施/README_backup_v03.md`、`项目实施/RentWise_初代作品源码_v0.4.zip` 保留，不解包、不移动、不覆盖。
- 治理指南原件保留，未扫描授权目录以外私人文件。

## Phase 4 验收
历史文档对比 Git HEAD 时归一化 CRLF/LF；三份旧正文完整保留，四份版本文档内容一致，三张图片字节一致。仅 Markdown 改动，不修改业务代码、配置、SQL、图片或交付包。
根入口、索引、历史区分、产品互链、维护规则已建立。UI/UX 独立规范与目标设计缺失，整体材料验收为部分完成，不能宣称全部齐备。
本轮未运行 Maven/npm，当前服务运行状态仍待复验。未提交、推送或创建 PR。

[文档导航](README.md)

静态检查：37 个本地链接/图片路径存在且大小写匹配，0 错误；git diff --check 通过。历史保留核验 7 个文件通过。外部 URL 与页面锚点未联网验证。

## 第二轮复核（2026-11-13）
开始分支 chore/docs-governance，HEAD 25e67be3b43dc2dae35af57bd0da94acd86ecec9，保留上一轮未提交文档；未提交、推送或合并。

| 本轮文件 | 变化 |
| --- | --- |
| docs/engineering/api-v0.4.md | 新建：11 个业务与 3 个内部接口、DTO、认证、requestId、SYNCED/PENDING、错误与运行验证边界 |
| docs/external-imports.md | 新建：UI/UX、v1.0 架构/服务/调用、API/DB、课程验收及图片待导入条件 |
| docs/architecture/current/v0.4.md | 原有配置描述一致；追加本地画像模式、测试局限、同步重试及当前 API 链接 |
| docs/README.md | 增加当前 API 和外部清单索引 |
| docs/governance-report.md | 保留此前内容，追加本轮记录 |

产品稿核验通过：Explore 完整包含原 Markdown 正文；Living Picks 的 364 段 Word w:t 文本按顺序保留，表格转为 Markdown，不声称保留 Word 版式。两份均正确标为 Draft / Not started，本轮未改写。
历史 API 保留原位；新 API 所列接口均存在源码实现，运行均为 Unverified。重要差异是训练答题必填 UUID requestId、同步状态与空画像响应、内部接口不使用 ApiResponse 包装。

剩余问题：独立 UI/UX 与 v1.0 目标原稿仍缺失；课程版本、图片许可待确认。远程画像、Gateway/Nacos、重试故障恢复尚未联调，本轮未运行业务测试或 HTTP 请求。未修改业务代码、配置、数据库或交付物，未删除历史文件。外部资料只核对授权工作目录，不编造缺失正文。

最终静态验收：92 个本地链接/图片路径存在且大小写匹配，14 个 Controller 路由覆盖通过；git diff --check 通过，跟踪差异仅 Markdown。外部 URL 与页面锚点未验证。

## 最终可提交性检查（2026-11-13）

结论：**当前文档治理变更达到可提交状态**，适合作为独立文档提交。此结论只覆盖本地文档质量、源码静态一致性和历史保留；不代表产品开发、运行联调或缺失设计原稿已完成。尚未暂存、提交、推送或创建 PR。

检查覆盖 Git 已跟踪修改和未跟踪新增共 13 个 Markdown 文件，未依赖 git diff 单独判断新增文件质量。核查 UTF-8、冲突标记、围栏闭合、新文档元数据、相对路径与大小写。Markdown 原稿中的双空格换行按语法保留。

本轮必要修正：AGENTS.md 补充状态/版本/来源；external-imports.md 修正误用的 Source 元数据；历史 api.md 增加当前 API 直达链接；当前架构补充本地 ProfileService 的 matchIfMissing 条件。其余既有未提交内容保留。

### 建议提交的完整文件清单
| Git 状态 | 文件 | 用途 |
| --- | --- | --- |
| 修改 | CHANGELOG.md | 文档治理记录 |
| 修改 | README.md | 根导航入口 |
| 修改 | docs/api.md | 历史 API 提示与当前 API 链接 |
| 修改 | docs/architecture.md | 历史架构提示 |
| 修改 | docs/product/mvp.md | 历史 MVP 提示 |
| 新增（未跟踪） | AGENTS.md | 长期维护规则 |
| 新增（未跟踪） | docs/README.md | 分类索引 |
| 新增（未跟踪） | docs/architecture/current/v0.4.md | 当前源码架构与验证边界 |
| 新增（未跟踪） | docs/engineering/api-v0.4.md | 当前 API 与 DTO 说明 |
| 新增（未跟踪） | docs/external-imports.md | 外部原稿待导入清单 |
| 新增（未跟踪） | docs/governance-report.md | 审计、迁移与验收结果 |
| 新增（未跟踪） | docs/product/explore/overview.md | Explore Draft 原稿 |
| 新增（未跟踪） | docs/product/explore/living-picks.md | Living Picks Draft 原稿文字与表格 |

### 已完成与遗留事项
- 父 POM 的三模块、Java 17、Spring Boot 3.5.16 与架构一致；服务端口、注册名、Gateway 路由、数据库默认值、固定 URL、远程/本地模式及测试配置已核对。
- 14 个 Controller 路由全部被 API 文档覆盖；23 个 DTO record 声明与当前源码一致。接口源码实现和运行 Unverified 分开标注。
- Explore 原 Markdown 主体完整；Living Picks 364 段 Word 文本顺序完整；两稿 Draft / Not started 保持不变。
- 3 份历史正文完整保留，4 份版本文档与 3 张历史图片保持原内容。未删除或迁移文件。
- UI/UX 独立规范、v1.0 目标架构/API/DB/验收原稿仍待提供；课程版本与图片授权待确认。缺失项已明确，不阻塞本次文档治理提交，但不能据此宣布全套项目资料齐备。
- 未运行 Maven/npm 或 HTTP 联调；外部 URL 未联网验证，不以文档检查替代运行验收。

最终验证：93 个本地 Markdown 链接/图片引用存在且大小写匹配；未发现需要单独核验的本地锚点引用。git diff --check 通过（仅涵盖跟踪差异），8 个未跟踪文件另行完成上述静态检查。本轮前后 SHA-256 对比仅上述五份必要修正文档发生变化；业务代码、配置及其他未提交文档保持原样。

## 本地资料库最终整理（2026-11-13）

本节是最新状态，此前“原位保留”“未提交”等表述保留为各轮历史记录。

- 审计时旧 chore/docs-governance 分支干净，提交 36228f7；GitHub PR #8 已合并到 feat/v0.4-microservices，合并提交 52212db31073c6facc15549c0610ecd567986bf0。
- 初次 fetch 的 Windows schannel 凭据后端失败；使用单次 http.sslBackend=openssl 后成功，无全局配置改动。开发分支仅 fast-forward，同步后创建 chore/docs-library-finalization；未改 main、未改写历史、未重复创建 PR。
- 13 个外部文件按内容分类移动，原路径/目标与分类理由见[本地资料库](local-library.md)。目标不存在才移动，13 个 SHA-256 全部保持；未解包/重打包 ZIP。总目录新增 00_资料目录.md 与资料迁移校验清单_2026-11-13.json，未创建外层 Git 仓库。
- 三张 PNG 与仓库历史图完全相同，保留两份，不删除、不增加 Git 图片。Word 与 Markdown 不视为逐字节重复。历史 README、治理指导书归历史文档。
- 本轮 Git 新增 docs/local-library.md；修改 AGENTS.md、docs/README.md、docs/external-imports.md、docs/architecture/current/v0.4.md、docs/engineering/api-v0.4.md、docs/governance-report.md、两份 Explore/Living Picks Markdown：补充文档版本、资料来源位置、维护规则和最新治理状态。
- 未导入课程/研究 Word、ZIP、原型副本：分别属于本地原稿、交付快照或已有重复历史资源。未确认早期材料为当前 Approved 需求，不额外制造需求文档。
- UI/UX 独立规范与 v1.0 目标架构/API/DB 原稿仍缺失。后续由用户提供可信原稿、确认课程交付批次及素材对外授权；推送/PR 需另行授权。
- 本轮仅文档和外部文件路径整理，不执行业务测试；既有架构/API 仍以源码静态核验为准，不升级为运行 Verified。
- 允许检查通过后创建本地 Commit；本报告随该提交保存，提交 SHA 以 Git 日志与最终交付回复为准（避免在提交内部自引用 SHA）。未推送、未创建或合并 PR。

最终验收：13 个移动文件 SHA-256 一致；114 个仓库及本地索引链接/图片路径存在且大小写匹配；本轮仅 9 个 Markdown 文件变更，无二进制导入。新增内容检查未发现常见密钥模式，未修改代码或配置。历史文档 docs/versions/v0.1/implementation.md, docs/versions/v0.2/auth-security-implementation.md 含既有 Unicode 替代字符，已确认与 HEAD 一致，未猜测修复；后续需可信原稿核对。外部 URL 未逐项联网验证。diff --check 与 cached --check 通过后创建本地提交。
