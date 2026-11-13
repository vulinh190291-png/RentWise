# 本地资料库与 Git 文档维护

> Document version: v1.0
> Status: Draft
> Implementation: Not applicable
> Baseline: v0.4 / 52212db31073c6facc15549c0610ecd567986bf0
> Updated: 2026-11-13
> Source: 授权工作目录实际文件、内容抽查与 SHA-256 校验；用户资料管理规则

## 分层管理

Git 仓库位于项目总目录的 `项目实施/RentWise`；总目录由 `00_资料目录.md` 导航，路径为本地说明，不创建无法在 GitHub 打开的跨仓库链接。

```text
RentWise/
├── 00_资料目录.md
├── 资料迁移校验清单_2026-11-13.json
├── 项目实施/
│   └── RentWise/                 # 唯一 Git 仓库
├── 01_产品资料/
│   ├── 初期需求/                # MVP Word 原稿
│   ├── 市场调研/                # 早期调研笔记
│   ├── 产品构想/                # Explore Markdown/Word、Living Picks Word
│   └── 历史文档/                # 旧 README、治理指导书
├── 02_设计素材/
│   └── 原型草图/                # 三张历史 PNG
├── 03_课程材料/
│   ├── 课程需求/                # 教育场景通用要求
│   └── 选题报告/                # 项目报告原件
└── 04_版本交付/
    └── v0.4/                    # 原始源码 ZIP
```

| 原路径（相对项目总目录） | 新路径 |
| --- | --- |
| `03-教育场景-需求文档.docx` | `03_课程材料/课程需求/03-教育场景-需求文档.docx` |
| `RentWise_MVP最小需求清单_v1.docx` | `01_产品资料/初期需求/RentWise_MVP最小需求清单_v1.docx` |
| `RentWise_调研笔记_v1.docx` | `01_产品资料/市场调研/RentWise_调研笔记_v1.docx` |
| `RentWise_选题报告_v1.docx` | `03_课程材料/选题报告/RentWise_选题报告_v1.docx` |
| `RentWise_Explore_v0.1.md` | `01_产品资料/产品构想/RentWise_Explore_v0.1.md` |
| `RentWise_Explore_v0.1说明.docx` | `01_产品资料/产品构想/RentWise_Explore_v0.1说明.docx` |
| `RentWise_Living_Picks_模块初步构想_v0.1.docx` | `01_产品资料/产品构想/RentWise_Living_Picks_模块初步构想_v0.1.docx` |
| `用户故事地图.png` | `02_设计素材/原型草图/用户故事地图.png` |
| `用户原型1.png` | `02_设计素材/原型草图/用户原型1.png` |
| `用户原型2.png` | `02_设计素材/原型草图/用户原型2.png` |
| `项目实施/RentWise_初代作品源码_v0.4.zip` | `04_版本交付/v0.4/RentWise_初代作品源码_v0.4.zip` |
| `项目实施/README_backup_v03.md` | `01_产品资料/历史文档/README_backup_v03.md` |
| `RentWise_Codex_项目文档治理与迁移指南_v1.0.md` | `01_产品资料/历史文档/RentWise_Codex_项目文档治理与迁移指南_v1.0.md` |

## 分类依据与重复资料
教育场景 Word 正文为课程通用要求，归课程需求；MVP Word 为早期最小功能范围，归初期需求；调研笔记为早期研究（仍含待填、未访谈说明），归市场调研；选题报告归课程报告。Explore 两种格式及 Living Picks 为规划稿；不转成已实现要求。

外层用户故事地图、用户原型1、用户原型2与仓库 user-story-map.png、wireframe-main.png、wireframe-flow.png 的 SHA-256 分别相同，保留本地原件与仓库历史引用，未重复导入。Explore 的 Markdown 与 Word 是不同格式版本，不据文件名判定完全相同；Word 留本地，仓库 Markdown 作为维护主稿。

## Git 纳入边界
- 纳入：当前架构/API、经确认的需求/设计、产品 Draft Markdown、具有使用许可且被文档引用的必要图片。
- 默认留本地：课程 Word/PDF、研究原稿、导出副本、交付 ZIP、外部参考图。当前早期需求/报告未确认为最新批准需求，本轮不导入。
- 缺失的 UI/UX 和 v1.0 原稿继续见[待导入清单](external-imports.md)。不根据引用、课程要求或愿景生成假正文。
- 新正式 Markdown 填标题、Document version、Status、Implementation、Baseline、Updated、Source；文档版本独立于项目版本。Unverified 表示尚未核实的事实，Approved 不代表已经实现。
- 外部文件迁移记录保留原路径、目标路径和 SHA-256；不删除同名内容，不覆盖不确定版本。变更先检查 Git 状态，文档用独立分支，提交前核查新增文件和链接。

[文档导航](README.md) · [治理报告](governance-report.md)
