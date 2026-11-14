# Explore 0.1 前端实施与验收

> Document version: 0.1
> Status: Draft（待用户视觉及内容评审）
> Implementation: Partial（城市列表与详情；地图未接入）
> Project version: v0.4 / Explore 0.1
> Baseline: ffc2bd09c0afba08cb59d10ed2873b2466b6eb5a
> Branch: feat/explore-city-map
> Updated: 2026-11-14
> Source: PR #11 合并状态、当前源码、官方内容来源与本机 Edge 回归

## 基线与实际交付

GitHub 已确认 PR #11 merged；fetch 后首页精修提交 8a71249 是远程开发基线祖先。本地开发基线及既有 Explore 分支均为 ffc2bd0，工作区起始干净，直接使用该独立分支。没有重新创建、覆盖分支或修改历史。

- 公开懒加载 `/explore`，首页与 Dashboard 提供入口；学习入口使用既有受保护路由。
- 八个城市的中文名/拼音/住宅关键词搜索、选择、地理气候/文化/历史住宅/编辑租住核对问题与来源。
- 桌面列表与侧边详情；手机列表进入详情，返回恢复原按钮焦点；键盘 Tab/Enter/Space 与 Escape 支持。
- 静态 JSON 按需请求、结构与重复 ID 校验、加载/空集合/无结果/错误/重试状态；12 秒超时反馈，卸载取消请求。
- 原照片复用为明确标注的非城市摄影参考，无新增外部图片、地图数据或依赖。

地图授权、服务 Key 和部署条件未确认，按用户授权执行列表降级方案。地图画布、节点、缩放、真实地图/列表切换都未实现，不能将接入说明视作地图验收通过。详见[地图技术验证](../engineering/explore-map-evaluation.md)。

## 已验证

| 检查 | 结果与证据 |
| --- | --- |
| TypeScript / 生产构建 | `npm --prefix frontend run build` 内含 `vue-tsc -b`，通过；Explore 独立懒加载 chunk 约 8.2 kB，主包仍超 500 kB，既有警告未消除 |
| Explore 回归 | `frontend/tests/explore.mjs`：八城市选择/来源、大小写与首尾空格搜索、无结果恢复、中文关键词、键盘焦点与 Escape、地图说明、加载、503、无效 JSON、重复 ID、空集合、重试、图片失败及减少运动环境通过 |
| 跨断点回归 | 独立审查实际复现“桌面选中→无结果→手机后列表被隐藏”；新增用例先失败，再通过搜索变化退出详情修复 |
| 响应式 | Edge 模拟 1440×900、1024×768、390×844、320×740；列表与详情均无横向溢出；持久保存截图 |
| 原首页 | `ui-home.mjs` 与 `ui-home-polish.mjs` 通过，包含轮播、弹窗焦点、表单校验、模拟登录注册、重复提交、守卫、五视口和桌面完整表单 |
| 画像契约 | 同一 5174 端口执行 `profile-state.mjs`，Dashboard/Results 各四状态共 8 检查通过；模拟 HTTP，不是真实后端联调 |
| 业务隔离 | Login/Dashboard 的 script setup 与基线一致；认证守卫不变；Java、数据库、服务配置、依赖与锁文件无修改 |
| Git / 链接 | `git diff --check` 与本轮 Markdown 相对路径检查通过；最终提交前检查暂存文件范围 |

## 真实截图

持久目录：外层项目 `04_版本交付/Explore-0.1/验收/2026-11-14/`。使用真实 Edge 渲染本地 5174 页面，不是设计稿。

- `explore-1440.png`、`explore-1024.png`：桌面列表/默认详情。
- `explore-390.png`、`explore-320.png`：手机列表。
- `explore-detail-1440.png`、`explore-detail-1024.png`、`explore-detail-390.png`、`explore-detail-320.png`：选择哈尔滨后的详情。
- 目录同时可能包含本轮原首页回归截图与测量文件；不纳入 Git。

桌面和手机截图已视觉复核。首轮发现手机标题孤字断行，调整为两行后重拍。截图中的通用庭院图不代表任何中国城市的实际建筑，页面已经标注用途与作者。

## 未验证及遗留

- 未安装地图 SDK、没有真实地图或移动地图性能结果；全国站天地图文档请求超时。后续需项目服务授权/Key、部署域名与地图发布核验。
- 未执行新的真实 MySQL 注册、训练或阶段测评写入。原业务源码不变、模拟契约及导航回归通过，不等于整个后端闭环重新验收；既往真实证据仍在[画像修复报告](../engineering/profile-502-investigation.md)。部分画像缺失与未初始化共用 404 的既有边界保留。
- 未做 Safari/Firefox、实体手机、完整屏幕阅读器验收。12 秒超时与离开时取消为源码检查；网络失败/重试由浏览器回归覆盖。
- 内容为首批样本，气象背景部分来源页面含旧统计，已经排除数值统计与旧行政信息；仍建议后续补充新的城市官方概况。各城专属授权摄影尚缺。
- 长期 Explore 构想、世界地图、Living Picks、房源搜索、交易与租金预测均不在本轮范围。

## 完整变更文件

新增：`frontend/public/data/explore-cities.json`、`frontend/src/data/explore.ts`、`frontend/src/views/explore/Explore.vue`、`frontend/tests/explore.mjs`、本报告、`docs/product/explore/city-sources.md`、`docs/engineering/explore-map-evaluation.md`、`docs/engineering/explore-0.1-plan.md`。

修改：`frontend/src/router/index.ts`（新增公开路由）、`frontend/src/views/auth/Login.vue`（导航入口及手机显示）、`frontend/src/views/dashboard/Dashboard.vue`（入口）、`docs/README.md`、`docs/product/explore/overview.md`（保留原稿，增加实施状态）。

本轮仅允许本地提交；未推送、未创建 PR。提交 SHA 见 Git 日志及本轮交付。

[导航](../README.md) · [内容来源](../product/explore/city-sources.md) · [实施计划](../engineering/explore-0.1-plan.md)
